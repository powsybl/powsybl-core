/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serializer.util.IidmSerializerUtil;

import java.util.Optional;
import java.util.function.Supplier;

import static com.powsybl.iidm.serializer.ConnectableSerializerUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DanglingLineSerializer extends AbstractSimpleIdentifiableSerializer<DanglingLine, DanglingLineAdder, VoltageLevel> {
    private static final String GENERATION = "generation";
    private static final String GENERATION_MAX_P = "generationMaxP";
    private static final String GENERATION_MIN_P = "generationMinP";
    private static final String GENERATION_TARGET_P = "generationTargetP";
    private static final String GENERATION_TARGET_Q = "generationTargetQ";
    private static final String GENERATION_TARGET_V = "generationTargetV";

    static final DanglingLineSerializer INSTANCE = new DanglingLineSerializer();

    static final String ROOT_ELEMENT_NAME = "danglingLine";
    static final String ARRAY_ELEMENT_NAME = "danglingLines";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(DanglingLine dl, VoltageLevel vl, NetworkSerializerWriterContext context) {
        writeRootElementAttributesInternal(dl, dl::getTerminal, context);
    }

    static void writeRootElementAttributesInternal(DanglingLine dl, Supplier<Terminal> terminalGetter, NetworkSerializerWriterContext context) {
        DanglingLine.Generation generation = dl.getGeneration();
        double[] p0 = new double[1];
        double[] q0 = new double[1];
        p0[0] = dl.getP0();
        q0[0] = dl.getQ0();
        if (generation != null) {
            IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, GENERATION, IidmSerializerUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_3, context);
            IidmSerializerUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
                if (!Double.isNaN(generation.getTargetP())) {
                    p0[0] -= generation.getTargetP();
                }
                if (!Double.isNaN(generation.getTargetQ())) {
                    q0[0] -= generation.getTargetQ();
                }
            });
        }
        context.getWriter().writeDoubleAttribute("p0", p0[0]);
        context.getWriter().writeDoubleAttribute("q0", q0[0]);
        context.getWriter().writeDoubleAttribute("r", dl.getR());
        context.getWriter().writeDoubleAttribute("x", dl.getX());
        context.getWriter().writeDoubleAttribute("g", dl.getG());
        context.getWriter().writeDoubleAttribute("b", dl.getB());
        if (generation != null) {
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
                context.getWriter().writeBooleanAttribute("generationVoltageRegulationOn", generation.isVoltageRegulationOn());
                context.getWriter().writeDoubleAttribute(GENERATION_MIN_P, generation.getMinP());
                context.getWriter().writeDoubleAttribute(GENERATION_MAX_P, generation.getMaxP());
                context.getWriter().writeDoubleAttribute(GENERATION_TARGET_P, generation.getTargetP());
                context.getWriter().writeDoubleAttribute(GENERATION_TARGET_V, generation.getTargetV());
                context.getWriter().writeDoubleAttribute(GENERATION_TARGET_Q, generation.getTargetQ());
            });
        }
        Terminal t = terminalGetter.get();
        writeNodeOrBus(null, t, context);
        if (dl.getPairingKey() != null) {
            IidmSerializerUtil.runUntilMaximumVersion(IidmVersion.V_1_10, context,
                () -> context.getWriter().writeStringAttribute("ucteXnodeCode", dl.getPairingKey())
            );
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_11, context,
                () -> context.getWriter().writeStringAttribute("pairingKey", dl.getPairingKey())
            );
        }
        writePQ(null, t, context.getWriter());

    }

    @Override
    protected DanglingLineAdder createAdder(VoltageLevel parent) {
        return parent.newDanglingLine();
    }

    static boolean hasValidGeneration(DanglingLine dl, NetworkSerializerWriterContext context) {
        if (dl.getGeneration() != null) {
            return context.getVersion().compareTo(IidmVersion.V_1_3) > 0;
        }
        return false;
    }

    @Override
    protected void writeSubElements(DanglingLine dl, VoltageLevel vl, NetworkSerializerWriterContext context) {
        if (dl.getGeneration() != null) {
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> ReactiveLimitsSerializer.INSTANCE.write(dl.getGeneration(), context));
        }
        Optional<ActivePowerLimits> activePowerLimits = dl.getActivePowerLimits();
        if (activePowerLimits.isPresent()) {
            IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS, IidmSerializerUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_5, context);
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> writeActivePowerLimits(null, activePowerLimits.get(), context.getWriter(),
                    context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<ApparentPowerLimits> apparentPowerLimits = dl.getApparentPowerLimits();
        if (apparentPowerLimits.isPresent()) {
            IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS, IidmSerializerUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_5, context);
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> writeApparentPowerLimits(null, apparentPowerLimits.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions()));
        }
        Optional<CurrentLimits> currentLimits = dl.getCurrentLimits();
        if (currentLimits.isPresent()) {
            writeCurrentLimits(null, currentLimits.get(), context.getWriter(), context.getVersion(), context.isValid(), context.getOptions());
        }
    }

    @Override
    protected DanglingLine readRootElementAttributes(DanglingLineAdder adder, VoltageLevel voltageLevel, NetworkSerializerReaderContext context) {
        readRootElementAttributesInternal(adder, context);
        IidmSerializerUtil.runUntilMaximumVersion(IidmVersion.V_1_10, context, () -> {
            String pairingKey = context.getReader().readStringAttribute("ucteXnodeCode");
            adder.setPairingKey(pairingKey);
        });
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_11, context, () -> {
            String pairingKey = context.getReader().readStringAttribute("pairingKey");
            adder.setPairingKey(pairingKey);
        });
        DanglingLine dl = adder.add();
        readPQ(null, dl.getTerminal(), context.getReader());
        return dl;
    }

    public static void readRootElementAttributesInternal(DanglingLineAdder adder, NetworkSerializerReaderContext context) {
        double p0 = context.getReader().readDoubleAttribute("p0");
        double q0 = context.getReader().readDoubleAttribute("q0");
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        double g = context.getReader().readDoubleAttribute("g");
        double b = context.getReader().readDoubleAttribute("b");
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            Boolean voltageRegulationOn = context.getReader().readBooleanAttribute("generationVoltageRegulationOn");
            if (voltageRegulationOn != null) {
                double minP = context.getReader().readDoubleAttribute(GENERATION_MIN_P);
                double maxP = context.getReader().readDoubleAttribute(GENERATION_MAX_P);
                double targetP = context.getReader().readDoubleAttribute(GENERATION_TARGET_P);
                double targetV = context.getReader().readDoubleAttribute(GENERATION_TARGET_V);
                double targetQ = context.getReader().readDoubleAttribute(GENERATION_TARGET_Q);
                adder.newGeneration()
                        .setMinP(minP)
                        .setMaxP(maxP)
                        .setVoltageRegulationOn(voltageRegulationOn)
                        .setTargetP(targetP)
                        .setTargetV(targetV)
                        .setTargetQ(targetQ)
                        .add();
            }
        });
        readNodeOrBus(adder, context);
        adder.setP0(p0)
                .setQ0(q0)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b);
    }

    @Override
    protected void readSubElements(DanglingLine dl, NetworkSerializerReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ACTIVE_POWER_LIMITS -> {
                    IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, ACTIVE_POWER_LIMITS, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(dl.newActivePowerLimits(), context.getReader()));
                }
                case APPARENT_POWER_LIMITS -> {
                    IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, APPARENT_POWER_LIMITS, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(dl.newApparentPowerLimits(), context.getReader()));
                }
                case CURRENT_LIMITS -> readCurrentLimits(dl.newCurrentLimits(), context.getReader());
                case ReactiveLimitsSerializer.ELEM_REACTIVE_CAPABILITY_CURVE -> {
                    IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME + ".generation", "reactiveLimits", IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                    ReactiveLimitsSerializer.INSTANCE.readReactiveCapabilityCurve(dl.getGeneration(), context);
                }
                case ReactiveLimitsSerializer.ELEM_MIN_MAX_REACTIVE_LIMITS -> {
                    IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME + ".generation", "reactiveLimits", IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                    ReactiveLimitsSerializer.INSTANCE.readMinMaxReactiveLimits(dl.getGeneration(), context);
                }
                default -> readSubElement(elementName, dl, context);
            }
        });
    }
}
