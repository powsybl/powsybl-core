/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * Simple {@link NetworkModification} for a battery.
 *
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
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
    public void apply(Network network, boolean throwException, ComputationManager computationManager,
                      Reporter reporter) {
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
