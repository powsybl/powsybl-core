/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseFixedShunt {

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"id", "shntid"}, defaultNullRead = "1")
    private String id;

    @Parsed(field = {"status", "stat"})
    private int status = 1;

    @Parsed
    private double gl = 0;

    @Parsed
    private double bl = 0;

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

    public PsseFixedShunt copy() {
        PsseFixedShunt copy = new PsseFixedShunt();
        copy.i = this.i;
        copy.id = this.id;
        copy.status = this.status;
        copy.gl = this.gl;
        copy.bl = this.bl;
        return copy;
    }
}
