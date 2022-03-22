/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.util.ReorientedBranchCaracteristics;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class HalfLineAdapter implements TieLine.HalfLine {

    private final MergingViewIndex index;
    private final DanglingLine danglingLine;
    private final Branch.Side side;

    private double r;

    private double x;

    private double g1;

    private double g2;

    private double b1;

    private double b2;

    public HalfLineAdapter(DanglingLine danglingLine, Branch.Side side, MergingViewIndex index) {
        this(danglingLine, side, index, false);
    }

    public HalfLineAdapter(DanglingLine danglingLine, Branch.Side side, MergingViewIndex index, boolean mustBeReoriented) {

        // if mustBeReoriented is false then end1 and end2 of the halfLine correspond to end1 and end2 of the associated danglingLine
        // if mustBeReoriented is true then end1 and end2 of the halfLine correspond to end2 and end1 of the associated danglingLine
        ReorientedBranchCaracteristics brp = new ReorientedBranchCaracteristics(danglingLine.getR(), danglingLine.getX(),
            danglingLine.getG(), danglingLine.getB(), 0.0, 0.0, mustBeReoriented);

        this.index = Objects.requireNonNull(index);
        this.danglingLine = Objects.requireNonNull(danglingLine);
        this.side = Objects.requireNonNull(side);

        this.r = brp.getR();
        this.x = brp.getX();
        this.g1 = brp.getG1();
        this.g2 = brp.getG2();
        this.b1 = brp.getB1();
        this.b2 = brp.getB2();
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
        return r;
    }

    @Override
    public TieLine.HalfLine setR(double r) {
        this.r = r;
        danglingLine.setR(r);
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public TieLine.HalfLine setX(double x) {
        this.x = x;
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
    public Boundary getBoundary() {
        return index.getBoundary(danglingLine.getBoundary(), side);
    }

    public TieLine.HalfLine setB(double b) {
        this.b1 = b / 2.0;
        this.b2 = b / 2.0;
        danglingLine.setB(b);
        return this;
    }
}
