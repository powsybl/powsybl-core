/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    public static class PointImpl implements Point {

        private double p;

        private double minQ;

        private double maxQ;

        public PointImpl(double p, double minQ, double maxQ) {
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

        @Override
        public int hashCode() {
            return Objects.hash(p, minQ, maxQ);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PointImpl) {
                PointImpl other = (PointImpl) obj;
                return p == other.p && minQ == other.minQ && maxQ == other.maxQ;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Point(p=" + p + ", minQ=" + minQ + ", maxQ=" + maxQ + ")";
        }
    }

    private final NavigableMap<Double, Point> points;

    ReactiveCapabilityCurveImpl(NavigableMap<Double, Point> points) {
        this.points = Objects.requireNonNull(points);
        if (points.size() < 2) {
            throw new IllegalArgumentException("A reactive capability curve is expected to have at least 2 points");
        }
    }

    public static ReactiveCapabilityCurveImpl of(PointImpl... points) {
        Objects.requireNonNull(points);
        TreeMap<Double, Point> pointMap = Arrays.stream(points)
                .collect(Collectors.toMap(PointImpl::getP,
                    Function.identity(),
                    (point, point2) -> {
                        throw new IllegalArgumentException("Duplicate point at p=" + point.getP());
                    },
                    TreeMap::new));
        return new ReactiveCapabilityCurveImpl(pointMap);
    }

    private static void mergePoint(TreeMap<Double, Point> pointMap, Point newPoint) {
        Point oldPoint = pointMap.get(newPoint.getP());
        if (oldPoint == null) {
            pointMap.put(newPoint.getP(), newPoint);
        } else {
            pointMap.put(newPoint.getP(), new PointImpl(newPoint.getP(),
                                                        Math.min(newPoint.getMinQ(), oldPoint.getMinQ()),
                                                        Math.max(newPoint.getMaxQ(), oldPoint.getMaxQ())));
        }
    }

    /**
     *   g1  g2
     *   |   |
     *   --+--
     *     n1
     *
     * Given 2 generators with a reactive capability curve connected to same bus n1, this method compute the equivalent
     * reactive capability curve at bus n1 so that limit_n1(p1 + p2) = limit_g1(p1) + limit_g2(p2) with p1 the active
     * power of g1 and p2 the active power of g2.
     *
     * @param curve1 first reactive capability curve
     * @param curve2 second  reactive capability curve
     * @return reactive capability curve addition of {@code curve1} and {@code curve2}
     */
    public static ReactiveCapabilityCurveImpl add(ReactiveCapabilityCurve curve1, ReactiveCapabilityCurve curve2) {
        Objects.requireNonNull(curve1);
        Objects.requireNonNull(curve2);
        TreeMap<Double, Point> pointMap = new TreeMap<>();
        for (Point point1 : curve1.getPoints()) {
            mergePoint(pointMap, point1);
            // add all combination of curve1 and curve2, merging with already added point at same active power.
            for (Point point2 : curve2.getPoints()) {
                mergePoint(pointMap, point2);
                double p = point1.getP() + point2.getP();
                double minQ = point1.getMinQ() + point2.getMinQ();
                double maxQ = point1.getMaxQ() + point2.getMaxQ();
                mergePoint(pointMap, new PointImpl(p, minQ, maxQ));
            }
        }
        return new ReactiveCapabilityCurveImpl(pointMap);
    }

    /**
     * Similar as {@link #add(ReactiveCapabilityCurve, ReactiveCapabilityCurve)} but between a reactive capability curve
     * and a min max reactive power limit.
     *
     * @param curve reactive capability curve
     * @param minMaxLimits min max reactive limit
     * @return reactive capability curve addition of {@code curve} and {@code minMaxLimits}
     */
    public static ReactiveCapabilityCurveImpl add(ReactiveCapabilityCurve curve, MinMaxReactiveLimits minMaxLimits) {
        Objects.requireNonNull(curve);
        Objects.requireNonNull(minMaxLimits);
        TreeMap<Double, Point> pointMap = new TreeMap<>();
        for (Point point : curve.getPoints()) {
            mergePoint(pointMap, point);
            mergePoint(pointMap, new PointImpl(point.getP(), minMaxLimits.getMinQ(), minMaxLimits.getMaxQ()));
        }
        return new ReactiveCapabilityCurveImpl(pointMap);
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

    private double getLimit(double p, ToDoubleFunction<Point> limitFunction) {
        Objects.requireNonNull(limitFunction);
        Point pt = points.get(p);
        if  (pt != null) {
            return limitFunction.applyAsDouble(pt);
        } else {
            Map.Entry<Double, Point> e1 = points.floorEntry(p);
            Map.Entry<Double, Point> e2 = points.ceilingEntry(p);
            if (e1 == null && e2 != null) {
                return limitFunction.applyAsDouble(e2.getValue());
            } else if (e1 != null && e2 == null) {
                return limitFunction.applyAsDouble(e1.getValue());
            } else if (e1 != null) {
                Point p1 = e1.getValue();
                Point p2 = e2.getValue();
                double minQ1 = limitFunction.applyAsDouble(p1);
                double minQ2 = limitFunction.applyAsDouble(p2);
                return minQ1 + (minQ2 - minQ1) / (p2.getP() - p1.getP()) * (p - p1.getP());
            } else {
                throw new IllegalStateException("At least one point should be found");
            }
        }
    }

    @Override
    public double getMinQ(double p) {
        return getLimit(p, Point::getMinQ);
    }

    @Override
    public double getMaxQ(double p) {
        return getLimit(p, Point::getMaxQ);
    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReactiveCapabilityCurveImpl) {
            ReactiveCapabilityCurveImpl other = (ReactiveCapabilityCurveImpl) obj;
            return points.equals(other.points);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ReactiveCapabilityCurve(points=" + points.values() + ")";
    }
}
