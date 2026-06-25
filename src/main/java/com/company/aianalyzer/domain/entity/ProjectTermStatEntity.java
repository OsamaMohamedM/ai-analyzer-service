package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "project_term_statistics")
@IdClass(ProjectTermStatId.class)
public class ProjectTermStatEntity {
    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Id
    @Column(name = "term", length = 255)
    private String term;

    @Column(name = "weight", nullable = false)
    private double weight;

    @Column(name = "developer_count", nullable = false)
    private long developerCount;

    protected ProjectTermStatEntity() {
    }

    public Long getProjectId() { return projectId; }
    public String getTerm() { return term; }
    public double getWeight() { return weight; }
}
