/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.TieLine;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class HalfLineAdapter implements TieLine.HalfLine {

    private final DanglingLine danglingLine;
    private final MergingViewIndex index;

    private double g1;

    private double g2;

    private double b1;

    private double b2;

    public HalfLineAdapter(DanglingLine danglingLine, MergingViewIndex index) {
        this.danglingLine = Objects.requireNonNull(danglingLine);
        this.index = Objects.requireNonNull(index);
        // TODO(mathbagu): is it correct? should we initialize only b1/g1 (resp. b2/g2) like it's done in the destructive merge?
        this.g1 = danglingLine.getG() / 2.0;
        this.g2 = danglingLine.getG() / 2.0;
        this.b1 = danglingLine.getB() / 2.0;
        this.b2 = danglingLine.getB() / 2.0;
    }

    DanglingLine getDanglingLine() {
        return danglingLine;
    }

    @Override
    public String getId() {
        return danglingLine.getId();
    }

    @Override
    public String getName() {
        return danglingLine.getNameOrId();
    }

    @Override
    public double getR() {
        return danglingLine.getR();
    }

    @Override
    public TieLine.HalfLine setR(double r) {
        danglingLine.setR(r);
        return this;
    }

    @Override
    public double getX() {
        return danglingLine.getX();
    }

    @Override
    public TieLine.HalfLine setX(double x) {
        danglingLine.setX(x);
        return this;
    }

    @Override
    public double getG1() {
        return g1;
    }

    @Override
    public TieLine.HalfLine setG1(double g1) {
        this.g1 = g1;
        danglingLine.setG(g1 + g2);
        return this;
    }

    @Override
    public double getG2() {
        return g2;
    }

    @Override
    public TieLine.HalfLine setG2(double g2) {
        this.g2 = g2;
        danglingLine.setG(g1 + g2);
        return this;
    }

    public TieLine.HalfLine setG(double g) {
        this.g1 = g / 2.0;
        this.g2 = g / 2.0;
        danglingLine.setG(g);
        return this;
    }

    @Override
    public double getB1() {
        return b1;
    }

    @Override
    public TieLine.HalfLine setB1(double b1) {
        this.b1 = b1;
        danglingLine.setB(b1 + b2);
        return this;
    }

    @Override
    public double getB2() {
        return b2;
    }

    @Override
    public TieLine.HalfLine setB2(double b2) {
        this.b2 = b2;
        danglingLine.setB(b1 + b2);
        return this;
    }

    @Override
    public BoundaryAdapter getBoundary() {
        return new BoundaryAdapter(true, danglingLine.getBoundary(), index);
    }

    public TieLine.HalfLine setB(double b) {
        this.b1 = b / 2.0;
        this.b2 = b / 2.0;
        danglingLine.setB(b);
        return this;
    }
}
