package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "bug_terms")
@IdClass(BugTermId.class)
public class BugTermEntity {
    @Id
    @Column(name = "bug_report_id", insertable = false, updatable = false)
    private Long bugReportId;

    @Id
    @Column(name = "term", length = 255)
    private String term;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bug_report_id", nullable = false)
    private BugReportEntity bugReport;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    protected BugTermEntity() {
    }
}
