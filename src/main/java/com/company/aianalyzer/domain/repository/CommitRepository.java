package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.CommitEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    @EntityGraph(attributePaths = "developer")
    @Query("""
            select c from CommitEntity c
            where c.projectId = :projectId and c.committedAt < :asOf
            order by c.developer.id, c.committedAt
            """)
    List<CommitEntity> findHistory(@Param("projectId") Long projectId, @Param("asOf") Instant asOf);

    @EntityGraph(attributePaths = "developer")
    @Query("""
            select c from CommitEntity c
            where c.developer.id in :developerIds and c.committedAt < :asOf
            order by c.projectId, c.developer.id, c.committedAt, c.id
            """)
    List<CommitEntity> findGlobalHistory(
            @Param("developerIds") Collection<Long> developerIds,
            @Param("asOf") Instant asOf);
}
