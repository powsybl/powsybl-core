/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.OptionalInt;
import java.util.function.DoubleConsumer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractTransformerSerDe<T extends Connectable<T>, A extends IdentifiableAdder<T, A>> extends AbstractSimpleIdentifiableSerDe<T, A, Substation> {

    private static final String ATTR_LOW_TAP_POSITION = "lowTapPosition";
    private static final String ATTR_TAP_POSITION = "tapPosition";
    private static final String ATTR_SOLVED_TAP_POSITION = "solvedTapPosition";
    private static final String ATTR_REGULATING = "regulating";
    private static final String ELEM_TERMINAL_REF = "terminalRef";
    private static final String ATTR_REGULATION_MODE = "regulationMode";
    private static final String ATTR_REGULATION_VALUE = "regulationValue";
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

    private static void writeTapChanger(TapChanger<?, ?, ?, ?> tc, NetworkSerializerContext context) {
        context.getWriter().writeIntAttribute(ATTR_LOW_TAP_POSITION, tc.getLowTapPosition());
        var tp = tc.findTapPosition();
        context.getWriter().writeOptionalIntAttribute(ATTR_TAP_POSITION, tp.isPresent() ? tp.getAsInt() : null);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
            var solvedTp = tc.findSolvedTapPosition();
            context.getWriter().writeOptionalIntAttribute(ATTR_SOLVED_TAP_POSITION, solvedTp.isPresent() ? solvedTp.getAsInt() : null);
        });
        writeTargetDeadband(tc.getTargetDeadband(), context);
    }

    protected static void writeRatioTapChanger(String name, RatioTapChanger rtc, NetworkSerializerContext context) {
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), name);

        Boolean optionalRegulatingValue = rtc.hasLoadTapChangingCapabilities() || rtc.isRegulating() ? rtc.isRegulating() : null;
        context.getWriter().writeOptionalBooleanAttribute(ATTR_REGULATING, optionalRegulatingValue);

        writeTapChanger(rtc, context);
        context.getWriter().writeBooleanAttribute("loadTapChangingCapabilities", rtc.hasLoadTapChangingCapabilities());
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_11, context, () -> context.getWriter().writeDoubleAttribute("targetV", rtc.getRegulationValue()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            context.getWriter().writeEnumAttribute(ATTR_REGULATION_MODE, rtc.getRegulationMode());
            context.getWriter().writeDoubleAttribute(ATTR_REGULATION_VALUE, rtc.getRegulationValue());
        });
        TerminalRefSerDe.writeTerminalRef(rtc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);

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
        readTapChangerAttributes(adder, context);

        boolean loadTapChangingCapabilities = context.getReader().readBooleanAttribute("loadTapChangingCapabilities");
        adder.setLoadTapChangingCapabilities(loadTapChangingCapabilities);

        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_11, context, () -> {
            double targetV = context.getReader().readDoubleAttribute("targetV");
            if (!Double.isNaN(targetV)) {
                adder.setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE);
            }
            adder.setRegulationValue(targetV);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> {
            RatioTapChanger.RegulationMode regulationMode = context.getReader().readEnumAttribute(ATTR_REGULATION_MODE, RatioTapChanger.RegulationMode.class);
            double regulationValue = context.getReader().readDoubleAttribute(ATTR_REGULATION_VALUE);
            adder.setRegulationMode(regulationMode)
                    .setRegulationValue(regulationValue);
        });

        boolean[] hasTerminalRef = new boolean[1];
        context.getReader().readChildNodes(subElementName -> {
            switch (subElementName) {
                case ELEM_TERMINAL_REF -> {
                    hasTerminalRef[0] = true;
                    readTapChangerTerminalRef(adder, terminal, context);
                }
                case STEP_ROOT_ELEMENT_NAME -> {
                    RatioTapChangerAdder.StepAdder stepAdder = adder.beginStep();
                    readSteps(context, stepAdder);
                    stepAdder.endStep();
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

        RegulationMode regMode = ptc.getRegulationMode();
        Boolean optionalRegulatingValue = (regMode == null || regMode == RegulationMode.FIXED_TAP) && !ptc.isRegulating() ? null : ptc.isRegulating();
        context.getWriter().writeOptionalBooleanAttribute(ATTR_REGULATING, optionalRegulatingValue);

        writeTapChanger(ptc, context);
        context.getWriter().writeEnumAttribute(ATTR_REGULATION_MODE, regMode);
        context.getWriter().writeDoubleAttribute(ATTR_REGULATION_VALUE, ptc.getRegulationValue());
        TerminalRefSerDe.writeTerminalRef(ptc.getRegulationTerminal(), context, ELEM_TERMINAL_REF);

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
        readTapChangerAttributes(adder, context);

        PhaseTapChanger.RegulationMode regulationMode = context.getReader().readEnumAttribute(ATTR_REGULATION_MODE, PhaseTapChanger.RegulationMode.class);
        double regulationValue = context.getReader().readDoubleAttribute(ATTR_REGULATION_VALUE);
        adder.setRegulationMode(regulationMode)
                .setRegulationValue(regulationValue);

        boolean[] hasTerminalRef = new boolean[1];
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ELEM_TERMINAL_REF -> {
                    hasTerminalRef[0] = true;
                    readTapChangerTerminalRef(adder, terminal, context);
                }
                case STEP_ROOT_ELEMENT_NAME -> {
                    PhaseTapChangerAdder.StepAdder stepAdder = adder.beginStep();
                    readSteps(context, stepAdder);
                    double alpha = context.getReader().readDoubleAttribute("alpha");
                    stepAdder.setAlpha(alpha)
                            .endStep();
                    context.getReader().readEndNode();
                }
                default -> throw new PowsyblException("Unknown element name '" + elementName + "' in '" + name + "'");
            }
        });
        if (!hasTerminalRef[0]) {
            adder.add();
        }
    }

    private static void readTapChangerTerminalRef(TapChangerAdder<?, ?, ?, ?, ?, ?> adder, Terminal terminal, NetworkDeserializerContext context) {
        TerminalRefSerDe.readTerminalRef(context, terminal.getVoltageLevel().getNetwork(), tRef -> {
            adder.setRegulationTerminal(tRef);
            adder.add();
        });
    }

    private static void readTapChangerAttributes(TapChangerAdder<?, ?, ?, ?, ?, ?> adder, NetworkDeserializerContext context) {
        boolean regulating = context.getReader().readOptionalBooleanAttribute(ATTR_REGULATING).orElse(false);
        int lowTapPosition = context.getReader().readIntAttribute(ATTR_LOW_TAP_POSITION);
        OptionalInt tapPosition = context.getReader().readOptionalIntAttribute(ATTR_TAP_POSITION);
        OptionalInt solvedTapPosition = context.getReader().readOptionalIntAttribute(ATTR_SOLVED_TAP_POSITION);
        double targetDeadband = readTargetDeadband(context, regulating);
        adder.setLowTapPosition(lowTapPosition)
                .setTargetDeadband(targetDeadband)
                .setRegulating(regulating);
        tapPosition.ifPresent(adder::setTapPosition);
        solvedTapPosition.ifPresent(adder::setSolvedTapPosition);
    }

    protected static void readPhaseTapChanger(TwoWindingsTransformer twt, NetworkDeserializerContext context) {
        readPhaseTapChanger(PHASE_TAP_CHANGER, twt.newPhaseTapChanger(), twt.getTerminal1(), context);
    }

    protected static void readPhaseTapChanger(int leg, ThreeWindingsTransformer.Leg twl, NetworkDeserializerContext context) {
        readPhaseTapChanger(PHASE_TAP_CHANGER + leg, twl.newPhaseTapChanger(), twl.getTerminal(), context);
    }

    private static void readSteps(NetworkDeserializerContext context, TapChangerStepAdder<?, ?> adder) {
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        double g = context.getReader().readDoubleAttribute("g");
        double b = context.getReader().readDoubleAttribute("b");
        double rho = context.getReader().readDoubleAttribute("rho");
        adder.setR(r).setX(x).setG(g).setB(b).setRho(rho);
    }

    /**
     * Read the apparent power in kVA.
     * @param name the field name to read
     * @param context the XMLStreamReader accessor
     * @param consumer the method will used apparent power value read
     */
    protected static void readRatedS(String name, NetworkDeserializerContext context, DoubleConsumer consumer) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> {
            double ratedS = context.getReader().readDoubleAttribute(name);
            consumer.accept(ratedS);
        });
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
