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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

import static com.powsybl.iidm.modification.util.ModificationReports.connectableConnectionReport;

/**
 * <p>This network modification is used to connect a network element to the closest bus or bus bar section.</p>
 * <p>It works on:</p>
 * <ul>
 *     <li>Connectables by connecting their terminals</li>
 *     <li>HVDC lines by connecting the terminals of their converter stations</li>
 *     <li>Tie lines by connecting the terminals of their underlying dangling lines</li>
 * </ul>
 * <p>The user can specify a side of the element to connect. If no side is specified, the network modification will
 * try to connect every side.</p>
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ConnectableConnection extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectableConnection.class);
    final String connectableId;
    final Predicate<Switch> isTypeSwitchToOperate;
    ThreeSides side;

    ConnectableConnection(String connectableId, boolean openFictitiousSwitches, boolean operateOnlyBreakers,
                          ThreeSides side) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.side = side;

        // Initial predicate
        Predicate<Switch> predicate = SwitchPredicates.IS_NON_NULL;

        // If selected, only non-fictitious switches can be operated
        if (!openFictitiousSwitches) {
            predicate = predicate.and(SwitchPredicates.IS_NONFICTIONAL);
        }

        // If selected, only breakers can be operated
        if (operateOnlyBreakers) {
            predicate = predicate.and(SwitchPredicates.IS_BREAKER);
        }

        // Save the predicate
        this.isTypeSwitchToOperate = predicate;
    }

    @Override
    public String getName() {
        return "ConnectableConnection";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        // Get the connectable
        Identifiable<?> identifiable = network.getIdentifiable(connectableId);

        // Add the reportNode to the network reportNode context
        network.getReportNodeContext().pushReportNode(reportNode);

        // Connect the element if it exists
        if (identifiable == null) {
            logOrThrow(throwException, "Identifiable '" + connectableId + "' not found");
        } else {
            connectIdentifiable(identifiable, network, throwException, reportNode);
        }
    }

    private void connectIdentifiable(Identifiable<?> identifiable, Network network, boolean throwException, ReportNode reportNode) {
        boolean hasBeenConnected = false;
        try {
            if (identifiable instanceof Connectable<?> connectable) {
                hasBeenConnected = connectable.connect(isTypeSwitchToOperate, side);
            } else if (identifiable instanceof TieLine tieLine) {
                hasBeenConnected = tieLine.connectDanglingLines(isTypeSwitchToOperate, side == null ? null : side.toTwoSides());
            } else if (identifiable instanceof HvdcLine hvdcLine) {
                hasBeenConnected = hvdcLine.connectConverterStations(isTypeSwitchToOperate, side == null ? null : side.toTwoSides());
            } else {
                logOrThrow(throwException, String.format("Connection not implemented for identifiable '%s'", connectableId));
            }
        } finally {
            network.getReportNodeContext().popReportNode();
        }

        if (hasBeenConnected) {
            LOG.info("Connectable {} has been connected {}.", connectableId, side == null ? "on each side" : "on side " + side.getNum());
        } else {
            LOG.info("Connectable {} has NOT been connected {}.", connectableId, side == null ? "on each side" : "on side " + side.getNum());
        }
        connectableConnectionReport(reportNode, identifiable, hasBeenConnected, side);
    }
}
