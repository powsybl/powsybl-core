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

    public static class FirstRecord {

        @Parsed(index = 0)
        @Validate
        private int i;

        @Parsed(index = 1)
        @Validate
        private int j;

        @Parsed(index = 2)
        private int k = 0;

        @Parsed(index = 3)
        private String ckt = "1";

        @Parsed(index = 4)
        private int cw = 1;

        @Parsed(index = 5)
        private int cz = 1;

        @Parsed(index = 6)
        private int cm = 1;

        @Parsed(index = 7)
        private double mag1 = 0;

        @Parsed(index = 8)
        private double mag2 = 0;

        @Parsed(index = 9)
        private int nmetr = 2;

        @Parsed(index = 10)
        private String name = "            ";

        @Parsed(index = 11)
        private int stat = 1;

        @Parsed(index = 12)
        private int o1 = -1;

        @Parsed(index = 13)
        private double f1 = 1;

        @Parsed(index = 14)
        private int o2 = 0;

        @Parsed(index = 15)
        private double f2 = 1;

        @Parsed(index = 16)
        private int o3 = 0;

        @Parsed(index = 17)
        private double f3 = 1;

        @Parsed(index = 18)
        private int o4 = 0;

        @Parsed(index = 19)
        private double f4 = 1;

        @Parsed(index = 20)
        private String vecgrp = "            ";

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
    }

    public static class SecondRecord {

        @Parsed(index = 0)
        private double r12 = 0;

        @Parsed(index = 1)
        @Validate
        private double x12;

        @Parsed(index = 2)
        private double sbase12 = Double.NaN;

        @Parsed(index = 3)
        private double r23 = 0;

        @Parsed(index = 4)
        private double x23 = Double.NaN;

        @Parsed(index = 5)
        private double sbase23 = Double.NaN;

        @Parsed(index = 6)
        private double r31 = 0;

        @Parsed(index = 7)
        private double x31 = Double.NaN;

        @Parsed(index = 8)
        private double sbase31 = Double.NaN;

        @Parsed(index = 9)
        private double vmstar = 1;

        @Parsed(index = 10)
        private double anstar = 0;

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
    }

    public static class ThirdRecord {

        @Parsed(index = 0)
        private double windv = Double.NaN;

        @Parsed(index = 1)
        private double nomv = 0;

        @Parsed(index = 2)
        private double ang = 0;

        @Parsed(index = 3)
        private double rata = 0;

        @Parsed(index = 4)
        private double ratb = 0;

        @Parsed(index = 5)
        private double ratc = 0;

        @Parsed(index = 6)
        private int cod = 0;

        @Parsed(index = 7)
        private int cont = 0;

        @Parsed(index = 8)
        private double rma = Double.NaN;

        @Parsed(index = 9)
        private double rmi = Double.NaN;

        @Parsed(index = 10)
        private double vma = Double.NaN;

        @Parsed(index = 11)
        private double vmi = Double.NaN;

        @Parsed(index = 12)
        private int ntp = 33;

        @Parsed(index = 13)
        private int tab = 0;

        @Parsed(index = 14)
        private double cr = 0;

        @Parsed(index = 15)
        private double cx = 0;

        @Parsed(index = 16)
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

    private FirstRecord firstRecord;

    private SecondRecord secondRecord;

    private ThirdRecord thirdRecord1;

    private ThirdRecord thirdRecord2;

    private ThirdRecord thirdRecord3;

    public FirstRecord getFirstRecord() {
        return firstRecord;
    }

    public void setFirstRecord(FirstRecord firstRecord) {
        this.firstRecord = firstRecord;
    }

    public SecondRecord getSecondRecord() {
        return secondRecord;
    }

    public void setSecondRecord(SecondRecord secondRecord) {
        this.secondRecord = secondRecord;
    }

    public ThirdRecord getThirdRecord1() {
        return thirdRecord1;
    }

    public void setThirdRecord1(ThirdRecord thirdRecord1) {
        this.thirdRecord1 = thirdRecord1;
    }

    public ThirdRecord getThirdRecord2() {
        return thirdRecord2;
    }

    public void setThirdRecord2(ThirdRecord thirdRecord2) {
        this.thirdRecord2 = thirdRecord2;
    }

    public ThirdRecord getThirdRecord3() {
        return thirdRecord3;
    }

    public void setThirdRecord3(ThirdRecord thirdRecord3) {
        this.thirdRecord3 = thirdRecord3;
    }
}
