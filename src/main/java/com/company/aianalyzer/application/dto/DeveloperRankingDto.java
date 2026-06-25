package com.company.aianalyzer.application.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeveloperRankingDto {
    private final Long developerId;
    private final String developerName;
    private final String developerEmail;
    private final Double score;
    private final Integer rank;
    private final Map<String, Double> scoreBreakdown;

    public DeveloperRankingDto(Long developerId, String developerName, String developerEmail,
                               Double score, Integer rank, Map<String, Double> scoreBreakdown) {
        this.developerId = developerId;
        this.developerName = developerName;
        this.developerEmail = developerEmail;
        this.score = score;
        this.rank = rank;
        this.scoreBreakdown = Collections.unmodifiableMap(new LinkedHashMap<>(scoreBreakdown));
    }

    public Long getDeveloperId() { return developerId; }
    public String getDeveloperName() { return developerName; }
    public String getDeveloperEmail() { return developerEmail; }
    public Double getScore() { return score; }
    public Integer getRank() { return rank; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
}
