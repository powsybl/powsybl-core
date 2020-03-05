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
public class PsseTransformer {

    @Parsed(index = 0)
    private int i;

    @Parsed(index = 1)
    private int j;

    @Parsed(index = 2)
    private int k;

    @Parsed(index = 3)
    private String ckt;

    @Parsed(index = 4)
    private int cw;

    @Parsed(index = 5)
    private int cz;

    @Parsed(index = 6)
    private int cm;

    @Parsed(index = 7)
    private double mag1;

    @Parsed(index = 8)
    private double mag2;

    @Parsed(index = 9)
    private int nmetr;

    @Parsed(index = 10)
    private String name;

    @Parsed(index = 11)
    private int stat;

    @Parsed(index = 12)
    private int o1;

    @Parsed(index = 13)
    private double f1;

    @Parsed(index = 14)
    private int o2;

    @Parsed(index = 15)
    private double f2;

    @Parsed(index = 16)
    private int o3;

    @Parsed(index = 17)
    private double f3;

    @Parsed(index = 18)
    private int o4;

    @Parsed(index = 19)
    private double f4;

    @Parsed(index = 20)
    private String vecgrp;

    @Parsed(index = 21)
    private double r12;

    @Parsed(index = 22)
    private double x12;

    @Parsed(index = 23)
    private double sbase12;

    @Parsed(index = 24)
    private double r23;

    @Parsed(index = 25)
    private double x23;

    @Parsed(index = 26)
    private double sbase23;

    @Parsed(index = 27)
    private double r31;

    @Parsed(index = 28)
    private double x31;

    @Parsed(index = 29)
    private double sbase31;

    @Parsed(index = 30)
    private double vmstar;

    @Parsed(index = 31)
    private double anstar;

    private PsseWinding winding1;

    private PsseWinding winding2;

    private PsseWinding winding3;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public int getCw() {
        return cw;
    }

    public void setCw(int cw) {
        this.cw = cw;
    }

    public int getCz() {
        return cz;
    }

    public void setCz(int cz) {
        this.cz = cz;
    }

    public int getCm() {
        return cm;
    }

    public void setCm(int cm) {
        this.cm = cm;
    }

    public double getMag1() {
        return mag1;
    }

    public void setMag1(double mag1) {
        this.mag1 = mag1;
    }

    public double getMag2() {
        return mag2;
    }

    public void setMag2(double mag2) {
        this.mag2 = mag2;
    }

    public int getNmetr() {
        return nmetr;
    }

    public void setNmetr(int nmetr) {
        this.nmetr = nmetr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

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

    public String getVecgrp() {
        return vecgrp;
    }

    public void setVecgrp(String vecgrp) {
        this.vecgrp = vecgrp;
    }

    public double getR12() {
        return r12;
    }

    public void setR12(double r12) {
        this.r12 = r12;
    }

    public double getX12() {
        return x12;
    }

    public void setX12(double x12) {
        this.x12 = x12;
    }

    public double getSbase12() {
        return sbase12;
    }

    public void setSbase12(double sbase12) {
        this.sbase12 = sbase12;
    }

    public double getR23() {
        return r23;
    }

    public void setR23(double r23) {
        this.r23 = r23;
    }

    public double getX23() {
        return x23;
    }

    public void setX23(double x23) {
        this.x23 = x23;
    }

    public double getSbase23() {
        return sbase23;
    }

    public void setSbase23(double sbase23) {
        this.sbase23 = sbase23;
    }

    public double getR31() {
        return r31;
    }

    public void setR31(double r31) {
        this.r31 = r31;
    }

    public double getX31() {
        return x31;
    }

    public void setX31(double x31) {
        this.x31 = x31;
    }

    public double getSbase31() {
        return sbase31;
    }

    public void setSbase31(double sbase31) {
        this.sbase31 = sbase31;
    }

    public double getVmstar() {
        return vmstar;
    }

    public void setVmstar(double vmstar) {
        this.vmstar = vmstar;
    }

    public double getAnstar() {
        return anstar;
    }

    public void setAnstar(double anstar) {
        this.anstar = anstar;
    }

    public PsseWinding getWinding1() {
        return winding1;
    }

    public void setWinding1(PsseWinding winding1) {
        this.winding1 = winding1;
    }

    public PsseWinding getWinding2() {
        return winding2;
    }

    public void setWinding2(PsseWinding winding2) {
        this.winding2 = winding2;
    }

    public PsseWinding getWinding3() {
        return winding3;
    }

    public void setWinding3(PsseWinding winding3) {
        this.winding3 = winding3;
    }
}
