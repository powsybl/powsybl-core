/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.cleanNodeBreakerTopology;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * This modification removes the whole feeder bay related to a given feeder connectable.
 * This means that it removes all the dangling switches and internal connections which remain once the connectable is removed.
 * Note that determining the bay which corresponds to a connectable needs some computation and graph traversals.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class RemoveFeederBay extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveFeederBay.class);

    private final String connectableId;

    /**
     * Constructor
     * @param connectableId non-null id of the connectable whose feeder bay will be removed (busbar section are not accepted)
     */
    public RemoveFeederBay(String connectableId) {
        this.connectableId = Objects.requireNonNull(connectableId);
    }

    @Override
    public String getName() {
        return "RemoveFeederBay";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Connectable<?> connectable = network.getConnectable(connectableId);
        if (!checkConnectable(throwException, reportNode, connectable)) {
            return;
        }

        cleanNodeBreakerTopology(network, connectableId, reportNode);
        connectable.remove();
        removedConnectableReport(reportNode, connectableId);
        LOGGER.info("Connectable {} removed", connectableId);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Connectable<?> connectable = network.getConnectable(connectableId);
        if (connectable == null || connectable instanceof BusbarSection) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private boolean checkConnectable(boolean throwException, ReportNode reportNode, Connectable<?> connectable) {
        if (connectable instanceof BusbarSection) {
            removeFeederBayBusbarSectionReport(reportNode, connectableId);
            logOrThrow(throwException, "BusbarSection connectables are not allowed as RemoveFeederBay input: " + connectableId);
            return false;
        }
        if (connectable == null) {
            notFoundConnectableReport(reportNode, connectableId);
            logOrThrow(throwException, "Connectable not found: " + connectableId);
            return false;
        }
        return true;
    }
}
