/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Line;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LineImpl extends AbstractBranch<Line> implements Line {

    private float r;

    private float x;

    private float g1;

    private float b1;

    private float g2;

    private float b2;

    LineImpl(String id, String name, float r, float x, float g1, float b1, float g2, float b2) {
        super(id, name);
        this.r = r;
        this.x = x;
        this.g1 = g1;
        this.b1 = b1;
        this.g2 = g2;
        this.b2 = b2;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.LINE;
    }

    @Override
    public float getR() {
        return r;
    }

    @Override
    public LineImpl setR(float r) {
        ValidationUtil.checkR(this, r);
        float oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public LineImpl setX(float x) {
        ValidationUtil.checkX(this, x);
        float oldValue = this.x;
        this.x = x;
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public float getG1() {
        return g1;
    }

    @Override
    public LineImpl setG1(float g1) {
        ValidationUtil.checkG1(this, g1);
        float oldValue = this.g1;
        this.g1 = g1;
        notifyUpdate("g1", oldValue, g1);
        return this;
    }

    @Override
    public float getB1() {
        return b1;
    }

    @Override
    public LineImpl setB1(float b1) {
        ValidationUtil.checkB1(this, b1);
        float oldValue = this.b1;
        this.b1 = b1;
        notifyUpdate("b1", oldValue, b1);
        return this;
    }

    @Override
    public float getG2() {
        return g2;
    }

    @Override
    public LineImpl setG2(float g2) {
        ValidationUtil.checkG2(this, g2);
        float oldValue = this.g2;
        this.g2 = g2;
        notifyUpdate("g2", oldValue, g2);
        return this;
    }

    @Override
    public float getB2() {
        return b2;
    }

    @Override
    public LineImpl setB2(float b2) {
        ValidationUtil.checkB2(this, b2);
        float oldValue = this.b2;
        this.b2 = b2;
        notifyUpdate("b2", oldValue, b2);
        return this;
    }

    @Override
    public boolean isTieLine() {
        return false;
    }

    @Override
    protected String getTypeDescription() {
        return "AC line";
    }

}
