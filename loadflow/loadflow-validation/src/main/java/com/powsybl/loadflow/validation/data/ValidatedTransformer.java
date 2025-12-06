package com.powsybl.loadflow.validation.data;

import com.powsybl.iidm.network.TwoSides;

import java.util.Objects;

public record ValidatedTransformer(String twtId, double error, double upIncrement, double downIncrement, double rho,
                                   double rhoPreviousStep, double rhoNextStep, int tapPosition, int lowTapPosition,
                                   int highTapPosition, double targetV, TwoSides regulatedSide, double v,
                                   boolean connected, boolean mainComponent, boolean validated) {
    public ValidatedTransformer {
        Objects.requireNonNull(twtId);
    }
}
