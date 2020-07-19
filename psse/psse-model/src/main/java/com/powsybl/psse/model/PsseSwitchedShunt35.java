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
public class PsseSwitchedShunt35 extends PsseSwitchedShunt {

    @Parsed(field = {"id", "shntid"}, defaultNullRead = "1")
    private String id;

    @Parsed
    private int swreg = 0;

    @Parsed
    private int nreg = 0;

    @Parsed
    private int s1 = 1;

    @Parsed
    private int s2 = 1;

    @Parsed
    private int s3 = 1;

    @Parsed
    private int s4 = 1;

    @Parsed
    private int s5 = 1;

    @Parsed
    private int s6 = 1;

    @Parsed
    private int s7 = 1;

    @Parsed
    private int s8 = 1;

    @Override
    public int getSwrem() {
        throw new PsseException("Swrem not available in version 35");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSwreg() {
        return swreg;
    }

    public void setSwreg(int swreg) {
        this.swreg = swreg;
    }

    public int getNreg() {
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public int getS1() {
        return s1;
    }

    public void setS1(int s1) {
        this.s1 = s1;
    }

    public int getS2() {
        return s2;
    }

    public void setS2(int s2) {
        this.s2 = s2;
    }

    public int getS3() {
        return s3;
    }

    public void setS3(int s3) {
        this.s3 = s3;
    }

    public int getS4() {
        return s4;
    }

    public void setS4(int s4) {
        this.s4 = s4;
    }

    public int getS5() {
        return s5;
    }

    public void setS5(int s5) {
        this.s5 = s5;
    }

    public int getS6() {
        return s6;
    }

    public void setS6(int s6) {
        this.s6 = s6;
    }

    public int getS7() {
        return s7;
    }

    public void setS7(int s7) {
        this.s7 = s7;
    }

    public int getS8() {
        return s8;
    }

    public void setS8(int s8) {
        this.s8 = s8;
    }
}
