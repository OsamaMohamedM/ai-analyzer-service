package com.company.aianalyzer;

import com.company.aianalyzer.config.AiRankingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiRankingProperties.class)
public class AiAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiAnalyzerApplication.class, args);
    }
}
