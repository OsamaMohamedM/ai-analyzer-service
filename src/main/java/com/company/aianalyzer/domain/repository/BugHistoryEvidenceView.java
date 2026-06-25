package com.company.aianalyzer.domain.repository;

import java.time.Instant;

public interface BugHistoryEvidenceView {
    Long getDeveloperId();
    Long getProjectId();
    String getTerm();
    Integer getFrequency();
    Integer getOriginalWordCount();
    Long getUniqueBugSequence();
    Long getAssignmentSequence();
    Instant getAssignedAt();
}
