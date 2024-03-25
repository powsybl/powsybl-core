/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TwoWindingsTransformerSerDe extends AbstractTransformerSerDe<TwoWindingsTransformer, TwoWindingsTransformerAdder> {

    static final TwoWindingsTransformerSerDe INSTANCE = new TwoWindingsTransformerSerDe();

    static final String ROOT_ELEMENT_NAME = "twoWindingsTransformer";
    static final String ARRAY_ELEMENT_NAME = "twoWindingsTransformers";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(TwoWindingsTransformer twt, Substation s, NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute("r", twt.getR());
        context.getWriter().writeDoubleAttribute("x", twt.getX());
        context.getWriter().writeDoubleAttribute("g", twt.getG());
        context.getWriter().writeDoubleAttribute("b", twt.getB());
        context.getWriter().writeDoubleAttribute("ratedU1", twt.getRatedU1());
        context.getWriter().writeDoubleAttribute("ratedU2", twt.getRatedU2());
        writeRatedS("ratedS", twt.getRatedS(), context);
        writeNodeOrBus(1, twt.getTerminal1(), context);
        writeNodeOrBus(2, twt.getTerminal2(), context);
        writeOptionalPQ(1, twt.getTerminal1(), context.getWriter(), context.getOptions()::isWithBranchSV);
        writeOptionalPQ(2, twt.getTerminal2(), context.getWriter(), context.getOptions()::isWithBranchSV);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            writeSelectedGroupId(1, twt.getSelectedOperationalLimitsGroupId1().orElse(null), context.getWriter());
            writeSelectedGroupId(2, twt.getSelectedOperationalLimitsGroupId2().orElse(null), context.getWriter());
        });
    }

    @Override
    protected void writeSubElements(TwoWindingsTransformer twt, Substation s, NetworkSerializerContext context) {
        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            writeRatioTapChanger("ratioTapChanger", rtc, context);
        }
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            writePhaseTapChanger("phaseTapChanger", ptc, context);
        }
        writeLimits(context, 1, ROOT_ELEMENT_NAME, twt.getSelectedOperationalLimitsGroup1().orElse(null), twt.getOperationalLimitsGroups1());
        writeLimits(context, 2, ROOT_ELEMENT_NAME, twt.getSelectedOperationalLimitsGroup2().orElse(null), twt.getOperationalLimitsGroups2());
    }

    @Override
    protected TwoWindingsTransformerAdder createAdder(Substation s) {
        return s.newTwoWindingsTransformer();
    }

    @Override
    protected TwoWindingsTransformer readRootElementAttributes(TwoWindingsTransformerAdder adder, Substation s, NetworkDeserializerContext context) {
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        double g = context.getReader().readDoubleAttribute("g");
        double b = context.getReader().readDoubleAttribute("b");
        double ratedU1 = context.getReader().readDoubleAttribute("ratedU1");
        double ratedU2 = context.getReader().readDoubleAttribute("ratedU2");
        adder.setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2);
        readRatedS("ratedS", context, adder::setRatedS);
        ConnectableSerDeUtil.readVoltageLevelAndNodeOrBus(adder, s.getNetwork(), context);
        TwoWindingsTransformer twt = adder.add();
        readOptionalPQ(1, twt.getTerminal1(), context.getReader());
        readOptionalPQ(2, twt.getTerminal2(), context.getReader());
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            readSelectedGroupId(1, twt::setSelectedOperationalLimitsGroup1, context);
            readSelectedGroupId(2, twt::setSelectedOperationalLimitsGroup2, context);
        });
        return twt;
    }

    @Override
    protected void readSubElements(TwoWindingsTransformer twt, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case LIMITS_GROUP_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, LIMITS_GROUP_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroup(twt::newOperationalLimitsGroup1, LIMITS_GROUP_1, context.getReader(), context.getVersion(), context.getOptions()));
                }
                case ACTIVE_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(twt.newActivePowerLimits1(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case APPARENT_POWER_LIMITS_1 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(twt.newApparentPowerLimits1(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case "currentLimits1" -> readCurrentLimits(twt.newCurrentLimits1(), context.getReader(), context.getVersion(), context.getOptions());
                case LIMITS_GROUP_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, LIMITS_GROUP_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroup(twt::newOperationalLimitsGroup2, LIMITS_GROUP_2, context.getReader(), context.getVersion(), context.getOptions()));
                }
                case ACTIVE_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(twt.newActivePowerLimits2(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case APPARENT_POWER_LIMITS_2 -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(twt.newApparentPowerLimits2(), context.getReader(), context.getVersion(), context.getOptions()));
                }
                case "currentLimits2" -> readCurrentLimits(twt.newCurrentLimits2(), context.getReader(), context.getVersion(), context.getOptions());
                case "ratioTapChanger" -> readRatioTapChanger(twt, context);
                case "phaseTapChanger" -> readPhaseTapChanger(twt, context);
                default -> readSubElement(elementName, twt, context);
            }
        });
    }
}
