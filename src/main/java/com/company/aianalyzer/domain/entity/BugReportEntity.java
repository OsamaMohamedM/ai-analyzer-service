package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "bug_reports")
public class BugReportEntity {
    @Id
    @Column(name = "bug_report_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "external_bug_number", nullable = false, length = 100)
    private String externalBugNumber;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    @Column(name = "unique_bug_sequence", nullable = false)
    private long uniqueBugSequence;

    @Column(name = "original_word_count", nullable = false)
    private int originalWordCount;

    protected BugReportEntity() {
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getExternalBugNumber() { return externalBugNumber; }
    public Instant getReportedAt() { return reportedAt; }
    public long getUniqueBugSequence() { return uniqueBugSequence; }
    public int getOriginalWordCount() { return originalWordCount; }
}
