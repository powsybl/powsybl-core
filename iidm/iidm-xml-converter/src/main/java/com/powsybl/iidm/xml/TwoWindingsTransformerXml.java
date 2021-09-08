/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerXml extends AbstractTransformerXml<TwoWindingsTransformer, TwoWindingsTransformerAdder> {

    static final TwoWindingsTransformerXml INSTANCE = new TwoWindingsTransformerXml();

    static final String ROOT_ELEMENT_NAME = "twoWindingsTransformer";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(TwoWindingsTransformer twt) {
        throw new AssertionError("Should not be called");
    }

    @Override
    protected boolean hasSubElements(TwoWindingsTransformer twt, NetworkXmlWriterContext context) {
        return hasValidOperationalLimits(twt, context) || twt.hasRatioTapChanger() || twt.hasPhaseTapChanger();
    }

    @Override
    protected void writeRootElementAttributes(TwoWindingsTransformer twt, Container<? extends Identifiable<?>> c, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("r", twt.getR(), context.getWriter());
        XmlUtil.writeDouble("x", twt.getX(), context.getWriter());
        XmlUtil.writeDouble("g", twt.getG(), context.getWriter());
        XmlUtil.writeDouble("b", twt.getB(), context.getWriter());
        XmlUtil.writeDouble("ratedU1", twt.getRatedU1(), context.getWriter());
        XmlUtil.writeDouble("ratedU2", twt.getRatedU2(), context.getWriter());
        writeRatedS("ratedS", twt.getRatedS(), context);
        writeNodeOrBus(1, twt.getTerminal1(), context);
        writeNodeOrBus(2, twt.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, twt.getTerminal1(), context.getWriter());
            writePQ(2, twt.getTerminal2(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(TwoWindingsTransformer twt, Container<? extends Identifiable<?>> c, NetworkXmlWriterContext context) throws XMLStreamException {
        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            writeRatioTapChanger("ratioTapChanger", rtc, context);
        }
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            writePhaseTapChanger("phaseTapChanger", ptc, context);
        }
        if (twt.getActivePowerLimits1() != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(1, twt.getActivePowerLimits1(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        if (twt.getApparentPowerLimits1() != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(1, twt.getApparentPowerLimits1(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        if (twt.getCurrentLimits1() != null) {
            writeCurrentLimits(1, twt.getCurrentLimits1(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
        if (twt.getActivePowerLimits2() != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeActivePowerLimits(2, twt.getActivePowerLimits2(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        if (twt.getApparentPowerLimits2() != null) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> writeApparentPowerLimits(2, twt.getApparentPowerLimits2(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        if (twt.getCurrentLimits2() != null) {
            writeCurrentLimits(2, twt.getCurrentLimits2(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
    }

    @Override
    protected TwoWindingsTransformerAdder createAdder(Container<? extends Identifiable<?>> c) {
        if (c instanceof Network) {
            return ((Network) c).newTwoWindingsTransformer();
        }
        if (c instanceof Substation) {
            return ((Substation) c).newTwoWindingsTransformer();
        }
        throw new AssertionError();
    }

    @Override
    protected TwoWindingsTransformer readRootElementAttributes(TwoWindingsTransformerAdder adder, NetworkXmlReaderContext context) {
        double r = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "r");
        double x = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "x");
        double g = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "g");
        double b = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "b");
        double ratedU1 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "ratedU1");
        double ratedU2 = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "ratedU2");
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
    protected void readSubElements(TwoWindingsTransformer twt, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ACTIVE_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(1, twt.newActivePowerLimits1(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_1:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_1, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(1, twt.newApparentPowerLimits1(), context.getReader()));
                    break;

                case "currentLimits1":
                    readCurrentLimits(1, twt.newCurrentLimits1(), context.getReader());
                    break;

                case ACTIVE_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readActivePowerLimits(2, twt.newActivePowerLimits2(), context.getReader()));
                    break;

                case APPARENT_POWER_LIMITS_2:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS_2, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_5, context);
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_5, context, () -> readApparentPowerLimits(2, twt.newApparentPowerLimits2(), context.getReader()));
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, twt.newCurrentLimits2(), context.getReader());
                    break;

                case "ratioTapChanger":
                    readRatioTapChanger(twt, context);
                    break;

                case "phaseTapChanger":
                    readPhaseTapChanger(twt, context);
                    break;

                default:
                    super.readSubElements(twt, context);
            }
        });
    }
}
