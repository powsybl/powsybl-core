/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.MutualCoupling;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class MutualCouplingImpl implements MutualCoupling {

    private Line line1;
    private Line line2;

    private double r;
    private double x;

    private double line1Start;
    private double line2Start;
    private double line1End;
    private double line2End;

    MutualCouplingImpl(Line line1, Line line2, double r, double x, double line1Start, double line1End, double line2Start, double line2End) {
        this.line1 = Objects.requireNonNull(line1);
        this.line2 = Objects.requireNonNull(line2);
        this.r = r;
        this.x = x;
        this.line1Start = line1Start;
        this.line1End = line1End;
        this.line2Start = line2Start;
        this.line2End = line2End;
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
    public double getLine1Start() {
        return line1Start;
    }

    @Override
    public double getLine2Start() {
        return line2Start;
    }

    @Override
    public double getLine1End() {
        return line1End;
    }

    @Override
    public double getLine2End() {
        return line2End;
    }

    @Override
    public void setLine1Position(double line1Start, double line1End) {
        if (!isValidPosition(line1Start, line1End)) {
            throw new PowsyblException("Invalid line1 positions: start=" + line1Start + ", end=" + line1End);
        }
        this.line1Start = line1Start;
        this.line1End = line1End;
    }

    @Override
    public void setLine2Position(double line2Start, double line2End) {
        if (!isValidPosition(line2Start, line2End)) {
            throw new PowsyblException("Invalid line2 positions: start=" + line2Start + ", end=" + line2End);
        }
        this.line2Start = line2Start;
        this.line2End = line2End;
    }

    private boolean isValidPosition(double start, double end) {
        return start >= 0 && start <= end && end <= 1;
    }
}
