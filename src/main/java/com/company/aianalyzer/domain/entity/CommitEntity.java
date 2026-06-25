package com.company.aianalyzer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "code_commits")
public class CommitEntity {
    @Id
    @Column(name = "commit_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "commit_sha", nullable = false, length = 64)
    private String sha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "developer_id", nullable = false)
    private DeveloperEntity developer;

    @Column(name = "committed_at", nullable = false)
    private Instant committedAt;

    @Column(name = "commit_sequence", nullable = false)
    private long commitSequence;

    @Lob
    @Column(name = "code_embedding", columnDefinition = "varbinary(max)")
    private byte[] codeEmbedding;

    protected CommitEntity() {
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public DeveloperEntity getDeveloper() { return developer; }
    public Instant getCommittedAt() { return committedAt; }
    public long getCommitSequence() { return commitSequence; }
    public byte[] getCodeEmbedding() { return codeEmbedding; }
}
