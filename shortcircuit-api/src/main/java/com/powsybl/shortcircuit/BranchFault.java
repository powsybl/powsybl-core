/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * Class to describe the characteristics of a fault that occurs on a branch and that is to be simulated.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BranchFault extends AbstractFault {

    private final double proportionalLocation;

    /**
     * Creates a branch fault by specifying the impedance, the fault type, and location.
     */
    public BranchFault(String id, String elementId, double r, double x, ConnectionType connection, FaultType faultType, double proportionalLocation) {
        // Here the elementId is the id of a branch.
        super(id, elementId, r, x, connection, faultType);
        this.proportionalLocation = proportionalLocation;
    }

    /**
     * Creates a three-phase branch fault with a specified impedance (connected in series) and location.
     */
    public BranchFault(String id, String elementId, double r, double x, double proportionalLocation) {
        // Here the elementId is the id of a branch.
        this(id, elementId, r, x, ConnectionType.SERIES, FaultType.THREE_PHASE, proportionalLocation);
    }

    /**
     * Creates a branch fault with zero impedance and the specified fault type and location.
     */
    public BranchFault(String id, String elementId, FaultType faultType, double proportionalLocation) {
        // Here the elementId is the id of a branch.
        this(id, elementId, 0.0, 0.0, ConnectionType.SERIES, faultType, proportionalLocation);
    }

    /**
     * Creates a three-phase branch fault with zero impedance.
     */
    public BranchFault(String id, String elementId, double proportionalLocation) {
        // Here the elementId is the id of a branch.
        this(id, elementId, FaultType.THREE_PHASE, proportionalLocation);
    }

    @Override
    public Type getType() {
        return Type.BRANCH;
    }

    /**
     * Get the location of the fault on the branch
     * @return the location of the fault in % of the branch length (with side ONE as reference).
     */
    public double getProportionalLocation() {
        return this.proportionalLocation;
    }
}
