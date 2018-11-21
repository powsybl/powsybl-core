package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;

import java.util.Objects;

/**
 * A simple, default implementation of {@link Branch.Overload}.
 */
public class OverloadImpl implements Branch.Overload {

    private final CurrentLimits.TemporaryLimit temporaryLimit;

    private final String previousLimitName;

    private final double previousLimit;

    public OverloadImpl(CurrentLimits.TemporaryLimit temporaryLimit, String previousLimitName, double previousLimit) {
        this.temporaryLimit = Objects.requireNonNull(temporaryLimit);
        this.previousLimitName = previousLimitName;
        this.previousLimit = previousLimit;
    }

    @Override
    public CurrentLimits.TemporaryLimit getTemporaryLimit() {
        return temporaryLimit;
    }

    @Override
    public String getPreviousLimitName() {
        return previousLimitName;
    }

    @Override
    public double getPreviousLimit() {
        return previousLimit;
    }
}
