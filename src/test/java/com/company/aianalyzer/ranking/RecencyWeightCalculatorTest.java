package com.company.aianalyzer.ranking;

import com.company.aianalyzer.config.AiRankingProperties;
import com.company.aianalyzer.domain.repository.BugHistoryEvidenceView;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RecencyWeightCalculatorTest {
    @Test
    void appliesUniqueBugDistance() {
        RecencyWeightCalculator calculator = new RecencyWeightCalculator(properties());
        BugHistoryEvidenceView evidence = evidence(8L, 12L, Instant.parse("2024-01-01T00:00:00Z"));
        double weight = calculator.bugHistoryWeight(
                evidence, 10L, 20L, Instant.parse("2024-07-01T00:00:00Z"));
        assertThat(weight).isEqualTo(0.5);
    }

    @Test
    void appliesMonthlyCommitActivityRate() {
        RecencyWeightCalculator calculator = new RecencyWeightCalculator(properties());
        double weight = calculator.commitWeight(0, 2, 3,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-04-01T00:00:00Z"));
        assertThat(weight).isBetween(0.32, 0.35);
    }

    private AiRankingProperties properties() {
        return new AiRankingProperties(1L, 20, 300, AiRankingProperties.RecencyPeriod.PER_MONTH, 0.8,
                new AiRankingProperties.BugHistory(AiRankingProperties.BugHistoryRecency.UNIQUE_BUG_DISTANCE,
                        180.0, AiRankingProperties.QueryExpansion.SO_TAG_GRAPH, 6, 0.25,
                        AiRankingProperties.TermWeight.SO_PROJECT_BLEND, 1.0));
    }

    private BugHistoryEvidenceView evidence(long uniqueSequence, long assignmentSequence, Instant assignedAt) {
        return new BugHistoryEvidenceView() {
            public Long getDeveloperId() { return 1L; }
            public Long getProjectId() { return 1L; }
            public String getTerm() { return "java"; }
            public Integer getFrequency() { return 1; }
            public Integer getOriginalWordCount() { return 10; }
            public Long getUniqueBugSequence() { return uniqueSequence; }
            public Long getAssignmentSequence() { return assignmentSequence; }
            public Instant getAssignedAt() { return assignedAt; }
        };
    }
}
