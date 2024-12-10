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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    static class PointImpl implements Point {

        private double p;

        private double minQ;

        private double maxQ;

        PointImpl(double p, double minQ, double maxQ) {
            if (minQ > maxQ) {
                throw new IllegalArgumentException("minQ should be lower than maxQ");
            }
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

    private Point extrapolateReactiveLimitsSlope(double p) {
        Map.Entry<Double, Point> e1 = points.floorEntry(p);
        Map.Entry<Double, Point> e2 = points.ceilingEntry(p);
        double minQ;
        double maxQ;
        if (e1 == null && e2 != null) {
            // Extrapolate reactive limits slope below min active power limit p2
            Point p2 = e2.getValue();
            Point p2bis = points.higherEntry(e2.getKey()).getValue(); // p < p2 < p2bis
            double slopeMinQ = (p2bis.getMinQ() - p2.getMinQ()) / (p2bis.getP() - p2.getP());
            double slopeMaxQ = (p2bis.getMaxQ() - p2.getMaxQ()) / (p2bis.getP() - p2.getP());
            minQ = p2.getMinQ() + slopeMinQ * (p - p2.getP());
            maxQ = p2.getMaxQ() + slopeMaxQ * (p - p2.getP());
        } else if (e1 != null && e2 == null) {
            // Extrapolate reactive limits slope above max active power limit p1
            Point p1 = e1.getValue();
            Point p1bis = points.lowerEntry(e1.getKey()).getValue(); // p1bis < p1 < p
            double slopeMinQ = (p1.getMinQ() - p1bis.getMinQ()) / (p1.getP() - p1bis.getP());
            double slopeMaxQ = (p1.getMaxQ() - p1bis.getMaxQ()) / (p1.getP() - p1bis.getP());
            minQ = p1.getMinQ() + slopeMinQ * (p - p1.getP());
            maxQ = p1.getMaxQ() + slopeMaxQ * (p - p1.getP());
        } else {
            throw new IllegalStateException();
        }
        if (minQ <= maxQ) {
            return new PointImpl(p, minQ, maxQ);
        } else { // Corner case of intersecting reactive limits when extrapolated
            double limitQ = (minQ + maxQ) / 2;
            return new PointImpl(p, limitQ, limitQ); // Returning the intersection as limits
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
        checkPointsSize(points);
        Point pt = points.get(p);
        if (pt != null) {
            return pt.getMinQ();
        } else {
            Map.Entry<Double, Point> e1 = points.floorEntry(p);
            Map.Entry<Double, Point> e2 = points.ceilingEntry(p);
            if (e1 == null && e2 != null) {
                return e2.getValue().getMinQ();
            } else if (e1 != null && e2 == null) {
                return e1.getValue().getMinQ();
            } else if (e1 != null && e2 != null) {
                Point p1 = e1.getValue();
                Point p2 = e2.getValue();
                return p1.getMinQ() + (p2.getMinQ() - p1.getMinQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public double getMaxQ(double p) {
        checkPointsSize(points);
        Point pt = points.get(p);
        if (pt != null) {
            return pt.getMaxQ();
        } else {
            Map.Entry<Double, Point> e1 = points.floorEntry(p);
            Map.Entry<Double, Point> e2 = points.ceilingEntry(p);
            if (e1 == null && e2 != null) {
                return e2.getValue().getMaxQ();
            } else if (e1 != null && e2 == null) {
                return e1.getValue().getMaxQ();
            } else if (e1 != null && e2 != null) {
                Point p1 = e1.getValue();
                Point p2 = e2.getValue();
                return p1.getMaxQ() + (p2.getMaxQ() - p1.getMaxQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public double getMinQ(double p, boolean extrapolateReactiveLimitSlope) {
        checkPointsSize(points);
        if (!extrapolateReactiveLimitSlope || p >= points.firstKey() && p <= points.lastKey()) {
            return getMinQ(p);
        }
        return extrapolateReactiveLimitsSlope(p).getMinQ();
    }

    @Override
    public double getMaxQ(double p, boolean extrapolateReactiveLimitSlope) {
        checkPointsSize(points);
        if (!extrapolateReactiveLimitSlope || p >= points.firstKey() && p <= points.lastKey()) {
            return getMaxQ(p);
        }
        return extrapolateReactiveLimitsSlope(p).getMaxQ();
    }
}
