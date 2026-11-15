/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Network;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * <p>The serialization of the CGMES Equipment profile.</p>
 *
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class EquipmentWriter extends AbstractCgmesWriter {

    /**
     * Create a new EquipmentWriter instance for writing the CGMES Equipment profile.
     *
     * @param writer the {@link XMLStreamWriter} to serialize the CGMES Equipment profile.
     * @param context the {@link CgmesExportContext} containing the export context.
     */
    public EquipmentWriter(XMLStreamWriter writer, CgmesExportContext context) {
        super(writer, context);
    }

    /**
     * Write the given ACDCConverter to the CGMES Equipment profile.
     */
    public void writeAcDcConverter(CgmesRecords.AcDcConverterEq acDcConverter) throws XMLStreamException {
        writeStartId(acDcConverter.className(), acDcConverter.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(acDcConverter.name()));
        writeAttribute(cimNamespace, ACDC_CONVERTER_RATED_UDC, format(acDcConverter.ratedUdc()));
        writeAttribute(cimNamespace, ACDC_CONVERTER_IDLE_LOSS, format(acDcConverter.idleLoss()));
        writeAttribute(cimNamespace, ACDC_CONVERTER_SWITCHING_LOSS, format(acDcConverter.switchingLoss()));
        writeAttribute(cimNamespace, ACDC_CONVERTER_RESISTIVE_LOSS, format(acDcConverter.resistiveLoss()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(acDcConverter.equipmentContainer()));
        if (acDcConverter.pccTerminal() != null) {
            writeResource(cimNamespace, ACDC_CONVERTER_PCC_TERMINAL, toRdfAbout(acDcConverter.pccTerminal()));
        }
        if (acDcConverter.capabilityCurve() != null) {
            writeResource(cimNamespace, VS_CONVERTER_CAPABILITY_CURVE, toRdfAbout(acDcConverter.capabilityCurve()));
        }
        writeEnd();
    }

    /**
     * Write the given ACLineSegment to the CGMES Equipment profile.
     */
    public void writeACLineSegment(CgmesRecords.ACLineSegmentEq line) throws XMLStreamException {
        writeStartId(AC_LINE_SEGMENT, line.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(line.name()));
        writeAttribute(cimNamespace, AC_LINE_SEGMENT_R, format(line.r()));
        writeAttribute(cimNamespace, AC_LINE_SEGMENT_X, format(line.x()));
        if (line.g() != 0.0) {
            writeAttribute(cimNamespace, AC_LINE_SEGMENT_GCH, format(line.g()));
        }
        writeAttribute(cimNamespace, AC_LINE_SEGMENT_BCH, format(line.b()));
        if (line.baseVoltage() != null) {
            writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, toRdfAbout(line.baseVoltage()));
        }
        writeEnd();
    }

    /**
     * Write the given AsynchronousMachine to the CGMES Equipment profile.
     */
    public void writeAsynchronousMachine(CgmesRecords.AsynchronousMachineEq asynchronousMachine) throws XMLStreamException {
        writeStartId(ASYNCHRONOUS_MACHINE, asynchronousMachine.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(asynchronousMachine.name()));
        writeEnd();
    }

    /**
     * Write the given BaseVoltage to the CGMES Equipment profile.
     */
    public void writeBaseVoltage(CgmesRecords.BaseVoltageEq baseVoltage) throws XMLStreamException {
        String nominalVoltage = format(baseVoltage.nominalVoltage());
        writeStartId(BASE_VOLTAGE, baseVoltage.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(nominalVoltage));
        writeAttribute(cimNamespace, BASE_VOLTAGE_NOMINAL_VOLTAGE, nominalVoltage);
        writeEnd();
    }

    /**
     * Write the given BusbarSection to the CGMES Equipment profile.
     */
    public void writeBusbarSection(CgmesRecords.BusbarSectionEq busbarSection) throws XMLStreamException {
        writeStartId(BUSBAR_SECTION, busbarSection.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(busbarSection.name()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(busbarSection.equipmentContainer()));
        writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, toRdfAbout(busbarSection.baseVoltage()));
        writeEnd();
    }

    /**
     * Write the given ConnectivityNode to the CGMES Equipment profile.
     */
    public void writeConnectivityNode(CgmesRecords.ConnectivityNodeEq node) throws XMLStreamException {
        writeStartId(CONNECTIVITY_NODE, node.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(node.name()));
        writeResource(cimNamespace, CONNECTIVITY_NODE_CONNECTIVITY_NODE_CONTAINER, toRdfAbout(node.connectivityNodeContainer()));
        writeEnd();
    }

    /**
     * Write the given ControlArea to the CGMES Equipment profile.
     */
    public void writeControlArea(CgmesRecords.ControlAreaEq controlArea) throws XMLStreamException {
        writeStartId(CONTROL_AREA, controlArea.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(controlArea.name()));
        if (controlArea.energyIdentCodeEic() != null) {
            writeAttribute(euNamespace, IDENTIFIED_OBJECT_ENERGY_IDENT_CODE_EIC, controlArea.energyIdentCodeEic());
        }
        writeResource(cimNamespace, CONTROL_AREA_TYPE, cimNamespace + CONTROL_AREA_TYPE_KIND_INTERCHANGE);
        if (!context.isCim16BusBranchExport()) {
            writeResource(cimNamespace, CONTROL_AREA_ENERGY_AREA, toRdfAbout(controlArea.energyArea()));
        }
        writeEnd();
    }

    /**
     * Write the given Curve to the CGMES Equipment profile.
     */
    public void writeCurve(CgmesRecords.CurveEq curve) throws XMLStreamException {
        writeStartId(curve.className(), curve.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(curve.name()));
        writeResource(cimNamespace, CURVE_CURVE_STYLE, cimNamespace + CURVE_STYLE_STRAIGHT_LINE_Y_VALUES);
        writeResource(cimNamespace, CURVE_X_UNIT, cimNamespace + UNIT_SYMBOL_W);
        writeResource(cimNamespace, CURVE_Y1_UNIT, cimNamespace + UNIT_SYMBOL_VAR);
        writeResource(cimNamespace, CURVE_Y2_UNIT, cimNamespace + UNIT_SYMBOL_VAR);
        writeEnd();
    }

    /**
     * Write the given CurveData to the CGMES Equipment profile.
     */
    public void writeCurveData(CgmesRecords.CurveDataEq curveData) throws XMLStreamException {
        writeStartId(CURVE_DATA, curveData.rdfId(), false);
        writeAttribute(cimNamespace, CURVE_DATA_XVALUE, format(curveData.xvalue()));
        writeAttribute(cimNamespace, CURVE_DATA_Y1VALUE, format(curveData.y1value()));
        writeAttribute(cimNamespace, CURVE_DATA_Y2VALUE, format(curveData.y2value()));
        writeResource(cimNamespace, CURVE_DATA_CURVE, toRdfAbout(curveData.curve()));
        writeEnd();
    }

    /**
     * Write the given DCConverterUnit to the CGMES Equipment profile.
     */
    public void writeDCConverterUnit(CgmesRecords.DCConverterUnitEq dcConverterUnit) throws XMLStreamException {
        writeStartId(DC_CONVERTER_UNIT, dcConverterUnit.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(dcConverterUnit.name()));
        writeResource(cimNamespace, DC_CONVERTER_UNIT_OPERATION_MODE,
            cimNamespace + OPERATING_MODE_KIND + dcConverterUnit.operationMode());
        writeResource(cimNamespace, DC_CONVERTER_UNIT_SUBSTATION, toRdfAbout(dcConverterUnit.substation()));
        writeEnd();
    }

    /**
     * Write the given DCGround to the CGMES Equipment profile.
     */
    public void writeDCGround(CgmesRecords.DCGroundEq dcGround) throws XMLStreamException {
        writeStartId(DC_GROUND, dcGround.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(dcGround.name()));
        writeAttribute(cimNamespace, DC_GROUND_R, format(dcGround.r()));
        writeEnd();
    }

    /**
     * Write the given DCLineSegment to the CGMES Equipment profile.
     */
    public void writeDCLineSegment(CgmesRecords.DCLineSegmentEq dcLineSegment) throws XMLStreamException {
        writeStartId(DC_LINE_SEGMENT, dcLineSegment.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(dcLineSegment.name()));
        writeAttribute(cimNamespace, DC_LINE_SEGMENT_RESISTANCE, format(dcLineSegment.resistance()));
        writeEnd();
    }

    /**
     * Write the given DCNode to the CGMES Equipment profile.
     */
    public void writeDCNode(CgmesRecords.DCNodeEq dcNode) throws XMLStreamException {
        writeStartId(DC_NODE, dcNode.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(dcNode.name()));
        if (dcNode.equipmentContainer() != null) {
            writeResource(cimNamespace, DC_NODE_DC_EQUIPMENT_CONTAINER, toRdfAbout(dcNode.equipmentContainer()));
        }
        writeEnd();
    }

    /**
     * Write the given DCSwitch to the CGMES Equipment profile.
     */
    public void writeDCSwitch(CgmesRecords.DCSwitchEq dcSwitch) throws XMLStreamException {
        writeStartId(dcSwitch.className(), dcSwitch.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(dcSwitch.name()));
        writeEnd();
    }

    /**
     * Write the given DCTerminal to the CGMES Equipment profile.
     */
    public void writeDCTerminal(CgmesRecords.DCTerminalEq dcTerminal) throws XMLStreamException {
        writeStartId(dcTerminal.className(), dcTerminal.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(dcTerminal.name()));
        writeResource(cimNamespace, dcTerminal.className() + "." + DC_CONDUCTING_EQUIPMENT, toRdfAbout(dcTerminal.dcConductingEquipment()));
        if (dcTerminal.dcNode() != null) {
            writeResource(cimNamespace, DC_BASE_TERMINAL_DC_NODE, toRdfAbout(dcTerminal.dcNode()));
        }
        writeAttribute(cimNamespace, AC_DC_TERMINAL_SEQUENCE_NUMBER, format(dcTerminal.sequenceNumber()));
        writeEnd();
    }

    /**
     * Write the given EnergyConsumer to the CGMES Equipment profile.
     */
    public void writeEnergyConsumer(CgmesRecords.EnergyConsumerEq energyConsumer) throws XMLStreamException {
        writeStartId(energyConsumer.className(), energyConsumer.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(energyConsumer.name()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(energyConsumer.equipmentContainer()));
        if (energyConsumer.loadGroup() != null) {
            writeResource(cimNamespace, energyConsumer.className() + "." + LOAD_GROUP, toRdfAbout(energyConsumer.loadGroup()));
        }
        if (energyConsumer.loadResponse() != null) {
            writeResource(cimNamespace, ENERGY_CONSUMER_LOAD_RESPONSE, toRdfAbout(energyConsumer.loadResponse()));
        }
        writeEnd();
    }

    /**
     * Write the given EnergySource to the CGMES Equipment profile.
     */
    public void writeEnergySource(CgmesRecords.EnergySourceEq energySource) throws XMLStreamException {
        writeStartId(ENERGY_SOURCE, energySource.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(energySource.name()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(energySource.equipmentContainer()));
        writeEnd();
    }

    /**
     * Write the given EquivalentBranch to the CGMES Equipment profile.
     */
    public void writeEquivalentBranch(CgmesRecords.EquivalentBranchEq branch) throws XMLStreamException {
        writeStartId(EQUIVALENT_BRANCH, branch.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(branch.name()));
        writeAttribute(cimNamespace, EQUIVALENT_BRANCH_R, format(branch.r()));
        writeAttribute(cimNamespace, EQUIVALENT_BRANCH_X, format(branch.x()));
        if (branch.baseVoltage() != null) {
            writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, toRdfAbout(branch.baseVoltage()));
        }
        writeEnd();
    }

    /**
     * Write the given EquivalentInjection to the CGMES Equipment profile.
     */
    public void writeEquivalentInjection(CgmesRecords.EquivalentInjectionEq equivalentInjection) throws XMLStreamException {
        writeStartId(EQUIVALENT_INJECTION, equivalentInjection.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(equivalentInjection.name()));
        writeAttribute(cimNamespace, EQUIVALENT_INJECTION_REGULATION_CAPABILITY, format(equivalentInjection.regulationCapability()));
        // To avoid Infinite values during reimport, default values are not exported
        if (isNotMinOrMaxValue(equivalentInjection.minP())) {
            writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MIN_P, format(equivalentInjection.minP()));
        }
        if (isNotMinOrMaxValue(equivalentInjection.maxP())) {
            writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MAX_P, format(equivalentInjection.maxP()));
        }
        if (equivalentInjection.minQ() != null && isNotMinOrMaxValue(equivalentInjection.minQ())) {
            writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MIN_Q, format(equivalentInjection.minQ()));
        }
        if (equivalentInjection.maxQ() != null && isNotMinOrMaxValue(equivalentInjection.maxQ())) {
            writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MAX_Q, format(equivalentInjection.maxQ()));
        }
        if (equivalentInjection.reactiveCapabilityCurve() != null) {
            writeResource(cimNamespace, EQUIVALENT_INJECTION_REACTIVE_CAPABILITY_CURVE, toRdfAbout(equivalentInjection.reactiveCapabilityCurve()));
        }
        writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, toRdfAbout(equivalentInjection.baseVoltage()));
        writeEnd();
    }

    /**
     * Write the given EquivalentShunt to the CGMES Equipment profile.
     */
    public void writeEquivalentShunt(CgmesRecords.EquivalentShuntEq equivalentShunt) throws XMLStreamException {
        writeStartId(EQUIVALENT_SHUNT, equivalentShunt.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(equivalentShunt.name()));
        writeAttribute(cimNamespace, EQUIVALENT_SHUNT_B, format(equivalentShunt.b()));
        writeAttribute(cimNamespace, EQUIVALENT_SHUNT_G, format(equivalentShunt.g()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(equivalentShunt.equipmentContainer()));
        writeEnd();
    }

    /**
     * Write the given ExternalNetworkInjection to the CGMES Equipment profile.
     */
    public void writeExternalNetworkInjection(CgmesRecords.ExternalNetworkInjectionEq externalNetworkInjection) throws XMLStreamException {
        writeStartId(EXTERNAL_NETWORK_INJECTION, externalNetworkInjection.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(externalNetworkInjection.name()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(externalNetworkInjection.equipmentContainer()));
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_GOVERNOR_SCD, format(externalNetworkInjection.governorScd()));
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MAX_P, format(externalNetworkInjection.maxP()));
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MAX_Q, format(externalNetworkInjection.maxQ()));
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MIN_P, format(externalNetworkInjection.minP()));
        writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MIN_Q, format(externalNetworkInjection.minQ()));
        if (externalNetworkInjection.regulatingControl() != null) {
            writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, toRdfAbout(externalNetworkInjection.regulatingControl()));
        }
        writeEnd();
    }

    /**
     * Write the given FossilFuel to the CGMES Equipment profile.
     */
    public void writeFossilFuel(CgmesRecords.FossilFuelEq fossilFuel) throws XMLStreamException {
        writeStartId(FOSSIL_FUEL, fossilFuel.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(fossilFuel.name()));
        writeResource(cimNamespace, FOSSIL_FUEL_FOSSIL_FUEL_TYPE, cimNamespace + fossilFuel.fossilFuelType());
        writeResource(cimNamespace, FOSSIL_FUEL_THERMAL_GENERATING_UNIT, toRdfAbout(fossilFuel.thermalGeneratingUnit()));
        writeEnd();
    }

    /**
     * Write the given GeneratingUnit to the CGMES Equipment profile.
     */
    public void writeGeneratingUnit(CgmesRecords.GeneratingUnitEq generatingUnit) throws XMLStreamException {
        writeStartId(generatingUnit.className(), generatingUnit.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(generatingUnit.name()));
        writeAttribute(cimNamespace, GENERATING_UNIT_MIN_OPERATING_P, format(generatingUnit.minOperatingP()));
        writeAttribute(cimNamespace, GENERATING_UNIT_MAX_OPERATING_P, format(generatingUnit.maxOperatingP()));
        if (context.getCimVersion() == 16) {
            writeAttribute(cimNamespace, GENERATING_UNIT_INITIAL_P, format(generatingUnit.initialP()));
        }
        if (generatingUnit.equipmentContainer() != null) {
            writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(generatingUnit.equipmentContainer()));
        }
        if (WIND_GENERATING_UNIT.equals(generatingUnit.className())) {
            writeResource(cimNamespace, WIND_GENERATING_UNIT_WIND_GEN_UNIT_TYPE,
                cimNamespace + WIND_GEN_UNIT_KIND + generatingUnit.windGenUnitType());
        }
        if (generatingUnit.hydroPowerPlant() != null) {
            writeResource(cimNamespace, HYDRO_GENERATING_UNIT_HYDRO_POWER_PLANT, toRdfAbout(generatingUnit.hydroPowerPlant()));
        }
        writeEnd();
    }

    /**
     * Write the given GeographicalRegion to the CGMES Equipment profile.
     */
    public void writeGeographicalRegion(CgmesRecords.GeographicalRegionEq region) throws XMLStreamException {
        writeStartId(GEOGRAPHICAL_REGION, region.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(region.name()));
        writeEnd();
    }

    /**
     * Write the given HydroPowerPlant to the CGMES Equipment profile.
     */
    public void writeHydroPowerPlant(CgmesRecords.HydroPowerPlantEq plant) throws XMLStreamException {
        writeStartId(HYDRO_POWER_PLANT, plant.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(plant.name()));
        writeResource(cimNamespace, HYDRO_POWER_PLANT_HYDRO_PLANT_STORAGE_TYPE,
            cimNamespace + plant.hydroPlantStorageType());
        writeEnd();
    }

    /**
     * Write the given LoadArea to the CGMES Equipment profile.
     */
    public void writeLoadArea(CgmesRecords.LoadAreaEq loadArea) throws XMLStreamException {
        writeStartId(LOAD_AREA, loadArea.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(loadArea.name() + " Load Area"));
        writeEnd();
    }

    /**
     * Write the given LoadGroup to the CGMES Equipment profile.
     */
    public void writeLoadGroup(CgmesRecords.LoadGroupEq loadGroup) throws XMLStreamException {
        writeStartId(loadGroup.className(), loadGroup.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(loadGroup.name()));
        if (!context.isCim16BusBranchExport()) {
            writeResource(cimNamespace, LOAD_GROUP_SUB_LOAD_AREA, toRdfAbout(loadGroup.subLoadArea()));
        }
        writeEnd();
    }

    /**
     * Write the given LoadResponseCharacteristic to the CGMES Equipment profile.
     */
    public void writeLoadResponseCharacteristic(CgmesRecords.LoadResponseCharacteristicEq loadResponseCharacteristic) throws XMLStreamException {
        writeStartId(LOAD_RESPONSE_CHARACTERISTIC, loadResponseCharacteristic.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(loadResponseCharacteristic.name()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_EXPONENT_MODEL, format(loadResponseCharacteristic.exponentModel()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_CURRENT, format(loadResponseCharacteristic.pConstantCurrent()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_IMPEDANCE, format(loadResponseCharacteristic.pConstantImpedance()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_POWER, format(loadResponseCharacteristic.pConstantPower()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_VOLTAGE_EXPONENT, format(loadResponseCharacteristic.pVoltageExponent()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_CURRENT, format(loadResponseCharacteristic.qConstantCurrent()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_IMPEDANCE, format(loadResponseCharacteristic.qConstantImpedance()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_POWER, format(loadResponseCharacteristic.qConstantPower()));
        writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_VOLTAGE_EXPONENT, format(loadResponseCharacteristic.qVoltageExponent()));
        writeEnd();
    }

    /**
     * Write the network metadata into the FullModel element in accordance with IEC-61970-552 for the Equipment profile.
     */
    public void writeModelDescription(Network network, CgmesMetadataModel model) throws XMLStreamException {
        writeModelDescription(network, CgmesSubset.EQUIPMENT, model);
    }

    /**
     * Write the given NonlinearShuntCompensatorPoint to the CGMES Equipment profile.
     */
    public void writeNonLinearShuntCompensatorPoint(CgmesRecords.NonlinearShuntCompensatorPointEq point) throws XMLStreamException {
        writeStartId(NONLINEAR_SHUNT_COMPENSATOR_POINT, point.rdfId(), false);
        writeResource(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_NONLINEAR_SHUNT_COMPENSATOR, toRdfAbout(point.nonlinearShuntCompensator()));
        writeAttribute(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_SECTION_NUMBER, format(point.sectionNumber()));
        writeAttribute(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_B, format(point.b()));
        writeAttribute(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_G, format(point.g()));
        writeEnd();
    }

    /**
     * Write the given OperationalLimit to the CGMES Equipment profile.
     */
    public void writeOperationalLimit(CgmesRecords.OperationalLimitEq loadingLimit) throws XMLStreamException {
        writeStartId(loadingLimit.className(), loadingLimit.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(loadingLimit.name()));
        String limitValueAttributeName = context.getCimVersion() == 16 ? VALUE : NORMAL_VALUE;
        writeAttribute(cimNamespace, loadingLimit.className() + "." + limitValueAttributeName, format(loadingLimit.value()));
        writeResource(cimNamespace, OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_SET, toRdfAbout(loadingLimit.operationalLimitSet()));
        writeResource(cimNamespace, OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_TYPE, toRdfAbout(loadingLimit.operationalLimitType()));
        writeEnd();
    }

    /**
     * Write the given OperationalLimitSet to the CGMES Equipment profile.
     */
    public void writeOperationalLimitSet(CgmesRecords.OperationalLimitSetEq operationalLimitSet) throws XMLStreamException {
        writeStartId(OPERATIONAL_LIMIT_SET, operationalLimitSet.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(operationalLimitSet.name()));
        writeResource(cimNamespace, OPERATIONAL_LIMIT_SET_TERMINAL, toRdfAbout(operationalLimitSet.terminal()));
        writeEnd();
    }

    /**
     * Write the given OperationalLimitType to the CGMES Equipment profile.
     */
    public void writeOperationalLimitType(CgmesRecords.OperationalLimitTypeEq limitType) throws XMLStreamException {
        writeStartId(OPERATIONAL_LIMIT_TYPE, limitType.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(limitType.name()));
        writeResource(cimNamespace, OPERATIONAL_LIMIT_TYPE_DIRECTION, cimNamespace + OPERATIONAL_LIMIT_DIRECTION_KIND_ABSOLUTE_VALUE);
        String limitTypeAttributeName = context.getCimVersion() == 16 ? OPERATIONAL_LIMIT_TYPE + ".limitType" : OPERATIONAL_LIMIT_TYPE + ".kind";
        String limitKindClassName = context.getCimVersion() == 16 ? "LimitTypeKind" : "LimitKind";
        writeResource(euNamespace, limitTypeAttributeName, euNamespace + limitKindClassName + "." + limitType.kind());
        if (context.getCimVersion() == 100) {
            writeAttribute(cimNamespace, OPERATIONAL_LIMIT_TYPE_IS_INFINITE_DURATION, Boolean.toString(limitType.isInfiniteDuration()));
        }
        if (!limitType.isInfiniteDuration()) {
            writeAttribute(cimNamespace, OPERATIONAL_LIMIT_TYPE_ACCEPTABLE_DURATION, format(limitType.acceptableDuration()));
        }
        writeEnd();
    }

    /**
     * Write the given PhaseTapChanger to the CGMES Equipment profile.
     */
    public void writePhaseTapChanger(CgmesRecords.PhaseTapChangerEq phaseTapChanger) throws XMLStreamException {
        writeStartId(PHASE_TAP_CHANGER_TABULAR, phaseTapChanger.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(phaseTapChanger.name()));
        writeResource(cimNamespace, PHASE_TAP_CHANGER_TRANSFORMER_END, toRdfAbout(phaseTapChanger.transformerEnd()));
        writeAttribute(cimNamespace, TAP_CHANGER_LOW_STEP, format(phaseTapChanger.lowStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_HIGH_STEP, format(phaseTapChanger.highStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_NORMAL_STEP, format(phaseTapChanger.normalStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_STEP, format(phaseTapChanger.neutralStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_U, format(phaseTapChanger.neutralU()));
        writeAttribute(cimNamespace, TAP_CHANGER_LTC_FLAG, format(phaseTapChanger.ltcFlag()));
        writeResource(cimNamespace, PHASE_TAP_CHANGER_TABULAR_PHASE_TAP_CHANGER_TABLE, toRdfAbout(phaseTapChanger.phaseTapChangerTable()));
        if (phaseTapChanger.tapChangerControl() != null) {
            writeResource(cimNamespace, TAP_CHANGER_TAP_CHANGER_CONTROL, toRdfAbout(phaseTapChanger.tapChangerControl()));
        }
        writeEnd();
    }

    /**
     * Write the given PhaseTapChangerTable to the CGMES Equipment profile.
     */
    public void writePhaseTapChangerTable(CgmesRecords.PhaseTapChangerTableEq phaseTapChangerTable) throws XMLStreamException {
        writeStartId(PHASE_TAP_CHANGER_TABLE, phaseTapChangerTable.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(phaseTapChangerTable.name()));
        writeEnd();
    }

    /**
     * Write the given PhaseTapChangerTablePoint to the CGMES Equipment profile.
     */
    public void writePhaseTapChangerTablePoint(CgmesRecords.PhaseTapChangerTablePointEq point) throws XMLStreamException {
        writeStartId(PHASE_TAP_CHANGER_TABLE_POINT, point.rdfId(), false);
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_R, format(point.r()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_X, format(point.x()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_G, format(point.g()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_B, format(point.b()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_STEP, format(point.step()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_RATIO, format(point.ratio()));
        writeAttribute(cimNamespace, PHASE_TAP_CHANGER_TABLE_POINT_ANGLE, format(point.angle()));
        writeResource(cimNamespace, PHASE_TAP_CHANGER_TABLE_POINT_PHASE_TAP_CHANGER_TABLE, toRdfAbout(point.phaseTapChangerTable()));
        writeEnd();
    }

    /**
     * Write the given PowerTransformer to the CGMES Equipment profile.
     */
    public void writePowerTransformer(CgmesRecords.PowerTransformerEq powerTransformer) throws XMLStreamException {
        writeStartId(POWER_TRANSFORMER, powerTransformer.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(powerTransformer.name()));
        if (powerTransformer.equipmentContainer() != null) {
            writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(powerTransformer.equipmentContainer()));
        }
        writeEnd();
    }

    /**
     * Write the given PowerTransformerEnd to the CGMES Equipment profile.
     */
    public void writePowerTransformerEnd(CgmesRecords.PowerTransformerEndEq powerTransformerEnd) throws XMLStreamException {
        writeStartId(POWER_TRANSFORMER_END, powerTransformerEnd.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(powerTransformerEnd.name()));
        writeAttribute(cimNamespace, TRANSFORMER_END_END_NUMBER, format(powerTransformerEnd.endNumber()));
        writeAttribute(cimNamespace, POWER_TRANSFORMER_END_R, format(powerTransformerEnd.r()));
        writeAttribute(cimNamespace, POWER_TRANSFORMER_END_X, format(powerTransformerEnd.x()));
        writeAttribute(cimNamespace, POWER_TRANSFORMER_END_G, format(powerTransformerEnd.g()));
        writeAttribute(cimNamespace, POWER_TRANSFORMER_END_B, format(powerTransformerEnd.b()));
        writeAttribute(cimNamespace, POWER_TRANSFORMER_END_RATED_S, format(powerTransformerEnd.ratedS()));
        writeAttribute(cimNamespace, POWER_TRANSFORMER_END_RATED_U, format(powerTransformerEnd.ratedU()));
        writeResource(cimNamespace, POWER_TRANSFORMER_END_POWER_TRANSFORMER, toRdfAbout(powerTransformerEnd.powerTransformer()));
        writeResource(cimNamespace, TRANSFORMER_END_TERMINAL, toRdfAbout(powerTransformerEnd.terminal()));
        writeResource(cimNamespace, TRANSFORMER_END_BASE_VOLTAGE, toRdfAbout(powerTransformerEnd.baseVoltage()));
        writeEnd();
    }

    /**
     * Write the given RatioTapChanger to the CGMES Equipment profile.
     */
    public void writeRatioTapChanger(CgmesRecords.RatioTapChangerEq ratioTapChanger) throws XMLStreamException {
        writeStartId(RATIO_TAP_CHANGER, ratioTapChanger.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(ratioTapChanger.name()));
        writeResource(cimNamespace, RATIO_TAP_CHANGER_TRANSFORMER_END, toRdfAbout(ratioTapChanger.transformerEnd()));
        writeAttribute(cimNamespace, RATIO_TAP_CHANGER_STEP_VOLTAGE_INCREMENT, format(ratioTapChanger.stepVoltageIncrement()));
        writeAttribute(cimNamespace, TAP_CHANGER_LOW_STEP, format(ratioTapChanger.lowStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_HIGH_STEP, format(ratioTapChanger.highStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_NORMAL_STEP, format(ratioTapChanger.normalStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_STEP, format(ratioTapChanger.neutralStep()));
        writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_U, format(ratioTapChanger.neutralU()));
        writeAttribute(cimNamespace, TAP_CHANGER_LTC_FLAG, format(ratioTapChanger.ltcFlag()));
        if (context.getCimVersion() == 16) {
            writeResource(cimNamespace, RATIO_TAP_CHANGER_TCUL_CONTROL_MODE,
                cimNamespace + TRANSFORMER_CONTROL_MODE + "." + ratioTapChanger.tculControlMode());
        }
        writeResource(cimNamespace, RATIO_TAP_CHANGER_RATIO_TAP_CHANGER_TABLE, toRdfAbout(ratioTapChanger.ratioTapChangerTable()));
        if (ratioTapChanger.tapChangerControl() != null) {
            writeResource(cimNamespace, TAP_CHANGER_TAP_CHANGER_CONTROL, toRdfAbout(ratioTapChanger.tapChangerControl()));
        }
        writeEnd();
    }

    /**
     * Write the given RatioTapChangerTable to the CGMES Equipment profile.
     */
    public void writeRatioTapChangerTable(CgmesRecords.RatioTapChangerTableEq ratioTapChangerTable) throws XMLStreamException {
        writeStartId(RATIO_TAP_CHANGER_TABLE, ratioTapChangerTable.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(ratioTapChangerTable.name()));
        writeEnd();
    }

    /**
     * Write the given RatioTapChangerTablePoint to the CGMES Equipment profile.
     */
    public void writeRatioTapChangerTablePoint(CgmesRecords.RatioTapChangerTablePointEq point) throws XMLStreamException {
        writeStartId(RATIO_TAP_CHANGER_TABLE_POINT, point.rdfId(), false);
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_R, format(point.r()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_X, format(point.x()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_G, format(point.g()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_B, format(point.b()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_STEP, format(point.step()));
        writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_RATIO, format(point.ratio()));
        writeResource(cimNamespace, RATIO_TAP_CHANGER_TABLE_POINT_RATIO_TAP_CHANGER_TABLE, toRdfAbout(point.ratioTapChangerTable()));
        writeEnd();
    }

    /**
     * Write the given RegulatingControl to the CGMES Equipment profile.
     */
    public void writeRegulatingControl(CgmesRecords.RegulatingControlEq regulatingControl) throws XMLStreamException {
        writeStartId(REGULATING_CONTROL, regulatingControl.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(regulatingControl.name()));
        writeResource(cimNamespace, REGULATING_CONTROL_TERMINAL, toRdfAbout(regulatingControl.terminal()));
        writeResource(cimNamespace, REGULATING_CONTROL_MODE,
            cimNamespace + regulatingControl.mode());
        writeEnd();
    }

    /**
     * Write the given SeriesCompensator to the CGMES Equipment profile.
     */
    public void writeSeriesCompensator(CgmesRecords.SeriesCompensatorEq sc) throws XMLStreamException {
        writeStartId(SERIES_COMPENSATOR, sc.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(sc.name()));
        writeAttribute(cimNamespace, SERIES_COMPENSATOR_R, format(sc.r()));
        writeAttribute(cimNamespace, SERIES_COMPENSATOR_X, format(sc.x()));
        writeAttribute(cimNamespace, SERIES_COMPENSATOR_VARISTOR_PRESENT, format(sc.varistorPresent()));
        writeAttribute(cimNamespace, SERIES_COMPENSATOR_VARISTOR_RATED_CURRENT, format(sc.varistorRatedCurrent()));
        writeAttribute(cimNamespace, SERIES_COMPENSATOR_VARISTOR_VOLTAGE_THRESHOLD, format(sc.varistorVoltageThreshold()));
        if (sc.baseVoltage() != null) {
            writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, toRdfAbout(sc.baseVoltage()));
        }
        writeEnd();
    }

    /**
     * Write the given ShuntCompensator to the CGMES Equipment profile.
     */
    public void writeShuntCompensator(CgmesRecords.ShuntCompensatorEq shuntCompensator) throws XMLStreamException {
        writeStartId(shuntCompensator.className(), shuntCompensator.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(shuntCompensator.name()));
        writeAttribute(cimNamespace, SHUNT_COMPENSATOR_NORMAL_SECTIONS, format(shuntCompensator.normalSections()));
        writeAttribute(cimNamespace, SHUNT_COMPENSATOR_MAXIMUM_SECTIONS, format(shuntCompensator.maximumSections()));
        writeAttribute(cimNamespace, SHUNT_COMPENSATOR_NOM_U, format(shuntCompensator.nomU()));
        if (LINEAR_SHUNT_COMPENSATOR.equals(shuntCompensator.className())) {
            writeAttribute(cimNamespace, LINEAR_SHUNT_COMPENSATOR_B_PER_SECTION, format(shuntCompensator.bPerSection()));
            writeAttribute(cimNamespace, LINEAR_SHUNT_COMPENSATOR_G_PER_SECTION, format(shuntCompensator.gPerSection()));
        }
        if (shuntCompensator.regulatingControl() != null) {
            writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, toRdfAbout(shuntCompensator.regulatingControl()));
        }
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(shuntCompensator.equipmentContainer()));
        writeEnd();
    }

    /**
     * Write the given StaticVarCompensator to the CGMES Equipment profile.
     */
    public void writeStaticVarCompensator(CgmesRecords.StaticVarCompensatorEq staticVarCompensator) throws XMLStreamException {
        writeStartId(STATIC_VAR_COMPENSATOR, staticVarCompensator.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(staticVarCompensator.name()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(staticVarCompensator.equipmentContainer()));
        if (staticVarCompensator.regulatingControl() != null) {
            writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, toRdfAbout(staticVarCompensator.regulatingControl()));
        }
        writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_INDUCTIVE_RATING, format(staticVarCompensator.inductiveRating()));
        writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_CAPACITIVE_RATING, format(staticVarCompensator.capacitiveRating()));
        writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_SLOPE, format(staticVarCompensator.slope()));
        writeResource(cimNamespace, STATIC_VAR_COMPENSATOR_SVC_CONTROL_MODE, cimNamespace + staticVarCompensator.svcControlMode());
        writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_VOLTAGE_SET_POINT, format(staticVarCompensator.voltageSetPoint()));
        writeEnd();
    }

    /**
     * Write the given SubGeographicalRegion to the CGMES Equipment profile.
     */
    public void writeSubGeographicalRegion(CgmesRecords.SubGeographicalRegionEq subRegion) throws XMLStreamException {
        writeStartId(SUB_GEOGRAPHICAL_REGION, subRegion.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(subRegion.name()));
        writeResource(cimNamespace, SUB_GEOGRAPHICAL_REGION_REGION, toRdfAbout(subRegion.region()));
        writeEnd();
    }

    /**
     * Write the given SubLoadArea to the CGMES Equipment profile.
     */
    public void writeSubLoadArea(CgmesRecords.SubLoadAreaEq subLoadArea) throws XMLStreamException {
        writeStartId(SUB_LOAD_AREA, subLoadArea.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(subLoadArea.name() + " SubLoad Area"));
        writeResource(cimNamespace, SUB_LOAD_AREA_LOAD_AREA, toRdfAbout(subLoadArea.loadArea()));
        writeEnd();
    }

    /**
     * Write the given Substation to the CGMES Equipment profile.
     */
    public void writeSubstation(CgmesRecords.SubstationEq substation) throws XMLStreamException {
        writeStartId(SUBSTATION, substation.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(substation.name()));
        writeResource(cimNamespace, SUBSTATION_REGION, toRdfAbout(substation.region()));
        writeEnd();
    }

    /**
     * Write the given Switch to the CGMES Equipment profile.
     */
    public void writeSwitch(CgmesRecords.SwitchEq switchEq) throws XMLStreamException {
        writeStartId(switchEq.className(), switchEq.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(switchEq.name()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(switchEq.equipmentContainer()));
        writeAttribute(cimNamespace, SWITCH_NORMAL_OPEN, format(switchEq.normalOpen()));
        writeAttribute(cimNamespace, SWITCH_RETAINED, format(switchEq.retained()));
        writeEnd();
    }

    /**
     * Write the given SynchronousMachine to the CGMES Equipment profile.
     */
    public void writeSynchronousMachine(CgmesRecords.SynchronousMachineEq synchronousMachine) throws XMLStreamException {
        writeStartId(SYNCHRONOUS_MACHINE, synchronousMachine.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(synchronousMachine.name()));
        writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, toRdfAbout(synchronousMachine.equipmentContainer()));
        if (synchronousMachine.generatingUnit() != null) {
            // Some synchronous machines are not associated to a generating unit (condensers)
            writeResource(cimNamespace, ROTATING_MACHINE_GENERATING_UNIT, toRdfAbout(synchronousMachine.generatingUnit()));
        }
        if (synchronousMachine.regulatingControl() != null) {
            writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, toRdfAbout(synchronousMachine.regulatingControl()));
        }
        if (synchronousMachine.initialReactiveCapabilityCurve() != null) {
            writeResource(cimNamespace, SYNCHRONOUS_MACHINE_INITIAL_REACTIVE_CAPABILITY_CURVE,
                toRdfAbout(synchronousMachine.initialReactiveCapabilityCurve()));
        }
        if (synchronousMachine.minQ() != null) {
            writeAttribute(cimNamespace, SYNCHRONOUS_MACHINE_MIN_Q, format(synchronousMachine.minQ()));
        }
        if (synchronousMachine.maxQ() != null) {
            writeAttribute(cimNamespace, SYNCHRONOUS_MACHINE_MAX_Q, format(synchronousMachine.maxQ()));
        }
        writeAttribute(cimNamespace, ROTATING_MACHINE_RATED_S, format(synchronousMachine.ratedS()));
        writeResource(cimNamespace, SYNCHRONOUS_MACHINE_TYPE,
            cimNamespace + SYNCHRONOUS_MACHINE_KIND + synchronousMachine.type());
        writeEnd();
    }

    /**
     * Write the given TapChangerControl to the CGMES Equipment profile.
     */
    public void writeTapChangerControl(CgmesRecords.TapChangerControlEq tapChangerControl) throws XMLStreamException {
        writeStartId(TAP_CHANGER_CONTROL, tapChangerControl.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(tapChangerControl.name()));
        writeResource(cimNamespace, REGULATING_CONTROL_MODE, cimNamespace + tapChangerControl.mode());
        writeResource(cimNamespace, REGULATING_CONTROL_TERMINAL, toRdfAbout(tapChangerControl.terminal()));
        writeEnd();
    }

    /**
     * Write the given Terminal to the CGMES Equipment profile.
     */
    public void writeTerminal(CgmesRecords.TerminalEq terminal) throws XMLStreamException {
        writeStartId(TERMINAL, terminal.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(terminal.conductingEquipment()));
        writeResource(cimNamespace, TERMINAL_CONDUCTING_EQUIPMENT, toRdfAbout(terminal.conductingEquipment()));
        if (terminal.connectivityNode() != null) {
            writeResource(cimNamespace, TERMINAL_CONNECTIVITY_NODE, toRdfAbout(terminal.connectivityNode()));
        }
        writeAttribute(cimNamespace, AC_DC_TERMINAL_SEQUENCE_NUMBER, format(terminal.sequenceNumber()));
        writeEnd();
    }

    /**
     * Write the given TieFlow to the CGMES Equipment profile.
     */
    public void writeTieFlow(CgmesRecords.TieFlowEq tieFlow) throws XMLStreamException {
        writeStartId(TIE_FLOW, tieFlow.rdfId(), false);
        writeResource(cimNamespace, TIE_FLOW_CONTROL_AREA, toRdfAbout(tieFlow.controlArea()));
        writeResource(cimNamespace, TIE_FLOW_TERMINAL, toRdfAbout(tieFlow.terminal()));
        writeAttribute(cimNamespace, TIE_FLOW_POSITIVE_FLOW_IN, Boolean.toString(true));
        writeEnd();
    }

    /**
     * Write the given VoltageLevel to the CGMES Equipment profile.
     */
    public void writeVoltageLevel(CgmesRecords.VoltageLevelEq voltageLevel) throws XMLStreamException {
        writeStartId(VOLTAGE_LEVEL, voltageLevel.rdfId(), true);
        writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, getValidName(voltageLevel.name()));
        if (!Double.isNaN(voltageLevel.lowVoltageLimit())) {
            writeAttribute(cimNamespace, VOLTAGE_LEVEL_LOW_VOLTAGE_LIMIT, format(voltageLevel.lowVoltageLimit()));
        }
        if (!Double.isNaN(voltageLevel.highVoltageLimit())) {
            writeAttribute(cimNamespace, VOLTAGE_LEVEL_HIGH_VOLTAGE_LIMIT, format(voltageLevel.highVoltageLimit()));
        }
        writeResource(cimNamespace, VOLTAGE_LEVEL_SUBSTATION, toRdfAbout(voltageLevel.substation()));
        writeResource(cimNamespace, VOLTAGE_LEVEL_BASE_VOLTAGE, toRdfAbout(voltageLevel.baseVoltage()));
        writeEnd();
    }
}
