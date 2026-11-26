/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.statement.CgmesObject;

import static com.powsybl.cgmes.conversion.export.AbstractCgmesWriter.format;
import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * <p>The definitions of the CGMES objects for serialization.</p>
 *
 * <p>Each record represents a CGMES object of a certain class uniquely defined by its rdf:Id,
 * its attributes and its associations.</p>
 *
 * <p>This is meant to decouple the extraction of the data in {@link EquipmentExport}
 * and the writing of the data in the CGMES XML file in {@link EquipmentWriter}</p>
 *
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public final class CgmesRecords {

    private CgmesRecords() {
    }

    /**
     * Start the SSH object of a CsConverter or VsConverter with the attributes of ACDCConverter.
     */
    private static CgmesObject.Builder acDcConverter(String className, String rdfId, double targetPpcc, double targetUdc, double p, double q) {
        return CgmesObject.builder(className, rdfId)
            .literal(ACDC_CONVERTER_TARGET_PPCC, format(targetPpcc))
            .literal(ACDC_CONVERTER_TARGET_UDC, format(targetUdc))
            .literal(ACDC_CONVERTER_P, format(p))
            .literal(ACDC_CONVERTER_Q, format(q));
    }

    /**
     * An ACDCConverter from the EQ profile.
     */
    public record AcDcConverterEq(
        String className,
        String rdfId,
        String name,
        double ratedUdc,
        double idleLoss,
        double switchingLoss,
        double resistiveLoss,
        String capabilityCurve,
        String pccTerminal,
        String equipmentContainer
    ) {
    }

    /**
     * An ACLineSegment from the EQ profile.
     */
    public record ACLineSegmentEq(
        String rdfId,
        String name,
        double r,
        double x,
        double g,
        double b,
        String baseVoltage
    ) {
    }

    /**
     * An AsynchronousMachine from the EQ profile.
     */
    public record AsynchronousMachineEq(
        String rdfId,
        String name
    ) {
    }

    /**
     * An AsynchronousMachine from the SSH profile.
     */
    public record AsynchronousMachineSsh(
        String rdfId,
        double p,
        double q,
        boolean controlEnabled,
        String asynchronousMachineType
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(ASYNCHRONOUS_MACHINE, rdfId)
                .literal(ROTATING_MACHINE_P, format(p))
                .literal(ROTATING_MACHINE_Q, format(q))
                .literal(REGULATING_COND_EQ_CONTROL_ENABLED, format(controlEnabled))
                .resource(ASYNCHRONOUS_MACHINE_TYPE, cimNamespace + ASYNCHRONOUS_MACHINE_KIND + asynchronousMachineType)
                .build();
        }
    }

    /**
     * A BaseVoltage from the EQ profile.
     */
    public record BaseVoltageEq(
        String rdfId,
        double nominalVoltage
    ) {
    }

    /**
     * A BusbarSection from the EQ profile.
     */
    public record BusbarSectionEq(
        String rdfId,
        String name,
        String equipmentContainer,
        String baseVoltage
    ) {
    }

    /**
     * A ConnectivityNode from the EQ profile.
     */
    public record ConnectivityNodeEq(
        String rdfId,
        String name,
        String connectivityNodeContainer
    ) {
    }

    /**
     * A ControlArea from the EQ profile.
     */
    public record ControlAreaEq(
        String rdfId,
        String name,
        String energyIdentCodeEic,
        String energyArea
    ) {
    }

    /**
     * A ControlArea from the SSH profile.
     */
    public record ControlAreaSsh(
        String rdfId,
        double netInterchange,
        Double pTolerance
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            CgmesObject.Builder builder = CgmesObject.builder(CONTROL_AREA, rdfId)
                .literal(CONTROL_AREA_NET_INTERCHANGE, format(netInterchange));
            if (pTolerance != null) {
                builder.literal(CONTROL_AREA_P_TOLERANCE, format(pTolerance));
            }
            return builder.build();
        }
    }

    /**
     * A CsConverter from the SSH profile.
     */
    public record CsConverterSsh(
        String rdfId,
        double targetPpcc,
        double targetUdc,
        double p,
        double q,
        double targetAlpha,
        double targetGamma,
        double targetIdc,
        String operatingMode,
        String pPccControl
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return acDcConverter(CS_CONVERTER, rdfId, targetPpcc, targetUdc, p, q)
                .literal(CS_CONVERTER_TARGET_ALPHA, format(targetAlpha))
                .literal(CS_CONVERTER_TARGET_GAMMA, format(targetGamma))
                .literal(CS_CONVERTER_TARGET_IDC, format(targetIdc))
                .resource(CS_CONVERTER_OPERATING_MODE, cimNamespace + CS_OPERATING_MODE_KIND + operatingMode)
                .resource(CS_CONVERTER_P_PCC_CONTROL, cimNamespace + CS_PPCC_CONTROL_KIND + pPccControl)
                .build();
        }
    }

    /**
     * A Curve from the EQ profile.
     */
    public record CurveEq(
        String className,
        String rdfId,
        String name
    ) {
    }

    /**
     * A CurveData from the EQ profile.
     */
    public record CurveDataEq(
        String rdfId,
        double xvalue,
        double y1value,
        double y2value,
        String curve
    ) {
    }

    /**
     * A DCConverterUnit from the EQ profile.
     */
    public record DCConverterUnitEq(
        String rdfId,
        String name,
        String operationMode,
        String substation
    ) {
    }

    /**
     * A DCGround from the EQ profile.
     */
    public record DCGroundEq(
        String rdfId,
        String name,
        double r
    ) {
    }

    /**
     * A DCLineSegment from the EQ profile.
     */
    public record DCLineSegmentEq(
        String rdfId,
        String name,
        double resistance
    ) {
    }

    /**
     * A DCNode from the EQ profile.
     */
    public record DCNodeEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

    /**
     * A DCSwitch from the EQ profile.
     */
    public record DCSwitchEq(
        String className,
        String rdfId,
        String name
    ) {
    }

    /**
     * A DCTerminal from the EQ profile.
     */
    public record DCTerminalEq(
        String className,
        String rdfId,
        String name,
        int sequenceNumber,
        String dcConductingEquipment,
        String dcNode
    ) {
    }

    /**
     * A DCTerminal from the SSH profile.
     */
    public record DCTerminalSsh(
        String className,
        String rdfId,
        boolean connected
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(className, rdfId)
                .literal(AC_DC_TERMINAL_CONNECTED, format(connected))
                .build();
        }
    }

    /**
     * An EnergyConsumer from the EQ profile.
     */
    public record EnergyConsumerEq(
        String className,
        String rdfId,
        String name,
        String loadGroup,
        String equipmentContainer,
        String loadResponse
    ) {
    }

    /**
     * An EnergyConsumer from the SSH profile.
     */
    public record EnergyConsumerSsh(
        String className,
        String rdfId,
        double p,
        double q
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(className, rdfId)
                .literal(ENERGY_CONSUMER_P, format(p))
                .literal(ENERGY_CONSUMER_Q, format(q))
                .build();
        }
    }

    /**
     * An EnergySource from the EQ profile.
     */
    public record EnergySourceEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

    /**
     * An EnergySource from the SSH profile.
     */
    public record EnergySourceSsh(
        String rdfId,
        double activePower,
        double reactivePower
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(ENERGY_SOURCE, rdfId)
                .literal(ENERGY_SOURCE_ACTIVE_POWER, format(activePower))
                .literal(ENERGY_SOURCE_REACTIVE_POWER, format(reactivePower))
                .build();
        }
    }

    /**
     * An EquivalentBranch from the EQ profile.
     */
    public record EquivalentBranchEq(
        String rdfId,
        String name,
        double r,
        double x,
        String baseVoltage
    ) {
    }

    /**
     * An EquivalentInjection from the EQ profile.
     */
    public record EquivalentInjectionEq(
        String rdfId,
        String name,
        boolean regulationCapability,
        double maxP,
        Double maxQ,
        double minP,
        Double minQ,
        String reactiveCapabilityCurve,
        String baseVoltage
    ) {
    }

    /**
     * An EquivalentInjection from the SSH profile.
     */
    public record EquivalentInjectionSsh(
        String rdfId,
        double p,
        double q,
        boolean regulationStatus,
        double regulationTarget
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(EQUIVALENT_INJECTION, rdfId)
                .literal(EQUIVALENT_INJECTION_P, format(p))
                .literal(EQUIVALENT_INJECTION_Q, format(q))
                .literal(EQUIVALENT_INJECTION_REGULATION_STATUS, format(regulationStatus))
                .literal(EQUIVALENT_INJECTION_REGULATION_TARGET, format(regulationTarget))
                .build();
        }
    }

    /**
     * An EquivalentShunt from the EQ profile.
     */
    public record EquivalentShuntEq(
        String rdfId,
        String name,
        double b,
        double g,
        String equipmentContainer
    ) {
    }

    /**
     * An ExternalNetworkInjection from the EQ profile.
     */
    public record ExternalNetworkInjectionEq(
        String rdfId,
        String name,
        String equipmentContainer,
        double governorScd,
        double maxP,
        double maxQ,
        double minP,
        double minQ,
        String regulatingControl
    ) {
    }

    /**
     * An ExternalNetworkInjection from the SSH profile.
     */
    public record ExternalNetworkInjectionSsh(
        String rdfId,
        boolean controlEnabled,
        double p,
        double q,
        int referencePriority
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(EXTERNAL_NETWORK_INJECTION, rdfId)
                .literal(REGULATING_COND_EQ_CONTROL_ENABLED, format(controlEnabled))
                .literal(EXTERNAL_NETWORK_INJECTION_P, format(p))
                .literal(EXTERNAL_NETWORK_INJECTION_Q, format(q))
                // reference priority is used for angle reference selection (slack)
                .literal(EXTERNAL_NETWORK_INJECTION_REFERENCE_PRIORITY, format(referencePriority))
                .build();
        }
    }

    /**
     * A FossilFuel from the EQ profile.
     */
    public record FossilFuelEq(
        String rdfId,
        String name,
        String fossilFuelType,
        String thermalGeneratingUnit
    ) {
    }

    /**
     * A GeneratingUnit from the EQ profile.
     */
    public record GeneratingUnitEq(
        String className,
        String rdfId,
        String name,
        double initialP,
        double maxOperatingP,
        double minOperatingP,
        String windGenUnitType,
        String equipmentContainer,
        String hydroPowerPlant
    ) {
    }

    /**
     * A GeneratingUnit from the SSH profile.
     */
    public record GeneratingUnitSsh(
        String className,
        String rdfId,
        double normalPF
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(className, rdfId)
                .literal(GENERATING_UNIT_NORMAL_PF, format(normalPF))
                .build();
        }
    }

    /**
     * A GeographicalRegion from the EQ profile.
     */
    public record GeographicalRegionEq(
        String rdfId,
        String name
    ) {
    }

    /**
     * A HydroPowerPlant from the EQ profile.
     */
    public record HydroPowerPlantEq(
        String rdfId,
        String name,
        String hydroPlantStorageType
    ) {
    }

    /**
     * A LoadArea from the EQ profile.
     */
    public record LoadAreaEq(
        String rdfId,
        String name
    ) {
    }

    /**
     * A LoadGroup from the EQ profile.
     */
    public record LoadGroupEq(
        String className,
        String rdfId,
        String name,
        String subLoadArea
    ) {
    }

    /**
     * A LoadResponseCharacteristic from the EQ profile.
     */
    public record LoadResponseCharacteristicEq(
        String rdfId,
        String name,
        boolean exponentModel,
        double pVoltageExponent,
        double qVoltageExponent,
        double pConstantPower,
        double qConstantPower,
        double pConstantCurrent,
        double qConstantCurrent,
        double pConstantImpedance,
        double qConstantImpedance
    ) {
    }

    /**
     * A NonlinearShuntCompensatorPoint from the EQ profile.
     */
    public record NonlinearShuntCompensatorPointEq(
        String rdfId,
        double b,
        double g,
        int sectionNumber,
        String nonlinearShuntCompensator
    ) {
    }

    /**
     * An OperationalLimit from the EQ profile.
     */
    public record OperationalLimitEq(
        String className,
        String rdfId,
        String name,
        double value,
        String operationalLimitType,
        String operationalLimitSet
    ) {
    }

    /**
     * An OperationalLimitSet from the EQ profile.
     */
    public record OperationalLimitSetEq(
        String rdfId,
        String name,
        String terminal
    ) {
    }

    /**
     * An OperationalLimitType from the EQ profile.
     */
    public record OperationalLimitTypeEq(
        String rdfId,
        String name,
        int acceptableDuration,
        boolean isInfiniteDuration,
        String kind
    ) {
    }

    /**
     * A PhaseTapChanger from the EQ profile.
     */
    public record PhaseTapChangerEq(
        String rdfId,
        String name,
        int highStep,
        int lowStep,
        boolean ltcFlag,
        int neutralStep,
        double neutralU,
        int normalStep,
        String phaseTapChangerTable,
        String transformerEnd,
        String tapChangerControl
    ) {
    }

    /**
     * A PhaseTapChangerTable from the EQ profile.
     */
    public record PhaseTapChangerTableEq(
        String rdfId,
        String name
    ) {
    }

    /**
     * A PhaseTapChangerTablePoint from the EQ profile.
     */
    public record PhaseTapChangerTablePointEq(
        String rdfId,
        String phaseTapChangerTable,
        int step,
        double r,
        double x,
        double g,
        double b,
        double ratio,
        double angle
    ) {
    }

    /**
     * A PowerTransformer from the EQ profile.
     */
    public record PowerTransformerEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

    /**
     * A PowerTransformerEnd from the EQ profile.
     */
    public record PowerTransformerEndEq(
        String rdfId,
        String name,
        int endNumber,
        double r,
        double x,
        double g,
        double b,
        double ratedS,
        double ratedU,
        String powerTransformer,
        String baseVoltage,
        String terminal
    ) {
    }

    /**
     * A RatioTapChanger from the EQ profile.
     */
    public record RatioTapChangerEq(
        String rdfId,
        String name,
        String tculControlMode,
        double stepVoltageIncrement,
        int highStep,
        int lowStep,
        boolean ltcFlag,
        int neutralStep,
        double neutralU,
        int normalStep,
        String ratioTapChangerTable,
        String transformerEnd,
        String tapChangerControl
    ) {
    }

    /**
     * A RatioTapChangerTable from the EQ profile.
     */
    public record RatioTapChangerTableEq(
        String rdfId,
        String name
    ) {
    }

    /**
     * A RatioTapChangerTablePoint from the EQ profile.
     */
    public record RatioTapChangerTablePointEq(
        String rdfId,
        String ratioTapChangerTable,
        int step,
        double r,
        double x,
        double g,
        double b,
        double ratio
    ) {
    }

    /**
     * A RegulatingControl from the EQ profile.
     */
    public record RegulatingControlEq(
        String rdfId,
        String name,
        String terminal,
        String mode
    ) {
    }

    /**
     * A RegulatingControl from the SSH profile.
     */
    public record RegulatingControlSsh(
        String className,
        String rdfId,
        boolean discrete,
        boolean enabled,
        double targetDeadband,
        double targetValue,
        String targetValueUnitMultiplier
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            CgmesObject.Builder builder = CgmesObject.builder(className, rdfId)
                .literal(REGULATING_CONTROL_DISCRETE, format(discrete))
                .literal(REGULATING_CONTROL_ENABLED, format(enabled));
            if (CgmesExportUtil.targetDeadbandIsDefined(targetDeadband)) {
                builder.literal(REGULATING_CONTROL_TARGET_DEADBAND, format(targetDeadband));
            }
            return builder
                .literal(REGULATING_CONTROL_TARGET_VALUE, format(targetValue))
                .resource(REGULATING_CONTROL_TARGET_VALUE_UNIT_MULTIPLIER, cimNamespace + UNIT_MULTIPLIER + targetValueUnitMultiplier)
                .build();
        }
    }

    /**
     * A SeriesCompensator from the EQ profile.
     */
    public record SeriesCompensatorEq(
        String rdfId,
        String name,
        double r,
        double x,
        boolean varistorPresent,
        double varistorRatedCurrent,
        double varistorVoltageThreshold,
        String baseVoltage
    ) {
    }

    /**
     * A ShuntCompensator from the EQ profile.
     */
    public record ShuntCompensatorEq(
        String className,
        String rdfId,
        String name,
        double bPerSection,
        double gPerSection,
        int maximumSections,
        double nomU,
        int normalSections,
        String regulatingControl,
        String equipmentContainer
    ) {
    }

    /**
     * A ShuntCompensator from the SSH profile.
     */
    public record ShuntCompensatorSsh(
        String className,
        String rdfId,
        int sections,
        boolean controlEnabled
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(className, rdfId)
                .literal(SHUNT_COMPENSATOR_SECTIONS, format(sections))
                .literal(REGULATING_COND_EQ_CONTROL_ENABLED, format(controlEnabled))
                .build();
        }
    }

    /**
     * A StaticVarCompensator from the EQ profile.
     */
    public record StaticVarCompensatorEq(
        String rdfId,
        String name,
        double capacitiveRating,
        double inductiveRating,
        double slope,
        String svcControlMode,
        double voltageSetPoint,
        String regulatingControl,
        String equipmentContainer
    ) {
    }

    /**
     * A StaticVarCompensator from the SSH profile.
     */
    public record StaticVarCompensatorSsh(
        String rdfId,
        boolean controlEnabled,
        double q
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(STATIC_VAR_COMPENSATOR, rdfId)
                .literal(REGULATING_COND_EQ_CONTROL_ENABLED, format(controlEnabled))
                .literal(STATIC_VAR_COMPENSATOR_Q, format(q))
                .build();
        }
    }

    /**
     * A SubGeographicalRegion from the EQ profile.
     */
    public record SubGeographicalRegionEq(
        String rdfId,
        String name,
        String region
    ) {
    }

    /**
     * A SubLoadArea from the EQ profile.
     */
    public record SubLoadAreaEq(
        String rdfId,
        String name,
        String loadArea
    ) {
    }

    /**
     * A Substation from the EQ profile.
     */
    public record SubstationEq(
        String rdfId,
        String name,
        String region
    ) {
    }

    /**
     * A Switch from the EQ profile.
     */
    public record SwitchEq(
        String className,
        String rdfId,
        String name,
        boolean normalOpen,
        boolean retained,
        String equipmentContainer
    ) {
    }

    /**
     * A Switch from the SSH profile.
     */
    public record SwitchSsh(
        String className,
        String rdfId,
        boolean open
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(className, rdfId)
                .literal(SWITCH_OPEN, format(open))
                .build();
        }
    }

    /**
     * A SynchronousMachine from the EQ profile.
     */
    public record SynchronousMachineEq(
        String rdfId,
        String name,
        Double maxQ,
        Double minQ,
        String type,
        double ratedS,
        String initialReactiveCapabilityCurve,
        String generatingUnit,
        String regulatingControl,
        String equipmentContainer
    ) {
    }

    /**
     * A SynchronousMachine from the SSH profile.
     */
    public record SynchronousMachineSsh(
        String rdfId,
        boolean controlEnabled,
        double p,
        double q,
        int referencePriority,
        String operatingMode
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(SYNCHRONOUS_MACHINE, rdfId)
                .literal(REGULATING_COND_EQ_CONTROL_ENABLED, format(controlEnabled))
                .literal(ROTATING_MACHINE_P, format(p))
                .literal(ROTATING_MACHINE_Q, format(q))
                // reference priority is used for angle reference selection (slack)
                .literal(SYNCHRONOUS_MACHINE_REFERENCE_PRIORITY, format(referencePriority))
                .resource(SYNCHRONOUS_MACHINE_OPERATING_MODE, cimNamespace + SYNCHRONOUS_MACHINE_OPERATING_MODE_KIND + operatingMode)
                .build();
        }
    }

    /**
     * A TapChanger from the SSH profile.
     */
    public record TapChangerSsh(
        String className,
        String rdfId,
        boolean controlEnabled,
        int step
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(className, rdfId)
                .literal(TAP_CHANGER_CONTROL_ENABLED_ATTRIBUTE, format(controlEnabled))
                .literal(TAP_CHANGER_STEP, format(step))
                .build();
        }
    }

    /**
     * A TapChangerControl from the EQ profile.
     */
    public record TapChangerControlEq(
        String rdfId,
        String name,
        String mode,
        String terminal
    ) {
    }

    /**
     * A Terminal from the EQ profile.
     */
    public record TerminalEq(
        String rdfId,
        int sequenceNumber,
        String conductingEquipment,
        String connectivityNode
    ) {
    }

    /**
     * A Terminal from the SSH profile.
     */
    public record TerminalSsh(
        String rdfId,
        boolean connected
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return CgmesObject.builder(TERMINAL, rdfId)
                .literal(AC_DC_TERMINAL_CONNECTED, format(connected))
                .build();
        }
    }

    /**
     * A TieFlow from the EQ profile.
     */
    public record TieFlowEq(
        String rdfId,
        String controlArea,
        String terminal
    ) {
    }

    /**
     * A VoltageLevel from the EQ profile.
     */
    public record VoltageLevelEq(
        String rdfId,
        String name,
        double lowVoltageLimit,
        double highVoltageLimit,
        String substation,
        String baseVoltage
    ) {
    }

    /**
     * A VsConverter from the SSH profile.
     */
    public record VsConverterSsh(
        String rdfId,
        double targetPpcc,
        double targetUdc,
        double p,
        double q,
        double droop,
        double droopCompensation,
        double qShare,
        double targetQpcc,
        double targetUpcc,
        String pPccControl,
        String qPccControl
    ) implements SshRecord {
        @Override
        public CgmesObject normalize(String cimNamespace) {
            return acDcConverter(VS_CONVERTER, rdfId, targetPpcc, targetUdc, p, q)
                .literal(VS_CONVERTER_DROOP, format(droop))
                .literal(VS_CONVERTER_DROOP_COMPENSATION, format(droopCompensation))
                .literal(VS_CONVERTER_Q_SHARE, format(qShare))
                .literal(VS_CONVERTER_TARGET_QPCC, format(targetQpcc))
                .literal(VS_CONVERTER_TARGET_UPCC, format(targetUpcc))
                .resource(VS_CONVERTER_P_PCC_CONTROL, cimNamespace + VS_PPCC_CONTROL_KIND + pPccControl)
                .resource(VS_CONVERTER_Q_PCC_CONTROL, cimNamespace + VS_QPCC_CONTROL_KIND + qPccControl)
                .build();
        }
    }
}
