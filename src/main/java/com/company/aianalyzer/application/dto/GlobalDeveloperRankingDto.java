package com.company.aianalyzer.application.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalDeveloperRankingDto {
    private final String developerKey;
    private final Double score;
    private final Integer rank;
    private final Map<String, Double> scoreBreakdown;

    public GlobalDeveloperRankingDto(String developerKey, Double score, Integer rank,
                                     Map<String, Double> scoreBreakdown) {
        this.developerKey = developerKey;
        this.score = score;
        this.rank = rank;
        this.scoreBreakdown = Collections.unmodifiableMap(new LinkedHashMap<>(scoreBreakdown));
    }

    public String getDeveloperKey() { return developerKey; }
    public Double getScore() { return score; }
    public Integer getRank() { return rank; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
}
