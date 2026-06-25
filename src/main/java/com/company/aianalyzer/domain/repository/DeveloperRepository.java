package com.company.aianalyzer.domain.repository;

import com.company.aianalyzer.domain.entity.DeveloperEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DeveloperRepository extends JpaRepository<DeveloperEntity, Long> {
    List<DeveloperEntity> findByProjectIdAndActiveTrueOrderById(Long projectId);

    @Query(value = """
            select * from developers
            where lower(ltrim(rtrim(name))) in (:developerKeys)
            order by lower(ltrim(rtrim(name))), developer_id
            """, nativeQuery = true)
    List<DeveloperEntity> findAllByNormalizedDeveloperKeyIn(
            @Param("developerKeys") Collection<String> developerKeys);
}
