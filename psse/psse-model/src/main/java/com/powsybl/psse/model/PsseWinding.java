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
public class PsseWinding {

    @Parsed(index = 0)
    private double windv;

    @Parsed(index = 1)
    private double nomv;

    @Parsed(index = 2)
    private double ang;

    @Parsed(index = 3)
    private double rata;

    @Parsed(index = 4)
    private double ratb;

    @Parsed(index = 5)
    private double ratc;

    @Parsed(index = 6)
    private int cod;

    @Parsed(index = 7)
    private int cont;

    @Parsed(index = 8)
    private double rma;

    @Parsed(index = 9)
    private double rmi;

    @Parsed(index = 10)
    private double vma;

    @Parsed(index = 11)
    private double vmi;

    @Parsed(index = 12)
    private int ntp;

    @Parsed(index = 13)
    private int tab;

    @Parsed(index = 14)
    private double cr;

    @Parsed(index = 15)
    private double cx;

    @Parsed(index = 16)
    private double cnxa;

    public double getWindv() {
        return windv;
    }

    public void setWindv(double windv) {
        this.windv = windv;
    }

    public double getNomv() {
        return nomv;
    }

    public void setNomv(double nomv) {
        this.nomv = nomv;
    }

    public double getAng() {
        return ang;
    }

    public void setAng(double ang) {
        this.ang = ang;
    }

    public double getRata() {
        return rata;
    }

    public void setRata(double rata) {
        this.rata = rata;
    }

    public double getRatb() {
        return ratb;
    }

    public void setRatb(double ratb) {
        this.ratb = ratb;
    }

    public double getRatc() {
        return ratc;
    }

    public void setRatc(double ratc) {
        this.ratc = ratc;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public int getCont() {
        return cont;
    }

    public void setCont(int cont) {
        this.cont = cont;
    }

    public double getRma() {
        return rma;
    }

    public void setRma(double rma) {
        this.rma = rma;
    }

    public double getRmi() {
        return rmi;
    }

    public void setRmi(double rmi) {
        this.rmi = rmi;
    }

    public double getVma() {
        return vma;
    }

    public void setVma(double vma) {
        this.vma = vma;
    }

    public double getVmi() {
        return vmi;
    }

    public void setVmi(double vmi) {
        this.vmi = vmi;
    }

    public int getNtp() {
        return ntp;
    }

    public void setNtp(int ntp) {
        this.ntp = ntp;
    }

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }

    public double getCr() {
        return cr;
    }

    public void setCr(double cr) {
        this.cr = cr;
    }

    public double getCx() {
        return cx;
    }

    public void setCx(double cx) {
        this.cx = cx;
    }

    public double getCnxa() {
        return cnxa;
    }

    public void setCnxa(double cnxa) {
        this.cnxa = cnxa;
    }
}
