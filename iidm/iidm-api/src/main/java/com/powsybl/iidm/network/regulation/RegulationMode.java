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
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public enum RegulationMode {
    VOLTAGE(1),
    REACTIVE_POWER(2),
    VOLTAGE_PER_REACTIVE_POWER(3);
    // REACTIVE_POWER_PER_ACTIVE_POWER not yet supported

    private final int index;

    RegulationMode(int index) {
        this.index = index;
    }

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

    public int getIndex() {
        return index;
    }

    public static Integer getIndexFromMode(RegulationMode mode) {
        return mode == null ? null : mode.index;
    }

}
