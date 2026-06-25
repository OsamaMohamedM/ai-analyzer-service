package com.company.aianalyzer.ranking;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class TextNormalizer {
    public String normalize(String text) {
        if (text == null || text.isBlank()) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[^a-z0-9+#.\\-]+", " ").replaceAll("\\s+", " ").trim();
    }
}
