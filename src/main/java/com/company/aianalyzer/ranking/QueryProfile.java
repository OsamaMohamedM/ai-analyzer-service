package com.company.aianalyzer.ranking;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record QueryProfile(Map<String, Integer> frequencies, Map<String, Double> multipliers) {
    public QueryProfile {
        frequencies = Collections.unmodifiableMap(new LinkedHashMap<>(frequencies));
        multipliers = Collections.unmodifiableMap(new LinkedHashMap<>(multipliers));
    }
}
