/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.function.DoubleConsumer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractTransformerSerDe<T extends Connectable<T>, A extends IdentifiableAdder<T, A>> extends AbstractSimpleIdentifiableSerDe<T, A, Substation> {

    private interface StepConsumer {

        void accept(double r, double x, double g, double b, double rho);
    }

    private static final String ATTR_LOW_TAP_POSITION = "lowTapPosition";
    private static final String ATTR_TAP_POSITION = "tapPosition";
    private static final String ATTR_REGULATING = "regulating";
    private static final String ELEM_TERMINAL_REF = "terminalRef";
    static final String STEP_ROOT_ELEMENT_NAME = "step";
    static final String STEP_ARRAY_ELEMENT_NAME = "steps";
    private static final String TARGET_DEADBAND = "targetDeadband";
    private static final String RATIO_TAP_CHANGER = "ratioTapChanger";
    private static final String PHASE_TAP_CHANGER = "phaseTapChanger";

    protected static void writeTapChangerStep(TapChangerStep<?> tcs, TreeDataWriter writer) {
        writer.writeDoubleAttribute("r", tcs.getR());
        writer.writeDoubleAttribute("x", tcs.getX());
        writer.writeDoubleAttribute("g", tcs.getG());
        writer.writeDoubleAttribute("b", tcs.getB());
        writer.writeDoubleAttribute("rho", tcs.getRho());
    }

    private static void writeTargetDeadband(double targetDeadband, NetworkSerializerContext context) {
        // in IIDM version 1.0, 0 as targetDeadband is ignored for backwards compatibility
        // (i.e. ensuring round trips in IIDM version 1.0)
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> context.getWriter().writeDoubleAttribute(TARGET_DEADBAND, targetDeadband, 0));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> context.getWriter().writeDoubleAttribute(TARGET_DEADBAND, targetDeadband));
    }

    private static double readTargetDeadband(NetworkDeserializerContext context, boolean regulating) {
        double[] targetDeadband = new double[1];
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> {
            targetDeadband[0] = context.getReader().readDoubleAttribute(TARGET_DEADBAND);
            // in IIDM version 1.0, NaN as targetDeadband when regulating is allowed.
            // in IIDM version 1.1 and more recent, it is forbidden and throws an exception
            // to prevent issues, targetDeadband is set to 0 in this case
            if (regulating && Double.isNaN(targetDeadband[0])) {
                targetDeadband[0] = 0;
            }
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2,
                context, () -> targetDeadband[0] = context.getReader().readDoubleAttribute(TARGET_DEADBAND));
        return targetDeadband[0];
    }

    private static void writeTapChanger(TapChanger<?, ?> tc, NetworkSerializerContext context) {
        context.getWriter().writeIntAttribute(ATTR_LOW_TAP_POSITION, tc.getLowTapPosition());
        if (tc.findTapPosition().isPresent()) {
            context.getWriter().writeIntAttribute(ATTR_TAP_POSITION, tc.getTapPosition());
        }
        writeTargetDeadband(tc.getTargetDeadband(), context);
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), name);
        if (rtc.hasLoadTapChangingCapabilities() || rtc.isRegulating()) {
            context.getWriter().writeBooleanAttribute(ATTR_REGULATING, rtc.isRegulating());
        }
        writeTapChanger(rtc, context);
        context.getWriter().writeBooleanAttribute("loadTapChangingCapabilities", rtc.hasLoadTapChangingCapabilities());
        context.getWriter().writeDoubleAttribute("targetV", rtc.getTargetV());
        if (rtc.getRegulationTerminal() != null) {
            TerminalRefSerDe.writeTerminalRef(rtc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }

        context.getWriter().writeStartNodes();
        for (int p = rtc.getLowTapPosition(); p <= rtc.getHighTapPosition(); p++) {
            RatioTapChangerStep rtcs = rtc.getStep(p);
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), STEP_ROOT_ELEMENT_NAME);
            writeTapChangerStep(rtcs, context.getWriter());
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    protected static void readRatioTapChanger(String elementName, RatioTapChangerAdder adder, Terminal terminal, NetworkDeserializerContext context) {
        boolean regulating = context.getReader().readBooleanAttribute(ATTR_REGULATING, false);
        int lowTapPosition = context.getReader().readIntAttribute(ATTR_LOW_TAP_POSITION);
        Integer tapPosition = context.getReader().readIntAttribute(ATTR_TAP_POSITION);
        double targetDeadband = readTargetDeadband(context, regulating);
        boolean loadTapChangingCapabilities = context.getReader().readBooleanAttribute("loadTapChangingCapabilities");
        double targetV = context.getReader().readDoubleAttribute("targetV");

        adder.setLowTapPosition(lowTapPosition)
                .setTargetDeadband(targetDeadband)
                .setLoadTapChangingCapabilities(loadTapChangingCapabilities)
                .setTargetV(targetV)
                .setRegulating(regulating);
        if (tapPosition != null) {
            adder.setTapPosition(tapPosition);
        }

        boolean[] hasTerminalRef = new boolean[1];
        context.getReader().readChildNodes(subElementName -> {
            switch (subElementName) {
                case ELEM_TERMINAL_REF -> {
                    hasTerminalRef[0] = true;
                    TerminalRefSerDe.readTerminalRef(context, terminal.getVoltageLevel().getNetwork(), tRef -> {
                        adder.setRegulationTerminal(tRef);
                        adder.add();
                    });
                }
                case STEP_ROOT_ELEMENT_NAME -> {
                    readSteps(context, (r, x, g, b, rho) -> adder.beginStep()
                            .setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho)
                            .endStep());
                    context.getReader().readEndNode();
                }
                default -> throw new PowsyblException("Unknown element name '" + subElementName + "' in '" + elementName + "'");
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }

    protected static void readRatioTapChanger(TwoWindingsTransformer twt, NetworkDeserializerContext context) {
        readRatioTapChanger(RATIO_TAP_CHANGER, twt.newRatioTapChanger(), twt.getTerminal1(), context);
    }

    protected static void readRatioTapChanger(int leg, ThreeWindingsTransformer.Leg twl, NetworkDeserializerContext context) {
        readRatioTapChanger(RATIO_TAP_CHANGER + leg, twl.newRatioTapChanger(), twl.getTerminal(), context);
    }

    protected static void writePhaseTapChanger(String name, PhaseTapChanger ptc, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), name);
        if (ptc.getRegulationMode() != null && ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP
                || ptc.isRegulating()) {
            context.getWriter().writeBooleanAttribute(ATTR_REGULATING, ptc.isRegulating());
        }
        writeTapChanger(ptc, context);
        context.getWriter().writeEnumAttribute("regulationMode", ptc.getRegulationMode());
        if (ptc.getRegulationMode() != null && ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP
                || !Double.isNaN(ptc.getRegulationValue())) {
            context.getWriter().writeDoubleAttribute("regulationValue", ptc.getRegulationValue());
        }
        if (ptc.getRegulationTerminal() != null) {
            TerminalRefSerDe.writeTerminalRef(ptc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);
        }

        context.getWriter().writeStartNodes();
        for (int p = ptc.getLowTapPosition(); p <= ptc.getHighTapPosition(); p++) {
            PhaseTapChangerStep ptcs = ptc.getStep(p);
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), STEP_ROOT_ELEMENT_NAME);
            writeTapChangerStep(ptcs, context.getWriter());
            context.getWriter().writeDoubleAttribute("alpha", ptcs.getAlpha());
            context.getWriter().writeEndNode();
        }
        context.getWriter().writeEndNodes();

        context.getWriter().writeEndNode();
    }

    protected static void readPhaseTapChanger(String name, PhaseTapChangerAdder adder, Terminal terminal, NetworkDeserializerContext context) {
        boolean regulating = context.getReader().readBooleanAttribute(ATTR_REGULATING, false);
        int lowTapPosition = context.getReader().readIntAttribute(ATTR_LOW_TAP_POSITION);
        Integer tapPosition = context.getReader().readIntAttribute(ATTR_TAP_POSITION);
        double targetDeadband = readTargetDeadband(context, regulating);
        PhaseTapChanger.RegulationMode regulationMode = context.getReader().readEnumAttribute("regulationMode", PhaseTapChanger.RegulationMode.class);
        double regulationValue = context.getReader().readDoubleAttribute("regulationValue");

        adder.setLowTapPosition(lowTapPosition)
                .setTargetDeadband(targetDeadband)
                .setRegulationMode(regulationMode)
                .setRegulationValue(regulationValue)
                .setRegulating(regulating);
        if (tapPosition != null) {
            adder.setTapPosition(tapPosition);
        }

        boolean[] hasTerminalRef = new boolean[1];
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ELEM_TERMINAL_REF -> {
                    hasTerminalRef[0] = true;
                    TerminalRefSerDe.readTerminalRef(context, terminal.getVoltageLevel().getNetwork(), tRef -> {
                        adder.setRegulationTerminal(tRef);
                        adder.add();
                    });
                }
                case STEP_ROOT_ELEMENT_NAME -> {
                    PhaseTapChangerAdder.StepAdder stepAdder = adder.beginStep();
                    readSteps(context, (r, x, g, b, rho) -> stepAdder.setR(r)
                            .setX(x)
                            .setG(g)
                            .setB(b)
                            .setRho(rho));
                    double alpha = context.getReader().readDoubleAttribute("alpha");
                    context.getReader().readEndNode();
                    stepAdder.setAlpha(alpha)
                            .endStep();
                }
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in '" + name + "'");
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }

    protected static void readPhaseTapChanger(TwoWindingsTransformer twt, NetworkDeserializerContext context) {
        readPhaseTapChanger(PHASE_TAP_CHANGER, twt.newPhaseTapChanger(), twt.getTerminal1(), context);
    }

    protected static void readPhaseTapChanger(int leg, ThreeWindingsTransformer.Leg twl, NetworkDeserializerContext context) {
        readPhaseTapChanger(PHASE_TAP_CHANGER + leg, twl.newPhaseTapChanger(), twl.getTerminal(), context);
    }

    private static void readSteps(NetworkDeserializerContext context, StepConsumer consumer) {
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        double g = context.getReader().readDoubleAttribute("g");
        double b = context.getReader().readDoubleAttribute("b");
        double rho = context.getReader().readDoubleAttribute("rho");
        consumer.accept(r, x, g, b, rho);
    }

    /**
     * Read the apparent power in kVA.
     * @param name the field name to read
     * @param context the XMLStreamReader accessor
     * @param consumer the method will used apparent power value read
     */
    protected static void readRatedS(String name, NetworkDeserializerContext context, DoubleConsumer consumer) {
        double ratedS = context.getReader().readDoubleAttribute(name);
        consumer.accept(ratedS);
    }

    /**
     * Write the apparent power in kVA.
     * @param name the field name to write
     * @param ratedS the apparent power value to serialize
     * @param context the XMLStreamWriter accessor
     */
    protected static void writeRatedS(String name, double ratedS, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> context.getWriter().writeDoubleAttribute(name, ratedS));
    }
}
