package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.TagExpansionEntity;
import com.company.aianalyzer.domain.entity.TagExpansionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TagExpansionRepository extends JpaRepository<TagExpansionEntity, TagExpansionId> {
    List<TagExpansionEntity> findBySourceTermInOrderBySourceTermAscWeightDesc(Collection<String> sourceTerms);
}
