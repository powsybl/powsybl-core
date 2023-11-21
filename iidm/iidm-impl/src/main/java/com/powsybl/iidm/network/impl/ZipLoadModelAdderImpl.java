/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ZipLoadModelAdder;

import java.util.Objects;

import static com.powsybl.iidm.network.impl.ZipLoadModelImpl.checkCoefficient;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ZipLoadModelAdderImpl implements ZipLoadModelAdder {

    private final LoadAdderImpl parentAdder;

    private double c0p = 1;
    private double c1p = 0;
    private double c2p = 0;
    private double c0q = 1;
    private double c1q = 0;
    private double c2q = 0;

    public ZipLoadModelAdderImpl(LoadAdderImpl parentAdder) {
        this.parentAdder = Objects.requireNonNull(parentAdder);
    }

    @Override
    public ZipLoadModelAdderImpl setC0p(double c0p) {
        this.c0p = checkCoefficient(parentAdder, c0p);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setC1p(double c1p) {
        this.c1p = checkCoefficient(parentAdder, c1p);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setC2p(double c2p) {
        this.c2p = checkCoefficient(parentAdder, c2p);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setC0q(double c0q) {
        this.c0q = checkCoefficient(parentAdder, c0q);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setC1q(double c1q) {
        this.c1q = checkCoefficient(parentAdder, c1q);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setC2q(double c2q) {
        this.c2q = checkCoefficient(parentAdder, c2q);
        return this;
    }

    @Override
    public LoadAdderImpl add() {
        if (Math.abs(c0p + c1p + c2p - 1d) > SUM_EPSILON) {
            throw new ValidationException(parentAdder, "Sum of c0p, c1p and c2p should be 1");
        }
        if (Math.abs(c0q + c1q + c2q - 1d) > SUM_EPSILON) {
            throw new ValidationException(parentAdder, "Sum of c0q, c1q and c2q should be 1");
        }
        parentAdder.setModel(new ZipLoadModelImpl(c0p, c1p, c2p, c0q, c1q, c2q));
        return parentAdder;
    }
}
