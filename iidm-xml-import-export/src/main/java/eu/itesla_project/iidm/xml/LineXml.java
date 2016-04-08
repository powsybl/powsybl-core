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
import javax.xml.stream.XMLStreamReader;
import java.util.List;

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
        writeFloat("r", l.getR(), context.getWriter());
        writeFloat("x", l.getX(), context.getWriter());
        writeFloat("g1", l.getG1(), context.getWriter());
        writeFloat("b1", l.getB1(), context.getWriter());
        writeFloat("g2", l.getG2(), context.getWriter());
        writeFloat("b2", l.getB2(), context.getWriter());
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
    protected Line readRootElementAttributes(LineAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float r = readFloatAttribute(reader, "r");
        float x = readFloatAttribute(reader, "x");
        float g1 = readFloatAttribute(reader, "g1");
        float b1 = readFloatAttribute(reader, "b1");
        float g2 = readFloatAttribute(reader, "g2");
        float b2 = readFloatAttribute(reader, "b2");
        adder.setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2);
        readNodeOrBus(adder, reader);
        Line l = adder.add();
        readPQ(1, l.getTerminal1(), reader);
        readPQ(2, l.getTerminal2(), reader);
        return l;
    }

    @Override
    protected void readSubElements(Line l, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            switch (reader.getLocalName()) {
                case "currentLimits1":
                    readCurrentLimits(1, l::newCurrentLimits1, reader);
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, l::newCurrentLimits2, reader);
                    break;

                default:
                    super.readSubElements(l, reader, endTasks);
            }
        });
    }
}