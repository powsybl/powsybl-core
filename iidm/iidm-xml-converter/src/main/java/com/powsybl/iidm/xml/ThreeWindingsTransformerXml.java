/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerXml extends AbstractTransformerXml<ThreeWindingsTransformer, ThreeWindingsTransformerAdder> {

    static final ThreeWindingsTransformerXml INSTANCE = new ThreeWindingsTransformerXml();

    static final String ROOT_ELEMENT_NAME = "threeWindingsTransformer";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ThreeWindingsTransformer twt) {
        return twt.getLeg2().getRatioTapChanger() != null
                || twt.getLeg3().getRatioTapChanger() != null
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
        XmlUtil.writeDouble("ratedU2", twt.getLeg2().getRatedU(), context.getWriter());
        XmlUtil.writeDouble("r3", twt.getLeg3().getR(), context.getWriter());
        XmlUtil.writeDouble("x3", twt.getLeg3().getX(), context.getWriter());
        XmlUtil.writeDouble("ratedU3", twt.getLeg3().getRatedU(), context.getWriter());
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
        RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
        if (rtc2 != null) {
            writeRatioTapChanger("ratioTapChanger2", rtc2, context);
        }
        RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
        if (rtc3 != null) {
            writeRatioTapChanger("ratioTapChanger3", rtc3, context);
        }
        if (twt.getLeg1().getCurrentLimits() != null) {
            writeCurrentLimits(1, twt.getLeg1().getCurrentLimits(), context.getWriter());
        }
        if (twt.getLeg2().getCurrentLimits() != null) {
            writeCurrentLimits(2, twt.getLeg2().getCurrentLimits(), context.getWriter());
        }
        if (twt.getLeg3().getCurrentLimits() != null) {
            writeCurrentLimits(3, twt.getLeg3().getCurrentLimits(), context.getWriter());
        }
    }

    @Override
    protected ThreeWindingsTransformerAdder createAdder(Substation s) {
        return s.newThreeWindingsTransformer();
    }

    @Override
    protected ThreeWindingsTransformer readRootElementAttributes(ThreeWindingsTransformerAdder adder, NetworkXmlReaderContext context) {
        double r1 = XmlUtil.readDoubleAttribute(context.getReader(), "r1");
        double x1 = XmlUtil.readDoubleAttribute(context.getReader(), "x1");
        double g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1");
        double b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1");
        double ratedU1 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU1");
        double r2 = XmlUtil.readDoubleAttribute(context.getReader(), "r2");
        double x2 = XmlUtil.readDoubleAttribute(context.getReader(), "x2");
        double ratedU2 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU2");
        double r3 = XmlUtil.readDoubleAttribute(context.getReader(), "r3");
        double x3 = XmlUtil.readDoubleAttribute(context.getReader(), "x3");
        double ratedU3 = XmlUtil.readDoubleAttribute(context.getReader(), "ratedU3");
        LegAdder legAdder1 = adder.newLeg1().setR(r1).setX(x1).setG(g1).setB(b1).setRatedU(ratedU1);
        LegAdder legAdder2 = adder.newLeg2().setR(r2).setX(x2).setRatedU(ratedU2);
        LegAdder legAdder3 = adder.newLeg3().setR(r3).setX(x3).setRatedU(ratedU3);
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

                case "ratioTapChanger2":
                    readRatioTapChanger(2, tx.getLeg2(), context);
                    break;

                case "ratioTapChanger3":
                    readRatioTapChanger(3, tx.getLeg3(), context);
                    break;

                default:
                    super.readSubElements(tx, context);
            }
        });
    }
}
