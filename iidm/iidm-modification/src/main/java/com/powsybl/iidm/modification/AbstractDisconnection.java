/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

import static com.powsybl.iidm.modification.util.ModificationReports.identifiableDisconnectionReport;

/**
 * <p>This network modification is used to disconnect a network element from the bus or bus bar section to which it is
 * currently connected.</p>
 * <p>It works on:</p>
 * <ul>
 *     <li>Connectables</li>
 *     <li>HVDC lines by disconnecting their converter stations</li>
 *     <li>Tie lines by disconnecting their underlying dangling lines</li>
 * </ul>
 * <p>The user can specify a side of the element to disconnect. If no side is specified, the network modification will
 * try to disconnect every side.</p>
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractDisconnection extends AbstractNetworkModification {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDisconnection.class);
    final String identifiableId;
    final Predicate<Switch> openableSwitches;
    final ThreeSides side;

    AbstractDisconnection(String identifiableId, Predicate<Switch> openableSwitches, ThreeSides side) {
        this.identifiableId = Objects.requireNonNull(identifiableId);
        this.openableSwitches = openableSwitches;
        this.side = side;
    }

    public void applyModification(Network network, boolean isPlanned, boolean throwException, ReportNode reportNode) {
        // Add the reportNode to the network reportNode context
        network.getReportNodeContext().pushReportNode(reportNode);

        // Get the connectable
        Identifiable<?> identifiable = network.getIdentifiable(identifiableId);

        // Disconnect the identifiable if it exists
        if (identifiable == null) {
            logOrThrow(throwException, "Identifiable '" + identifiableId + "' not found");
        } else {
            disconnectIdentifiable(identifiable, network, isPlanned, throwException, reportNode);
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(identifiableId);
        if (identifiable == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (identifiable instanceof Connectable<?> connectable && connectable.getTerminals().stream().noneMatch(Terminal::isConnected)
            || identifiable instanceof TieLine tieLine && !tieLine.getTerminal1().isConnected() && !tieLine.getTerminal2().isConnected()
            || identifiable instanceof HvdcLine hvdcLine && !hvdcLine.getConverterStation1().getTerminal().isConnected() && !hvdcLine.getConverterStation2().getTerminal().isConnected()) {
            // TODO: Some cases are not covered since we don't check if the connection can really happen
            impact = NetworkModificationImpact.HAS_IMPACT_ON_NETWORK;
        }
        return impact;
    }

    private void disconnectIdentifiable(Identifiable<?> identifiable, Network network, boolean isPlanned, boolean throwException, ReportNode reportNode) {
        boolean hasBeenDisconnected;
        try {
            hasBeenDisconnected = disconnect(identifiable, throwException);
        } finally {
            network.getReportNodeContext().popReportNode();
        }

        if (hasBeenDisconnected) {
            LOG.info("Identifiable {} has been disconnected ({} disconnection) {}.", identifiableId, isPlanned ? "planned" : "unplanned", side == null ? "on each side" : "on side " + side.getNum());
        } else {
            LOG.info("Identifiable {} has NOT been disconnected ({} disconnection) {}.", identifiableId, isPlanned ? "planned" : "unplanned", side == null ? "on each side" : "on side " + side.getNum());
        }
        identifiableDisconnectionReport(reportNode, identifiable, hasBeenDisconnected, isPlanned, side);
    }

    private boolean disconnect(Identifiable<?> identifiable, boolean throwException) {
        boolean hasBeenDisconnected = false;
        if (identifiable instanceof Connectable<?> connectable) {
            hasBeenDisconnected = connectable.disconnect(openableSwitches, side);
        } else if (identifiable instanceof TieLine tieLine) {
            hasBeenDisconnected = tieLine.disconnectDanglingLines(openableSwitches, side == null ? null : side.toTwoSides());
        } else if (identifiable instanceof HvdcLine hvdcLine) {
            hasBeenDisconnected = hvdcLine.disconnectConverterStations(openableSwitches, side == null ? null : side.toTwoSides());
        } else {
            logOrThrow(throwException, String.format("Disconnection not implemented for identifiable '%s'", identifiableId));
        }
        return hasBeenDisconnected;
    }
}
