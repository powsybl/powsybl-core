/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineSegment;
import com.powsybl.iidm.network.extensions.MutualCoupling;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class MutualCouplingImpl implements MutualCoupling {

    private final Line line1;
    private final Line line2;

    private double r;
    private double x;

    private LineSegment line1Segment;
    private LineSegment line2Segment;

    MutualCouplingImpl(Line line1, Line line2, double r, double x, LineSegment line1Segment, LineSegment line2Segment) {
        this.line1 = Objects.requireNonNull(line1);
        this.line2 = Objects.requireNonNull(line2);
        this.r = r;
        this.x = x;
        this.line1Segment = Objects.requireNonNull(line1Segment);
        this.line2Segment = Objects.requireNonNull(line2Segment);
    }

    @Override
    public Line getLine1() {
        return line1;
    }

    @Override
    public Line getLine2() {
        return line2;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public void setR(double r) {
        this.r = r;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public LineSegment getLine1Segment() {
        return line1Segment;
    }

    @Override
    public LineSegment getLine2Segment() {
        return line2Segment;
    }

    @Override
    public void setLine1Segment(LineSegment lineSegment) {
        this.line1Segment = Objects.requireNonNull(lineSegment);
    }

    @Override
    public void setLine2Segment(LineSegment lineSegment) {
        this.line2Segment = Objects.requireNonNull(lineSegment);
    }
}
