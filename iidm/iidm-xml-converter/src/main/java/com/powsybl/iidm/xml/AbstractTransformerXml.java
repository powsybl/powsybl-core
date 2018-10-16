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
    private static final String ELEM_TAP = "tap";


    protected static void writeTapChangerTap(TapChanger.Tap<?> tcs, XMLStreamWriter writer) throws XMLStreamException {
        XmlUtil.writeOptionalDouble("rdr", tcs.getRdr(), 0.0, writer);
        XmlUtil.writeOptionalDouble("rdx", tcs.getRdx(), 0.0, writer);
        XmlUtil.writeOptionalDouble("rdg", tcs.getRdg(), 0.0, writer);
        XmlUtil.writeOptionalDouble("rdb", tcs.getRdb(), 0.0, writer);
        XmlUtil.writeOptionalDouble("ratio", tcs.getRatio(), 1.0, writer);
    }

    protected static void writeTapChanger(TapChanger<?, ?> tc, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(ATTR_LOW_TAP_POSITION, Integer.toString(tc.getLowTapPosition()));
        writer.writeAttribute(ATTR_TAP_POSITION, Integer.toString(tc.getTapPosition()));
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(IIDM_URI, name);
        writeTapChanger(rtc, context.getWriter());
        context.getWriter().writeAttribute("onLoadTapChanger", Boolean.toString(rtc.onLoadTapChanger()));
        if (rtc.onLoadTapChanger() || rtc.isRegulating()) {
            context.getWriter().writeAttribute(ATTR_REGULATING, Boolean.toString(rtc.isRegulating()));
        }
        if (rtc.onLoadTapChanger() || !Double.isNaN(rtc.getTargetV())) {
            XmlUtil.writeDouble("targetV", rtc.getTargetV(), context.getWriter());
        }
        if (rtc.getRegulationTerminal() != null) {
            writeTerminalRef(rtc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }
        for (int p = rtc.getLowTapPosition(); p <= rtc.getHighTapPosition(); p++) {
            RatioTapChangerTap rtcs = rtc.getTap(p);
            context.getWriter().writeEmptyElement(IIDM_URI, ELEM_TAP);
            writeTapChangerTap(rtcs, context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readRatioTapChanger(String elementName, RatioTapChangerAdder adder, Terminal terminal, String transfoId, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_LOW_TAP_POSITION);
        int tapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_TAP_POSITION);
        boolean regulating = XmlUtil.readOptionalBoolAttribute(context.getReader(), ATTR_REGULATING, false);
        boolean onLoadTapChanger = XmlUtil.readBoolAttribute(context.getReader(), getVersionCompatibleAttribute(context, "loadTapChangingCapabilities"));
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        adder.setLowTapPosition(lowTapPosition)
                .setTapPosition(tapPosition)
                .setOnLoadTapChanger(onLoadTapChanger)
                .setTargetV(targetV);
        if (onLoadTapChanger) {
            adder.setRegulating(regulating);
        }
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement(elementName, context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ELEM_TERMINAL_REF:
                    String id = getCompatibleTerminalRefId(context, transfoId);
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() ->  {
                        adder.setRegulationTerminal(readTerminalRef(terminal.getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case ELEM_TAP:
                case ELEM_STEP:
                    double r = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "r"), 0.0);
                    double x = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "x"), 0.0);
                    double g = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "g"), 0.0);
                    double b = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "b"), 0.0);
                    double ratio = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "rho"), 1.0);
                    adder.beginTap()
                            .setRdr(r)
                            .setRdx(x)
                            .setRdg(g)
                            .setRdb(b)
                            .setRatio(ratio)
                            .endTap();
                    break;

                default:
                    throw new AssertionError(context.getReader().getLocalName() + " is not a valide element in RatioTapChanger.");
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }

    private static String getVersionCompatibleAttribute(NetworkXmlReaderContext context, String attribute) {
        if (context.getVersion().equals("1_1")) {
            if (attribute.equals("rho")) {
                return "ratio";
            } else if (attribute.equals("alpha")) {
                return "phaseShift";
            } else if (attribute.equals("loadTapChangingCapabilities")) {
                return "onLoadTapChanger";
            }
            return "rd" + attribute;
        } else {
            return attribute;
        }
    }

    private static String getCompatibleTerminalRefId(NetworkXmlReaderContext context, String transfoId) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        if (context.getVersion().equals("1_1")) {
            if (id == null) {
                return transfoId;
            } else {
                return id;
            }
        } else {
            return id;
        }
    }

    protected static void readRatioTapChanger(TwoWindingsTransformer twt, NetworkXmlReaderContext context) throws XMLStreamException {
        readRatioTapChanger("ratioTapChanger", twt.newRatioTapChanger(), twt.getTerminal1(), twt.getId(), context);
    }

    protected static void readRatioTapChanger(int leg, ThreeWindingsTransformer.Leg2or3 twl, String transfoId, NetworkXmlReaderContext context) throws XMLStreamException {
        readRatioTapChanger("ratioTapChanger" + leg, twl.newRatioTapChanger(), twl.getTerminal(), transfoId, context);
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
            PhaseTapChangerTap ptcs = ptc.getTap(p);
            context.getWriter().writeEmptyElement(IIDM_URI, ELEM_TAP);
            writeTapChangerTap(ptcs, context.getWriter());
            XmlUtil.writeOptionalDouble("phaseShift", ptcs.getPhaseShift(), 0.0, context.getWriter());
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
                    String id = getCompatibleTerminalRefId(context, twt.getId());
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() ->  {
                        adder.setRegulationTerminal(readTerminalRef(twt.getTerminal1().getVoltageLevel().getSubstation().getNetwork(), id, side));
                        adder.add();
                    });
                    hasTerminalRef[0] = true;
                    break;

                case ELEM_TAP:
                case ELEM_STEP:
                    // TODO check v1.0 missing attributes???
                    double r = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "r"), 0.0);
                    double x = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "x"), 0.0);
                    double g = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "g"), 0.0);
                    double b = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "b"), 0.0);
                    double ratio = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "rho"), 1.0);
                    double phaseShift = XmlUtil.readOptionalDoubleAttribute(context.getReader(), getVersionCompatibleAttribute(context, "alpha"), 0.0);
                    adder.endTap()
                            .setRdr(r)
                            .setRdx(x)
                            .setRdg(g)
                            .setRdb(b)
                            .setRatio(ratio)
                            .setPhaseShift(phaseShift)
                            .endTap();
                    break;

                default:
                    throw new AssertionError(context.getReader().getLocalName() + " is not a valide element in PhaseTapChanger.");
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }
}
