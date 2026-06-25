package com.company.aianalyzer.ranking;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TermExtractor {
    private final TextNormalizer textNormalizer;

    public TermExtractor(TextNormalizer textNormalizer) {
        this.textNormalizer = textNormalizer;
    }

    public Map<String, Integer> extract(String text) {
        LinkedHashMap<String, Integer> frequencies = new LinkedHashMap<>();
        String normalized = textNormalizer.normalize(text);
        if (normalized.isEmpty()) return frequencies;
        List<String> tokens = new ArrayList<>();
        for (String token : normalized.split(" ")) {
            String cleaned = token.replaceAll("^[#-]+|[.-]+$", "");
            if (!cleaned.isBlank()) {
                tokens.add(cleaned);
                frequencies.merge(cleaned, 1, Integer::sum);
            }
        }
        for (int size = 2; size <= 4; size++) {
            for (int start = 0; start + size <= tokens.size(); start++) {
                String candidate = String.join("-", tokens.subList(start, start + size));
                frequencies.merge(candidate, 1, Integer::sum);
            }
        }
        return frequencies;
    }
}
