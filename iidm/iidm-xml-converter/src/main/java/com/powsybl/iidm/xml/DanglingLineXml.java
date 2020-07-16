/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineXml extends AbstractConnectableXml<DanglingLine, DanglingLineAdder, VoltageLevel> {

    private static final String GENERATION = "generation";

    static final DanglingLineXml INSTANCE = new DanglingLineXml();

    static final String ROOT_ELEMENT_NAME = "danglingLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(DanglingLine dl) {
        return dl.getCurrentLimits() != null;
    }

    @Override
    protected void writeRootElementAttributes(DanglingLine dl, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("p0", dl.getP0(), context.getWriter());
        XmlUtil.writeDouble("q0", dl.getQ0(), context.getWriter());
        XmlUtil.writeDouble("r", dl.getR(), context.getWriter());
        XmlUtil.writeDouble("x", dl.getX(), context.getWriter());
        XmlUtil.writeDouble("g", dl.getG(), context.getWriter());
        XmlUtil.writeDouble("b", dl.getB(), context.getWriter());
        if (dl.getUcteXnodeCode() != null) {
            context.getWriter().writeAttribute("ucteXnodeCode", dl.getUcteXnodeCode());
        }
        writeNodeOrBus(null, dl.getTerminal(), context);
        writePQ(null, dl.getTerminal(), context.getWriter());
    }

    private static void writeGeneration(DanglingLine.Generation generation, XMLStreamWriter writer, IidmXmlVersion version, NetworkXmlWriterContext context) throws XMLStreamException {
        writer.writeStartElement(version.getNamespaceURI(), GENERATION);
        XmlUtil.writeDouble("minP", generation.getMinP(), writer);
        XmlUtil.writeDouble("maxP", generation.getMaxP(), writer);
        writer.writeAttribute("voltageRegulationOn", Boolean.toString(generation.isVoltageRegulationOn()));
        XmlUtil.writeDouble("targetP", generation.getTargetP(), writer);
        XmlUtil.writeDouble("targetV", generation.getTargetV(), writer);
        XmlUtil.writeDouble("targetQ", generation.getTargetQ(), writer);
        ReactiveLimitsXml.INSTANCE.write(generation, context);
        writer.writeEndElement();
    }

    @Override
    protected void writeSubElements(DanglingLine dl, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (dl.getGeneration() != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, GENERATION, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
            writeGeneration(dl.getGeneration(), context.getWriter(), context.getVersion(), context);
        }
        if (dl.getCurrentLimits() != null) {
            writeCurrentLimits(null, dl.getCurrentLimits(), context.getWriter(), context.getVersion());
        }
    }

    @Override
    protected DanglingLineAdder createAdder(VoltageLevel vl) {
        return vl.newDanglingLine();
    }

    @Override
    protected DanglingLine readRootElementAttributes(DanglingLineAdder adder, NetworkXmlReaderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void readElement(String id, DanglingLineAdder adder, NetworkXmlReaderContext context) throws XMLStreamException {
        double p0 = XmlUtil.readDoubleAttribute(context.getReader(), "p0");
        double q0 = XmlUtil.readDoubleAttribute(context.getReader(), "q0");
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
        double g = XmlUtil.readDoubleAttribute(context.getReader(), "g");
        double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
        readNodeOrBus(adder, context);
        adder.setP0(p0)
                .setQ0(q0)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setUcteXnodeCode(ucteXnodeCode);
        Map<String, String> properties = new HashMap<>();
        double p = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p");
        double q = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q");
        double[] permanentLimit = new double[1];
        permanentLimit[0] = Double.NaN;
        Map<Integer, TemporaryLimitXml> temporaryLimits = new HashMap<>();
        DanglingLine[] danglingLine = new DanglingLine[1];
        boolean[] hasGeneration = new boolean[1];
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "property":
                    String name = context.getReader().getAttributeValue(null, "name");
                    String value = context.getReader().getAttributeValue(null, "value");
                    properties.put(name, value);
                    break;
                case "currentLimits":
                    permanentLimit[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "permanentLimit");
                    XmlUtil.readUntilEndElement("currentLimits", context.getReader(), () -> {
                        if ("temporaryLimit".equals(context.getReader().getLocalName())) {
                            String tlName = context.getReader().getAttributeValue(null, "name");
                            int acceptableDuration = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "acceptableDuration", Integer.MAX_VALUE);
                            double tlValue = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "value", Double.MAX_VALUE);
                            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious", false);
                            temporaryLimits.put(acceptableDuration, new TemporaryLimitXml(tlName, tlValue, fictitious));
                        }
                    });
                    break;
                case GENERATION:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, GENERATION, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    hasGeneration[0] = true;
                    danglingLine[0] = readGeneration(adder, context.getReader(), context);
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + id + ">");
            }
        });
        if (!hasGeneration[0]) {
            danglingLine[0] = adder.add();
        }
        properties.forEach(danglingLine[0]::setProperty);
        danglingLine[0].getTerminal().setP(p).setQ(q);
        if (!Double.isNaN(permanentLimit[0]) || !temporaryLimits.isEmpty()) {
            CurrentLimitsAdder limitsAdder = danglingLine[0].newCurrentLimits()
                    .setPermanentLimit(permanentLimit[0]);
            temporaryLimits
                    .forEach((acceptableDuration, tl) -> limitsAdder.beginTemporaryLimit()
                    .setAcceptableDuration(acceptableDuration)
                    .setName(tl.name)
                    .setValue(tl.value)
                    .setFictitious(tl.fictitious)
                    .endTemporaryLimit());
            limitsAdder.add();
        }
    }

    private static DanglingLine readGeneration(DanglingLineAdder adder, XMLStreamReader reader, NetworkXmlReaderContext context) throws XMLStreamException {
        double minP = XmlUtil.readOptionalDoubleAttribute(reader, "minP");
        double maxP = XmlUtil.readOptionalDoubleAttribute(reader, "maxP");
        boolean voltageRegulationOn = XmlUtil.readBoolAttribute(reader, "voltageRegulationOn");
        double targetP = XmlUtil.readOptionalDoubleAttribute(reader, "targetP");
        double targetV = XmlUtil.readOptionalDoubleAttribute(reader, "targetV");
        double targetQ = XmlUtil.readOptionalDoubleAttribute(reader, "targetQ");
        adder.newGeneration()
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulationOn(voltageRegulationOn)
                .setTargetP(targetP)
                .setTargetV(targetV)
                .setTargetQ(targetQ)
                .add();
        DanglingLine danglingLine = adder.add();
        ReactiveLimitsXml.INSTANCE.read(danglingLine.getGeneration(), context);
        return danglingLine;
    }

    class TemporaryLimitXml {
        private final String name;
        private final double value;
        private final boolean fictitious;

        TemporaryLimitXml(String name, double value, boolean fictitious) {
            this.name = name;
            this.value = value;
            this.fictitious = fictitious;
        }
    }
}
