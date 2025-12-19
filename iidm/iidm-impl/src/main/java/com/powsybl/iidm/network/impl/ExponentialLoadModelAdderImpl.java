/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.network.impl.ExponentialLoadModelImpl.checkExponent;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ExponentialLoadModelAdderImpl extends AbstractAdderWithProperties<ExponentialLoadModel, LoadAdder, ExponentialLoadModelAdder> implements ExponentialLoadModelAdder {

    private final LoadAdderImpl parentAdder;

    private double np = 0;
    private double nq = 0;

    public ExponentialLoadModelAdderImpl(LoadAdderImpl parentAdder) {
        this.parentAdder = Objects.requireNonNull(parentAdder);
    }

    @Override
    public ExponentialLoadModelAdderImpl setNp(double np) {
        this.np = checkExponent(parentAdder, np);
        return this;
    }

    @Override
    public ExponentialLoadModelAdderImpl setNq(double nq) {
        this.nq = checkExponent(parentAdder, nq);
        return this;
    }

    @Override
    protected ExponentialLoadModelImpl prepareObjectToAdd() {
        return new ExponentialLoadModelImpl(np, nq);
    }

    @Override
    protected LoadAdder addObject(ExponentialLoadModel model) {
        parentAdder.setModel((ExponentialLoadModelImpl) model);
        return parentAdder;
    }
}
