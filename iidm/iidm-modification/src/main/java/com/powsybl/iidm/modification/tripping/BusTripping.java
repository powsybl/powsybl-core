/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.*;

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
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        if (network.getBusBreakerView().getBus(id) == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "BusTripping",
                "Bus '" + id + "' not found");
        }
        return dryRunConclusive;
    }
}
