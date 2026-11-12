/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.export.CgmesRecords.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * <p>The serialization of the CGMES Equipment profile.</p>
 *
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class EquipmentWriter {

    private static final double POWER_TRANSFORMER_END_RATED_S_DEFAULT_VALUE = 100.0;
    private static final double B_PER_SECTION_FORMAT_THRESHOLD = 1E-10;

    private EquipmentWriter() {
    }

    /**
     * Write the given ACDCConverter in the CGMES Equipment profile using the provided writer.
     */
    public static void writeAcDcConverter(AcDcConverterEq acDcConverter, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(acDcConverter.className(), acDcConverter.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(acDcConverter.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, ACDC_CONVERTER_RATED_UDC, CgmesExportUtil.format(acDcConverter.ratedUdc()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, ACDC_CONVERTER_IDLE_LOSS, CgmesExportUtil.format(acDcConverter.idleLoss()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, ACDC_CONVERTER_SWITCHING_LOSS, CgmesExportUtil.format(acDcConverter.switchingLoss()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, ACDC_CONVERTER_RESISTIVE_LOSS, CgmesExportUtil.format(acDcConverter.resistiveLoss()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(acDcConverter.equipmentContainer(), context), writer);
        if (acDcConverter.pccTerminal() != null) {
            CgmesExportUtil.writeResource(cimNamespace, ACDC_CONVERTER_PCC_TERMINAL, CgmesExportUtil.toRdfAbout(acDcConverter.pccTerminal(), context), writer);
        }
        if (acDcConverter.capabilityCurve() != null) {
            CgmesExportUtil.writeResource(cimNamespace, VS_CONVERTER_CAPABILITY_CURVE, CgmesExportUtil.toRdfAbout(acDcConverter.capabilityCurve(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given ACLineSegment in the CGMES Equipment profile using the provided writer.
     */
    public static void writeACLineSegment(ACLineSegmentEq line, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(AC_LINE_SEGMENT, line.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(line.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, AC_LINE_SEGMENT_R, CgmesExportUtil.format(line.r()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, AC_LINE_SEGMENT_X, CgmesExportUtil.format(line.x()), writer);
        if (line.g() != 0.0) {
            CgmesExportUtil.writeAttribute(cimNamespace, AC_LINE_SEGMENT_GCH, CgmesExportUtil.format(line.g()), writer);
        }
        CgmesExportUtil.writeAttribute(cimNamespace, AC_LINE_SEGMENT_BCH, CgmesExportUtil.format(line.b()), writer);
        if (line.baseVoltage() != null) {
            CgmesExportUtil.writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, CgmesExportUtil.toRdfAbout(line.baseVoltage(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given AsynchronousMachine in the CGMES Equipment profile using the provided writer.
     */
    public static void writeAsynchronousMachine(AsynchronousMachineEq asynchronousMachine, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(ASYNCHRONOUS_MACHINE, asynchronousMachine.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(asynchronousMachine.name()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given BaseVoltage in the CGMES Equipment profile using the provided writer.
     */
    public static void writeBaseVoltage(BaseVoltageEq baseVoltage, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        String nominalVoltage = CgmesExportUtil.format(baseVoltage.nominalVoltage());
        CgmesExportUtil.writeStartId(BASE_VOLTAGE, baseVoltage.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(nominalVoltage), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, BASE_VOLTAGE_NOMINAL_VOLTAGE, nominalVoltage, writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given BusbarSection in the CGMES Equipment profile using the provided writer.
     */
    public static void writeBusbarSection(BusbarSectionEq busbarSection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(BUSBAR_SECTION, busbarSection.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(busbarSection.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(busbarSection.equipmentContainer(), context), writer);
        CgmesExportUtil.writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, CgmesExportUtil.toRdfAbout(busbarSection.baseVoltage(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given ConnectivityNode in the CGMES Equipment profile using the provided writer.
     */
    public static void writeConnectivityNode(ConnectivityNodeEq node, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(CONNECTIVITY_NODE, node.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(node.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, CONNECTIVITY_NODE_CONNECTIVITY_NODE_CONTAINER, CgmesExportUtil.toRdfAbout(node.connectivityNodeContainer(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given ControlArea in the CGMES Equipment profile using the provided writer.
     */
    public static void writeControlArea(ControlAreaEq controlArea, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        String euNamespace = context.getCim().getEuNamespace();
        CgmesExportUtil.writeStartId(CONTROL_AREA, controlArea.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(controlArea.name()), writer);
        if (controlArea.energyIdentCodeEic() != null) {
            CgmesExportUtil.writeAttribute(euNamespace, IDENTIFIED_OBJECT_ENERGY_IDENT_CODE_EIC, controlArea.energyIdentCodeEic(), writer);
        }
        CgmesExportUtil.writeResource(cimNamespace, CONTROL_AREA_TYPE, cimNamespace + CONTROL_AREA_TYPE_KIND_INTERCHANGE, writer);
        if (!context.isCim16BusBranchExport()) {
            CgmesExportUtil.writeResource(cimNamespace, CONTROL_AREA_ENERGY_AREA, CgmesExportUtil.toRdfAbout(controlArea.energyArea(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given Curve in the CGMES Equipment profile using the provided writer.
     */
    public static void writeCurve(CurveEq curve, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(curve.className(), curve.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(curve.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, CURVE_CURVE_STYLE, cimNamespace + CURVE_STYLE_STRAIGHT_LINE_Y_VALUES, writer);
        CgmesExportUtil.writeResource(cimNamespace, CURVE_X_UNIT, cimNamespace + UNIT_SYMBOL_W, writer);
        CgmesExportUtil.writeResource(cimNamespace, CURVE_Y1_UNIT, cimNamespace + UNIT_SYMBOL_VAR, writer);
        CgmesExportUtil.writeResource(cimNamespace, CURVE_Y2_UNIT, cimNamespace + UNIT_SYMBOL_VAR, writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given CurveData in the CGMES Equipment profile using the provided writer.
     */
    public static void writeCurveData(CurveDataEq curveData, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(CURVE_DATA, curveData.rdfId(), false, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, CURVE_DATA_XVALUE, CgmesExportUtil.format(curveData.xvalue()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, CURVE_DATA_Y1VALUE, CgmesExportUtil.format(curveData.y1value()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, CURVE_DATA_Y2VALUE, CgmesExportUtil.format(curveData.y2value()), writer);
        CgmesExportUtil.writeResource(cimNamespace, CURVE_DATA_CURVE, CgmesExportUtil.toRdfAbout(curveData.curve(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given DCConverterUnit in the CGMES Equipment profile using the provided writer.
     */
    public static void writeDCConverterUnit(DCConverterUnitEq dcConverterUnit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(DC_CONVERTER_UNIT, dcConverterUnit.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcConverterUnit.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, DC_CONVERTER_UNIT_OPERATION_MODE,
            cimNamespace + OPERATING_MODE_KIND + dcConverterUnit.operationMode(), writer);
        CgmesExportUtil.writeResource(cimNamespace, DC_CONVERTER_UNIT_SUBSTATION, CgmesExportUtil.toRdfAbout(dcConverterUnit.substation(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given DCGround in the CGMES Equipment profile using the provided writer.
     */
    public static void writeDCGround(DCGroundEq dcGround, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(DC_GROUND, dcGround.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcGround.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, DC_GROUND_R, CgmesExportUtil.format(dcGround.r()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given DCLineSegment in the CGMES Equipment profile using the provided writer.
     */
    public static void writeDCLineSegment(DCLineSegmentEq dcLineSegment, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(DC_LINE_SEGMENT, dcLineSegment.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcLineSegment.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, DC_LINE_SEGMENT_RESISTANCE, CgmesExportUtil.format(dcLineSegment.resistance()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given DCNode in the CGMES Equipment profile using the provided writer.
     */
    public static void writeDCNode(DCNodeEq dcNode, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(DC_NODE, dcNode.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcNode.name()), writer);
        if (dcNode.equipmentContainer() != null) {
            CgmesExportUtil.writeResource(cimNamespace, DC_NODE_DC_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(dcNode.equipmentContainer(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given DCSwitch in the CGMES Equipment profile using the provided writer.
     */
    public static void writeDCSwitch(DCSwitchEq dcSwitch, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(dcSwitch.className(), dcSwitch.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcSwitch.name()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given DCTerminal in the CGMES Equipment profile using the provided writer.
     */
    public static void writeDCTerminal(DCTerminalEq dcTerminal, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(dcTerminal.className(), dcTerminal.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(dcTerminal.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, dcTerminal.className() + "." + DC_CONDUCTING_EQUIPMENT, CgmesExportUtil.toRdfAbout(dcTerminal.dcConductingEquipment(), context), writer);
        if (dcTerminal.dcNode() != null) {
            CgmesExportUtil.writeResource(cimNamespace, DC_BASE_TERMINAL_DC_NODE, CgmesExportUtil.toRdfAbout(dcTerminal.dcNode(), context), writer);
        }
        CgmesExportUtil.writeAttribute(cimNamespace, AC_DC_TERMINAL_SEQUENCE_NUMBER, CgmesExportUtil.format(dcTerminal.sequenceNumber()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given EnergyConsumer in the CGMES Equipment profile using the provided writer.
     */
    public static void writeEnergyConsumer(EnergyConsumerEq energyConsumer, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(energyConsumer.className(), energyConsumer.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(energyConsumer.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(energyConsumer.equipmentContainer(), context), writer);
        if (energyConsumer.loadGroup() != null) {
            CgmesExportUtil.writeResource(cimNamespace, energyConsumer.className() + "." + LOAD_GROUP, CgmesExportUtil.toRdfAbout(energyConsumer.loadGroup(), context), writer);
        }
        if (energyConsumer.loadResponse() != null) {
            CgmesExportUtil.writeResource(cimNamespace, ENERGY_CONSUMER_LOAD_RESPONSE, CgmesExportUtil.toRdfAbout(energyConsumer.loadResponse(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given EnergySource in the CGMES Equipment profile using the provided writer.
     */
    public static void writeEnergySource(EnergySourceEq energySource, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(ENERGY_SOURCE, energySource.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(energySource.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(energySource.equipmentContainer(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given EquivalentBranch in the CGMES Equipment profile using the provided writer.
     */
    public static void writeEquivalentBranch(EquivalentBranchEq branch, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(EQUIVALENT_BRANCH, branch.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(branch.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_BRANCH_R, CgmesExportUtil.format(branch.r()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_BRANCH_X, CgmesExportUtil.format(branch.x()), writer);
        if (branch.baseVoltage() != null) {
            CgmesExportUtil.writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, CgmesExportUtil.toRdfAbout(branch.baseVoltage(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given EquivalentInjection in the CGMES Equipment profile using the provided writer.
     */
    public static void writeEquivalentInjection(EquivalentInjectionEq equivalentInjection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(EQUIVALENT_INJECTION, equivalentInjection.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(equivalentInjection.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_INJECTION_REGULATION_CAPABILITY, CgmesExportUtil.format(equivalentInjection.regulationCapability()), writer);
        // To avoid Infinite values during reimport, default values are not exported
        if (!CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.minP())) {
            CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MIN_P, CgmesExportUtil.format(equivalentInjection.minP()), writer);
        }
        if (!CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.maxP())) {
            CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MAX_P, CgmesExportUtil.format(equivalentInjection.maxP()), writer);
        }
        if (equivalentInjection.minQ() != null && !CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.minQ())) {
            CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MIN_Q, CgmesExportUtil.format(equivalentInjection.minQ()), writer);
        }
        if (equivalentInjection.maxQ() != null && !CgmesExportUtil.isMinusOrMaxValue(equivalentInjection.maxQ())) {
            CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_INJECTION_MAX_Q, CgmesExportUtil.format(equivalentInjection.maxQ()), writer);
        }
        if (equivalentInjection.reactiveCapabilityCurve() != null) {
            CgmesExportUtil.writeResource(cimNamespace, EQUIVALENT_INJECTION_REACTIVE_CAPABILITY_CURVE, CgmesExportUtil.toRdfAbout(equivalentInjection.reactiveCapabilityCurve(), context), writer);
        }
        CgmesExportUtil.writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, CgmesExportUtil.toRdfAbout(equivalentInjection.baseVoltage(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given EquivalentShunt in the CGMES Equipment profile using the provided writer.
     */
    public static void writeEquivalentShunt(EquivalentShuntEq equivalentShunt, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(EQUIVALENT_SHUNT, equivalentShunt.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(equivalentShunt.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_SHUNT_B, CgmesExportUtil.format(equivalentShunt.b()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EQUIVALENT_SHUNT_G, CgmesExportUtil.format(equivalentShunt.g()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(equivalentShunt.equipmentContainer(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given ExternalNetworkInjection in the CGMES Equipment profile using the provided writer.
     */
    public static void writeExternalNetworkInjection(ExternalNetworkInjectionEq externalNetworkInjection, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(EXTERNAL_NETWORK_INJECTION, externalNetworkInjection.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(externalNetworkInjection.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(externalNetworkInjection.equipmentContainer(), context), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_GOVERNOR_SCD, CgmesExportUtil.format(externalNetworkInjection.governorScd()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MAX_P, CgmesExportUtil.format(externalNetworkInjection.maxP()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MAX_Q, CgmesExportUtil.format(externalNetworkInjection.maxQ()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MIN_P, CgmesExportUtil.format(externalNetworkInjection.minP()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, EXTERNAL_NETWORK_INJECTION_MIN_Q, CgmesExportUtil.format(externalNetworkInjection.minQ()), writer);
        if (externalNetworkInjection.regulatingControl() != null) {
            CgmesExportUtil.writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, CgmesExportUtil.toRdfAbout(externalNetworkInjection.regulatingControl(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given FossilFuel in the CGMES Equipment profile using the provided writer.
     */
    public static void writeFossilFuel(FossilFuelEq fossilFuel, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(FOSSIL_FUEL, fossilFuel.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(fossilFuel.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, FOSSIL_FUEL_FOSSIL_FUEL_TYPE,
            cimNamespace + fossilFuel.fossilFuelType(), writer);
        CgmesExportUtil.writeResource(cimNamespace, FOSSIL_FUEL_THERMAL_GENERATING_UNIT, CgmesExportUtil.toRdfAbout(fossilFuel.thermalGeneratingUnit(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given GeneratingUnit in the CGMES Equipment profile using the provided writer.
     */
    public static void writeGeneratingUnit(GeneratingUnitEq generatingUnit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(generatingUnit.className(), generatingUnit.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(generatingUnit.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, GENERATING_UNIT_MIN_OPERATING_P, CgmesExportUtil.format(generatingUnit.minOperatingP()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, GENERATING_UNIT_MAX_OPERATING_P, CgmesExportUtil.format(generatingUnit.maxOperatingP()), writer);
        if (context.getCimVersion() == 16) {
            CgmesExportUtil.writeAttribute(cimNamespace, GENERATING_UNIT_INITIAL_P, CgmesExportUtil.format(generatingUnit.initialP()), writer);
        }
        if (generatingUnit.equipmentContainer() != null) {
            CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(generatingUnit.equipmentContainer(), context), writer);
        }
        if (WIND_GENERATING_UNIT.equals(generatingUnit.className())) {
            CgmesExportUtil.writeResource(cimNamespace, WIND_GENERATING_UNIT_WIND_GEN_UNIT_TYPE,
                cimNamespace + WIND_GEN_UNIT_KIND + generatingUnit.windGenUnitType(), writer);
        }
        if (generatingUnit.hydroPowerPlant() != null) {
            CgmesExportUtil.writeResource(cimNamespace, HYDRO_GENERATING_UNIT_HYDRO_POWER_PLANT, CgmesExportUtil.toRdfAbout(generatingUnit.hydroPowerPlant(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given GeographicalRegion in the CGMES Equipment profile using the provided writer.
     */
    public static void writeGeographicalRegion(GeographicalRegionEq region, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(GEOGRAPHICAL_REGION, region.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(region.name()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given HydroPowerPlant in the CGMES Equipment profile using the provided writer.
     */
    public static void writeHydroPowerPlant(HydroPowerPlantEq plant, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(HYDRO_POWER_PLANT, plant.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(plant.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, HYDRO_POWER_PLANT_HYDRO_PLANT_STORAGE_TYPE,
            cimNamespace + plant.hydroPlantStorageType(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given LoadArea in the CGMES Equipment profile using the provided writer.
     */
    public static void writeLoadArea(LoadAreaEq loadArea, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(LOAD_AREA, loadArea.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(loadArea.name() + " Load Area"), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given LoadGroup in the CGMES Equipment profile using the provided writer.
     */
    public static void writeLoadGroup(LoadGroupEq loadGroup, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(loadGroup.className(), loadGroup.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(loadGroup.name()), writer);
        if (!context.isCim16BusBranchExport()) {
            CgmesExportUtil.writeResource(cimNamespace, LOAD_GROUP_SUB_LOAD_AREA, CgmesExportUtil.toRdfAbout(loadGroup.subLoadArea(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given LoadResponseCharacteristic in the CGMES Equipment profile using the provided writer.
     */
    public static void writeLoadResponseCharacteristic(LoadResponseCharacteristicEq loadResponseCharacteristic, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(LOAD_RESPONSE_CHARACTERISTIC, loadResponseCharacteristic.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(loadResponseCharacteristic.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_EXPONENT_MODEL, CgmesExportUtil.format(loadResponseCharacteristic.exponentModel()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_CURRENT, CgmesExportUtil.format(loadResponseCharacteristic.pConstantCurrent()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_IMPEDANCE, CgmesExportUtil.format(loadResponseCharacteristic.pConstantImpedance()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_CONSTANT_POWER, CgmesExportUtil.format(loadResponseCharacteristic.pConstantPower()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_P_VOLTAGE_EXPONENT, CgmesExportUtil.format(loadResponseCharacteristic.pVoltageExponent()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_CURRENT, CgmesExportUtil.format(loadResponseCharacteristic.qConstantCurrent()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_IMPEDANCE, CgmesExportUtil.format(loadResponseCharacteristic.qConstantImpedance()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_CONSTANT_POWER, CgmesExportUtil.format(loadResponseCharacteristic.qConstantPower()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, LOAD_RESPONSE_CHARACTERISTIC_Q_VOLTAGE_EXPONENT, CgmesExportUtil.format(loadResponseCharacteristic.qVoltageExponent()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given NonlinearShuntCompensatorPoint in the CGMES Equipment profile using the provided writer.
     */
    public static void writeNonLinearShuntCompensatorPoint(NonlinearShuntCompensatorPointEq point, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(NONLINEAR_SHUNT_COMPENSATOR_POINT, point.rdfId(), false, writer, context);
        CgmesExportUtil.writeResource(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_NONLINEAR_SHUNT_COMPENSATOR, CgmesExportUtil.toRdfAbout(point.nonlinearShuntCompensator(), context), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_SECTION_NUMBER, CgmesExportUtil.format(point.sectionNumber()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_B, CgmesExportUtil.format(point.b()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, NONLINEAR_SHUNT_COMPENSATOR_POINT_G, CgmesExportUtil.format(point.g()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given OperationalLimit in the CGMES Equipment profile using the provided writer.
     */
    public static void writeOperationalLimit(OperationalLimitEq loadingLimit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(loadingLimit.className(), loadingLimit.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(loadingLimit.name()), writer);
        String limitValueAttributeName = context.getCimVersion() == 16 ? VALUE : NORMAL_VALUE;
        CgmesExportUtil.writeAttribute(cimNamespace, loadingLimit.className() + "." + limitValueAttributeName, CgmesExportUtil.format(loadingLimit.value()), writer);
        CgmesExportUtil.writeResource(cimNamespace, OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_SET, CgmesExportUtil.toRdfAbout(loadingLimit.operationalLimitSet(), context), writer);
        CgmesExportUtil.writeResource(cimNamespace, OPERATIONAL_LIMIT_OPERATIONAL_LIMIT_TYPE, CgmesExportUtil.toRdfAbout(loadingLimit.operationalLimitType(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given OperationalLimitSet in the CGMES Equipment profile using the provided writer.
     */
    public static void writeOperationalLimitSet(OperationalLimitSetEq operationalLimitSet, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(OPERATIONAL_LIMIT_SET, operationalLimitSet.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(operationalLimitSet.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, OPERATIONAL_LIMIT_SET_TERMINAL, CgmesExportUtil.toRdfAbout(operationalLimitSet.terminal(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given OperationalLimitType in the CGMES Equipment profile using the provided writer.
     */
    public static void writeOperationalLimitType(OperationalLimitTypeEq limitType, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        String euNamespace = context.getCim().getEuNamespace();
        CgmesExportUtil.writeStartId(OPERATIONAL_LIMIT_TYPE, limitType.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(limitType.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, OPERATIONAL_LIMIT_TYPE_DIRECTION, cimNamespace + OPERATIONAL_LIMIT_DIRECTION_KIND_ABSOLUTE_VALUE, writer);
        String limitTypeAttributeName = context.getCimVersion() == 16 ? OPERATIONAL_LIMIT_TYPE + ".limitType" : OPERATIONAL_LIMIT_TYPE + ".kind";
        String limitKindClassName = context.getCimVersion() == 16 ? "LimitTypeKind" : "LimitKind";
        CgmesExportUtil.writeResource(euNamespace, limitTypeAttributeName, euNamespace + limitKindClassName + "." + limitType.kind(), writer);
        if (context.getCimVersion() == 100) {
            CgmesExportUtil.writeAttribute(cimNamespace, OPERATIONAL_LIMIT_TYPE_IS_INFINITE_DURATION, Boolean.toString(limitType.isInfiniteDuration()), writer);
        }
        if (!limitType.isInfiniteDuration()) {
            CgmesExportUtil.writeAttribute(cimNamespace, OPERATIONAL_LIMIT_TYPE_ACCEPTABLE_DURATION, CgmesExportUtil.format(limitType.acceptableDuration()), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given PhaseTapChanger in the CGMES Equipment profile using the provided writer.
     */
    public static void writePhaseTapChanger(PhaseTapChangerEq phaseTapChanger, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(PHASE_TAP_CHANGER_TABULAR, phaseTapChanger.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(phaseTapChanger.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, PHASE_TAP_CHANGER_TRANSFORMER_END, CgmesExportUtil.toRdfAbout(phaseTapChanger.transformerEnd(), context), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_LOW_STEP, CgmesExportUtil.format(phaseTapChanger.lowStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_HIGH_STEP, CgmesExportUtil.format(phaseTapChanger.highStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_NORMAL_STEP, CgmesExportUtil.format(phaseTapChanger.normalStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_STEP, CgmesExportUtil.format(phaseTapChanger.neutralStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_U, CgmesExportUtil.format(phaseTapChanger.neutralU()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_LTC_FLAG, CgmesExportUtil.format(phaseTapChanger.ltcFlag()), writer);
        CgmesExportUtil.writeResource(cimNamespace, PHASE_TAP_CHANGER_TABULAR_PHASE_TAP_CHANGER_TABLE, CgmesExportUtil.toRdfAbout(phaseTapChanger.phaseTapChangerTable(), context), writer);
        if (phaseTapChanger.tapChangerControl() != null) {
            CgmesExportUtil.writeResource(cimNamespace, TAP_CHANGER_TAP_CHANGER_CONTROL, CgmesExportUtil.toRdfAbout(phaseTapChanger.tapChangerControl(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given PhaseTapChangerTable in the CGMES Equipment profile using the provided writer.
     */
    public static void writePhaseTapChangerTable(PhaseTapChangerTableEq phaseTapChangerTable, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(PHASE_TAP_CHANGER_TABLE, phaseTapChangerTable.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(phaseTapChangerTable.name()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given PhaseTapChangerTablePoint in the CGMES Equipment profile using the provided writer.
     */
    public static void writePhaseTapChangerTablePoint(PhaseTapChangerTablePointEq point, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(PHASE_TAP_CHANGER_TABLE_POINT, point.rdfId(), false, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_R, CgmesExportUtil.format(point.r()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_X, CgmesExportUtil.format(point.x()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_G, CgmesExportUtil.format(point.g()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_B, CgmesExportUtil.format(point.b()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_STEP, CgmesExportUtil.format(point.step()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_RATIO, CgmesExportUtil.format(point.ratio()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, PHASE_TAP_CHANGER_TABLE_POINT_ANGLE, CgmesExportUtil.format(point.angle()), writer);
        CgmesExportUtil.writeResource(cimNamespace, PHASE_TAP_CHANGER_TABLE_POINT_PHASE_TAP_CHANGER_TABLE, CgmesExportUtil.toRdfAbout(point.phaseTapChangerTable(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given PowerTransformer in the CGMES Equipment profile using the provided writer.
     */
    public static void writePowerTransformer(PowerTransformerEq powerTransformer, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(POWER_TRANSFORMER, powerTransformer.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(powerTransformer.name()), writer);
        if (powerTransformer.equipmentContainer() != null) {
            CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(powerTransformer.equipmentContainer(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given PowerTransformerEnd in the CGMES Equipment profile using the provided writer.
     */
    public static void writePowerTransformerEnd(PowerTransformerEndEq powerTransformerEnd, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(POWER_TRANSFORMER_END, powerTransformerEnd.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(powerTransformerEnd.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TRANSFORMER_END_END_NUMBER, CgmesExportUtil.format(powerTransformerEnd.endNumber()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, POWER_TRANSFORMER_END_R, CgmesExportUtil.format(powerTransformerEnd.r()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, POWER_TRANSFORMER_END_X, CgmesExportUtil.format(powerTransformerEnd.x()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, POWER_TRANSFORMER_END_G, CgmesExportUtil.format(powerTransformerEnd.g()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, POWER_TRANSFORMER_END_B, CgmesExportUtil.format(powerTransformerEnd.b()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, POWER_TRANSFORMER_END_RATED_S, CgmesExportUtil.format(powerTransformerEnd.ratedS(), POWER_TRANSFORMER_END_RATED_S_DEFAULT_VALUE), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, POWER_TRANSFORMER_END_RATED_U, CgmesExportUtil.format(powerTransformerEnd.ratedU()), writer);
        CgmesExportUtil.writeResource(cimNamespace, POWER_TRANSFORMER_END_POWER_TRANSFORMER, CgmesExportUtil.toRdfAbout(powerTransformerEnd.powerTransformer(), context), writer);
        CgmesExportUtil.writeResource(cimNamespace, TRANSFORMER_END_TERMINAL, CgmesExportUtil.toRdfAbout(powerTransformerEnd.terminal(), context), writer);
        CgmesExportUtil.writeResource(cimNamespace, TRANSFORMER_END_BASE_VOLTAGE, CgmesExportUtil.toRdfAbout(powerTransformerEnd.baseVoltage(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given RatioTapChanger in the CGMES Equipment profile using the provided writer.
     */
    public static void writeRatioTapChanger(RatioTapChangerEq ratioTapChanger, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(RATIO_TAP_CHANGER, ratioTapChanger.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(ratioTapChanger.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, RATIO_TAP_CHANGER_TRANSFORMER_END, CgmesExportUtil.toRdfAbout(ratioTapChanger.transformerEnd(), context), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, RATIO_TAP_CHANGER_STEP_VOLTAGE_INCREMENT, CgmesExportUtil.format(ratioTapChanger.stepVoltageIncrement()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_LOW_STEP, CgmesExportUtil.format(ratioTapChanger.lowStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_HIGH_STEP, CgmesExportUtil.format(ratioTapChanger.highStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_NORMAL_STEP, CgmesExportUtil.format(ratioTapChanger.normalStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_STEP, CgmesExportUtil.format(ratioTapChanger.neutralStep()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_NEUTRAL_U, CgmesExportUtil.format(ratioTapChanger.neutralU()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_LTC_FLAG, CgmesExportUtil.format(ratioTapChanger.ltcFlag()), writer);
        if (context.getCimVersion() == 16) {
            CgmesExportUtil.writeResource(cimNamespace, RATIO_TAP_CHANGER_TCUL_CONTROL_MODE,
                cimNamespace + TRANSFORMER_CONTROL_MODE + "." + ratioTapChanger.tculControlMode(), writer);
        }
        CgmesExportUtil.writeResource(cimNamespace, RATIO_TAP_CHANGER_RATIO_TAP_CHANGER_TABLE, CgmesExportUtil.toRdfAbout(ratioTapChanger.ratioTapChangerTable(), context), writer);
        if (ratioTapChanger.tapChangerControl() != null) {
            CgmesExportUtil.writeResource(cimNamespace, TAP_CHANGER_TAP_CHANGER_CONTROL, CgmesExportUtil.toRdfAbout(ratioTapChanger.tapChangerControl(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given RatioTapChangerTable in the CGMES Equipment profile using the provided writer.
     */
    public static void writeRatioTapChangerTable(RatioTapChangerTableEq ratioTapChangerTable, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(RATIO_TAP_CHANGER_TABLE, ratioTapChangerTable.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(ratioTapChangerTable.name()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given RatioTapChangerTablePoint in the CGMES Equipment profile using the provided writer.
     */
    public static void writeRatioTapChangerTablePoint(RatioTapChangerTablePointEq point, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(RATIO_TAP_CHANGER_TABLE_POINT, point.rdfId(), false, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_R, CgmesExportUtil.format(point.r()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_X, CgmesExportUtil.format(point.x()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_G, CgmesExportUtil.format(point.g()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_B, CgmesExportUtil.format(point.b()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_STEP, CgmesExportUtil.format(point.step()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TAP_CHANGER_TABLE_POINT_RATIO, CgmesExportUtil.format(point.ratio()), writer);
        CgmesExportUtil.writeResource(cimNamespace, RATIO_TAP_CHANGER_TABLE_POINT_RATIO_TAP_CHANGER_TABLE, CgmesExportUtil.toRdfAbout(point.ratioTapChangerTable(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given RegulatingControl in the CGMES Equipment profile using the provided writer.
     */
    public static void writeRegulatingControl(RegulatingControlEq regulatingControl, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(REGULATING_CONTROL, regulatingControl.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(regulatingControl.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, REGULATING_CONTROL_TERMINAL, CgmesExportUtil.toRdfAbout(regulatingControl.terminal(), context), writer);
        CgmesExportUtil.writeResource(cimNamespace, REGULATING_CONTROL_MODE,
            cimNamespace + regulatingControl.mode(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given SeriesCompensator in the CGMES Equipment profile using the provided writer.
     */
    public static void writeSeriesCompensator(SeriesCompensatorEq sc, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(SERIES_COMPENSATOR, sc.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(sc.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SERIES_COMPENSATOR_R, CgmesExportUtil.format(sc.r()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SERIES_COMPENSATOR_X, CgmesExportUtil.format(sc.x()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SERIES_COMPENSATOR_VARISTOR_PRESENT, CgmesExportUtil.format(sc.varistorPresent()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SERIES_COMPENSATOR_VARISTOR_RATED_CURRENT, CgmesExportUtil.format(sc.varistorRatedCurrent()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SERIES_COMPENSATOR_VARISTOR_VOLTAGE_THRESHOLD, CgmesExportUtil.format(sc.varistorVoltageThreshold()), writer);
        if (sc.baseVoltage() != null) {
            CgmesExportUtil.writeResource(cimNamespace, CONDUCTING_EQUIPMENT_BASE_VOLTAGE, CgmesExportUtil.toRdfAbout(sc.baseVoltage(), context), writer);
        }
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given ShuntCompensator in the CGMES Equipment profile using the provided writer.
     */
    public static void writeShuntCompensator(ShuntCompensatorEq shuntCompensator, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(shuntCompensator.className(), shuntCompensator.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(shuntCompensator.name()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SHUNT_COMPENSATOR_NORMAL_SECTIONS, CgmesExportUtil.format(shuntCompensator.normalSections()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SHUNT_COMPENSATOR_MAXIMUM_SECTIONS, CgmesExportUtil.format(shuntCompensator.maximumSections()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SHUNT_COMPENSATOR_NOM_U, CgmesExportUtil.format(shuntCompensator.nomU()), writer);
        if (LINEAR_SHUNT_COMPENSATOR.equals(shuntCompensator.className())) {
            String bPerSection = Math.abs(shuntCompensator.bPerSection()) < B_PER_SECTION_FORMAT_THRESHOLD
                ? CgmesExportUtil.scientificFormat(shuntCompensator.bPerSection())
                : CgmesExportUtil.format(shuntCompensator.bPerSection());
            CgmesExportUtil.writeAttribute(cimNamespace, LINEAR_SHUNT_COMPENSATOR_B_PER_SECTION, bPerSection, writer);
            String gPerSection = Double.isNaN(shuntCompensator.gPerSection()) ? "0" : CgmesExportUtil.format(shuntCompensator.gPerSection());
            CgmesExportUtil.writeAttribute(cimNamespace, LINEAR_SHUNT_COMPENSATOR_G_PER_SECTION, gPerSection, writer);
        }
        if (shuntCompensator.regulatingControl() != null) {
            CgmesExportUtil.writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, CgmesExportUtil.toRdfAbout(shuntCompensator.regulatingControl(), context), writer);
        }
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(shuntCompensator.equipmentContainer(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given StaticVarCompensator in the CGMES Equipment profile using the provided writer.
     */
    public static void writeStaticVarCompensator(StaticVarCompensatorEq staticVarCompensator, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(STATIC_VAR_COMPENSATOR, staticVarCompensator.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(staticVarCompensator.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(staticVarCompensator.equipmentContainer(), context), writer);
        if (staticVarCompensator.regulatingControl() != null) {
            CgmesExportUtil.writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, CgmesExportUtil.toRdfAbout(staticVarCompensator.regulatingControl(), context), writer);
        }
        CgmesExportUtil.writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_INDUCTIVE_RATING, CgmesExportUtil.format(staticVarCompensator.inductiveRating()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_CAPACITIVE_RATING, CgmesExportUtil.format(staticVarCompensator.capacitiveRating()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_SLOPE, CgmesExportUtil.format(Double.isNaN(staticVarCompensator.slope()) ? 0. : staticVarCompensator.slope()), writer);
        CgmesExportUtil.writeResource(cimNamespace, STATIC_VAR_COMPENSATOR_SVC_CONTROL_MODE,
            cimNamespace + staticVarCompensator.svcControlMode(), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, STATIC_VAR_COMPENSATOR_VOLTAGE_SET_POINT, CgmesExportUtil.format(staticVarCompensator.voltageSetPoint()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given SubGeographicalRegion in the CGMES Equipment profile using the provided writer.
     */
    public static void writeSubGeographicalRegion(SubGeographicalRegionEq subRegion, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(SUB_GEOGRAPHICAL_REGION, subRegion.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(subRegion.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, SUB_GEOGRAPHICAL_REGION_REGION, CgmesExportUtil.toRdfAbout(subRegion.region(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given SubLoadArea in the CGMES Equipment profile using the provided writer.
     */
    public static void writeSubLoadArea(SubLoadAreaEq subLoadArea, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(SUB_LOAD_AREA, subLoadArea.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(subLoadArea.name() + " SubLoad Area"), writer);
        CgmesExportUtil.writeResource(cimNamespace, SUB_LOAD_AREA_LOAD_AREA, CgmesExportUtil.toRdfAbout(subLoadArea.loadArea(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given Substation in the CGMES Equipment profile using the provided writer.
     */
    public static void writeSubstation(SubstationEq substation, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(SUBSTATION, substation.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(substation.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, SUBSTATION_REGION, CgmesExportUtil.toRdfAbout(substation.region(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given Switch in the CGMES Equipment profile using the provided writer.
     */
    public static void writeSwitch(SwitchEq switchEq, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(switchEq.className(), switchEq.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(switchEq.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(switchEq.equipmentContainer(), context), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SWITCH_NORMAL_OPEN, CgmesExportUtil.format(switchEq.normalOpen()), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, SWITCH_RETAINED, CgmesExportUtil.format(switchEq.retained()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given SynchronousMachine in the CGMES Equipment profile using the provided writer.
     */
    public static void writeSynchronousMachine(SynchronousMachineEq synchronousMachine, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(SYNCHRONOUS_MACHINE, synchronousMachine.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(synchronousMachine.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, EQUIPMENT_EQUIPMENT_CONTAINER, CgmesExportUtil.toRdfAbout(synchronousMachine.equipmentContainer(), context), writer);
        if (synchronousMachine.generatingUnit() != null) {
            // Some synchronous machines are not associated to a generating unit (condensers)
            CgmesExportUtil.writeResource(cimNamespace, ROTATING_MACHINE_GENERATING_UNIT, CgmesExportUtil.toRdfAbout(synchronousMachine.generatingUnit(), context), writer);
        }
        if (synchronousMachine.regulatingControl() != null) {
            CgmesExportUtil.writeResource(cimNamespace, REGULATING_COND_EQ_REGULATING_CONTROL, CgmesExportUtil.toRdfAbout(synchronousMachine.regulatingControl(), context), writer);
        }
        if (synchronousMachine.initialReactiveCapabilityCurve() != null) {
            CgmesExportUtil.writeResource(cimNamespace, SYNCHRONOUS_MACHINE_INITIAL_REACTIVE_CAPABILITY_CURVE,
                CgmesExportUtil.toRdfAbout(synchronousMachine.initialReactiveCapabilityCurve(), context), writer);
        }
        if (synchronousMachine.minQ() != null) {
            CgmesExportUtil.writeAttribute(cimNamespace, SYNCHRONOUS_MACHINE_MIN_Q, CgmesExportUtil.format(synchronousMachine.minQ()), writer);
        }
        if (synchronousMachine.maxQ() != null) {
            CgmesExportUtil.writeAttribute(cimNamespace, SYNCHRONOUS_MACHINE_MAX_Q, CgmesExportUtil.format(synchronousMachine.maxQ()), writer);
        }
        CgmesExportUtil.writeAttribute(cimNamespace, ROTATING_MACHINE_RATED_S, synchronousMachine.ratedS(), writer);
        CgmesExportUtil.writeResource(cimNamespace, SYNCHRONOUS_MACHINE_TYPE,
            cimNamespace + SYNCHRONOUS_MACHINE_KIND + synchronousMachine.type(), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given TapChangerControl in the CGMES Equipment profile using the provided writer.
     */
    public static void writeTapChangerControl(TapChangerControlEq tapChangerControl, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(TAP_CHANGER_CONTROL, tapChangerControl.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(tapChangerControl.name()), writer);
        CgmesExportUtil.writeResource(cimNamespace, REGULATING_CONTROL_MODE,
            cimNamespace + tapChangerControl.mode(), writer);
        CgmesExportUtil.writeResource(cimNamespace, REGULATING_CONTROL_TERMINAL, CgmesExportUtil.toRdfAbout(tapChangerControl.terminal(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given Terminal in the CGMES Equipment profile using the provided writer.
     */
    public static void writeTerminal(TerminalEq terminal, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(TERMINAL, terminal.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(terminal.conductingEquipment()), writer);
        CgmesExportUtil.writeResource(cimNamespace, TERMINAL_CONDUCTING_EQUIPMENT, CgmesExportUtil.toRdfAbout(terminal.conductingEquipment(), context), writer);
        if (terminal.connectivityNode() != null) {
            CgmesExportUtil.writeResource(cimNamespace, TERMINAL_CONNECTIVITY_NODE, CgmesExportUtil.toRdfAbout(terminal.connectivityNode(), context), writer);
        }
        CgmesExportUtil.writeAttribute(cimNamespace, AC_DC_TERMINAL_SEQUENCE_NUMBER, CgmesExportUtil.format(terminal.sequenceNumber()), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given TieFlow in the CGMES Equipment profile using the provided writer.
     */
    public static void writeTieFlow(TieFlowEq tieFlow, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(TIE_FLOW, tieFlow.rdfId(), false, writer, context);
        CgmesExportUtil.writeResource(cimNamespace, TIE_FLOW_CONTROL_AREA, CgmesExportUtil.toRdfAbout(tieFlow.controlArea(), context), writer);
        CgmesExportUtil.writeResource(cimNamespace, TIE_FLOW_TERMINAL, CgmesExportUtil.toRdfAbout(tieFlow.terminal(), context), writer);
        CgmesExportUtil.writeAttribute(cimNamespace, TIE_FLOW_POSITIVE_FLOW_IN, Boolean.toString(true), writer);
        CgmesExportUtil.writeEnd(writer);
    }

    /**
     * Write the given VoltageLevel in the CGMES Equipment profile using the provided writer.
     */
    public static void writeVoltageLevel(VoltageLevelEq voltageLevel, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartId(VOLTAGE_LEVEL, voltageLevel.rdfId(), true, writer, context);
        CgmesExportUtil.writeAttribute(cimNamespace, IDENTIFIED_OBJECT_NAME, CgmesExportUtil.getValidName(voltageLevel.name()), writer);
        if (!Double.isNaN(voltageLevel.lowVoltageLimit())) {
            CgmesExportUtil.writeAttribute(cimNamespace, VOLTAGE_LEVEL_LOW_VOLTAGE_LIMIT, CgmesExportUtil.format(voltageLevel.lowVoltageLimit()), writer);
        }
        if (!Double.isNaN(voltageLevel.highVoltageLimit())) {
            CgmesExportUtil.writeAttribute(cimNamespace, VOLTAGE_LEVEL_HIGH_VOLTAGE_LIMIT, CgmesExportUtil.format(voltageLevel.highVoltageLimit()), writer);
        }
        CgmesExportUtil.writeResource(cimNamespace, VOLTAGE_LEVEL_SUBSTATION, CgmesExportUtil.toRdfAbout(voltageLevel.substation(), context), writer);
        CgmesExportUtil.writeResource(cimNamespace, VOLTAGE_LEVEL_BASE_VOLTAGE, CgmesExportUtil.toRdfAbout(voltageLevel.baseVoltage(), context), writer);
        CgmesExportUtil.writeEnd(writer);
    }
}
