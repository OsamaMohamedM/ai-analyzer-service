package com.company.aianalyzer.application;

import com.company.aianalyzer.application.dto.GlobalDeveloperRankingDto;
import com.company.aianalyzer.ranking.RankingEngine;
import com.company.aianalyzer.ranking.TermExtractor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAnalyzerServiceTest {
    @Test
    void normalizesAndDeduplicatesDeveloperKeysBeforeRanking() {
        RankingEngine rankingEngine = mock(RankingEngine.class);
        TermExtractor termExtractor = mock(TermExtractor.class);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AiAnalyzerService service = new AiAnalyzerService(
                rankingEngine, termExtractor, Clock.fixed(now, ZoneOffset.UTC));
        Map<String, Integer> terms = Map.of("token", 1);
        when(termExtractor.extract("Title Description")).thenReturn(terms);
        when(rankingEngine.rankGlobalDeveloperHistory(eq(terms), eq(now), any()))
                .thenReturn(List.of(new GlobalDeveloperRankingDto("pra85", 0.0, 1, Map.of())));

        var response = service.recommendSubmittedDevelopersForTask(
                "Title", "Description", List.of(" PRA85 ", "drnic", "pra85"));

        ArgumentCaptor<Collection<String>> keys = ArgumentCaptor.forClass(Collection.class);
        verify(rankingEngine).rankGlobalDeveloperHistory(eq(terms), eq(now), keys.capture());
        assertThat(keys.getValue()).containsExactly("pra85", "drnic");
        assertThat(response.getTaskTitle()).isEqualTo("Title");
        assertThat(response.getRecommendations()).extracting(GlobalDeveloperRankingDto::getDeveloperKey)
                .containsExactly("pra85");
    }
}
