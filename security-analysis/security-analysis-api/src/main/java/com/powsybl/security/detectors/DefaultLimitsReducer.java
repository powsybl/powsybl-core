/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DefaultLimitsReducer extends AbstractLimitsReducer<LoadingLimits> {

    private final VoltageLevel voltageLevel;
    private DanglingLine danglingLine;
    private OperationalLimitsGroup operationalLimitsGroup;

    public DefaultLimitsReducer() {
        //TODO Find a better way to create LoadingLimits not linked to an element of the real network with the API.
        Network n = Network.create("Tmp", "Manual");
        voltageLevel = n.newVoltageLevel().setId("vl").setNominalV(225.).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevel.getBusBreakerView().newBus().setId("bus").add();
    }

    @Override
    LoadingLimits generateReducedLimits() {
        LoadingLimits originalLimits = getOriginalLimits();
        LoadingLimitsAdder<?, ?> adder = getLoadingLimitsAdder(originalLimits);
        adder.setPermanentLimit(originalLimits.getPermanentLimit() * getPermanentLimitReduction());

        // Compute the temporary limits:
        // A temporary limit L1 should be ignored (not created) if there exists another temporary limit L2
        // such as: acceptableDuration(L2) < acceptableDuration(L1) AND reducedValue(L2) <= reducedValue(L1)
        List<LoadingLimits.TemporaryLimit> temporaryLimits = originalLimits.getTemporaryLimits().stream()
                .sorted(Comparator.comparing(LoadingLimits.TemporaryLimit::getAcceptableDuration)).toList();
        double previousRetainedReducedValue = Double.NaN;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order of the durations
            double tlReducedValue = tl.getValue() * getTemporaryLimitReduction(tl.getAcceptableDuration());
            if (Double.isNaN(previousRetainedReducedValue) || tlReducedValue < previousRetainedReducedValue) {
                previousRetainedReducedValue = tlReducedValue;
                adder.beginTemporaryLimit()
                        .setName(tl.getName())
                        .setAcceptableDuration(tl.getAcceptableDuration())
                        .setValue(tlReducedValue)
                        .setFictitious(tl.isFictitious())
                        .endTemporaryLimit();
            }
        }
        return adder.add();
    }

    @Override
    IntStream getTemporaryLimitsAcceptableDurationStream() {
        return getOriginalLimits().getTemporaryLimits().stream().mapToInt(LoadingLimits.TemporaryLimit::getAcceptableDuration);
    }

    @Override
    void initialize(String networkElementId, LoadingLimits originalLimits) {
        super.initialize(networkElementId, originalLimits);
        operationalLimitsGroup = createOperationalLimitsGroup(networkElementId);
    }

    private LoadingLimitsAdder<?, ?> getLoadingLimitsAdder(LoadingLimits originalLimits) {
        return switch (originalLimits.getLimitType()) {
            case ACTIVE_POWER -> newActivePowerLimitsAdder();
            case APPARENT_POWER -> newApparentPowerLimitsAdder();
            case CURRENT -> newCurrentLimitsAdder();
            default -> throw new IllegalArgumentException(
                    String.format("Unsupported limits type for reductions (%s)", originalLimits.getLimitType()));
        };
    }

    private OperationalLimitsGroup createOperationalLimitsGroup(String networkElementId) {
        //TODO Find a better way to create LoadingLimits not linked to an element of the real network with the API.
        if (danglingLine != null) {
            danglingLine.remove();
        }
        danglingLine = voltageLevel.newDanglingLine().setId(networkElementId).setConnectableBus("bus")
                .setR(0.).setX(0).setG(0.).setB(0.).setP0(0.).setQ0(0.).add();
        return danglingLine.newOperationalLimitsGroup("Reduced limits");
    }

    private CurrentLimitsAdder newCurrentLimitsAdder() {
        return operationalLimitsGroup.newCurrentLimits();
    }

    private ApparentPowerLimitsAdder newApparentPowerLimitsAdder() {
        return operationalLimitsGroup.newApparentPowerLimits();
    }

    private ActivePowerLimitsAdder newActivePowerLimitsAdder() {
        return operationalLimitsGroup.newActivePowerLimits();
    }
}
