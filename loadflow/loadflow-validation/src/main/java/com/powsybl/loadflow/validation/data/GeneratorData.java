package com.powsybl.loadflow.validation.data;

import com.powsybl.loadflow.validation.BalanceTypeGuesser;

import java.util.Objects;

public record GeneratorData(String generatorId,
                            double p, double q, double v,
                            double targetP, double targetQ, double targetV,
                            boolean voltageRegulatorOn,
                            double minP, double maxP, double minQ, double maxQ,
                            boolean connected, boolean mainComponent,
                            BalanceTypeGuesser guesser) implements ValidationData {

    public GeneratorData {
        Objects.requireNonNull(generatorId);
        Objects.requireNonNull(guesser);
    }

    public double expectedP() {
        if (Math.abs(p + targetP) <= guesser.getThreshold()) {
            return targetP;
        }
        return switch (guesser.getBalanceType()) {
            case NONE -> generatorId.equals(guesser.getSlack()) ? -p : targetP;
            case PROPORTIONAL_TO_GENERATION_P_MAX ->
                Math.max(Math.max(0, minP), Math.min(maxP, targetP + maxP * guesser.getKMax()));
            case PROPORTIONAL_TO_GENERATION_P ->
                Math.max(Math.max(0, minP), Math.min(maxP, targetP + targetP * guesser.getKTarget()));
            case PROPORTIONAL_TO_GENERATION_HEADROOM ->
                Math.max(Math.max(0, minP), Math.min(maxP, targetP + (maxP - targetP) * guesser.getKHeadroom()));
        };
    }

    public static GeneratorData createEmpty(String generatorId) {
        return new GeneratorData(generatorId,
                Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN,
                 false,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                false, false,
                BalanceTypeGuesser.NO_BALANCING_GUESSER
        );
    }

    public static Validated<GeneratorData> createEmptyValidated(String generatorId) {
        return new Validated<>(GeneratorData.createEmpty(generatorId), false);
    }
}
