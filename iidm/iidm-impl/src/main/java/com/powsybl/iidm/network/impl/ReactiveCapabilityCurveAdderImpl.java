/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.impl.ReactiveCapabilityCurveImpl.PointImpl;
import com.powsybl.iidm.network.util.NetworkReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ReactiveCapabilityCurveAdderImpl<O extends ReactiveLimitsOwner & Validable> implements ReactiveCapabilityCurveAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCapabilityCurveAdderImpl.class);

    private final O owner;

    private final TreeMap<Double, ReactiveCapabilityCurve.Point> points = new TreeMap<>();

    private boolean shouldCheckMinMaxValues = true;

    private final class PointAdderImpl implements PointAdder {

        private double p = Double.NaN;

        private double minQ = Double.NaN;

        private double maxQ = Double.NaN;

        @Override
        public PointAdder setP(double p) {
            this.p = p;
            return this;
        }

        @Override
        public PointAdder setMinQ(double minQ) {
            this.minQ = minQ;
            return this;
        }

        @Override
        public PointAdder setMaxQ(double maxQ) {
            this.maxQ = maxQ;
            return this;
        }

        @Override
        public ReactiveCapabilityCurveAdder endPoint() {
            if (Double.isNaN(p)) {
                throw new ValidationException(owner, "P is not set");
            }
            if (Double.isNaN(minQ)) {
                throw new ValidationException(owner, "min Q is not set");
            }
            if (Double.isNaN(maxQ)) {
                throw new ValidationException(owner, "max Q is not set");
            }
            ReactiveCapabilityCurve.Point point = points.get(p);
            if (point != null) {
                if (point.getMinQ() != minQ || point.getMaxQ() != maxQ) {
                    throw new ValidationException(owner,
                            "a point already exists for active power " + p + " with a different reactive power range: [" +
                            minQ + ", " + maxQ + "] != " + "[" + point.getMinQ() + ", " + point.getMaxQ() + "]");
                } else {
                    LOGGER.warn("{}duplicate point for active power {}", owner.getMessageHeader(), p);
                    NetworkReports.parentHasDuplicatePointForActivePower(owner.getNetwork().getReportNodeContext().getReportNode(), owner.getMessageHeader(), p);
                }
            }

            // Check the min/max Q values
            if (shouldCheckMinMaxValues && maxQ < minQ) {
                throw new ValidationException(owner,
                    "maximum reactive power is expected to be greater than or equal to minimum reactive power");
            }

            points.put(p, new PointImpl(p, minQ, maxQ, shouldCheckMinMaxValues));
            return ReactiveCapabilityCurveAdderImpl.this;
        }

    }

    ReactiveCapabilityCurveAdderImpl(O owner) {
        this.owner = owner;
    }

    @Override
    public PointAdder beginPoint() {
        return new PointAdderImpl();
    }

    @Override
    public ReactiveCapabilityCurve add() {
        if (points.size() < 2) {
            throw new ValidationException(owner, "a reactive capability curve should have at least two points");
        }
        ReactiveCapabilityCurveImpl curve = new ReactiveCapabilityCurveImpl(points, owner.getMessageHeader());
        owner.setReactiveLimits(curve);
        return curve;
    }

    @Override
    public ReactiveCapabilityCurveAdder setShouldCheckMinMaxValues(boolean shouldCheckMinMaxValues) {
        this.shouldCheckMinMaxValues = shouldCheckMinMaxValues;
        return this;
    }
}
