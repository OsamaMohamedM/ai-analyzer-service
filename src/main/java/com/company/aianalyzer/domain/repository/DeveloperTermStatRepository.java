package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.DeveloperTermStatEntity;
import com.company.aianalyzer.domain.entity.DeveloperTermStatId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperTermStatRepository extends JpaRepository<DeveloperTermStatEntity, DeveloperTermStatId> {
}
