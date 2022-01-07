/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

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

    ReactiveCapabilityCurveImpl(TreeMap<Double, Point> points) {
        assert points.size() >= 2;
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
        assert points.size() >= 2;

        Point pt = points.get(p);
        if  (pt != null) {
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
                throw new AssertionError();
            }
        }
    }

    @Override
    public double getMaxQ(double p) {
        assert points.size() >= 2;

        Point pt = points.get(p);
        if  (pt != null) {
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
                throw new AssertionError();
            }
        }
    }
}
