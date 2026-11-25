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
import com.powsybl.iidm.network.ReactiveCapabilityShape;
import com.powsybl.iidm.network.ReactiveCapabilityShapeAdder;
import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;
import com.powsybl.iidm.network.ReactiveLimitsHolder;
import com.powsybl.iidm.network.util.PropertiesBufferHolder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class ReactiveLimitsSerDe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveLimitsSerDe.class);

    static final ReactiveLimitsSerDe INSTANCE = new ReactiveLimitsSerDe();

    static final String ELEM_REACTIVE_CAPABILITY_SHAPE = "reactiveCapabilityShape";
    static final String ELEM_REACTIVE_CAPABILITY_CURVE = "reactiveCapabilityCurve";
    static final String ELEM_MIN_MAX_REACTIVE_LIMITS = "minMaxReactiveLimits";
    private static final String ATTR_MIN_Q = "minQ";
    private static final String ATTR_MAX_Q = "maxQ";
    private static final String ATTR_P = "p";
    public static final String POINT_ARRAY_ELEMENT_NAME = "points";
    public static final String POINT_ROOT_ELEMENT_NAME = "point";
    public static final String PLANE_ARRAY_ELEMENT_NAME = "planes";
    public static final String PLANE_ROOT_ELEMENT_NAME = "plane";
    public static final String ATTR_COEFF_U = "coeffU";
    public static final String ATTR_COEFF_P = "coeffP";
    public static final String ATTR_COEFF_Q = "coeffQ";
    public static final String ATTR_RHS = "rhs";
    public static final String ATTR_IS_GREATER_OR_EQUAL = "isGreaterOrEqual";

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

            case SHAPE:
                if (context.getVersion().compareTo(IidmVersion.V_1_19) >= 0) {
                    writeReactiveCapabilityShape(holder, context);
                } else if (!context.getOptions().isForceExportNetworkWithBetaFeatures()) {
                    throw new NotImplementedException("The network contains reactive capability shape limits, export of this kind of reactive limits is not supported " +
                        "for IIDM versions older than " + IidmVersion.V_1_19.toString(".") + ". " +
                        "To force the export of the network and ignore those limits, either use the config parameter iidm.export.xml.force-export-network-with-beta-features, " +
                        "or ExportOptions.setForceExportNetworkWithBetaFeatures");
                }
                break;

            default:
                throw new IllegalStateException();
        }
    }

    private static void writeReactiveCapabilityShape(ReactiveLimitsHolder holder, NetworkSerializerContext context) {
        ReactiveCapabilityShape shape = holder.getReactiveLimits(ReactiveCapabilityShape.class);
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ELEM_REACTIVE_CAPABILITY_SHAPE);
        context.getWriter().writeStartNodes();
        PropertiesSerDe.write(shape, context);
        for (ReactiveCapabilityShapePlane plane : shape.getPlanes()) {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), PLANE_ROOT_ELEMENT_NAME);
            context.getWriter().writeDoubleAttribute(ATTR_COEFF_U, plane.getCoeffU());
            context.getWriter().writeDoubleAttribute(ATTR_COEFF_P, plane.getCoeffP());
            context.getWriter().writeDoubleAttribute(ATTR_COEFF_Q, plane.getCoeffQ());
            context.getWriter().writeDoubleAttribute(ATTR_RHS, plane.getRhs());
            context.getWriter().writeBooleanAttribute(ATTR_IS_GREATER_OR_EQUAL, plane.isGreaterOrEqual());
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();
        context.getWriter().writeEndNode();
    }

    public void readReactiveCapabilityShape(ReactiveLimitsHolder holder, NetworkDeserializerContext context) {
        readReactiveCapabilityShape(context).accept(holder);
    }

    public <T extends ReactiveLimitsHolder> void readReactiveCapabilityShape(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        toApply.add(readReactiveCapabilityShape(context));
    }

    private <T extends ReactiveLimitsHolder> Consumer<T> readReactiveCapabilityShape(NetworkDeserializerContext context) {
        PropertiesBufferHolder shapePropertiesBuffer = new PropertiesBufferHolder();
        List<PlaneData> planeDataList = new ArrayList<>();
        context.getReader().readChildNodes(elementName ->
                readReactiveCapabilityShapeElement(elementName, shapePropertiesBuffer, planeDataList, context));

        return reactiveLimitsHolder -> {
            ReactiveCapabilityShapeAdder shapeAdder = reactiveLimitsHolder.newReactiveCapabilityShape();
            shapePropertiesBuffer.copyPropertiesTo(shapeAdder);
            planeDataList.forEach(planeData -> shapeAdder.addPlane(planeData.coeffU(), planeData.coeffP(),
                    planeData.coeffQ(), planeData.isGreaterOrEqual(), planeData.rhs()));
            shapeAdder.add();
        };
    }

    private void readReactiveCapabilityShapeElement(String elementName,
                                                    PropertiesBufferHolder shapePropertiesBuffer,
                                                    List<PlaneData> planeDataList,
                                                    NetworkDeserializerContext context) {
        if (elementName.equals(PropertiesSerDe.ROOT_ELEMENT_NAME)) {
            PropertiesSerDe.read(shapePropertiesBuffer, context);
        } else if (elementName.equals(PLANE_ROOT_ELEMENT_NAME)) {
            readReactiveCapabilityShapePlane(planeDataList, context);
        } else {
            throw new PowsyblException("Unknown element name '" + elementName + "' in '" + ELEM_REACTIVE_CAPABILITY_SHAPE + "'");
        }
    }

    private void readReactiveCapabilityShapePlane(List<PlaneData> planeDataList, NetworkDeserializerContext context) {
        double coeffU = context.getReader().readDoubleAttribute(ATTR_COEFF_U);
        double coeffP = context.getReader().readDoubleAttribute(ATTR_COEFF_P);
        double coeffQ = context.getReader().readDoubleAttribute(ATTR_COEFF_Q);
        double rhs = context.getReader().readDoubleAttribute(ATTR_RHS);
        boolean isGreaterOrEqual = context.getReader().readBooleanAttribute(ATTR_IS_GREATER_OR_EQUAL);
        context.getReader().readEndNode();
        planeDataList.add(new PlaneData(coeffU, coeffP, coeffQ, isGreaterOrEqual, rhs));
    }

    public void readReactiveCapabilityCurve(ReactiveLimitsHolder holder, NetworkDeserializerContext context) {
        readReactiveCapabilityCurve(context).accept(holder);
    }

    public <T extends ReactiveLimitsHolder> void readReactiveCapabilityCurve(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        toApply.add(readReactiveCapabilityCurve(context));
    }

    private <T extends ReactiveLimitsHolder> Consumer<T> readReactiveCapabilityCurve(NetworkDeserializerContext context) {
        PropertiesBufferHolder curvePropertiesBuffer = new PropertiesBufferHolder();
        List<PointData> pointDataList = new ArrayList<>();
        context.getReader().readChildNodes(elementName ->
                readReactiveCapabilityCurveElement(elementName, curvePropertiesBuffer, pointDataList, context));

        return reactiveLimitsHolder -> {
            ReactiveCapabilityCurveAdder curveAdder = reactiveLimitsHolder.newReactiveCapabilityCurve();
            curvePropertiesBuffer.copyPropertiesTo(curveAdder);
            pointDataList.forEach(pointData -> {
                ReactiveCapabilityCurveAdder.PointAdder pointAdder = curveAdder.beginPoint();
                pointData.pointPropertiesBuffer().copyPropertiesTo(pointAdder);
                pointAdder.setP(pointData.p())
                        .setMinQ(pointData.minQ())
                        .setMaxQ(pointData.maxQ())
                        .endPoint();
            });
            curveAdder.add();
        };
    }

    private void readReactiveCapabilityCurveElement(String elementName,
                                                    PropertiesBufferHolder curvePropertiesBuffer,
                                                    List<PointData> pointDataList,
                                                    NetworkDeserializerContext context) {
        if (elementName.equals(PropertiesSerDe.ROOT_ELEMENT_NAME)) {
            PropertiesSerDe.read(curvePropertiesBuffer, context);
        } else if (elementName.equals(POINT_ROOT_ELEMENT_NAME)) {
            readReactiveCapabilityCurvePoint(pointDataList, context);
        } else {
            throw new PowsyblException("Unknown element name '" + elementName + "' in 'reactiveCapabilityCurve'");
        }
    }

    private void readReactiveCapabilityCurvePoint(List<PointData> pointDataList,
                                                  NetworkDeserializerContext context) {
        double p = context.getReader().readDoubleAttribute(ATTR_P);
        double minQ = context.getReader().readDoubleAttribute(ATTR_MIN_Q);
        double maxQ = context.getReader().readDoubleAttribute(ATTR_MAX_Q);

        PropertiesBufferHolder pointPropertiesBuffer = new PropertiesBufferHolder();
        PropertiesSerDe.readProperties(context, pointPropertiesBuffer);

        if (minQ > maxQ && (context.getVersion().compareTo(IidmVersion.V_1_18) < 0 || context.getOptions().isRepairInvalidReactiveCurveLimits())) {
            LOGGER.warn("Reactive capability curve point at P={} has inverted limits (minQ={} > maxQ={}); values have been reordered", p, minQ, maxQ);
            double tmp = minQ;
            minQ = maxQ;
            maxQ = tmp;
        }

        pointDataList.add(new PointData(p, minQ, maxQ, pointPropertiesBuffer));
    }

    public void readMinMaxReactiveLimits(ReactiveLimitsHolder holder, NetworkDeserializerContext context) {
        readMinMaxReactiveLimits(context).accept(holder);
    }

    public <T extends ReactiveLimitsHolder> void readMinMaxReactiveLimits(List<Consumer<T>> toApply, NetworkDeserializerContext context) {
        toApply.add(readMinMaxReactiveLimits(context));
    }

    private <T extends ReactiveLimitsHolder> Consumer<T> readMinMaxReactiveLimits(NetworkDeserializerContext context) {
        double min = context.getReader().readDoubleAttribute(ATTR_MIN_Q);
        double max = context.getReader().readDoubleAttribute(ATTR_MAX_Q);
        context.getReader().readEndNode();
        return holder -> holder.newMinMaxReactiveLimits()
            .setMinQ(min)
            .setMaxQ(max)
            .add();
    }

    private record PointData(double p, double minQ, double maxQ, PropertiesBufferHolder pointPropertiesBuffer) { }

    private record PlaneData(double coeffU, double coeffP, double coeffQ, boolean isGreaterOrEqual, double rhs) { }
}
