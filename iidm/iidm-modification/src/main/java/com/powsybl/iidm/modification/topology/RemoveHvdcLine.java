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
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
 */
public class RemoveHvdcLine extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveHvdcLine.class);

    private final String hvdcLineId;
    private final List<String> shuntCompensatorsIds;

    public RemoveHvdcLine(String hvdcLineId, List<String> shuntCompensatorsIds) {
        this.hvdcLineId = Objects.requireNonNull(hvdcLineId);
        this.shuntCompensatorsIds = Objects.requireNonNull(shuntCompensatorsIds);
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
        } else {
            LOGGER.error("HVDC Line {} not found", hvdcLineId);
            notFoundHvdcLineReport(reporter, hvdcLineId);
            if (throwException) {
                throw new PowsyblException("HVDC Line not found: " + hvdcLineId);
            }
        }
    }

    private void removeShuntCompensators(Network network,
                                         HvdcConverterStation<?> hvdcConverterStation1,
                                         HvdcConverterStation<?> hvdcConverterStation2,
                                         Reporter reporter) {
        if (!shuntCompensatorsIds.isEmpty()
                && (isLccConverterStation(hvdcConverterStation1)
                || isLccConverterStation(hvdcConverterStation2))) {

            // Get Lcc voltage levels
            List<VoltageLevel> lccVoltageLevel = network.getVoltageLevelStream()
                    .filter(voltageLevel -> voltageLevel.getLccConverterStationStream()
                            .anyMatch(lccConverterStation -> lccConverterStation.getId().equals(hvdcConverterStation1.getId())
                                    || lccConverterStation.getId().equals(hvdcConverterStation2.getId())))
                    .collect(Collectors.toList());

            // removing shunt compensators
            shuntCompensatorsIds.forEach(shuntCompensatorId -> {
                // check whether the shunt compensator is connected to the same voltage level as the LCC
                boolean isSameVoltageLevel = network.getVoltageLevelStream()
                        .filter(voltageLevel -> lccVoltageLevel.contains(voltageLevel))
                        .flatMap(voltageLevel -> voltageLevel.getConnectableStream(ShuntCompensator.class))
                        .anyMatch(shuntCompensator -> shuntCompensator.getId().equals(shuntCompensatorId));

                if (isSameVoltageLevel) {
                    ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);
                    shuntCompensator.remove();
                    removedShuntCompensatorReport(reporter, shuntCompensator.getId());
                }
            });
        }
    }

    private void removeConverterStations(HvdcConverterStation<?> hvdcConverterStation1, HvdcConverterStation<?> hvdcConverterStation2, Reporter reporter) {
        hvdcConverterStation1.remove();
        hvdcConverterStation2.remove();
        reportConverterStationRemoved(reporter, hvdcConverterStation1);
        reportConverterStationRemoved(reporter, hvdcConverterStation2);
    }

    private boolean isLccConverterStation(HvdcConverterStation<?> hvdcConverterStation) {
        Objects.requireNonNull(hvdcConverterStation);
        return hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.LCC;
    }

    private boolean isVscConverterStation(HvdcConverterStation<?> hvdcConverterStation) {
        Objects.requireNonNull(hvdcConverterStation);
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
