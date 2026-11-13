/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
        double highStep,
        double lowStep,
        boolean ltcFlag,
        double neutralStep,
        double neutralU,
        double normalStep,
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
        double highStep,
        double lowStep,
        boolean ltcFlag,
        double neutralStep,
        double neutralU,
        double normalStep,
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
    ) {
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
    ) {
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
    ) {
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
    ) {
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
        String ratedS,
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
    ) {
    }

    /**
     * A TapChanger from the SSH profile.
     */
    public record TapChangerSsh(
        String className,
        String rdfId,
        boolean controlEnabled,
        int step
    ) {
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
    ) {
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
    ) {
    }
}
