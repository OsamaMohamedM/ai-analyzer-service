package com.company.aianalyzer.ranking;

import com.company.aianalyzer.config.AiRankingProperties;
import com.company.aianalyzer.domain.entity.TagExpansionEntity;
import com.company.aianalyzer.domain.repository.TagExpansionRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueryExpansionService {
    private final TagExpansionRepository tagExpansionRepository;
    private final AiRankingProperties properties;

    public QueryExpansionService(TagExpansionRepository tagExpansionRepository, AiRankingProperties properties) {
        this.tagExpansionRepository = tagExpansionRepository;
        this.properties = properties;
    }

    public QueryProfile expand(Map<String, Integer> sourceTerms) {
        LinkedHashMap<String, Integer> frequencies = new LinkedHashMap<>(sourceTerms);
        LinkedHashMap<String, Double> multipliers = new LinkedHashMap<>();
        sourceTerms.keySet().forEach(term -> multipliers.put(term, 1.0));
        if (properties.bugHistory().queryExpansion() == AiRankingProperties.QueryExpansion.NONE
                || properties.bugHistory().expansionMaxTags() == 0) {
            return new QueryProfile(frequencies, multipliers);
        }
        List<TagExpansionEntity> expansions = tagExpansionRepository
                .findBySourceTermInOrderBySourceTermAscWeightDesc(sourceTerms.keySet());
        LinkedHashMap<String, Integer> acceptedBySource = new LinkedHashMap<>();
        for (TagExpansionEntity expansion : expansions) {
                int accepted = acceptedBySource.getOrDefault(expansion.getSourceTerm(), 0);
                if (accepted >= properties.bugHistory().expansionMaxTags()) continue;
                if (sourceTerms.containsKey(expansion.getTargetTerm())) continue;
                frequencies.putIfAbsent(expansion.getTargetTerm(), 1);
                double weightedExpansion = Math.min(1.0,
                        properties.bugHistory().expansionWeight() * expansion.getWeight());
                multipliers.merge(expansion.getTargetTerm(), weightedExpansion, (left, right) -> Math.min(1.0, left + right));
                acceptedBySource.put(expansion.getSourceTerm(), accepted + 1);
        }
        return new QueryProfile(frequencies, multipliers);
    }
}
