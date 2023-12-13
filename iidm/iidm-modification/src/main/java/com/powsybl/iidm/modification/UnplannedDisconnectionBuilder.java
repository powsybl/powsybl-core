/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class UnplannedDisconnectionBuilder {
    String connectableId = null;
    boolean openFictitiousSwitches = false;

    public UnplannedDisconnectionBuilder withConnectableId(String connectableId) {
        this.connectableId = connectableId;
        return this;
    }

    public UnplannedDisconnectionBuilder withFictitiousSwitchesOperable(boolean openFictitiousSwitches) {
        this.openFictitiousSwitches = openFictitiousSwitches;
        return this;
    }

    public UnplannedDisconnection build() {
        return new UnplannedDisconnection(connectableId, openFictitiousSwitches);
    }
}
