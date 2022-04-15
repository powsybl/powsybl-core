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

    private final String id;

    private final double threePhaseFaultCurrent;

    private final double threePhaseFaultActivePower;

    private final List<FeederResult> feederResults;

    private final ShortCircuitBusResults faultBusResults; // voltages and currents on three phases.

    public FaultResult(String id, double threePhaseFaultCurrent, double threePhaseFaultActivePower,
                       List<FeederResult> feederResults, ShortCircuitBusResults faultBusResults) {
        this.id = Objects.requireNonNull(id);
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
        this.threePhaseFaultActivePower = threePhaseFaultActivePower;
        this.feederResults = List.copyOf(feederResults);
        this.faultBusResults = faultBusResults;
    }

    public FaultResult(String id, double threePhaseFaultCurrent, List<FeederResult> feederResults) {
        this(id, threePhaseFaultCurrent, Double.NaN, feederResults, null);
    }

    public FaultResult(String id, double threePhaseFaultCurrent) {
        this(id, threePhaseFaultCurrent, Double.NaN, Collections.emptyList(), null);
    }

    /**
     * ID of the equipment for which a fault has been simulated. In a first simple approach, the equipment is a voltage
     * level, and no side is needed.
     */
    public String getId() {
        return id;
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
     * The results on three phases for current and voltage at fault bus.
     */
    public ShortCircuitBusResults getFaultBusResults() {
        return faultBusResults;
    }
}
