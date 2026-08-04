/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.export.NetworkEventRecorderSshExport.UnsupportedChangeBehavior;
import com.powsybl.cgmes.conversion.export.elements.RegulatingControlEq;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

import static com.powsybl.cgmes.conversion.Conversion.*;

/**
 * Translates the changes recorded on an IIDM network into the CGMES properties of a partial Steady State
 * Hypothesis file.
 *
 * <p>Only the steady state changes listed in the CGMES SSH profile can be translated. Anything else is reported
 * through {@link UnsupportedChangeBehavior}, either by failing or by logging a warning and skipping the change.
 * The exporter never emits a description of an object that the receiving side would not be able to resolve.</p>
 *
 * <p>Two rules drive most of the mapping decisions:</p>
 * <ul>
 *     <li>A property is written only when the change actually affects it, so that the receiving side keeps its
 *     previous value for everything else. The one exception is a group of properties that the CGMES importer only
 *     accepts as a whole, such as the active and reactive power of an injection.</li>
 *     <li>The exported value is always the one that is consistent with the current state of the network, not the
 *     one carried by the event. Compaction and the buffering done by {@link PartialSshUpdates} therefore cannot
 *     produce a file that contradicts the network it was exported from.</li>
 * </ul>
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class PartialSshEventCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartialSshEventCollector.class);

    private static final String OPEN = "open";
    private static final String P0 = "p0";
    private static final String Q0 = "q0";
    private static final String TARGET_P = "targetP";
    private static final String TARGET_Q = "targetQ";
    private static final String TARGET_V = "targetV";
    private static final String VOLTAGE_REGULATOR_ON = "voltageRegulatorOn";
    private static final String SECTION_COUNT = "sectionCount";
    private static final String VOLTAGE_SETPOINT = "voltageSetpoint";
    private static final String REACTIVE_POWER_SETPOINT = "reactivePowerSetpoint";
    private static final String ACTIVE_POWER_SETPOINT = "activePowerSetpoint";
    private static final String TAP_POSITION_SUFFIX = ".tapPosition";
    private static final String PHASE_TAP_CHANGER_PREFIX = "phaseTapChanger";
    private static final String RATIO_TAP_CHANGER_PREFIX = "ratioTapChanger";

    private static final String REGULATING_COND_EQ_CONTROL_ENABLED = "RegulatingCondEq.controlEnabled";
    private static final String ACDC_TERMINAL_CONNECTED = "ACDCTerminal.connected";
    private static final String ROTATING_MACHINE_P = "RotatingMachine.p";
    private static final String ROTATING_MACHINE_Q = "RotatingMachine.q";
    private static final String REGULATING_CONTROL = "RegulatingControl";
    private static final String UNIT_MULTIPLIER = "UnitMultiplier";
    private static final String KILO = "k";
    private static final String MEGA = "M";

    private static final Set<String> LOAD_ATTRIBUTES = Set.of(P0, Q0);
    private static final Set<String> GENERATOR_ATTRIBUTES = Set.of(TARGET_P, TARGET_Q, TARGET_V, VOLTAGE_REGULATOR_ON);
    private static final Set<String> SHUNT_ATTRIBUTES = Set.of(SECTION_COUNT, TARGET_V, VOLTAGE_REGULATOR_ON);
    private static final Set<String> STATIC_VAR_COMPENSATOR_ATTRIBUTES = Set.of(VOLTAGE_SETPOINT, REACTIVE_POWER_SETPOINT);

    private final Network network;
    private final CgmesExportContext context;
    private final UnsupportedChangeBehavior unsupportedChangeBehavior;
    private final PartialSshUpdates updates = new PartialSshUpdates();

    PartialSshEventCollector(Network network, CgmesExportContext context, UnsupportedChangeBehavior unsupportedChangeBehavior) {
        this.network = network;
        this.context = context;
        this.unsupportedChangeBehavior = unsupportedChangeBehavior;
    }

    PartialSshUpdates collect(Collection<NetworkEvent> events) {
        for (NetworkEvent event : NetworkEventRecorderSshExport.compactEvents(events)) {
            if (event instanceof UpdateNetworkEvent updateEvent) {
                collectUpdate(updateEvent);
            } else {
                reject(event, "only attribute updates can be exported, but this is a "
                        + event.getClass().getSimpleName());
            }
        }
        return updates;
    }

    private void collectUpdate(UpdateNetworkEvent event) {
        Identifiable<?> identifiable = network.getIdentifiable(event.id());
        if (identifiable == null) {
            reject(event, "the network has no identifiable with id " + event.id());
            return;
        }
        String attribute = event.attribute();
        switch (identifiable) {
            case Switch sw when OPEN.equals(attribute) -> collectSwitch(event, sw);
            case DcSwitch dcSwitch when OPEN.equals(attribute) -> collectDcSwitch(dcSwitch);
            case Load load when LOAD_ATTRIBUTES.contains(attribute) -> collectLoad(event, load);
            case Generator generator when GENERATOR_ATTRIBUTES.contains(attribute) -> collectGenerator(event, generator, attribute);
            case TwoWindingsTransformer transformer when isTapPosition(attribute) -> collectTwoWindingsTapPosition(event, transformer, attribute);
            case ThreeWindingsTransformer transformer when isTapPosition(attribute) -> collectThreeWindingsTapPosition(event, transformer, attribute);
            case ShuntCompensator shunt when SHUNT_ATTRIBUTES.contains(attribute) -> collectShuntCompensator(event, shunt, attribute);
            case StaticVarCompensator svc when STATIC_VAR_COMPENSATOR_ATTRIBUTES.contains(attribute) -> collectStaticVarCompensator(event, svc);
            case HvdcLine hvdcLine when ACTIVE_POWER_SETPOINT.equals(attribute) -> collectHvdcActivePowerSetpoint(hvdcLine);
            case VscConverterStation converter when VOLTAGE_SETPOINT.equals(attribute) -> collectVscVoltageSetpoint(converter);
            case VscConverterStation converter when REACTIVE_POWER_SETPOINT.equals(attribute) -> {
                reject(event, "the reactive power setpoint of VSC converter " + converter.getId()
                        + " cannot be exported yet, because the CGMES import and export of VsConverter.targetQpcc"
                        + " do not use the same sign convention");
            }
            default -> reject(event, "no CGMES steady state property corresponds to "
                    + identifiable.getType() + "." + attribute);
        }
    }

    // AC switches

    private void collectSwitch(UpdateNetworkEvent event, Switch sw) {
        if (!context.isExportedEquipment(sw)) {
            reject(event, "switch " + sw.getId() + " has no counterpart in the CGMES equipment model"
                    + " (it was created by the import, for instance to represent a disconnected terminal),"
                    + " so its state cannot be referenced from a steady state hypothesis file");
            return;
        }
        String originalClass = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
        if (isBranchModelledAsSwitch(originalClass)) {
            // In CGMES this equipment is a branch and has no open state of its own:
            // the CGMES import derives the state of the IIDM switch from the connection status of its terminals.
            collectSwitchTerminals(sw);
            return;
        }
        String className = originalClass != null ? originalClass : CgmesExportUtil.switchClassname(sw.getKind());
        updates.object(className, cgmesId(sw)).value("Switch.open", sw.isOpen());
    }

    private static boolean isBranchModelledAsSwitch(String originalClass) {
        return CgmesNames.AC_LINE_SEGMENT.equals(originalClass)
                || CgmesNames.EQUIVALENT_BRANCH.equals(originalClass)
                || CgmesNames.SERIES_COMPENSATOR.equals(originalClass);
    }

    private void collectSwitchTerminals(Switch sw) {
        boolean connected = !sw.isOpen();
        updates.object(CgmesNames.TERMINAL, cgmesIdFromAlias(sw, ALIAS_TERMINAL1)).value(ACDC_TERMINAL_CONNECTED, connected);
        updates.object(CgmesNames.TERMINAL, cgmesIdFromAlias(sw, ALIAS_TERMINAL2)).value(ACDC_TERMINAL_CONNECTED, connected);
    }

    // DC switches

    private void collectDcSwitch(DcSwitch dcSwitch) {
        // A DCSwitch has no open state in the SSH profile either, it is carried by its two DC terminals.
        boolean connected = !dcSwitch.isOpen();
        updates.object(CgmesNames.DC_TERMINAL, cgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL1)).value(ACDC_TERMINAL_CONNECTED, connected);
        updates.object(CgmesNames.DC_TERMINAL, cgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL2)).value(ACDC_TERMINAL_CONNECTED, connected);
    }

    // Loads

    private void collectLoad(UpdateNetworkEvent event, Load load) {
        if (!context.isExportedEquipment(load)) {
            reject(event, "load " + load.getId() + " has no counterpart in the CGMES equipment model");
            return;
        }
        // The CGMES import only accepts an injection power when both components are present,
        // so a change of either setpoint exports both.
        String className = loadClassName(load);
        switch (className) {
            case CgmesNames.ENERGY_SOURCE -> updates.object(className, cgmesId(load))
                    .value("EnergySource.activePower", load.getP0())
                    .value("EnergySource.reactivePower", load.getQ0());
            case CgmesNames.ASYNCHRONOUS_MACHINE -> reject(event, "load " + load.getId()
                    + " is exported as an AsynchronousMachine, whose steady state setpoints the CGMES import"
                    + " cannot read back, neither from a partial nor from a full SSH file");
            case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY -> {
                updates.object(className, cgmesId(load))
                        .value("EnergyConsumer.p", load.getP0())
                        .value("EnergyConsumer.q", load.getQ0());
            }
            default -> reject(event, "load " + load.getId() + " is exported as a " + className
                    + ", which has no steady state setpoints");
        }
    }

    private String loadClassName(Load load) {
        String originalClass = load.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
        return originalClass != null && !context.isExportEquipment() ? originalClass : CgmesExportUtil.loadClassName(load);
    }

    // Generators

    private void collectGenerator(UpdateNetworkEvent event, Generator generator, String attribute) {
        String originalClass = generator.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);
        if (!CgmesNames.SYNCHRONOUS_MACHINE.equals(originalClass)) {
            reject(event, "generator " + generator.getId() + " is exported as a " + originalClass
                    + ", which is not supported yet");
            return;
        }
        switch (attribute) {
            case TARGET_P, TARGET_Q -> collectSynchronousMachine(generator);
            case TARGET_V -> collectGeneratorRegulatingControl(event, generator);
            case VOLTAGE_REGULATOR_ON -> {
                updates.object(CgmesNames.SYNCHRONOUS_MACHINE, cgmesId(generator))
                        .value(REGULATING_COND_EQ_CONTROL_ENABLED, generator.isVoltageRegulatorOn());
                collectGeneratorRegulatingControl(event, generator);
            }
            default -> throw new IllegalStateException("Unhandled generator attribute " + attribute);
        }
    }

    private void collectSynchronousMachine(Generator generator) {
        // Sign convention: CGMES uses the load convention for machines, IIDM the generator convention.
        updates.object(CgmesNames.SYNCHRONOUS_MACHINE, cgmesId(generator))
                .value(REGULATING_COND_EQ_CONTROL_ENABLED, generator.isVoltageRegulatorOn())
                .value(ROTATING_MACHINE_P, -generator.getTargetP())
                .value(ROTATING_MACHINE_Q, -generator.getTargetQ())
                .value("SynchronousMachine.referencePriority", ReferencePriority.get(generator))
                .enumValue("SynchronousMachine.operatingMode", "SynchronousMachineOperatingMode",
                        SteadyStateHypothesisExport.obtainOperatingMode(generator, generator.getMinP(), generator.getMaxP(), generator.getTargetP()));
    }

    private void collectGeneratorRegulatingControl(UpdateNetworkEvent event, Generator generator) {
        String regulatingControlId = regulatingControlId(event, generator);
        if (regulatingControlId == null) {
            return;
        }
        // A RegulatingControl carries a single target, whose meaning depends on the regulation mode,
        // so the exported target is the one matching the mode the generator is currently in.
        RemoteReactivePowerControl reactivePowerControl = generator.getExtension(RemoteReactivePowerControl.class);
        String mode = CgmesExportUtil.getGeneratorRegulatingControlMode(generator, reactivePowerControl);
        if (RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER.equals(mode)) {
            collectRegulatingControl(regulatingControlId, false, reactivePowerControl.isEnabled(),
                    reactivePowerControl.getTargetQ(), MEGA);
        } else {
            collectRegulatingControl(regulatingControlId, false, generator.isVoltageRegulatorOn(),
                    generatorTargetV(generator), KILO);
        }
    }

    private double generatorTargetV(Generator generator) {
        double targetV = generator.getTargetV();
        if (context.isExportGeneratorsInLocalRegulationMode() && generator.getRegulatingTerminal() != null) {
            double remoteNominalV = generator.getRegulatingTerminal().getVoltageLevel().getNominalV();
            double localNominalV = generator.getTerminal().getVoltageLevel().getNominalV();
            if (localNominalV != remoteNominalV) {
                targetV = localNominalV * targetV / remoteNominalV;
            }
        }
        return targetV;
    }

    // Tap changers

    private static boolean isTapPosition(String attribute) {
        return attribute.endsWith(TAP_POSITION_SUFFIX);
    }

    private void collectTwoWindingsTapPosition(UpdateNetworkEvent event, TwoWindingsTransformer transformer, String attribute) {
        if ((PHASE_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX).equals(attribute) && transformer.hasPhaseTapChanger()) {
            collectTapChanger(transformer, endAliasType(transformer, ALIAS_PHASE_TAP_CHANGER1, ALIAS_PHASE_TAP_CHANGER2),
                    CgmesNames.PHASE_TAP_CHANGER_TABULAR, transformer.getPhaseTapChanger());
        } else if ((RATIO_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX).equals(attribute) && transformer.hasRatioTapChanger()) {
            collectTapChanger(transformer, endAliasType(transformer, ALIAS_RATIO_TAP_CHANGER1, ALIAS_RATIO_TAP_CHANGER2),
                    CgmesNames.RATIO_TAP_CHANGER, transformer.getRatioTapChanger());
        } else {
            reject(event, "two windings transformer " + transformer.getId() + " has no tap changer matching " + attribute);
        }
    }

    private static String endAliasType(TwoWindingsTransformer transformer, String end1AliasType, String end2AliasType) {
        return transformer.getAliasFromType(end2AliasType).isPresent() && transformer.getAliasFromType(end1AliasType).isEmpty()
                ? end2AliasType : end1AliasType;
    }

    private void collectThreeWindingsTapPosition(UpdateNetworkEvent event, ThreeWindingsTransformer transformer, String attribute) {
        boolean phase = attribute.startsWith(PHASE_TAP_CHANGER_PREFIX);
        String prefix = phase ? PHASE_TAP_CHANGER_PREFIX : RATIO_TAP_CHANGER_PREFIX;
        if (!attribute.startsWith(prefix)) {
            reject(event, "three windings transformer " + transformer.getId() + " has no tap changer matching " + attribute);
            return;
        }
        String end = attribute.substring(prefix.length(), attribute.length() - TAP_POSITION_SUFFIX.length());
        ThreeWindingsTransformer.Leg leg = leg(transformer, end);
        if (leg == null || (phase ? !leg.hasPhaseTapChanger() : !leg.hasRatioTapChanger())) {
            reject(event, "three windings transformer " + transformer.getId() + " has no tap changer matching " + attribute);
            return;
        }
        if (phase) {
            collectTapChanger(transformer, CgmesExportUtil.getPhaseTapChangerAliasType(end),
                    CgmesNames.PHASE_TAP_CHANGER_TABULAR, leg.getPhaseTapChanger());
        } else {
            collectTapChanger(transformer, CgmesExportUtil.getRatioTapChangerAliasType(end),
                    CgmesNames.RATIO_TAP_CHANGER, leg.getRatioTapChanger());
        }
    }

    private static ThreeWindingsTransformer.Leg leg(ThreeWindingsTransformer transformer, String end) {
        return switch (end) {
            case "1" -> transformer.getLeg1();
            case "2" -> transformer.getLeg2();
            case "3" -> transformer.getLeg3();
            default -> null;
        };
    }

    private <C extends Connectable<C>> void collectTapChanger(C transformer, String aliasType, String defaultClassName,
                                                              TapChanger<?, ?, ?, ?> tapChanger) {
        String className = defaultClassName;
        if (tapChanger instanceof PhaseTapChanger && !context.isExportEquipment()) {
            className = CgmesExportUtil.getPhaseTapChangerType(transformer, transformer.getAliasFromType(aliasType).orElse(null));
        }
        updates.object(className, cgmesIdFromAlias(transformer, aliasType))
                .value("TapChanger.controlEnabled", tapChanger.isRegulating())
                .value("TapChanger.step", tapChanger.getTapPosition());
    }

    // Shunt compensators

    private void collectShuntCompensator(UpdateNetworkEvent event, ShuntCompensator shunt, String attribute) {
        if (Boolean.parseBoolean(shunt.getProperty(PROPERTY_IS_EQUIVALENT_SHUNT))) {
            reject(event, "shunt compensator " + shunt.getId()
                    + " is exported as an EquivalentShunt, which has no steady state properties");
            return;
        }
        switch (attribute) {
            case SECTION_COUNT -> updates.object(shuntClassName(shunt), cgmesId(shunt))
                    .value("ShuntCompensator.sections", shunt.getSectionCount())
                    .value(REGULATING_COND_EQ_CONTROL_ENABLED, shunt.isVoltageRegulatorOn());
            case TARGET_V -> collectShuntRegulatingControl(event, shunt);
            case VOLTAGE_REGULATOR_ON -> {
                updates.object(shuntClassName(shunt), cgmesId(shunt))
                        .value(REGULATING_COND_EQ_CONTROL_ENABLED, shunt.isVoltageRegulatorOn());
                collectShuntRegulatingControl(event, shunt);
            }
            default -> throw new IllegalStateException("Unhandled shunt compensator attribute " + attribute);
        }
    }

    private void collectShuntRegulatingControl(UpdateNetworkEvent event, ShuntCompensator shunt) {
        String regulatingControlId = regulatingControlId(event, shunt);
        if (regulatingControlId != null) {
            collectRegulatingControl(regulatingControlId, true, shunt.isVoltageRegulatorOn(), shunt.getTargetV(), KILO);
        }
    }

    private static String shuntClassName(ShuntCompensator shunt) {
        return switch (shunt.getModelType()) {
            case LINEAR -> "LinearShuntCompensator";
            case NON_LINEAR -> "NonlinearShuntCompensator";
        };
    }

    // Static var compensators

    private void collectStaticVarCompensator(UpdateNetworkEvent event, StaticVarCompensator svc) {
        String regulatingControlId = regulatingControlId(event, svc);
        if (regulatingControlId == null) {
            return;
        }
        // As for generators, the single target of the RegulatingControl is the one matching the current mode.
        // A change of the setpoint of the other mode is therefore not observable in the SSH profile.
        String mode = CgmesExportUtil.getSvcMode(svc);
        if (RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER.equals(mode)) {
            collectRegulatingControl(regulatingControlId, false, svc.isRegulating(), svc.getReactivePowerSetpoint(), MEGA);
        } else {
            collectRegulatingControl(regulatingControlId, false, svc.isRegulating(), svc.getVoltageSetpoint(), KILO);
        }
    }

    // HVDC

    private void collectHvdcActivePowerSetpoint(HvdcLine hvdcLine) {
        collectConverterActivePower(hvdcLine.getConverterStation1());
        collectConverterActivePower(hvdcLine.getConverterStation2());
    }

    private void collectConverterActivePower(HvdcConverterStation<?> converter) {
        // The CGMES import reads targetPpcc, targetUdc, p and q as a single block, and derives the power factor of
        // a line commutated converter from p and q, so the four quantities are always exported together. They are
        // computed exactly as the full SSH export computes them.
        SteadyStateHypothesisExport.ConverterState state = SteadyStateHypothesisExport.computeConverterState(converter);
        boolean rectifier = CgmesExportUtil.isConverterStationRectifier(converter);
        if (converter instanceof LccConverterStation) {
            collectConverterState(CgmesNames.CS_CONVERTER, converter, state)
                    .enumValue("CsConverter.operatingMode", "CsOperatingModeKind", rectifier ? "rectifier" : "inverter")
                    .enumValue("CsConverter.pPccControl", "CsPpccControlKind", rectifier ? "activePower" : "dcVoltage");
        } else if (converter instanceof VscConverterStation vscConverter) {
            collectConverterState(CgmesNames.VS_CONVERTER, converter, state);
            collectVscControlModes(vscConverter);
        }
    }

    private PartialSshUpdates.ObjectUpdate collectConverterState(String className, HvdcConverterStation<?> converter,
                                                                SteadyStateHypothesisExport.ConverterState state) {
        return updates.object(className, cgmesId(converter))
                .value("ACDCConverter.targetPpcc", state.targetPpcc())
                .value("ACDCConverter.targetUdc", state.targetUdc())
                .value("ACDCConverter.p", state.p())
                .value("ACDCConverter.q", state.q());
    }

    private void collectVscVoltageSetpoint(VscConverterStation converter) {
        collectVscControlModes(converter);
        updates.object(CgmesNames.VS_CONVERTER, cgmesId(converter))
                .value("VsConverter.targetUpcc", converter.getVoltageSetpoint());
    }

    /** The CGMES import only reads the targets of a voltage source converter when both control modes are present. */
    private void collectVscControlModes(VscConverterStation converter) {
        updates.object(CgmesNames.VS_CONVERTER, cgmesId(converter))
                .enumValue("VsConverter.pPccControl", "VsPpccControlKind",
                        CgmesExportUtil.isConverterStationRectifier(converter) ? "pPcc" : "udc")
                .enumValue("VsConverter.qPccControl", "VsQpccControlKind",
                        converter.isVoltageRegulatorOn() ? "voltagePcc" : "reactivePcc");
    }

    // Regulating controls

    private void collectRegulatingControl(String regulatingControlId, boolean discrete, boolean enabled,
                                          double targetValue, String targetValueUnitMultiplier) {
        // The target deadband is deliberately left out: it is not part of the changes this exporter supports,
        // and omitting it keeps the value the receiving side already has.
        updates.object(REGULATING_CONTROL, regulatingControlId)
                .value("RegulatingControl.discrete", discrete)
                .value("RegulatingControl.enabled", enabled)
                .value("RegulatingControl.targetValue", targetValue)
                .enumValue("RegulatingControl.targetValueUnitMultiplier", UNIT_MULTIPLIER, targetValueUnitMultiplier);
    }

    private String regulatingControlId(UpdateNetworkEvent event, Identifiable<?> identifiable) {
        String regulatingControlId = context.getNamingStrategy().getCgmesIdFromProperty(identifiable, PROPERTY_REGULATING_CONTROL);
        if (regulatingControlId == null) {
            reject(event, identifiable.getType() + " " + identifiable.getId()
                    + " has no CGMES regulating control to carry this change");
        }
        return regulatingControlId;
    }

    // Helpers

    private String cgmesId(Identifiable<?> identifiable) {
        return context.getNamingStrategy().getCgmesId(identifiable);
    }

    private String cgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        return context.getNamingStrategy().getCgmesIdFromAlias(identifiable, aliasType);
    }

    private void reject(NetworkEvent event, String reason) {
        String message = "Change cannot be exported to a partial SSH file: " + reason + ". Change: " + describe(event);
        if (unsupportedChangeBehavior == UnsupportedChangeBehavior.FAIL) {
            throw new PowsyblException(message);
        }
        LOGGER.warn("{}", message);
    }

    private static String describe(NetworkEvent event) {
        return event instanceof UpdateNetworkEvent updateEvent
                ? updateEvent.id() + "." + updateEvent.attribute()
                : String.valueOf(event);
    }
}
