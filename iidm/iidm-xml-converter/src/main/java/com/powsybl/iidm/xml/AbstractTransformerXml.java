/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTransformerXml<T extends Connectable, A extends IdentifiableAdder<A>> extends AbstractConnectableXml<T, A, Substation> {

    protected static void writeTapChangerStep(TapChangerStep<?> tcs, XMLStreamWriter writer) throws XMLStreamException {
        XmlUtil.writeFloat("r", tcs.getR(), writer);
        XmlUtil.writeFloat("x", tcs.getX(), writer);
        XmlUtil.writeFloat("g", tcs.getG(), writer);
        XmlUtil.writeFloat("b", tcs.getB(), writer);
        XmlUtil.writeFloat("rho", tcs.getRho(), writer);
    }

    protected static void writeTapChanger(TapChanger<?, ?> tc, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute("lowTapPosition", Integer.toString(tc.getLowTapPosition()));
        writer.writeAttribute("tapPosition", Integer.toString(tc.getTapPosition()));
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, name);
        writeTapChanger(rtc, context.getWriter());
        context.getWriter().writeAttribute("loadTapChangingCapabilities", Boolean.toString(rtc.hasLoadTapChangingCapabilities()));
        if (rtc.hasLoadTapChangingCapabilities() || rtc.isRegulating()) {
            context.getWriter().writeAttribute("regulating", Boolean.toString(rtc.isRegulating()));
        }
        if (rtc.hasLoadTapChangingCapabilities() || !Float.isNaN(rtc.getTargetV())) {
            XmlUtil.writeFloat("targetV", rtc.getTargetV(), context.getWriter());
        }
        if (rtc.hasLoadTapChangingCapabilities() || rtc.getRegulationTerminal() != null) {
            writeTerminalRef(rtc.getRegulationTerminal(), context, "terminalRef");
        }
        for (int p = rtc.getLowTapPosition(); p <= rtc.getHighTapPosition(); p++) {
            RatioTapChangerStep rtcs = rtc.getStep(p);
            context.getWriter().writeEmptyElement(IIDM_URI, "step");
            writeTapChangerStep(rtcs, context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readRatioTapChanger(String elementName, RatioTapChangerAdder adder, Terminal terminal, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), "lowTapPosition");
        int tapPosition = XmlUtil.readIntAttribute(context.getReader(), "tapPosition");
        boolean regulating = XmlUtil.readOptionalBoolAttribute(context.getReader(), "regulating", false);
        boolean loadTapChangingCapabilities = XmlUtil.readBoolAttribute(context.getReader(), "loadTapChangingCapabilities");
        float targetV = XmlUtil.readOptionalFloatAttribute(context.getReader(), "targetV");
        adder
                .setLowTapPosition(lowTapPosition)
                .setTapPosition(tapPosition)
                .setLoadTapChangingCapabilities(loadTapChangingCapabilities)
                .setTargetV(targetV);
        if (loadTapChangingCapabilities) {
            adder.setRegulating(regulating);
        }
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement(elementName, context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "terminalRef":
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() ->  {
                        adder.setRegulationTerminal(readTerminalRef(terminal.getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case "step":
                    float r = XmlUtil.readFloatAttribute(context.getReader(), "r");
                    float x = XmlUtil.readFloatAttribute(context.getReader(), "x");
                    float g = XmlUtil.readFloatAttribute(context.getReader(), "g");
                    float b = XmlUtil.readFloatAttribute(context.getReader(), "b");
                    float rho = XmlUtil.readFloatAttribute(context.getReader(), "rho");
                    adder.beginStep()
                            .setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho)
                            .endStep();
                    break;

                default:
                    throw new AssertionError();
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }

    protected static void readRatioTapChanger(TwoWindingsTransformer twt, NetworkXmlReaderContext context) throws XMLStreamException {
        readRatioTapChanger("ratioTapChanger", twt.newRatioTapChanger(), twt.getTerminal1(), context);
    }

    protected static void readRatioTapChanger(int leg, ThreeWindingsTransformer.Leg2or3 twl, NetworkXmlReaderContext context) throws XMLStreamException {
        readRatioTapChanger("ratioTapChanger" + leg, twl.newRatioTapChanger(), twl.getTerminal(), context);
    }

    protected static void writePhaseTapChanger(String name, PhaseTapChanger ptc, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, name);
        writeTapChanger(ptc, context.getWriter());
        context.getWriter().writeAttribute("regulationMode", ptc.getRegulationMode().name());
        if (ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP || !Float.isNaN(ptc.getRegulationValue())) {
            XmlUtil.writeFloat("regulationValue", ptc.getRegulationValue(), context.getWriter());
        }
        if (ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP || ptc.isRegulating()) {
            context.getWriter().writeAttribute("regulating", Boolean.toString(ptc.isRegulating()));
        }
        if (ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP || ptc.getRegulationTerminal() != null) {
            writeTerminalRef(ptc.getRegulationTerminal(), context, "terminalRef");
        }
        for (int p = ptc.getLowTapPosition(); p <= ptc.getHighTapPosition(); p++) {
            PhaseTapChangerStep ptcs = ptc.getStep(p);
            context.getWriter().writeEmptyElement(IIDM_URI, "step");
            writeTapChangerStep(ptcs, context.getWriter());
            XmlUtil.writeFloat("alpha", ptcs.getAlpha(), context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readPhaseTapChanger(TwoWindingsTransformer twt, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), "lowTapPosition");
        int tapPosition = XmlUtil.readIntAttribute(context.getReader(), "tapPosition");
        PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.valueOf(context.getReader().getAttributeValue(null, "regulationMode"));
        float regulationValue = XmlUtil.readOptionalFloatAttribute(context.getReader(), "regulationValue");
        boolean regulating = XmlUtil.readOptionalBoolAttribute(context.getReader(), "regulating", false);
        PhaseTapChangerAdder adder = twt.newPhaseTapChanger()
                .setLowTapPosition(lowTapPosition)
                .setTapPosition(tapPosition)
                .setRegulationMode(regulationMode)
                .setRegulationValue(regulationValue)
                .setRegulating(regulating);
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement("phaseTapChanger", context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "terminalRef":
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() ->  {
                        adder.setRegulationTerminal(readTerminalRef(twt.getTerminal1().getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case "step":
                    float r = XmlUtil.readFloatAttribute(context.getReader(), "r");
                    float x = XmlUtil.readFloatAttribute(context.getReader(), "x");
                    float g = XmlUtil.readFloatAttribute(context.getReader(), "g");
                    float b = XmlUtil.readFloatAttribute(context.getReader(), "b");
                    float rho = XmlUtil.readFloatAttribute(context.getReader(), "rho");
                    float alpha = XmlUtil.readFloatAttribute(context.getReader(), "alpha");
                    adder.beginStep()
                            .setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho)
                            .setAlpha(alpha)
                            .endStep();
                    break;

                default:
                    throw new AssertionError();
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }
}
