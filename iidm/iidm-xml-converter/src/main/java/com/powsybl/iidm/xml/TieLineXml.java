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
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineXml extends AbstractConnectableXml<TieLine, TieLineAdder, Network> {

    private static final String XNODE_P = "xnodeP_";
    private static final String XNODE_Q = "xnodeQ_";

    private static final Logger LOGGER = LoggerFactory.getLogger(TieLineXml.class);

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
        if (!halfLine.getId().equals(halfLine.getName())) {
            context.getWriter().writeAttribute("name_" + side, context.getAnonymizer().anonymizeString(halfLine.getName()));
        }
        XmlUtil.writeDouble("r_" + side, halfLine.getR(), context.getWriter());
        XmlUtil.writeDouble("x_" + side, halfLine.getX(), context.getWriter());
        XmlUtil.writeDouble("g1_" + side, halfLine.getG1(), context.getWriter());
        XmlUtil.writeDouble("b1_" + side, halfLine.getB1(), context.getWriter());
        XmlUtil.writeDouble("g2_" + side, halfLine.getG2(), context.getWriter());
        XmlUtil.writeDouble("b2_" + side, halfLine.getB2(), context.getWriter());
        XmlUtil.writeDouble(XNODE_P + side, halfLine.getXnodeP(), context.getWriter());
        XmlUtil.writeDouble(XNODE_Q + side, halfLine.getXnodeQ(), context.getWriter());
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> {
            XmlUtil.writeDouble("xnodeV_" + side, halfLine.getXnodeV(), context.getWriter());
            XmlUtil.writeDouble("xnodeAngle_" + side, halfLine.getXnodeAngle(), context.getWriter());
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> XmlUtil.writeOptionalBoolean("fictitious_" + side, halfLine.isFictitious(), false, context.getWriter()));
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
            writeCurrentLimits(1, tl.getCurrentLimits1(), context.getWriter(), context.getVersion(), context.getOptions());
        }
        if (tl.getCurrentLimits2() != null) {
            writeCurrentLimits(2, tl.getCurrentLimits2(), context.getWriter(), context.getVersion(), context.getOptions());
        }
    }

    @Override
    protected TieLineAdder createAdder(Network n) {
        return n.newTieLine();
    }

    private static void readHalf(TieLineAdder.HalfLineAdder adder, NetworkXmlReaderContext context, int side) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id_" + side));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name_" + side));
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r_" + side);
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x_" + side);
        double g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1_" + side);
        double b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1_" + side);
        double g2 = XmlUtil.readDoubleAttribute(context.getReader(), "g2_" + side);
        double b2 = XmlUtil.readDoubleAttribute(context.getReader(), "b2_" + side);
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> adder
                .setXnodeP(XmlUtil.readDoubleAttribute(context.getReader(), XNODE_P + side))
                .setXnodeQ(XmlUtil.readDoubleAttribute(context.getReader(), XNODE_Q + side)));
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> adder
                .setXnodeP(XmlUtil.readOptionalDoubleAttribute(context.getReader(), XNODE_P + side))
                .setXnodeQ(XmlUtil.readOptionalDoubleAttribute(context.getReader(), XNODE_Q + side)));
        adder.setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> adder
                .setXnodeV(XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeV_" + side))
                .setXnodeAngle(XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeAngle_" + side)));

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious_" + side, false);
            adder.setFictitious(fictitious);
        });
        adder.add();
    }

    @Override
    protected TieLine readRootElementAttributes(TieLineAdder adder, NetworkXmlReaderContext context) {
        readHalf(adder.newHalfLine1(), context, 1);
        readHalf(adder.newHalfLine2(), context, 2);
        readNodeOrBus(adder, context);
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
        TieLine tl  = adder.setUcteXnodeCode(ucteXnodeCode)
                .add();
        readPQ(1, tl.getTerminal1(), context.getReader());
        readPQ(2, tl.getTerminal2(), context.getReader());
        double xnodeV1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeV_1");
        double xnodeV2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeV_2");
        double xnodeAngle1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeAngle_1");
        double xnodeAngle2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeAngle_2");
        double xnodeP1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_1");
        double xnodeP2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_2");
        double xnodeQ1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_1");
        double xnodeQ2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_2");
        context.getEndTasks().add(() -> {
            checkXnodeValue(xnodeV1, tl.getHalf1().getXnodeV(), "xnodeV1", tl.getId());
            checkXnodeValue(xnodeV2, tl.getHalf2().getXnodeV(), "xnodeV2", tl.getId());
            checkXnodeValue(xnodeAngle1, tl.getHalf1().getXnodeAngle(), "xnodeAngle1", tl.getId());
            checkXnodeValue(xnodeAngle2, tl.getHalf2().getXnodeAngle(), "xnodeAngle2", tl.getId());
            checkXnodeValue(xnodeP1, tl.getHalf1().getXnodeP(), "xnodeP1", tl.getId());
            checkXnodeValue(xnodeP2, tl.getHalf2().getXnodeP(), "xnodeP2", tl.getId());
            checkXnodeValue(xnodeQ1, tl.getHalf1().getXnodeQ(), "xnodeQ1", tl.getId());
            checkXnodeValue(xnodeQ2, tl.getHalf2().getXnodeQ(), "xnodeQ2", tl.getId());
        });
        return tl;
    }

    private static void checkXnodeValue(double imported, double calculated, String name, String tlId) {
        if (!Double.isNaN(imported) && imported != calculated) {
            LOGGER.warn("{} of TieLine {} is recalculated. Its imported value is not used (imported value = {}; calculated value = {})", name, tlId, imported, calculated);
        }
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
