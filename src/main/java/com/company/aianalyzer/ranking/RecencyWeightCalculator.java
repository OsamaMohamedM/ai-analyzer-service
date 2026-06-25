package com.company.aianalyzer.ranking;

import com.company.aianalyzer.config.AiRankingProperties;
import com.company.aianalyzer.domain.repository.BugHistoryEvidenceView;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class RecencyWeightCalculator {
    private final AiRankingProperties properties;

    public RecencyWeightCalculator(AiRankingProperties properties) {
        this.properties = properties;
    }

    public double bugHistoryWeight(BugHistoryEvidenceView evidence, long currentUniqueBugSequence,
                                   long currentAssignmentSequence, Instant asOf) {
        return switch (properties.bugHistory().recency()) {
            case UNIQUE_BUG_DISTANCE -> 1.0 / Math.max(1L, currentUniqueBugSequence - evidence.getUniqueBugSequence());
            case PAPER_ASSIGNMENT_DISTANCE -> 1.0 / Math.max(1L, currentAssignmentSequence - evidence.getAssignmentSequence());
            case EXPONENTIAL_TIME -> {
                double ageDays = Math.max(0.0, Duration.between(evidence.getAssignedAt(), asOf).toHours() / 24.0);
                yield Math.pow(0.5, ageDays / properties.bugHistory().halfLifeDays());
            }
        };
    }

    public double commitWeight(int commitIndex, int lastCommitIndex, int commitCount,
                               Instant firstCommitAt, Instant asOf) {
        double periodDays = properties.recencyPeriod() == AiRankingProperties.RecencyPeriod.PER_MONTH
                ? 365.2425 / 12.0 : 1.0;
        double elapsedDays = Math.max(1.0, Duration.between(firstCommitAt, asOf).toHours() / 24.0);
        double averageCommitsPerPeriod = commitCount / (elapsedDays / periodDays);
        int commitsAfter = lastCommitIndex - commitIndex;
        return 1.0 / (1.0 + commitsAfter / averageCommitsPerPeriod);
    }
}
