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

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_URI;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTransformerXml<T extends Connectable, A extends IdentifiableAdder<A>> extends AbstractConnectableXml<T, A, Substation> {

    private static final String ATTR_LOW_TAP_POSITION = "lowTapPosition";
    private static final String ATTR_TAP_POSITION = "tapPosition";
    private static final String ATTR_REGULATING = "regulating";
    private static final String ELEM_TERMINAL_REF = "terminalRef";
    private static final String ELEM_STEP = "step";


    protected static void writeTapChangerStep(TapChangerStep<?> tcs, XMLStreamWriter writer) throws XMLStreamException {
        XmlUtil.writeDouble("r", tcs.getR(), writer);
        XmlUtil.writeDouble("x", tcs.getX(), writer);
        XmlUtil.writeDouble("g", tcs.getG(), writer);
        XmlUtil.writeDouble("b", tcs.getB(), writer);
        XmlUtil.writeDouble("rho", tcs.getRho(), writer);
    }

    protected static void writeTapChanger(TapChanger<?, ?> tc, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(ATTR_LOW_TAP_POSITION, Integer.toString(tc.getLowTapPosition()));
        writer.writeAttribute(ATTR_TAP_POSITION, Integer.toString(tc.getTapPosition()));
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, name);
        writeTapChanger(rtc, context.getWriter());
        context.getWriter().writeAttribute("loadTapChangingCapabilities", Boolean.toString(rtc.hasLoadTapChangingCapabilities()));
        if (rtc.hasLoadTapChangingCapabilities() || rtc.isRegulating()) {
            context.getWriter().writeAttribute(ATTR_REGULATING, Boolean.toString(rtc.isRegulating()));
        }
        if (rtc.hasLoadTapChangingCapabilities() || !Double.isNaN(rtc.getTargetV())) {
            XmlUtil.writeDouble("targetV", rtc.getTargetV(), context.getWriter());
        }
        if (rtc.getRegulationTerminal() != null) {
            writeTerminalRef(rtc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }
        for (int p = rtc.getLowTapPosition(); p <= rtc.getHighTapPosition(); p++) {
            RatioTapChangerStep rtcs = rtc.getStep(p);
            context.getWriter().writeEmptyElement(IIDM_URI, ELEM_STEP);
            writeTapChangerStep(rtcs, context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readRatioTapChanger(String elementName, RatioTapChangerAdder adder, Terminal terminal, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_LOW_TAP_POSITION);
        int tapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_TAP_POSITION);
        boolean regulating = XmlUtil.readOptionalBoolAttribute(context.getReader(), ATTR_REGULATING, false);
        boolean loadTapChangingCapabilities = XmlUtil.readBoolAttribute(context.getReader(), "loadTapChangingCapabilities");
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        adder.setLowTapPosition(lowTapPosition)
                .setTapPosition(tapPosition)
                .setLoadTapChangingCapabilities(loadTapChangingCapabilities)
                .setTargetV(targetV);
        if (loadTapChangingCapabilities) {
            adder.setRegulating(regulating);
        }
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement(elementName, context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ELEM_TERMINAL_REF:
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() ->  {
                        adder.setRegulationTerminal(readTerminalRef(terminal.getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case ELEM_STEP:
                    double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
                    double x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
                    double g = XmlUtil.readDoubleAttribute(context.getReader(), "g");
                    double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
                    double rho = XmlUtil.readDoubleAttribute(context.getReader(), "rho");
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
        if (ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP || !Double.isNaN(ptc.getRegulationValue())) {
            XmlUtil.writeDouble("regulationValue", ptc.getRegulationValue(), context.getWriter());
        }
        if (ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP || ptc.isRegulating()) {
            context.getWriter().writeAttribute(ATTR_REGULATING, Boolean.toString(ptc.isRegulating()));
        }
        if (ptc.getRegulationTerminal() != null) {
            writeTerminalRef(ptc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }
        for (int p = ptc.getLowTapPosition(); p <= ptc.getHighTapPosition(); p++) {
            PhaseTapChangerStep ptcs = ptc.getStep(p);
            context.getWriter().writeEmptyElement(IIDM_URI, ELEM_STEP);
            writeTapChangerStep(ptcs, context.getWriter());
            XmlUtil.writeDouble("alpha", ptcs.getAlpha(), context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readPhaseTapChanger(TwoWindingsTransformer twt, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_LOW_TAP_POSITION);
        int tapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_TAP_POSITION);
        PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.valueOf(context.getReader().getAttributeValue(null, "regulationMode"));
        double regulationValue = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "regulationValue");
        boolean regulating = XmlUtil.readOptionalBoolAttribute(context.getReader(), ATTR_REGULATING, false);
        PhaseTapChangerAdder adder = twt.newPhaseTapChanger()
                .setLowTapPosition(lowTapPosition)
                .setTapPosition(tapPosition)
                .setRegulationMode(regulationMode)
                .setRegulationValue(regulationValue)
                .setRegulating(regulating);
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement("phaseTapChanger", context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ELEM_TERMINAL_REF:
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() ->  {
                        adder.setRegulationTerminal(readTerminalRef(twt.getTerminal1().getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case ELEM_STEP:
                    double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
                    double x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
                    double g = XmlUtil.readDoubleAttribute(context.getReader(), "g");
                    double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
                    double rho = XmlUtil.readDoubleAttribute(context.getReader(), "rho");
                    double alpha = XmlUtil.readDoubleAttribute(context.getReader(), "alpha");
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
