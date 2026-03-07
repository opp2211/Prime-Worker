package ru.maltsev.primeworker.domain.rate;

import java.util.List;

public record RatesSnapshot(List<RateValue> values) {
    public RatesSnapshot {
        values = values == null ? List.of() : List.copyOf(values);
    }

    public List<String> formattedValues() {
        return values.stream()
                .map(RateValue::formatComma)
                .toList();
    }
}
