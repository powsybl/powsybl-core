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

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ReactiveLimitsXml implements XmlConstants {

    static final ReactiveLimitsXml INSTANCE = new ReactiveLimitsXml();

    public void write(ReactiveLimitsHolder holder, NetworkXmlWriterContext context) throws XMLStreamException {
        switch (holder.getReactiveLimits().getKind()) {
            case CURVE: {
                ReactiveCapabilityCurve curve = holder.getReactiveLimits(ReactiveCapabilityCurve.class);
                context.getWriter().writeStartElement(IIDM_URI, "reactiveCapabilityCurve");
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    context.getWriter().writeEmptyElement(IIDM_URI, "point");
                    XmlUtil.writeDouble("p", point.getP(), context.getWriter());
                    XmlUtil.writeDouble("minQ", point.getMinQ(), context.getWriter());
                    XmlUtil.writeDouble("maxQ", point.getMaxQ(), context.getWriter());
                }
                context.getWriter().writeEndElement();
            }
            break;

            case MIN_MAX: {
                MinMaxReactiveLimits limits = holder.getReactiveLimits(MinMaxReactiveLimits.class);
                context.getWriter().writeEmptyElement(IIDM_URI, "minMaxReactiveLimits");
                XmlUtil.writeDouble("minQ", limits.getMinQ(), context.getWriter());
                XmlUtil.writeDouble("maxQ", limits.getMaxQ(), context.getWriter());
            }
            break;

            default:
                throw new AssertionError();
        }
    }

    public void read(ReactiveLimitsHolder holder, NetworkXmlReaderContext context) throws XMLStreamException {
        switch (context.getReader().getLocalName()) {
            case "reactiveCapabilityCurve":
                ReactiveCapabilityCurveAdder curveAdder = holder.newReactiveCapabilityCurve();
                XmlUtil.readUntilEndElement("reactiveCapabilityCurve", context.getReader(), () -> {
                    if (context.getReader().getLocalName().equals("point")) {
                        double p = XmlUtil.readDoubleAttribute(context.getReader(), "p");
                        double minQ = XmlUtil.readDoubleAttribute(context.getReader(), "minQ");
                        double maxQ = XmlUtil.readDoubleAttribute(context.getReader(), "maxQ");
                        curveAdder.beginPoint()
                                .setP(p)
                                .setMinQ(minQ)
                                .setMaxQ(maxQ)
                                .endPoint();
                    }
                });
                curveAdder.add();
                break;

            case "minMaxReactiveLimits":
                double min = XmlUtil.readDoubleAttribute(context.getReader(), "minQ");
                double max = XmlUtil.readDoubleAttribute(context.getReader(), "maxQ");
                holder.newMinMaxReactiveLimits()
                        .setMinQ(min)
                        .setMaxQ(max)
                        .add();
                break;

            default:

        }
    }
}
