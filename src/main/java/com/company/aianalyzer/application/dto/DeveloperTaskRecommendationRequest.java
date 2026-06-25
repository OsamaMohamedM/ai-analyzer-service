package com.company.aianalyzer.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class DeveloperTaskRecommendationRequest {
    @NotBlank
    @Size(max = 500)
    private String taskTitle;

    @NotBlank
    @Size(max = 20000)
    private String taskDescription;

    @NotEmpty
    private List<@NotNull @Valid DeveloperKeyRequest> developers;

    public DeveloperTaskRecommendationRequest() {
    }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    public List<DeveloperKeyRequest> getDevelopers() { return developers; }
    public void setDevelopers(List<DeveloperKeyRequest> developers) { this.developers = developers; }
}
