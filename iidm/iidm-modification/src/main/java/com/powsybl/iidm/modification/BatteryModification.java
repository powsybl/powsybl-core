/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} for a battery.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 */
public class BatteryModification extends AbstractNetworkModification {

    private final String batteryId;
    private final Double targetQ;
    private final Double targetP;

    public BatteryModification(String batteryId, Double targetP, Double targetQ) {
        this.batteryId = Objects.requireNonNull(batteryId);
        this.targetP = targetP;
        this.targetQ = targetQ;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                      ReportNode reportNode) {
        Battery battery = network.getBattery(batteryId);
        if (battery == null) {
            logOrThrow(throwException, "Battery '" + batteryId + "' not found");
            return;
        }
        if (targetP != null) {
            battery.setTargetP(targetP);
        }
        if (targetQ != null) {
            battery.setTargetQ(targetQ);
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Battery battery = network.getBattery(batteryId);
        if (battery == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else {
            if ((targetP == null || Math.abs(targetP - battery.getTargetP()) < EPSILON)
                && (targetQ == null || Math.abs(targetQ - battery.getTargetQ()) < EPSILON)) {
                impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
            }
        }
        return impact;
    }

    public String getBatteryId() {
        return batteryId;
    }

    public Double getTargetP() {
        return targetP;
    }

    public Double getTargetQ() {
        return targetQ;
    }
}
