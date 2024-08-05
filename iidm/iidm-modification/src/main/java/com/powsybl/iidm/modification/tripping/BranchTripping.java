/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
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

    @Override
    public String getName() {
        return "BranchTripping";
    }
}
