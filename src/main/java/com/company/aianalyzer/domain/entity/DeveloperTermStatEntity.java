package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "developer_term_statistics")
@IdClass(DeveloperTermStatId.class)
public class DeveloperTermStatEntity {
    @Id
    @Column(name = "developer_id")
    private Long developerId;

    @Id
    @Column(name = "term", length = 255)
    private String term;

    @Column(name = "bug_frequency", nullable = false)
    private long bugFrequency;

    @Column(name = "code_frequency", nullable = false)
    private long codeFrequency;

    @Column(name = "last_observed_at")
    private Instant lastObservedAt;

    protected DeveloperTermStatEntity() {
    }
}
