package com.company.aianalyzer.ranking;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TermExtractorTest {
    private final TermExtractor extractor = new TermExtractor(new TextNormalizer());

    @Test
    void normalizesAndCountsTechnicalTerms() {
        Map<String, Integer> terms = extractor.extract("Fix C++ and .NET integration with C++.");
        assertThat(terms).containsEntry("c++", 2).containsEntry(".net", 1).containsEntry("integration", 1);
    }

    @Test
    void handlesBlankText() {
        assertThat(extractor.extract("  ")).isEmpty();
    }

    @Test
    void createsCompositeTagCandidates() {
        assertThat(extractor.extract("Spring Boot API")).containsEntry("spring-boot", 1)
                .containsEntry("spring-boot-api", 1);
    }
}
