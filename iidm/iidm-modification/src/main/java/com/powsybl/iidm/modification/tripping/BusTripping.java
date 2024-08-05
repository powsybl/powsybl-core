/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Set;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class BusTripping extends AbstractTripping {

    public BusTripping(String id) {
        super(id);
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        Bus bus = network.getBusBreakerView().getBus(id);
        if (bus == null) {
            throw new PowsyblException("Bus section '" + id + "' not found");
        }

        for (Terminal t : bus.getConnectedTerminals()) {
            TrippingTopologyTraverser.traverse(t, switchesToOpen, terminalsToDisconnect, traversedTerminals);
        }
    }

    @Override
    public String getName() {
        return "BusTripping";
    }
}
