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
 * @author Jean-Baptiste Heyberger <Jean-Baptiste.Heyberger at rte-france.com>
 */
public class PsseSwitchedShunt {

    @Parsed(index = 0)
    @Validate
    private int i;

    @Parsed(index = 1)
    private int modsw = 1;

    @Parsed(index = 2)
    private int adjm = 0;

    @Parsed(index = 3)
    private int stat = 1;

    @Parsed(index = 4)
    private double vswhi = 1.0;

    @Parsed(index = 5)
    private double vswlo = 1.0;

    @Parsed(index = 6)
    private int swrem = 0;

    @Parsed(index = 7)
    private double rmpct = 100.0;

    @Parsed(index = 8)
    private String rmidnt = " ";

    @Parsed(index = 9)
    private double binit = 0.0;

    @Parsed(index = 10)
    private int n1 = 0;

    @Parsed(index = 11)
    private double b1 = 0.0;

    @Parsed(index = 12)
    private int n2 = 0;

    @Parsed(index = 13)
    private double b2 = 0.0;

    @Parsed(index = 14)
    private int n3 = 0;

    @Parsed(index = 15)
    private double b3 = 0.0;

    @Parsed(index = 16)
    private int n4 = 0;

    @Parsed(index = 17)
    private double b4 = 0.0;

    @Parsed(index = 18)
    private int n5 = 0;

    @Parsed(index = 19)
    private double b5 = 0.0;

    @Parsed(index = 20)
    private int n6 = 0;

    @Parsed(index = 21)
    private double b6 = 0.0;

    @Parsed(index = 22)
    private int n7 = 0;

    @Parsed(index = 23)
    private double b7 = 0.0;

    @Parsed(index = 24)
    private int n8 = 0;

    @Parsed(index = 25)
    private double b8 = 0.0;

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

    public  double getVswhi() {
        return vswhi;
    }

    public void setVswhi(double vswhi) {
        this.vswhi = vswhi;
    }

    public  double getVswlo() {
        return vswlo;
    }

    public void setVswlo(double vswlo) {
        this.vswlo = vswlo;
    }

    public int getSwrem() {
        return swrem;
    }

    public void setSwrem(int swrem) {
        this.swrem = swrem;
    }

    public  double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public  String getRmidnt() {
        return rmidnt;
    }

    public void setRmidnt(String rmidnt) {
        this.rmidnt = rmidnt;
    }

    public  double getBinit() {
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

    public  double getB1() {
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

    public  double getB2() {
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

    public  double getB3() {
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

    public  double getB4() {
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

    public  double getB5() {
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

    public  double getB6() {
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

    public  double getB7() {
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

    public  double getB8() {
        return b8;
    }

    public void setB8(double b8) {
        this.b8 = b8;
    }

}
