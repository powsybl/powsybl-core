package com.powsybl.loadflow.validation.data;

import com.powsybl.iidm.network.TwoSides;

import java.util.Objects;

public record TransformerData(String twtId,
                              double rho, double rhoPreviousStep, double rhoNextStep,
                              int tapPosition, int lowTapPosition, int highTapPosition,
                              double targetV, TwoSides regulatedSide,
                              double v,
                              boolean connected, boolean mainComponent) {
    public TransformerData {
        Objects.requireNonNull(twtId);
    }

    public static TransformerData createEmpty(String twtId) {
        return new TransformerData(twtId,
                Double.NaN, Double.NaN, Double.NaN,
                -1, -1, -1,
                Double.NaN, TwoSides.ONE,
                Double.NaN,
                false, false
        );
    }

    public static Validated<TransformerData> createEmptyValidated(String twtId) {
        return new Validated<>(TransformerData.createEmpty(twtId), false);
    }

    public double error() {
        return v - targetV;
    }

    public double upIncrement() {
        return Double.isNaN(rhoNextStep) ? Double.NaN : evaluateVoltage(regulatedSide, v, rho, rhoNextStep) - v;
    }

    public double downIncrement() {
        return Double.isNaN(rhoPreviousStep) ? Double.NaN : evaluateVoltage(regulatedSide, v, rho, rhoPreviousStep) - v;
    }

    /**
     *  Evaluates the voltage value for a transformation ratio different from the current ratio,
     *  assuming "nothing else changes": voltage on the other side is kept constant,
     *  voltage decrease through the impedance is kept constant (perfect transformer approximation).
     */
    private static double evaluateVoltage(TwoSides regulatedSide, double voltage, double ratio, double nextRatio) {
        if (regulatedSide == null) {
            return Double.NaN;
        }
        return switch (regulatedSide) {
            case ONE -> voltage * ratio / nextRatio;
            case TWO -> voltage * nextRatio / ratio;
        };
    }
}
