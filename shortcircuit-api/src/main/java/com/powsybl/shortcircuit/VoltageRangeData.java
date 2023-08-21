/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import org.apache.commons.lang3.Range;

/**
 * A class that stores coefficients to be applied to every nominal voltage in a range. This is used to define the configured initial voltage profile
 * for short circuit calculation.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class VoltageRangeData {
    final Range<Double> voltageRange;
    final double rangeCoefficient;

    public VoltageRangeData(double lowVoltage, double highVoltage, double rangeCoefficient) {
        this.voltageRange = Range.between(lowVoltage, highVoltage);
        this.rangeCoefficient = rangeCoefficient;
    }

    /**
     * The voltage range to which the coefficient should be applied
     */
    public Range<Double> getVoltageRange() {
        return voltageRange;
    }

    /**
     * The coefficient by which each voltage in the range will be multiplied by (e.g. 1.1)
     */
    public double getRangeCoefficient() {
        return rangeCoefficient;
    }

    /**
     * The minimum nominal voltage of the range
     */
    public double getMinimumNominalVoltage() {
        return voltageRange.getMinimum();
    }

    /**
     * The maximum nominal voltage of the range
     */
    public double getMaximumNominalVoltage() {
        return voltageRange.getMaximum();
    }

}
