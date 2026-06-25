package com.company.aianalyzer.domain.entity;

import java.io.Serializable;
import java.util.Objects;

public class DeveloperTermStatId implements Serializable {
    private Long developerId;
    private String term;

    public DeveloperTermStatId() {
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DeveloperTermStatId that)) return false;
        return Objects.equals(developerId, that.developerId) && Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() { return Objects.hash(developerId, term); }
}
