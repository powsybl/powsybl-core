/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseLoad35 extends PsseLoad {

    @Parsed
    private double dgenp = 0;

    @Parsed
    private double dgenq = 0;

    @Parsed
    private double dgenm = 0;

    @Parsed(defaultNullRead = "            ")
    private String loadtype;

    public double getDgenp() {
        return dgenp;
    }

    public void setDgenp(double dgenp) {
        this.dgenp = dgenp;
    }

    public double getDgenq() {
        return dgenq;
    }

    public void setDgenq(double dgenq) {
        this.dgenq = dgenq;
    }

    public double getDgenm() {
        return dgenm;
    }

    public void setDgenm(double dgenm) {
        this.dgenm = dgenm;
    }

    public String getLoadtype() {
        return loadtype;
    }

    public void setLoadtype(String loadtype) {
        this.loadtype = loadtype;
    }

    public void print() {
        System.err.printf("Ibus %d loadId %s p %f q %f %n", this.getI(), this.getId(), this.getPl(), this.getQl());
    }
}
