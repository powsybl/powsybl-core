/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.google.common.collect.Range;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractReactiveLimitsOwnerConversion extends AbstractConductingEquipmentConversion {

    public AbstractReactiveLimitsOwnerConversion(
        String type,
        PropertyBag p,
        Context context) {
        super(type, p, context);
    }

    protected void convertReactiveLimits(Generator g) {
        // IIDM only accepts one definition of limits,
        // either MinMaxReactiveLimits or ReactiveCapabilityCurve.
        // If an element has both definitions we will select the
        // ReactiveCapabilityCurve.
        if (p.containsKey("ReactiveCapabilityCurve")) {
            String curveId = p.getId("ReactiveCapabilityCurve");
            PropertyBags curveData = context.reactiveCapabilityCurveData(curveId);

            Map<Double, Range<Double>> qRanges = new HashMap<>();
            curveData.forEach(d -> {
                double p = d.asDouble("xvalue");
                double minQ = d.asDouble("y1value");
                double maxQ = d.asDouble("y2value");

                if (!checkPointValidity(p, minQ, maxQ)) {
                    return;
                }

                Range<Double> qRange = fixedMinMaxQ("ReactiveCapabilityCurve Point", minQ, maxQ);

                // check if there is a previous point with same p
                Range<Double> prev = qRanges.get(p);
                if (prev != null) {
                    if (prev.isConnected(qRange)) {
                        fixed("reactive capability curve", () -> String.format("point merged with another one with same p (%f)", p));
                        qRanges.put(p, prev.span(qRange));
                    } else {
                        ignored("reactive capability curve point", () -> String.format("another point with same p (%f) and a disconnected reactive range", p));
                    }
                } else {
                    qRanges.put(p, qRange);
                }
            });

            if (qRanges.size() >= 2) {
                // create curve
                ReactiveCapabilityCurveAdder rcca = g.newReactiveCapabilityCurve();
                for (Map.Entry<Double, Range<Double>> e : qRanges.entrySet()) {
                    double p = e.getKey();
                    Range<Double> qRange = e.getValue();
                    rcca.beginPoint()
                            .setP(p)
                            .setMinQ(qRange.lowerEndpoint())
                            .setMaxQ(qRange.upperEndpoint())
                            .endPoint();
                }
                rcca.add();
            } else if (qRanges.size() == 1) {
                fixed("reactive capability curve", "only one point");
                Map.Entry<Double, Range<Double>> e = qRanges.entrySet().iterator().next();
                Range<Double> qRange = e.getValue();
                g.newMinMaxReactiveLimits()
                            .setMinQ(qRange.lowerEndpoint())
                            .setMaxQ(qRange.upperEndpoint())
                            .add();
            } else {
                missing("Empty curve");
                convertMinMaxQ(g);
            }
        } else {
            convertMinMaxQ(g);
        }
    }

    private void convertMinMaxQ(Generator g) {
        if (p.containsKey("minQ") && p.containsKey("maxQ")) {
            double minQ = p.asDouble("minQ");
            double maxQ = p.asDouble("maxQ");
            Range<Double> qRange = fixedMinMaxQ("Reactive Limits", minQ, maxQ);
            g.newMinMaxReactiveLimits()
                    .setMinQ(qRange.lowerEndpoint())
                    .setMaxQ(qRange.upperEndpoint())
                    .add();
        } else {
            missing("minQ/maxQ are missing, default to unbounded reactive limits");
        }
    }

    private boolean checkPointValidity(double p, double minQ, double maxQ) {
        if (Double.isNaN(p) || Double.isNaN(minQ) || Double.isNaN(maxQ)) {
            Supplier<String> reason = () -> String.format("Incomplete point p, minQ, maxQ = %f, %f, %f", p, minQ, maxQ);
            ignored("ReactiveCapabilityCurvePoint", reason);
            return false;
        }
        return true;
    }

    private Range<Double> fixedMinMaxQ(String context, double minQ, double maxQ) {
        if (minQ > maxQ) {
            Supplier<String> reason = () -> String.format("minQ > maxQ (%.4f > %.4f)", minQ, maxQ);
            fixed(context, reason);
            return Range.closed(maxQ, minQ);
        }
        return Range.closed(minQ, maxQ);
    }
}
