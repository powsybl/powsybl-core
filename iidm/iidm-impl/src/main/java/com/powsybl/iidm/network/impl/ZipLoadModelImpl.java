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

    private double pp;
    private double ip;
    private double zp;
    private double pq;
    private double iq;
    private double zq;

    public ZipLoadModelImpl(double pp, double ip, double zp, double pq, double iq, double zq) {
        this.pp = pp;
        this.ip = ip;
        this.zp = zp;
        this.pq = pq;
        this.iq = iq;
        this.zq = zq;
    }

    static double checkCoefficient(Validable validable, double coefficient) {
        if (Double.isNaN(coefficient) || coefficient < 0) {
            throw new ValidationException(validable, "Invalid zip load model coefficient: " + coefficient);
        }
        return coefficient;
    }

    @Override
    public double getPp() {
        return pp;
    }

    @Override
    public ZipLoadModelImpl setPp(double pp) {
        this.pp = checkCoefficient(load, pp);
        return this;
    }

    @Override
    public double getIp() {
        return ip;
    }

    @Override
    public ZipLoadModelImpl setIp(double ip) {
        this.ip = checkCoefficient(load, ip);
        return this;
    }

    @Override
    public double getZp() {
        return zp;
    }

    @Override
    public ZipLoadModelImpl setZp(double zp) {
        this.zp = checkCoefficient(load, zp);
        return this;
    }

    @Override
    public double getPq() {
        return pq;
    }

    @Override
    public ZipLoadModelImpl setPq(double pq) {
        this.pq = checkCoefficient(load, pq);
        return this;
    }

    @Override
    public double getIq() {
        return iq;
    }

    @Override
    public ZipLoadModelImpl setIq(double iq) {
        this.iq = checkCoefficient(load, iq);
        return this;
    }

    @Override
    public double getZq() {
        return zq;
    }

    @Override
    public ZipLoadModelImpl setZq(double zq) {
        this.zq = checkCoefficient(load, zq);
        return this;
    }
}
