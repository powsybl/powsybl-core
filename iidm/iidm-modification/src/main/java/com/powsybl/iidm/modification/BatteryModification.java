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

import static com.powsybl.iidm.modification.util.ModificationReports.notFoundBatteryReport;

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
    public void doApply(Network network, NamingStrategy namingStrategy, boolean throwException,
                        ComputationManager computationManager, ReportNode reportNode, boolean dryRun) {
        Battery battery = network.getBattery(batteryId);
        if (battery == null) {
            notFoundBatteryReport(reportNode, batteryId);
            logOrThrow(throwException, "Battery '" + batteryId + "' not found");
            return;
        }
        if (!dryRun) {
            if (targetP != null) {
                battery.setTargetP(targetP);
            }
            if (targetQ != null) {
                battery.setTargetQ(targetQ);
            }
        }
    }

    @Override
    public String getName() {
        return "BatteryModification";
    }

    @Override
    public boolean hasImpactOnNetwork() {
        return false;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return true;
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
