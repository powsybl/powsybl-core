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
import java.util.Objects;

/**
 * A class that stores coefficients to be applied to every nominal voltage in a range. This is used to define the configured initial voltage profile
 * for short circuit calculation.
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class VoltageRange {
    private final Range<Double> range;
    private final double rangeCoefficient;

    public VoltageRange(double lowVoltage, double highVoltage, double rangeCoefficient) {
        this.range = Range.of(lowVoltage, highVoltage);
        this.rangeCoefficient = checkCoefficient(rangeCoefficient);
    }

    /**
     * The voltage range to which the coefficient should be applied. Voltages are given in kV.
     */
    public Range<Double> getRange() {
        return range;
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
        return range.getMinimum();
    }

    /**
     * The maximum nominal voltage of the range (in kV)
     */
    public double getMaximumNominalVoltage() {
        return range.getMaximum();
    }

    static void checkVoltageRange(List<VoltageRange> voltageRange) {
        if (voltageRange == null || voltageRange.isEmpty()) {
            return;
        }
        for (int i = 0; i < voltageRange.size() - 1; i++) {
            for (int j = i + 1; j < voltageRange.size(); j++) {
                // Check if the range overlaps with any other range
                if (voltageRange.get(i).getRange().isOverlappedBy(voltageRange.get(j).getRange())) {
                    throw new PowsyblException("Voltage ranges for configured initial voltage profile are overlapping");
                }
            }
        }
    }

    private static double checkCoefficient(double rangeCoefficient) {
        if (rangeCoefficient < 0.8 || rangeCoefficient > 1.2) {
            throw new PowsyblException("rangeCoefficient " + rangeCoefficient + " is out of bounds, should be between 0.8 and 1.2.");
        }
        return rangeCoefficient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VoltageRange that = (VoltageRange) o;
        return range.equals(that.range) && rangeCoefficient == that.rangeCoefficient;
    }

    @Override
    public int hashCode() {
        return Objects.hash(range, rangeCoefficient);
    }

}
