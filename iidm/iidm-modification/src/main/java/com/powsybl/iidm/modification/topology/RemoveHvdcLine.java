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
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * @author Anis Touri {@literal <anis-1.touri@rte-france.com>}
 */
public class RemoveHvdcLine extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveHvdcLine.class);

    private final String hvdcLineId;
    private final List<String> shuntCompensatorIds;

    RemoveHvdcLine(String hvdcLineId, List<String> shuntCompensatorIds) {
        this.hvdcLineId = Objects.requireNonNull(hvdcLineId);
        this.shuntCompensatorIds = Objects.requireNonNull(shuntCompensatorIds);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        HvdcLine hvdcLine = network.getHvdcLine(hvdcLineId);
        if (hvdcLine != null) {
            HvdcConverterStation<?> hvdcConverterStation1 = hvdcLine.getConverterStation1();
            HvdcConverterStation<?> hvdcConverterStation2 = hvdcLine.getConverterStation2();
            Set<ShuntCompensator> shunts = null;
            if (hvdcConverterStation1.getHvdcType() == HvdcConverterStation.HvdcType.LCC) { // in real-life cases, both converter stations are of the same type
                shunts = shuntCompensatorIds.stream()
                        .map(id -> getShuntCompensator(id, network, throwException, reportNode))
                        .filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
            } else if (!shuntCompensatorIds.isEmpty()) { // VSC converter stations and defined shunts
                String shuntIds = String.join(",", shuntCompensatorIds);
                LOGGER.warn("Shunts {} are ignored since converter stations {} and {} are VSC", shuntIds, hvdcConverterStation1.getId(), hvdcConverterStation2.getId());
                ignoredVscShunts(reportNode, shuntIds, hvdcConverterStation1.getId(), hvdcConverterStation2.getId());
            }
            hvdcLine.remove();
            removedHvdcLineReport(reportNode, hvdcLineId);
            LOGGER.info("Hvdc line {} has been removed", hvdcLineId);
            // Remove the Shunt compensators that represent the filters of the LCC
            removeShuntCompensators(network, hvdcConverterStation1, hvdcConverterStation2, shunts, throwException, computationManager, reportNode);
            removeConverterStations(network, hvdcConverterStation1, hvdcConverterStation2, throwException, computationManager, reportNode);
        } else {
            LOGGER.error("Hvdc Line {} not found", hvdcLineId);
            notFoundHvdcLineReport(reportNode, hvdcLineId);
            if (throwException) {
                throw new PowsyblException("Hvdc Line " + hvdcLineId + " not found");
            }
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        if (network.getHvdcLine(hvdcLineId) == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            return impact;
        }
        impact = NetworkModificationImpact.HAS_IMPACT_ON_NETWORK;
        return impact;
    }

    private static ShuntCompensator getShuntCompensator(String id, Network network, boolean throwException, ReportNode reportNode) {
        ShuntCompensator sc = network.getShuntCompensator(id);
        if (sc == null) {
            notFoundShuntReport(reportNode, id);
            LOGGER.error("Shunt {} not found", id);
            if (throwException) {
                throw new PowsyblException("Shunt " + id + " not found");
            }
        }
        return sc;
    }

    private static void removeShuntCompensators(Network network, HvdcConverterStation<?> hvdcConverterStation1, HvdcConverterStation<?> hvdcConverterStation2, Set<ShuntCompensator> shunts, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        if (shunts == null) {
            return;
        }

        // Get the voltage levels of both lcc converter stations
        VoltageLevel vl1 = hvdcConverterStation1.getTerminal().getVoltageLevel();
        VoltageLevel vl2 = hvdcConverterStation2.getTerminal().getVoltageLevel();

        // removing shunt compensators
        for (ShuntCompensator shuntCompensator : shunts) {
            VoltageLevel shuntVl = shuntCompensator.getTerminal().getVoltageLevel();
            // check whether the shunt compensator is connected to the same voltage level as the lcc
            String shuntId = shuntCompensator.getId();
            if (vl1 == shuntVl || vl2 == shuntVl) {
                new RemoveFeederBay(shuntId).apply(network, throwException, computationManager, reportNode);
                removedShuntCompensatorReport(reportNode, shuntId);
                LOGGER.info("Shunt compensator {} has been removed", shuntId);
            } else {
                LOGGER.warn("Shunt compensator {} has been ignored because it is not in the same voltage levels as the Lcc ({} or {})", shuntId, vl1.getId(), vl2.getId());
                ignoredShuntInAnotherVoltageLevel(reportNode, shuntId, vl1.getId(), vl2.getId());
            }
        }
    }

    private static void removeConverterStations(Network network, HvdcConverterStation<?> hvdcConverterStation1, HvdcConverterStation<?> hvdcConverterStation2, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        String station1Id = hvdcConverterStation1.getId();
        String station2Id = hvdcConverterStation2.getId();
        HvdcConverterStation.HvdcType station1Type = hvdcConverterStation1.getHvdcType();
        HvdcConverterStation.HvdcType station2Type = hvdcConverterStation2.getHvdcType();
        new RemoveFeederBay(station1Id).apply(network, throwException, computationManager, reportNode);
        new RemoveFeederBay(station2Id).apply(network, throwException, computationManager, reportNode);
        reportConverterStationRemoved(reportNode, station1Id, station1Type);
        reportConverterStationRemoved(reportNode, station2Id, station2Type);
    }

    private static void reportConverterStationRemoved(ReportNode reportNode, String stationId, HvdcConverterStation.HvdcType converterStationType) {
        if (converterStationType == HvdcConverterStation.HvdcType.LCC) {
            removedLccConverterStationReport(reportNode, stationId);
            LOGGER.info("Lcc converter station {} has been removed", stationId);
        } else if (converterStationType == HvdcConverterStation.HvdcType.VSC) {
            removedVscConverterStationReport(reportNode, stationId);
            LOGGER.info("Vsc converter station {} has been removed", stationId);
        }
    }

}
