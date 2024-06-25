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

    public void applyModification(Network network, boolean isPlanned, boolean throwException, ReportNode reportNode) {
        // Add the reportNode to the network reportNode context
        network.getReportNodeContext().pushReportNode(reportNode);

        // Get the connectable
        Identifiable<?> identifiable = network.getIdentifiable(connectableId);

        // Disconnect the connectable
        if (identifiable == null) {
            logOrThrow(throwException, "Identifiable '" + connectableId + "' not found");
        } else {
            disconnectIdentifiable(identifiable, network, isPlanned, throwException, reportNode);
        }
    }

    private void disconnectIdentifiable(Identifiable<?> identifiable, Network network, boolean isPlanned, boolean throwException, ReportNode reportNode) {
        boolean hasBeenDisconnected;
        try {
            hasBeenDisconnected = disconnect(identifiable, throwException);
        } finally {
            network.getReportNodeContext().popReportNode();
        }

        if (hasBeenDisconnected) {
            LOG.info("Connectable {} has been disconnected ({} disconnection) {}.", connectableId, isPlanned ? "planned" : "unplanned", side == null ? "on each side" : "on side " + side.getNum());
        } else {
            LOG.info("Connectable {} has NOT been disconnected ({} disconnection) {}.", connectableId, isPlanned ? "planned" : "unplanned", side == null ? "on each side" : "on side " + side.getNum());
        }
        connectableDisconnectionReport(reportNode, identifiable, hasBeenDisconnected, isPlanned, side);
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
            logOrThrow(throwException, String.format("Disconnection not implemented for identifiable '%s'", connectableId));
        }
        return hasBeenDisconnected;
    }
}
