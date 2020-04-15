/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Validate;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseArea {

    @Parsed(index = 0)
    @Validate
    private int i;

    @Parsed(index = 1)
    private int isw = 0;

    @Parsed(index = 2)
    private double pdes = 0;

    @Parsed(index = 3)
    private double ptol = 10;

    @Parsed(index = 4)
    private String arname = "            ";

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getIsw() {
        return isw;
    }

    public void setIsw(int isw) {
        this.isw = isw;
    }

    public double getPdes() {
        return pdes;
    }

    public void setPdes(double pdes) {
        this.pdes = pdes;
    }

    public double getPtol() {
        return ptol;
    }

    public void setPtol(double ptol) {
        this.ptol = ptol;
    }

    public String getArname() {
        return arname;
    }

    public void setArname(String arname) {
        this.arname = arname;
    }
}
