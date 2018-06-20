/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.iidm.network.ReactiveLimitsHolder;

import javax.xml.stream.XMLStreamException;

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_URI;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ReactiveLimitsXml {

    static final ReactiveLimitsXml INSTANCE = new ReactiveLimitsXml();

    private static final String ELEM_REACTIVE_CAPABILITY_CURVE = "reactiveCapabilityCurve";
    private static final String ELEM_MIN_MAX_REACTIVE_LIMITS = "minMaxReactiveLimits";
    private static final String ATTR_MIN_Q = "minQ";
    private static final String ATTR_MAX_Q = "maxQ";

    public void write(ReactiveLimitsHolder holder, NetworkXmlWriterContext context) throws XMLStreamException {
        switch (holder.getReactiveLimits().getKind()) {
            case CURVE:
                ReactiveCapabilityCurve curve = holder.getReactiveLimits(ReactiveCapabilityCurve.class);
                context.getWriter().writeStartElement(IIDM_URI, ELEM_REACTIVE_CAPABILITY_CURVE);
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    context.getWriter().writeEmptyElement(IIDM_URI, "point");
                    XmlUtil.writeDouble("p", point.getP(), context.getWriter());
                    XmlUtil.writeDouble(ATTR_MIN_Q, point.getMinQ(), context.getWriter());
                    XmlUtil.writeDouble(ATTR_MAX_Q, point.getMaxQ(), context.getWriter());
                }
                context.getWriter().writeEndElement();
                break;

            case MIN_MAX:
                MinMaxReactiveLimits limits = holder.getReactiveLimits(MinMaxReactiveLimits.class);
                context.getWriter().writeEmptyElement(IIDM_URI, ELEM_MIN_MAX_REACTIVE_LIMITS);
                XmlUtil.writeDouble(ATTR_MIN_Q, limits.getMinQ(), context.getWriter());
                XmlUtil.writeDouble(ATTR_MAX_Q, limits.getMaxQ(), context.getWriter());
                break;

            default:
                throw new AssertionError();
        }
    }

    public void read(ReactiveLimitsHolder holder, NetworkXmlReaderContext context) throws XMLStreamException {
        switch (context.getReader().getLocalName()) {
            case ELEM_REACTIVE_CAPABILITY_CURVE:
                ReactiveCapabilityCurveAdder curveAdder = holder.newReactiveCapabilityCurve();
                XmlUtil.readUntilEndElement(ELEM_REACTIVE_CAPABILITY_CURVE, context.getReader(), () -> {
                    if (context.getReader().getLocalName().equals("point")) {
                        double p = XmlUtil.readDoubleAttribute(context.getReader(), "p");
                        double minQ = XmlUtil.readDoubleAttribute(context.getReader(), ATTR_MIN_Q);
                        double maxQ = XmlUtil.readDoubleAttribute(context.getReader(), ATTR_MAX_Q);
                        curveAdder.beginPoint()
                                .setP(p)
                                .setMinQ(minQ)
                                .setMaxQ(maxQ)
                                .endPoint();
                    }
                });
                curveAdder.add();
                break;

            case ELEM_MIN_MAX_REACTIVE_LIMITS:
                double min = XmlUtil.readDoubleAttribute(context.getReader(), ATTR_MIN_Q);
                double max = XmlUtil.readDoubleAttribute(context.getReader(), ATTR_MAX_Q);
                holder.newMinMaxReactiveLimits()
                        .setMinQ(min)
                        .setMaxQ(max)
                        .add();
                break;

            default:

        }
    }
}
