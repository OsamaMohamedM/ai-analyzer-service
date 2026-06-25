package com.company.aianalyzer.application.dto;

import java.util.List;

public class DeveloperTaskRecommendationResponse {
    private final String taskTitle;
    private final List<GlobalDeveloperRankingDto> recommendations;

    public DeveloperTaskRecommendationResponse(String taskTitle,
                                               List<GlobalDeveloperRankingDto> recommendations) {
        this.taskTitle = taskTitle;
        this.recommendations = List.copyOf(recommendations);
    }

    public String getTaskTitle() { return taskTitle; }
    public List<GlobalDeveloperRankingDto> getRecommendations() { return recommendations; }
}
