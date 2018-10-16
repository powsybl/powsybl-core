/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TapChangerTapImpl<S extends TapChangerTapImpl<S>> {

    private double ratio;

    private double rdr;

    private double rdx;

    private double rdg;

    private double rdb;

    protected TapChangerTapImpl(double ratio, double rdr, double rdx, double rdg, double rdb) {
        this.ratio = ratio;
        this.rdr = rdr;
        this.rdx = rdx;
        this.rdg = rdg;
        this.rdb = rdb;
    }

    public double getRatio() {
        return ratio;
    }

    public S setRatio(double ratio) {
        this.ratio = ratio;
        return (S) this;
    }

    public double getRdr() {
        return rdr;
    }

    public S setRdr(double rdr) {
        this.rdr = rdr;
        return (S) this;
    }

    public double getRdx() {
        return rdx;
    }

    public S setRdx(double rdx) {
        this.rdx = rdx;
        return (S) this;
    }

    public double getRdb() {
        return rdb;
    }

    public S setRdb(double rdb) {
        this.rdb = rdb;
        return (S) this;
    }

    public double getRdg() {
        return rdg;
    }

    public S setRdg(double rdg) {
        this.rdg = rdg;
        return (S) this;
    }

}
