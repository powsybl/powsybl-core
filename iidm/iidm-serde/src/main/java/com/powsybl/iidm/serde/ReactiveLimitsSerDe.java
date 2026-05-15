/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.iidm.network.ReactiveLimitsHolder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class ReactiveLimitsSerDe {

    static final ReactiveLimitsSerDe INSTANCE = new ReactiveLimitsSerDe();

    static final String ELEM_REACTIVE_CAPABILITY_CURVE = "reactiveCapabilityCurve";
    static final String ELEM_MIN_MAX_REACTIVE_LIMITS = "minMaxReactiveLimits";
    private static final String ATTR_MIN_Q = "minQ";
    private static final String ATTR_MAX_Q = "maxQ";
    private static final String ATTR_P = "p";
    public static final String POINT_ARRAY_ELEMENT_NAME = "points";
    public static final String POINT_ROOT_ELEMENT_NAME = "point";

    public void write(ReactiveLimitsHolder holder, NetworkSerializerContext context) {
        switch (holder.getReactiveLimits().getKind()) {
            case CURVE:
                ReactiveCapabilityCurve curve = holder.getReactiveLimits(ReactiveCapabilityCurve.class);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ELEM_REACTIVE_CAPABILITY_CURVE);
                context.getWriter().writeStartNodes();
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> PropertiesSerDe.write(curve, context));
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), POINT_ROOT_ELEMENT_NAME);
                    context.getWriter().writeDoubleAttribute(ATTR_P, point.getP());
                    context.getWriter().writeDoubleAttribute(ATTR_MIN_Q, point.getMinQ());
                    context.getWriter().writeDoubleAttribute(ATTR_MAX_Q, point.getMaxQ());
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> PropertiesSerDe.write(point, context));
                    context.getWriter().writeEndNode();
                }
                context.getWriter().writeEndNodes();
                context.getWriter().writeEndNode();
                break;

            case MIN_MAX:
                MinMaxReactiveLimits limits = holder.getReactiveLimits(MinMaxReactiveLimits.class);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ELEM_MIN_MAX_REACTIVE_LIMITS);
                context.getWriter().writeDoubleAttribute(ATTR_MIN_Q, limits.getMinQ());
                context.getWriter().writeDoubleAttribute(ATTR_MAX_Q, limits.getMaxQ());
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> PropertiesSerDe.write(limits, context));
                context.getWriter().writeEndNode();
                break;

            default:
                throw new IllegalStateException();
        }
    }

    public void readReactiveCapabilityCurve(ReactiveLimitsHolder holder, NetworkDeserializerContext context) {
        ReactiveCapabilityCurveAdder curveAdder = holder.newReactiveCapabilityCurve();
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(PropertiesSerDe.ROOT_ELEMENT_NAME)) {
                PropertiesSerDe.read(curveAdder, context);
            } else if (elementName.equals(POINT_ROOT_ELEMENT_NAME)) {
                double p = context.getReader().readDoubleAttribute(ATTR_P);
                double minQ = context.getReader().readDoubleAttribute(ATTR_MIN_Q);
                double maxQ = context.getReader().readDoubleAttribute(ATTR_MAX_Q);
                ReactiveCapabilityCurveAdder.PointAdder pointAdder = curveAdder.beginPoint();
                PropertiesSerDe.readProperties(context, pointAdder);
                pointAdder.setP(p)
                        .setMinQ(minQ)
                        .setMaxQ(maxQ)
                        .endPoint();
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'reactiveCapabilityCurve'");
            }
        });
        curveAdder.add();
    }

    public void readMinMaxReactiveLimits(ReactiveLimitsHolder holder, NetworkDeserializerContext context) {
        double min = context.getReader().readDoubleAttribute(ATTR_MIN_Q);
        double max = context.getReader().readDoubleAttribute(ATTR_MAX_Q);
        context.getReader().readEndNode();
        holder.newMinMaxReactiveLimits()
                .setMinQ(min)
                .setMaxQ(max)
                .add();
    }
}
