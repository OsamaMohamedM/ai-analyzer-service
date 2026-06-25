package com.company.aianalyzer.application;

import java.util.Locale;

public final class DeveloperKeyNormalizer {
    private DeveloperKeyNormalizer() {
    }

    public static String normalize(String developerKey) {
        return developerKey.trim().toLowerCase(Locale.ROOT);
    }
}
