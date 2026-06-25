package com.company.aianalyzer.api;

import com.company.aianalyzer.application.IAiAnalyzerService;
import com.company.aianalyzer.application.dto.DeveloperTaskRecommendationRequest;
import com.company.aianalyzer.application.dto.DeveloperTaskRecommendationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class DeveloperTaskRecommendationController {
    private final IAiAnalyzerService aiAnalyzerService;

    public DeveloperTaskRecommendationController(IAiAnalyzerService aiAnalyzerService) {
        this.aiAnalyzerService = aiAnalyzerService;
    }

    @PostMapping("/recommend-developers-for-task")
    public ResponseEntity<DeveloperTaskRecommendationResponse> recommendDevelopersForTask(
            @Valid @RequestBody DeveloperTaskRecommendationRequest request) {
        return ResponseEntity.ok(aiAnalyzerService.recommendSubmittedDevelopersForTask(
                request.getTaskTitle(),
                request.getTaskDescription(),
                request.getDevelopers().stream().map(item -> item.getDeveloperKey()).toList()));
    }
}
