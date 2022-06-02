/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.security.LimitViolation;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Results for one fault computation.
 *
 * @author Boubakeur Brahimi
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public final class FaultResult extends AbstractExtendable<FaultResult> {

    private final Fault fault;

    private final double shortCircuitPower;

    private final Duration timeConstant;

    private final List<FeederResult> feederResults;

    private final List<LimitViolation> limitViolations;

    private final FortescueValue current;

    private final FortescueValue voltage;

    private final List<ShortCircuitBusResults> shortCircuitBusResults;

    public FaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                       List<LimitViolation> limitViolations, FortescueValue current, FortescueValue voltage, List<ShortCircuitBusResults> shortCircuitBusResults,
                       Duration timeConstant) {
        this.fault = Objects.requireNonNull(fault);
        this.shortCircuitPower = shortCircuitPower;
        this.feederResults = feederResults;
        this.limitViolations = limitViolations;
        this.current = Objects.requireNonNull(current);
        this.voltage = voltage;
        this.shortCircuitBusResults = shortCircuitBusResults;
        this.timeConstant = timeConstant;
    }

    public FaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                       List<LimitViolation> limitViolations, FortescueValue current, Duration timeConstant) {
        this(fault, shortCircuitPower, feederResults, limitViolations, current, null, Collections.emptyList(), timeConstant);
    }

    public FaultResult(Fault fault, double shortCircuitPower, List<FeederResult> feederResults,
                       List<LimitViolation> limitViolations, FortescueValue current) {
        this(fault, shortCircuitPower, feederResults, limitViolations, current, null, Collections.emptyList(), null);
    }

    /**
     * The fault associated to the results.
     */
    public Fault getFault() {
        return fault;
    }

    /**
     * Value of the 3-phase short-circuit current for this fault (in A).
     */
    public double getThreePhaseFaultCurrent() {
        return current.getDirectMagnitude();
    }

    /**
     * Value of the short-circuit power for this fault (in MVA).
     */
    public double getShortCircuitPower() {
        return shortCircuitPower;
    }

    /**
     * List of contributions to the three phase fault current of each connectable connected to the equipment
     */
    public List<FeederResult> getFeederResults() {
        return feederResults;
    }

    public List<LimitViolation> getLimitViolations() {
        return limitViolations;
    }

    public double getFeederCurrent(String feederId) {
        for (FeederResult feederResult : feederResults) {
            if (feederResult.getConnectableId().equals(feederId)) {
                return feederResult.getFeederThreePhaseCurrent();
            }
        }
        return Double.NaN;
    }

    /**
     * The results on three phases for current.
     */
    public FortescueValue getCurrent() {
        return current;
    }

    /**
     * The results on three phases for voltage.
     */
    public FortescueValue getVoltage() {
        return voltage;
    }

    /**
     *
     * The duration before reaching the permanent current.
     */
    public Duration getTimeConstant() {
        return timeConstant;
    }

    public List<ShortCircuitBusResults> getShortCircuitBusResults() {
        return shortCircuitBusResults;
    }
}
