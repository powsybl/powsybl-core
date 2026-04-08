/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseInductionMachine {

    // This dataBlock is valid since version 33

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"id", "imid"}, defaultNullRead = "1")
    private String id;

    @NullString(nulls = {"null"})
    @Parsed
    private int stat = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private int scode = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private int dcode = 2;

    @NullString(nulls = {"null"})
    @Parsed
    private int area = -1;

    @NullString(nulls = {"null"})
    @Parsed
    private int zone = -1;

    @NullString(nulls = {"null"})
    @Parsed
    private int owner = -1;

    @NullString(nulls = {"null"})
    @Parsed
    private int tcode = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private int bcode = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private double mbase = -1.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double ratekv = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private int pcode = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private Double pset;

    @NullString(nulls = {"null"})
    @Parsed(field = {"h", "hconst"})
    private double h = 1.0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"a", "aconst"})
    private double a = 1.0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"b", "bconst"})
    private double b = 1.0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"d", "dconst"})
    private double d = 1.0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"e", "econst"})
    private double e = 1.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double ra = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double xa = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double xm = 2.5;

    @NullString(nulls = {"null"})
    @Parsed
    private double r1 = 999.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double x1 = 999.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double r2 = 999.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double x2 = 999.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double x3 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double e1 = 1.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double se1 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double e2 = 1.2;

    @NullString(nulls = {"null"})
    @Parsed
    private double se2 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double ia1 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double ia2 = 0.0;

    @NullString(nulls = {"null"})
    @Parsed
    private double xamult = 1.0;

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

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public int getScode() {
        return scode;
    }

    public void setScode(int scode) {
        this.scode = scode;
    }

    public int getDcode() {
        return dcode;
    }

    public void setDcode(int dcode) {
        this.dcode = dcode;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getTcode() {
        return tcode;
    }

    public void setTcode(int tcode) {
        this.tcode = tcode;
    }

    public int getBcode() {
        return bcode;
    }

    public void setBcode(int bcode) {
        this.bcode = bcode;
    }

    public double getMbase() {
        return mbase;
    }

    public void setMbase(double mbase) {
        this.mbase = mbase;
    }

    public double getRatekv() {
        return ratekv;
    }

    public void setRatekv(double ratekv) {
        this.ratekv = ratekv;
    }

    public int getPcode() {
        return pcode;
    }

    public void setPcode(int pcode) {
        this.pcode = pcode;
    }

    public Double getPset() {
        return pset;
    }

    public void setPset(Double pset) {
        this.pset = pset;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getE() {
        return e;
    }

    public void setE(double e) {
        this.e = e;
    }

    public double getRa() {
        return ra;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public double getXa() {
        return xa;
    }

    public void setXa(double xa) {
        this.xa = xa;
    }

    public double getXm() {
        return xm;
    }

    public void setXm(double xm) {
        this.xm = xm;
    }

    public double getR1() {
        return r1;
    }

    public void setR1(double r1) {
        this.r1 = r1;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getR2() {
        return r2;
    }

    public void setR2(double r2) {
        this.r2 = r2;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getX3() {
        return x3;
    }

    public void setX3(double x3) {
        this.x3 = x3;
    }

    public double getE1() {
        return e1;
    }

    public void setE1(double e1) {
        this.e1 = e1;
    }

    public double getSe1() {
        return se1;
    }

    public void setSe1(double se1) {
        this.se1 = se1;
    }

    public double getE2() {
        return e2;
    }

    public void setE2(double e2) {
        this.e2 = e2;
    }

    public double getSe2() {
        return se2;
    }

    public void setSe2(double se2) {
        this.se2 = se2;
    }

    public double getIa1() {
        return ia1;
    }

    public void setIa1(double ia1) {
        this.ia1 = ia1;
    }

    public double getIa2() {
        return ia2;
    }

    public void setIa2(double ia2) {
        this.ia2 = ia2;
    }

    public double getXamult() {
        return xamult;
    }

    public void setXamult(double xamult) {
        this.xamult = xamult;
    }
}
