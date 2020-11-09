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
public class PsseTransformer {

    @Parsed
    @Validate
    private int i;

    @Parsed
    @Validate
    private int j;

    @Parsed
    private int k = 0;

    @Parsed
    private String ckt = "1";

    @Parsed
    private int cw = 1;

    @Parsed
    private int cz = 1;

    @Parsed
    private int cm = 1;

    @Parsed
    private double mag1 = 0;

    @Parsed
    private double mag2 = 0;

    @Parsed
    private int nmetr = 2;

    @Parsed
    private String name = "            ";

    @Parsed
    private int stat = 1;

    @Parsed
    private int o1 = -1;

    @Parsed
    private double f1 = 1;

    @Parsed
    private int o2 = 0;

    @Parsed
    private double f2 = 1;

    @Parsed
    private int o3 = 0;

    @Parsed
    private double f3 = 1;

    @Parsed
    private int o4 = 0;

    @Parsed
    private double f4 = 1;

    @Parsed
    private String vecgrp = "            ";

    @Parsed
    private double r12 = 0;

    @Parsed
    @Validate
    private double x12;

    @Parsed
    private double sbase12 = Double.NaN;

    @Parsed
    private double r23 = 0;

    @Parsed
    private double x23 = Double.NaN;

    @Parsed
    private double sbase23 = Double.NaN;

    @Parsed
    private double r31 = 0;

    @Parsed
    private double x31 = Double.NaN;

    @Parsed
    private double sbase31 = Double.NaN;

    @Parsed
    private double vmstar = 1;

    @Parsed
    private double anstar = 0;

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

    public static class WindingRecord {

        @Parsed
        private double windv = Double.NaN;

        @Parsed
        private double nomv = 0;

        @Parsed
        private double ang = 0;

        @Parsed
        private double rata = 0;

        @Parsed
        private double ratb = 0;

        @Parsed
        private double ratc = 0;

        @Parsed
        private int cod = 0;

        @Parsed
        private int cont = 0;

        @Parsed
        private double rma = Double.NaN;

        @Parsed
        private double rmi = Double.NaN;

        @Parsed
        private double vma = Double.NaN;

        @Parsed
        private double vmi = Double.NaN;

        @Parsed
        private int ntp = 33;

        @Parsed
        private int tab = 0;

        @Parsed
        private double cr = 0;

        @Parsed
        private double cx = 0;

        @Parsed
        private double cnxa = 0;

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

    private WindingRecord windingRecord1;
    private WindingRecord windingRecord2;
    private WindingRecord windingRecord3;

    public WindingRecord getWindingRecord1() {
        return windingRecord1;
    }

    public void setWindingRecord1(WindingRecord windingRecord1) {
        this.windingRecord1 = windingRecord1;
    }

    public WindingRecord getWindingRecord2() {
        return windingRecord2;
    }

    public void setWindingRecord2(WindingRecord windingRecord2) {
        this.windingRecord2 = windingRecord2;
    }

    public WindingRecord getWindingRecord3() {
        return windingRecord3;
    }

    public void setWindingRecord3(WindingRecord windingRecord3) {
        this.windingRecord3 = windingRecord3;
    }
}
