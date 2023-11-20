/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import java.util.Optional;

import static com.powsybl.iidm.xml.ConnectableXmlUtil.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TwoWindingsTransformerXml extends AbstractTransformerXml<TwoWindingsTransformer, TwoWindingsTransformerAdder> {

    static final TwoWindingsTransformerXml INSTANCE = new TwoWindingsTransformerXml();

    static final String ROOT_ELEMENT_NAME = "twoWindingsTransformer";
    static final String ARRAY_ELEMENT_NAME = "twoWindingsTransformers";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(TwoWindingsTransformer twt, Substation s, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("r", twt.getR());
        context.getWriter().writeDoubleAttribute("x", twt.getX());
        context.getWriter().writeDoubleAttribute("g", twt.getG());
        context.getWriter().writeDoubleAttribute("b", twt.getB());
        context.getWriter().writeDoubleAttribute("ratedU1", twt.getRatedU1());
        context.getWriter().writeDoubleAttribute("ratedU2", twt.getRatedU2());
        writeRatedS("ratedS", twt.getRatedS(), context);
        writeNodeOrBus(1, twt.getTerminal1(), context);
        writeNodeOrBus(2, twt.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, twt.getTerminal1(), context.getWriter());
            writePQ(2, twt.getTerminal2(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(TwoWindingsTransformer twt, Substation s, NetworkXmlWriterContext context) {
        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            writeRatioTapChanger("ratioTapChanger", rtc, context);
        }
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            writePhaseTapChanger("phaseTapChanger", ptc, context);
        }
        Optional<ActivePowerLimits> activePowerLimits1 = twt.getActivePowerLimits1();
        if (activePowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(1, activePowerLimits1.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits1 = twt.getApparentPowerLimits1();
        if (apparentPowerLimits1.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(1, apparentPowerLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits1 = twt.getCurrentLimits1();
        if (currentLimits1.isPresent()) {
            writeCurrentLimits(1, currentLimits1.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
        Optional<ActivePowerLimits> activePowerLimits2 = twt.getActivePowerLimits2();
        if (activePowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(2, activePowerLimits2.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits2 = twt.getApparentPowerLimits2();
        if (apparentPowerLimits2.isPresent()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(2, apparentPowerLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits2 = twt.getCurrentLimits2();
        if (currentLimits2.isPresent()) {
            writeCurrentLimits(2, currentLimits2.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
    }

    @Override
    protected TwoWindingsTransformerAdder createAdder(Substation s) {
        return s.newTwoWindingsTransformer();
    }

    @Override
    protected TwoWindingsTransformer readRootElementAttributes(TwoWindingsTransformerAdder adder, Substation s, NetworkXmlReaderContext context) {
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
        readNodeOrBus(adder, context);
        TwoWindingsTransformer twt = adder.add();
        readPQ(1, twt.getTerminal1(), context.getReader());
        readPQ(2, twt.getTerminal2(), context.getReader());
        return twt;
    }

    @Override
    protected void readSubElements(TwoWindingsTransformer twt, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ACTIVE_POWER_LIMITS_1 -> {
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(twt.newActivePowerLimits1(), context.getReader()));
                }
                case APPARENT_POWER_LIMITS_1 -> {
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(twt.newApparentPowerLimits1(), context.getReader()));
                }
                case "currentLimits1" -> readCurrentLimits(twt.newCurrentLimits1(), context.getReader());
                case ACTIVE_POWER_LIMITS_2 -> {
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(twt.newActivePowerLimits2(), context.getReader()));
                }
                case APPARENT_POWER_LIMITS_2 -> {
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(twt.newApparentPowerLimits2(), context.getReader()));
                }
                case "currentLimits2" -> readCurrentLimits(twt.newCurrentLimits2(), context.getReader());
                case "ratioTapChanger" -> readRatioTapChanger(twt, context);
                case "phaseTapChanger" -> readPhaseTapChanger(twt, context);
                default -> readSubElement(elementName, twt, context);
            }
        });
    }
}
