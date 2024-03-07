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
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationReports.notFoundSubstationReport;
import static com.powsybl.iidm.modification.util.ModificationReports.removedSubstationReport;

/**
 * @author Maissa Souissi {@literal <maissa.souissi at rte-france.com>}
 */
public class RemoveSubstation extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveSubstation.class);

    private final String substationId;

    RemoveSubstation(String substationId) {
        this.substationId = Objects.requireNonNull(substationId);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Substation substation = network.getSubstation(substationId);
        if (substation == null) {
            LOGGER.error("Substation {} not found", substationId);
            notFoundSubstationReport(reportNode, substationId);
            if (throwException) {
                throw new PowsyblException("Substation not found: " + substationId);
            }
            return;
        }
        List<String> vlIds = substation.getVoltageLevelStream().map(VoltageLevel::getId).toList();
        vlIds.forEach(id -> new RemoveVoltageLevel(id).apply(network, true, reportNode));
        substation.remove();
        removedSubstationReport(reportNode, substationId);
        LOGGER.info("Substation {} and its voltage levels have been removed", substationId);
    }
}

