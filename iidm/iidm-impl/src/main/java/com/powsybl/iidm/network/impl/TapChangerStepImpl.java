/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TapChangerStepImpl<S extends TapChangerStepImpl<S>> {

    private double rho;

    private double r;

    private double x;

    private double g1;

    private double b1;

    private double g2;

    private double b2;

    protected TapChangerStepImpl(double rho, double r, double x, double g1, double b1, double g2, double b2) {
        this.rho = rho;
        this.r = r;
        this.x = x;
        this.g1 = g1;
        this.b1 = b1;
        this.g2 = g2;
        this.b2 = b2;
    }

    public double getRho() {
        return rho;
    }

    public S setRho(double rho) {
        this.rho = rho;
        return (S) this;
    }

    public double getR() {
        return r;
    }

    public S setR(double r) {
        this.r = r;
        return (S) this;
    }

    public double getX() {
        return x;
    }

    public S setX(double x) {
        this.x = x;
        return (S) this;
    }

    public double getB1() {
        return b1;
    }

    public S setB1(double b1) {
        this.b1 = b1;
        return (S) this;
    }

    public double getG1() {
        return g1;
    }

    public S setG1(double g1) {
        this.g1 = g1;
        return (S) this;
    }

    public double getB2() {
        return b2;
    }

    public S setB2(double b2) {
        this.b2 = b2;
        return (S) this;
    }

    public double getG2() {
        return g2;
    }

    public S setG2(double g2) {
        this.g2 = g2;
        return (S) this;
    }
}
