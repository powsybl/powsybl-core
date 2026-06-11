/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.DroopCurveImpl.SegmentImpl;

import java.util.*;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DroopCurveAdderImpl<O extends Validable & AcDcConverter<?>> implements DroopCurveAdder {

    private final O owner;

    private final List<DroopCurve.Segment> segments = new ArrayList<>() {
    };

    private final class SegmentAdderImpl implements SegmentAdder {

        private double k = Double.NaN;

        private double minV = Double.NaN;

        private double maxV = Double.NaN;

        @Override
        public SegmentAdder setK(double k) {
            this.k = k;
            return this;
        }

        @Override
        public SegmentAdder setMinV(double minV) {
            this.minV = minV;
            return this;
        }

        @Override
        public SegmentAdder setMaxV(double maxV) {
            this.maxV = maxV;
            return this;
        }

        @Override
        public DroopCurveAdder endSegment() {
            if (Double.isNaN(k)) {
                throw new ValidationException(owner, "k is not set");
            }
            if (Double.isNaN(minV)) {
                throw new ValidationException(owner, "min V is not set");
            }
            if (Double.isNaN(maxV)) {
                throw new ValidationException(owner, "max V is not set");
            }
            segments.add(new SegmentImpl(k, minV, maxV));
            return DroopCurveAdderImpl.this;
        }

    }

    DroopCurveAdderImpl(O owner) {
        this.owner = owner;
    }

    @Override
    public SegmentAdder beginSegment() {
        return new SegmentAdderImpl();
    }

    @Override
    public DroopCurve add() {
        segments.sort(Comparator.comparingDouble(DroopCurve.Segment::getMinV));
        for (int i = 0; i < segments.size() - 1; i++) {

            DroopCurve.Segment curr = segments.get(i);
            DroopCurve.Segment next = segments.get(i + 1);

            if (curr.getMaxV() > next.getMinV()) {
                throw new ValidationException(owner, "Droop segments are overlapping");
            }

            if (curr.getMaxV() < next.getMinV()) {
                throw new ValidationException(owner, "Droop curve is not continuous");
            }
        }
        DroopCurveImpl curve = new DroopCurveImpl(segments);
        ((AbstractAcDcConverter<?>) owner).setDroopCurve(curve);
        return curve;
    }
}
