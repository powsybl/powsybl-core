package com.powsybl.loadflow.validation.data;

import com.powsybl.iidm.network.StaticVarCompensator;

import java.util.Objects;

public record SvcData(String svcId,
                      double p, double q,
                      double vControlled, double vController,
                      double nominalVcontroller,
                      double reactivePowerSetpoint, double voltageSetpoint,
                      StaticVarCompensator.RegulationMode regulationMode, boolean regulating,
                      double bMin, double bMax,
                      boolean connected, boolean mainComponent) {
    public SvcData {
        Objects.requireNonNull(svcId);
    }
    public static SvcData createEmpty(String svcId) {
        return new SvcData(svcId,
                Double.NaN, Double.NaN,
                Double.NaN, Double.NaN,
                Double.NaN,
                Double.NaN, Double.NaN,
                null, false,
                Double.NaN, Double.NaN,
                false, false
        );
    }

    public static Validated<SvcData> createEmptyValidated(String svcId) {
        return new Validated<>(SvcData.createEmpty(svcId), false);
    }
}
