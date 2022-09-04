/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
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

    protected static void writeTapChangerStep(TapChangerStep<?> tcs, XmlWriter writer) {
        writer.writeDoubleAttribute("r", tcs.getR());
        writer.writeDoubleAttribute("x", tcs.getX());
        writer.writeDoubleAttribute("g", tcs.getG());
        writer.writeDoubleAttribute("b", tcs.getB());
        writer.writeDoubleAttribute("rho", tcs.getRho());
    }

    private static void writeTargetDeadband(double targetDeadband, NetworkXmlWriterContext context) {
        // in IIDM-XML version 1.0, 0 as targetDeadband is ignored for backwards compatibility
        // (i.e. ensuring round trips in IIDM-XML version 1.0)
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> context.getWriter().writeDoubleAttribute(TARGET_DEADBAND, targetDeadband, 0));
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> context.getWriter().writeDoubleAttribute(TARGET_DEADBAND, targetDeadband));
    }

    private static double readTargetDeadband(NetworkXmlReaderContext context) {
        double[] targetDeadband = new double[1];
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> {
            targetDeadband[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), TARGET_DEADBAND);
            // in IIDM-XML version 1.0, NaN as targetDeadband when regulating is allowed.
            // in IIDM-XML version 1.1 and more recent, it is forbidden and throws an exception
            // to prevent issues, targetDeadband is set to 0 in this case
            if (Boolean.parseBoolean(context.getReader().getAttributeValue(null, ATTR_REGULATING)) && Double.isNaN(targetDeadband[0])) {
                targetDeadband[0] = 0;
            }
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2,
                context, () -> targetDeadband[0] = XmlUtil.readOptionalDoubleAttribute(context.getReader(), TARGET_DEADBAND));
        return targetDeadband[0];
    }

    private static void writeTapChanger(TapChanger<?, ?> tc, NetworkXmlWriterContext context) {
        context.getWriter().writeStringAttribute(ATTR_LOW_TAP_POSITION, Integer.toString(tc.getLowTapPosition()));
        if (tc.findTapPosition().isPresent()) {
            context.getWriter().writeStringAttribute(ATTR_TAP_POSITION, Integer.toString(tc.getTapPosition()));
        }
        writeTargetDeadband(tc.getTargetDeadband(), context);
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, NetworkXmlWriterContext context) {
        context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), name);
        writeTapChanger(rtc, context);
        context.getWriter().writeStringAttribute("loadTapChangingCapabilities", Boolean.toString(rtc.hasLoadTapChangingCapabilities()));
        if (rtc.hasLoadTapChangingCapabilities() || rtc.isRegulating()) {
            context.getWriter().writeStringAttribute(ATTR_REGULATING, Boolean.toString(rtc.isRegulating()));
        }
        context.getWriter().writeDoubleAttribute("targetV", rtc.getTargetV());
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
        double targetDeadband = readTargetDeadband(context);
        boolean loadTapChangingCapabilities = XmlUtil.readBoolAttribute(context.getReader(), "loadTapChangingCapabilities");
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        adder.setLowTapPosition(lowTapPosition)
                .setTargetDeadband(targetDeadband)
                .setLoadTapChangingCapabilities(loadTapChangingCapabilities)
                .setTargetV(targetV);
        XmlUtil.consumeOptionalIntAttribute(context.getReader(), ATTR_TAP_POSITION, adder::setTapPosition);
        XmlUtil.consumeOptionalBoolAttribute(context.getReader(), ATTR_REGULATING, adder::setRegulating);
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

    protected static void writePhaseTapChanger(String name, PhaseTapChanger ptc, NetworkXmlWriterContext context) {
        context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), name);
        writeTapChanger(ptc, context);
        context.getWriter().writeEnumAttribute("regulationMode", ptc.getRegulationMode());
        if ((ptc.getRegulationMode() != null && ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP) || !Double.isNaN(ptc.getRegulationValue())) {
            context.getWriter().writeDoubleAttribute("regulationValue", ptc.getRegulationValue());
        }
        if ((ptc.getRegulationMode() != null && ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP) || ptc.isRegulating()) {
            context.getWriter().writeStringAttribute(ATTR_REGULATING, Boolean.toString(ptc.isRegulating()));
        }
        if (ptc.getRegulationTerminal() != null) {
            TerminalRefXml.writeTerminalRef(ptc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }
        for (int p = ptc.getLowTapPosition(); p <= ptc.getHighTapPosition(); p++) {
            PhaseTapChangerStep ptcs = ptc.getStep(p);
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), ELEM_STEP);
            writeTapChangerStep(ptcs, context.getWriter());
            context.getWriter().writeDoubleAttribute("alpha", ptcs.getAlpha());
        }
        context.getWriter().writeEndElement();
    }

    protected static void readPhaseTapChanger(String name, PhaseTapChangerAdder adder, Terminal terminal, NetworkXmlReaderContext context) throws XMLStreamException {
        int lowTapPosition = XmlUtil.readIntAttribute(context.getReader(), ATTR_LOW_TAP_POSITION);
        double targetDeadband = readTargetDeadband(context);
        PhaseTapChanger.RegulationMode regulationMode = XmlUtil.readOptionalEnum(context.getReader(), "regulationMode", PhaseTapChanger.RegulationMode.class);
        double regulationValue = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "regulationValue");
        adder
                .setLowTapPosition(lowTapPosition)
                .setTargetDeadband(targetDeadband)
                .setRegulationMode(regulationMode)
                .setRegulationValue(regulationValue);
        XmlUtil.consumeOptionalIntAttribute(context.getReader(), ATTR_TAP_POSITION, adder::setTapPosition);
        XmlUtil.consumeOptionalBoolAttribute(context.getReader(), ATTR_REGULATING, adder::setRegulating);
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
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> context.getWriter().writeDoubleAttribute(name, ratedS, Double.NaN));
    }
}
