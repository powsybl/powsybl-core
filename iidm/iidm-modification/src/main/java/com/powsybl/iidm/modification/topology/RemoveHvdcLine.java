/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
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
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        HvdcLine hvdcLine = network.getHvdcLine(hvdcLineId);
        if (hvdcLine != null) {
            HvdcConverterStation<?> hvdcConverterStation1 = hvdcLine.getConverterStation1();
            HvdcConverterStation<?> hvdcConverterStation2 = hvdcLine.getConverterStation2();
            Set<ShuntCompensator> shunts = null;
            if (isLccConverterStation(hvdcConverterStation1)) { // in real-life cases, both converter stations are of the same type
                shunts = shuntCompensatorIds.stream()
                        .map(id -> {
                            ShuntCompensator sc = network.getShuntCompensator(id);
                            if (sc == null) {
                                notFoundShuntReport(reporter, id);
                                LOGGER.error("Shunt {} not found", id);
                                if (throwException) {
                                    throw new PowsyblException("Shunt " + id + " not found");
                                }
                            }
                            return sc;
                        }).filter(Objects::nonNull).collect(Collectors.toSet());
            } else if (!shuntCompensatorIds.isEmpty()) { // VSC converter stations and defined shunts
                String shuntIds = String.join(",", shuntCompensatorIds);
                LOGGER.warn("Shunts {} are ignored since converter stations {} and {} are VSC", shuntIds, hvdcConverterStation1.getId(), hvdcConverterStation2.getId());
                ignoredVscShunts(reporter, shuntIds, hvdcConverterStation1.getId(), hvdcConverterStation2.getId());
            }
            hvdcLine.remove();
            removedHvdcLineReport(reporter, hvdcLineId);
            // Remove the Shunt compensators that represent the filters of the LCC
            removeShuntCompensators(hvdcConverterStation1, hvdcConverterStation2, shunts, reporter);
            removeConverterStations(hvdcConverterStation1, hvdcConverterStation2, reporter);
        } else {
            LOGGER.error("Hvdc Line {} not found", hvdcLineId);
            notFoundHvdcLineReport(reporter, hvdcLineId);
            if (throwException) {
                throw new PowsyblException("Hvdc Line " + hvdcLineId + " not found");
            }
        }
    }

    private static void removeShuntCompensators(HvdcConverterStation<?> hvdcConverterStation1, HvdcConverterStation<?> hvdcConverterStation2, Set<ShuntCompensator> shunts, Reporter reporter) {
        if (shunts != null) {
            // Get the voltage levels of both lcc converter stations
            VoltageLevel vl1 = hvdcConverterStation1.getTerminal().getVoltageLevel();
            VoltageLevel vl2 = hvdcConverterStation2.getTerminal().getVoltageLevel();

            // removing shunt compensators
            shunts.forEach(shuntCompensator -> {
                VoltageLevel shuntVl = shuntCompensator.getTerminal().getVoltageLevel();
                // check whether the shunt compensator is connected to the same voltage level as the lcc
                if (vl1 == shuntVl || vl2 == shuntVl) {
                    shuntCompensator.remove();
                    removedShuntCompensatorReport(reporter, shuntCompensator.getId());
                } else {
                    LOGGER.warn("Shunt compensator {} has been ignored because it is not in the same voltage levels as the Lcc ({} or {})", shuntCompensator.getId(), vl1.getId(), vl2.getId());
                    ignoredShuntInAnotherVoltageLevel(reporter, shuntCompensator.getId(), vl1.getId(), vl2.getId());
                }
            });
        }
    }

    private static void removeConverterStations(HvdcConverterStation<?> hvdcConverterStation1, HvdcConverterStation<?> hvdcConverterStation2, Reporter reporter) {
        hvdcConverterStation1.remove();
        hvdcConverterStation2.remove();
        reportConverterStationRemoved(reporter, hvdcConverterStation1);
        reportConverterStationRemoved(reporter, hvdcConverterStation2);
    }

    private static boolean isLccConverterStation(HvdcConverterStation<?> hvdcConverterStation) {
        return hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.LCC;
    }

    private static boolean isVscConverterStation(HvdcConverterStation<?> hvdcConverterStation) {
        return hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC;
    }

    private static void reportConverterStationRemoved(Reporter reporter, HvdcConverterStation<?> hvdcConverterStation) {
        if (isLccConverterStation(hvdcConverterStation)) {
            removedLccConverterStationReport(reporter, hvdcConverterStation.getId());
        }
        if (isVscConverterStation(hvdcConverterStation)) {
            removedVscConverterStationReport(reporter, hvdcConverterStation.getId());
        }
    }

}
