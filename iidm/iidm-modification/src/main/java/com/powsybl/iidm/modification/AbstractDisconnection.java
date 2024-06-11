/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeSides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

import static com.powsybl.iidm.modification.util.ModificationReports.connectableDisconnectionReport;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractDisconnection extends AbstractNetworkModification {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDisconnection.class);
    final String connectableId;
    final Predicate<Switch> openableSwitches;
    final ThreeSides side;

    AbstractDisconnection(String connectableId, Predicate<Switch> openableSwitches, ThreeSides side) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.openableSwitches = openableSwitches;
        this.side = side;
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        Connectable<?> connectable = network.getConnectable(connectableId);
        if (connectable == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "AbstractDisconnection",
                "Connectable '" + connectableId + "' not found");
        } else if (!connectable.disconnect(openableSwitches, side, true)) {
            // TODO : differenciate the cases where the connectable does not exist, where it is already disconnected, where it cannot be disconnected, etc.
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "AbstractDisconnection",
                "Disconnection failed");
        }
        return dryRunConclusive;
    }

    @Override
    public boolean hasImpactOnNetwork() {
        return false;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        // TODO: see TODO in applyDryRun
        return true;
    }

    public void applyModification(Network network, boolean isPlanned, ReportNode reportNode) {
        // Add the reportNode to the network reportNode context
        network.getReportNodeContext().pushReportNode(reportNode);

        // Get the connectable
        Connectable<?> connectable = network.getConnectable(connectableId);

        // Disconnect the connectable
        boolean hasBeenDisconnected;
        try {
            hasBeenDisconnected = connectable.disconnect(openableSwitches, side);
        } finally {
            network.getReportNodeContext().popReportNode();
        }

        if (hasBeenDisconnected) {
            LOG.info("Connectable {} has been disconnected ({} disconnection) {}.", connectableId, isPlanned ? "planned" : "unplanned", side == null ? "on each side" : "on side " + side.getNum());
        } else {
            LOG.info("Connectable {} has NOT been disconnected ({} disconnection) {}.", connectableId, isPlanned ? "planned" : "unplanned", side == null ? "on each side" : "on side " + side.getNum());
        }
        connectableDisconnectionReport(reportNode, connectable, hasBeenDisconnected, isPlanned, side);
    }
}
