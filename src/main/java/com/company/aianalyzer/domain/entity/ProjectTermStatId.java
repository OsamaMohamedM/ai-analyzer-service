package com.company.aianalyzer.domain.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProjectTermStatId implements Serializable {
    private Long projectId;
    private String term;

    public ProjectTermStatId() {
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProjectTermStatId that)) return false;
        return Objects.equals(projectId, that.projectId) && Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() { return Objects.hash(projectId, term); }
}
