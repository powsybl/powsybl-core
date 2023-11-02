/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Network;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class Subcomponent implements Component {

    private final Component parent;
    private final Network subnetwork;

    Subcomponent(Component parent, Network subnetwork) {
        this.parent = parent;
        this.subnetwork = subnetwork;
    }

    @Override
    public int getNum() {
        return parent.getNum();
    }

    @Override
    public int getSize() {
        return parent.getSize();
    }

    @Override
    public Iterable<Bus> getBuses() {
        return getBusStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<Bus> getBusStream() {
        return parent.getBusStream().filter(b -> b.getParentNetwork() == subnetwork);
    }
}
