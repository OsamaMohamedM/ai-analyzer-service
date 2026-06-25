package com.company.aianalyzer.application;

import com.company.aianalyzer.application.dto.DeveloperRankingDto;
import com.company.aianalyzer.application.dto.DeveloperTaskRecommendationResponse;
import com.company.aianalyzer.ranking.RankingEngine;
import com.company.aianalyzer.ranking.TermExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AiAnalyzerService implements IAiAnalyzerService {
    private final RankingEngine rankingEngine;
    private final TermExtractor termExtractor;
    private final Clock clock;

    public AiAnalyzerService(RankingEngine rankingEngine, TermExtractor termExtractor, Clock clock) {
        this.rankingEngine = rankingEngine;
        this.termExtractor = termExtractor;
        this.clock = clock;
    }

    @Override
    public List<DeveloperRankingDto> recommendForTask(String title, String description) {
        return recommend(title + " " + description);
    }

    @Override
    public List<DeveloperRankingDto> recommendForBugReport(String bugReportText) {
        return recommend(bugReportText);
    }

    @Override
    public DeveloperTaskRecommendationResponse recommendSubmittedDevelopersForTask(
            String taskTitle, String taskDescription, Collection<String> developerKeys) {
        LinkedHashSet<String> normalizedKeys = new LinkedHashSet<>();
        for (String developerKey : developerKeys)
            normalizedKeys.add(DeveloperKeyNormalizer.normalize(developerKey));

        Map<String, Integer> terms = termExtractor.extract(taskTitle + " " + taskDescription);
        return new DeveloperTaskRecommendationResponse(taskTitle,
                rankingEngine.rankGlobalDeveloperHistory(terms, Instant.now(clock), normalizedKeys));
    }

    private List<DeveloperRankingDto> recommend(String text) {
        Map<String, Integer> terms = termExtractor.extract(text);
        return rankingEngine.rank(terms, Instant.now(clock));
    }
}
