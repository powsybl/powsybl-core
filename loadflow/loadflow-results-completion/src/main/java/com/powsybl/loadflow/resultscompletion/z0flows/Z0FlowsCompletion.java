/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.*;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author Marcos De Miguel {@literal <demiguelm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class Z0FlowsCompletion {

    public Z0FlowsCompletion(Network network, Z0LineChecker z0checker) {
        this.network = Objects.requireNonNull(network);
        this.z0checker = Objects.requireNonNull(z0checker);
    }

    public void complete() {
        z0busGroups().forEach(Z0BusGroup::complete);
    }

    private List<Z0BusGroup> z0busGroups() {
        List<Z0BusGroup> z0busGroups = new ArrayList<>();
        network.getBusView().getBusStream().forEach(bus -> {
            if (!processed.contains(bus)) {
                Z0BusGroup z0bg = new Z0BusGroup(bus, z0checker);
                z0bg.exploreZ0(processed);
                if (z0bg.valid()) {
                    z0busGroups.add(z0bg);
                }
            }
        });
        return z0busGroups;
    }

    private final Network network;
    private final Z0LineChecker z0checker;
    private final Set<Bus> processed = new HashSet<>();
}
