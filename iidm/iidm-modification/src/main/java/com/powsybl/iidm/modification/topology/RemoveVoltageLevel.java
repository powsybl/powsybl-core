/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.modification.util.ModificationLogs;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * Removes a voltage level and the feeder bays connected to that voltage level. Note that dangling lines connected to
 * this voltage level (hence paired) are not removed but unpaired.
 * @author Etienne Homer {@literal <etienne.homer at rte-france.com>}
 */
public class RemoveVoltageLevel extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveVoltageLevel.class);

    private final String voltageLevelId;

    public RemoveVoltageLevel(String voltageLevelId) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
    }

    @Override
    public String getName() {
        return "RemoveVoltageLevel";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            notFoundVoltageLevelReport(reportNode, voltageLevelId);
            ModificationLogs.logOrThrow(throwException, "Voltage level not found: " + voltageLevelId);
            return;
        }

        voltageLevel.getConnectables(HvdcConverterStation.class).forEach(hcs -> {
            if (hcs.getHvdcLine() != null) {
                new RemoveHvdcLineBuilder().withHvdcLineId(hcs.getHvdcLine().getId()).build().apply(network, throwException, computationManager, reportNode);
            }
        });

        voltageLevel.getDanglingLines().forEach(dl ->
            dl.getTieLine().ifPresent(tieLine -> {
                String tlId = tieLine.getId();
                String pairingKey = tieLine.getPairingKey();
                tieLine.remove();
                removedTieLineReport(reportNode, tlId, pairingKey);
                LOGGER.info("Tie line {} removed", tlId);
            })
        );

        Consumer<String> removeConnectableFeederBay = id -> new RemoveFeederBayBuilder().withConnectableId(id).build()
                .apply(network, throwException, computationManager, reportNode);
        voltageLevel.getLines().forEach(line -> removeConnectableFeederBay.accept(line.getId()));
        voltageLevel.getTwoWindingsTransformers().forEach(transformer -> removeConnectableFeederBay.accept(transformer.getId()));
        voltageLevel.getThreeWindingsTransformers().forEach(transformer -> removeConnectableFeederBay.accept(transformer.getId()));

        voltageLevel.getConnectables().forEach(connectable -> {
            String connectableId = connectable.getId();
            connectable.remove();
            removedConnectableReport(reportNode, connectableId);
            LOGGER.info("Connectable {} removed", connectableId);
        });

        voltageLevel.remove();
        removedVoltageLevelReport(reportNode, voltageLevelId);
        LOGGER.info("Voltage level {}, its equipments and the branches it is connected to have been removed", voltageLevelId);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        if (network.getVoltageLevel(voltageLevelId) == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }
}
