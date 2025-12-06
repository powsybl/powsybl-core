package com.powsybl.loadflow.validation.data;

import java.util.Objects;

public record ValidatedGenerator(String generatorId, double p, double q, double v, double targetP, double targetQ,
                                 double targetV, double expectedP, boolean connected, boolean voltageRegulatorOn,
                                 double minP, double maxP, double minQ, double maxQ, boolean mainComponent,
                                 boolean validated) {
    public ValidatedGenerator {
        Objects.requireNonNull(generatorId);
    }
}
