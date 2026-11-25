/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Convert the values of a CGMES record to the text that the CGMES file contains.
 * The values are the same with or without an XML writer.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesNormalizer {

    private static final double SCIENTIFIC_FORMAT_MIN_THRESHOLD = 1E-10;
    private static final DecimalFormatSymbols DOUBLE_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##############", DOUBLE_FORMAT_SYMBOLS);
    private static final DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat("0.######E0", DOUBLE_FORMAT_SYMBOLS);

    private CgmesNormalizer() {
    }

    /**
     * Format the given boolean to a String value.
     */
    public static String format(boolean value) {
        return String.valueOf(value);
    }

    /**
     * Format the given int to a String value.
     */
    public static String format(int value) {
        return CgmesExportUtil.format(value);
    }

    /**
     * Format the given double to a String value, with the same rules as {@link AbstractCgmesWriter#format(double)}.
     */
    public static String format(double value) {
        // CGMES expects xsd:float values
        double floatValue;
        if (value > Float.MAX_VALUE) {
            floatValue = Float.MAX_VALUE;
        } else if (value < -Float.MAX_VALUE) {
            floatValue = -Float.MAX_VALUE;
        } else {
            floatValue = Double.isNaN(value) ? 0.0 : value;
        }

        // Always use scientific format for extreme values
        if (0.0 < Math.abs(floatValue) && Math.abs(floatValue) < SCIENTIFIC_FORMAT_MIN_THRESHOLD
            || Math.abs(floatValue) >= Float.MAX_VALUE) {
            return SCIENTIFIC_FORMAT.format(floatValue);
        } else {
            return DOUBLE_FORMAT.format(floatValue);
        }
    }
}
