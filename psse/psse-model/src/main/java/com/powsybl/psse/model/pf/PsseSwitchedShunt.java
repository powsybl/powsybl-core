/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Jean-Baptiste Heyberger {@literal <Jean-Baptiste.Heyberger at rte-france.com>}
 */
public class PsseSwitchedShunt extends PsseVersioned {

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed
    private int modsw = 1;

    @Parsed
    private int adjm = 0;

    @Parsed
    private int stat = 1;

    @Parsed
    private double vswhi = 1.0;

    @Parsed
    private double vswlo = 1.0;

    @Parsed
    @Revision(until = 33)
    private int swrem = 0;

    @Parsed
    private double rmpct = 100.0;

    @Parsed(defaultNullRead = " ")
    private String rmidnt;

    @Parsed
    private double binit = 0.0;

    @Parsed
    private int n1 = 0;

    @Parsed
    private double b1 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int n2 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double b2 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int n3 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double b3 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int n4 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double b4 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int n5 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double b5 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int n6 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double b6 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int n7 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double b7 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int n8 = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double b8 = 0.0;

    @Parsed(field = {"id", "shntid"}, defaultNullRead = "1")
    @Revision(since = 35)
    private String id;

    @Parsed
    @Revision(since = 35)
    private int swreg = 0;

    @Parsed
    @Revision(since = 35)
    private int nreg = 0;

    @Parsed
    @Revision(since = 35)
    private int s1 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int s2 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int s3 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int s4 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int s5 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int s6 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int s7 = 1;

    @NullString(nulls = {"null"})
    @Parsed
    @Revision(since = 35)
    private int s8 = 1;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getModsw() {
        return modsw;
    }

    public void setModsw(int modsw) {
        this.modsw = modsw;
    }

    public int getAdjm() {
        return adjm;
    }

    public void setAdjm(int adjm) {
        this.adjm = adjm;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public double getVswhi() {
        return vswhi;
    }

    public void setVswhi(double vswhi) {
        this.vswhi = vswhi;
    }

    public double getVswlo() {
        return vswlo;
    }

    public void setVswlo(double vswlo) {
        this.vswlo = vswlo;
    }

    public int getSwrem() {
        checkVersion("swrem");
        return swrem;
    }

    public void setSwrem(int swrem) {
        this.swrem = swrem;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public String getRmidnt() {
        return rmidnt;
    }

    public void setRmidnt(String rmidnt) {
        this.rmidnt = rmidnt;
    }

    public double getBinit() {
        return binit;
    }

    public void setBinit(double binit) {
        this.binit = binit;
    }

    public int getN1() {
        return n1;
    }

    public void setN1(int n1) {
        this.n1 = n1;
    }

    public double getB1() {
        return b1;
    }

    public void setB1(double b1) {
        this.b1 = b1;
    }

    public int getN2() {
        return n2;
    }

    public void setN2(int n2) {
        this.n2 = n2;
    }

    public double getB2() {
        return b2;
    }

    public void setB2(double b2) {
        this.b2 = b2;
    }

    public int getN3() {
        return n3;
    }

    public void setN3(int n3) {
        this.n3 = n3;
    }

    public double getB3() {
        return b3;
    }

    public void setB3(double b3) {
        this.b3 = b3;
    }

    public int getN4() {
        return n4;
    }

    public void setN4(int n4) {
        this.n4 = n4;
    }

    public double getB4() {
        return b4;
    }

    public void setB4(double b4) {
        this.b4 = b4;
    }

    public int getN5() {
        return n5;
    }

    public void setN5(int n5) {
        this.n5 = n5;
    }

    public double getB5() {
        return b5;
    }

    public void setB5(double b5) {
        this.b5 = b5;
    }

    public int getN6() {
        return n6;
    }

    public void setN6(int n6) {
        this.n6 = n6;
    }

    public double getB6() {
        return b6;
    }

    public void setB6(double b6) {
        this.b6 = b6;
    }

    public int getN7() {
        return n7;
    }

    public void setN7(int n7) {
        this.n7 = n7;
    }

    public double getB7() {
        return b7;
    }

    public void setB7(double b7) {
        this.b7 = b7;
    }

    public int getN8() {
        return n8;
    }

    public void setN8(int n8) {
        this.n8 = n8;
    }

    public double getB8() {
        return b8;
    }

    public void setB8(double b8) {
        this.b8 = b8;
    }

    public String getId() {
        checkVersion("id");
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSwreg() {
        checkVersion("swreg");
        return swreg;
    }

    public void setSwreg(int swreg) {
        this.swreg = swreg;
    }

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public int getS1() {
        checkVersion("s1");
        return s1;
    }

    public void setS1(int s1) {
        this.s1 = s1;
    }

    public int getS2() {
        checkVersion("s2");
        return s2;
    }

    public void setS2(int s2) {
        this.s2 = s2;
    }

    public int getS3() {
        checkVersion("s3");
        return s3;
    }

    public void setS3(int s3) {
        this.s3 = s3;
    }

    public int getS4() {
        checkVersion("s4");
        return s4;
    }

    public void setS4(int s4) {
        this.s4 = s4;
    }

    public int getS5() {
        checkVersion("s5");
        return s5;
    }

    public void setS5(int s5) {
        this.s5 = s5;
    }

    public int getS6() {
        checkVersion("s6");
        return s6;
    }

    public void setS6(int s6) {
        this.s6 = s6;
    }

    public int getS7() {
        checkVersion("s7");
        return s7;
    }

    public void setS7(int s7) {
        this.s7 = s7;
    }

    public int getS8() {
        checkVersion("s8");
        return s8;
    }

    public void setS8(int s8) {
        this.s8 = s8;
    }

    public PsseSwitchedShunt copy() {
        PsseSwitchedShunt copy = new PsseSwitchedShunt();
        copy.i = this.i;
        copy.modsw = this.modsw;
        copy.adjm = this.adjm;
        copy.stat = this.stat;
        copy.vswhi = this.vswhi;
        copy.vswlo = this.vswlo;
        copy.swrem = this.swrem;
        copy.rmpct = this.rmpct;
        copy.rmidnt = this.rmidnt;
        copy.binit = this.binit;
        copy.n1 = this.n1;
        copy.b1 = this.b1;
        copy.n2 = this.n2;
        copy.b2 = this.b2;
        copy.n3 = this.n3;
        copy.b3 = this.b3;
        copy.n4 = this.n4;
        copy.b4 = this.b4;
        copy.n5 = this.n5;
        copy.b5 = this.b5;
        copy.n6 = this.n6;
        copy.b6 = this.b6;
        copy.n7 = this.n7;
        copy.b7 = this.b7;
        copy.n8 = this.n8;
        copy.b8 = this.b8;
        copy.id = this.id;
        copy.swreg = this.swreg;
        copy.nreg = this.nreg;
        copy.s1 = this.s1;
        copy.s2 = this.s2;
        copy.s3 = this.s3;
        copy.s4 = this.s4;
        copy.s5 = this.s5;
        copy.s6 = this.s6;
        copy.s7 = this.s7;
        copy.s8 = this.s8;
        return copy;
    }
}
