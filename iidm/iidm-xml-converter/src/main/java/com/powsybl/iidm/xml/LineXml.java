/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ext.TieLineExt;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
class LineXml extends AbstractConnectableXml<Line, LineAdder, Network> {

    static final LineXml INSTANCE = new LineXml();

    static final String ROOT_ELEMENT_NAME = "line";

    static final String ROOT_ELEMENT_NAME_TIELINE = "tieLine";

    private String currentRootElement = ROOT_ELEMENT_NAME;

    @Override
    protected String getRootElementName() {
        return currentRootElement;
    }

    @Override
    protected boolean hasSubElements(Line l) {
        return l.getCurrentLimits1() != null || l.getCurrentLimits2() != null;
    }

    @Override
    protected void writeRootElementAttributes(Line l, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("r", l.getR(), context.getWriter());
        XmlUtil.writeDouble("x", l.getX(), context.getWriter());
        XmlUtil.writeDouble("g1", l.getG1(), context.getWriter());
        XmlUtil.writeDouble("b1", l.getB1(), context.getWriter());
        XmlUtil.writeDouble("g2", l.getG2(), context.getWriter());
        XmlUtil.writeDouble("b2", l.getB2(), context.getWriter());
        writeNodeOrBus(1, l.getTerminal1(), context);
        writeNodeOrBus(2, l.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, l.getTerminal1(), context.getWriter());
            writePQ(2, l.getTerminal2(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(Line l, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
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
    protected Line readRootElementAttributes(LineAdder adder, NetworkXmlReaderContext context) {
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
        String localName = context.getReader().getLocalName();
        if (localName.equals(ROOT_ELEMENT_NAME)) {
            r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
            x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
            g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1");
            b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1");
            g2 = XmlUtil.readDoubleAttribute(context.getReader(), "g2");
            b2 = XmlUtil.readDoubleAttribute(context.getReader(), "b2");
        } else if (isReadingTieLine(context)) {
            r = XmlUtil.readDoubleAttribute(context.getReader(), "r_1") + XmlUtil.readDoubleAttribute(context.getReader(), "r_2");
            x = XmlUtil.readDoubleAttribute(context.getReader(), "x_1") + XmlUtil.readDoubleAttribute(context.getReader(), "x_2");
            g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1_1") + XmlUtil.readDoubleAttribute(context.getReader(), "g1_2");
            b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1_1") + XmlUtil.readDoubleAttribute(context.getReader(), "b1_2");
            g2 = XmlUtil.readDoubleAttribute(context.getReader(), "g2_1") + XmlUtil.readDoubleAttribute(context.getReader(), "g2_2");
            b2 = XmlUtil.readDoubleAttribute(context.getReader(), "b2_1") + XmlUtil.readDoubleAttribute(context.getReader(), "b2_2");
        } else {
            throw new AssertionError(localName + " is not a valid element. Line/TieLine are expected.");
        }
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
        if (isReadingTieLine(context)) {
            TieLineExt.HalfLineImpl hl1 = readHalf(context, 1);
            TieLineExt.HalfLineImpl hl2 = readHalf(context, 2);
            String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
            TieLineExt tieLineExt = new TieLineExt(l, ucteXnodeCode, hl1, hl2);
            l.addExtension(TieLineExt.class, tieLineExt);
        }
        return l;
    }

    private static TieLineExt.HalfLineImpl readHalf(NetworkXmlReaderContext context, int side) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id_" + side));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name_" + side));
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r_" + side);
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x_" + side);
        double g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1_" + side);
        double b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1_" + side);
        double g2 = XmlUtil.readDoubleAttribute(context.getReader(), "g2_" + side);
        double b2 = XmlUtil.readDoubleAttribute(context.getReader(), "b2_" + side);
        double xnodeP = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeP_" + side);
        double xnodeQ = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeQ_" + side);
        TieLineExt.HalfLineImpl hl = new TieLineExt.HalfLineImpl();
        hl.setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2)
                .setXnodeP(xnodeP)
                .setXnodeQ(xnodeQ);
        return hl;
    }

    @Override
    protected void readSubElements(Line l, NetworkXmlReaderContext context) throws XMLStreamException {
        if (isReadingTieLine(context)) {
            currentRootElement = ROOT_ELEMENT_NAME_TIELINE;
        }
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
        if (isReadingTieLine(context)) {
            currentRootElement = ROOT_ELEMENT_NAME;
        }
    }

    private boolean isReadingTieLine(NetworkXmlReaderContext context) {
        return context.getReader().getLocalName().equals(ROOT_ELEMENT_NAME_TIELINE);
    }
}
