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
import javax.xml.stream.XMLStreamWriter;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTransformerXml<T extends Connectable, A extends IdentifiableAdder<A>> extends AbstractConnectableXml<T, A, Container<? extends Identifiable<?>>> {

    private interface StepConsumer {
        void accept(double r, double x, double g, double b, double rho);
    }

    private static final String ATTR_LOW_TAP_POSITION = "lowTapPosition";
    private static final String ATTR_TAP_POSITION = "tapPosition";
    private static final String ATTR_REGULATING = "regulating";
    private static final String ELEM_TERMINAL_REF = "terminalRef";
    private static final String ELEM_STEP = "step";
    private static final String TARGET_DEADBAND = "targetDeadband";
    private static final String RATIO_TAP_CHANGER = "ratioTapChanger";
    private static final String PHASE_TAP_CHANGER = "phaseTapChanger";

    protected static void writeTapChangerStep(TapChangerStep<?> tcs, XMLStreamWriter writer) throws XMLStreamException {
        XmlUtil.writeDouble("r", tcs.getR(), writer);
        XmlUtil.writeDouble("x", tcs.getX(), writer);
        XmlUtil.writeDouble("g", tcs.getG(), writer);
        XmlUtil.writeDouble("b", tcs.getB(), writer);
        XmlUtil.writeDouble("rho", tcs.getRho(), writer);
    }

    private static void writeTargetDeadband(double targetDeadband, NetworkXmlWriterContext context) {
        // in IIDM-XML version 1.0, 0 as targetDeadband is ignored for backwards compatibility
        // (i.e. ensuring round trips in IIDM-XML version 1.0)
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> XmlUtil.writeOptionalDouble(TARGET_DEADBAND, targetDeadband, 0, context.getWriter()));
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> XmlUtil.writeDouble(TARGET_DEADBAND, targetDeadband, context.getWriter()));
    }

    private static double readTargetDeadband(String regulating, NetworkXmlReaderContext context) {
        double[] targetDeadband = new double[1];
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> {
            targetDeadband[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), TARGET_DEADBAND);
            // in IIDM-XML version 1.0, NaN as targetDeadband when regulating is allowed.
            // in IIDM-XML version 1.1 and more recent, it is forbidden and throws an exception
            // to prevent issues, targetDeadband is set to 0 in this case
            if (Boolean.parseBoolean(regulating) && Double.isNaN(targetDeadband[0])) {
                targetDeadband[0] = 0;
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2,
                context, () -> targetDeadband[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), TARGET_DEADBAND));
        return targetDeadband[0];
    }

    private static void writeTapChanger(TapChanger<?, ?> tc, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute(ATTR_LOW_TAP_POSITION, Integer.toString(tc.getLowTapPosition()));
        if (tc.findTapPosition().isPresent()) {
            context.getWriter().writeAttribute(ATTR_TAP_POSITION, Integer.toString(tc.getTapPosition()));
        }
        writeTargetDeadband(tc.getTargetDeadband(), context);
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), name);
        writeTapChanger(rtc, context);
        context.getWriter().writeAttribute("loadTapChangingCapabilities", Boolean.toString(rtc.hasLoadTapChangingCapabilities()));
        Optional<Boolean> regulating = rtc.findRegulatingStatus();
        if (regulating.isPresent()) {
            if (rtc.hasLoadTapChangingCapabilities() || regulating.get() || context.getVersion().compareTo(IidmXmlVersion.V_1_7) >= 0) {
                context.getWriter().writeAttribute(ATTR_REGULATING, Boolean.toString(regulating.get()));
            }
        }
        XmlUtil.writeDouble("targetV", rtc.getTargetV(), context.getWriter());
        if (rtc.getRegulationTerminal() != null) {
            TerminalRefXml.writeTerminalRef(rtc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }
        for (int p = rtc.getLowTapPosition(); p <= rtc.getHighTapPosition(); p++) {
            RatioTapChangerStep rtcs = rtc.getStep(p);
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), ELEM_STEP);
            writeTapChangerStep(rtcs, context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readRatioTapChanger(String elementName, RatioTapChangerAdder adder, Terminal terminal, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_LOW_TAP_POSITION);
        Integer tapPosition = XmlUtil.readOptionalIntegerAttribute(context.getReader(), ATTR_TAP_POSITION);
        String regulatingStr = context.getReader().getAttributeValue(null, ATTR_REGULATING);
        double targetDeadband = readTargetDeadband(regulatingStr, context);
        boolean loadTapChangingCapabilities = XmlUtil.readBoolAttribute(context.getReader(), "loadTapChangingCapabilities");
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        adder.setLowTapPosition(lowTapPosition)
                .setTargetDeadband(targetDeadband)
                .setLoadTapChangingCapabilities(loadTapChangingCapabilities)
                .setTargetV(targetV);
        if (tapPosition != null) {
            adder.setTapPosition(tapPosition);
        }
        if (regulatingStr != null || context.getVersion().compareTo(IidmXmlVersion.V_1_7) < 0) {
            adder.setRegulating(Boolean.parseBoolean(regulatingStr));
        } else {
            adder.unsetRegulating();
        }
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement(elementName, context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ELEM_TERMINAL_REF:
                    readTerminalRef(context, hasTerminalRef, (id, side) -> {
                        adder.setRegulationTerminal(TerminalRefXml.readTerminalRef(terminal.getVoltageLevel().getNetwork(), id, side));
                        adder.add();
                    });
                    break;

                case ELEM_STEP:
                    readSteps(context, (r, x, g, b, rho) -> adder.beginStep()
                            .setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho)
                            .endStep());
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
        readRatioTapChanger(RATIO_TAP_CHANGER, twt.newRatioTapChanger(), twt.getTerminal1(), context);
    }

    protected static void readRatioTapChanger(int leg, ThreeWindingsTransformer.Leg twl, NetworkXmlReaderContext context) throws XMLStreamException {
        readRatioTapChanger(RATIO_TAP_CHANGER + leg, twl.newRatioTapChanger(), twl.getTerminal(), context);
    }

    protected static void writePhaseTapChanger(String name, PhaseTapChanger ptc, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), name);
        writeTapChanger(ptc, context);
        if (ptc.getRegulationMode() != null) {
            context.getWriter().writeAttribute("regulationMode", ptc.getRegulationMode().name());
        }
        if (ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP || !Double.isNaN(ptc.getRegulationValue())) {
            XmlUtil.writeDouble("regulationValue", ptc.getRegulationValue(), context.getWriter());
        }
        Optional<Boolean> regulating = ptc.findRegulatingStatus();
        if (regulating.isPresent()) {
            if (ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP || regulating.get() || context.getVersion().compareTo(IidmXmlVersion.V_1_7) >= 0) {
                context.getWriter().writeAttribute(ATTR_REGULATING, Boolean.toString(regulating.get()));
            }
        }
        if (ptc.getRegulationTerminal() != null) {
            TerminalRefXml.writeTerminalRef(ptc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }
        for (int p = ptc.getLowTapPosition(); p <= ptc.getHighTapPosition(); p++) {
            PhaseTapChangerStep ptcs = ptc.getStep(p);
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), ELEM_STEP);
            writeTapChangerStep(ptcs, context.getWriter());
            XmlUtil.writeDouble("alpha", ptcs.getAlpha(), context.getWriter());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readPhaseTapChanger(String name, PhaseTapChangerAdder adder, Terminal terminal, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_LOW_TAP_POSITION);
        Integer tapPosition = XmlUtil.readOptionalIntegerAttribute(context.getReader(), ATTR_TAP_POSITION);
        String regulationModeStr = context.getReader().getAttributeValue(null, "regulationMode");
        String regulatingStr = context.getReader().getAttributeValue(null, ATTR_REGULATING);
        double targetDeadband = readTargetDeadband(regulatingStr, context);
        PhaseTapChanger.RegulationMode regulationMode = regulationModeStr != null ? PhaseTapChanger.RegulationMode.valueOf(regulationModeStr) : null;
        double regulationValue = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "regulationValue");
        adder
                .setLowTapPosition(lowTapPosition)
                .setTargetDeadband(targetDeadband)
                .setRegulationMode(regulationMode)
                .setRegulationValue(regulationValue);
        if (tapPosition != null) {
            adder.setTapPosition(tapPosition);
        }
        if (regulatingStr != null || context.getVersion().compareTo(IidmXmlVersion.V_1_7) < 0) {
            adder.setRegulating(Boolean.parseBoolean(regulatingStr));
        }
        boolean[] hasTerminalRef = new boolean[1];
        XmlUtil.readUntilEndElement(name, context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case ELEM_TERMINAL_REF:
                    readTerminalRef(context, hasTerminalRef, (id, side) -> {
                        adder.setRegulationTerminal(TerminalRefXml.readTerminalRef(terminal.getVoltageLevel().getNetwork(), id, side));
                        adder.add();
                    });
                    break;

                case ELEM_STEP:
                    PhaseTapChangerAdder.StepAdder stepAdder = adder.beginStep();
                    readSteps(context, (r, x, g, b, rho) -> stepAdder.setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho));
                    double alpha = XmlUtil.readDoubleAttribute(context.getReader(), "alpha");
                    stepAdder.setAlpha(alpha)
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

    protected static void readPhaseTapChanger(TwoWindingsTransformer twt, NetworkXmlReaderContext context) throws XMLStreamException {
        readPhaseTapChanger(PHASE_TAP_CHANGER, twt.newPhaseTapChanger(), twt.getTerminal1(), context);
    }

    protected static void readPhaseTapChanger(int leg, ThreeWindingsTransformer.Leg twl, NetworkXmlReaderContext context) throws XMLStreamException {
        readPhaseTapChanger(PHASE_TAP_CHANGER + leg, twl.newPhaseTapChanger(), twl.getTerminal(), context);
    }

    private static void readTerminalRef(NetworkXmlReaderContext context, boolean[] hasTerminalRef, BiConsumer<String, String > consumer) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
        String side = context.getReader().getAttributeValue(null, "side");
        context.getEndTasks().add(() -> consumer.accept(id, side));
        hasTerminalRef[0] = true;
    }

    private static void readSteps(NetworkXmlReaderContext context, StepConsumer consumer) {
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r");
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x");
        double g = XmlUtil.readDoubleAttribute(context.getReader(), "g");
        double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
        double rho = XmlUtil.readDoubleAttribute(context.getReader(), "rho");
        consumer.accept(r, x, g, b, rho);
    }

    /**
     * Read the apparent power in kVA.
     * @param name the field name to read
     * @param context the XMLStreamReader accessor
     * @param consumer the method will used apparent power value read
     */
    protected static void readRatedS(String name, NetworkXmlReaderContext context, DoubleConsumer consumer) {
        double ratedS = XmlUtil.readOptionalDoubleAttribute(context.getReader(), name);
        consumer.accept(ratedS);
    }

    /**
     * Write the apparent power in kVA.
     * @param name the field name to write
     * @param ratedS the apparent power value to serialize
     * @param context the XMLStreamWriter accessor
     */
    protected static void writeRatedS(String name, double ratedS, NetworkXmlWriterContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> XmlUtil.writeOptionalDouble(name, ratedS, Double.NaN, context.getWriter()));
    }
}
