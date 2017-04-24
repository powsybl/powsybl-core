/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.MinMaxReactiveLimits;
import eu.itesla_project.iidm.network.ReactiveCapabilityCurve;
import eu.itesla_project.iidm.network.ReactiveCapabilityCurveAdder;
import eu.itesla_project.iidm.network.ReactiveLimitsHolder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ReactiveLimitsXml implements XmlConstants {

    static final ReactiveLimitsXml INSTANCE = new ReactiveLimitsXml();

    public void write(ReactiveLimitsHolder holder, XmlWriterContext context) throws XMLStreamException {
        switch (holder.getReactiveLimits().getKind()) {
            case CURVE: {
                ReactiveCapabilityCurve curve = holder.getReactiveLimits(ReactiveCapabilityCurve.class);
                context.getWriter().writeStartElement(IIDM_URI, "reactiveCapabilityCurve");
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    context.getWriter().writeEmptyElement(IIDM_URI, "point");
                    XmlUtil.writeFloat("p", point.getP(), context.getWriter());
                    XmlUtil.writeFloat("minQ", point.getMinQ(), context.getWriter());
                    XmlUtil.writeFloat("maxQ", point.getMaxQ(), context.getWriter());
                }
                context.getWriter().writeEndElement();
            }
            break;

            case MIN_MAX: {
                MinMaxReactiveLimits limits = holder.getReactiveLimits(MinMaxReactiveLimits.class);
                context.getWriter().writeEmptyElement(IIDM_URI, "minMaxReactiveLimits");
                XmlUtil.writeFloat("minQ", limits.getMinQ(), context.getWriter());
                XmlUtil.writeFloat("maxQ", limits.getMaxQ(), context.getWriter());
            }
            break;

            default:
                throw new AssertionError();
        }
    }

    public void read(ReactiveLimitsHolder holder, XmlReaderContext context) throws XMLStreamException {
        switch (context.getReader().getLocalName()) {
            case "reactiveCapabilityCurve":
                ReactiveCapabilityCurveAdder curveAdder = holder.newReactiveCapabilityCurve();
                XmlUtil.readUntilEndElement("reactiveCapabilityCurve", context.getReader(), () -> {
                    if (context.getReader().getLocalName().equals("point")) {
                        float p = XmlUtil.readFloatAttribute(context.getReader(), "p");
                        float minQ = XmlUtil.readFloatAttribute(context.getReader(), "minQ");
                        float maxQ = XmlUtil.readFloatAttribute(context.getReader(), "maxQ");
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
                float min = XmlUtil.readFloatAttribute(context.getReader(), "minQ");
                float max = XmlUtil.readFloatAttribute(context.getReader(), "maxQ");
                holder.newMinMaxReactiveLimits()
                        .setMinQ(min)
                        .setMaxQ(max)
                        .add();
                break;

            default:

        }
    }
}
