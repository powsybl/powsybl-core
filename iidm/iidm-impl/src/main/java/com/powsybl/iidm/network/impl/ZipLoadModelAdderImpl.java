/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ZipLoadModelAdder;

import java.util.Objects;

import static com.powsybl.iidm.network.impl.ZipLoadModelImpl.checkCoefficient;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipLoadModelAdderImpl implements ZipLoadModelAdder {

    private final LoadAdderImpl parentAdder;

    private double pp = 1;
    private double ip = 0;
    private double zp = 0;
    private double pq = 1;
    private double iq = 0;
    private double zq = 0;

    public ZipLoadModelAdderImpl(LoadAdderImpl parentAdder) {
        this.parentAdder = Objects.requireNonNull(parentAdder);
    }

    @Override
    public ZipLoadModelAdderImpl setPp(double pp) {
        this.pp = checkCoefficient(parentAdder, pp);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setIp(double ip) {
        this.ip = checkCoefficient(parentAdder, ip);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setZp(double zp) {
        this.zp = checkCoefficient(parentAdder, zp);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setPq(double pq) {
        this.pq = checkCoefficient(parentAdder, pq);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setIq(double iq) {
        this.iq = checkCoefficient(parentAdder, iq);
        return this;
    }

    @Override
    public ZipLoadModelAdderImpl setZq(double zq) {
        this.zq = checkCoefficient(parentAdder, zq);
        return this;
    }

    @Override
    public LoadAdderImpl add() {
        parentAdder.setModel(new ZipLoadModelImpl(pp, ip, zp, pq, iq, zq));
        return parentAdder;
    }
}
