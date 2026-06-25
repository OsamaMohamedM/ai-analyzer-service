package com.company.aianalyzer.api;

import com.company.aianalyzer.application.IAiAnalyzerService;
import com.company.aianalyzer.application.dto.BugReportRecommendationRequest;
import com.company.aianalyzer.application.dto.DeveloperRankingDto;
import com.company.aianalyzer.application.dto.TaskRecommendationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/ai/recommendations")
public class AiRecommendationController {
    private final IAiAnalyzerService aiAnalyzerService;

    public AiRecommendationController(IAiAnalyzerService aiAnalyzerService) {
        this.aiAnalyzerService = aiAnalyzerService;
    }

    @PostMapping("/tasks")
    public ResponseEntity<List<DeveloperRankingDto>> recommendForTask(
            @Valid @RequestBody TaskRecommendationRequest request) {
        return ResponseEntity.ok(aiAnalyzerService.recommendForTask(request.getTitle(), request.getDescription()));
    }

    @PostMapping({"/bug-reports", "/bugs"})
    public ResponseEntity<List<DeveloperRankingDto>> recommendForBugReport(
            @Valid @RequestBody BugReportRecommendationRequest request) {
        return ResponseEntity.ok(aiAnalyzerService.recommendForBugReport(request.getBugReportText()));
    }
}
