/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TwoTerminalsConnectable;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BranchTripping implements ModificationTask {

    private final String branchId;
    private final String substationId;

    public BranchTripping(String branchId) {
        this(branchId, null);
    }

    public BranchTripping(String branchId, String substationId) {
        this.branchId = Objects.requireNonNull(branchId);
        this.substationId = substationId;
    }

    @Override
    public void modify(Network network) {
        TwoTerminalsConnectable branch = network.getLine(branchId);
        if (branch == null) {
            branch = network.getTwoWindingsTransformer(branchId);
            if (branch == null) {
                throw new ITeslaException("Branch '" + branchId + "' not found");
            }
        }
        if (substationId != null) {
            if (substationId.equalsIgnoreCase(branch.getTerminal1().getVoltageLevel().getSubstation().getId())) {
                branch.getTerminal1().disconnect();
            } else if (substationId.equalsIgnoreCase(branch.getTerminal2().getVoltageLevel().getSubstation().getId())) {
                branch.getTerminal2().disconnect();
            } else {
                throw new ITeslaException("Substation '" + substationId + "' not connected to branch '" + branchId + "'");
            }
        } else {
            branch.getTerminal1().disconnect();
            branch.getTerminal2().disconnect();
        }
    }

}
