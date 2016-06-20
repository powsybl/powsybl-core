/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerXml extends TransformerXml<TwoWindingsTransformer, TwoWindingsTransformerAdder> {

    static final TwoWindingsTransformerXml INSTANCE = new TwoWindingsTransformerXml();

    static final String ROOT_ELEMENT_NAME = "twoWindingsTransformer";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(TwoWindingsTransformer twt) {
        return twt.getRatioTapChanger() != null
                || twt.getPhaseTapChanger() != null
                || twt.getCurrentLimits1() != null
                || twt.getCurrentLimits2() != null;
    }

    @Override
    protected void writeRootElementAttributes(TwoWindingsTransformer twt, Substation s, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("r", twt.getR(), context.getWriter());
        XmlUtil.writeFloat("x", twt.getX(), context.getWriter());
        XmlUtil.writeFloat("g", twt.getG(), context.getWriter());
        XmlUtil.writeFloat("b", twt.getB(), context.getWriter());
        XmlUtil.writeFloat("ratedU1", twt.getRatedU1(), context.getWriter());
        XmlUtil.writeFloat("ratedU2", twt.getRatedU2(), context.getWriter());
        writeNodeOrBus(1, twt.getTerminal1(), context);
        writeNodeOrBus(2, twt.getTerminal2(), context);
        if (context.getOptions().isWithBranchSV()) {
            writePQ(1, twt.getTerminal1(), context.getWriter());
            writePQ(2, twt.getTerminal2(), context.getWriter());
        }
    }

    @Override
    protected void writeSubElements(TwoWindingsTransformer twt, Substation s, XmlWriterContext context) throws XMLStreamException {
        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            writeRatioTapChanger("ratioTapChanger", rtc, context);
        }
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            writePhaseTapChanger("phaseTapChanger", ptc, context);
        }
        if (twt.getCurrentLimits1() != null) {
            writeCurrentLimits(1, twt.getCurrentLimits1(), context.getWriter());
        }
        if (twt.getCurrentLimits2() != null) {
            writeCurrentLimits(2, twt.getCurrentLimits2(), context.getWriter());
        }
    }

    @Override
    protected TwoWindingsTransformerAdder createAdder(Substation s) {
        return s.newTwoWindingsTransformer();
    }

    @Override
    protected TwoWindingsTransformer readRootElementAttributes(TwoWindingsTransformerAdder adder, XMLStreamReader reader, List<Runnable> endTasks) {
        float r = XmlUtil.readFloatAttribute(reader, "r");
        float x = XmlUtil.readFloatAttribute(reader, "x");
        float g = XmlUtil.readFloatAttribute(reader, "g");
        float b = XmlUtil.readFloatAttribute(reader, "b");
        float ratedU1 = XmlUtil.readFloatAttribute(reader, "ratedU1");
        float ratedU2 = XmlUtil.readFloatAttribute(reader, "ratedU2");
        adder.setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2);
        readNodeOrBus(adder, reader);
        TwoWindingsTransformer twt = adder.add();
        readPQ(1, twt.getTerminal1(), reader);
        readPQ(2, twt.getTerminal2(), reader);
        return twt;
    }

    @Override
    protected void readSubElements(TwoWindingsTransformer twt, XMLStreamReader reader, List<Runnable> endTasks) throws XMLStreamException {
        readUntilEndRootElement(reader, () -> {
            switch (reader.getLocalName()) {
                case "currentLimits1":
                    readCurrentLimits(1, twt::newCurrentLimits1, reader);
                    break;

                case "currentLimits2":
                    readCurrentLimits(2, twt::newCurrentLimits2, reader);
                    break;

                case "ratioTapChanger":
                    readRatioTapChanger(twt, reader, endTasks);
                    break;

                case "phaseTapChanger":
                    readPhaseTapChanger(twt, reader, endTasks);
                    break;

                default:
                    super.readSubElements(twt, reader, endTasks);
            }
        });
    }
}