/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.BoundaryLine.Generation;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */

class BoundaryLineSerDe extends AbstractComplexIdentifiableSerDe<BoundaryLine, BoundaryLineAdder, VoltageLevel> {
    private static final String GENERATION = "generation";
    private static final String GENERATION_MAX_P = "generationMaxP";
    private static final String GENERATION_MIN_P = "generationMinP";
    private static final String GENERATION_TARGET_P = "generationTargetP";
    // Version < v1_19
    private static final String GENERATION_TARGET_Q = "generationTargetQ";
    private static final String GENERATION_TARGET_V = "generationTargetV";
    private static final String GENERATION_VOLTAGE_REGULATION_ON = "generationVoltageRegulationOn";
    // Version >= v1_19
    private static final String GENERATION_LOCAL_TARGET_Q = "generationLocalTargetQ";
    private static final String GENERATION_LOCAL_TARGET_V = "generationLocalTargetV";

    static final BoundaryLineSerDe INSTANCE = new BoundaryLineSerDe();

    static final String ROOT_ELEMENT_NAME = "boundaryLine";
    static final String ARRAY_ELEMENT_NAME = "boundaryLines";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(BoundaryLine bl, VoltageLevel vl, NetworkSerializerContext context) {
        writeEquivalent(bl, context);
        writeRootElementAttributesInternal(INSTANCE.getRootElementName(), bl, bl::getTerminal, context);
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
                if (!Double.isNaN(generation.getLocalTargetQ())) {
                    q0[0] -= generation.getLocalTargetQ();
                }
            });
        }
        context.getWriter().writeDoubleAttribute("p0", p0[0]);
        context.getWriter().writeDoubleAttribute("q0", q0[0]);
        context.getWriter().writeDoubleAttribute("r", bl.getR());
        context.getWriter().writeDoubleAttribute("x", bl.getX());
        IidmSerDeUtil.writeFormerlyMandatoryDoubleAttribute("g", bl.getG(), IidmVersion.V_1_17, context);
        IidmSerDeUtil.writeFormerlyMandatoryDoubleAttribute("b", bl.getB(), IidmVersion.V_1_17, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_18, context, () ->
                context.getWriter().writeOptionalBooleanAttribute(GENERATION_VOLTAGE_REGULATION_ON,
                    getOptionalValue(generation, g -> g.isRegulatingWithMode(RegulationMode.VOLTAGE))));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_MIN_P, getOptionalValue(generation, Generation::getMinP));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_MAX_P, getOptionalValue(generation, Generation::getMaxP));
            context.getWriter().writeOptionalDoubleAttribute(GENERATION_TARGET_P, getOptionalValue(generation, Generation::getTargetP));
            // Version < v1_19
            IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_18, context, () ->
                context.getWriter().writeOptionalDoubleAttribute(GENERATION_TARGET_V, getOptionalValue(generation, Generation::getLocalTargetV)));
            IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_18, context, () ->
                context.getWriter().writeOptionalDoubleAttribute(GENERATION_TARGET_Q, getOptionalValue(generation, Generation::getLocalTargetQ)));
            // Version >= v1_19
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_19, context, () ->
                context.getWriter().writeOptionalDoubleAttribute(GENERATION_LOCAL_TARGET_V, getOptionalValue(generation, Generation::getLocalTargetV)));
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_19, context, () ->
                context.getWriter().writeOptionalDoubleAttribute(GENERATION_LOCAL_TARGET_Q, getOptionalValue(generation, Generation::getLocalTargetQ)));
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
            VoltageRegulationSerDe.writeLocalVoltageRegulation(dl.getGeneration().getVoltageRegulation(), context);
        }
        writeLimits(context, null, getRootElementName(), dl.getSelectedOperationalLimitsGroup().orElse(null),
            context.getOptions().isOnlySelectedOperationalLimitsGroups() ? dl.getAllSelectedOperationalLimitsGroups() : dl.getOperationalLimitsGroups());
    }

    @Override
    protected void readRootElementAttributes(BoundaryLineAdder adder, VoltageLevel voltageLevel, List<Consumer<BoundaryLine>> toApply, NetworkDeserializerContext context) {
        checkVersion(context);
        readEquivalent(adder, context);
        double p0 = context.getReader().readDoubleAttribute("p0");
        double q0 = context.getReader().readDoubleAttribute("q0");
        double r = context.getReader().readDoubleAttribute("r");
        double x = context.getReader().readDoubleAttribute("x");
        double g = IidmSerDeUtil.readFormerlyMandatoryDoubleAttribute("g", IidmVersion.V_1_17, context);
        double b = IidmSerDeUtil.readFormerlyMandatoryDoubleAttribute("b", IidmVersion.V_1_17, context);
        List<Consumer<BoundaryLineAdder.GenerationAdder>> generationAdderToApply = new ArrayList<>();
        readGenerationAttributes(generationAdderToApply, context);
        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        adder.setP0(p0)
            .setQ0(q0)
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_10, context, () -> {
            String pairingKey = context.getReader().readStringAttribute("ucteXnodeCode");
            adder.setPairingKey(pairingKey);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_11, context, () -> {
            String pairingKey = context.getReader().readStringAttribute("pairingKey");
            adder.setPairingKey(pairingKey);
        });
        readPQ(toApply, context.getReader());
        IidmSerDeUtil.runInBetweenTwoVersions(IidmVersion.V_1_12, IidmVersion.V_1_15, context, () ->
            readSelectedGroupIdDeferred(toApply, context));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () ->
            readAllSelectedGroupIds(toApply, context));

        // Child nodes are read here, before the adder is built, because the generation must be complete when it is added
        List<Consumer<Generation>> generationToApply = new ArrayList<>();
        context.getReader().readChildNodes(elementName -> readChildNode(toApply, generationAdderToApply, generationToApply, context, elementName));
        if (!generationAdderToApply.isEmpty()) {
            BoundaryLineAdder.GenerationAdder generationAdder = adder.newGeneration();
            generationAdderToApply.forEach(setter -> setter.accept(generationAdder));
            generationAdder.add();
        }
        if (!generationToApply.isEmpty()) {
            toApply.add(bl -> generationToApply.forEach(setter -> setter.accept(bl.getGeneration())));
        }
    }

    protected void checkVersion(NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(getRootElementName(), IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_16, context);
    }

    /**
     * Reads the generation attributes and, when needed, queues their application on the generation adder
     */
    private static void readGenerationAttributes(List<Consumer<BoundaryLineAdder.GenerationAdder>> generationAdderToApply, NetworkDeserializerContext context) {
        if (context.getVersion().compareTo(IidmVersion.V_1_3) < 0) {
            return;
        }

        String targetVAttribute;
        String targetQAttribute;
        if (context.getVersion().compareTo(IidmVersion.V_1_19) < 0) {
            targetVAttribute = GENERATION_TARGET_V;
            targetQAttribute = GENERATION_TARGET_Q;
        } else {
            targetVAttribute = GENERATION_LOCAL_TARGET_V;
            targetQAttribute = GENERATION_LOCAL_TARGET_Q;
        }
        Optional<Boolean> voltageRegulationOn = context.getReader().readOptionalBooleanAttribute(GENERATION_VOLTAGE_REGULATION_ON);
        OptionalDouble minP = context.getReader().readOptionalDoubleAttribute(GENERATION_MIN_P);
        OptionalDouble maxP = context.getReader().readOptionalDoubleAttribute(GENERATION_MAX_P);
        OptionalDouble targetP = context.getReader().readOptionalDoubleAttribute(GENERATION_TARGET_P);
        OptionalDouble localTargetV = context.getReader().readOptionalDoubleAttribute(targetVAttribute);
        OptionalDouble localTargetQ = context.getReader().readOptionalDoubleAttribute(targetQAttribute);

        boolean hasGenerationAttributes = voltageRegulationOn.isPresent() || minP.isPresent() || maxP.isPresent()
            || targetP.isPresent() || localTargetV.isPresent() || localTargetQ.isPresent();
        if (!hasGenerationAttributes) {
            return;
        }

        generationAdderToApply.add(generationAdder -> {
            if (voltageRegulationOn.isPresent()) {
                generationAdder.newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withRegulating(voltageRegulationOn.get())
                    .add();
            }
            minP.ifPresent(generationAdder::setMinP);
            maxP.ifPresent(generationAdder::setMaxP);
            targetP.ifPresent(generationAdder::setTargetP);
            localTargetV.ifPresent(generationAdder::setLocalTargetV);
            localTargetQ.ifPresent(generationAdder::setLocalTargetQ);
        });
    }

    /**
     * Reads the local voltage regulation child element (version 1.19 and above) and queues its application on the generation adder
     */
    private static void readLocalVoltageRegulation(List<Consumer<BoundaryLineAdder.GenerationAdder>> generationAdderToApply, NetworkDeserializerContext context) {
        double targetDeadband = context.getReader().readDoubleAttribute(VoltageRegulationSerDe.TARGET_DEADBAND);
        double slope = context.getReader().readDoubleAttribute(VoltageRegulationSerDe.SLOPE);
        RegulationMode mode = context.getReader().readEnumAttribute(VoltageRegulationSerDe.MODE, RegulationMode.class);
        boolean regulating = context.getReader().readBooleanAttribute(VoltageRegulationSerDe.REGULATING);
        context.getReader().readChildNodes(elementName -> {
            throw new PowsyblException("Unknown sub element name '" + elementName + "' in 'voltageRegulation'");
        });
        generationAdderToApply.add(generationAdder -> generationAdder.newVoltageRegulation()
            .withTargetDeadband(targetDeadband)
            .withSlope(slope)
            .withMode(mode)
            .withRegulating(regulating)
            .add());
    }

    @Override
    protected void readSubElements(String id, BoundaryLineAdder adder, List<Consumer<BoundaryLine>> toApply, NetworkDeserializerContext context) {
        // All sub-elements are read in readRootElementAttributes, before the adder is added
    }

    private void readChildNode(List<Consumer<BoundaryLine>> toApply, List<Consumer<BoundaryLineAdder.GenerationAdder>> generationAdderToApply,
                               List<Consumer<Generation>> generationToApply, NetworkDeserializerContext context, String elementName) {
        switch (elementName) {
            case LIMITS_GROUP -> {
                IidmSerDeUtil.assertMinimumVersion(getRootElementName(), LIMITS_GROUP, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_12, context);
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_12, context, () -> readLoadingLimitsGroupsDeferred(toApply, LIMITS_GROUP, context));
            }
            case ACTIVE_POWER_LIMITS -> {
                IidmSerDeUtil.assertMinimumVersion(getRootElementName(), ACTIVE_POWER_LIMITS, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readSelectedGroupLimitsDeferred(toApply, readActivePowerLimitsDeferred(context)));
            }
            case APPARENT_POWER_LIMITS -> {
                IidmSerDeUtil.assertMinimumVersion(getRootElementName(), APPARENT_POWER_LIMITS, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_5, context);
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_5, context, () -> readSelectedGroupLimitsDeferred(toApply, readApparentPowerLimitsDeferred(context)));
            }
            case CURRENT_LIMITS ->
                readSelectedGroupLimitsDeferred(toApply, readCurrentLimitsDeferred(context));
            case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> {
                IidmSerDeUtil.assertMinimumVersion(getRootElementName() + ".generation", "reactiveLimits", IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(generationToApply, context);
            }
            case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> {
                IidmSerDeUtil.assertMinimumVersion(getRootElementName() + ".generation", "reactiveLimits", IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(generationToApply, context);
            }
            case VoltageRegulationSerDe.ELEMENT_NAME -> {
                IidmSerDeUtil.assertMinimumVersion(getRootElementName() + ".generation", VoltageRegulationSerDe.ELEMENT_NAME, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_19, context);
                readLocalVoltageRegulation(generationAdderToApply, context);
            }

            default -> readSubElement(elementName, getRootElementName(), toApply, context);
        }
    }

    /**
     * Applies the legacy limits (versions 1.5 to 1.11) on the selected operational limits group of the boundary line
     */
    private static void readSelectedGroupLimitsDeferred(List<Consumer<BoundaryLine>> toApply, Consumer<OperationalLimitsGroup> limitsSetter) {
        toApply.add(bl -> limitsSetter.accept(bl.getOrCreateSelectedOperationalLimitsGroup()));
    }
}
