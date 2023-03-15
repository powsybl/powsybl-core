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
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

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
            hvdcLine.remove();
            removedHvdcLineReport(reporter, hvdcLineId);
            // Remove the Shunt compensators that represent the filters of the LCC
            removeShuntCompensators(network, hvdcConverterStation1, hvdcConverterStation2, reporter);
            removeConverterStations(hvdcConverterStation1, hvdcConverterStation2, reporter);
            LOGGER.info("Hvdc Line {} and Hvdc converter stations {}, {} have been removed", hvdcLineId, hvdcConverterStation1.getId(), hvdcConverterStation2.getId());
        } else {
            LOGGER.error("Hvdc Line {} not found", hvdcLineId);
            notFoundHvdcLineReport(reporter, hvdcLineId);
            if (throwException) {
                throw new PowsyblException("Hvdc Line {} not found: " + hvdcLineId);
            }
        }
    }

    private void removeShuntCompensators(Network network, HvdcConverterStation<?> hvdcConverterStation1, HvdcConverterStation<?> hvdcConverterStation2, Reporter reporter) {
        if (!shuntCompensatorIds.isEmpty()) {
            if (isLccConverterStation(hvdcConverterStation1)) {
                // Get the voltage levels of both lcc converter stations
                String vl1Id = hvdcConverterStation1.getTerminal().getVoltageLevel().getId();
                String vl2Id = hvdcConverterStation2.getTerminal().getVoltageLevel().getId();

                // removing shunt compensators
                shuntCompensatorIds.forEach(shuntCompensatorId -> {
                    ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);
                    if (shuntCompensator != null) {
                        String shuntCompensatorVoltageLevelId = shuntCompensator.getTerminal().getVoltageLevel().getId();
                        // check whether the shunt compensator is connected to the same voltage level as the lcc
                        if (shuntCompensatorVoltageLevelId.equals(vl1Id) || shuntCompensatorVoltageLevelId.equals(vl2Id)) {
                            shuntCompensator.remove();
                            removedShuntCompensatorReport(reporter, shuntCompensator.getId());
                        } else {
                            LOGGER.info("The shunt compensators has not been removed because it is not at the same voltage level as the Lcc");
                        }
                    }
                });
            } else {
                LOGGER.info("The shunt compensators are not removed since we are in VSC");
            }

        }

    }

    private void removeConverterStations(HvdcConverterStation<?> hvdcConverterStation1, HvdcConverterStation<?> hvdcConverterStation2, Reporter reporter) {
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

    private void reportConverterStationRemoved(Reporter reporter, HvdcConverterStation<?> hvdcConverterStation) {
        if (isLccConverterStation(hvdcConverterStation)) {
            removedLccConverterStationReport(reporter, hvdcConverterStation.getId());
        }
        if (isVscConverterStation(hvdcConverterStation)) {
            removedVscConverterStationReport(reporter, hvdcConverterStation.getId());
        }
    }

}
