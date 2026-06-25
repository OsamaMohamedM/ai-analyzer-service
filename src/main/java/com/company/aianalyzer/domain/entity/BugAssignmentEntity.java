package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "bug_assignments")
public class BugAssignmentEntity {
    @Id
    @Column(name = "assignment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bug_report_id", nullable = false)
    private BugReportEntity bugReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    private DeveloperEntity developer;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "assignment_sequence", nullable = false)
    private long assignmentSequence;

    protected BugAssignmentEntity() {
    }
}
