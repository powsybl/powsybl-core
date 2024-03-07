/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.computation.ComputationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationReports.notFoundVoltageLevelReport;
import static com.powsybl.iidm.modification.util.ModificationReports.removedVoltageLevelReport;

/**
 * @author Etienne Homer {@literal <etienne.homer at rte-france.com>}
 */
public class RemoveVoltageLevel extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveVoltageLevel.class);

    private final String voltageLevelId;

    public RemoveVoltageLevel(String voltageLevelId) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            LOGGER.error("Voltage level {} not found", voltageLevelId);
            notFoundVoltageLevelReport(reportNode, voltageLevelId);
            if (throwException) {
                throw new PowsyblException("Voltage level not found: " + voltageLevelId);
            }
            return;
        }

        voltageLevel.getConnectables(HvdcConverterStation.class).forEach(hcs -> {
            if (hcs.getHvdcLine() != null) {
                new RemoveHvdcLineBuilder().withHvdcLineId(hcs.getHvdcLine().getId()).build().apply(network, throwException, computationManager, reportNode);
            }
        });

        voltageLevel.getConnectables().forEach(connectable -> {
            if (connectable instanceof Injection) {
                connectable.remove();
            } else {
                new RemoveFeederBayBuilder().withConnectableId(connectable.getId()).build().apply(network, throwException, computationManager, reportNode);
            }
        });

        voltageLevel.remove();
        removedVoltageLevelReport(reportNode, voltageLevelId);
        LOGGER.info("Voltage level {}, its equipments and the branches it is connected to have been removed", voltageLevelId);
    }

}
