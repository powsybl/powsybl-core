/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.*;

import java.util.Set;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public enum RegulationMode {
    VOLTAGE,
    REACTIVE_POWER,
    VOLTAGE_PER_REACTIVE_POWER,
    REACTIVE_POWER_PER_ACTIVE_POWER;

    public static Set<RegulationMode> getAllowedRegulationModes(Class<? extends VoltageRegulationHolderBuilder> voltageRegulationHolder) {
        return switch (voltageRegulationHolder) {
            case Class<?> c when c == Battery.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == Generator.class -> Set.of(VOLTAGE, REACTIVE_POWER, REACTIVE_POWER_PER_ACTIVE_POWER);
            case Class<?> c when c == RatioTapChanger.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == ShuntCompensator.class -> Set.of(VOLTAGE);
            case Class<?> c when c == StaticVarCompensator.class -> Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER);
            case Class<?> c when c == VscConverterStation.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == VoltageSourceConverter.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            default -> throw new IllegalArgumentException(voltageRegulationHolder.getSimpleName() + " class is not valid for use with VoltageRegulation");
        };
    }
}
