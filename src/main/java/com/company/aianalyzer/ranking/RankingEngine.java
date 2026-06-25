package com.company.aianalyzer.ranking;

import com.company.aianalyzer.application.dto.DeveloperRankingDto;
import com.company.aianalyzer.application.DeveloperKeyNormalizer;
import com.company.aianalyzer.application.dto.GlobalDeveloperRankingDto;
import com.company.aianalyzer.config.AiRankingProperties;
import com.company.aianalyzer.domain.entity.CommitEntity;
import com.company.aianalyzer.domain.entity.DeveloperEntity;
import com.company.aianalyzer.domain.entity.ProjectTermStatEntity;
import com.company.aianalyzer.domain.entity.TermStatEntity;
import com.company.aianalyzer.domain.repository.BugAssignmentRepository;
import com.company.aianalyzer.domain.repository.BugHistoryEvidenceView;
import com.company.aianalyzer.domain.repository.BugReportRepository;
import com.company.aianalyzer.domain.repository.CommitRepository;
import com.company.aianalyzer.domain.repository.DeveloperRepository;
import com.company.aianalyzer.domain.repository.ProjectTermStatRepository;
import com.company.aianalyzer.domain.repository.TermStatRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RankingEngine {
    private final AiRankingProperties properties;
    private final DeveloperRepository developerRepository;
    private final BugReportRepository bugReportRepository;
    private final BugAssignmentRepository bugAssignmentRepository;
    private final CommitRepository commitRepository;
    private final TermStatRepository termStatRepository;
    private final ProjectTermStatRepository projectTermStatRepository;
    private final QueryExpansionService queryExpansionService;
    private final RecencyWeightCalculator recencyWeightCalculator;
    private final VectorCodec vectorCodec;

    public RankingEngine(AiRankingProperties properties,
                         DeveloperRepository developerRepository,
                         BugReportRepository bugReportRepository,
                         BugAssignmentRepository bugAssignmentRepository,
                         CommitRepository commitRepository,
                         TermStatRepository termStatRepository,
                         ProjectTermStatRepository projectTermStatRepository,
                         QueryExpansionService queryExpansionService,
                         RecencyWeightCalculator recencyWeightCalculator,
                         VectorCodec vectorCodec) {
        this.properties = properties;
        this.developerRepository = developerRepository;
        this.bugReportRepository = bugReportRepository;
        this.bugAssignmentRepository = bugAssignmentRepository;
        this.commitRepository = commitRepository;
        this.termStatRepository = termStatRepository;
        this.projectTermStatRepository = projectTermStatRepository;
        this.queryExpansionService = queryExpansionService;
        this.recencyWeightCalculator = recencyWeightCalculator;
        this.vectorCodec = vectorCodec;
    }

    public List<DeveloperRankingDto> rank(Map<String, Integer> extractedTerms, Instant asOf) {
        List<DeveloperEntity> developers = developerRepository.findByProjectIdAndActiveTrueOrderById(properties.projectId());
        if (developers.isEmpty() || extractedTerms.isEmpty()) return List.of();

        Map<String, TermStatEntity> originalTermStats = loadTermStats(extractedTerms.keySet());
        LinkedHashMap<String, Integer> recognizedTerms = extractedTerms.entrySet().stream()
                .filter(entry -> originalTermStats.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (left, right) -> left, LinkedHashMap::new));
        if (recognizedTerms.isEmpty()) return List.of();

        QueryProfile expandedProfile = queryExpansionService.expand(recognizedTerms);
        Map<String, TermStatEntity> expandedTermStats = loadTermStats(expandedProfile.frequencies().keySet());
        QueryProfile filteredExpandedProfile = filterProfile(expandedProfile, expandedTermStats.keySet());
        Map<String, Double> bugTermWeights = calculateBugTermWeights(filteredExpandedProfile, expandedTermStats);

        Map<Long, Double> bugScores = calculateBugHistoryScores(filteredExpandedProfile, bugTermWeights, asOf);
        Map<Long, Double> codeScores = calculateCodeHistoryScores(recognizedTerms, originalTermStats, asOf);
        Map<Long, Double> normalizedBugScores = minMaxNormalize(developers, bugScores);
        Map<Long, Double> normalizedCodeScores = minMaxNormalize(developers, codeScores);

        double bugWeight = properties.bugHistoryWeight();
        double codeWeight = 1.0 - bugWeight;
        List<RankedDeveloper> ranked = new ArrayList<>();
        for (DeveloperEntity developer : developers) {
            double bugRaw = bugScores.getOrDefault(developer.getId(), 0.0);
            double codeRaw = codeScores.getOrDefault(developer.getId(), 0.0);
            double bugNormalized = normalizedBugScores.getOrDefault(developer.getId(), 0.0);
            double codeNormalized = normalizedCodeScores.getOrDefault(developer.getId(), 0.0);
            double bugContribution = bugWeight * bugNormalized;
            double codeContribution = codeWeight * codeNormalized;
            ScoreBreakdown breakdown = new ScoreBreakdown(bugRaw, codeRaw, bugNormalized,
                    codeNormalized, bugContribution, codeContribution);
            ranked.add(new RankedDeveloper(developer, bugContribution + codeContribution, breakdown));
        }
        ranked.sort(Comparator.comparingDouble(RankedDeveloper::score).reversed()
                .thenComparing(item -> item.developer().getId()));

        List<DeveloperRankingDto> result = new ArrayList<>();
        int limit = Math.min(properties.resultLimit(), ranked.size());
        for (int i = 0; i < limit; i++) {
            RankedDeveloper item = ranked.get(i);
            result.add(new DeveloperRankingDto(item.developer().getId(), item.developer().getName(),
                    item.developer().getEmail(), item.score(), i + 1, item.breakdown().asMap()));
        }
        return result;
    }

    public List<GlobalDeveloperRankingDto> rankGlobalDeveloperHistory(
            Map<String, Integer> extractedTerms, Instant asOf, Collection<String> submittedDeveloperKeys) {
        if (submittedDeveloperKeys.isEmpty()) return List.of();

        LinkedHashMap<String, Boolean> requestedKeys = new LinkedHashMap<>();
        for (String key : submittedDeveloperKeys)
            requestedKeys.put(DeveloperKeyNormalizer.normalize(key), Boolean.TRUE);

        List<DeveloperEntity> aliases = developerRepository
                .findAllByNormalizedDeveloperKeyIn(requestedKeys.keySet());
        Map<Long, String> developerKeyByAliasId = aliases.stream().collect(Collectors.toMap(
                DeveloperEntity::getId,
                developer -> DeveloperKeyNormalizer.normalize(developer.getName()),
                (left, right) -> left,
                LinkedHashMap::new));
        Set<String> foundKeySet = Set.copyOf(developerKeyByAliasId.values());
        List<String> candidates = requestedKeys.keySet().stream().filter(foundKeySet::contains).toList();
        if (candidates.isEmpty()) return List.of();

        Map<String, TermStatEntity> originalTermStats = loadTermStats(extractedTerms.keySet());
        LinkedHashMap<String, Integer> recognizedTerms = extractedTerms.entrySet().stream()
                .filter(entry -> originalTermStats.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (left, right) -> left, LinkedHashMap::new));
        if (recognizedTerms.isEmpty()) return zeroGlobalRankings(candidates);

        QueryProfile expandedProfile = queryExpansionService.expand(recognizedTerms);
        Map<String, TermStatEntity> expandedTermStats = loadTermStats(expandedProfile.frequencies().keySet());
        QueryProfile filteredExpandedProfile = filterProfile(expandedProfile, expandedTermStats.keySet());

        Collection<Long> aliasIds = developerKeyByAliasId.keySet();
        Map<String, Double> bugScores = calculateGlobalBugHistoryScores(
                filteredExpandedProfile, expandedTermStats, asOf, aliasIds, developerKeyByAliasId);
        Map<String, Double> codeScores = calculateGlobalCodeHistoryScores(
                recognizedTerms, originalTermStats, asOf, aliasIds, developerKeyByAliasId);
        Map<String, Double> normalizedBugScores = minMaxNormalizeKeys(candidates, bugScores);
        Map<String, Double> normalizedCodeScores = minMaxNormalizeKeys(candidates, codeScores);

        double bugWeight = properties.bugHistoryWeight();
        double codeWeight = 1.0 - bugWeight;
        List<RankedGlobalDeveloper> ranked = new ArrayList<>();
        for (String developerKey : candidates) {
            double bugRaw = bugScores.getOrDefault(developerKey, 0.0);
            double codeRaw = codeScores.getOrDefault(developerKey, 0.0);
            double bugNormalized = normalizedBugScores.getOrDefault(developerKey, 0.0);
            double codeNormalized = normalizedCodeScores.getOrDefault(developerKey, 0.0);
            double bugContribution = bugWeight * bugNormalized;
            double codeContribution = codeWeight * codeNormalized;
            ranked.add(new RankedGlobalDeveloper(developerKey, bugContribution + codeContribution,
                    new ScoreBreakdown(bugRaw, codeRaw, bugNormalized, codeNormalized,
                            bugContribution, codeContribution)));
        }
        ranked.sort(Comparator.comparingDouble(RankedGlobalDeveloper::score).reversed()
                .thenComparing(RankedGlobalDeveloper::developerKey));

        List<GlobalDeveloperRankingDto> result = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            RankedGlobalDeveloper item = ranked.get(i);
            result.add(new GlobalDeveloperRankingDto(
                    item.developerKey(), item.score(), i + 1, item.breakdown().asMap()));
        }
        return result;
    }

    private Map<String, Double> calculateGlobalBugHistoryScores(
            QueryProfile query, Map<String, TermStatEntity> globalStats, Instant asOf,
            Collection<Long> aliasIds, Map<Long, String> developerKeyByAliasId) {
        List<BugHistoryEvidenceView> evidence = bugAssignmentRepository.findGlobalHistoryEvidence(
                aliasIds, query.frequencies().keySet(), asOf);
        if (evidence.isEmpty()) return Map.of();

        Set<Long> projectIds = evidence.stream().map(BugHistoryEvidenceView::getProjectId)
                .collect(Collectors.toSet());
        Map<Long, Long> currentUniqueSequenceByProject = projectIds.stream().collect(Collectors.toMap(
                Function.identity(), projectId -> bugReportRepository.findMaximumUniqueBugSequence(projectId) + 1));
        Map<Long, Long> currentAssignmentSequenceByProject = projectIds.stream().collect(Collectors.toMap(
                Function.identity(), projectId -> bugAssignmentRepository.findMaximumAssignmentSequence(projectId) + 1));

        Map<ProjectTermKey, Double> projectWeights = properties.bugHistory().termWeight()
                == AiRankingProperties.TermWeight.SO_PROJECT_BLEND
                ? projectTermStatRepository.findByProjectIdInAndTermIn(projectIds, query.frequencies().keySet())
                    .stream().collect(Collectors.toMap(
                            stat -> new ProjectTermKey(stat.getProjectId(), stat.getTerm()),
                            ProjectTermStatEntity::getWeight))
                : Map.of();

        HashMap<String, Double> scores = new HashMap<>();
        for (BugHistoryEvidenceView item : evidence) {
            String developerKey = developerKeyByAliasId.get(item.getDeveloperId());
            if (developerKey == null) continue;
            TermStatEntity global = globalStats.get(item.getTerm());
            if (global == null) continue;

            double termWeight = global.getGlobalWeight();
            if (properties.bugHistory().termWeight() == AiRankingProperties.TermWeight.SO_PROJECT_BLEND) {
                double sourceProjectWeight = projectWeights.getOrDefault(
                        new ProjectTermKey(item.getProjectId(), item.getTerm()), 0.0);
                double blend = properties.bugHistory().projectWeight();
                termWeight = (1.0 - blend) * termWeight + blend * sourceProjectWeight;
            }

            int wordCount = Math.max(1, item.getOriginalWordCount());
            double evidenceTf = (double) item.getFrequency() / wordCount;
            double queryFrequency = query.frequencies().get(item.getTerm());
            double queryMultiplier = query.multipliers().getOrDefault(item.getTerm(), 1.0);
            double recency = recencyWeightCalculator.bugHistoryWeight(
                    item,
                    currentUniqueSequenceByProject.get(item.getProjectId()),
                    currentAssignmentSequenceByProject.get(item.getProjectId()),
                    asOf);
            scores.merge(developerKey,
                    queryFrequency * queryMultiplier * termWeight * evidenceTf * recency,
                    Double::sum);
        }
        return scores;
    }

    private Map<String, Double> calculateGlobalCodeHistoryScores(
            Map<String, Integer> queryTerms, Map<String, TermStatEntity> termStats, Instant asOf,
            Collection<Long> aliasIds, Map<Long, String> developerKeyByAliasId) {
        List<CommitEntity> commits = commitRepository.findGlobalHistory(aliasIds, asOf);
        Map<GlobalCommitGroup, List<CommitEntity>> histories = commits.stream()
                .filter(commit -> commit.getCodeEmbedding() != null)
                .filter(commit -> developerKeyByAliasId.containsKey(commit.getDeveloper().getId()))
                .collect(Collectors.groupingBy(
                        commit -> new GlobalCommitGroup(
                                developerKeyByAliasId.get(commit.getDeveloper().getId()), commit.getProjectId()),
                        LinkedHashMap::new, Collectors.toList()));

        HashMap<String, Double> scores = new HashMap<>();
        for (Map.Entry<GlobalCommitGroup, List<CommitEntity>> entry : histories.entrySet()) {
            List<CommitEntity> history = entry.getValue();
            float[] aggregate = new float[properties.embeddingDimension()];
            int lastIndex = history.size() - 1;
            Instant firstCommitAt = history.get(0).getCommittedAt();
            for (int i = 0; i < history.size(); i++) {
                float[] vector = vectorCodec.decode(history.get(i).getCodeEmbedding(), properties.embeddingDimension());
                double recency = recencyWeightCalculator.commitWeight(
                        i, lastIndex, history.size(), firstCommitAt, asOf);
                for (int dimension = 0; dimension < aggregate.length; dimension++)
                    aggregate[dimension] += (float) (recency * vector[dimension]);
            }

            double score = 0.0;
            for (Map.Entry<String, Integer> queryTerm : queryTerms.entrySet()) {
                TermStatEntity termStat = termStats.get(queryTerm.getKey());
                if (termStat == null || termStat.getEmbedding() == null) continue;
                float[] termVector = vectorCodec.decode(termStat.getEmbedding(), properties.embeddingDimension());
                double similarity = vectorCodec.dot(aggregate, termVector);
                if (similarity > 0.0)
                    score += queryTerm.getValue() * termStat.getGlobalWeight() * similarity;
            }
            scores.merge(entry.getKey().developerKey(), score, Double::sum);
        }
        return scores;
    }

    private List<GlobalDeveloperRankingDto> zeroGlobalRankings(List<String> candidates) {
        List<String> sorted = candidates.stream().sorted().toList();
        List<GlobalDeveloperRankingDto> result = new ArrayList<>();
        ScoreBreakdown zero = new ScoreBreakdown(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        for (int i = 0; i < sorted.size(); i++)
            result.add(new GlobalDeveloperRankingDto(sorted.get(i), 0.0, i + 1, zero.asMap()));
        return result;
    }

    private Map<String, Double> minMaxNormalizeKeys(List<String> candidates, Map<String, Double> scores) {
        double minimum = candidates.stream().mapToDouble(key -> scores.getOrDefault(key, 0.0)).min().orElse(0.0);
        double maximum = candidates.stream().mapToDouble(key -> scores.getOrDefault(key, 0.0)).max().orElse(0.0);
        double range = maximum - minimum;
        HashMap<String, Double> normalized = new HashMap<>();
        for (String candidate : candidates) {
            double value = scores.getOrDefault(candidate, 0.0);
            normalized.put(candidate, range > 0.0 ? (value - minimum) / range : 0.0);
        }
        return normalized;
    }

    private Map<Long, Double> calculateBugHistoryScores(QueryProfile query, Map<String, Double> termWeights,
                                                         Instant asOf) {
        long currentUniqueSequence = bugReportRepository.findMaximumUniqueBugSequence(properties.projectId()) + 1;
        long currentAssignmentSequence = bugAssignmentRepository.findMaximumAssignmentSequence(properties.projectId()) + 1;
        List<BugHistoryEvidenceView> evidence = bugAssignmentRepository.findHistoryEvidence(
                properties.projectId(), query.frequencies().keySet(), asOf);
        HashMap<Long, Double> scores = new HashMap<>();
        for (BugHistoryEvidenceView item : evidence) {
            int wordCount = Math.max(1, item.getOriginalWordCount());
            double evidenceTf = (double) item.getFrequency() / wordCount;
            double queryFrequency = query.frequencies().get(item.getTerm());
            double queryMultiplier = query.multipliers().getOrDefault(item.getTerm(), 1.0);
            double termWeight = termWeights.getOrDefault(item.getTerm(), 0.0);
            double recency = recencyWeightCalculator.bugHistoryWeight(
                    item, currentUniqueSequence, currentAssignmentSequence, asOf);
            double score = queryFrequency * queryMultiplier * termWeight * evidenceTf * recency;
            scores.merge(item.getDeveloperId(), score, Double::sum);
        }
        return scores;
    }

    private Map<Long, Double> calculateCodeHistoryScores(Map<String, Integer> queryTerms,
                                                          Map<String, TermStatEntity> termStats,
                                                          Instant asOf) {
        List<CommitEntity> commits = commitRepository.findHistory(
                properties.projectId(), asOf);
        Map<Long, List<CommitEntity>> byDeveloper = commits.stream()
                .filter(commit -> commit.getCodeEmbedding() != null)
                .collect(Collectors.groupingBy(commit -> commit.getDeveloper().getId(), LinkedHashMap::new, Collectors.toList()));
        HashMap<Long, Double> scores = new HashMap<>();
        for (Map.Entry<Long, List<CommitEntity>> entry : byDeveloper.entrySet()) {
            List<CommitEntity> history = entry.getValue();
            float[] aggregate = new float[properties.embeddingDimension()];
            int lastIndex = history.size() - 1;
            Instant firstCommitAt = history.get(0).getCommittedAt();
            for (int i = 0; i < history.size(); i++) {
                float[] vector = vectorCodec.decode(history.get(i).getCodeEmbedding(), properties.embeddingDimension());
                double recency = recencyWeightCalculator.commitWeight(
                        i, lastIndex, history.size(), firstCommitAt, asOf);
                for (int dimension = 0; dimension < aggregate.length; dimension++)
                    aggregate[dimension] += (float) (recency * vector[dimension]);
            }
            double score = 0.0;
            for (Map.Entry<String, Integer> queryTerm : queryTerms.entrySet()) {
                TermStatEntity termStat = termStats.get(queryTerm.getKey());
                if (termStat == null || termStat.getEmbedding() == null) continue;
                float[] termVector = vectorCodec.decode(termStat.getEmbedding(), properties.embeddingDimension());
                double similarity = vectorCodec.dot(aggregate, termVector);
                if (similarity > 0.0)
                    score += queryTerm.getValue() * termStat.getGlobalWeight() * similarity;
            }
            scores.put(entry.getKey(), score);
        }
        return scores;
    }

    private Map<String, Double> calculateBugTermWeights(QueryProfile query,
                                                         Map<String, TermStatEntity> globalStats) {
        Map<String, ProjectTermStatEntity> projectStats = projectTermStatRepository
                .findByProjectIdAndTermIn(properties.projectId(), query.frequencies().keySet()).stream()
                .collect(Collectors.toMap(ProjectTermStatEntity::getTerm, Function.identity()));
        HashMap<String, Double> result = new HashMap<>();
        for (String term : query.frequencies().keySet()) {
            TermStatEntity global = globalStats.get(term);
            if (global == null) continue;
            double globalWeight = global.getGlobalWeight();
            if (properties.bugHistory().termWeight() == AiRankingProperties.TermWeight.SO_ONLY) {
                result.put(term, globalWeight);
                continue;
            }
            double projectWeight = projectStats.containsKey(term) ? projectStats.get(term).getWeight() : 0.0;
            double blend = properties.bugHistory().projectWeight();
            result.put(term, (1.0 - blend) * globalWeight + blend * projectWeight);
        }
        return result;
    }

    private Map<String, TermStatEntity> loadTermStats(Collection<String> terms) {
        if (terms.isEmpty()) return Map.of();
        return termStatRepository.findByTermIn(terms).stream()
                .collect(Collectors.toMap(TermStatEntity::getTerm, Function.identity()));
    }

    private QueryProfile filterProfile(QueryProfile profile, Collection<String> recognizedTerms) {
        LinkedHashMap<String, Integer> frequencies = new LinkedHashMap<>();
        LinkedHashMap<String, Double> multipliers = new LinkedHashMap<>();
        for (String term : profile.frequencies().keySet()) {
            if (!recognizedTerms.contains(term)) continue;
            frequencies.put(term, profile.frequencies().get(term));
            multipliers.put(term, profile.multipliers().getOrDefault(term, 1.0));
        }
        return new QueryProfile(frequencies, multipliers);
    }

    private Map<Long, Double> minMaxNormalize(List<DeveloperEntity> developers, Map<Long, Double> scores) {
        double minimum = developers.stream().mapToDouble(developer -> scores.getOrDefault(developer.getId(), 0.0)).min().orElse(0.0);
        double maximum = developers.stream().mapToDouble(developer -> scores.getOrDefault(developer.getId(), 0.0)).max().orElse(0.0);
        double range = maximum - minimum;
        HashMap<Long, Double> normalized = new HashMap<>();
        for (DeveloperEntity developer : developers) {
            double value = scores.getOrDefault(developer.getId(), 0.0);
            normalized.put(developer.getId(), range > 0.0 ? (value - minimum) / range : 0.0);
        }
        return normalized;
    }

    private record RankedDeveloper(DeveloperEntity developer, double score, ScoreBreakdown breakdown) {
    }

    private record RankedGlobalDeveloper(String developerKey, double score, ScoreBreakdown breakdown) {
    }

    private record ProjectTermKey(Long projectId, String term) {
    }

    private record GlobalCommitGroup(String developerKey, Long projectId) {
    }
}
