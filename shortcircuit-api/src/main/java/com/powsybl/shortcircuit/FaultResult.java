/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtendable;

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

    private final double threePhaseFaultCurrent;

    private final double threePhaseFaultActivePower;

    private final double timeConstant;

    private final List<FeederResult> feederResults;

    private final ThreePhaseValue current; // FIXME optional?

    private final ThreePhaseValue voltage; // FIXME optional?

    public FaultResult(Fault fault, double threePhaseFaultCurrent, double threePhaseFaultActivePower,
                       List<FeederResult> feederResults, ThreePhaseValue current, ThreePhaseValue voltage, double timeConstant) {
        this.fault = Objects.requireNonNull(fault);
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
        this.threePhaseFaultActivePower = threePhaseFaultActivePower;
        this.feederResults = List.copyOf(feederResults);
        this.current = current;
        this.voltage = voltage;
        this.timeConstant = timeConstant;
    }

    public FaultResult(Fault fault, double threePhaseFaultCurrent, List<FeederResult> feederResults) {
        this(fault, threePhaseFaultCurrent, Double.NaN, feederResults, null, null, Double.NaN);
    }

    public FaultResult(Fault fault, double threePhaseFaultCurrent, List<FeederResult> feederResults, double timeConstant) {
        this(fault, threePhaseFaultCurrent, Double.NaN, feederResults, null, null, timeConstant);
    }

    public FaultResult(Fault fault, double threePhaseFaultCurrent, double timeConstant) {
        this(fault, threePhaseFaultCurrent, Double.NaN, Collections.emptyList(), null, null, timeConstant);
    }

    public FaultResult(Fault fault, double threePhaseFaultCurrent) {
        this(fault, threePhaseFaultCurrent, Double.NaN, Collections.emptyList(), null, null, Double.NaN);
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
        return threePhaseFaultCurrent;
    }

    /**
     * Value of the 3-phase short-circuit active power for this fault (in MVA).
     */
    public double getThreePhaseFaultActivePower() {
        return threePhaseFaultActivePower;
    }

    /**
     * List of contributions to the three phase fault current of each connectable connected to the equipment
     */
    public List<FeederResult> getFeederResults() {
        return Collections.unmodifiableList(feederResults);
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
    public ThreePhaseValue getCurrent() {
        return current;
    }

    /**
     * The results on three phases for voltage.
     */
    public ThreePhaseValue getVoltage() {
        return voltage;
    }

    /**
     *
     * The duration before reaching the permanent current.
     */
    public double getTimeConstant() {
        return timeConstant;
    }
}
