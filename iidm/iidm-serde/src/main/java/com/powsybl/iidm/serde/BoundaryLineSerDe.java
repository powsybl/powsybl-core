/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.BoundaryLine.Generation;
import com.powsybl.iidm.network.BoundaryLineAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */

class BoundaryLineSerDe extends AbstractSimpleIdentifiableSerDe<BoundaryLine, BoundaryLineAdder, VoltageLevel> {
    private static final String GENERATION = "generation";
    private static final String GENERATION_MAX_P = "generationMaxP";
    private static final String GENERATION_MIN_P = "generationMinP";
    private static final String GENERATION_TARGET_P = "generationTargetP";
    private static final String GENERATION_TARGET_Q = "generationTargetQ";
    private static final String GENERATION_TARGET_V = "generationTargetV";

    static final BoundaryLineSerDe INSTANCE = new BoundaryLineSerDe();

    static final String ROOT_ELEMENT_NAME = "boundaryLine";
    static final String ARRAY_ELEMENT_NAME = "boundaryLines";
    private static final String GENERATION_CONSTANT = ".generation";
    private static final String REACTIVE_LIMITS = "reactiveLimits";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(BoundaryLine dl, VoltageLevel vl, NetworkSerializerContext context) {
        writeRootElementAttributesInternal(INSTANCE.getRootElementName(), dl, dl::getTerminal, context);
    }

    static void writeRootElementAttributesInternal(String rootElementName, BoundaryLine bl, Supplier<Terminal> terminalGetter, NetworkSerializerContext context) {
        Generation generation = bl.getGeneration();
        double[] p0 = new double[1];
        double[] q0 = new double[1];
        p0[0] = bl.getP0();
        q0[0] = bl.getQ0();
        if (generation != null) {
            IidmSerDeUtil.assertMinimumVersion(rootElementName, GENERATION, IidmSerDeUtil.ErrorMessage.NOT_NULL_NOT_SUPPORTED, IidmVersion.V_1_3, context);
            IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
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
        context.getWriter().writeDoubleAttribute("r", bl.getR());
        context.getWriter().writeDoubleAttribute("x", bl.getX());
        context.getWriter().writeDoubleAttribute("g", bl.getG());
        context.getWriter().writeDoubleAttribute("b", bl.getB());
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            context.getWriter().writeOptionalBooleanAttribute("generationVoltageRegulationOn", getOptionalValue(generation, Generation::isVoltageRegulationOn));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_MIN_P, getOptionalValue(generation, Generation::getMinP));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_MAX_P, getOptionalValue(generation, Generation::getMaxP));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_TARGET_P, getOptionalValue(generation, Generation::getTargetP));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_TARGET_V, getOptionalValue(generation, Generation::getTargetV));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_TARGET_Q, getOptionalValue(generation, Generation::getTargetQ));
        });
        Terminal t = terminalGetter.get();
        writeNodeOrBus(null, t, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_10, context,
            () -> context.getWriter().writeStringAttribute("ucteXnodeCode", bl.getPairingKey())
        );
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_11, context,
            () -> context.getWriter().writeStringAttribute("pairingKey", bl.getPairingKey())
        );
        writePQ(null, t, context.getWriter());
        IidmSerDeUtil.runInBetweenTwoVersions(IidmVersion.V_1_12, IidmVersion.V_1_15, context, () ->
                writeSelectedGroupId(null, bl.getSelectedOperationalLimitsGroupId().orElse(null), context.getWriter()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () ->
                writeAllSelectedGroupIds(bl, context.getWriter()));
    }

    private static <T> T getOptionalValue(Generation generation, Function<Generation, T> valueGetter) {
        return Optional.ofNullable(generation).map(valueGetter).orElse(null);
    }

    @Override
    protected BoundaryLineAdder createAdder(VoltageLevel parent) {
        return parent.newBoundaryLine();
    }

    @Override
    protected void writeSubElements(BoundaryLine dl, VoltageLevel vl, NetworkSerializerContext context) {
        if (dl.getGeneration() != null) {
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> ReactiveLimitsSerDe.INSTANCE.write(dl.getGeneration(), context));
        }
        writeLimits(context, null, getRootElementName(), dl.getSelectedOperationalLimitsGroup().orElse(null), dl.getOperationalLimitsGroups());
    }

    @Override
    protected BoundaryLine readRootElementAttributes(BoundaryLineAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        checkVersion(context);
        readRootElementAttributesInternal(adder, voltageLevel, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_10, context, () -> {
            String pairingKey = context.getReader().readStringAttribute("ucteXnodeCode");
            adder.setPairingKey(pairingKey);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_11, context, () -> {
            String pairingKey = context.getReader().readStringAttribute("pairingKey");
            adder.setPairingKey(pairingKey);
        });
        BoundaryLine bl = adder.add();
        readPQ(null, bl.getTerminal(), context.getReader());
        IidmSerDeUtil.runInBetweenTwoVersions(IidmVersion.V_1_12, IidmVersion.V_1_15, context, () ->
                readSelectedGroupId(null, bl::setSelectedOperationalLimitsGroup, context));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () ->
            readAllSelectedGroupIds(bl, context));
        return bl;
    }

    protected void checkVersion(NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(getRootElementName(), IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_16, context);
    }

    public static void readRootElementAttributesInternal(BoundaryLineAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        double p0 = context.getReader().readDoubleAttribute("p0");
        double q0 = context.getReader().readDoubleAttribute("q0");
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        double g = context.getReader().readDoubleAttribute("g");
        double b = context.getReader().readDoubleAttribute("b");
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            Optional<Boolean> voltageRegulationOn = context.getReader().readOptionalBooleanAttribute("generationVoltageRegulationOn");
            OptionalDouble minP = context.getReader().readOptionalDoubleAttribute(GENERATION_MIN_P);
            OptionalDouble maxP = context.getReader().readOptionalDoubleAttribute(GENERATION_MAX_P);
            OptionalDouble targetP = context.getReader().readOptionalDoubleAttribute(GENERATION_TARGET_P);
            OptionalDouble targetV = context.getReader().readOptionalDoubleAttribute(GENERATION_TARGET_V);
            OptionalDouble targetQ = context.getReader().readOptionalDoubleAttribute(GENERATION_TARGET_Q);
            if (voltageRegulationOn.isPresent()) {
                BoundaryLineAdder.GenerationAdder generationAdder = adder.newGeneration()
                        .setVoltageRegulationOn(voltageRegulationOn.get());
                minP.ifPresent(generationAdder::setMinP);
                maxP.ifPresent(generationAdder::setMaxP);
                targetP.ifPresent(generationAdder::setTargetP);
                targetV.ifPresent(generationAdder::setTargetV);
                targetQ.ifPresent(generationAdder::setTargetQ);
                generationAdder.add();
            }
        });
        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        adder.setP0(p0)
                .setQ0(q0)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b);
    }

    @Override
    protected void readSubElements(BoundaryLine dl, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case LIMITS_GROUP -> {
                    IidmSerDeUtil.assertMinimumVersion(getRootElementName(), LIMITS_GROUP, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroups(dl, LIMITS_GROUP, context));
                }
                case ACTIVE_POWER_LIMITS -> {
                    IidmSerDeUtil.assertMinimumVersion(getRootElementName(), ACTIVE_POWER_LIMITS, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readActivePowerLimits(dl.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits(), context));
                }
                case APPARENT_POWER_LIMITS -> {
                    IidmSerDeUtil.assertMinimumVersion(getRootElementName(), APPARENT_POWER_LIMITS, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readApparentPowerLimits(dl.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits(), context));
                }
                case CURRENT_LIMITS -> readCurrentLimits(dl.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits(), context);
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_SHAPE -> {
                    IidmSerDeUtil.assertMinimumVersion(getRootElementName() + GENERATION_CONSTANT, REACTIVE_LIMITS, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                    ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityShape(dl.getGeneration(), context);
                }
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> {
                    IidmSerDeUtil.assertMinimumVersion(getRootElementName() + GENERATION_CONSTANT, REACTIVE_LIMITS, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                    ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(dl.getGeneration(), context);
                }
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> {
                    IidmSerDeUtil.assertMinimumVersion(getRootElementName() + GENERATION_CONSTANT, REACTIVE_LIMITS, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                    ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(dl.getGeneration(), context);
                }
                default -> readSubElement(elementName, dl, context);
            }
        });
    }
}
