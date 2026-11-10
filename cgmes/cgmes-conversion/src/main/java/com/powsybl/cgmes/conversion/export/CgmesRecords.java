/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesRecords {

    private CgmesRecords() {
    }

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

    public record AsynchronousMachineEq(
        String rdfId,
        String name
    ) {
    }

    public record BaseVoltageEq(
        String rdfId,
        double nominalVoltage
    ) {
    }

    public record BusbarSectionEq(
        String rdfId,
        String name,
        String equipmentContainer,
        String baseVoltage
    ) {
    }

    public record ConnectivityNodeEq(
        String rdfId,
        String name,
        String connectivityNodeContainer
    ) {
    }

    public record ControlAreaEq(
        String rdfId,
        String name,
        String energyIdentCodeEic,
        String energyArea
    ) {
    }

    public record CurveEq(
        String className,
        String rdfId,
        String name
    ) {
    }

    public record CurveDataEq(
        String rdfId,
        double xvalue,
        double y1value,
        double y2value,
        String curve
    ) {
    }

    public record DCConverterUnitEq(
        String rdfId,
        String name,
        String operationMode,
        String substation
    ) {
    }

    public record DCGroundEq(
        String rdfId,
        String name,
        double r
    ) {
    }

    public record DCLineSegmentEq(
        String rdfId,
        String name,
        double resistance
    ) {
    }

    public record DCNodeEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

    public record DCSwitchEq(
        String className,
        String rdfId,
        String name
    ) {
    }

    public record DCTerminalEq(
        String className,
        String rdfId,
        String name,
        int sequenceNumber,
        String dcConductingEquipment,
        String dcNode
    ) {
    }

    public record EnergyConsumerEq(
        String className,
        String rdfId,
        String name,
        String loadGroup,
        String equipmentContainer,
        String loadResponse
    ) {
    }

    public record EnergySourceEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

    public record EquivalentBranchEq(
        String rdfId,
        String name,
        double r,
        double x,
        String baseVoltage
    ) {
    }

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

    public record EquivalentShuntEq(
        String rdfId,
        String name,
        double b,
        double g,
        String equipmentContainer
    ) {
    }

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

    public record FossilFuelEq(
        String rdfId,
        String name,
        String fossilFuelType,
        String thermalGeneratingUnit
    ) {
    }

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

    public record GeographicalRegionEq(
        String rdfId,
        String name
    ) {
    }

    public record HydroPowerPlantEq(
        String rdfId,
        String name,
        String hydroPlantStorageType
    ) {
    }

    public record LoadAreaEq(
        String rdfId,
        String name
    ) {
    }

    public record LoadGroupEq(
        String className,
        String rdfId,
        String name,
        String subLoadArea
    ) {
    }

    public record SubLoadAreaEq(
        String rdfId,
        String name,
        String loadArea
    ) {
    }

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

    public record OperationalLimitEq(
        String className,
        String rdfId,
        String name,
        double value,
        String operationalLimitType,
        String operationalLimitSet
    ) {
    }

    public record OperationalLimitSetEq(
        String rdfId,
        String name,
        String terminal
    ) {
    }

    public record OperationalLimitTypeEq(
        String rdfId,
        String name,
        int acceptableDuration,
        boolean isInfiniteDuration,
        String kind
    ) {
    }

    public record PowerTransformerEq(
        String rdfId,
        String name,
        String equipmentContainer
    ) {
    }

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

    public record RegulatingControlEq(
        String rdfId,
        String name,
        String terminal,
        String mode
    ) {
    }

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

    public record NonlinearShuntCompensatorPointEq(
        String rdfId,
        double b,
        double g,
        int sectionNumber,
        String nonlinearShuntCompensator
    ) {
    }

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

    public record SubGeographicalRegionEq(
        String rdfId,
        String name,
        String region
    ) {
    }

    public record SubstationEq(
        String rdfId,
        String name,
        String region
    ) {
    }

    public record SwitchEq(
        String className,
        String rdfId,
        String name,
        boolean normalOpen,
        boolean retained,
        String equipmentContainer
    ) {
    }

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

    public record PhaseTapChangerTableEq(
        String rdfId,
        String name
    ) {
    }

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

    public record RatioTapChangerTableEq(
        String rdfId,
        String name
    ) {
    }

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

    public record TapChangerControlEq(
        String rdfId,
        String name,
        String mode,
        String terminal
    ) {
    }

    public record TerminalEq(
        String rdfId,
        int sequenceNumber,
        String conductingEquipment,
        String connectivityNode
    ) {
    }

    public record TieFlowEq(
        String rdfId,
        String controlArea,
        String terminal
    ) {
    }

    public record VoltageLevelEq(
        String rdfId,
        String name,
        double lowVoltageLimit,
        double highVoltageLimit,
        String substation,
        String baseVoltage
    ) {
    }
}
