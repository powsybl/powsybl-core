/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.ThreeSides;

import java.util.Optional;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ConnectableConnectionBuilder {
    String connectableId = null;
    boolean operateFictitiousSwitches = false;
    boolean operateOnlyBreakers = false;
    Optional<ThreeSides> side = Optional.empty();

    public ConnectableConnectionBuilder withConnectableId(String connectableId) {
        this.connectableId = connectableId;
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
        this.side = Optional.of(side);
        return this;
    }

    public ConnectableConnection build() {
        return new ConnectableConnection(connectableId, operateFictitiousSwitches, operateOnlyBreakers, side);
    }
}
