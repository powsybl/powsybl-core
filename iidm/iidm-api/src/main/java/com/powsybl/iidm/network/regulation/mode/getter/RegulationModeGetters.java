/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation.mode.getter;

import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.List;
import java.util.Set;

/**
 * Utility class providing methods to retrieve the allowed regulation modes for voltage regulation holders.
 * This class supports various voltage regulation types and determines supported regulation modes
 * based on the type of voltage regulation holder and whether remote regulation is enabled.
 *
 * The class operates using a list of validators that implement the {@code RegulationModeGetter} interface.
 * Each validator is responsible for supporting a specific type of voltage regulation holder and determining
 * the allowed regulation modes for that type.
 *
 * The class is designed as a non-instantiable utility and thus has a private constructor to prevent instantiation.
 */
public final class RegulationModeGetters {
    private RegulationModeGetters() {
        /* This utility class should not be instantiated */
    }

    private static final List<RegulationModeGetter> VALIDATORS = List.of(
        new GeneratorRegulationModeGetter(),
        new BatteryRegulationModeGetter(),
        new RatioTapChangerRegulationModeGetter(),
        new ShuntCompensatorRegulationModeGetter(),
        new StaticVarCompensatorRegulationModeGetter(),
        new VscConverterStationRegulationModeGetter(),
        new VoltageSourceConverterRegulationModeGetter()
    );

    public static Set<RegulationMode> getAllowedRegulationModes(Class<? extends VoltageRegulationHolder<?>> voltageRegulationHolder, boolean isRemoteRegulating) {
        for (RegulationModeGetter validator : VALIDATORS) {
            if (validator.supports(voltageRegulationHolder)) {
                return validator.getAllowedRegulationModes(isRemoteRegulating);
            }
        }
        throw new IllegalArgumentException("The voltage regulation is not supported for " + voltageRegulationHolder.getSimpleName());
    }
}
