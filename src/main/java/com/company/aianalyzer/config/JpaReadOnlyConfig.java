package com.company.aianalyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Clock;

@Configuration
@EnableJpaRepositories(basePackages = "com.company.aianalyzer.domain.repository")
public class JpaReadOnlyConfig {
    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
