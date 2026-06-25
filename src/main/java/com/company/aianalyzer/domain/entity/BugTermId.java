package com.company.aianalyzer.domain.entity;

import java.io.Serializable;
import java.util.Objects;

public class BugTermId implements Serializable {
    private Long bugReportId;
    private String term;

    public BugTermId() {
    }

    public BugTermId(Long bugReportId, String term) {
        this.bugReportId = bugReportId;
        this.term = term;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BugTermId that)) return false;
        return Objects.equals(bugReportId, that.bugReportId) && Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bugReportId, term);
    }
}
