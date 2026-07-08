/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.math.matrix;

import com.powsybl.commons.config.PlatformConfig;

import java.util.OptionalInt;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public final class MatrixConfig {

    private final OptionalInt printDecimalDigits;

    private MatrixConfig(OptionalInt decimalDigits) {
        this.printDecimalDigits = decimalDigits;
    }

    public static MatrixConfig load(PlatformConfig platformConfig) {
        OptionalInt printDecimalDigits = platformConfig.getOptionalModuleConfig("matrix")
                .map(m -> m.getOptionalIntProperty("print-decimal-digits"))
                .orElse(OptionalInt.empty());
        return new MatrixConfig(printDecimalDigits);
    }

    public static MatrixConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public OptionalInt getDecimalDigits() {
        return printDecimalDigits;
    }
}
