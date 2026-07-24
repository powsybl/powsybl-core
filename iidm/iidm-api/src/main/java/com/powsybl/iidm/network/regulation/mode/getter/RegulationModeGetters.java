/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation.mode.getter;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.Set;

import static com.powsybl.iidm.network.regulation.RegulationMode.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE_PER_REACTIVE_POWER;

public final class RegulationModeGetters {
    private RegulationModeGetters() {
        /* This utility class should not be instantiated */
    }

    public static Set<RegulationMode> getAllowedRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder, boolean isRemoteRegulating) {
        return isRemoteRegulating ? getRemoteAllowedRegulationModes(voltageRegulationHolder) : getLocalAllowedRegulationModes(voltageRegulationHolder);
    }

    private static Set<RegulationMode> getRemoteAllowedRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder) {
        return switch (voltageRegulationHolder) {
            case Class<?> c when c == Battery.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == Generator.class -> Set.of(VOLTAGE, REACTIVE_POWER); // REACTIVE_POWER_PER_ACTIVE_POWER not yet supported
            case Class<?> c when c == RatioTapChanger.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == ShuntCompensator.class -> Set.of(VOLTAGE);
            case Class<?> c when c == StaticVarCompensator.class -> Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER);
            case Class<?> c when c == VscConverterStation.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            case Class<?> c when c == VoltageSourceConverter.class -> Set.of(VOLTAGE, REACTIVE_POWER);
            default -> throwError(voltageRegulationHolder);
        };
    }

    private static Set<RegulationMode> getLocalAllowedRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder) {
        return switch (voltageRegulationHolder) {
            case Class<?> c when c == Battery.class -> Set.of(VOLTAGE);
            case Class<?> c when c == Generator.class -> Set.of(VOLTAGE); // REACTIVE_POWER_PER_ACTIVE_POWER not yet supported
            case Class<?> c when c == RatioTapChanger.class -> Set.of();
            case Class<?> c when c == ShuntCompensator.class -> Set.of(VOLTAGE);
            case Class<?> c when c == StaticVarCompensator.class -> Set.of(VOLTAGE, REACTIVE_POWER, VOLTAGE_PER_REACTIVE_POWER);
            case Class<?> c when c == VscConverterStation.class -> Set.of(VOLTAGE);
            case Class<?> c when c == VoltageSourceConverter.class -> Set.of(VOLTAGE);
            default -> throwError(voltageRegulationHolder);
        };
    }

    private static Set<RegulationMode> throwError(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder) {
        throw new IllegalArgumentException(voltageRegulationHolder.getSimpleName() + " class cannot be used with VoltageRegulation");
    }
}
