/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.security.LimitViolation;

import java.time.Duration;
import java.util.List;

/**
 * Interface to describe the result of the short-circuit analysis for a given fault.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface FaultResult extends Extendable<FaultResult> {

    /**
     * The status of the computation.
     */
    enum Status {
        /**
         * The computation went ok and no error was returned
         */
        SUCCESS,
        /**
         * Data useful to short-circuit current calculation is missing, typically the transient reactance of generators
         */
        NO_SHORT_CIRCUIT_DATA,
        /**
         * The computation failed due to an error in the solver.
         */
        SOLVER_FAILURE,
        /**
         *  The computation failed due to something not related to the solver.
         */
        FAILURE
    }

    /**
     * The fault associated to the results.
     */
    Fault getFault();

    /**
     * Value of the short-circuit power for this fault (in MVA).
     */
    double getShortCircuitPower();

    /**
     * List of contributions to the three-phase fault current of each connectable connected to the equipment.
     */
    List<FeederResult> getFeederResults();

    /**
     * List of violations in current after the fault.
     */
    List<LimitViolation> getLimitViolations();

    /**
     * The duration before reaching the permanent current.
     */
    Duration getTimeConstant();

    /**
     * The voltage results on the network.
     */
    List<ShortCircuitBusResults> getShortCircuitBusResults();

    /**
     * The computation status.
     */
    Status getStatus();

    /**
     * Returns the three-phase current associated to a feeder.
     */
    double getFeederCurrent(String feederId);
}
