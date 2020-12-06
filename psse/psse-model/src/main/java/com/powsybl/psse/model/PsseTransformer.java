/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

// order using alphabetic order
@JsonPropertyOrder(alphabetic = true)
@JsonFilter("PsseVersionFilter")
public class PsseTransformer extends Versioned {

    private static final String WINDING_RECORD = "WindingRecord";

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"j", "jbus"})
    private int j;

    @Parsed(field = {"k", "kbus"})
    private int k = 0;

    @Parsed(defaultNullRead = "1")
    private String ckt;

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

    @Parsed(field = {"nmetr", "nmet"})
    private int nmetr = 2;

    @Parsed(defaultNullRead = "            ")
    private String name;

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

    @Parsed(defaultNullRead = "            ")
    private String vecgrp;

    @Parsed(field = {"r12", "r1_2"})
    private double r12 = 0;

    @Parsed(field = {"x12", "x1_2"})
    private double x12;

    @Parsed(field = {"sbase12", "sbase1_2"})
    private double sbase12 = Double.NaN;

    @Parsed(field = {"r23", "r2_3"})
    private double r23 = 0;

    @Parsed(field = {"x23", "x2_3"})
    private double x23 = Double.NaN;

    @Parsed(field = {"sbase23", "sbase2_3"})
    private double sbase23 = Double.NaN;

    @Parsed(field = {"r31", "r3_1"})
    private double r31 = 0;

    @Parsed(field = {"x31", "x3_1"})
    private double x31 = Double.NaN;

    @Parsed(field = {"sbase31", "sbase3_1"})
    private double sbase31 = Double.NaN;

    @Parsed
    private double vmstar = 1;

    @Parsed
    private double anstar = 0;

    @Parsed
    protected double windv1 = Double.NaN;

    @Parsed
    protected double nomv1 = 0;

    @Parsed
    protected double ang1 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double rata1 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double ratb1 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double ratc1 = 0;

    @Parsed
    protected int cod1 = 0;

    @Parsed
    protected int cont1 = 0;

    @Parsed
    protected double rma1 = Double.NaN;

    @Parsed
    protected double rmi1 = Double.NaN;

    @Parsed
    protected double vma1 = Double.NaN;

    @Parsed
    protected double vmi1 = Double.NaN;

    @Parsed
    protected int ntp1 = 33;

    @Parsed
    protected int tab1 = 0;

    @Parsed
    protected double cr1 = 0;

    @Parsed
    protected double cx1 = 0;

    @Parsed
    protected double cnxa1 = 0;

    @Parsed
    protected double windv2 = Double.NaN;

    @Parsed
    protected double nomv2 = 0;

    @Parsed
    protected double ang2 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double rata2 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double ratb2 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double ratc2 = 0;

    @Parsed
    protected int cod2 = 0;

    @Parsed
    protected int cont2 = 0;

    @Parsed
    protected double rma2 = Double.NaN;

    @Parsed
    protected double rmi2 = Double.NaN;

    @Parsed
    protected double vma2 = Double.NaN;

    @Parsed
    protected double vmi2 = Double.NaN;

    @Parsed
    protected int ntp2 = 33;

    @Parsed
    protected int tab2 = 0;

    @Parsed
    protected double cr2 = 0;

    @Parsed
    protected double cx2 = 0;

    @Parsed
    protected double cnxa2 = 0;

    @Parsed
    protected double windv3 = Double.NaN;

    @Parsed
    protected double nomv3 = 0;

    @Parsed
    protected double ang3 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double rata3 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double ratb3 = 0;

    @Parsed
    @PsseRev(until = 33)
    protected double ratc3 = 0;

    @Parsed
    protected int cod3 = 0;

    @Parsed
    protected int cont3 = 0;

    @Parsed
    protected double rma3 = Double.NaN;

    @Parsed
    protected double rmi3 = Double.NaN;

    @Parsed
    protected double vma3 = Double.NaN;

    @Parsed
    protected double vmi3 = Double.NaN;

    @Parsed
    protected int ntp3 = 33;

    @Parsed
    protected int tab3 = 0;

    @Parsed
    protected double cr3 = 0;

    @Parsed
    protected double cx3 = 0;

    @Parsed
    protected double cnxa3 = 0;

    @Parsed
    @PsseRev(since = 35)
    private int zcod = 0;

    @Parsed(field = {"wdg1rate1"})
    @PsseRev(since = 35)
    private double rate11 = 0.0;

    @Parsed(field = {"wdg1rate2"})
    @PsseRev(since = 35)
    private double rate21 = 0.0;

    @Parsed(field = {"wdg1rate3"})
    @PsseRev(since = 35)
    private double rate31 = 0.0;

    @Parsed(field = {"wdg1rate4"})
    @PsseRev(since = 35)
    private double rate41 = 0.0;

    @Parsed(field = {"wdg1rate5"})
    @PsseRev(since = 35)
    private double rate51 = 0.0;

    @Parsed(field = {"wdg1rate6"})
    @PsseRev(since = 35)
    private double rate61 = 0.0;

    @Parsed(field = {"wdg1rate7"})
    @PsseRev(since = 35)
    private double rate71 = 0.0;

    @Parsed(field = {"wdg1rate8"})
    @PsseRev(since = 35)
    private double rate81 = 0.0;

    @Parsed(field = {"wdg1rate9"})
    @PsseRev(since = 35)
    private double rate91 = 0.0;

    @Parsed(field = {"wdg1rate10"})
    @PsseRev(since = 35)
    private double rate101 = 0.0;

    @Parsed(field = {"wdg1rate11"})
    @PsseRev(since = 35)
    private double rate111 = 0.0;

    @Parsed(field = {"wdg1rate12"})
    @PsseRev(since = 35)
    private double rate121 = 0.0;

    @Parsed
    @PsseRev(since = 35)
    private int node1 = 0;

    @Parsed(field = {"wdg2rate1"})
    @PsseRev(since = 35)
    private double rate12 = 0.0;

    @Parsed(field = {"wdg2rate2"})
    @PsseRev(since = 35)
    private double rate22 = 0.0;

    @Parsed(field = {"wdg2rate3"})
    @PsseRev(since = 35)
    private double rate32 = 0.0;

    @Parsed(field = {"wdg2rate4"})
    @PsseRev(since = 35)
    private double rate42 = 0.0;

    @Parsed(field = {"wdg2rate5"})
    @PsseRev(since = 35)
    private double rate52 = 0.0;

    @Parsed(field = {"wdg2rate6"})
    @PsseRev(since = 35)
    private double rate62 = 0.0;

    @Parsed(field = {"wdg2rate7"})
    @PsseRev(since = 35)
    private double rate72 = 0.0;

    @Parsed(field = {"wdg2rate8"})
    @PsseRev(since = 35)
    private double rate82 = 0.0;

    @Parsed(field = {"wdg2rate9"})
    @PsseRev(since = 35)
    private double rate92 = 0.0;

    @Parsed(field = {"wdg2rate10"})
    @PsseRev(since = 35)
    private double rate102 = 0.0;

    @Parsed(field = {"wdg2rate11"})
    @PsseRev(since = 35)
    private double rate112 = 0.0;

    @Parsed(field = {"wdg2rate12"})
    @PsseRev(since = 35)
    private double rate122 = 0.0;

    @Parsed
    @PsseRev(since = 35)
    private int node2 = 0;

    @Parsed(field = {"wdg3rate1"})
    @PsseRev(since = 35)
    private double rate13 = 0.0;

    @Parsed(field = {"wdg3rate2"})
    @PsseRev(since = 35)
    private double rate23 = 0.0;

    @Parsed(field = {"wdg3rate3"})
    @PsseRev(since = 35)
    private double rate33 = 0.0;

    @Parsed(field = {"wdg3rate4"})
    @PsseRev(since = 35)
    private double rate43 = 0.0;

    @Parsed(field = {"wdg3rate5"})
    @PsseRev(since = 35)
    private double rate53 = 0.0;

    @Parsed(field = {"wdg3rate6"})
    @PsseRev(since = 35)
    private double rate63 = 0.0;

    @Parsed(field = {"wdg3rate7"})
    @PsseRev(since = 35)
    private double rate73 = 0.0;

    @Parsed(field = {"wdg3rate8"})
    @PsseRev(since = 35)
    private double rate83 = 0.0;

    @Parsed(field = {"wdg3rate9"})
    @PsseRev(since = 35)
    private double rate93 = 0.0;

    @Parsed(field = {"wdg3rate10"})
    @PsseRev(since = 35)
    private double rate103 = 0.0;

    @Parsed(field = {"wdg3rate11"})
    @PsseRev(since = 35)
    private double rate113 = 0.0;

    @Parsed(field = {"wdg3rate12"})
    @PsseRev(since = 35)
    private double rate123 = 0.0;

    @Parsed
    @PsseRev(since = 35)
    private int node3 = 0;

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

    public int getZcod() {
        checkVersion("zcod");
        return zcod;
    }

    public void setZcod(int zcod) {
        this.zcod = zcod;
    }

    public WindingRecord getWindingRecord1() {
        return new WindingRecord(windv1, nomv1, ang1, rata1, ratb1, ratc1, rate11, rate21, rate31, rate41, rate51, rate61,
            rate71, rate81, rate91, rate101, rate111, rate121, cod1, cont1, node1, rma1, rmi1, vma1, vmi1, ntp1, tab1, cr1,
            cx1, cnxa1);
    }

    public WindingRecord getWindingRecord2() {
        return new WindingRecord(windv2, nomv2, ang2, rata2, ratb2, ratc2, rate11, rate22, rate32, rate42, rate52, rate62,
            rate72, rate82, rate92, rate102, rate112, rate122, cod2, cont2, node2, rma2, rmi2, vma2, vmi2, ntp2, tab2, cr2,
            cx2, cnxa2);
    }

    public WindingRecord getWindingRecord3() {
        return new WindingRecord(windv3, nomv3, ang3, rata3, ratb3, ratc3, rate13, rate23, rate33, rate43, rate53, rate63,
            rate73, rate83, rate93, rate103, rate113, rate123, cod3, cont3, node3, rma3, rmi3, vma3, vmi3, ntp3, tab3, cr3,
            cx3, cnxa3);
    }

    @JsonFilter("PsseVersionFilter")
    public class WindingRecord {
        private final double windv;
        private final double nomv;
        private final double ang;
        @PsseRev(until = 33)
        private final double rata;
        @PsseRev(until = 33)
        private final double ratb;
        @PsseRev(until = 33)
        private final double ratc;
        private final int cod;
        private final int cont;
        private final double rma;
        private final double rmi;
        private final double vma;
        private final double vmi;
        private final int ntp;
        private final int tab;
        private final double cr;
        private final double cx;
        private final double cnxa;
        @PsseRev(since = 35)
        private final double rate1;
        @PsseRev(since = 35)
        private final double rate2;
        @PsseRev(since = 35)
        private final double rate3;
        @PsseRev(since = 35)
        private final double rate4;
        @PsseRev(since = 35)
        private final double rate5;
        @PsseRev(since = 35)
        private final double rate6;
        @PsseRev(since = 35)
        private final double rate7;
        @PsseRev(since = 35)
        private final double rate8;
        @PsseRev(since = 35)
        private final double rate9;
        @PsseRev(since = 35)
        private final double rate10;
        @PsseRev(since = 35)
        private final double rate11;
        @PsseRev(since = 35)
        private final double rate12;
        @PsseRev(since = 35)
        private final int node;

        WindingRecord(double windv, double nomv, double ang, double rata, double ratb, double ratc, int cod, int cont,
            double rma, double rmi, double vma, double vmi, int ntp, int tab, double cr, double cx, double cnxa) {
            this.windv = windv;
            this.nomv = nomv;
            this.ang = ang;
            this.rata = rata;
            this.ratb = ratb;
            this.ratc = ratc;
            this.cod = cod;
            this.cont = cont;
            this.rma = rma;
            this.rmi = rmi;
            this.vma = vma;
            this.vmi = vmi;
            this.ntp = ntp;
            this.tab = tab;
            this.cr = cr;
            this.cx = cx;
            this.cnxa = cnxa;
            this.rate1 = 0.0;
            this.rate2 = 0.0;
            this.rate3 = 0.0;
            this.rate4 = 0.0;
            this.rate5 = 0.0;
            this.rate6 = 0.0;
            this.rate7 = 0.0;
            this.rate8 = 0.0;
            this.rate9 = 0.0;
            this.rate10 = 0.0;
            this.rate11 = 0.0;
            this.rate12 = 0.0;
            this.node = 0;
        }

        WindingRecord(double windv, double nomv, double ang, double rate1, double rate2, double rate3,
            double rate4, double rate5, double rate6, double rate7, double rate8, double rate9, double rate10,
            double rate11, double rate12, int cod, int cont, int node, double rma, double rmi, double vma, double vmi,
            int ntp, int tab, double cr, double cx, double cnxa) {
            this.windv = windv;
            this.nomv = nomv;
            this.ang = ang;
            this.rata = 0.0;
            this.ratb = 0.0;
            this.ratc = 0.0;
            this.cod = cod;
            this.cont = cont;
            this.rma = rma;
            this.rmi = rmi;
            this.vma = vma;
            this.vmi = vmi;
            this.ntp = ntp;
            this.tab = tab;
            this.cr = cr;
            this.cx = cx;
            this.cnxa = cnxa;
            this.rate1 = rate1;
            this.rate2 = rate2;
            this.rate3 = rate3;
            this.rate4 = rate4;
            this.rate5 = rate5;
            this.rate6 = rate6;
            this.rate7 = rate7;
            this.rate8 = rate8;
            this.rate9 = rate9;
            this.rate10 = rate10;
            this.rate11 = rate11;
            this.rate12 = rate12;
            this.node = node;
        }

        WindingRecord(double windv, double nomv, double ang, double rata, double ratb, double ratc,
            double rate1, double rate2, double rate3, double rate4, double rate5, double rate6,
            double rate7, double rate8, double rate9, double rate10, double rate11, double rate12,
            int cod, int cont, int node, double rma, double rmi, double vma, double vmi,
            int ntp, int tab, double cr, double cx, double cnxa) {
            this.windv = windv;
            this.nomv = nomv;
            this.ang = ang;
            this.rata = rata;
            this.ratb = ratb;
            this.ratc = ratc;
            this.cod = cod;
            this.cont = cont;
            this.rma = rma;
            this.rmi = rmi;
            this.vma = vma;
            this.vmi = vmi;
            this.ntp = ntp;
            this.tab = tab;
            this.cr = cr;
            this.cx = cx;
            this.cnxa = cnxa;
            this.rate1 = rate1;
            this.rate2 = rate2;
            this.rate3 = rate3;
            this.rate4 = rate4;
            this.rate5 = rate5;
            this.rate6 = rate6;
            this.rate7 = rate7;
            this.rate8 = rate8;
            this.rate9 = rate9;
            this.rate10 = rate10;
            this.rate11 = rate11;
            this.rate12 = rate12;
            this.node = node;
        }

        public double getWindv() {
            return windv;
        }

        public double getNomv() {
            return nomv;
        }

        public double getAng() {
            return ang;
        }

        public double getRata() {
            checkVersion(WINDING_RECORD, "rata");
            return rata;
        }

        public double getRatb() {
            checkVersion(WINDING_RECORD, "ratb");
            return ratb;
        }

        public double getRatc() {
            checkVersion(WINDING_RECORD, "ratc");
            return ratc;
        }

        public int getCod() {
            return cod;
        }

        public int getCont() {
            return cont;
        }

        public double getRma() {
            return rma;
        }

        public double getRmi() {
            return rmi;
        }

        public double getVma() {
            return vma;
        }

        public double getVmi() {
            return vmi;
        }

        public int getNtp() {
            return ntp;
        }

        public int getTab() {
            return tab;
        }

        public double getCr() {
            return cr;
        }

        public double getCx() {
            return cx;
        }

        public double getCnxa() {
            return cnxa;
        }

        public double getRate1() {
            checkVersion(WINDING_RECORD, "rate1");
            return rate1;
        }

        public double getRate2() {
            checkVersion(WINDING_RECORD, "rate2");
            return rate2;
        }

        public double getRate3() {
            checkVersion(WINDING_RECORD, "rate3");
            return rate3;
        }

        public double getRate4() {
            checkVersion(WINDING_RECORD, "rate4");
            return rate4;
        }

        public double getRate5() {
            checkVersion(WINDING_RECORD, "rate5");
            return rate5;
        }

        public double getRate6() {
            checkVersion(WINDING_RECORD, "rate6");
            return rate6;
        }

        public double getRate7() {
            checkVersion(WINDING_RECORD, "rate7");
            return rate7;
        }

        public double getRate8() {
            checkVersion(WINDING_RECORD, "rate8");
            return rate8;
        }

        public double getRate9() {
            checkVersion(WINDING_RECORD, "rate9");
            return rate9;
        }

        public double getRate10() {
            checkVersion(WINDING_RECORD, "rate10");
            return rate10;
        }

        public double getRate11() {
            checkVersion(WINDING_RECORD, "rate11");
            return rate11;
        }

        public double getRate12() {
            checkVersion(WINDING_RECORD, "rate12");
            return rate12;
        }

        public int getNode() {
            checkVersion(WINDING_RECORD, "node");
            return node;
        }
    }
}
