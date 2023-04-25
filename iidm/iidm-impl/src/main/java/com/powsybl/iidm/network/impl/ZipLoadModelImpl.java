/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ZipLoadModel;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipLoadModelImpl extends AbstractLoadModelImpl implements ZipLoadModel {

    private double c0p;
    private double c1p;
    private double c2p;
    private double c0q;
    private double c1q;
    private double c2q;

    public ZipLoadModelImpl(double c0p, double c1p, double c2p, double c0q, double c1q, double c2q) {
        this.c0p = c0p;
        this.c1p = c1p;
        this.c2p = c2p;
        this.c0q = c0q;
        this.c1q = c1q;
        this.c2q = c2q;
    }

    static double checkCoefficient(Validable validable, double coefficient) {
        if (Double.isNaN(coefficient) || coefficient < 0) {
            throw new ValidationException(validable, "Invalid zip load model coefficient: " + coefficient);
        }
        return coefficient;
    }

    @Override
    public double getC0p() {
        return c0p;
    }

    @Override
    public ZipLoadModelImpl setC0p(double c0p) {
        this.c0p = checkCoefficient(load, c0p);
        return this;
    }

    @Override
    public double getC1p() {
        return c1p;
    }

    @Override
    public ZipLoadModelImpl setC1p(double c1p) {
        this.c1p = checkCoefficient(load, c1p);
        return this;
    }

    @Override
    public double getC2p() {
        return c2p;
    }

    @Override
    public ZipLoadModelImpl setC2p(double c2p) {
        this.c2p = checkCoefficient(load, c2p);
        return this;
    }

    @Override
    public double getC0q() {
        return c0q;
    }

    @Override
    public ZipLoadModelImpl setC0q(double c0q) {
        this.c0q = checkCoefficient(load, c0q);
        return this;
    }

    @Override
    public double getC1q() {
        return c1q;
    }

    @Override
    public ZipLoadModelImpl setC1q(double c1q) {
        this.c1q = checkCoefficient(load, c1q);
        return this;
    }

    @Override
    public double getC2q() {
        return c2q;
    }

    @Override
    public ZipLoadModelImpl setC2q(double c2q) {
        this.c2q = checkCoefficient(load, c2q);
        return this;
    }
}
