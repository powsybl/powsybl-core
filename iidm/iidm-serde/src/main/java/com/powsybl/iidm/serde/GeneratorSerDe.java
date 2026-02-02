/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorSerDe extends AbstractSimpleIdentifiableSerDe<Generator, GeneratorAdder, VoltageLevel> {

    static final GeneratorSerDe INSTANCE = new GeneratorSerDe();

    static final String ROOT_ELEMENT_NAME = "generator";
    static final String ARRAY_ELEMENT_NAME = "generators";

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
        writeVoltageRegulatorOnByVersion(g, context);
        context.getWriter().writeDoubleAttribute("targetP", g.getTargetP());
        writeTargetVByVersion(g, context);
        context.getWriter().writeDoubleAttribute("targetQ", g.getTargetQ());
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_13, context, () ->
            context.getWriter().writeBooleanAttribute("isCondenser", g.isCondenser(), false));
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_15, IidmVersion.V_1_15, context, () ->
            context.getWriter().writeDoubleAttribute("equivalentLocalTargetV", g.getEquivalentLocalTargetV(), Double.NaN));
        writeNodeOrBus(null, g.getTerminal(), context);
        writePQ(null, g.getTerminal(), context.getWriter());
    }

    private static void writeVoltageRegulatorOnByVersion(Generator g, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () ->
            context.getWriter().writeBooleanAttribute("voltageRegulatorOn", g.isVoltageRegulatorOn()));
    }

    private static void writeTargetVByVersion(Generator g, NetworkSerializerContext context) {
        double localTargetV = g.getTargetV();
        double remoteTargetV = g.getRemoteTargetV();
        context.getWriter().writeDoubleAttribute("targetV", Double.isNaN(remoteTargetV) ? localTargetV : remoteTargetV);
    }

    @Override
    protected void writeSubElements(Generator g, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> {
            if (g != g.getRegulatingTerminal().getConnectable()) {
                TerminalRefSerDe.writeTerminalRef(g.getRegulatingTerminal(), context, "regulatingTerminal");
            }
        });
        ReactiveLimitsSerDe.INSTANCE.write(g, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> VoltageRegulationSerDe.writeVoltageRegulation(g.getVoltageRegulation(), context));
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
        readAndSetVoltageRegulatorOnByVersion(context, adder);
        double targetP = context.getReader().readDoubleAttribute("targetP");
        double targetV = context.getReader().readDoubleAttribute("targetV");
        double targetQ = context.getReader().readDoubleAttribute("targetQ");
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_13, context, () ->
                adder.setCondenser(context.getReader().readBooleanAttribute("isCondenser", false)));
        adder.setTargetP(targetP)
            .setTargetQ(targetQ);
        // Since V_1_15 -> use 'setTargetV(targetV, equivalentLocalTargetV)' instead of 'setTargetV(targetV)'
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_14, context, () ->
            adder.setTargetV(targetV));
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_15, IidmVersion.V_1_15, context, () ->
            adder.setTargetV(targetV, context.getReader().readDoubleAttribute("equivalentLocalTargetV", Double.NaN)));
        // From 1_16 to infinite
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> adder.setTargetV(targetV));

        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        Generator g = adder.add();
        readPQ(null, g.getTerminal(), context.getReader());
        return g;
    }

    @Override
    protected void readSubElements(Generator g, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case "regulatingTerminal" -> TerminalRefSerDe.readTerminalRef(context, g.getNetwork(), g::setRegulatingTerminal);
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(g, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(g, context);
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(g, context, g.getNetwork());
                default -> readSubElement(elementName, g, context);
            }
        });
    }

    private void readAndSetVoltageRegulatorOnByVersion(NetworkDeserializerContext context, final GeneratorAdder adder) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> {
            boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
            adder.setVoltageRegulatorOn(voltageRegulatorOn);
        });
    }
}
