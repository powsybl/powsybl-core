/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.ThreeSides;

/**
 * <p>This builder help creating the network modification used to disconnect a network element from the bus or bus bar
 * section to which it is currently connected. This builder should be used if the disconnection is planned.
 * If it is not, {@link UnplannedDisconnectionBuilder} should be used instead.</p>
 * <p>It works on:</p>
 * <ul>
 *     <li>Connectables</li>
 *     <li>HVDC lines by disconnecting their converter stations</li>
 *     <li>Tie lines by disconnecting their underlying dangling lines</li>
 * </ul>
 * <p>The user can specify a side of the element to disconnect. If no side is specified, the network modification will
 * try to disconnect every side.</p>
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PlannedDisconnectionBuilder {
    String identifiableId = null;
    boolean openFictitiousSwitches = false;
    ThreeSides side;

    /**
     * Specify the network element to disconnect. It can be either a connectable, an HVDC line or a tie line.
     * @param identifiableId id of the network element to disconnect
     */
    public PlannedDisconnectionBuilder withIdentifiableId(String identifiableId) {
        this.identifiableId = identifiableId;
        return this;
    }

    public PlannedDisconnectionBuilder withFictitiousSwitchesOperable(boolean openFictitiousSwitches) {
        this.openFictitiousSwitches = openFictitiousSwitches;
        return this;
    }

    public PlannedDisconnectionBuilder withSide(ThreeSides side) {
        this.side = side;
        return this;
    }

    public PlannedDisconnection build() {
        return new PlannedDisconnection(identifiableId, openFictitiousSwitches, side);
    }
}
