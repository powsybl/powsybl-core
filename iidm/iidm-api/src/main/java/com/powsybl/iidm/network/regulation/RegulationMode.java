/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import java.util.Arrays;

/**
 * Mode of regulation for equipment with voltage regulation capabilities.
 *
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public enum RegulationMode {

    /**
     * Direct voltage regulation to a target voltage value.
     */
    VOLTAGE(1),

    /**
     * Reactive power regulation to a target reactive power value.
     */
    REACTIVE_POWER(2),

    /**
     * Voltage regulation with reactive power droop slope ({@code U0 = U + lambda * Q}).
     */
    VOLTAGE_PER_REACTIVE_POWER(3),

    /**
     * Reactive power regulation based on active power with a power factor slope ({@code Q = tan(phi) * P}).
     */
    REACTIVE_POWER_PER_ACTIVE_POWER(4);

    private final int index;

    RegulationMode(int index) {
        this.index = index;
    }

    /**
     * Returns the {@link RegulationMode} corresponding to the specified index.
     *
     * @param index the index of the regulation mode
     * @return the corresponding {@code RegulationMode}
     * @throws IllegalArgumentException if the index does not match any regulation mode
     */
    public static RegulationMode fromIndex(int index) {
        for (RegulationMode mode : RegulationMode.values()) {
            if (mode.index == index) {
                return mode;
            }
        }
        String allowedRegulationModeIndices = Arrays.toString(Arrays.stream(values())
            .mapToInt(RegulationMode::getIndex)
            .toArray());
        throw new IllegalArgumentException("Unknown or unsupported regulation mode index: " + index
            + ". Allowed values are: " + allowedRegulationModeIndices);
    }

    /**
     * Returns the numeric index of this regulation mode.
     *
     * @return the integer index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the numeric index of the given regulation mode, or {@code null} if {@code mode} is {@code null}.
     *
     * @param mode the regulation mode (may be {@code null})
     * @return the integer index, or {@code null} if {@code mode} is {@code null}
     */
    public static Integer getIndexFromMode(RegulationMode mode) {
        return mode == null ? null : mode.index;
    }

}
