package com.powsybl.cgmes.conversion.elements.extensions;

import com.powsybl.iidm.network.RatioTapChangerStep;

public class RatioTapChangerExtensionStep implements RatioTapChangerStep {

    private double rho;
    private double r;
    private double x;
    private double g;
    private double b;

    RatioTapChangerExtensionStep(double rho, double r, double x, double g, double b) {
        this.rho = rho;
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
    }

    @Override
    public double getRho() {
        return rho;
    }

    @Override
    public RatioTapChangerStep setRho(double rho) {
        this.rho = rho;
        return this;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public RatioTapChangerStep setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public RatioTapChangerStep setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public double getB() {
        return b;
    }

    @Override
    public RatioTapChangerStep setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public double getG() {
        return g;
    }

    @Override
    public RatioTapChangerStep setG(double g) {
        this.g = g;
        return this;
    }
}