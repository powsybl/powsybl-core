/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 *
 */
public class BranchTripping extends AbstractTripping {

    private final String branchId;
    private final String voltageLevelId;
    private final BiFunction<Network, String, Branch<?>> supplier;

    public BranchTripping(String branchId) {
        this(branchId, null);
    }

    public BranchTripping(String branchId, String voltageLevelId) {
        this(branchId, voltageLevelId, Network::getBranch);
    }

    protected BranchTripping(String branchId, String voltageLevelId, BiFunction<Network, String, Branch<?>> supplier) {
        this.branchId = Objects.requireNonNull(branchId);
        this.voltageLevelId = voltageLevelId;
        this.supplier = supplier;
    }

    protected String getBranchId() {
        return branchId;
    }

    protected String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect) {
        Objects.requireNonNull(network);

        Branch<?> branch = supplier.apply(network, branchId);
        if (branch == null) {
            throw createNotFoundException();
        }
        if (voltageLevelId != null) {
            if (voltageLevelId.equals(branch.getTerminal1().getVoltageLevel().getId())) {
                TrippingTopologyTraverser.traverse(branch.getTerminal1(), switchesToOpen, terminalsToDisconnect);
            } else if (voltageLevelId.equals(branch.getTerminal2().getVoltageLevel().getId())) {
                TrippingTopologyTraverser.traverse(branch.getTerminal2(), switchesToOpen, terminalsToDisconnect);
            } else {
                throw createNotConnectedException();
            }
        } else {
            TrippingTopologyTraverser.traverse(branch.getTerminal1(), switchesToOpen, terminalsToDisconnect);
            TrippingTopologyTraverser.traverse(branch.getTerminal2(), switchesToOpen, terminalsToDisconnect);
        }
    }

    protected PowsyblException createNotFoundException() {
        return new PowsyblException("Branch '" + branchId + "' not found");
    }

    protected PowsyblException createNotConnectedException() {
        return new PowsyblException("VoltageLevel '" + voltageLevelId + "' not connected to branch '" + branchId + "'");
    }

}
