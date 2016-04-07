/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.ReactiveCapabilityCurve;
import eu.itesla_project.iidm.network.ReactiveLimitsKind;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCapabilityCurveImpl.class);

    static class PointImpl implements Point {

        private float p;

        private float minQ;

        private float maxQ;

        PointImpl(float p, float minQ, float maxQ) {
            this.p = p;
            this.minQ = minQ;
            this.maxQ = maxQ;
        }

        @Override
        public float getP() {
            return p;
        }

        @Override
        public float getMinQ() {
            return minQ;
        }

        @Override
        public float getMaxQ() {
            return maxQ;
        }

    }

    private final GeneratorImpl generator;

    private final TreeMap<Float, Point> points;

    ReactiveCapabilityCurveImpl(GeneratorImpl generator, TreeMap<Float, Point> points) {
        assert points.size() >= 2;
        this.generator = generator;
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
    public float getMinP() {
        return points.firstKey();
    }

    @Override
    public float getMaxP() {
        return points.lastKey();
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.CURVE;
    }

    @Override
    public float getMinQ(float p) {
        assert points.size() >= 2;

//        if (p < generator.getMinP() || p > generator.getMaxP()) {
//            LOGGER.warn("Generator {}: active power {} out of range [{}, {}]",
//                    generator.getId(), p, generator.getMinP(), generator.getMaxP());
//        }

        Point pt = points.get(p);
        if  (pt != null) {
            return pt.getMinQ();
        } else {
            Map.Entry<Float, Point> e1 = points.floorEntry(p);
            Map.Entry<Float, Point> e2 = points.ceilingEntry(p);
            if (e1 == null && e2 != null) {
                return e2.getValue().getMinQ();
            } else if (e1 != null && e2 == null) {
                return e1.getValue().getMinQ();
            } else if (e1 != null && e2 != null) {
                Point p1 = e1.getValue();
                Point p2 = e2.getValue();
                return p1.getMinQ() + (p2.getMinQ() - p1.getMinQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
            } else {
                throw new InternalError();
            }
        }
    }

    @Override
    public float getMaxQ(float p) {
        assert points.size() >= 2;

//        if (p < generator.getMinP() || p > generator.getMaxP()) {
//            LOGGER.warn("Generator {}: active power {} out of range [{}, {}]",
//                    generator.getId(), p, generator.getMinP(), generator.getMaxP());
//        }

        Point pt = points.get(p);
        if  (pt != null) {
            return pt.getMaxQ();
        } else {
            Map.Entry<Float, Point> e1 = points.floorEntry(p);
            Map.Entry<Float, Point> e2 = points.ceilingEntry(p);
            if (e1 == null && e2 != null) {
                return e2.getValue().getMaxQ();
            } else if (e1 != null && e2 == null) {
                return e1.getValue().getMaxQ();
            } else if (e1 != null && e2 != null) {
                Point p1 = e1.getValue();
                Point p2 = e2.getValue();
                return p1.getMaxQ() + (p2.getMaxQ() - p1.getMaxQ()) / (p2.getP() - p1.getP()) * (p - p1.getP());
            } else {
                throw new InternalError();
            }
        }
    }
}
