/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.action.Action;
import com.powsybl.action.GeneratorActionBuilder;
import com.powsybl.action.PhaseTapChangerTapPositionActionBuilder;
import com.powsybl.action.RatioTapChangerTapPositionActionBuilder;
import com.powsybl.action.ShuntCompensatorPositionActionBuilder;
import com.powsybl.action.SwitchAction;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.remedialaction.NcPropertyReference;
import com.powsybl.nc.model.remedialaction.NcRelativeDirectionKind;
import com.powsybl.nc.model.remedialaction.NcRotatingMachineAction;
import com.powsybl.nc.model.remedialaction.NcShuntCompensatorModification;
import com.powsybl.nc.model.remedialaction.NcStaticPropertyRange;
import com.powsybl.nc.model.remedialaction.NcTapPositionAction;
import com.powsybl.nc.model.remedialaction.NcTopologyAction;
import com.powsybl.nc.model.remedialaction.NcValueOffsetKind;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class ActionConverter extends AbstractNcConverter {

    /**
     * IIDM alias types written by the CGMES importer for tap changers. They are duplicated here instead of
     * referencing {@code Conversion.ALIAS_RATIO_TAP_CHANGER*}, because this module must not depend on
     * cgmes-conversion. {@code CgmesTapChangerAliasTest} fails if the CGMES constants stop matching.
     */
    static final String RATIO_TAP_CHANGER_ALIAS_PREFIX = "CGMES.RatioTapChanger";
    static final String PHASE_TAP_CHANGER_ALIAS_PREFIX = "CGMES.PhaseTapChanger";

    private final Map<String, List<NcStaticPropertyRange>> rangesByAction;

    ActionConverter(NcModel model, Network network, ReportNode reportNode) {
        super(model, network, reportNode);

        rangesByAction = model.getStaticPropertyRanges().stream()
            .collect(Collectors.groupingBy(NcStaticPropertyRange::gridStateAlteration));
    }

    Map<String, Action> convert() {
        Map<String, Action> actions = new LinkedHashMap<>();

        for (NcTopologyAction action : model.getTopologyActions()) {
            add(actions, process(action));
        }
        for (NcShuntCompensatorModification action : model.getShuntCompensatorModifications()) {
            add(actions, process(action));
        }
        for (NcRotatingMachineAction action : model.getRotatingMachineActions()) {
            add(actions, process(action));
        }
        for (NcTapPositionAction action : model.getTapPositionActions()) {
            add(actions, process(action));
        }

        return actions;
    }

    private static void add(Map<String, Action> actions, Action action) {
        if (action != null) {
            actions.put(action.getId(), action);
        }
    }

    private Action process(NcTopologyAction action) {
        NcStaticPropertyRange range = singleRange(action.mrid());
        if (range == null) {
            return null;
        }
        Identifiable<?> element = action.switchId() == null ? null : network.getIdentifiable(action.switchId());
        if (!(element instanceof Switch)) {
            NcConversionReports.actionEquipmentNotFound(reportNode, action.mrid(), action.switchId());
            return null;
        }
        if (action.propertyReference() != NcPropertyReference.SWITCH_OPEN) {
            NcConversionReports.actionPropertyReferenceNotSupported(reportNode, action.mrid(),
                String.valueOf(action.propertyReference()), NcPropertyReference.SWITCH_OPEN.toString());
            return null;
        }
        if (!action.enabled()) {
            NcConversionReports.actionDisabled(reportNode, action.mrid());
            return null;
        }
        if (!hasExpectedPropertyReference(range, NcPropertyReference.SWITCH_OPEN, action.mrid())) {
            return null;
        }
        if (!isAbsolute(range)) {
            NcConversionReports.staticPropertyRangeNotAbsolute(reportNode, range.mrid(), action.mrid());
            return null;
        }
        double value = range.value();
        if (value != 0d && value != 1d) {
            NcConversionReports.staticPropertyRangeValueNotSupported(reportNode, range.mrid(), action.mrid(), value);
            return null;
        }
        return new SwitchAction(action.mrid(), element.getId(), value == 1d);
    }

    private Action process(NcShuntCompensatorModification action) {
        NcStaticPropertyRange range = singleRange(action.mrid());
        if (range == null) {
            return null;
        }
        Identifiable<?> element = action.shuntCompensatorId() == null ? null : network.getIdentifiable(action.shuntCompensatorId());
        if (!(element instanceof ShuntCompensator)) {
            NcConversionReports.actionEquipmentNotFound(reportNode, action.mrid(), action.shuntCompensatorId());
            return null;
        }
        if (action.propertyReference() != NcPropertyReference.SHUNT_COMPENSATOR_SECTIONS) {
            NcConversionReports.actionPropertyReferenceNotSupported(reportNode, action.mrid(),
                String.valueOf(action.propertyReference()), NcPropertyReference.SHUNT_COMPENSATOR_SECTIONS.toString());
            return null;
        }
        if (!action.enabled()) {
            NcConversionReports.actionDisabled(reportNode, action.mrid());
            return null;
        }
        if (!hasExpectedPropertyReference(range, NcPropertyReference.SHUNT_COMPENSATOR_SECTIONS, action.mrid())) {
            return null;
        }
        if (!isAbsolute(range)) {
            NcConversionReports.staticPropertyRangeNotAbsolute(reportNode, range.mrid(), action.mrid());
            return null;
        }
        double value = range.value();
        if (value < 0 || value != Math.rint(value)) {
            NcConversionReports.staticPropertyRangeValueNotSupported(reportNode, range.mrid(), action.mrid(), value);
            return null;
        }
        return new ShuntCompensatorPositionActionBuilder()
                .withId(action.mrid())
                .withShuntCompensatorId(element.getId())
                .withSectionCount((int) value)
                .build();
    }

    private Action process(NcRotatingMachineAction action) {
        NcStaticPropertyRange range = singleRange(action.mrid());
        if (range == null) {
            return null;
        }
        Identifiable<?> element = action.rotatingMachineId() == null ? null : network.getIdentifiable(action.rotatingMachineId());
        if (!(element instanceof Generator)) {
            NcConversionReports.actionEquipmentNotFound(reportNode, action.mrid(), action.rotatingMachineId());
            return null;
        }
        if (action.propertyReference() != NcPropertyReference.ROTATING_MACHINE_P) {
            NcConversionReports.actionPropertyReferenceNotSupported(reportNode, action.mrid(),
                String.valueOf(action.propertyReference()), NcPropertyReference.ROTATING_MACHINE_P.toString());
            return null;
        }
        if (!action.enabled()) {
            NcConversionReports.actionDisabled(reportNode, action.mrid());
            return null;
        }
        if (!hasExpectedPropertyReference(range, NcPropertyReference.ROTATING_MACHINE_P, action.mrid())) {
            return null;
        }
        boolean relative = range.valueKind() == NcValueOffsetKind.INCREMENTAL
            && (range.direction() == NcRelativeDirectionKind.UP || range.direction() == NcRelativeDirectionKind.DOWN);
        if (!relative && !isAbsolute(range)) {
            NcConversionReports.staticPropertyRangeDirectionNotSupported(reportNode, range.mrid(), action.mrid());
            return null;
        }
        double multiplier = range.direction() == NcRelativeDirectionKind.DOWN ? -1d : 1d;
        return new GeneratorActionBuilder()
                .withId(action.mrid())
                .withGeneratorId(element.getId())
                .withActivePowerValue(multiplier * range.value())
                .withActivePowerRelativeValue(relative)
                .build();
    }

    private Action process(NcTapPositionAction action) {
        NcStaticPropertyRange range = singleRange(action.mrid());
        if (range == null) {
            return null;
        }
        if (!action.enabled()) {
            NcConversionReports.actionDisabled(reportNode, action.mrid());
            return null;
        }
        if (action.propertyReference() != NcPropertyReference.TAP_CHANGER_STEP
            || !hasExpectedPropertyReference(range, NcPropertyReference.TAP_CHANGER_STEP, action.mrid())) {
            NcConversionReports.actionPropertyReferenceNotSupported(reportNode, action.mrid(),
                String.valueOf(action.propertyReference()), NcPropertyReference.TAP_CHANGER_STEP.toString());
            return null;
        }
        ResolvedTapChanger resolvedTapChanger = resolveTapChanger(action);
        if (resolvedTapChanger == null) {
            NcConversionReports.tapChangerNotResolved(reportNode, action.mrid(), action.tapChangerId());
            return null;
        }
        boolean relative = range.valueKind() == NcValueOffsetKind.INCREMENTAL
            && (range.direction() == NcRelativeDirectionKind.UP || range.direction() == NcRelativeDirectionKind.DOWN);
        if (!relative && !isAbsolute(range)) {
            NcConversionReports.staticPropertyRangeDirectionNotSupported(reportNode, range.mrid(), action.mrid());
            return null;
        }
        if (range.value() != Math.rint(range.value())) {
            NcConversionReports.staticPropertyRangeValueNotSupported(reportNode, range.mrid(), action.mrid(), range.value());
            return null;
        }
        int position = (int) (range.direction() == NcRelativeDirectionKind.DOWN ? -range.value() : range.value());
        if (resolvedTapChanger.target().ratioTapChanger()) {
            return new RatioTapChangerTapPositionActionBuilder()
                    .withId(action.mrid())
                    .withTransformerId(resolvedTapChanger.transformer().getId())
                    .withSide(resolvedTapChanger.target().side())
                    .withRelativeValue(relative).withTapPosition(position)
                    .build();
        }
        return new PhaseTapChangerTapPositionActionBuilder()
                .withId(action.mrid())
                .withTransformerId(resolvedTapChanger.transformer().getId()).withSide(resolvedTapChanger.target().side())
                .withRelativeValue(relative).withTapPosition(position)
                .build();
    }

    private ResolvedTapChanger resolveTapChanger(NcTapPositionAction action) {
        return resolve(targetFromAlias(action.tapChangerId()));
    }

    private TapChangerTarget targetFromAlias(String tapChangerId) {
        Identifiable<?> transformer = network.getIdentifiable(tapChangerId);
        if (!(transformer instanceof TwoWindingsTransformer) && !(transformer instanceof ThreeWindingsTransformer)) {
            return null;
        }
        String aliasType = transformer.getAliasType(tapChangerId).orElse(null);
        if (aliasType == null) {
            return null;
        }
        boolean ratioTapChanger;
        if (aliasType.startsWith(RATIO_TAP_CHANGER_ALIAS_PREFIX)) {
            ratioTapChanger = true;
        } else if (aliasType.startsWith(PHASE_TAP_CHANGER_ALIAS_PREFIX)) {
            ratioTapChanger = false;
        } else {
            return null;
        }
        return new TapChangerTarget(transformer.getId(), ratioTapChanger, sideFromAliasType(aliasType));
    }

    private ResolvedTapChanger resolve(TapChangerTarget target) {
        if (target == null) {
            return null;
        }
        Identifiable<?> transformer = network.getIdentifiable(target.transformerId());
        if (!(transformer instanceof TwoWindingsTransformer) && !(transformer instanceof ThreeWindingsTransformer)) {
            return null;
        }
        TapChangerTarget resolvedTarget = transformer instanceof TwoWindingsTransformer
            ? new TapChangerTarget(target.transformerId(), target.ratioTapChanger(), null)
            : target;
        if (transformer instanceof ThreeWindingsTransformer && resolvedTarget.side() == null) {
            return null;
        }
        return hasTapChanger(transformer, resolvedTarget.side(), resolvedTarget.ratioTapChanger())
            ? new ResolvedTapChanger(transformer, resolvedTarget)
            : null;
    }

    private static boolean hasTapChanger(Identifiable<?> transformer, ThreeSides side, boolean ratioTapChanger) {
        if (transformer instanceof TwoWindingsTransformer twoWindingsTransformer) {
            return ratioTapChanger ? twoWindingsTransformer.hasRatioTapChanger() : twoWindingsTransformer.hasPhaseTapChanger();
        }
        if (transformer instanceof ThreeWindingsTransformer threeWindingsTransformer) {
            ThreeWindingsTransformer.Leg leg = threeWindingsTransformer.getLeg(side);
            return ratioTapChanger ? leg.hasRatioTapChanger() : leg.hasPhaseTapChanger();
        }
        return false;
    }

    private static ThreeSides sideFromAliasType(String aliasType) {
        return switch (aliasType.charAt(aliasType.length() - 1)) {
            case '1' -> ThreeSides.ONE;
            case '2' -> ThreeSides.TWO;
            case '3' -> ThreeSides.THREE;
            default -> null;
        };
    }

    private record TapChangerTarget(String transformerId, boolean ratioTapChanger, ThreeSides side) {
    }

    private record ResolvedTapChanger(Identifiable<?> transformer, TapChangerTarget target) {
    }

    private NcStaticPropertyRange singleRange(String actionId) {
        List<NcStaticPropertyRange> ranges = rangesByAction.getOrDefault(actionId, List.of());
        if (ranges.isEmpty()) {
            NcConversionReports.actionWithoutStaticPropertyRange(reportNode, actionId);
            return null;
        }
        if (ranges.size() > 1) {
            NcConversionReports.actionWithMultipleStaticPropertyRanges(reportNode, actionId);
            return null;
        }
        return ranges.getFirst();
    }

    private boolean hasExpectedPropertyReference(NcStaticPropertyRange range, NcPropertyReference expected, String actionId) {
        if (range.propertyReference() != expected) {
            NcConversionReports.staticPropertyRangePropertyReferenceNotSupported(reportNode, range.mrid(), actionId,
                String.valueOf(range.propertyReference()), expected.toString());
            return false;
        }
        return true;
    }

    private static boolean isAbsolute(NcStaticPropertyRange range) {
        return range.valueKind() == NcValueOffsetKind.ABSOLUTE && range.direction() == NcRelativeDirectionKind.NONE;
    }
}
