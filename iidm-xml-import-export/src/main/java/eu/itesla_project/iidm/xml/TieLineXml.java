/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TieLine;
import eu.itesla_project.iidm.network.TieLineAdder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineXml extends ConnectableXml<TieLine, TieLineAdder, Network> {

    static final TieLineXml INSTANCE = new TieLineXml();

    static final String ROOT_ELEMENT_NAME = "tieLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(TieLine tl) {
        return tl.getCurrentLimits1() != null || tl.getCurrentLimits2() != null;
    }

    private static void writeHalf(TieLine.HalfLine halfLine, XMLStreamWriter writer, int side) throws XMLStreamException {
        writer.writeAttribute("id_" + side, halfLine.getId());
        if (halfLine.getName() != null) {
            writer.writeAttribute("name_" + side, halfLine.getName());
        }
        XmlUtil.writeFloat("r_" + side, halfLine.getR(), writer);
        XmlUtil.writeFloat("x_" + side, halfLine.getX(), writer);
        XmlUtil.writeFloat("g1_" + side, halfLine.getG1(), writer);
        XmlUtil.writeFloat("b1_" + side, halfLine.getB1(), writer);
        XmlUtil.writeFloat("g2_" + side, halfLine.getG2(), writer);
        XmlUtil.writeFloat("b2_" + side, halfLine.getB2(), writer);
        XmlUtil.writeFloat("xnodeP_" + side, halfLine.getXnodeP(), writer);
        XmlUtil.writeFloat("xnodeQ_" + side, halfLine.getXnodeQ(), writer);
    }

    @Override
    protected void writeRootElementAttributes(TieLine tl, Network n, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("ucteXnodeCode", tl.getUcteXnodeCode());
        writeNodeOrBus(1, tl.getTerminal1(), context);
        writeNodeOrBus(2, tl.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, tl.getTerminal1(), context.getWriter());
            writePQ(2, tl.getTerminal2(), context.getWriter());
        }
        writeHalf(tl.getHalf1(), context.getWriter(), 1);
        writeHalf(tl.getHalf2(), context.getWriter(), 2);
    }

    @Override
    protected void writeSubElements(TieLine tl, Network n, XmlWriterContext context) throws XMLStreamException {
        if (tl.getCurrentLimits1() != null) {
            writeCurrentLimits(1, tl.getCurrentLimits1(), context.getWriter());
        }
        if (tl.getCurrentLimits2() != null) {
            writeCurrentLimits(2, tl.getCurrentLimits2(), context.getWriter());
        }
    }

    @Override
    protected TieLineAdder createAdder(Network n) {
        return n.newTieLine();
    }

    private static void readHalf(TieLineAdder adder, XMLStreamReader reader, int side) {
        String id = reader.getAttributeValue(null, "id_" + side);
        String name = reader.getAttributeValue(null, "name_" + side);
        float r = XmlUtil.readFloatAttribute(reader, "r_" + side);
        float x = XmlUtil.readFloatAttribute(reader, "x_" + side);
        float g1 = XmlUtil.readFloatAttribute(reader, "g1_" + side);
        float b1 = XmlUtil.readFloatAttribute(reader, "b1_" + side);
        float g2 = XmlUtil.readFloatAttribute(reader, "g2_" + side);
        float b2 = XmlUtil.readFloatAttribute(reader, "b2_" + side);
        float xnodeP = XmlUtil.readFloatAttribute(reader, "xnodeP_" + side);
        float xnodeQ = XmlUtil.readFloatAttribute(reader, "xnodeQ_" + side);
        adder.setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2)
                .setXnodeP(xnodeP)
                .setXnodeQ(xnodeQ);
    }

    @Override
    protected TieLine readRootElementAttributes(TieLineAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        readHalf(adder.line1(), reader, 1);
        readHalf(adder.line2(), reader, 2);
        readNodeOrBus(adder, reader);
        String ucteXnodeCode = reader.getAttributeValue(null, "ucteXnodeCode");
        TieLine tl  = adder.setUcteXnodeCode(ucteXnodeCode)
                .add();
        readPQ(1, tl.getTerminal1(), reader);
        readPQ(2, tl.getTerminal2(), reader);
        return tl;
    }

    @Override
    protected void readSubElements(TieLine tl, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            switch (reader.getLocalName()) {
                case "currentLimits1":
                    readCurrentLimits(1, tl::newCurrentLimits1, reader);
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, tl::newCurrentLimits2, reader);
                    break;

                default:
                    super.readSubElements(tl, reader, endTasks);
            }
        });
    }
}