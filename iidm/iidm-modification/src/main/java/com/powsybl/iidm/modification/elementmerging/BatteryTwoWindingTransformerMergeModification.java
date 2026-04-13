/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.elementmerging;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.vlequivalent.BatteryVlEquivalent;

import java.util.function.Function;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class BatteryTwoWindingTransformerMergeModification extends AbstractInjectionTwoWindingTransformerMergeModification<Battery, BatteryAdder, BatteryVlEquivalent> {

    public BatteryTwoWindingTransformerMergeModification(String voltageLevelId) {
        super(voltageLevelId);
    }

    @Override
    public String getName() {
        return "BatteryTwoWindingTransformerMergeModification";
    }

    @Override
    int getInjectionCount(VoltageLevel voltageLevel) {
        return voltageLevel.getBatteryCount();
    }

    @Override
    String getInjectionName() {
        return "battery";
    }

    @Override
    Function<Double, BatteryAdder> getActivePowerSetter(BatteryAdder adder) {
        return adder::setTargetP;
    }

    @Override
    Function<Double, BatteryAdder> getReactivePowerSetter(BatteryAdder adder) {
        return adder::setTargetQ;
    }

    @Override
    BatteryAdder setSpecificInjectionParameters(BatteryAdder adder, BatteryVlEquivalent equivalent) {
        return adder
            .setMinP(equivalent.getMinP())
            .setMaxP(equivalent.getMaxP());
    }

    @Override
    BatteryVlEquivalent createEquivalent(Battery injection, TwoWindingsTransformer transformer) {
        return new BatteryVlEquivalent(injection, transformer);
    }

    @Override
    BatteryAdder createAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newBattery();
    }

    @Override
    Battery getInjection(VoltageLevel voltageLevel) {
        return voltageLevel.getBatteries().iterator().next();
    }
}
