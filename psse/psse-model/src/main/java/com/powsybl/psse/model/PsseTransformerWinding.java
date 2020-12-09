package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.univocity.parsers.annotations.Parsed;

@JsonFilter("PsseVersionFilter")
public class PsseTransformerWinding extends Versioned {
    @Parsed
    private double windv = Double.NaN;

    @Parsed
    private double nomv = 0;

    @Parsed
    private double ang = 0;

    @Parsed
    @PsseRev(until = 33)
    private double rata = 0;

    @Parsed
    @PsseRev(until = 33)
    private double ratb = 0;

    @Parsed
    @PsseRev(until = 33)
    private double ratc =  0;

    @Parsed
    @PsseRev(since = 35)
    private double rate1 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate2 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate3 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate4 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate5 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate6 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate7 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate8 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate9 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate10 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate11 = 0;

    @Parsed
    @PsseRev(since = 35)
    private double rate12 = 0;

    @Parsed
    private int cod = 0;

    @Parsed
    private int cont = 0;

    @Parsed
    @PsseRev(since = 35)
    private int node = 0;

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

    public double getNomv() {
        return nomv;
    }

    public double getAng() {
        return ang;
    }

    public double getRata() {
        checkVersion("rata");
        return rata;
    }

    public double getRatb() {
        checkVersion("ratb");
        return ratb;
    }

    public double getRatc() {
        checkVersion("ratc");
        return ratc;
    }

    public double getRate1() {
        checkVersion("rate1");
        return rate1;
    }

    public double getRate2() {
        checkVersion("rate2");
        return rate2;
    }

    public double getRate3() {
        checkVersion("rate3");
        return rate3;
    }

    public double getRate4() {
        checkVersion("rate4");
        return rate4;
    }

    public double getRate5() {
        checkVersion("rate5");
        return rate5;
    }

    public double getRate6() {
        checkVersion("rate6");
        return rate6;
    }

    public double getRate7() {
        checkVersion("rate7");
        return rate7;
    }

    public double getRate8() {
        checkVersion("rate8");
        return rate8;
    }

    public double getRate9() {
        checkVersion("rate9");
        return rate9;
    }

    public double getRate10() {
        checkVersion("rate10");
        return rate10;
    }

    public double getRate11() {
        checkVersion("rate11");
        return rate11;
    }

    public double getRate12() {
        checkVersion("rate12");
        return rate12;
    }

    public int getCod() {
        return cod;
    }

    public int getCont() {
        return cont;
    }

    public int getNode() {
        checkVersion("node");
        return node;
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
}
