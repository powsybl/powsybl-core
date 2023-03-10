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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;

/**
 * @author Anis Touri <anis-1.touri@rte-france.com>
 */
public class RemoveHVDCLine extends AbstractNetworkModification {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveHVDCLine.class);

    private final String hvdcLineId;
    private final List<String> mscIds;

    public RemoveHVDCLine(String hvdcLineId, List<String> mscIds) {
        this.hvdcLineId = Objects.requireNonNull(hvdcLineId);
        this.mscIds = mscIds;
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        AtomicBoolean hvdcLineRemoved = new AtomicBoolean(false);
        AtomicReference<HvdcConverterStation> hvdcConverterStation1Atomic = new AtomicReference<>(null);
        ;
        AtomicReference<HvdcConverterStation> hvdcConverterStation2Atomic = new AtomicReference<>(null);
        ;
        network.getHvdcConverterStationStream()
                .filter(Objects::nonNull)
                .forEach(hvdcConverterStation -> {
                    HvdcLine hvdcLine = hvdcConverterStation.getHvdcLine();
                    if (hvdcLine != null && hvdcLine.getId().equals(hvdcLineId)) {
                        hvdcConverterStation1Atomic.set(hvdcLine.getConverterStation1());
                        hvdcConverterStation2Atomic.set(hvdcLine.getConverterStation2());
                        hvdcLine.remove();
                        hvdcLineRemoved.set(true);
                        removedHvdcLineReport(reporter, hvdcLineId);

                    }
                });

        HvdcConverterStation hvdcConverterStation1 = hvdcConverterStation1Atomic.get();
        HvdcConverterStation hvdcConverterStation2 = hvdcConverterStation2Atomic.get();

        if (!hvdcLineRemoved.get()) {
            LOGGER.error("HVDC Line {} not found", hvdcLineId);
            notFoundHvdcLineReport(reporter, hvdcLineId);
            if (throwException) {
                throw new PowsyblException("HVDC Line not found: " + hvdcLineId);
            }
        } else {
            hvdcConverterStation1.remove();
            hvdcConverterStation2.remove();
            reportConverterStationRemoved(reporter, hvdcConverterStation1);
            reportConverterStationRemoved(reporter, hvdcConverterStation2);

            // Remove the MCSs that represent the filters of the LCC
            if (mscIds != null
                    && (isLccConverterStation(hvdcConverterStation1)
                    || isLccConverterStation(hvdcConverterStation2))) {
                removeMCSs(network, reporter);
            }
        }
    }

    private boolean isLccConverterStation(HvdcConverterStation hvdcConverterStation) {
        Objects.requireNonNull(hvdcConverterStation);
        return hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.LCC;
    }

    private boolean isVscConverterStation(HvdcConverterStation hvdcConverterStation) {
        Objects.requireNonNull(hvdcConverterStation);
        return hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC;
    }

    private void reportConverterStationRemoved(Reporter reporter, HvdcConverterStation hvdcConverterStation) {
        if (isLccConverterStation(hvdcConverterStation)) {
            removedLccConverterStationReport(reporter, hvdcConverterStation.getId());
        }
        if (isVscConverterStation(hvdcConverterStation)) {
            removedVscConverterStationReport(reporter, hvdcConverterStation.getId());
        }
    }

    private void removeMCSs(Network network, Reporter reporter) {
        mscIds.forEach(mscId ->
                network.getShuntCompensatorStream().filter(shuntCompensator -> shuntCompensator.getId().equals(mscId))
                        .findAny().ifPresent(shuntCompensator -> {
                                    shuntCompensator.remove();
                                    removedMcsReport(reporter, shuntCompensator.getId());
                                }
                        )
        );
    }
}
