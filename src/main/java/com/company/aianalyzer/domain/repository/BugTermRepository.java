package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.BugTermEntity;
import com.company.aianalyzer.domain.entity.BugTermId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugTermRepository extends JpaRepository<BugTermEntity, BugTermId> {
}
