/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class NominalVoltageNetworkPredicate implements NetworkPredicate {

    private final double minNominalVoltage;

    private final double maxNominalVoltage;

    public NominalVoltageNetworkPredicate(double minNominalVoltage, double maxNominalVoltage) {
        this.minNominalVoltage = checkMinNominalVoltage(minNominalVoltage);
        this.maxNominalVoltage = checkMaxNominalVoltage(maxNominalVoltage);

        if (minNominalVoltage >= maxNominalVoltage) {
            throw new IllegalArgumentException("Nominal voltage range is empty");
        }
    }

    @Override
    public boolean test(Substation substation) {
        Objects.requireNonNull(substation);
        return substation.getVoltageLevelStream()
                .anyMatch(this::test);
    }

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        return voltageLevel.getNominalV() >= minNominalVoltage && voltageLevel.getNominalV() <= maxNominalVoltage;
    }

    private static double checkMinNominalVoltage(double minNominalVoltage) {
        if (Double.isNaN(minNominalVoltage)) {
            throw new IllegalArgumentException("Minimal nominal voltage is undefined");
        }
        if (minNominalVoltage < 0.0) {
            throw new IllegalArgumentException("Minimal nominal voltage must be greater or equal to zero");
        }

        return minNominalVoltage;
    }

    private static double checkMaxNominalVoltage(double maxNominalVoltage) {
        if (Double.isNaN(maxNominalVoltage)) {
            throw new IllegalArgumentException("Maximal nominal voltage is undefined");
        }
        if (maxNominalVoltage < 0.0) {
            throw new IllegalArgumentException("Maximal nominal voltage must be greater or equal to zero");
        }

        return maxNominalVoltage;
    }
}
