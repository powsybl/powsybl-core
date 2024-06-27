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
 * <p>This builder help creating the network modification used to connect a network element to the closest bus or bus
 * bar section. It works on:</p>
 * <ul>
 *     <li>Connectables by connecting their terminals</li>
 *     <li>HVDC lines by connecting the terminals of their converter stations</li>
 *     <li>Tie lines by connecting the terminals of their underlying dangling lines</li>
 * </ul>
 * <p>The user can specify a side of the element to connect. If no side is specified, the network modification will
 * try to connect every side.</p>
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ConnectableConnectionBuilder {
    String identifiableId = null;
    boolean operateFictitiousSwitches = false;
    boolean operateOnlyBreakers = false;
    ThreeSides side;

    /**
     * Specify the network element to connect. It can be either a connectable, an HVDC line or a tie line.
     * @param identifiableId id of the network element to connect
     */
    public ConnectableConnectionBuilder withIdentifiableId(String identifiableId) {
        this.identifiableId = identifiableId;
        return this;
    }

    public ConnectableConnectionBuilder withFictitiousSwitchesOperable(boolean operateFictitiousSwitches) {
        this.operateFictitiousSwitches = operateFictitiousSwitches;
        return this;
    }

    public ConnectableConnectionBuilder withOnlyBreakersOperable(boolean operateOnlyBreakers) {
        this.operateOnlyBreakers = operateOnlyBreakers;
        return this;
    }

    public ConnectableConnectionBuilder withSide(ThreeSides side) {
        this.side = side;
        return this;
    }

    public ConnectableConnection build() {
        return new ConnectableConnection(identifiableId, operateFictitiousSwitches, operateOnlyBreakers, side);
    }
}
