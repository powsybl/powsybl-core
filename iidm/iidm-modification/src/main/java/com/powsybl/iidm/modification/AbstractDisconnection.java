/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Predicate;

import static com.powsybl.iidm.modification.util.ModificationReports.connectableDisconnectionReport;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractDisconnection extends AbstractNetworkModification {
    final String connectableId;
    final Predicate<Switch> openableSwitches;

    AbstractDisconnection(String connectableId, Predicate<Switch> openableSwitches) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.openableSwitches = openableSwitches;
    }

    public void applyModification(Network network, boolean isPlanned, Logger logger, Reporter reporter) {
        // Add the reporter to the network reporter context
        network.getReporterContext().pushReporter(reporter);

        // Get the connectable
        Connectable<?> connectable = network.getConnectable(connectableId);

        // Disconnect the connectable
        boolean hasBeenDisconnected;
        Reporter poppedReporter;
        try {
            hasBeenDisconnected = connectable.disconnect(openableSwitches);
        } finally {
            poppedReporter = network.getReporterContext().popReporter();
        }

        if (hasBeenDisconnected) {
            logger.info("Connectable {} has been disconnected.", connectableId);
        } else {
            logger.info("Connectable {} has NOT been disconnected.", connectableId);
        }
        connectableDisconnectionReport(poppedReporter, connectable, hasBeenDisconnected, isPlanned);
    }
}
