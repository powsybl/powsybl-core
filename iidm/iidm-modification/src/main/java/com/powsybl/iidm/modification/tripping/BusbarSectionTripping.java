/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class BusbarSectionTripping extends AbstractTripping {

    public BusbarSectionTripping(String busbarSectionId) {
        super(busbarSectionId);
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        BusbarSection busbarSection = network.getBusbarSection(id);
        if (busbarSection == null) {
            throw new PowsyblException("Busbar section '" + id + "' not found");
        }

        TrippingTopologyTraverser.traverse(busbarSection.getTerminal(), switchesToOpen, terminalsToDisconnect, traversedTerminals);
    }

    @Override
    public String getName() {
        return "BusbarSectionTripping";
    }
}
