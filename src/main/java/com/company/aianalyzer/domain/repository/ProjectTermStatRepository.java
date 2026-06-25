package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.ProjectTermStatEntity;
import com.company.aianalyzer.domain.entity.ProjectTermStatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProjectTermStatRepository extends JpaRepository<ProjectTermStatEntity, ProjectTermStatId> {
    List<ProjectTermStatEntity> findByProjectIdAndTermIn(Long projectId, Collection<String> terms);
    List<ProjectTermStatEntity> findByProjectIdInAndTermIn(Collection<Long> projectIds, Collection<String> terms);
}
