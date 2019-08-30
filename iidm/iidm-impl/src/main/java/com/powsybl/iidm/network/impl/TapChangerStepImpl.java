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

    private AbstractTapChanger<?, ?, ?> parent;

    private int position;

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

    protected void setPosition(int position) {
        this.position = position;
    }

    protected void setParent(AbstractTapChanger<?, ?, ?> parent) {
        this.parent = parent;
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        this.parent.getNetwork().getListeners().notifyUpdate(this.parent.parent.getTransformer(), () -> this.parent.getTapChangerAttribute() + ".step[" + position + "]." + attribute, oldValue, newValue);
    }

    public double getRho() {
        return rho;
    }

    public S setRho(double rho) {
        double oldValue = this.rho;
        this.rho = rho;
        notifyUpdate("rho", oldValue, rho);
        return (S) this;
    }

    public double getR() {
        return r;
    }

    public S setR(double r) {
        double oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return (S) this;
    }

    public double getX() {
        return x;
    }

    public S setX(double x) {
        double oldValue = this.x;
        this.x = x;
        notifyUpdate("x", oldValue, x);
        return (S) this;
    }

    public double getB() {
        return b;
    }

    public S setB(double b) {
        double oldValue = this.b;
        this.b = b;
        notifyUpdate("b", oldValue, b);
        return (S) this;
    }

    public double getG() {
        return g;
    }

    public S setG(double g) {
        double oldValue = this.g;
        this.g = g;
        notifyUpdate("g", oldValue, g);
        return (S) this;
    }

}
