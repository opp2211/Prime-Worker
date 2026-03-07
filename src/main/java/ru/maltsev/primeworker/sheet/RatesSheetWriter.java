package ru.maltsev.primeworker.sheet;

import ru.maltsev.primeworker.domain.rate.RatesSnapshot;

public interface RatesSheetWriter {
    void updateRates(RatesSnapshot snapshot);
}
