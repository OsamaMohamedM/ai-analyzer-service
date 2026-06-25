package com.company.aianalyzer.ranking;

import java.util.LinkedHashMap;
import java.util.Map;

public record ScoreBreakdown(double bugHistoryRaw, double codeHistoryRaw,
                             double bugHistoryNormalized, double codeHistoryNormalized,
                             double bugHistoryContribution, double codeHistoryContribution) {
    public Map<String, Double> asMap() {
        LinkedHashMap<String, Double> values = new LinkedHashMap<>();
        values.put("bugHistoryRaw", bugHistoryRaw);
        values.put("codeHistoryRaw", codeHistoryRaw);
        values.put("bugHistoryNormalized", bugHistoryNormalized);
        values.put("codeHistoryNormalized", codeHistoryNormalized);
        values.put("bugHistoryContribution", bugHistoryContribution);
        values.put("codeHistoryContribution", codeHistoryContribution);
        return values;
    }
}
