/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerXml extends AbstractTransformerXml<ThreeWindingsTransformer, ThreeWindingsTransformerAdder> {

    static final ThreeWindingsTransformerXml INSTANCE = new ThreeWindingsTransformerXml();

    static final String ROOT_ELEMENT_NAME = "threeWindingsTransformer";

    private static final String RATIO_TAP_CHANGER_1 = "ratioTapChanger1";
    private static final String PHASE_TAP_CHANGER_1 = "phaseTapChanger1";
    private static final String RATIO_TAP_CHANGER_2 = "ratioTapChanger2";
    private static final String PHASE_TAP_CHANGER_2 = "phaseTapChanger2";
    private static final String RATIO_TAP_CHANGER_3 = "ratioTapChanger3";
    private static final String PHASE_TAP_CHANGER_3 = "phaseTapChanger3";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(ThreeWindingsTransformer twt, Container<? extends Identifiable<?>> c, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("r1", twt.getLeg1().getR());
        context.getWriter().writeDoubleAttribute("x1", twt.getLeg1().getX());
        context.getWriter().writeDoubleAttribute("g1", twt.getLeg1().getG());
        context.getWriter().writeDoubleAttribute("b1", twt.getLeg1().getB());
        context.getWriter().writeDoubleAttribute("ratedU1", twt.getLeg1().getRatedU());
        writeRatedS("ratedS1", twt.getLeg1().getRatedS(), context);
        context.getWriter().writeDoubleAttribute("r2", twt.getLeg2().getR());
        context.getWriter().writeDoubleAttribute("x2", twt.getLeg2().getX());
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g2", twt.getLeg2().getG(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b2", twt.getLeg2().getB(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        context.getWriter().writeDoubleAttribute("ratedU2", twt.getLeg2().getRatedU());
        writeRatedS("ratedS2", twt.getLeg2().getRatedS(), context);
        context.getWriter().writeDoubleAttribute("r3", twt.getLeg3().getR());
        context.getWriter().writeDoubleAttribute("x3", twt.getLeg3().getX());
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g3", twt.getLeg3().getG(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b3", twt.getLeg3().getB(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        context.getWriter().writeDoubleAttribute("ratedU3", twt.getLeg3().getRatedU());
        writeRatedS("ratedS3", twt.getLeg3().getRatedS(), context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "ratedU0", twt.getRatedU0(), twt.getLeg1().getRatedU(),
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        writeNodeOrBus(1, twt.getLeg1().getTerminal(), context);
        writeNodeOrBus(2, twt.getLeg2().getTerminal(), context);
        writeNodeOrBus(3, twt.getLeg3().getTerminal(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, twt.getLeg1().getTerminal(), context.getWriter());
            writePQ(2, twt.getLeg2().getTerminal(), context.getWriter());
            writePQ(3, twt.getLeg3().getTerminal(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(ThreeWindingsTransformer twt, Container<? extends Identifiable<?>> c, NetworkXmlWriterContext context) {
        IidmXmlUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg1().hasRatioTapChanger(), ROOT_ELEMENT_NAME, RATIO_TAP_CHANGER_1,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context, () -> writeRatioTapChanger(twt.getLeg1().getRatioTapChanger(), 1, context));
        IidmXmlUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg1().hasPhaseTapChanger(), ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_1,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context, () -> writePhaseTapChanger(twt.getLeg1().getPhaseTapChanger(), 1, context));
        writeRatioTapChanger(twt.getLeg2().getRatioTapChanger(), 2, context);
        IidmXmlUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg2().hasPhaseTapChanger(), ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_2,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context, () -> writePhaseTapChanger(twt.getLeg2().getPhaseTapChanger(), 2, context));
        writeRatioTapChanger(twt.getLeg3().getRatioTapChanger(), 3, context);
        IidmXmlUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg3().hasPhaseTapChanger(), ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_3,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context, () -> writePhaseTapChanger(twt.getLeg3().getPhaseTapChanger(), 3, context));
        int[] i = new int[1];
        i[0] = 1;
        for (ThreeWindingsTransformer.Leg leg : twt.getLegs()) {
            Optional<ActivePowerLimits> activePowerLimits = leg.getActivePowerLimits();
            if (activePowerLimits.isPresent()) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(i[0], activePowerLimits.get(), context.getWriter(),
                        context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<ApparentPowerLimits> apparentPowerLimits = leg.getApparentPowerLimits();
            if (apparentPowerLimits.isPresent()) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(i[0], apparentPowerLimits.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
            }
            Optional<CurrentLimits> currentLimits = leg.getCurrentLimits();
            currentLimits.ifPresent(limits -> writeCurrentLimits(i[0], limits, context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
            i[0]++;
        }
    }

    private static void writeRatioTapChanger(RatioTapChanger rtc, int index, NetworkXmlWriterContext context) {
        if (rtc != null) {
            writeRatioTapChanger("ratioTapChanger" + index, rtc, context);
        }
    }

    private static void writePhaseTapChanger(PhaseTapChanger ptc, int index, NetworkXmlWriterContext context) {
        if (ptc != null) {
            writePhaseTapChanger("phaseTapChanger" + index, ptc, context);
        }
    }

    @Override
    protected ThreeWindingsTransformerAdder createAdder(Container<? extends Identifiable<?>> c) {
        if (c instanceof Network) {
            return ((Network) c).newThreeWindingsTransformer();
        }
        if (c instanceof Substation) {
            return ((Substation) c).newThreeWindingsTransformer();
        }
        throw new AssertionError();
    }

    @Override
    protected ThreeWindingsTransformer readRootElementAttributes(ThreeWindingsTransformerAdder adder, NetworkXmlReaderContext context) {
        double r1 = context.getReader().readDoubleAttribute("r1");
        double x1 = context.getReader().readDoubleAttribute("x1");
        double g1 = context.getReader().readDoubleAttribute("g1");
        double b1 = context.getReader().readDoubleAttribute("b1");
        double ratedU1 = context.getReader().readDoubleAttribute("ratedU1");
        double r2 = context.getReader().readDoubleAttribute("r2");
        double x2 = context.getReader().readDoubleAttribute("x2");
        double ratedU2 = context.getReader().readDoubleAttribute("ratedU2");
        double r3 = context.getReader().readDoubleAttribute("r3");
        double x3 = context.getReader().readDoubleAttribute("x3");
        double ratedU3 = context.getReader().readDoubleAttribute("ratedU3");
        LegAdder legAdder1 = adder.newLeg1().setR(r1).setX(x1).setG(g1).setB(b1).setRatedU(ratedU1);
        LegAdder legAdder2 = adder.newLeg2().setR(r2).setX(x2).setRatedU(ratedU2);
        LegAdder legAdder3 = adder.newLeg3().setR(r3).setX(x3).setRatedU(ratedU3);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_1, context, () -> {
            double ratedU0 = context.getReader().readDoubleAttribute("ratedU0");
            adder.setRatedU0(ratedU0);

            double g2 = context.getReader().readDoubleAttribute("g2");
            double b2 = context.getReader().readDoubleAttribute("b2");
            legAdder2.setG(g2).setB(b2);

            double g3 = context.getReader().readDoubleAttribute("g3");
            double b3 = context.getReader().readDoubleAttribute("b3");
            legAdder3.setG(g3).setB(b3);
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            readRatedS("ratedS1", context, legAdder1::setRatedS);
            readRatedS("ratedS2", context, legAdder2::setRatedS);
            readRatedS("ratedS3", context, legAdder3::setRatedS);
        });
        readNodeOrBus(1, legAdder1, context);
        readNodeOrBus(2, legAdder2, context);
        readNodeOrBus(3, legAdder3, context);
        legAdder1.add();
        legAdder2.add();
        legAdder3.add();
        ThreeWindingsTransformer twt = adder.add();
        readPQ(1, twt.getLeg1().getTerminal(), context.getReader());
        readPQ(2, twt.getLeg2().getTerminal(), context.getReader());
        readPQ(3, twt.getLeg3().getTerminal(), context.getReader());
        return twt;
    }

    @Override
    protected void readSubElements(ThreeWindingsTransformer tx, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(getRootElementName(), () -> {
            switch (context.getReader().getNodeName()) {
                case ACTIVE_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(1, tx.getLeg1().newActivePowerLimits(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(1, tx.getLeg1().newApparentPowerLimits(), context.getReader()));
                    break;

                case "currentLimits1":
                    readCurrentLimits(1, tx.getLeg1().newCurrentLimits(), context.getReader());
                    break;

                case ACTIVE_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(2, tx.getLeg2().newActivePowerLimits(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(2, tx.getLeg2().newApparentPowerLimits(), context.getReader()));
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, tx.getLeg2().newCurrentLimits(), context.getReader());
                    break;

                case ACTIVE_POWER_LIMITS_3:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_3, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(3, tx.getLeg3().newActivePowerLimits(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_3:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_3, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(3, tx.getLeg3().newApparentPowerLimits(), context.getReader()));
                    break;

                case "currentLimits3":
                    readCurrentLimits(3, tx.getLeg3().newCurrentLimits(), context.getReader());
                    break;

                case RATIO_TAP_CHANGER_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, RATIO_TAP_CHANGER_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
                    readRatioTapChanger(1, tx.getLeg1(), context);
                    break;

                case PHASE_TAP_CHANGER_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
                    readPhaseTapChanger(1, tx.getLeg1(), context);
                    break;

                case RATIO_TAP_CHANGER_2:
                    readRatioTapChanger(2, tx.getLeg2(), context);
                    break;

                case PHASE_TAP_CHANGER_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
                    readPhaseTapChanger(2, tx.getLeg2(), context);
                    break;

                case RATIO_TAP_CHANGER_3:
                    readRatioTapChanger(3, tx.getLeg3(), context);
                    break;

                case PHASE_TAP_CHANGER_3:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_3, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
                    readPhaseTapChanger(3, tx.getLeg3(), context);
                    break;

                default:
                    super.readSubElements(tx, context);
                    break;
            }
        });
    }
}
