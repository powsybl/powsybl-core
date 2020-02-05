/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

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
    protected boolean hasSubElements(ThreeWindingsTransformer twt) {
        return twt.getLeg1().getRatioTapChanger() != null
                || twt.getLeg2().getRatioTapChanger() != null
                || twt.getLeg3().getRatioTapChanger() != null
                || twt.getLeg1().getPhaseTapChanger() != null
                || twt.getLeg2().getPhaseTapChanger() != null
                || twt.getLeg3().getPhaseTapChanger() != null
                || twt.getLeg1().getCurrentLimits() != null
                || twt.getLeg2().getCurrentLimits() != null
                || twt.getLeg3().getCurrentLimits() != null;
    }

    @Override
    protected void writeRootElementAttributes(ThreeWindingsTransformer twt, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("r1", twt.getLeg1().getR(), context.getWriter());
        XmlUtil.writeDouble("x1", twt.getLeg1().getX(), context.getWriter());
        XmlUtil.writeDouble("g1", twt.getLeg1().getG(), context.getWriter());
        XmlUtil.writeDouble("b1", twt.getLeg1().getB(), context.getWriter());
        XmlUtil.writeDouble("ratedU1", twt.getLeg1().getRatedU(), context.getWriter());
        XmlUtil.writeDouble("r2", twt.getLeg2().getR(), context.getWriter());
        XmlUtil.writeDouble("x2", twt.getLeg2().getX(), context.getWriter());
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g2", twt.getLeg2().getG(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b2", twt.getLeg2().getB(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        XmlUtil.writeDouble("ratedU2", twt.getLeg2().getRatedU(), context.getWriter());
        XmlUtil.writeDouble("r3", twt.getLeg3().getR(), context.getWriter());
        XmlUtil.writeDouble("x3", twt.getLeg3().getX(), context.getWriter());
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g3", twt.getLeg3().getG(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b3", twt.getLeg3().getB(), 0,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        XmlUtil.writeDouble("ratedU3", twt.getLeg3().getRatedU(), context.getWriter());
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
    protected void writeSubElements(ThreeWindingsTransformer twt, Substation s, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.assertMinimumVersionIfNotDefault(twt.getLeg1().getRatioTapChanger() != null, ROOT_ELEMENT_NAME, RATIO_TAP_CHANGER_1,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        writeRatioTapChanger(twt.getLeg1().getRatioTapChanger(), 1, context);
        IidmXmlUtil.assertMinimumVersionIfNotDefault(twt.getLeg1().getPhaseTapChanger() != null, ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_1,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        writePhaseTapChanger(twt.getLeg1().getPhaseTapChanger(), 1, context);
        writeRatioTapChanger(twt.getLeg2().getRatioTapChanger(), 2, context);
        IidmXmlUtil.assertMinimumVersionIfNotDefault(twt.getLeg2().getPhaseTapChanger() != null, ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_2,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        writePhaseTapChanger(twt.getLeg2().getPhaseTapChanger(), 2, context);
        writeRatioTapChanger(twt.getLeg3().getRatioTapChanger(), 3, context);
        IidmXmlUtil.assertMinimumVersionIfNotDefault(twt.getLeg3().getPhaseTapChanger() != null, ROOT_ELEMENT_NAME, PHASE_TAP_CHANGER_3,
                IidmXmlUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        writePhaseTapChanger(twt.getLeg3().getPhaseTapChanger(), 3, context);
        if (twt.getLeg1().getCurrentLimits() != null) {
            writeCurrentLimits(1, twt.getLeg1().getCurrentLimits(), context.getWriter(), context.getVersion());
        }
        if (twt.getLeg2().getCurrentLimits() != null) {
            writeCurrentLimits(2, twt.getLeg2().getCurrentLimits(), context.getWriter(), context.getVersion());
        }
        if (twt.getLeg3().getCurrentLimits() != null) {
            writeCurrentLimits(3, twt.getLeg3().getCurrentLimits(), context.getWriter(), context.getVersion());
        }
    }

    private static void writeRatioTapChanger(RatioTapChanger rtc, int index, NetworkXmlWriterContext context) throws XMLStreamException {
        if (rtc != null) {
            writeRatioTapChanger("ratioTapChanger" + index, rtc, context);
        }
    }

    private static void writePhaseTapChanger(PhaseTapChanger ptc, int index, NetworkXmlWriterContext context) throws XMLStreamException {
        if (ptc != null) {
            writePhaseTapChanger("phaseTapChanger" + index, ptc, context);
        }
    }

    @Override
    protected ThreeWindingsTransformerAdder createAdder(Substation s) {
        return s.newThreeWindingsTransformer();
    }

    @Override
    protected ThreeWindingsTransformer readRootElementAttributes(ThreeWindingsTransformerAdder adder, NetworkXmlReaderContext context) {
        double ratedU0 = IidmXmlUtil.readDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "ratedU0", IidmXmlVersion.V_1_1, context);
        adder.setRatedU0(ratedU0);
        double r1 = XmlUtil.readDoubleAttribute(context.getReader(), "r1");
        double x1 = XmlUtil.readDoubleAttribute(context.getReader(), "x1");
        double g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1");
        double b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1");
        double ratedU1 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU1");
        double r2 = XmlUtil.readDoubleAttribute(context.getReader(), "r2");
        double x2 = XmlUtil.readDoubleAttribute(context.getReader(), "x2");
        double g2 = IidmXmlUtil.readDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g2", 0, IidmXmlVersion.V_1_1, context);
        double b2 = IidmXmlUtil.readDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b2", 0, IidmXmlVersion.V_1_1, context);
        double ratedU2 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU2");
        double r3 = XmlUtil.readDoubleAttribute(context.getReader(), "r3");
        double x3 = XmlUtil.readDoubleAttribute(context.getReader(), "x3");
        double g3 = IidmXmlUtil.readDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "g3", 0, IidmXmlVersion.V_1_1, context);
        double b3 = IidmXmlUtil.readDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "b3", 0, IidmXmlVersion.V_1_1, context);
        double ratedU3 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU3");
        LegAdder legAdder1 = adder.newLeg1().setR(r1).setX(x1).setG(g1).setB(b1).setRatedU(ratedU1);
        LegAdder legAdder2 = adder.newLeg2().setR(r2).setX(x2).setG(g2).setB(b2).setRatedU(ratedU2);
        LegAdder legAdder3 = adder.newLeg3().setR(r3).setX(x3).setG(g3).setB(b3).setRatedU(ratedU3);
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
    protected void readSubElements(ThreeWindingsTransformer tx, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "currentLimits1":
                    readCurrentLimits(1, tx.getLeg1()::newCurrentLimits, context.getReader());
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, tx.getLeg2()::newCurrentLimits, context.getReader());
                    break;

                case "currentLimits3":
                    readCurrentLimits(3, tx.getLeg3()::newCurrentLimits, context.getReader());
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
