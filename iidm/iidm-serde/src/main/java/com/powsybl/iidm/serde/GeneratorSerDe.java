/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.removed.RemoteReactivePowerControl;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.serde.extensions.RemoteReactivePowerControlSerDe;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.concurrent.atomic.AtomicReference;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;
import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorSerDe extends AbstractSimpleIdentifiableSerDe<Generator, GeneratorAdder, VoltageLevel> {

    static final GeneratorSerDe INSTANCE = new GeneratorSerDe();

    static final String ROOT_ELEMENT_NAME = "generator";
    static final String ARRAY_ELEMENT_NAME = "generators";
    static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String TARGET_V = "targetV";
    private static final String TARGET_Q = "targetQ";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Generator g, VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeEnumAttribute("energySource", g.getEnergySource());
        context.getWriter().writeDoubleAttribute("minP", g.getMinP());
        context.getWriter().writeDoubleAttribute("maxP", g.getMaxP());
        context.getWriter().writeDoubleAttribute("ratedS", g.getRatedS());
        writeVoltageRegulatorOn(g, context);
        context.getWriter().writeDoubleAttribute("targetP", g.getTargetP());
        writeTargetV(g, context);
        writeTargetQ(g, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_13, context, () ->
            context.getWriter().writeBooleanAttribute("isCondenser", g.isCondenser(), false));
        writeEquivalentLocalTargetV(g, context);
        writeNodeOrBus(null, g.getTerminal(), context);
        writePQ(null, g.getTerminal(), context.getWriter());
    }

    private static void writeEquivalentLocalTargetV(Generator g, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_15, IidmVersion.V_1_16, context, () -> {
            double equivalentTargetV = Double.NaN;
            if (g.isRegulatingWithMode(RegulationMode.VOLTAGE)) {
                equivalentTargetV = g.getTargetV();
            }
            context.getWriter().writeDoubleAttribute("equivalentLocalTargetV", equivalentTargetV, Double.NaN);
        });
    }

    private static void writeVoltageRegulatorOn(Generator g, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            context.getWriter().writeBooleanAttribute("voltageRegulatorOn", g.isRegulatingWithMode(RegulationMode.VOLTAGE)));
    }

    private static void writeTargetV(Generator g, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
            double targetV;
            if (g.isRegulatingWithMode(RegulationMode.VOLTAGE)) {
                targetV = g.getVoltageRegulation().getTargetValue();
            } else {
                targetV = g.getTargetV();
            }
            context.getWriter().writeDoubleAttribute(TARGET_V, targetV);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(TARGET_V, g.getTargetV()));
    }

    private static void writeTargetQ(Generator g, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
            double targetQ;
            if (g.isRegulatingWithMode(RegulationMode.REACTIVE_POWER) && g.getTerminal() == g.getRegulatingTerminal()) {
                targetQ = g.getVoltageRegulation().getTargetValue();
            } else {
                // In distant reactive power regulation, the VoltageRegulation's targetValue is exported in the old "RemoteReactivePowerControl" extension.
                targetQ = g.getTargetQ();
            }
            context.getWriter().writeDoubleAttribute(TARGET_Q, targetQ);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () ->
            context.getWriter().writeDoubleAttribute(TARGET_Q, g.getTargetQ()));
    }

    @Override
    protected void addExtinctExtensions(Generator g, NetworkSerializerContext context) {
        if (RemoteReactivePowerControlSerDe.isExtensionNeededAndExportable(g, context)) {
            RemoteReactivePowerControl extension = new RemoteReactivePowerControl(g,
                    g.getVoltageRegulation().getTargetValue(),
                    g.getVoltageRegulation().getTerminal(),
                    g.getVoltageRegulation().isRegulating());
            context.addExtinctExtensionsToSerialize(g.getId(), extension);
        }
    }

    @Override
    protected void writeSubElements(Generator g, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
            if (g.getVoltageRegulation() != null
                && g.getVoltageRegulation().getTerminal() != null
                && g != g.getVoltageRegulation().getTerminal().getConnectable()
                && g.getVoltageRegulation().getMode() == RegulationMode.VOLTAGE) {
                TerminalRefSerDe.writeTerminalRef(g.getVoltageRegulation().getTerminal(), context, REGULATING_TERMINAL);
            }
        });
        ReactiveLimitsSerDe.INSTANCE.write(g, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> VoltageRegulationSerDe.writeVoltageRegulation(g.getVoltageRegulation(), context, g));
    }

    @Override
    protected GeneratorAdder createAdder(VoltageLevel vl) {
        return vl.newGenerator();
    }

    @Override
    protected Generator readRootElementAttributes(GeneratorAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        EnergySource energySource = context.getReader().readEnumAttribute("energySource", EnergySource.class);
        double minP = context.getReader().readDoubleAttribute("minP");
        double maxP = context.getReader().readDoubleAttribute("maxP");
        double ratedS = context.getReader().readDoubleAttribute("ratedS");
        adder.setEnergySource(energySource)
            .setMinP(minP)
            .setMaxP(maxP)
            .setRatedS(ratedS);
        Boolean voltageRegulatorOn = readVoltageRegulatorOnByVersion(context);
        double targetP = context.getReader().readDoubleAttribute("targetP");
        double targetV = context.getReader().readDoubleAttribute(TARGET_V);
        double targetQ = context.getReader().readDoubleAttribute(TARGET_Q);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_13, context, () ->
            adder.setCondenser(context.getReader().readBooleanAttribute("isCondenser", false)));
        adder.setTargetP(targetP);
        AtomicReference<Double> equivalentLocalTargetV = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_15, IidmVersion.V_1_16, context, () -> equivalentLocalTargetV.set(context.getReader().readDoubleAttribute("equivalentLocalTargetV", Double.NaN)));
        buildVoltageRegulationForOlderVersions(context, adder, targetV, targetQ, voltageRegulatorOn);
        addTargetV(context, adder, targetV, equivalentLocalTargetV.get(), voltageRegulatorOn);
        addTargetQ(context, adder, targetQ, voltageRegulatorOn);

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        Generator g = adder.add();
        readPQ(null, g.getTerminal(), context.getReader());
        return g;
    }

    @Override
    protected void readSubElements(Generator g, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case REGULATING_TERMINAL ->
                    TerminalRefSerDe.readTerminalRef(context, g.getNetwork(), terminal -> {
                        if (g.getVoltageRegulation() != null) {
                            g.getVoltageRegulation().setTerminal(terminal);
                        }
                    });
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(g, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(g, context);
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(g, context, g.getNetwork());
                default -> readSubElement(elementName, g, context);
            }
        });
    }

    private void buildVoltageRegulationForOlderVersions(NetworkDeserializerContext context, final GeneratorAdder adder,
                                                        double targetV, double targetQ, Boolean voltageRegulatorOn) {
        // VOLTAGE REGULATION
        // version < V_1_17
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            createVoltageRegulationBackwardCompatibility(adder.newVoltageRegulation(), targetV, targetQ, voltageRegulatorOn, null));
        // version >= V_1_17 -> voltageRegulation is read with VoltageRegulationSerDe
        // Nothing to do
    }

    private static void addTargetQ(NetworkDeserializerContext context, GeneratorAdder adder, double targetQ, Boolean voltageRegulatorOn) {
        // version < V_1_17
        if (Boolean.TRUE.equals(voltageRegulatorOn)) {
            IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> adder.setTargetQ(targetQ));
        }
        // From 1_16
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> adder.setTargetQ(targetQ));
    }

    private static void addTargetV(NetworkDeserializerContext context, GeneratorAdder adder, double targetV, double equivalentLocalTargetV, Boolean voltageRegulatorOn) {
        // version < V_1_17
        if (Boolean.FALSE.equals(voltageRegulatorOn)) {
            IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
                double newTargetV = targetV;
                if (!Double.isNaN(equivalentLocalTargetV)) {
                    newTargetV = equivalentLocalTargetV;
                }
                adder.setTargetV(newTargetV);
            });
        } else if (Boolean.TRUE.equals(voltageRegulatorOn)) {
            IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_15, IidmVersion.V_1_16, context, () ->
                adder.setTargetV(equivalentLocalTargetV));
        }
        // From V_1_17
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> adder.setTargetV(targetV));
    }

    private Boolean readVoltageRegulatorOnByVersion(NetworkDeserializerContext context) {
        AtomicReference<Boolean> voltageRegulatorOn = new AtomicReference<>();
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            voltageRegulatorOn.set(context.getReader().readBooleanAttribute("voltageRegulatorOn")));
        return voltageRegulatorOn.get();
    }
}
