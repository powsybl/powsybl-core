/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;
import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorSerDe extends AbstractComplexIdentifiableSerDe<Generator, GeneratorAdder, VoltageLevel> {

    static final GeneratorSerDe INSTANCE = new GeneratorSerDe();

    static final String ROOT_ELEMENT_NAME = "generator";
    static final String ARRAY_ELEMENT_NAME = "generators";
    static final String REGULATING_TERMINAL = "regulatingTerminal";

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
            if (g.isRemoteRegulating() && g.isWithMode(RegulationMode.VOLTAGE)) {
                equivalentTargetV = g.getLocalTargetV();
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
            if (g.isRemoteRegulating() && g.isWithMode(RegulationMode.VOLTAGE)) {
                targetV = g.getVoltageRegulation().getTargetValue();
            } else {
                targetV = g.getLocalTargetV();
            }
            context.getWriter().writeDoubleAttribute(getTargetVName(context), targetV);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(getTargetVName(context), g.getLocalTargetV()));
    }

    private static void writeTargetQ(Generator g, NetworkSerializerContext context) {
        context.getWriter().writeDoubleAttribute(getTargetQName(context), g.getLocalTargetQ());
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
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> VoltageRegulationSerDe.writeVoltageRegulation(g.getVoltageRegulation(), context));
    }

    @Override
    protected GeneratorAdder createAdder(VoltageLevel vl) {
        return vl.newGenerator();
    }

    @Override
    protected void readRootElementAttributes(GeneratorAdder adder, VoltageLevel voltageLevel, List<Consumer<Generator>> toApply, NetworkDeserializerContext context) {
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
        double targetV = context.getReader().readDoubleAttribute(getTargetVName(context));
        double targetQ = context.getReader().readDoubleAttribute(getTargetQName(context));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_13, context, () ->
            adder.setCondenser(context.getReader().readBooleanAttribute("isCondenser", false)));
        adder.setTargetP(targetP);
        AtomicReference<Double> equivalentLocalTargetV = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_15, IidmVersion.V_1_16, context, () -> equivalentLocalTargetV.set(context.getReader().readDoubleAttribute("equivalentLocalTargetV", Double.NaN)));
        buildVoltageRegulationFromOlderVersions(context, adder, voltageRegulatorOn);
        addTargetV(context, adder, targetV, equivalentLocalTargetV.get());
        adder.setLocalTargetQ(targetQ);

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());

        double p = context.getReader().readDoubleAttribute("p");
        double q = context.getReader().readDoubleAttribute("q");
        toApply.add(generator -> generator.getTerminal().setP(p).setQ(q));
        toApply.add(generator -> {
            double targetValueDouble = Double.isNaN(targetV) ? equivalentLocalTargetV.get() : targetV;
            Runnable actionOnRemoteTerminal;
            if (!Double.isNaN(targetV) && !Double.isNaN(equivalentLocalTargetV.get())) {
                actionOnRemoteTerminal = () -> { };
            } else {
                actionOnRemoteTerminal = () -> generator.setLocalTargetV(Double.NaN);
            }
            context.addExtraProperties(generator, new NetworkDeserializerContext.ExtraPropertiesData(targetValueDouble, actionOnRemoteTerminal));
        });
    }

    @Override
    protected void readSubElements(String id, GeneratorAdder adder, List<Consumer<Generator>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case REGULATING_TERMINAL -> VoltageRegulationSerDe.readRegulatingTerminal(toApply, context);
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(toApply, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(toApply, context);
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(toApply, adder, context);
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }

    private void buildVoltageRegulationFromOlderVersions(NetworkDeserializerContext context, final GeneratorAdder adder,
                                                         Boolean voltageRegulatorOn) {
        // VOLTAGE REGULATION
        // version < V_1_17
        // Terminal null because remote terminal information come from subElements regulatingTerminal
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            createVoltageRegulationBackwardCompatibility(adder, Double.NaN, Double.NaN, voltageRegulatorOn, null));
        // version >= V_1_17 -> voltageRegulation is read with VoltageRegulationSerDe
        // Nothing to do
    }

    private static void addTargetV(NetworkDeserializerContext context, GeneratorAdder adder, double targetV, double equivalentLocalTargetV) {
        // version < V_1_17
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
            double newTargetV = targetV;
            if (!Double.isNaN(equivalentLocalTargetV)) {
                newTargetV = equivalentLocalTargetV;
            }
            adder.setLocalTargetV(newTargetV);
        });
        // From V_1_17
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> adder.setLocalTargetV(targetV));
    }

    private Boolean readVoltageRegulatorOnByVersion(NetworkDeserializerContext context) {
        AtomicReference<Boolean> voltageRegulatorOn = new AtomicReference<>();
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () ->
            voltageRegulatorOn.set(context.getReader().readBooleanAttribute("voltageRegulatorOn")));
        return voltageRegulatorOn.get();
    }

    private static <T extends AbstractOptions<T>> @Nonnull String getTargetQName(AbstractNetworkSerDeContext<T> context) {
        AtomicReference<String> targetQName = new AtomicReference<>("targetQ");
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> targetQName.set("localTargetQ"));
        return targetQName.get();
    }

    private static <T extends AbstractOptions<T>> @Nonnull String getTargetVName(AbstractNetworkSerDeContext<T> context) {
        AtomicReference<String> targetVName = new AtomicReference<>("targetV");
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> targetVName.set("localTargetV"));
        return targetVName.get();
    }
}
