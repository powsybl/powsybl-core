/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;

import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public interface SimulatorInputSupplier<T> {

    /**
     * Return the name of the {@link DynamicSimulationProvider} instance, this provider is compatible with.
     * This method can return null, if this supplier is compatible with any {@link DynamicSimulationProvider} objects.
     *
     * @return The name of a compatible {@link DynamicSimulationProvider}, or null for any
     */
    default String getName() {
        return null;
    }

    /**
     * Return a list of <pre>T</pre> objects specific to a given network
     *
     * @param network The network used to filter the content of the list
     * @param reportNode the reportNode used for functional logs
     *
     * @return A list of <pre>T</pre> for the given network
     */
    List<T> get(Network network, ReportNode reportNode);

    default List<T> get(Network network) {
        return get(network, ReportNode.NO_OP);
    }
}
