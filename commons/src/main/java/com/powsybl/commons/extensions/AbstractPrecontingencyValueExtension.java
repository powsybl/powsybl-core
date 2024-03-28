/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.commons.extensions;

/**
 * Abstract extension for Voltage and Current Extensions
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */

public abstract class AbstractPrecontingencyValueExtension<T> extends AbstractExtension<T> {

    private final double preContingencyValue;

    public AbstractPrecontingencyValueExtension(double value) {
        this.preContingencyValue = checkValue(value);
    }

    public double getPreContingencyValue() {
        return preContingencyValue;
    }

    private static double checkValue(double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Value is undefined");
        }
        return value;
    }
}
