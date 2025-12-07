package com.powsybl.loadflow.validation.data;

import java.util.Objects;

public record ShuntData(String shuntId,
                        double p, double q,
                        int currentSectionCount, int maximumSectionCount,
                        double bPerSection,
                        double qMax,
                        double v, boolean connected,
                        double nominalV,
                        boolean mainComponent) implements ValidationData {
    public ShuntData {
        Objects.requireNonNull(shuntId);
    }

    public double expectedQ() {
        // “q” = - bPerSection * currentSectionCount * v^2
        return -bPerSection * currentSectionCount * v * v;
    }

    public static ShuntData createEmpty(String shuntId) {
        return new ShuntData(shuntId,
                Double.NaN, Double.NaN,
                -1, -1,
                Double.NaN,
                Double.NaN,
                Double.NaN, false,
                Double.NaN,
                false
                );
    }

    public static Validated<ShuntData> createEmptyValidated(String shuntId) {
        return new Validated<>(ShuntData.createEmpty(shuntId), false);
    }
}
