/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.elementmerging;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.vlequivalent.LoadVlEquivalent;

import java.util.function.Function;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LoadTwoWindingTransformerMergeModification extends AbstractInjectionTwoWindingTransformerMergeModification<Load, LoadAdder, LoadVlEquivalent> {

    public LoadTwoWindingTransformerMergeModification(String voltageLevelId) {
        super(voltageLevelId);
    }

    @Override
    public String getName() {
        return "LoadTwoWindingTransformerMergeModification";
    }

    @Override
    int getInjectionCount(VoltageLevel voltageLevel) {
        return voltageLevel.getLoadCount();
    }

    @Override
    String getInjectionName() {
        return "load";
    }

    @Override
    Function<Double, LoadAdder> getActivePowerSetter(LoadAdder adder) {
        return adder::setP0;
    }

    @Override
    Function<Double, LoadAdder> getReactivePowerSetter(LoadAdder adder) {
        return adder::setQ0;
    }

    @Override
    LoadAdder setSpecificInjectionParameters(LoadAdder adder, LoadVlEquivalent equivalent) {
        return adder.setLoadType(equivalent.getType());
    }

    @Override
    LoadVlEquivalent createEquivalent(Load injection, TwoWindingsTransformer transformer) {
        return new LoadVlEquivalent(injection, transformer);
    }

    @Override
    LoadAdder createAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newLoad();
    }

    @Override
    Load getInjection(VoltageLevel voltageLevel) {
        return voltageLevel.getLoads().iterator().next();
    }
}
