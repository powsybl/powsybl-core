package com.powsybl.loadflow.validation.data;

import com.powsybl.iidm.network.StaticVarCompensator;

import java.util.Objects;

public record SvcData(String svcId, double p, double q, double vControlled, double vController,
                      double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                      boolean connected, StaticVarCompensator.RegulationMode regulationMode, boolean regulating,
                      double bMin,
                      double bMax, boolean mainComponent, boolean validated) {
    public SvcData {
        Objects.requireNonNull(svcId);
    }
}
