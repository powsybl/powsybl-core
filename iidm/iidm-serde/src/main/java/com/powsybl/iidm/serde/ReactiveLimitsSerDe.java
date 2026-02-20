/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class ReactiveLimitsSerDe {

    static final ReactiveLimitsSerDe INSTANCE = new ReactiveLimitsSerDe();

    static final String ELEM_REACTIVE_CAPABILITY_SHAPE = "reactiveCapabilityShape";
    static final String ELEM_REACTIVE_CAPABILITY_CURVE = "reactiveCapabilityCurve";
    static final String ELEM_MIN_MAX_REACTIVE_LIMITS = "minMaxReactiveLimits";
    private static final String ATTR_MIN_Q = "minQ";
    private static final String ATTR_MAX_Q = "maxQ";
    public static final String PLANE_ROOT_ELEMENT_NAME = "plane";
    // WHAT IS THIS?
    public static final String PLANE_ARRAY_ELEMENT_NAME = "planes";
    public static final String POINT_ARRAY_ELEMENT_NAME = "points";
    public static final String POINT_ROOT_ELEMENT_NAME = "point";
    public static final String ATTR_ALPHA = "alpha";
    public static final String ATTR_BETA = "beta";
    public static final String ATTR_GAMMA = "gamma";
    public static final String ATTR_IS_GREATER_OR_EQUAL = "isGreaterOrEqual";

    public void write(ReactiveLimitsHolder holder, NetworkSerializerContext context) {
        switch (holder.getReactiveLimits().getKind()) {
            case SHAPE:
                ReactiveCapabilityShape shape = holder.getReactiveLimits(ReactiveCapabilityShape.class);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ELEM_REACTIVE_CAPABILITY_SHAPE);
                context.getWriter().writeStartNodes();
                for (ReactiveCapabilityShapePlane plane : shape.getPlanes()) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), PLANE_ROOT_ELEMENT_NAME);
                    context.getWriter().writeDoubleAttribute(ATTR_ALPHA, plane.getAlpha());
                    context.getWriter().writeDoubleAttribute(ATTR_BETA, plane.getBeta());
                    context.getWriter().writeDoubleAttribute(ATTR_GAMMA, plane.getGamma());
                    context.getWriter().writeBooleanAttribute(ATTR_IS_GREATER_OR_EQUAL, plane.isGreaterOrEqual());
                    context.getWriter().writeEndNode();
                }
                context.getWriter().writeEndNodes();
                context.getWriter().writeEndNode();
                break;
            case CURVE:
                ReactiveCapabilityCurve curve = holder.getReactiveLimits(ReactiveCapabilityCurve.class);
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ELEM_REACTIVE_CAPABILITY_CURVE);
                context.getWriter().writeStartNodes();
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), POINT_ROOT_ELEMENT_NAME);
                    context.getWriter().writeDoubleAttribute("p", point.getP());
                    context.getWriter().writeDoubleAttribute(ATTR_MIN_Q, point.getMinQ());
                    context.getWriter().writeDoubleAttribute(ATTR_MAX_Q, point.getMaxQ());
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
                context.getWriter().writeEndNode();
                break;

            default:
                throw new IllegalStateException();
        }
    }

    public void readReactiveCapabilityShape(ReactiveLimitsHolder holder, NetworkDeserializerContext context) {
        ReactiveCapabilityShapeAdder shapeAdder = holder.newReactiveCapabilityShape();
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(PLANE_ROOT_ELEMENT_NAME)) {
                double alpha = context.getReader().readDoubleAttribute(ATTR_ALPHA);
                double beta = context.getReader().readDoubleAttribute(ATTR_BETA);
                double gamma = context.getReader().readDoubleAttribute(ATTR_GAMMA);
                boolean isGreaterOrEqual = context.getReader().readBooleanAttribute(ATTR_IS_GREATER_OR_EQUAL);
                context.getReader().readEndNode();
                shapeAdder.addPlane(alpha, beta, gamma, isGreaterOrEqual);
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'reactiveCapabilityShape'");
            }
        });
        shapeAdder.add();
    }

    public void readReactiveCapabilityCurve(ReactiveLimitsHolder holder, NetworkDeserializerContext context) {
        ReactiveCapabilityCurveAdder curveAdder = holder.newReactiveCapabilityCurve();
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(POINT_ROOT_ELEMENT_NAME)) {
                double p = context.getReader().readDoubleAttribute("p");
                double minQ = context.getReader().readDoubleAttribute(ATTR_MIN_Q);
                double maxQ = context.getReader().readDoubleAttribute(ATTR_MAX_Q);
                context.getReader().readEndNode();
                curveAdder.beginPoint()
                        .setP(p)
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
