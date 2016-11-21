/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.ReactiveCapabilityCurve;
import eu.itesla_project.iidm.network.ReactiveCapabilityCurveAdder;
import eu.itesla_project.iidm.network.impl.ReactiveCapabilityCurveImpl.PointImpl;
import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class ReactiveCapabilityCurveAdderImpl<OWNER extends ReactiveLimitsOwner & Validable> implements ReactiveCapabilityCurveAdder {

    private final OWNER owner;

    private final TreeMap<Float, ReactiveCapabilityCurve.Point> points = new TreeMap<>();

    private class PointAdderImpl implements PointAdder {

        private float p = Float.NaN;

        private float minQ = Float.NaN;

        private float maxQ = Float.NaN;

        @Override
        public PointAdder setP(float p) {
            this.p = p;
            return this;
        }

        @Override
        public PointAdder setMinQ(float minQ) {
            this.minQ = minQ;
            return this;
        }

        @Override
        public PointAdder setMaxQ(float maxQ) {
            this.maxQ = maxQ;
            return this;
        }

        @Override
        public ReactiveCapabilityCurveAdder endPoint() {
            if (Float.isNaN(p)) {
                throw new ValidationException(owner, "P is not set");
            }
            if (Float.isNaN(minQ)) {
                throw new ValidationException(owner, "min Q is not set");
            }
            if (Float.isNaN(maxQ)) {
                throw new ValidationException(owner, "max Q is not set");
            }
            if (points.containsKey(p)) {
                throw new ValidationException(owner,
                        "a point already exists for active power " + p);
            }
            points.put(p, new PointImpl(p, minQ, maxQ));
            return ReactiveCapabilityCurveAdderImpl.this;
        }

    }

    ReactiveCapabilityCurveAdderImpl(OWNER owner) {
        this.owner = owner;
    }

    @Override
    public PointAdderImpl beginPoint() {
        return new PointAdderImpl();
    }

    @Override
    public ReactiveCapabilityCurve add() {
        if (points.size() < 2) {
            throw new ValidationException(owner, "a reactive capability curve should have at least two points");
        }
        ReactiveCapabilityCurveImpl curve = new ReactiveCapabilityCurveImpl(points);
        owner.setReactiveLimits(curve);
        return curve;
    }

}
