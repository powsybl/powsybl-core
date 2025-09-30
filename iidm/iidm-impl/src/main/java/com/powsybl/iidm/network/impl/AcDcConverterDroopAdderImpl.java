/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class AcDcConverterDroopAdderImpl implements AcDcConverterDoopAdder {

    double uMin;

    double uMax;

    double k;

    AcDcConverter<?> converter;

    public AcDcConverterDroopAdderImpl(AcDcConverter<?> converter) {
        this.converter = converter;
    }

    @Override
    public AcDcConverterDoopAdder setDroopCoefficient(double k) {
        this.k = k;
        return this;
    }

    @Override
    public AcDcConverterDoopAdder setUMax(double uMax) {
        this.uMax = uMax;
        return this;
    }

    @Override
    public AcDcConverterDoopAdder setUMin(double uMin) {
        this.uMin = uMin;
        return this;
    }

    @Override
    public AcDcConverterDroop add() {
        return new AcDcConverterDroopImpl(uMin, uMax, k, converter);
    }
}
