package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.TermStatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TermStatRepository extends JpaRepository<TermStatEntity, String> {
    List<TermStatEntity> findByTermIn(Collection<String> terms);
}
