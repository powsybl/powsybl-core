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
import com.powsybl.iidm.network.util.ReactiveCapabilityCurveUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    /**
     * <p>Comparator to handle the -0.0 == 0.0 case:</p>
     * <p>According to the JLS: "Positive zero and negative zero compare equal, so the result of the expression 0.0==-0.0 is true".
     * But the {@link Double#compareTo(Double)} method consider -0.0 lower than 0.0. Therefore, using the default
     * Double comparator causes a problem when the lower point's <code>p</code> is equal to 0.0 and the tested <code>p</code>
     * is -0.0.
     * </p>
     * <p>This comparator considers 0.0 and -0.0 as equal.</p>
     * <p>Note: it throws a {@link NullPointerException} when one of the Doubles are null,
     * similarly as the default Double comparator. But in our use case, this cannot happen.</p>
     */
    private static final Comparator<Double> COMPARATOR = (d1, d2) -> d1 - d2 == 0 ? 0 : Double.compare(d1, d2);

    private final String ownerDescription;

    static class PointImpl implements Point {

        private double p;

        private double minQ;

        private double maxQ;

        PointImpl(double p, double minQ, double maxQ) {
            this(p, minQ, maxQ, true);
        }

        PointImpl(double p, double minQ, double maxQ, boolean shouldCheckMinMaxValues) {
            if (shouldCheckMinMaxValues && minQ > maxQ) {
                throw new IllegalStateException("maximum reactive power is expected to be greater than or equal to minimum reactive power");
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

    ReactiveCapabilityCurveImpl(TreeMap<Double, Point> points, String ownerDescription) {
        checkPointsSize(points);
        this.points = new TreeMap<>(COMPARATOR);
        this.points.putAll(points);
        this.ownerDescription = ownerDescription;
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
        return getReactiveLimit(p, extrapolateReactiveLimitSlope, Point::getMinQ);
    }

    @Override
    public double getMaxQ(double p, boolean extrapolateReactiveLimitSlope) {
        return getReactiveLimit(p, extrapolateReactiveLimitSlope, Point::getMaxQ);
    }

    private double getReactiveLimit(double p, boolean extrapolateReactiveLimitSlope, ToDoubleFunction<Point> getMinOrMaxQ) {
        checkPointsSize(points);

        // First case : searched point is one of the points defining the curve
        Point pt = points.get(p);
        if (pt != null) {
            return getMinOrMaxQ.applyAsDouble(pt);
        }

        // Second case : searched point is between minP and maxP
        if (p >= this.getMinP() && p <= this.getMaxP()) {
            Point p1 = points.floorEntry(p).getValue();
            Point p2 = points.ceilingEntry(p).getValue();
            return getMinOrMaxQ.applyAsDouble(p1) + (getMinOrMaxQ.applyAsDouble(p2) - getMinOrMaxQ.applyAsDouble(p1)) / (p2.getP() - p1.getP()) * (p - p1.getP());
        }

        // Third case : searched point is outside minP and maxP
        if (extrapolateReactiveLimitSlope) {
            Point extrapolatedPoint = ReactiveCapabilityCurveUtil.extrapolateReactiveLimitsSlope(p, points, PointImpl::new, ownerDescription);
            return getMinOrMaxQ.applyAsDouble(extrapolatedPoint);
        } else {
            if (p < this.getMinP()) { // p < minP
                Point pMin = points.firstEntry().getValue();
                return getMinOrMaxQ.applyAsDouble(pMin);
            } else { // p > maxP
                Point pMax = points.lastEntry().getValue();
                return getMinOrMaxQ.applyAsDouble(pMax);
            }
        }
    }

}
