/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static com.powsybl.iidm.xml.ConnectableXmlUtil.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineXml extends AbstractSimpleIdentifiableXml<TieLine, TieLineAdder, Network> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TieLineXml.class);

    static final TieLineXml INSTANCE = new TieLineXml();

    static final String ROOT_ELEMENT_NAME = "tieLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(TieLine tl) {
        throw new IllegalStateException("Should not be called");
    }

    @Override
    protected boolean hasSubElements(TieLine tl, NetworkXmlWriterContext context) {
        return (hasValidOperationalLimits(tl.getDanglingLine1(), context) || hasValidOperationalLimits(tl.getDanglingLine2(), context)) && context.getVersion().compareTo(IidmXmlVersion.V_1_10) < 0;
    }

    private static void writeDanglingLine(DanglingLine danglingLine, NetworkXmlWriterContext context, int side) throws XMLStreamException {
        Boundary boundary = danglingLine.getBoundary();
        context.getWriter().writeAttribute("id_" + side, context.getAnonymizer().anonymizeString(danglingLine.getId()));
        danglingLine.getOptionalName().ifPresent(name -> {
            try {
                context.getWriter().writeAttribute("name_" + side, context.getAnonymizer().anonymizeString(name));
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
        XmlUtil.writeDouble("r_" + side, danglingLine.getR(), context.getWriter());
        XmlUtil.writeDouble("x_" + side, danglingLine.getX(), context.getWriter());
        // TODO change serialization
        XmlUtil.writeDouble("g1_" + side, danglingLine.getG() / 2, context.getWriter());
        XmlUtil.writeDouble("b1_" + side, danglingLine.getB() / 2, context.getWriter());
        XmlUtil.writeDouble("g2_" + side, danglingLine.getG() / 2, context.getWriter());
        XmlUtil.writeDouble("b2_" + side, danglingLine.getB() / 2, context.getWriter());
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            XmlUtil.writeDouble("xnodeP_" + side, boundary.getP(), context.getWriter());
            XmlUtil.writeDouble("xnodeQ_" + side, boundary.getQ(), context.getWriter());
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> XmlUtil.writeOptionalBoolean("fictitious_" + side, danglingLine.isFictitious(), false, context.getWriter()));
    }

    @Override
    protected void writeRootElementAttributes(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {
            context.getWriter().writeAttribute("danglingLineId1", context.getAnonymizer().anonymizeString(tl.getDanglingLine1().getId()));
            context.getWriter().writeAttribute("danglingLineId2", context.getAnonymizer().anonymizeString(tl.getDanglingLine2().getId()));
        });
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            if (tl.getUcteXnodeCode() != null) {
                context.getWriter().writeAttribute("ucteXnodeCode", tl.getUcteXnodeCode());
            }
            writeNodeOrBus(1, tl.getDanglingLine1().getTerminal(), context);
            writeNodeOrBus(2, tl.getDanglingLine2().getTerminal(), context);
            if (context.getOptions().isWithBranchSV()) {
                writePQ(1, tl.getDanglingLine1().getTerminal(), context.getWriter());
                writePQ(2, tl.getDanglingLine2().getTerminal(), context.getWriter());
            }
            writeDanglingLine(tl.getDanglingLine1(), context, 1);
            writeDanglingLine(tl.getDanglingLine2(), context, 2);
        });
    }

    @Override
    protected void writeSubElements(TieLine tl, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            Optional<ActivePowerLimits> activePowerLimits1 = tl.getDanglingLine1().getActivePowerLimits();
            if (activePowerLimits1.isPresent()) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(1, activePowerLimits1.get(), context.getWriter(),
                        context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<ApparentPowerLimits> apparentPowerLimits1 = tl.getDanglingLine1().getApparentPowerLimits();
            if (apparentPowerLimits1.isPresent()) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(1, apparentPowerLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<CurrentLimits> currentLimits1 = tl.getDanglingLine1().getCurrentLimits();
            if (currentLimits1.isPresent()) {
                writeCurrentLimits(1, currentLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
            }
            Optional<ActivePowerLimits> activePowerLimits2 = tl.getDanglingLine2().getActivePowerLimits();
            if (activePowerLimits2.isPresent()) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(2, activePowerLimits2.get(), context.getWriter(),
                        context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<ApparentPowerLimits> apparentPowerLimits2 = tl.getDanglingLine2().getApparentPowerLimits();
            if (apparentPowerLimits2.isPresent()) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(2, apparentPowerLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<CurrentLimits> currentLimits2 = tl.getDanglingLine2().getCurrentLimits();
            if (currentLimits2.isPresent()) {
                writeCurrentLimits(2, currentLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
            }
        });
    }

    @Override
    protected TieLineAdder createAdder(Network n) {
        return n.newTieLine();
    }

    @Override
    protected TieLine readRootElementAttributes(TieLineAdder adder, Network network, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> adder.setDanglingLine1(readHalf(network, context, 1)).setDanglingLine2(readHalf(network, context, 2)));
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {
            String dl1Id = context.getReader().getAttributeValue(null, "danglingLineId1");
            String dl2Id = context.getReader().getAttributeValue(null, "danglingLineId2");
            adder.setDanglingLine1(dl1Id).setDanglingLine2(dl2Id);
        });
        TieLine tl = adder.add();
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            double dl1BoundaryP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_1");
            double dl2BoundaryP = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeP_2");
            double dl1BoundaryQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_1");
            double dl2BoundaryQ = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "xnodeQ_2");
            checkBoundaryValue(dl1BoundaryP, tl.getDanglingLine1().getBoundary().getP(), "xnodeP_1", tl.getId());
            checkBoundaryValue(dl2BoundaryP, tl.getDanglingLine2().getBoundary().getP(), "xnodeP_2", tl.getId());
            checkBoundaryValue(dl1BoundaryQ, tl.getDanglingLine1().getBoundary().getQ(), "xnodeQ_1", tl.getId());
            checkBoundaryValue(dl2BoundaryQ, tl.getDanglingLine2().getBoundary().getQ(), "xnodeQ_2", tl.getId());
        });
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            double p1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p1");
            double q1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q1");
            double p2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p2");
            double q2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q2");
            tl.getDanglingLine1().getTerminal().setP(p1).setQ(q1);
            tl.getDanglingLine2().getTerminal().setP(p2).setQ(q2);
        });
        return tl;
    }

    private static String readHalf(Network network, NetworkXmlReaderContext context, int side) {
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "voltageLevelId" + side));
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id_" + side));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "name_" + side));
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r_" + side);
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x_" + side);
        double g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1_" + side);
        double b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1_" + side);
        double g2 = XmlUtil.readDoubleAttribute(context.getReader(), "g2_" + side);
        double b2 = XmlUtil.readDoubleAttribute(context.getReader(), "b2_" + side);
        String ucteXnodeCode = context.getReader().getAttributeValue(null, "ucteXnodeCode");
        DanglingLineAdder adder = network.getVoltageLevel(voltageLevelId).newDanglingLine().setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g1 + g2)
                .setB(b1 + b2)
                .setP0(0.0)
                .setQ0(0.0)
                .setUcteXnodeCode(ucteXnodeCode);
        readNodeOrBus(adder, String.valueOf(side), context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            boolean fictitious = XmlUtil.readOptionalBoolAttribute(context.getReader(), "fictitious_" + side, false);
            adder.setFictitious(fictitious);
        });
        DanglingLine dl = adder.add();
        return dl.getId();
    }

    @Override
    protected void readSubElements(TieLine tl, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ACTIVE_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(1, tl.getDanglingLine1().newActivePowerLimits(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(1, tl.getDanglingLine1().newApparentPowerLimits(), context.getReader()));
                    break;

                case "currentLimits1":
                    IidmXmlUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    readCurrentLimits(1, tl.getDanglingLine1().newCurrentLimits(), context.getReader());
                    break;

                case ACTIVE_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(2, tl.getDanglingLine2().newActivePowerLimits(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(2, tl.getDanglingLine2().newApparentPowerLimits(), context.getReader()));
                    break;

                case "currentLimits2":
                    IidmXmlUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_9, context);
                    readCurrentLimits(2, tl.getDanglingLine2().newCurrentLimits(), context.getReader());
                    break;

                default:
                    super.readSubElements(tl, context);
            }
        });
    }

    private static void checkBoundaryValue(double imported, double calculated, String name, String tlId) {
        if (!Double.isNaN(imported) && imported != calculated) {
            LOGGER.info("{} of TieLine {} is recalculated. Its imported value is not used (imported value = {}; calculated value = {})", name, tlId, imported, calculated);
        }
    }
}
