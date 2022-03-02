package com.powsybl.shortcircuit;

public class ShortCircuitBusResult {
    double icc;
    double pcc;
    double cte;
    double rd;
    double xd;
    double currentModule;
    double currentPhase;
    double voltageModule;
    double voltagePhase;
    double i1mod;
    double i2mod;
    double i3mod;
    double i1phase;
    double i2phase;
    double i3phase;
    double v1mod;
    double v2mod;
    double v3mod;
    double v1phase;
    double v2phase;
    double v3phase;

    public double getIcc() {
        return icc;
    }

    public void setIcc(double icc) {
        this.icc = icc;
    }

    public double getPcc() {
        return pcc;
    }

    public void setPcc(double pcc) {
        this.pcc = pcc;
    }

    public double getCte() {
        return cte;
    }

    public void setCte(double cte) {
        this.cte = cte;
    }
}
