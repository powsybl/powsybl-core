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
import com.powsybl.iidm.network.extensions.LineCouplings;
import com.powsybl.iidm.network.extensions.LineSegment;
import com.powsybl.iidm.network.extensions.MutualCoupling;
import com.powsybl.iidm.network.extensions.MutualCouplingAdder;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class MutualCouplingAdderImpl implements MutualCouplingAdder {

    LineCouplings mutualCouplings;

    private Line line1;
    private Line line2;

    private double r;
    private double x;

    private LineSegment line1Segment = new LineSegment(0, 1);
    private LineSegment line2Segment = new LineSegment(0, 1);

    MutualCouplingAdderImpl(LineCouplings mutualCouplings) {
        this.mutualCouplings = Objects.requireNonNull(mutualCouplings);
    }

    @Override
    public MutualCouplingAdder withLine1(Line line1) {
        this.line1 = line1;
        return this;
    }

    @Override
    public MutualCouplingAdder withLine2(Line line2) {
        this.line2 = line2;
        return this;
    }

    @Override
    public MutualCouplingAdder withR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public MutualCouplingAdder withX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public MutualCouplingAdder withLine1Segment(LineSegment segment) {
        this.line1Segment = Objects.requireNonNull(segment);
        return this;
    }

    @Override
    public MutualCouplingAdder withLine2Segment(LineSegment segment) {
        this.line2Segment = Objects.requireNonNull(segment);
        return this;
    }

    @Override
    public MutualCoupling add() {
        validate();
        MutualCouplingImpl mutualCoupling = new MutualCouplingImpl(line1, line2, r, x, line1Segment, line2Segment);
        mutualCouplings.add(mutualCoupling);
        return mutualCoupling;
    }

    private void validate() {
        if (line1 == null || line2 == null) {
            throw new PowsyblException("Lines cannot be null.");
        }
        if (line1.equals(line2)) {
            throw new PowsyblException("Lines must be different.");
        }
    }
}
