/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.stream.Stream;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcBus extends Identifiable<DcBus> {

    /**
     * Get the network to which the DC bus belongs.
     */
    Network getNetwork();

    /**
     * Get the voltage of the bus in kV.
     */
    double getV();

    /**
     * Set the voltage of the bus in kV.
     */
    DcBus setV(double v);

    /**
     * Get the connected component that the bus is part of.
     */
    Component getConnectedComponent();

    /**
     * Check if the bus belongs to the main connected component
     * @return true if the bus belongs to the main connected component, false otherwise
     */
    boolean isInMainConnectedComponent();

    /**
     * Get the direct current component that the bus is part of.
     */
    Component getDcComponent();

    /**
     * Get the DC Nodes part of the bus.
     */
    Iterable<DcNode> getDcNodes();

    /**
     * Get the DC Nodes part of the bus.
     */
    Stream<DcNode> getDcNodeStream();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_BUS;
    }
}
