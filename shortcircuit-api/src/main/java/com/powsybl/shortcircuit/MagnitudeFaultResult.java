/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.security.LimitViolation;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Results for one fault computation with current magnitude.
 *
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class MagnitudeFaultResult extends AbstractFaultResult {

    private final double current;

    public MagnitudeFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, double current, List<ShortCircuitBusResults> shortCircuitBusResults,
                                Duration timeConstant, Status status) {
        super(fault, status, shortCircuitPower, timeConstant, feederResults, limitViolations, shortCircuitBusResults);
        this.current = current;
    }

    public MagnitudeFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, double current, Duration timeConstant, Status status) {
        this(fault, shortCircuitPower, feederResults, limitViolations, current, Collections.emptyList(), timeConstant, status);
    }

    public MagnitudeFaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                                List<LimitViolation> limitViolations, double current, Status status) {
        this(fault, shortCircuitPower, feederResults, limitViolations, current, Collections.emptyList(), null, status);
    }

    public MagnitudeFaultResult(Fault fault, Status status) {
        this(fault, Double.NaN, null, null, Double.NaN, Collections.emptyList(), null, status);
    }

    /**
     * The three-phase current magnitude.
     */
    public double getCurrent() {
        return current;
    }

}
