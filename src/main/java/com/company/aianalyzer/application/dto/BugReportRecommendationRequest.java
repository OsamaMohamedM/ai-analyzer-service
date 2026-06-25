package com.company.aianalyzer.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BugReportRecommendationRequest {
    @NotBlank
    @Size(max = 20000)
    private String bugReportText;

    public BugReportRecommendationRequest() {
    }

    public String getBugReportText() { return bugReportText; }
    public void setBugReportText(String bugReportText) { this.bugReportText = bugReportText; }
}
