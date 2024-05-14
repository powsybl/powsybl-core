/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * Class to describe the characteristics of the fault to be simulated.
 * Used for elementary short-circuit analysis only.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BranchFault extends AbstractFault {

    // Location of the fault in % of the branch length (with side ONE as reference).
    private final double proportionalLocation;

    public BranchFault(String id, String elementId, double r, double x, ConnectionType connection, FaultType faultType, double proportionalLocation) {
        // Here the elementId is the id of a branch.
        super(id, elementId, r, x, connection, faultType);
        this.proportionalLocation = proportionalLocation;
    }

    public BranchFault(String id, String elementId, double r, double x, double proportionalLocation) {
        // Here the elementId is the id of a bus from the bus view.
        this(id, elementId, r, x, ConnectionType.SERIES, FaultType.THREE_PHASE, proportionalLocation);
    }

    public BranchFault(String id, String elementId, double proportionalLocation) {
        // Here the elementId is the id of a bus from the bus view.
        this(id, elementId, 0.0, 0.0, ConnectionType.SERIES, FaultType.THREE_PHASE, proportionalLocation);
    }

    @Override
    public Type getType() {
        return Type.BRANCH;
    }

    public double getProportionalLocation() {
        return this.proportionalLocation;
    }
}
