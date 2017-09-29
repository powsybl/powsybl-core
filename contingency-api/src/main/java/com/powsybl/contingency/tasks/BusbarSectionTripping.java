/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.BusbarSection;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.Terminal;

import java.util.Objects;
import java.util.Set;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class BusbarSectionTripping extends AbstractTrippingTask {

    private final String busbarSectionId;

    public BusbarSectionTripping(String busbarSectionId) {
        this.busbarSectionId = Objects.requireNonNull(busbarSectionId);
    }

    @Override
    public void traverse(Network network, ComputationManager computationManager, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect) {
        Objects.requireNonNull(network);

        BusbarSection busbarSection = network.getBusbarSection(busbarSectionId);
        if (busbarSection == null) {
            throw new ITeslaException("Busbar section '" + busbarSectionId + "' not found");
        }

        ContingencyTopologyTraverser.traverse(busbarSection.getTerminal(), switchesToOpen, terminalsToDisconnect);
    }
}
