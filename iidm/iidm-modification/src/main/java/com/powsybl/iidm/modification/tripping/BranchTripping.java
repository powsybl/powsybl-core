/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 *
 */
public class BranchTripping extends AbstractTripping {

    protected final String voltageLevelId;

    private final BiFunction<Network, String, Branch<?>> supplier;

    private static final String NETWORK_MODIFICATION_NAME = "BranchTripping";

    public BranchTripping(String branchId) {
        this(branchId, null);
    }

    public BranchTripping(String branchId, String voltageLevelId) {
        this(branchId, voltageLevelId, Network::getBranch);
    }

    protected BranchTripping(String branchId, String voltageLevelId, BiFunction<Network, String, Branch<?>> supplier) {
        super(branchId);
        this.voltageLevelId = voltageLevelId;
        this.supplier = supplier;
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        Branch<?> branch = supplier.apply(network, id);
        if (branch == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                String.format("Branch %s not found", id));
        }
        if (voltageLevelId == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                "voltageLevelId should not be null");
        }
        if (branch != null && voltageLevelId != null
            && !voltageLevelId.equals(branch.getTerminal1().getVoltageLevel().getId())
            && !voltageLevelId.equals(branch.getTerminal2().getVoltageLevel().getId())) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                String.format("Branch %s is not connected to voltage level %s", id, voltageLevelId));
        }
        return dryRunConclusive;
    }

    protected String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        Branch<?> branch = supplier.apply(network, id);
        if (branch == null) {
            throw createNotFoundException();
        }
        traverseDoubleSidedEquipment(voltageLevelId, branch.getTerminal1(), branch.getTerminal2(), switchesToOpen, terminalsToDisconnect, traversedTerminals, branch.getType().name());
    }

    protected PowsyblException createNotFoundException() {
        return new PowsyblException("Branch '" + id + "' not found");
    }

    protected PowsyblException createNotConnectedException() {
        return new PowsyblException("VoltageLevel '" + voltageLevelId + "' not connected to branch '" + id + "'");
    }

}
