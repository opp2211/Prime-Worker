package ru.maltsev.primeworker.domain.rate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record RateValue(RateKind kind, BigDecimal value) {
    public RateValue {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(value, "value");
    }

    public BigDecimal scaled() {
        return value.setScale(kind.getScale(), RoundingMode.DOWN);
    }

    public String formatComma() {
        return scaled().toPlainString().replace('.', ',');
    }
}
