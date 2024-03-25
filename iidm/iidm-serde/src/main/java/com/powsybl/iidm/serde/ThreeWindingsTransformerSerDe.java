/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ThreeWindingsTransformerSerDe extends AbstractTransformerSerDe<ThreeWindingsTransformer, ThreeWindingsTransformerAdder> {

    static final ThreeWindingsTransformerSerDe INSTANCE = new ThreeWindingsTransformerSerDe();

    static final String ROOT_ELEMENT_NAME = "threeWindingsTransformer";
    static final String ARRAY_ELEMENT_NAME = "threeWindingsTransformers";

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
    protected void writeRootElementAttributes(ThreeWindingsTransformer twt, Substation s, NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute("r1", twt.getLeg1().getR());
        context.getWriter().writeDoubleAttribute("x1", twt.getLeg1().getX());
        context.getWriter().writeDoubleAttribute("g1", twt.getLeg1().getG());
        context.getWriter().writeDoubleAttribute("b1", twt.getLeg1().getB());
        context.getWriter().writeDoubleAttribute("ratedU1", twt.getLeg1().getRatedU());
        writeRatedS("ratedS1", twt.getLeg1().getRatedS(), context);
        context.getWriter().writeDoubleAttribute("r2", twt.getLeg2().getR());
        context.getWriter().writeDoubleAttribute("x2", twt.getLeg2().getX());
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g2", twt.getLeg2().getG(), 0,
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_1, context);
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b2", twt.getLeg2().getB(), 0,
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_1, context);
        context.getWriter().writeDoubleAttribute("ratedU2", twt.getLeg2().getRatedU());
        writeRatedS("ratedS2", twt.getLeg2().getRatedS(), context);
        context.getWriter().writeDoubleAttribute("r3", twt.getLeg3().getR());
        context.getWriter().writeDoubleAttribute("x3", twt.getLeg3().getX());
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g3", twt.getLeg3().getG(), 0,
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_1, context);
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b3", twt.getLeg3().getB(), 0,
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_1, context);
        context.getWriter().writeDoubleAttribute("ratedU3", twt.getLeg3().getRatedU());
        writeRatedS("ratedS3", twt.getLeg3().getRatedS(), context);
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "ratedU0", twt.getRatedU0(), twt.getLeg1().getRatedU(),
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_1, context);
        writeNodeOrBus(1, twt.getLeg1().getTerminal(), context);
        writeNodeOrBus(2, twt.getLeg2().getTerminal(), context);
        writeNodeOrBus(3, twt.getLeg3().getTerminal(), context);
        writeOptionalPQ(1, twt.getLeg1().getTerminal(), context.getWriter(), context.getOptions()::isWithBranchSV);
        writeOptionalPQ(2, twt.getLeg2().getTerminal(), context.getWriter(), context.getOptions()::isWithBranchSV);
        writeOptionalPQ(3, twt.getLeg3().getTerminal(), context.getWriter(), context.getOptions()::isWithBranchSV);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            writeSelectedGroupId(1, twt.getLeg1().getSelectedOperationalLimitsGroupId().orElse(null), context.getWriter());
            writeSelectedGroupId(2, twt.getLeg2().getSelectedOperationalLimitsGroupId().orElse(null), context.getWriter());
            writeSelectedGroupId(3, twt.getLeg3().getSelectedOperationalLimitsGroupId().orElse(null), context.getWriter());
        });
    }

    @Override
    protected void writeSubElements(ThreeWindingsTransformer twt, Substation s, NetworkSerializerContext context) {
        IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg1().hasRatioTapChanger(), ROOT_ELEMENT_NAME, RATIO_TAP_CHANGER_1,
                IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_1, context, () -> writeRatioTapChanger(twt.getLeg1().getRatioTapChanger(), 1, context));
        IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg1().hasPhaseTapChanger(), ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_1,
                IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_1, context, () -> writePhaseTapChanger(twt.getLeg1().getPhaseTapChanger(), 1, context));
        writeRatioTapChanger(twt.getLeg2().getRatioTapChanger(), 2, context);
        IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg2().hasPhaseTapChanger(), ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_2,
                IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_1, context, () -> writePhaseTapChanger(twt.getLeg2().getPhaseTapChanger(), 2, context));
        writeRatioTapChanger(twt.getLeg3().getRatioTapChanger(), 3, context);
        IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(twt.getLeg3().hasPhaseTapChanger(), ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_3,
                IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_1, context, () -> writePhaseTapChanger(twt.getLeg3().getPhaseTapChanger(), 3, context));
        int[] i = new int[1];
        i[0] = 1;
        for (ThreeWindingsTransformer.Leg leg : twt.getLegs()) {
            writeLimits(context, i[0], ROOT_ELEMENT_NAME, leg.getSelectedOperationalLimitsGroup().orElse(null), leg.getOperationalLimitsGroups());
            i[0]++;
        }
    }

    private static void writeRatioTapChanger(RatioTapChanger rtc, int index, NetworkSerializerContext context) {
        if (rtc != null) {
            writeRatioTapChanger("ratioTapChanger" + index, rtc, context);
        }
    }

    private static void writePhaseTapChanger(PhaseTapChanger ptc, int index, NetworkSerializerContext context) {
        if (ptc != null) {
            writePhaseTapChanger("phaseTapChanger" + index, ptc, context);
        }
    }

    @Override
    protected ThreeWindingsTransformerAdder createAdder(Substation s) {
        return s.newThreeWindingsTransformer();
    }

    @Override
    protected ThreeWindingsTransformer readRootElementAttributes(ThreeWindingsTransformerAdder adder, Substation s, NetworkDeserializerContext context) {
        LegAdder legAdder1 = adder.newLeg1();
        LegAdder legAdder2 = adder.newLeg2();
        LegAdder legAdder3 = adder.newLeg3();

        double r1 = context.getReader().readDoubleAttribute("r1");
        double x1 = context.getReader().readDoubleAttribute("x1");
        double g1 = context.getReader().readDoubleAttribute("g1");
        double b1 = context.getReader().readDoubleAttribute("b1");
        double ratedU1 = context.getReader().readDoubleAttribute("ratedU1");
        legAdder1.setR(r1).setX(x1).setG(g1).setB(b1).setRatedU(ratedU1);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> readRatedS("ratedS1", context, legAdder1::setRatedS));

        double r2 = context.getReader().readDoubleAttribute("r2");
        double x2 = context.getReader().readDoubleAttribute("x2");
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_1, context, () -> {
            double g2 = context.getReader().readDoubleAttribute("g2");
            double b2 = context.getReader().readDoubleAttribute("b2");
            legAdder2.setG(g2).setB(b2);
        });
        double ratedU2 = context.getReader().readDoubleAttribute("ratedU2");
        legAdder2.setR(r2).setX(x2).setRatedU(ratedU2);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> readRatedS("ratedS2", context, legAdder2::setRatedS));

        double r3 = context.getReader().readDoubleAttribute("r3");
        double x3 = context.getReader().readDoubleAttribute("x3");
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_1, context, () -> {
            double g3 = context.getReader().readDoubleAttribute("g3");
            double b3 = context.getReader().readDoubleAttribute("b3");
            legAdder3.setG(g3).setB(b3);
        });
        double ratedU3 = context.getReader().readDoubleAttribute("ratedU3");
        legAdder3.setR(r3).setX(x3).setRatedU(ratedU3);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> readRatedS("ratedS3", context, legAdder3::setRatedS));

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_1, context, () -> {
            double ratedU0 = context.getReader().readDoubleAttribute("ratedU0");
            adder.setRatedU0(ratedU0);
        });

        readNodeOrBus(1, legAdder1, s.getNetwork(), context);
        readNodeOrBus(2, legAdder2, s.getNetwork(), context);
        readNodeOrBus(3, legAdder3, s.getNetwork(), context);
        legAdder1.add();
        legAdder2.add();
        legAdder3.add();
        ThreeWindingsTransformer twt = adder.add();
        readOptionalPQ(1, twt.getLeg1().getTerminal(), context.getReader());
        readOptionalPQ(2, twt.getLeg2().getTerminal(), context.getReader());
        readOptionalPQ(3, twt.getLeg3().getTerminal(), context.getReader());
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            readSelectedGroupId(1, twt.getLeg1()::setSelectedOperationalLimitsGroup, context);
            readSelectedGroupId(2, twt.getLeg2()::setSelectedOperationalLimitsGroup, context);
            readSelectedGroupId(3, twt.getLeg3()::setSelectedOperationalLimitsGroup, context);
        });
        return twt;
    }

    @Override
    protected void readSubElements(ThreeWindingsTransformer tx, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case LIMITS_GROUP_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, LIMITS_GROUP_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroups(tx.getLeg1(), LIMITS_GROUP_1, context.getReader(), context.getVersion(), context.getOptions()));
                }
                case ACTIVE_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(tx.getLeg1().newActivePowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case APPARENT_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(tx.getLeg1().newApparentPowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case "currentLimits1" -> readCurrentLimits(tx.getLeg1().newCurrentLimits(), context.getReader(), context.getVersion(), context.getOptions());
                case LIMITS_GROUP_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, LIMITS_GROUP_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroups(tx.getLeg2(), LIMITS_GROUP_2, context.getReader(), context.getVersion(), context.getOptions()));
                }
                case ACTIVE_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(tx.getLeg2().newActivePowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case APPARENT_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(tx.getLeg2().newApparentPowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case "currentLimits2" -> readCurrentLimits(tx.getLeg2().newCurrentLimits(), context.getReader(), context.getVersion(), context.getOptions());
                case LIMITS_GROUP_3 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, LIMITS_GROUP_3, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroups(tx.getLeg3(), LIMITS_GROUP_3, context.getReader(), context.getVersion(), context.getOptions()));
                }
                case ACTIVE_POWER_LIMITS_3 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_3, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(tx.getLeg3().newActivePowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case APPARENT_POWER_LIMITS_3 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_3, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(tx.getLeg3().newApparentPowerLimits(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case "currentLimits3" -> readCurrentLimits(tx.getLeg3().newCurrentLimits(), context.getReader(), context.getVersion(), context.getOptions());
                case RATIO_TAP_CHANGER_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, RATIO_TAP_CHANGER_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
                    readRatioTapChanger(1, tx.getLeg1(), context);
                }
                case PHASE_TAP_CHANGER_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
                    readPhaseTapChanger(1, tx.getLeg1(), context);
                }
                case RATIO_TAP_CHANGER_2 -> readRatioTapChanger(2, tx.getLeg2(), context);
                case PHASE_TAP_CHANGER_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
                    readPhaseTapChanger(2, tx.getLeg2(), context);
                }
                case RATIO_TAP_CHANGER_3 -> readRatioTapChanger(3, tx.getLeg3(), context);
                case PHASE_TAP_CHANGER_3 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_3, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
                    readPhaseTapChanger(3, tx.getLeg3(), context);
                }
                default -> readSubElement(elementName, tx, context);
            }
        });
    }
}
