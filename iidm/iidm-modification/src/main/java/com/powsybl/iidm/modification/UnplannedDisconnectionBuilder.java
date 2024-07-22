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
 * section to which it is currently connected. This builder  should be used if the disconnection is not planned. If it
 * is planned, {@link PlannedDisconnectionBuilder} should be used instead.</p>
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
public class UnplannedDisconnectionBuilder {
    String identifiableId = null;
    boolean openFictitiousSwitches = false;
    ThreeSides side;

    /**
     * Specify the network element to disconnect. It can be either a connectable, an HVDC line or a tie line.
     * @param identifiableId id of the network element to disconnect
     */
    public UnplannedDisconnectionBuilder withIdentifiableId(String identifiableId) {
        this.identifiableId = identifiableId;
        return this;
    }

    /**
     * @deprecated Use {@link UnplannedDisconnectionBuilder#withIdentifiableId(String)} instead
     */
    @Deprecated(since = "6.4.0")
    public UnplannedDisconnectionBuilder withConnectableId(String connectableId) {
        this.identifiableId = connectableId;
        return this;
    }

    public UnplannedDisconnectionBuilder withFictitiousSwitchesOperable(boolean openFictitiousSwitches) {
        this.openFictitiousSwitches = openFictitiousSwitches;
        return this;
    }

    public UnplannedDisconnectionBuilder withSide(ThreeSides side) {
        this.side = side;
        return this;
    }

    public UnplannedDisconnection build() {
        return new UnplannedDisconnection(identifiableId, openFictitiousSwitches, side);
    }
}
