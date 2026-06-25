package com.company.aianalyzer.api;

import com.company.aianalyzer.application.IAiAnalyzerService;
import com.company.aianalyzer.application.dto.DeveloperTaskRecommendationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeveloperTaskRecommendationController.class)
class DeveloperTaskRecommendationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAiAnalyzerService aiAnalyzerService;

    @Test
    void acceptsDeveloperKeysAtThePublicEndpoint() throws Exception {
        when(aiAnalyzerService.recommendSubmittedDevelopersForTask(
                eq("Fix token"), eq("Refresh expiration"), eq(List.of("pra85", "drnic"))))
                .thenReturn(new DeveloperTaskRecommendationResponse("Fix token", List.of()));

        mockMvc.perform(post("/api/ai/recommend-developers-for-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "Fix token",
                                  "taskDescription": "Refresh expiration",
                                  "developers": [
                                    {"developerKey": "pra85"},
                                    {"developerKey": "drnic"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskTitle").value("Fix token"))
                .andExpect(jsonPath("$.recommendations").isArray());

        verify(aiAnalyzerService).recommendSubmittedDevelopersForTask(
                "Fix token", "Refresh expiration", List.of("pra85", "drnic"));
    }

    @Test
    void rejectsAnEmptyDeveloperList() throws Exception {
        mockMvc.perform(post("/api/ai/recommend-developers-for-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "Fix token",
                                  "taskDescription": "Refresh expiration",
                                  "developers": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankDeveloperKeysAsInvalidStructure() throws Exception {
        mockMvc.perform(post("/api/ai/recommend-developers-for-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskTitle": "Fix token",
                                  "taskDescription": "Refresh expiration",
                                  "developers": [{"developerKey": " "}]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
