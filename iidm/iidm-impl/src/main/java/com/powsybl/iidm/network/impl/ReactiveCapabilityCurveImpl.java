/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCapabilityCurveImpl.class);
    private int warning_count = 0;

    static class PointImpl implements Point {

        private double p;

        private double minQ;

        private double maxQ;

        PointImpl(double p, double minQ, double maxQ) {
            this.p = p;
            this.minQ = minQ;
            this.maxQ = maxQ;
        }

        @Override
        public double getP() {
            return p;
        }

        @Override
        public double getMinQ() {
            return minQ;
        }

        @Override
        public double getMaxQ() {
            return maxQ;
        }

    }

    private final TreeMap<Double, Point> points;

    private static void checkPointsSize(TreeMap<Double, Point> points) {
        if (points.size() < 2) {
            throw new IllegalStateException("points size should be >= 2");
        }
    }

    private Point extrapolateReactiveLimitsSlope(double p, Point pBound) {
        double minQ;
        double maxQ;
        Point pbis;
        if (p < pBound.getP()) {
            // Extrapolate reactive limits slope below min active power limit (bound_p = min active power limit)
            pbis = points.higherEntry(points.firstKey()).getValue(); // p < bound_p < pbis
        } else if (p > pBound.getP()) {
            // Extrapolate reactive limits slope above max active power limit (bound_p = max active power limit)
            pbis = points.lowerEntry(points.lastKey()).getValue(); // pbis < bound_p < p
        } else {
            throw new IllegalStateException();
        }
        double slopeMinQ = (pbis.getMinQ() - pBound.getMinQ()) / (pbis.getP() - pBound.getP());
        double slopeMaxQ = (pbis.getMaxQ() - pBound.getMaxQ()) / (pbis.getP() - pBound.getP());
        minQ = pBound.getMinQ() + slopeMinQ * (p - pBound.getP());
        maxQ = pBound.getMaxQ() + slopeMaxQ * (p - pBound.getP());
        if (minQ <= maxQ) {
            return new PointImpl(p, minQ, maxQ);
        } else { // Corner case of intersecting reactive limits when extrapolated
            double limitQ = (minQ + maxQ) / 2;
            if (warning_count++ % 100 == 0) { // Warn message only every 100 calls to avoid logging overflow
                LOGGER.warn("PQ diagram extrapolation leads to minQ > maxQ ({} > {}) for P = {} => changing to minQ = maxQ = {}", minQ, maxQ, p, limitQ);
            }
            return new PointImpl(p, limitQ, limitQ); // Returning the mean as limits minQ and maxQ
        }
    }

    ReactiveCapabilityCurveImpl(TreeMap<Double, Point> points) {
        checkPointsSize(points);
        this.points = points;
    }

    @Override
    public Collection<Point> getPoints() {
        return Collections.unmodifiableCollection(points.values());
    }

    @Override
    public int getPointCount() {
        return points.size();
    }

    @Override
    public double getMinP() {
        return points.firstKey();
    }

    @Override
    public double getMaxP() {
        return points.lastKey();
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.CURVE;
    }

    @Override
    public double getMinQ(double p) {
        return getMinQ(p, false);
    }

    @Override
    public double getMaxQ(double p) {
        return getMaxQ(p, false);
    }

    @Override
    public double getMinQ(double p, boolean extrapolateReactiveLimitSlope) {
        checkPointsSize(points);

        Point pt = points.get(p);
        if (pt != null) {
            return pt.getMinQ();
        }

        Map.Entry<Double, Point> e1 = points.floorEntry(p);
        Map.Entry<Double, Point> e2 = points.ceilingEntry(p);
        if (e1 == null && e2 != null) {
            Point pMin = e2.getValue();
            return extrapolateReactiveLimitSlope ? extrapolateReactiveLimitsSlope(p, pMin).getMinQ() : pMin.getMinQ();
        } else if (e1 != null && e2 == null) {
            Point pMax = e1.getValue();
            return extrapolateReactiveLimitSlope ? extrapolateReactiveLimitsSlope(p, pMax).getMinQ() : pMax.getMinQ();
        } else if (e1 != null && e2 != null) {
            Point p1 = e1.getValue();
            Point p2 = e2.getValue();
            return p1.getMinQ() + (p2.getMinQ() - p1.getMinQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public double getMaxQ(double p, boolean extrapolateReactiveLimitSlope) {
        checkPointsSize(points);

        Point pt = points.get(p);
        if (pt != null) {
            return pt.getMaxQ();
        }

        Map.Entry<Double, Point> e1 = points.floorEntry(p);
        Map.Entry<Double, Point> e2 = points.ceilingEntry(p);
        if (e1 == null && e2 != null) {
            Point pMin = e2.getValue();
            return extrapolateReactiveLimitSlope ? extrapolateReactiveLimitsSlope(p, pMin).getMaxQ() : pMin.getMaxQ();
        } else if (e1 != null && e2 == null) {
            Point pMax = e1.getValue();
            return extrapolateReactiveLimitSlope ? extrapolateReactiveLimitsSlope(p, pMax).getMaxQ() : pMax.getMaxQ();
        } else if (e1 != null && e2 != null) {
            Point p1 = e1.getValue();
            Point p2 = e2.getValue();
            return p1.getMaxQ() + (p2.getMaxQ() - p1.getMaxQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
        } else {
            throw new IllegalStateException();
        }
    }
}
