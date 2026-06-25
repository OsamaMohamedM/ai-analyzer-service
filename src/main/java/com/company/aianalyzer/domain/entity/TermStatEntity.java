package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "term_statistics")
public class TermStatEntity {
    @Id
    @Column(name = "term", length = 255)
    private String term;

    @Column(name = "global_weight", nullable = false)
    private double globalWeight;

    @Lob
    @Column(name = "embedding", columnDefinition = "varbinary(max)")
    private byte[] embedding;

    protected TermStatEntity() {
    }

    public String getTerm() { return term; }
    public double getGlobalWeight() { return globalWeight; }
    public byte[] getEmbedding() { return embedding; }
}
