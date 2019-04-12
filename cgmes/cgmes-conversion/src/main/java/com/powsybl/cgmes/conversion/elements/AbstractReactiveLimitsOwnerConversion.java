/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

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
            ReactiveCapabilityCurveAdder rcca = g.newReactiveCapabilityCurve();
            PropertyBags curveData = context.reactiveCapabilityCurveData(curveId);
            curveData.forEach(d -> {
                double p = d.asDouble("xvalue");
                double minQ = d.asDouble("y1value");
                double maxQ = d.asDouble("y2value");
                // Check the point is valid
                if (Double.isNaN(p) || Double.isNaN(minQ) || Double.isNaN(maxQ)) {
                    String reason = String.format("Incomplete point p, minQ, maxQ = %f, %f, %f", p, minQ, maxQ);
                    ignored("ReactiveCapabilityCurvePoint", reason);
                    return;
                }
                double[] minMaxQ = fixedMinMaxQ("ReactiveCapabilityCurve Point", minQ, maxQ);
                minQ = minMaxQ[0];
                maxQ = minMaxQ[1];
                rcca.beginPoint()
                    .setP(p)
                    .setMinQ(minQ)
                    .setMaxQ(maxQ)
                    .endPoint();
            });
            rcca.add();
        } else if (p.containsKey("minQ") && p.containsKey("maxQ")) {
            double minQ = p.asDouble("minQ");
            double maxQ = p.asDouble("maxQ");
            double[] minMaxQ = fixedMinMaxQ("Reactive Limits", minQ, maxQ);
            minQ = minMaxQ[0];
            maxQ = minMaxQ[1];
            g.newMinMaxReactiveLimits()
                .setMinQ(minQ)
                .setMaxQ(maxQ)
                .add();
        }
    }

    private double[] fixedMinMaxQ(String context, double minQ, double maxQ) {
        double[] r = {minQ, maxQ};
        if (minQ > maxQ) {
            String reason = String.format("minQ > maxQ (%.4f > %.4f)", minQ, maxQ);
            fixed(context, reason);
            r[0] = maxQ;
            r[1] = minQ;
        }
        return r;
    }
}
