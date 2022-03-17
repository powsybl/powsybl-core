/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.security.LimitViolation;

import java.util.ArrayList;
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
    // FIXME: direct resistance and reactance missing, zero resistance and reactance will be added for asymmetrical calculations.

    private final String id;

    private final double threePhaseFaultCurrent;

    private final double threePhaseFaultActivePower;

    private DetailedShortCircuitValue voltage = null; // FIXME: could be optional.

    private DetailedShortCircuitValue current = null; // FIXME: could be optional.

    private final List<FeederResult> feederResults; // Could be optional.

    private final List<LimitViolation> limitViolations = new ArrayList<>();

    private final List<ShortCircuitBusResults> shortCircuitBusResults;

    public FaultResult(String id, double threePhaseFaultCurrent, double threePhaseFaultActivePower, List<FeederResult> feederResults,
                       DetailedShortCircuitValue voltage, DetailedShortCircuitValue current, List<ShortCircuitBusResults> shortCircuitBusResults) {
        this.id = Objects.requireNonNull(id);
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
        this.threePhaseFaultActivePower = threePhaseFaultActivePower;
        this.feederResults = List.copyOf(feederResults);
        this.voltage = voltage;
        this.current = current;
        this.shortCircuitBusResults = List.copyOf(shortCircuitBusResults);
    }

    public FaultResult(String id, double threePhaseFaultCurrent, double threePhaseFaultActivePower, List<LimitViolation> limitViolations, List<FeederResult> feederResults) {
        this.id = Objects.requireNonNull(id);
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
        this.threePhaseFaultActivePower = threePhaseFaultActivePower;
        this.limitViolations.addAll(Objects.requireNonNull(limitViolations));
        this.feederResults = List.copyOf(feederResults);
        this.shortCircuitBusResults = Collections.emptyList();
    }

    public FaultResult(String id, double threePhaseFaultCurrent, double threePhaseFaultActivePower, List<LimitViolation> limitViolations) {
        this(id, threePhaseFaultCurrent, threePhaseFaultActivePower, limitViolations, Collections.emptyList());
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
     * The list of limit violations: for instance when the computed short-circuit current on a given equipment is higher
     * than the maximum admissible value for that equipment. In general, the equipment ID can be completed by the side where
     * the violation occurs. In a first simple approach, the equipment is a voltage level, and no side is needed.
     */
    public List<LimitViolation> getLimitViolations() {
        return Collections.unmodifiableList(limitViolations);
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

    public double getThreePhaseFaultActivePower() {
        return threePhaseFaultActivePower;
    }

    public DetailedShortCircuitValue getVoltage() {
        return voltage;
    }

    public DetailedShortCircuitValue getCurrent() {
        return current;
    }

    public List<ShortCircuitBusResults> getShortCircuitBusResults() {
        return Collections.unmodifiableList(shortCircuitBusResults);
    }
}
