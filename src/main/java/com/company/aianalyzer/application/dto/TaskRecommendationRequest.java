package com.company.aianalyzer.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaskRecommendationRequest {
    @NotBlank
    @Size(max = 500)
    private String title;

    @NotBlank
    @Size(max = 20000)
    private String description;

    public TaskRecommendationRequest() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
