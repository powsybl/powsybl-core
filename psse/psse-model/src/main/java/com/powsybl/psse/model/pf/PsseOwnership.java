/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseOwnership extends PsseVersioned {
    @Parsed
    private int o1 = -1;

    @Parsed
    private double f1 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private int o2 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double f2 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private int o3 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double f3 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private int o4 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double f4 = 1;

    public int getO1() {
        return o1;
    }

    public void setO1(int o1) {
        this.o1 = o1;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public int getO2() {
        return o2;
    }

    public void setO2(int o2) {
        this.o2 = o2;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public int getO3() {
        return o3;
    }

    public void setO3(int o3) {
        this.o3 = o3;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public int getO4() {
        return o4;
    }

    public void setO4(int o4) {
        this.o4 = o4;
    }

    public double getF4() {
        return f4;
    }

    public void setF4(double f4) {
        this.f4 = f4;
    }

    public PsseOwnership copy() {
        PsseOwnership copy = new PsseOwnership();
        copy.o1 = this.o1;
        copy.f1 = this.f1;
        copy.o2 = this.o2;
        copy.f2 = this.f2;
        copy.o3 = this.o3;
        copy.f3 = this.f3;
        copy.o4 = this.o4;
        copy.f4 = this.f4;
        return copy;
    }
}
