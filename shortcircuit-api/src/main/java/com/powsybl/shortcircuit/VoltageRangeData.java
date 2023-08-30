/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.PowsyblException;
import org.apache.commons.lang3.Range;

import java.util.List;

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
     * The voltage range to which the coefficient should be applied. Voltages are given in kV.
     */
    public Range<Double> getVoltageRange() {
        return voltageRange;
    }

    /**
     * The coefficient by which each voltage in the range will be multiplied by (e.g. 1.1). Should be between 0.8 and 1.2.
     */
    public double getRangeCoefficient() {
        return rangeCoefficient;
    }

    /**
     * The minimum nominal voltage of the range (in kV)
     */
    public double getMinimumNominalVoltage() {
        return voltageRange.getMinimum();
    }

    /**
     * The maximum nominal voltage of the range (in kV)
     */
    public double getMaximumNominalVoltage() {
        return voltageRange.getMaximum();
    }

    static void checkVoltageRangeData(List<VoltageRangeData> voltageRangeData) {
        if (voltageRangeData == null || voltageRangeData.isEmpty()) {
            return;
        }
        for (int i = 0; i < voltageRangeData.size() - 1; i++) {
            for (int j = i + 1; j < voltageRangeData.size(); j++) {
                // Check if the range overlaps with any other range
                if (voltageRangeData.get(i).getVoltageRange().isOverlappedBy(voltageRangeData.get(j).getVoltageRange())) {
                    throw new PowsyblException("Voltage ranges for configured initial voltage profile are overlapping");
                }
            }
            // Check if coefficient is correct
            checkCoefficient(voltageRangeData.get(i).getRangeCoefficient());
        }
        // Check last coefficient
        checkCoefficient(voltageRangeData.get(voltageRangeData.size() - 1).getRangeCoefficient());

    }

    private static void checkCoefficient(double rangeCoefficient) {
        if (rangeCoefficient < 0.8 || rangeCoefficient > 1.2) {
            throw new PowsyblException("rangeCoefficient " + rangeCoefficient + " is out of bounds, should be between 0.8 and 1.2.");
        }
    }

}
