package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.BugReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BugReportRepository extends JpaRepository<BugReportEntity, Long> {
    @Query("select coalesce(max(b.uniqueBugSequence), 0) from BugReportEntity b where b.projectId = :projectId")
    long findMaximumUniqueBugSequence(@Param("projectId") Long projectId);
}
