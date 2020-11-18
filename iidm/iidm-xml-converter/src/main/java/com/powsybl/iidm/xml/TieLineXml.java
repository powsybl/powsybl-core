/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.OtherSide;
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

    private static final String BOUNDARY_POINT_P = "boundaryPointP_";
    private static final String BOUNDARY_POINT_Q = "boundaryPointQ_";

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
        OtherSide otherSide = halfLine.getOtherSide();
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
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            XmlUtil.writeDouble("xnodeP_" + side, otherSide.getP(), context.getWriter());
            XmlUtil.writeDouble("xnodeQ_" + side, otherSide.getQ(), context.getWriter());
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> {
            XmlUtil.writeDouble(BOUNDARY_POINT_P + side, otherSide.getP(), context.getWriter());
            XmlUtil.writeDouble(BOUNDARY_POINT_Q + side, otherSide.getQ(), context.getWriter());
            XmlUtil.writeDouble("boundaryPointV_" + side, otherSide.getV(), context.getWriter());
            XmlUtil.writeDouble("boundaryPointAngle_" + side, otherSide.getAngle(), context.getWriter());
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
        adder.setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setB1(b1)
                .setG2(g2)
                .setB2(b2);

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
        double boundaryPointV1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointV_1");
        double boundaryPointV2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointV_2");
        double boundaryPointAngle1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointAngle_1");
        double boundaryPointAngle2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointAngle_2");
        double[] boundaryPointP1 = new double[1];
        double[] boundaryPointP2 = new double[1];
        double[] boundaryPointQ1 = new double[1];
        double[] boundaryPointQ2 = new double[1];
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            boundaryPointP1[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_1");
            boundaryPointP2[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_2");
            boundaryPointQ1[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_1");
            boundaryPointQ2[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_2");
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> {
            boundaryPointP1[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointP_1");
            boundaryPointP2[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointP_2");
            boundaryPointQ1[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointQ_1");
            boundaryPointQ2[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "boundaryPointQ_2");
        });
        context.getEndTasks().add(() -> {
            checkBoundaryPointValue(boundaryPointV1, tl.getHalf1().getOtherSide().getV(), "boundaryPointV1", tl.getId());
            checkBoundaryPointValue(boundaryPointV2, tl.getHalf2().getOtherSide().getV(), "boundaryPointV2", tl.getId());
            checkBoundaryPointValue(boundaryPointAngle1, tl.getHalf1().getOtherSide().getAngle(), "boundaryPointAngle1", tl.getId());
            checkBoundaryPointValue(boundaryPointAngle2, tl.getHalf2().getOtherSide().getAngle(), "boundaryPointAngle2", tl.getId());
            checkBoundaryPointValue(boundaryPointP1[0], tl.getHalf1().getOtherSide().getP(), "boundaryPointP1", tl.getId());
            checkBoundaryPointValue(boundaryPointP2[0], tl.getHalf2().getOtherSide().getP(), "boundaryPointP2", tl.getId());
            checkBoundaryPointValue(boundaryPointQ1[0], tl.getHalf1().getOtherSide().getQ(), "boundaryPointQ1", tl.getId());
            checkBoundaryPointValue(boundaryPointQ2[0], tl.getHalf2().getOtherSide().getQ(), "boundaryPointQ2", tl.getId());
        });
        return tl;
    }

    private static void checkBoundaryPointValue(double imported, double calculated, String name, String tlId) {
        if (!Double.isNaN(imported) && imported != calculated) {
            LOGGER.info("{} of TieLine {} is recalculated. Its imported value is not used (imported value = {}; calculated value = {})", name, tlId, imported, calculated);
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
