/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.export.PartialSshExport.UnsupportedChangeBehavior;
import com.powsybl.cgmes.conversion.export.elements.RegulatingControlEq;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.Result;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DcSwitch;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.powsybl.cgmes.conversion.Conversion.ALIAS_DC_TERMINAL1;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_DC_TERMINAL2;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_PHASE_TAP_CHANGER1;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_PHASE_TAP_CHANGER2;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_RATIO_TAP_CHANGER1;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_RATIO_TAP_CHANGER2;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_TERMINAL1;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_TERMINAL2;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_CGMES_ORIGINAL_CLASS;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_IS_EQUIVALENT_SHUNT;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static com.powsybl.cgmes.conversion.export.PartialSshUpdates.merge;
import static com.powsybl.cgmes.conversion.export.PartialSshUpdates.newUpdates;
import static com.powsybl.commons.util.Result.failure;
import static com.powsybl.commons.util.Result.success;

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
class PartialSshEventTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartialSshEventTranslator.class);

    // Internal names that live somewhere in the iidm module, pinned by PartialSshAttributeNameTest
    static final String OPEN = "open";
    static final String P0 = "p0";
    static final String Q0 = "q0";
    static final String TARGET_P = "targetP";
    static final String TARGET_Q = "targetQ";
    static final String TARGET_V = "targetV";
    static final String VOLTAGE_REGULATOR_ON = "voltageRegulatorOn";
    static final String SECTION_COUNT = "sectionCount";
    static final String VOLTAGE_SETPOINT = "voltageSetpoint";
    static final String REACTIVE_POWER_SETPOINT = "reactivePowerSetpoint";
    static final String ACTIVE_POWER_SETPOINT = "activePowerSetpoint";
    static final String TAP_POSITION_SUFFIX = ".tapPosition";
    static final String PHASE_TAP_CHANGER_PREFIX = "phaseTapChanger";
    static final String RATIO_TAP_CHANGER_PREFIX = "ratioTapChanger";

    private static final String REGULATING_COND_EQ_CONTROL_ENABLED = "RegulatingCondEq.controlEnabled";
    private static final String ACDC_TERMINAL_CONNECTED = "ACDCTerminal.connected";
    private static final String ROTATING_MACHINE_P = "RotatingMachine.p";
    private static final String ROTATING_MACHINE_Q = "RotatingMachine.q";
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
    private final List<NetworkEvent> exportedEvents = new ArrayList<>();

    PartialSshEventTranslator(Network network, CgmesExportContext context, UnsupportedChangeBehavior unsupportedChangeBehavior) {
        this.network = network;
        this.context = context;
        this.unsupportedChangeBehavior = unsupportedChangeBehavior;
    }

    /**
     * Translate every recorded change into the CGMES properties that describe it, buffered per object.
     *
     * <p>The changes are expected to have been compacted by {@link PartialSshExport#compactEvents}
     * already to avoid writing the same attribute twice.</p>
     *
     * <p>The values buffered are read from the network as it currently stands, not taken from the changes
     * themselves, so the result describes a state the network really was in.</p>
     *
     * @param events the compacted changes to translate, in the order in which they are to be written
     * @return the properties to write, buffered per CGMES object
     * @throws PowsyblException under {@link UnsupportedChangeBehavior#FAIL}, on the first change that cannot be
     *                          exported
     */
    PartialSshUpdates translateAll(Collection<NetworkEvent> events) {
        for (NetworkEvent event : events) {
            switch (translate(event)) {
                case Result.Success(PartialSshUpdates staged) -> {
                    updates.mergeFrom(staged);
                    exportedEvents.add(event);
                }
                case Result.Failure(String reason) -> reject(event, reason);
            }
        }
        return updates;
    }

    /**
     * The changes that reached the file so IGNORE users can track what they exported.
     */
    List<NetworkEvent> exportedEvents() {
        return exportedEvents;
    }

    /**
     * Translate a single change into the CGMES properties describing it
     *
     * Note that one change may map to multiple properties, they will be merged upstream in `translateAll`.
     */
    private Result<PartialSshUpdates, String> translate(NetworkEvent event) {
        if (event instanceof UpdateNetworkEvent updateEvent) {
            return translateAttributeChange(updateEvent);
        }
        return failure("only attribute updates can be exported, but this is a " + event.getClass().getSimpleName());
    }

    private Result<PartialSshUpdates, String> translateAttributeChange(UpdateNetworkEvent event) {
        Identifiable<?> identifiable = network.getIdentifiable(event.id());
        if (identifiable == null) {
            return failure("the network has no identifiable with id " + event.id());
        }
        String attribute = event.attribute();
        return switch (identifiable) {
            case Switch sw when OPEN.equals(attribute) -> switchUpdates(sw);
            case DcSwitch dcSwitch when OPEN.equals(attribute) -> dcSwitchUpdates(dcSwitch);
            case Load load when LOAD_ATTRIBUTES.contains(attribute) -> loadUpdates(load);
            case Generator generator when GENERATOR_ATTRIBUTES.contains(attribute) -> generatorUpdates(generator, attribute);
            case TwoWindingsTransformer transformer when isTapPosition(attribute) -> twoWindingsTapPositionUpdates(transformer, attribute);
            case ThreeWindingsTransformer transformer when isTapPosition(attribute) -> threeWindingsTapPositionUpdates(transformer, attribute);
            case ShuntCompensator shunt when SHUNT_ATTRIBUTES.contains(attribute) -> shuntCompensatorUpdates(shunt, attribute);
            case StaticVarCompensator svc when STATIC_VAR_COMPENSATOR_ATTRIBUTES.contains(attribute) -> staticVarCompensatorUpdates(svc);
            case HvdcLine hvdcLine when ACTIVE_POWER_SETPOINT.equals(attribute) -> hvdcActivePowerSetpointUpdates(hvdcLine);
            case VscConverterStation converter when VOLTAGE_SETPOINT.equals(attribute) -> vscVoltageSetpointUpdates(converter);
            case VscConverterStation converter when REACTIVE_POWER_SETPOINT.equals(attribute) -> vscReactivePowerSetpointUpdates(converter);
            default -> unmappedAttributeUpdates(identifiable, attribute);
        };
    }

    /** No mapping claimed the change: the SSH profile has no property for this attribute of this equipment. */
    private static Result<PartialSshUpdates, String> unmappedAttributeUpdates(Identifiable<?> identifiable, String attribute) {
        return failure("no CGMES steady state property corresponds to " + identifiable.getType() + "." + attribute);
    }

    // AC switches
    private Result<PartialSshUpdates, String> switchUpdates(Switch sw) {
        if (!context.isExportedEquipment(sw)) {
            return failure("switch " + sw.getId() + " has no counterpart in the CGMES equipment model"
                    + " (it was created by the import, for instance to represent a disconnected terminal),"
                    + " so its state cannot be referenced from a steady state hypothesis file");
        }
        String originalClass = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
        if (isBranchModelledAsSwitch(originalClass)) {
            // In CGMES this equipment is a branch and has no open state of its own:
            // the CGMES import derives the state of the IIDM switch from the connection status of its terminals.
            return success(switchTerminalUpdates(sw));
        }
        String className = originalClass != null ? originalClass : CgmesExportUtil.switchClassname(sw.getKind());
        return success(newUpdates(className, cgmesId(sw))
                .value("Switch.open", sw.isOpen())
                .updates());
    }

    private static boolean isBranchModelledAsSwitch(String originalClass) {
        return CgmesNames.AC_LINE_SEGMENT.equals(originalClass)
                || CgmesNames.EQUIVALENT_BRANCH.equals(originalClass)
                || CgmesNames.SERIES_COMPENSATOR.equals(originalClass);
    }

    private PartialSshUpdates switchTerminalUpdates(Switch sw) {
        boolean connected = !sw.isOpen();
        return newUpdates(CgmesNames.TERMINAL, cgmesIdFromAlias(sw, ALIAS_TERMINAL1)).value(ACDC_TERMINAL_CONNECTED, connected)
                .object(CgmesNames.TERMINAL, cgmesIdFromAlias(sw, ALIAS_TERMINAL2)).value(ACDC_TERMINAL_CONNECTED, connected)
                .updates();
    }

    // DC switches

    private Result<PartialSshUpdates, String> dcSwitchUpdates(DcSwitch dcSwitch) {
        // A DCSwitch has no open state in the SSH profile either, it is carried by its two DC terminals.
        boolean connected = !dcSwitch.isOpen();
        return success(newUpdates(CgmesNames.DC_TERMINAL, cgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL1)).value(ACDC_TERMINAL_CONNECTED, connected)
                .object(CgmesNames.DC_TERMINAL, cgmesIdFromAlias(dcSwitch, ALIAS_DC_TERMINAL2)).value(ACDC_TERMINAL_CONNECTED, connected)
                .updates());
    }

    // Loads

    private Result<PartialSshUpdates, String> loadUpdates(Load load) {
        if (!context.isExportedEquipment(load)) {
            return failure("load " + load.getId() + " has no counterpart in the CGMES equipment model");
        }
        // The CGMES import only accepts an injection power when both components are present,
        // so a change of either setpoint exports both.
        String className = loadClassName(load);
        return switch (className) {
            case CgmesNames.ENERGY_SOURCE -> success(newUpdates(className, cgmesId(load))
                    .value("EnergySource.activePower", load.getP0())
                    .value("EnergySource.reactivePower", load.getQ0())
                    .updates());
            case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY ->
                success(newUpdates(className, cgmesId(load))
                        .value("EnergyConsumer.p", load.getP0())
                        .value("EnergyConsumer.q", load.getQ0())
                        .updates());
            case CgmesNames.ASYNCHRONOUS_MACHINE -> failure("load " + load.getId()
                    + " is exported as an AsynchronousMachine, whose steady state setpoints the CGMES import"
                    + " cannot read back, neither from a partial nor from a full SSH file");
            default -> failure("load " + load.getId() + " is exported as a " + className
                    + ", which has no steady state setpoints");
        };
    }

    private String loadClassName(Load load) {
        String originalClass = load.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS);
        return originalClass != null && !context.isExportEquipment() ? originalClass : CgmesExportUtil.loadClassName(load);
    }

    // Generators

    private Result<PartialSshUpdates, String> generatorUpdates(Generator generator, String attribute) {
        String originalClass = generator.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);
        if (!CgmesNames.SYNCHRONOUS_MACHINE.equals(originalClass)) {
            return failure("generator " + generator.getId() + " is exported as a " + originalClass
                    + ", which is not supported yet");
        }
        return switch (attribute) {
            case TARGET_P, TARGET_Q -> success(synchronousMachineUpdates(generator));
            case TARGET_V -> generatorRegulatingControlUpdates(generator);
            case VOLTAGE_REGULATOR_ON -> generatorRegulatingControlUpdates(generator)
                    .map(regulatingControl -> merge(
                            newUpdates(CgmesNames.SYNCHRONOUS_MACHINE, cgmesId(generator))
                                    .value(REGULATING_COND_EQ_CONTROL_ENABLED, generator.isVoltageRegulatorOn())
                                    .updates(),
                            regulatingControl));
            default -> throw new IllegalStateException("Unhandled generator attribute " + attribute);
        };
    }

    private PartialSshUpdates synchronousMachineUpdates(Generator generator) {
        // Sign convention: CGMES uses the load convention for machines, IIDM the generator convention.
        return newUpdates(CgmesNames.SYNCHRONOUS_MACHINE, cgmesId(generator))
                .value(REGULATING_COND_EQ_CONTROL_ENABLED, generator.isVoltageRegulatorOn())
                .value(ROTATING_MACHINE_P, -generator.getTargetP())
                .value(ROTATING_MACHINE_Q, -generator.getTargetQ())
                .value("SynchronousMachine.referencePriority", ReferencePriority.get(generator))
                .enumValue("SynchronousMachine.operatingMode", "SynchronousMachineOperatingMode",
                        SteadyStateHypothesisExport.obtainOperatingMode(generator, generator.getMinP(), generator.getMaxP(), generator.getTargetP()))
                .updates();
    }

    private Result<PartialSshUpdates, String> generatorRegulatingControlUpdates(Generator generator) {
        // A RegulatingControl carries a single target, whose meaning depends on the regulation mode,
        // so the exported target is the one matching the mode the generator is currently in.
        return regulatingControlId(generator).map(regulatingControlId -> {
            RemoteReactivePowerControl reactivePowerControl = generator.getExtension(RemoteReactivePowerControl.class);
            String mode = CgmesExportUtil.getGeneratorRegulatingControlMode(generator, reactivePowerControl);
            return RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER.equals(mode)
                    ? regulatingControlUpdates(regulatingControlId, false, reactivePowerControl.isEnabled(),
                            reactivePowerControl.getTargetQ(), MEGA)
                    : regulatingControlUpdates(regulatingControlId, false, generator.isVoltageRegulatorOn(),
                            generatorTargetV(generator), KILO);
        });
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

    private Result<PartialSshUpdates, String> twoWindingsTapPositionUpdates(TwoWindingsTransformer transformer, String attribute) {
        if ((PHASE_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX).equals(attribute) && transformer.hasPhaseTapChanger()) {
            return success(tapChangerUpdates(transformer, endAliasType(transformer, ALIAS_PHASE_TAP_CHANGER1, ALIAS_PHASE_TAP_CHANGER2),
                    CgmesNames.PHASE_TAP_CHANGER_TABULAR, transformer.getPhaseTapChanger()));
        }
        if ((RATIO_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX).equals(attribute) && transformer.hasRatioTapChanger()) {
            return success(tapChangerUpdates(transformer, endAliasType(transformer, ALIAS_RATIO_TAP_CHANGER1, ALIAS_RATIO_TAP_CHANGER2),
                    CgmesNames.RATIO_TAP_CHANGER, transformer.getRatioTapChanger()));
        }
        return failure("two windings transformer " + transformer.getId() + " has no tap changer matching " + attribute);
    }

    private static String endAliasType(TwoWindingsTransformer transformer, String end1AliasType, String end2AliasType) {
        return transformer.getAliasFromType(end2AliasType).isPresent() && transformer.getAliasFromType(end1AliasType).isEmpty()
                ? end2AliasType : end1AliasType;
    }

    private Result<PartialSshUpdates, String> threeWindingsTapPositionUpdates(ThreeWindingsTransformer transformer, String attribute) {
        boolean phase = attribute.startsWith(PHASE_TAP_CHANGER_PREFIX);
        String prefix = phase ? PHASE_TAP_CHANGER_PREFIX : RATIO_TAP_CHANGER_PREFIX;
        if (!attribute.startsWith(prefix)) {
            return noTapChangerMatching(transformer, attribute);
        }
        String end = attribute.substring(prefix.length(), attribute.length() - TAP_POSITION_SUFFIX.length());
        ThreeWindingsTransformer.Leg leg = leg(transformer, end);
        if (leg == null || (phase ? !leg.hasPhaseTapChanger() : !leg.hasRatioTapChanger())) {
            return noTapChangerMatching(transformer, attribute);
        }
        return success(phase
                ? tapChangerUpdates(transformer, CgmesExportUtil.getPhaseTapChangerAliasType(end),
                        CgmesNames.PHASE_TAP_CHANGER_TABULAR, leg.getPhaseTapChanger())
                : tapChangerUpdates(transformer, CgmesExportUtil.getRatioTapChangerAliasType(end),
                        CgmesNames.RATIO_TAP_CHANGER, leg.getRatioTapChanger()));
    }

    private static Result<PartialSshUpdates, String> noTapChangerMatching(Identifiable<?> transformer, String attribute) {
        return failure("three windings transformer " + transformer.getId() + " has no tap changer matching " + attribute);
    }

    private static ThreeWindingsTransformer.Leg leg(ThreeWindingsTransformer transformer, String end) {
        return switch (end) {
            case "1" -> transformer.getLeg1();
            case "2" -> transformer.getLeg2();
            case "3" -> transformer.getLeg3();
            default -> null;
        };
    }

    private <C extends Connectable<C>> PartialSshUpdates tapChangerUpdates(C transformer, String aliasType, String defaultClassName,
                                                                          TapChanger<?, ?, ?, ?> tapChanger) {
        String className = defaultClassName;
        if (tapChanger instanceof PhaseTapChanger && !context.isExportEquipment()) {
            className = CgmesExportUtil.getPhaseTapChangerType(transformer, transformer.getAliasFromType(aliasType).orElse(null));
        }
        return newUpdates(className, cgmesIdFromAlias(transformer, aliasType))
                .value("TapChanger.controlEnabled", tapChanger.isRegulating())
                .value("TapChanger.step", tapChanger.getTapPosition())
                .updates();
    }

    // Shunt compensators

    private Result<PartialSshUpdates, String> shuntCompensatorUpdates(ShuntCompensator shunt, String attribute) {
        if (Boolean.parseBoolean(shunt.getProperty(PROPERTY_IS_EQUIVALENT_SHUNT))) {
            return failure("shunt compensator " + shunt.getId()
                    + " is exported as an EquivalentShunt, which has no steady state properties");
        }
        return switch (attribute) {
            case SECTION_COUNT -> success(newUpdates(shuntClassName(shunt), cgmesId(shunt))
                    .value("ShuntCompensator.sections", shunt.getSectionCount())
                    .value(REGULATING_COND_EQ_CONTROL_ENABLED, shunt.isVoltageRegulatorOn())
                    .updates());
            case TARGET_V -> shuntRegulatingControlUpdates(shunt);
            case VOLTAGE_REGULATOR_ON -> shuntRegulatingControlUpdates(shunt)
                    .map(regulatingControl -> merge(
                            newUpdates(shuntClassName(shunt), cgmesId(shunt))
                                    .value(REGULATING_COND_EQ_CONTROL_ENABLED, shunt.isVoltageRegulatorOn())
                                    .updates(),
                            regulatingControl));
            default -> throw new IllegalStateException("Unhandled shunt compensator attribute " + attribute);
        };
    }

    private Result<PartialSshUpdates, String> shuntRegulatingControlUpdates(ShuntCompensator shunt) {
        return regulatingControlId(shunt).map(regulatingControlId ->
                regulatingControlUpdates(regulatingControlId, true, shunt.isVoltageRegulatorOn(), shunt.getTargetV(), KILO));
    }

    private static String shuntClassName(ShuntCompensator shunt) {
        return switch (shunt.getModelType()) {
            case LINEAR -> "LinearShuntCompensator";
            case NON_LINEAR -> "NonlinearShuntCompensator";
        };
    }

    // Static var compensators

    private Result<PartialSshUpdates, String> staticVarCompensatorUpdates(StaticVarCompensator svc) {
        // As for generators, the single target of the RegulatingControl is the one matching the current mode.
        // A change of the setpoint of the other mode is therefore not observable in the SSH profile.
        return regulatingControlId(svc).map(regulatingControlId ->
                RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER.equals(CgmesExportUtil.getSvcMode(svc))
                        ? regulatingControlUpdates(regulatingControlId, false, svc.isRegulating(), svc.getReactivePowerSetpoint(), MEGA)
                        : regulatingControlUpdates(regulatingControlId, false, svc.isRegulating(), svc.getVoltageSetpoint(), KILO));
    }

    // HVDC

    private Result<PartialSshUpdates, String> hvdcActivePowerSetpointUpdates(HvdcLine hvdcLine) {
        return success(merge(converterActivePowerUpdates(hvdcLine.getConverterStation1()),
                converterActivePowerUpdates(hvdcLine.getConverterStation2())));
    }

    private PartialSshUpdates converterActivePowerUpdates(HvdcConverterStation<?> converter) {
        // The CGMES import reads targetPpcc, targetUdc, p and q as a single block, and derives the power factor of
        // a line commutated converter from p and q, so the four quantities are always exported together. They are
        // computed exactly as the full SSH export computes them.
        SteadyStateHypothesisExport.ConverterState state = SteadyStateHypothesisExport.computeConverterState(converter);
        boolean rectifier = CgmesExportUtil.isConverterStationRectifier(converter);
        return switch (converter) {
            case LccConverterStation lcc -> converterStateUpdate(CgmesNames.CS_CONVERTER, lcc, state)
                    .enumValue("CsConverter.operatingMode", "CsOperatingModeKind", rectifier ? "rectifier" : "inverter")
                    .enumValue("CsConverter.pPccControl", "CsPpccControlKind", rectifier ? "activePower" : "dcVoltage")
                    .updates();
            case VscConverterStation vsc -> merge(converterStateUpdate(CgmesNames.VS_CONVERTER, vsc, state).updates(),
                    vscControlModeUpdates(vsc));
            default -> throw new IllegalStateException("Unhandled converter station " + converter.getClass().getSimpleName());
        };
    }

    private PartialSshUpdates.ObjectUpdate converterStateUpdate(String className, HvdcConverterStation<?> converter,
                                                          SteadyStateHypothesisExport.ConverterState state) {
        return newUpdates(className, cgmesId(converter))
                .value("ACDCConverter.targetPpcc", state.targetPpcc())
                .value("ACDCConverter.targetUdc", state.targetUdc())
                .value("ACDCConverter.p", state.p())
                .value("ACDCConverter.q", state.q());
    }

    private Result<PartialSshUpdates, String> vscVoltageSetpointUpdates(VscConverterStation converter) {
        return success(merge(vscControlModeUpdates(converter),
                newUpdates(CgmesNames.VS_CONVERTER, cgmesId(converter))
                        .value("VsConverter.targetUpcc", converter.getVoltageSetpoint())
                        .updates()));
    }

    private static Result<PartialSshUpdates, String> vscReactivePowerSetpointUpdates(VscConverterStation converter) {
        return failure("the reactive power setpoint of VSC converter " + converter.getId()
                + " cannot be exported yet, see issue #4027");
    }

    /** The CGMES import only reads the targets of a voltage source converter when both control modes are present. */
    private PartialSshUpdates vscControlModeUpdates(VscConverterStation converter) {
        return newUpdates(CgmesNames.VS_CONVERTER, cgmesId(converter))
                .enumValue("VsConverter.pPccControl", "VsPpccControlKind",
                        CgmesExportUtil.isConverterStationRectifier(converter) ? "pPcc" : "udc")
                .enumValue("VsConverter.qPccControl", "VsQpccControlKind",
                        converter.isVoltageRegulatorOn() ? "voltagePcc" : "reactivePcc")
                .updates();
    }

    // Regulating controls

    private static PartialSshUpdates regulatingControlUpdates(String regulatingControlId, boolean discrete, boolean enabled,
                                                              double targetValue, String targetValueUnitMultiplier) {
        // The target deadband is deliberately left out: it is not part of the changes this exporter supports,
        // and omitting it keeps the value the receiving side already has.
        return newUpdates(CgmesNames.REGULATING_CONTROL, regulatingControlId)
                .value("RegulatingControl.discrete", discrete)
                .value("RegulatingControl.enabled", enabled)
                .value("RegulatingControl.targetValue", targetValue)
                .enumValue("RegulatingControl.targetValueUnitMultiplier", UNIT_MULTIPLIER, targetValueUnitMultiplier)
                .updates();
    }

    /**
     * The identifier of the RegulatingControl carrying the regulation of the given equipment, or a failure if it
     * has none.
     */
    private Result<String, String> regulatingControlId(Identifiable<?> identifiable) {
        if (!identifiable.hasProperty(PROPERTY_REGULATING_CONTROL)) {
            return failure(identifiable.getType() + " " + identifiable.getId()
                    + " has no CGMES regulating control to carry this change");
        }
        return success(context.getNamingStrategy().getCgmesIdFromProperty(identifiable, PROPERTY_REGULATING_CONTROL));
    }

    // Helpers

    private String cgmesId(Identifiable<?> identifiable) {
        return context.getNamingStrategy().getCgmesId(identifiable);
    }

    private String cgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        return context.getNamingStrategy().getCgmesIdFromAlias(identifiable, aliasType);
    }

    /**
     * Report a change the SSH profile cannot express, as {@link UnsupportedChangeBehavior} asks: by failing when
     * changes must not be lost, and by logging and moving on to the next change otherwise.
     */
    private void reject(NetworkEvent event, String reason) {
        String change = event instanceof UpdateNetworkEvent updateEvent
                ? updateEvent.id() + "." + updateEvent.attribute()
                : String.valueOf(event);
        String message = "Change cannot be exported to a partial SSH file: " + reason + ". Change: " + change;
        if (unsupportedChangeBehavior == UnsupportedChangeBehavior.FAIL) {
            throw new PowsyblException(message);
        }
        LOGGER.warn("{}", message);
    }

}
