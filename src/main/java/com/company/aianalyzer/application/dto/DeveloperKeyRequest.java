package com.company.aianalyzer.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeveloperKeyRequest {
    @NotBlank
    @Size(max = 255)
    private String developerKey;

    public DeveloperKeyRequest() {
    }

    public String getDeveloperKey() { return developerKey; }
    public void setDeveloperKey(String developerKey) { this.developerKey = developerKey; }
}
