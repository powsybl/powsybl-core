/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface Boundary {

    /**
     * Get the voltage of the boundary fictitious bus.
     */
    double getV();

    /**
     * Get the angle of the boundary fictitious bus.
     */
    double getAngle();

    /**
     * Get the active power at the fictitious terminal going from the boundary fictitious bus to the network.
     */
    double getP();

    /**
     * Get the reactive power at the fictitious terminal going from the boundary fictitious bus to the network.
     */
    double getQ();

    /**
     * Get the danglingLine the boundary is associated to.
     */
    DanglingLine getDanglingLine();

    /**
     * Get the voltage level at network side.
     */
    default VoltageLevel getNetworkSideVoltageLevel() {
        return getDanglingLine().getTerminal().getVoltageLevel();
    }
}
