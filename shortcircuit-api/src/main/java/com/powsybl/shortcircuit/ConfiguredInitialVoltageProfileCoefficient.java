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
 * A class storing coefficient to apply to a certain voltage range to define custom initial voltage profile
 * for short circuit computation.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ConfiguredInitialVoltageProfileCoefficient {
    final Range<Double> voltageRange;
    final double rangeCoefficient;

    public ConfiguredInitialVoltageProfileCoefficient(double lowVoltage, double highVoltage, double rangeCoefficient) {
        this.voltageRange = Range.between(lowVoltage, highVoltage);
        this.rangeCoefficient = rangeCoefficient;
    }

    public Range<Double> getVoltageRange() {
        return voltageRange;
    }

    public double getRangeCoefficient() {
        return rangeCoefficient;
    }

    public double getMinimumVoltage() {
        return voltageRange.getMinimum();
    }

    public double getMaximumVoltage() {
        return voltageRange.getMaximum();
    }

}
