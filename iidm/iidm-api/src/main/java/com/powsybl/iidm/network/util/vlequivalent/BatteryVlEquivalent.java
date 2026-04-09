/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.vlequivalent;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class BatteryVlEquivalent extends AbstractInjectionVlEquivalent {

    private final double minP;
    private final double maxP;
    private final ReactiveLimits reactiveLimits;

    /**
     * Calculate the characteristics of an equivalent battery for a voltage level containing only a battery and linked to another voltage level with a two-winding transformer.
     * Only use this if those conditions are met, otherwise the result might be unpredictable. See {@link Networks#getSingleConnectableReducibleVoltageLevelStream(Network)}
     * @param voltageLevel the voltage level containing a single battery and a side of a two-winding transformer
     */
    public BatteryVlEquivalent(VoltageLevel voltageLevel) {
        this(voltageLevel.getBatteries().iterator().next(), voltageLevel.getTwoWindingsTransformers().iterator().next());
    }

    public BatteryVlEquivalent(Battery battery, TwoWindingsTransformer transformer) {
        super(battery.getId(), battery.getOptionalName().orElse(null), battery.getTargetP(), battery.getTargetQ(), transformer);
        // the min and max do not change, as a transformer will keep the apparent power equal on both sides, meaning the range of active power does not change either
        this.minP = battery.getMinP();
        this.maxP = battery.getMaxP();
        this.reactiveLimits = battery.getReactiveLimits();
    }

    public double getMinP() {
        return minP;
    }

    public double getMaxP() {
        return maxP;
    }

    public double getTargetP() {
        return activePower;
    }

    public double getTargetQ() {
        return reactivePower;
    }

    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits;
    }
}
