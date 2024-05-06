/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.security.LimitViolation;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Results for one fault computation with currents and voltage on the three phases.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class FortescueFaultResult extends AbstractFaultResult {

    private final FortescueValue current;

    private final FortescueValue voltage;

    public FortescueFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, FortescueValue current, FortescueValue voltage, List<ShortCircuitBusResults> shortCircuitBusResults,
                                Duration timeConstant, Status status) {
        super(fault, status, shortCircuitPower, timeConstant, feederResults, limitViolations, shortCircuitBusResults);
        this.current = current;
        this.voltage = voltage;
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

}
