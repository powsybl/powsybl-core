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

import java.text.DecimalFormat;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public final class PrintConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintConfig.class);
    private static final String MATH_MATRIX_MODULE = "math-matrix";
    private static final String PRINT_DECIMAL_PLACES_PROPERTY = "print-decimal-places";
    private final Integer printDecimalPlaces;

    private PrintConfig(Integer decimalDigits) {
        this.printDecimalPlaces = decimalDigits;
    }

    public static PrintConfig load(PlatformConfig platformConfig) {
        Integer printDecimalPlaces = platformConfig.getOptionalModuleConfig(MATH_MATRIX_MODULE)
                .flatMap(m -> m.getOptionalIntProperty(PRINT_DECIMAL_PLACES_PROPERTY).stream().boxed().findFirst())
                .orElse(null);
        if (printDecimalPlaces != null && printDecimalPlaces < 0) {
            LOGGER.warn("Invalid {}.{}={} (must be >= 0). Falling back to default formatting.", MATH_MATRIX_MODULE, PRINT_DECIMAL_PLACES_PROPERTY, printDecimalPlaces);
            return new PrintConfig(null);
        }
        return new PrintConfig(printDecimalPlaces);
    }

    public static PrintConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public DecimalFormat createFormatter() {
        if (printDecimalPlaces != null) {
            return createFormatter(printDecimalPlaces);
        }
        return null;
    }

    private DecimalFormat createFormatter(int maxDecimals) {
        String pattern = "0.0" + "#".repeat(Math.max(0, maxDecimals - 1));
        return new DecimalFormat(pattern);
    }
}
