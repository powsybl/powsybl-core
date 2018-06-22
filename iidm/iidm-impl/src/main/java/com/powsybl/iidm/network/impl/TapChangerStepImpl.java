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

    private double g;

    private double b;

    protected TapChangerStepImpl(double rho, double r, double x, double g, double b) {
        this.rho = rho;
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
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

    public double getB() {
        return b;
    }

    public S setB(double b) {
        this.b = b;
        return (S) this;
    }

    public double getG() {
        return g;
    }

    public S setG(double g) {
        this.g = g;
        return (S) this;
    }

}
