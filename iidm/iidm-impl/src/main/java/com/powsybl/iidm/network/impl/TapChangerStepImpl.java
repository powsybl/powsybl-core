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

    private float rho;

    private float r;

    private float x;

    private float g;

    private float b;

    protected TapChangerStepImpl(float rho, float r, float x, float g, float b) {
        this.rho = rho;
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
    }

    public float getRho() {
        return rho;
    }

    public S setRho(float rho) {
        this.rho = rho;
        return (S) this;
    }

    public float getR() {
        return r;
    }

    public S setR(float r) {
        this.r = r;
        return (S) this;
    }

    public float getX() {
        return x;
    }

    public S setX(float x) {
        this.x = x;
        return (S) this;
    }

    public float getB() {
        return b;
    }

    public S setB(float b) {
        this.b = b;
        return (S) this;
    }

    public float getG() {
        return g;
    }

    public S setG(float g) {
        this.g = g;
        return (S) this;
    }

}
