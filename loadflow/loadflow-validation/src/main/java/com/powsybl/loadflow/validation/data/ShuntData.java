package com.powsybl.loadflow.validation.data;

import java.util.Objects;

public record ShuntData(String shuntId, double q, double expectedQ, double p, int currentSectionCount,
                        int maximumSectionCount, double bPerSection, double v, boolean connected, double qMax,
                        double nominalV, boolean mainComponent, boolean validated) {
    public ShuntData {
        Objects.requireNonNull(shuntId);
    }
}
