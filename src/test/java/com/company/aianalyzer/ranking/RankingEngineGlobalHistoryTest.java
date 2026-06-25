package com.company.aianalyzer.ranking;

import com.company.aianalyzer.config.AiRankingProperties;
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
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RankingEngineGlobalHistoryTest {
    @Test
    void skipsUnknownKeysAndReturnsAFoundKeyWithZeroScoreWhenTermsAreUnknown() {
        DeveloperRepository developerRepository = mock(DeveloperRepository.class);
        TermStatRepository termStatRepository = mock(TermStatRepository.class);
        DeveloperEntity alias = mock(DeveloperEntity.class);
        when(alias.getId()).thenReturn(17L);
        when(alias.getName()).thenReturn(" Pra85 ");
        when(developerRepository.findAllByNormalizedDeveloperKeyIn(Set.of("pra85", "missing")))
                .thenReturn(List.of(alias));
        when(termStatRepository.findByTermIn(Set.of("unrecognized"))).thenReturn(List.of());

        RankingEngine engine = new RankingEngine(properties(), developerRepository,
                mock(BugReportRepository.class), mock(BugAssignmentRepository.class),
                mock(CommitRepository.class), termStatRepository,
                mock(ProjectTermStatRepository.class), mock(QueryExpansionService.class),
                mock(RecencyWeightCalculator.class), mock(VectorCodec.class));

        var result = engine.rankGlobalDeveloperHistory(
                Map.of("unrecognized", 1), Instant.parse("2026-01-01T00:00:00Z"),
                List.of(" PRA85 ", "missing", "pra85"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeveloperKey()).isEqualTo("pra85");
        assertThat(result.get(0).getScore()).isZero();
        assertThat(result.get(0).getRank()).isEqualTo(1);
        verify(developerRepository).findAllByNormalizedDeveloperKeyIn(Set.of("pra85", "missing"));
    }

    @Test
    void aggregatesBugHistoryFromEveryProjectAliasUsingEachSourceProjectWeight() {
        DeveloperRepository developerRepository = mock(DeveloperRepository.class);
        BugReportRepository bugReportRepository = mock(BugReportRepository.class);
        BugAssignmentRepository bugAssignmentRepository = mock(BugAssignmentRepository.class);
        CommitRepository commitRepository = mock(CommitRepository.class);
        TermStatRepository termStatRepository = mock(TermStatRepository.class);
        ProjectTermStatRepository projectTermStatRepository = mock(ProjectTermStatRepository.class);
        QueryExpansionService queryExpansionService = mock(QueryExpansionService.class);
        RecencyWeightCalculator recencyWeightCalculator = mock(RecencyWeightCalculator.class);

        DeveloperEntity firstAlias = developer(17L, "pra85");
        DeveloperEntity secondAlias = developer(29L, " PRA85 ");
        when(developerRepository.findAllByNormalizedDeveloperKeyIn(Set.of("pra85")))
                .thenReturn(List.of(firstAlias, secondAlias));

        TermStatEntity token = mock(TermStatEntity.class);
        when(token.getTerm()).thenReturn("token");
        when(token.getGlobalWeight()).thenReturn(0.4);
        when(termStatRepository.findByTermIn(anyCollection())).thenReturn(List.of(token));
        when(queryExpansionService.expand(Map.of("token", 1)))
                .thenReturn(new QueryProfile(Map.of("token", 1), Map.of("token", 1.0)));

        BugHistoryEvidenceView projectOneEvidence = evidence(17L, 10L, 1, 10);
        BugHistoryEvidenceView projectTwoEvidence = evidence(29L, 20L, 2, 10);
        when(bugAssignmentRepository.findGlobalHistoryEvidence(
                anyCollection(), eq(Set.of("token")), any(Instant.class)))
                .thenReturn(List.of(projectOneEvidence, projectTwoEvidence));
        when(bugReportRepository.findMaximumUniqueBugSequence(10L)).thenReturn(5L);
        when(bugReportRepository.findMaximumUniqueBugSequence(20L)).thenReturn(8L);
        when(bugAssignmentRepository.findMaximumAssignmentSequence(10L)).thenReturn(7L);
        when(bugAssignmentRepository.findMaximumAssignmentSequence(20L)).thenReturn(9L);
        when(recencyWeightCalculator.bugHistoryWeight(
                any(BugHistoryEvidenceView.class), anyLong(), anyLong(), any(Instant.class)))
                .thenReturn(1.0);

        ProjectTermStatEntity projectOneWeight = projectWeight(10L, 0.5);
        ProjectTermStatEntity projectTwoWeight = projectWeight(20L, 0.25);
        when(projectTermStatRepository.findByProjectIdInAndTermIn(
                eq(Set.of(10L, 20L)), eq(Set.of("token"))))
                .thenReturn(List.of(projectOneWeight, projectTwoWeight));
        when(commitRepository.findGlobalHistory(anyCollection(), any(Instant.class))).thenReturn(List.of());

        RankingEngine engine = new RankingEngine(properties(), developerRepository,
                bugReportRepository, bugAssignmentRepository, commitRepository, termStatRepository,
                projectTermStatRepository, queryExpansionService, recencyWeightCalculator,
                mock(VectorCodec.class));

        var result = engine.rankGlobalDeveloperHistory(
                Map.of("token", 1), Instant.parse("2026-01-01T00:00:00Z"), List.of("pra85"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeveloperKey()).isEqualTo("pra85");
        assertThat(result.get(0).getScoreBreakdown().get("bugHistoryRaw")).isCloseTo(0.1, within(1.0e-12));
    }

    private DeveloperEntity developer(long id, String name) {
        DeveloperEntity developer = mock(DeveloperEntity.class);
        when(developer.getId()).thenReturn(id);
        when(developer.getName()).thenReturn(name);
        return developer;
    }

    private BugHistoryEvidenceView evidence(long developerId, long projectId, int frequency, int wordCount) {
        BugHistoryEvidenceView evidence = mock(BugHistoryEvidenceView.class);
        when(evidence.getDeveloperId()).thenReturn(developerId);
        when(evidence.getProjectId()).thenReturn(projectId);
        when(evidence.getTerm()).thenReturn("token");
        when(evidence.getFrequency()).thenReturn(frequency);
        when(evidence.getOriginalWordCount()).thenReturn(wordCount);
        return evidence;
    }

    private ProjectTermStatEntity projectWeight(long projectId, double weight) {
        ProjectTermStatEntity statistic = mock(ProjectTermStatEntity.class);
        when(statistic.getProjectId()).thenReturn(projectId);
        when(statistic.getTerm()).thenReturn("token");
        when(statistic.getWeight()).thenReturn(weight);
        return statistic;
    }

    private AiRankingProperties properties() {
        return new AiRankingProperties(1L, 1, 2, AiRankingProperties.RecencyPeriod.PER_MONTH, 0.8,
                new AiRankingProperties.BugHistory(AiRankingProperties.BugHistoryRecency.UNIQUE_BUG_DISTANCE,
                        180.0, AiRankingProperties.QueryExpansion.SO_TAG_GRAPH, 6, 0.25,
                        AiRankingProperties.TermWeight.SO_PROJECT_BLEND, 1.0));
    }
}
