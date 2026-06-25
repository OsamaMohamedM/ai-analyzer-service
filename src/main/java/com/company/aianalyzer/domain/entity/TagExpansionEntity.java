package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "tag_expansions")
@IdClass(TagExpansionId.class)
public class TagExpansionEntity {
    @Id
    @Column(name = "source_term", length = 255)
    private String sourceTerm;

    @Id
    @Column(name = "target_term", length = 255)
    private String targetTerm;

    @Column(name = "weight", nullable = false)
    private double weight;

    protected TagExpansionEntity() {
    }

    public String getSourceTerm() { return sourceTerm; }
    public String getTargetTerm() { return targetTerm; }
    public double getWeight() { return weight; }
}
