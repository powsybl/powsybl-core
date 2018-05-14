/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TieLineAdder;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineXml extends AbstractConnectableXml<TieLine, TieLineAdder, Network> {

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

    private static void writeHalf(TieLine.HalfLine halfLine, NetworkXmlWriterContext context, int side) throws XMLStreamException {
        context.getWriter().writeAttribute("id_" + side, context.getAnonymizer().anonymizeString(halfLine.getId()));
        if (halfLine.getName() != null) {
            context.getWriter().writeAttribute("name_" + side, context.getAnonymizer().anonymizeString(halfLine.getName()));
        }
        XmlUtil.writeDouble("r_" + side, halfLine.getR(), context.getWriter());
        XmlUtil.writeDouble("x_" + side, halfLine.getX(), context.getWriter());
        XmlUtil.writeDouble("g1_" + side, halfLine.getG1(), context.getWriter());
        XmlUtil.writeDouble("b1_" + side, halfLine.getB1(), context.getWriter());
        XmlUtil.writeDouble("g2_" + side, halfLine.getG2(), context.getWriter());
        XmlUtil.writeDouble("b2_" + side, halfLine.getB2(), context.getWriter());
        XmlUtil.writeDouble("xnodeP_" + side, halfLine.getXnodeP(), context.getWriter());
        XmlUtil.writeDouble("xnodeQ_" + side, halfLine.getXnodeQ(), context.getWriter());
    }

    @Override
    protected void writeRootElementAttributes(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("ucteXnodeCode", tl.getUcteXnodeCode());
        writeNodeOrBus(1, tl.getTerminal1(), context);
        writeNodeOrBus(2, tl.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, tl.getTerminal1(), context.getWriter());
            writePQ(2, tl.getTerminal2(), context.getWriter());
        }
        writeHalf(tl.getHalf1(), context, 1);
        writeHalf(tl.getHalf2(), context, 2);
    }

    @Override
    protected void writeSubElements(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
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

    private static void readHalf(TieLineAdder adder, NetworkXmlReaderContext context, int side) {
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
    protected TieLine readRootElementAttributes(TieLineAdder adder, NetworkXmlReaderContext context) {
        readHalf(adder.line1(), context, 1);
        readHalf(adder.line2(), context, 2);
        readNodeOrBus(adder, context);
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
        TieLine tl  = adder.setUcteXnodeCode(ucteXnodeCode)
                .add();
        readPQ(1, tl.getTerminal1(), context.getReader());
        readPQ(2, tl.getTerminal2(), context.getReader());
        return tl;
    }

    @Override
    protected void readSubElements(TieLine tl, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "currentLimits1":
                    readCurrentLimits(1, tl::newCurrentLimits1, context.getReader());
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, tl::newCurrentLimits2, context.getReader());
                    break;

                default:
                    super.readSubElements(tl, context);
            }
        });
    }
}
