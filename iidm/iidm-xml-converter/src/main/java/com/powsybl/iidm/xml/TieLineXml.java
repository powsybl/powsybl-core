/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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

    private static void writeDanglingLine(DanglingLine danglingLine, NetworkXmlWriterContext context, int side) {
        Boundary boundary = danglingLine.getBoundary();
        context.getWriter().writeStringAttribute("id_" + side, context.getAnonymizer().anonymizeString(danglingLine.getId()));
        danglingLine.getOptionalName().ifPresent(name -> context.getWriter().writeStringAttribute("name_" + side, context.getAnonymizer().anonymizeString(name)));
        context.getWriter().writeDoubleAttribute("r_" + side, danglingLine.getR());
        context.getWriter().writeDoubleAttribute("x_" + side, danglingLine.getX());
        // TODO change serialization
        context.getWriter().writeDoubleAttribute("g1_" + side, danglingLine.getG() / 2);
        context.getWriter().writeDoubleAttribute("b1_" + side, danglingLine.getB() / 2);
        context.getWriter().writeDoubleAttribute("g2_" + side, danglingLine.getG() / 2);
        context.getWriter().writeDoubleAttribute("b2_" + side, danglingLine.getB() / 2);
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            context.getWriter().writeDoubleAttribute("xnodeP_" + side, boundary.getP());
            context.getWriter().writeDoubleAttribute("xnodeQ_" + side, boundary.getQ());
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> context.getWriter().writeBooleanAttribute("fictitious_" + side, danglingLine.isFictitious(), false));
    }

    @Override
    protected void writeRootElementAttributes(TieLine tl, Network n, NetworkXmlWriterContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {
            context.getWriter().writeStringAttribute("danglingLineId1", context.getAnonymizer().anonymizeString(tl.getDanglingLine1().getId()));
            context.getWriter().writeStringAttribute("danglingLineId2", context.getAnonymizer().anonymizeString(tl.getDanglingLine2().getId()));
        });
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            if (tl.getUcteXnodeCode() != null) {
                context.getWriter().writeStringAttribute("ucteXnodeCode", tl.getUcteXnodeCode());
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
    protected void writeSubElements(TieLine tl, Network n, NetworkXmlWriterContext context) {
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

    private static String readDanglingLine(Network network, NetworkXmlReaderContext context, int side) {
        String voltageLevelId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("voltageLevelId" + side));
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id_" + side));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("name_" + side));
        double r = context.getReader().readDoubleAttribute("r_" + side);
        double x = context.getReader().readDoubleAttribute("x_" + side);
        double g1 = context.getReader().readDoubleAttribute("g1_" + side);
        double b1 = context.getReader().readDoubleAttribute("b1_" + side);
        double g2 = context.getReader().readDoubleAttribute("g2_" + side);
        double b2 = context.getReader().readDoubleAttribute("b2_" + side);
        String ucteXnodeCode = context.getReader().readStringAttribute("ucteXnodeCode");
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
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious_" + side, false);
            adder.setFictitious(fictitious);
        });
        DanglingLine dl = adder.add();
        return dl.getId();
    }

    @Override
    protected TieLine readRootElementAttributes(TieLineAdder adder, Network network, NetworkXmlReaderContext context) {
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> adder.setDanglingLine1(readDanglingLine(network, context, 1)).setDanglingLine2(readDanglingLine(network, context, 2)));
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_10, context, () -> {
            String dl1Id = context.getReader().readStringAttribute("danglingLineId1");
            String dl2Id = context.getReader().readStringAttribute("danglingLineId2");
            adder.setDanglingLine1(dl1Id).setDanglingLine2(dl2Id);
        });
        TieLine tl = adder.add();
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_4, context, () -> {
            double half1BoundaryP = context.getReader().readDoubleAttribute("xnodeP_1");
            double half2BoundaryP = context.getReader().readDoubleAttribute("xnodeP_2");
            double half1BoundaryQ = context.getReader().readDoubleAttribute("xnodeQ_1");
            double half2BoundaryQ = context.getReader().readDoubleAttribute("xnodeQ_2");
            checkBoundaryValue(half1BoundaryP, tl.getDanglingLine1().getBoundary().getP(), "xnodeP_1", tl.getId());
            checkBoundaryValue(half2BoundaryP, tl.getDanglingLine2().getBoundary().getP(), "xnodeP_2", tl.getId());
            checkBoundaryValue(half1BoundaryQ, tl.getDanglingLine1().getBoundary().getQ(), "xnodeQ_1", tl.getId());
            checkBoundaryValue(half2BoundaryQ, tl.getDanglingLine2().getBoundary().getQ(), "xnodeQ_2", tl.getId());
        });
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_9, context, () -> {
            double p1 = context.getReader().readDoubleAttribute("p1");
            double q1 = context.getReader().readDoubleAttribute("q1");
            double p2 = context.getReader().readDoubleAttribute("p2");
            double q2 = context.getReader().readDoubleAttribute("q2");
            tl.getDanglingLine1().getTerminal().setP(p1).setQ(q1);
            tl.getDanglingLine2().getTerminal().setP(p2).setQ(q2);
        });
        return tl;
    }

    @Override
    protected void readSubElements(TieLine tl, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(getRootElementName(), () -> {
            switch (context.getReader().getNodeName()) {
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
