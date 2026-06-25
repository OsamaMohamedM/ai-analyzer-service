package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.BugAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface BugAssignmentRepository extends JpaRepository<BugAssignmentEntity, Long> {
    @Query("""
            select ba.developer.id as developerId,
                   bt.term as term,
                   bt.frequency as frequency,
                   ba.bugReport.originalWordCount as originalWordCount,
                   ba.bugReport.uniqueBugSequence as uniqueBugSequence,
                   ba.assignmentSequence as assignmentSequence,
                   ba.assignedAt as assignedAt
            from BugAssignmentEntity ba
            join BugTermEntity bt on bt.bugReportId = ba.bugReport.id
            where ba.bugReport.projectId = :projectId
              and ba.assignedAt < :asOf
              and bt.term in :terms
            order by ba.assignmentSequence
            """)
    List<BugHistoryEvidenceView> findHistoryEvidence(
            @Param("projectId") Long projectId,
            @Param("terms") Collection<String> terms,
            @Param("asOf") Instant asOf);

    @Query("""
            select ba.developer.id as developerId,
                   ba.bugReport.projectId as projectId,
                   bt.term as term,
                   bt.frequency as frequency,
                   ba.bugReport.originalWordCount as originalWordCount,
                   ba.bugReport.uniqueBugSequence as uniqueBugSequence,
                   ba.assignmentSequence as assignmentSequence,
                   ba.assignedAt as assignedAt
            from BugAssignmentEntity ba
            join BugTermEntity bt on bt.bugReportId = ba.bugReport.id
            where ba.developer.id in :developerIds
              and ba.assignedAt < :asOf
              and bt.term in :terms
            order by ba.bugReport.projectId, ba.assignmentSequence
            """)
    List<BugHistoryEvidenceView> findGlobalHistoryEvidence(
            @Param("developerIds") Collection<Long> developerIds,
            @Param("terms") Collection<String> terms,
            @Param("asOf") Instant asOf);

    @Query("select coalesce(max(ba.assignmentSequence), 0) from BugAssignmentEntity ba where ba.bugReport.projectId = :projectId")
    long findMaximumAssignmentSequence(@Param("projectId") Long projectId);
}
