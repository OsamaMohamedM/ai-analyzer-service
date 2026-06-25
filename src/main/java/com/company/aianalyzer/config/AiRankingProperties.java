package com.company.aianalyzer.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ai.ranking")
public record AiRankingProperties(
        @NotNull Long projectId,
        @Min(1) int resultLimit,
        @Min(1) int embeddingDimension,
        @NotNull RecencyPeriod recencyPeriod,
        @DecimalMin("0.0") @DecimalMax("1.0") double bugHistoryWeight,
        @Valid @NotNull BugHistory bugHistory) {

    public enum RecencyPeriod { PER_DAY, PER_MONTH }
    public enum BugHistoryRecency { PAPER_ASSIGNMENT_DISTANCE, UNIQUE_BUG_DISTANCE, EXPONENTIAL_TIME }
    public enum QueryExpansion { NONE, SO_TAG_GRAPH }
    public enum TermWeight { SO_ONLY, SO_PROJECT_BLEND }

    public record BugHistory(
            @NotNull BugHistoryRecency recency,
            @DecimalMin(value = "0.0", inclusive = false) double halfLifeDays,
            @NotNull QueryExpansion queryExpansion,
            @Min(0) int expansionMaxTags,
            @DecimalMin("0.0") @DecimalMax("1.0") double expansionWeight,
            @NotNull TermWeight termWeight,
            @DecimalMin("0.0") @DecimalMax("1.0") double projectWeight) {
    }
}
