/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.math.matrix;

import com.powsybl.commons.config.PlatformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public final class MatrixConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixConfig.class);
    private final Integer printDecimalPlaces;

    private MatrixConfig(Integer decimalDigits) {
        this.printDecimalPlaces = decimalDigits;
    }

    public static MatrixConfig load(PlatformConfig platformConfig) {
        Integer printDecimalPlaces = platformConfig.getOptionalModuleConfig("matrix")
                .flatMap(m -> m.getOptionalIntProperty("print-decimal-places").stream().boxed().findFirst())
                .orElse(null);
        if (printDecimalPlaces != null && printDecimalPlaces < 0) {
            LOGGER.warn("Invalid matrix.print-decimal-places={} (must be >= 0). Falling back to default formatting.", printDecimalPlaces);
            printDecimalPlaces = null;
        }
        return new MatrixConfig(printDecimalPlaces);
    }

    public static MatrixConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public Integer getPrintDecimalPlaces() {
        return printDecimalPlaces;
    }
}
