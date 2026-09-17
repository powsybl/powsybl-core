/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.contingency.violations.LimitViolation;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Results of the short-circuit calculation with the voltage and currents detailed on the three phases.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class FortescueFaultResult extends AbstractFaultResult {

    private final FortescueValue current;

    private final FortescueValue voltage;

    private final double equivalentRZero;

    private final double equivalentXZero;

    public FortescueFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, FortescueValue current, FortescueValue voltage, List<ShortCircuitBusResults> shortCircuitBusResults,
                                Duration timeConstant, Status status, double equivalentR, double equivalentRZero,
                                double equivalentX, double equivalentXZero) {
        super(fault, status, shortCircuitPower, timeConstant, feederResults, limitViolations, shortCircuitBusResults, equivalentR, equivalentX);
        this.current = current;
        this.voltage = voltage;
        this.equivalentRZero = equivalentRZero;
        this.equivalentXZero = equivalentXZero;
    }

    public FortescueFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, FortescueValue current, FortescueValue voltage, List<ShortCircuitBusResults> shortCircuitBusResults,
                                Duration timeConstant, Status status) {
        this(fault, shortCircuitPower, feederResults, limitViolations, current, voltage, shortCircuitBusResults, timeConstant, status, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public FortescueFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, FortescueValue current, Duration timeConstant, Status status) {
        this(fault, shortCircuitPower, feederResults, limitViolations, current, null, Collections.emptyList(), timeConstant, status);
    }

    public FortescueFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, FortescueValue current, Status status) {
        this(fault, shortCircuitPower, feederResults, limitViolations, current, null, Collections.emptyList(), null, status);
    }

    public FortescueFaultResult(Fault fault, Status status) {
        this(fault, Double.NaN, null, null, null, null, Collections.emptyList(), null, status);
    }

    public FortescueFaultResult(Fault fault, double shortCircuitPower, FortescueValue current, FortescueValue voltage,
                                Duration timeConstant, Status status, double equivalentR, double equivalentRZero, double equivalentX, double equivalentXZero) {
        this(fault, shortCircuitPower, Collections.emptyList(), Collections.emptyList(), current, voltage, Collections.emptyList(),
                timeConstant, status, equivalentR, equivalentRZero, equivalentX, equivalentXZero);
    }

    /**
     * The results on three phases for current [in A]
     */
    public FortescueValue getCurrent() {
        return current;
    }

    /**
     * The results on three phases for voltage [in kV].
     */
    public FortescueValue getVoltage() {
        return voltage;
    }

    /**
     * The zero-sequence equivalent resistance of the network seen from the fault.
     */
    public double getEquivalentRZero() {
        return equivalentRZero;
    }

    /**
     * The zero-sequence equivalent three-phase reactance of the network seen from the fault.
     */
    public double getEquivalentXZero() {
        return equivalentXZero;
    }

}
