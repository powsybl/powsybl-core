/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DroopCurve;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DroopCurveImpl implements DroopCurve {

    static class SegmentImpl implements Segment {

        private final double k;

        private final double minV;

        private final double maxV;

        SegmentImpl(double k, double minV, double maxV) {
            this.k = k;
            this.minV = minV;
            this.maxV = maxV;
        }

        @Override
        public double getK() {
            return k;
        }

        @Override
        public double getMinV() {
            return minV;
        }

        @Override
        public double getMaxV() {
            return maxV;
        }

    }

    private final List<Segment> segments;

    DroopCurveImpl(List<Segment> segments) {
        this.segments = segments;
    }

    @Override
    public Collection<Segment> getSegments() {
        return Collections.unmodifiableCollection(segments);
    }

    @Override
    public double getK(double v) {
        //segments list is already sorted in the DroopCurveAdder
        if (segments.isEmpty()) {
            return 0.0;
        }
        if (v <= segments.getFirst().getMinV()) {
            return segments.getFirst().getK();
        }
        for (Segment segment : segments) {
            if (v >= segment.getMinV() && v < segment.getMaxV()) {
                return segment.getK();
            }
        }
        return segments.getLast().getK();
    }
}
