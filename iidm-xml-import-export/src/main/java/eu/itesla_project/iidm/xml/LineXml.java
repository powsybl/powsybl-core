/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.LineAdder;
import eu.itesla_project.iidm.network.Network;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LineXml extends ConnectableXml<Line, LineAdder, Network> {

    static final LineXml INSTANCE = new LineXml();

    static final String ROOT_ELEMENT_NAME = "line";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Line l) {
        return l.getCurrentLimits1() != null || l.getCurrentLimits2() != null;
    }

    @Override
    protected void writeRootElementAttributes(Line l, Network n, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("r", l.getR(), context.getWriter());
        XmlUtil.writeFloat("x", l.getX(), context.getWriter());
        XmlUtil.writeFloat("g1", l.getG1(), context.getWriter());
        XmlUtil.writeFloat("b1", l.getB1(), context.getWriter());
        XmlUtil.writeFloat("g2", l.getG2(), context.getWriter());
        XmlUtil.writeFloat("b2", l.getB2(), context.getWriter());
        writeNodeOrBus(1, l.getTerminal1(), context);
        writeNodeOrBus(2, l.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, l.getTerminal1(), context.getWriter());
            writePQ(2, l.getTerminal2(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(Line l, Network n, XmlWriterContext context) throws XMLStreamException {
        if (l.getCurrentLimits1() != null) {
            writeCurrentLimits(1, l.getCurrentLimits1(), context.getWriter());
        }
        if (l.getCurrentLimits2() != null) {
            writeCurrentLimits(2, l.getCurrentLimits2(), context.getWriter());
        }
    }

    @Override
    protected LineAdder createAdder(Network n) {
        return n.newLine();
    }

    @Override
    protected Line readRootElementAttributes(LineAdder adder, XmlReaderContext context) {
        float r = XmlUtil.readFloatAttribute(context.getReader(), "r");
        float x = XmlUtil.readFloatAttribute(context.getReader(), "x");
        float g1 = XmlUtil.readFloatAttribute(context.getReader(), "g1");
        float b1 = XmlUtil.readFloatAttribute(context.getReader(), "b1");
        float g2 = XmlUtil.readFloatAttribute(context.getReader(), "g2");
        float b2 = XmlUtil.readFloatAttribute(context.getReader(), "b2");
        adder.setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2);
        readNodeOrBus(adder, context);
        Line l = adder.add();
        readPQ(1, l.getTerminal1(), context.getReader());
        readPQ(2, l.getTerminal2(), context.getReader());
        return l;
    }

    @Override
    protected void readSubElements(Line l, XmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "currentLimits1":
                    readCurrentLimits(1, l::newCurrentLimits1, context.getReader());
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, l::newCurrentLimits2, context.getReader());
                    break;

                default:
                    super.readSubElements(l, context);
            }
        });
    }
}