package com.company.aianalyzer.application;

import com.company.aianalyzer.application.dto.DeveloperRankingDto;
import com.company.aianalyzer.application.dto.DeveloperTaskRecommendationResponse;

import java.util.Collection;
import java.util.List;

public interface IAiAnalyzerService {
    List<DeveloperRankingDto> recommendForTask(String title, String description);
    List<DeveloperRankingDto> recommendForBugReport(String bugReportText);
    DeveloperTaskRecommendationResponse recommendSubmittedDevelopersForTask(
            String taskTitle, String taskDescription, Collection<String> developerKeys);
}
