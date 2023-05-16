/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ExponentialLoadModel;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExponentialLoadModelImpl extends AbstractLoadModelImpl implements ExponentialLoadModel {

    private double np;
    private double nq;

    ExponentialLoadModelImpl(double np, double nq) {
        this.np = np;
        this.nq = nq;
    }

    static double checkExponent(Validable validable, double n) {
        if (Double.isNaN(n) || n < 0) {
            throw new ValidationException(validable, "Invalid load model exponential value: " + n);
        }
        return n;
    }

    @Override
    public double getNp() {
        return np;
    }

    @Override
    public ExponentialLoadModelImpl setNp(double np) {
        this.np = checkExponent(load, np);
        return this;
    }

    @Override
    public double getNq() {
        return nq;
    }

    @Override
    public ExponentialLoadModelImpl setNq(double nq) {
        this.nq = checkExponent(load, nq);
        return this;
    }
}
