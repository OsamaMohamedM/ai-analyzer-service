package com.company.aianalyzer.domain.entity;

import java.io.Serializable;
import java.util.Objects;

public class TagExpansionId implements Serializable {
    private String sourceTerm;
    private String targetTerm;

    public TagExpansionId() {
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof TagExpansionId that)) return false;
        return Objects.equals(sourceTerm, that.sourceTerm) && Objects.equals(targetTerm, that.targetTerm);
    }

    @Override
    public int hashCode() { return Objects.hash(sourceTerm, targetTerm); }
}
