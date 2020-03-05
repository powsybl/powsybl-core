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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseFixedShunt {

    @Parsed(index = 0)
    private int i;

    @Parsed(index = 1)
    private String id;

    @Parsed(index = 2)
    private int status;

    @Parsed(index = 3)
    private double gl;

    @Parsed(index = 4)
    private double bl;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getGl() {
        return gl;
    }

    public void setGl(double gl) {
        this.gl = gl;
    }

    public double getBl() {
        return bl;
    }

    public void setBl(double bl) {
        this.bl = bl;
    }
}
