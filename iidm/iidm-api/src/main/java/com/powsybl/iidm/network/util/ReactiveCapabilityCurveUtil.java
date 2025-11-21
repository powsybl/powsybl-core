/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * @author Sylvestre Prabakaran {@literal <sylvestre.prabakaran at rte-france.com>}
 */
public final class ReactiveCapabilityCurveUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCapabilityCurveUtil.class);

    private ReactiveCapabilityCurveUtil() {
    }

    /**
     * Extrapolate reactive limits when p is outside [minP,maxP] using slopes of reactive limits at the crossed limit of p
     * (Note that this method throws an exception if p is inside [minP, maxP])
     * @param p                               Active power value to evaluate the reactive limits
     * @param points                          TreeMap of all points defining the reactive capability curve mapped by their active power values
     * @param valuesToReactiveCapabilityPoint TriFunction returning the used implementation of {@link ReactiveCapabilityCurve.Point} interface: <code>(p, minQ, maxQ) -> Point</code>
     * @param ownerDescription                Description of the ReactiveCapabilityCurve's owner (for logging purpose)
     * @return A ReactiveCapabilityCurve.Point of the extrapolated limits at the requested value of p
     */
    public static ReactiveCapabilityCurve.Point extrapolateReactiveLimitsSlope(double p, TreeMap<Double, ReactiveCapabilityCurve.Point> points, TriFunction<Double, Double, Double, ReactiveCapabilityCurve.Point> valuesToReactiveCapabilityPoint, String ownerDescription) {
        double minQ;
        double maxQ;
        ReactiveCapabilityCurve.Point pBound;
        ReactiveCapabilityCurve.Point pbis;

        if (p < points.firstKey()) {
            // Extrapolate reactive limits slope below min active power limit (pBound = min active power limit)
            pBound = points.firstEntry().getValue();
            pbis = points.higherEntry(points.firstKey()).getValue(); // p < pBound < pbis
        } else if (p > points.lastKey()) {
            // Extrapolate reactive limits slope above max active power limit (pBound = max active power limit)
            pBound = points.lastEntry().getValue();
            pbis = points.lowerEntry(points.lastKey()).getValue(); // pbis < pBound < p
        } else {
            throw new IllegalStateException();
        }
        double slopeMinQ = (pbis.getMinQ() - pBound.getMinQ()) / (pbis.getP() - pBound.getP());
        double slopeMaxQ = (pbis.getMaxQ() - pBound.getMaxQ()) / (pbis.getP() - pBound.getP());
        minQ = pBound.getMinQ() + slopeMinQ * (p - pBound.getP());
        maxQ = pBound.getMaxQ() + slopeMaxQ * (p - pBound.getP());

        if (minQ <= maxQ) {
            return valuesToReactiveCapabilityPoint.apply(p, minQ, maxQ);
        } else { // Corner case of intersecting reactive limits when extrapolated
            double limitQ = (minQ + maxQ) / 2;
            LOGGER.warn("Extrapolation of reactive capability curve for {} leads to minQ > maxQ, correcting to minQ = maxQ", ownerDescription); // This log message can be over flowing (if called at each iteration), apply filters in logback to avoid it
            return valuesToReactiveCapabilityPoint.apply(p, limitQ, limitQ); // Returning the mean as limits minQ and maxQ
        }
    }
}
