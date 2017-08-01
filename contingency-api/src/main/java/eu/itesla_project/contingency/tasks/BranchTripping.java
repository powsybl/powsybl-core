/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.Terminal;
import eu.itesla_project.iidm.network.TwoTerminalsConnectable;

import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BranchTripping extends TrippingTask {

    private final String branchId;
    private final String voltageLevelId;

    public BranchTripping(String branchId) {
        this(branchId, null);
    }

    public BranchTripping(String branchId, String voltageLevelId) {
        this.branchId = Objects.requireNonNull(branchId);
        this.voltageLevelId = voltageLevelId;
    }

    @Override
    public void traverse(Network network, ComputationManager computationManager, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect) {
        Objects.requireNonNull(network);

        TwoTerminalsConnectable branch = network.getBranch(branchId);
        if (branch == null) {
            throw new ITeslaException("Branch '" + branchId + "' not found");
        }
        if (voltageLevelId != null) {
            if (voltageLevelId.equals(branch.getTerminal1().getVoltageLevel().getId())) {
                ContingencyTopologyTraverser.traverse(branch.getTerminal1(), switchesToOpen, terminalsToDisconnect);
            } else if (voltageLevelId.equals(branch.getTerminal2().getVoltageLevel().getId())) {
                ContingencyTopologyTraverser.traverse(branch.getTerminal2(), switchesToOpen, terminalsToDisconnect);
            } else {
                throw new ITeslaException("VoltageLevel '" + voltageLevelId + "' not connected to branch '" + branchId + "'");
            }
        } else {
            ContingencyTopologyTraverser.traverse(branch.getTerminal1(), switchesToOpen, terminalsToDisconnect);
            ContingencyTopologyTraverser.traverse(branch.getTerminal2(), switchesToOpen, terminalsToDisconnect);
        }
    }

}
