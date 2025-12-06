package com.powsybl.loadflow.validation.data;

import java.util.Objects;

public record BusData(String busId,
                      double loadP, double loadQ,
                      double genP, double genQ,
                      double batP, double batQ,
                      double shuntP, double shuntQ,
                      double svcP, double svcQ,
                      double vscCSP, double vscCSQ,
                      double lineP, double lineQ,
                      double danglingLineP, double danglingLineQ,
                      double twtP, double twtQ,
                      double tltP, double tltQ,
                      boolean mainComponent) implements ValidationData {
    public BusData {
        Objects.requireNonNull(busId);
    }

    public double incomingP() {
        return genP + batP + shuntP + svcP + vscCSP + lineP + danglingLineP + twtP + tltP;
    }

    public double incomingQ() {
        return genQ + batQ + shuntQ + svcQ + vscCSQ + lineQ + danglingLineQ + twtQ + tltQ;
    }

    public static BusData createEmpty(String busId) {
        return new BusData(busId, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, false);
    }

    public static Validated<BusData> createEmptyValidated(String busId) {
        return new Validated<>(BusData.createEmpty(busId), false);
    }
}
