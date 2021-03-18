/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class EquipmentExport {

    private static final String EQ_BASEVOLTAGE_NOMINALV = "BaseVoltage.nominalVoltage";

    private static final String EQ_GENERATINGUNIT_MINP = "GeneratingUnit.minOperatingP";
    private static final String EQ_GENERATINGUNIT_MAXP = "GeneratingUnit.maxOperatingP";
    private static final String EQ_GENERATINGUNIT_INITIALP = "GeneratingUnit.initialP";

    private static final String EQ_SHUNTCOMPENSATOR_NORMALSECTIONS = "ShuntCompensator.normalSections";
    private static final String EQ_SHUNTCOMPENSATOR_MAXIMUMSECTIONS = "ShuntCompensator.maximumSections";

    private static final String EQ_STATICVARCOMPENSATOR_INDUCTIVERATING = "StaticVarCompensator.inductiveRating";
    private static final String EQ_STATICVARCOMPENSATOR_CAPACITIVERATING = "StaticVarCompensator.capacitiveRating";
    private static final String EQ_STATICVARCOMPENSATOR_SLOPE = "StaticVarCompensator.slope";
    private static final String EQ_STATICVARCOMPENSATOR_SVCCONTROLMODE = "StaticVarCompensator.sVCControlMode";
    private static final String EQ_STATICVARCOMPENSATOR_VOLTAGESETPOINT = "StaticVarCompensator.voltageSetPoint";

    private static final String EQ_ACLINESEGMENT_R = "ACLineSegment.r";
    private static final String EQ_ACLINESEGMENT_X = "ACLineSegment.x";
    private static final String EQ_ACLINESEGMENT_BCH = "ACLineSegment.bch";

    private static final String EQ_EQUIVALENTBRANCH_R = "EquivalentBranch.r";
    private static final String EQ_EQUIVALENTBRANCH_X = "EquivalentBranch.x";

    private static final String EQ_TRANSFORMEREND_NAME = "TransformerEnd.endNumber";
    private static final String EQ_POWERTRANSFORMEREND_R = "PowerTransformerEnd.r";
    private static final String EQ_POWERTRANSFORMEREND_X = "PowerTransformerEnd.x";
    private static final String EQ_POWERTRANSFORMEREND_B = "PowerTransformerEnd.b";

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeRdfRoot(context.getCimVersion(), writer);
            String cimNamespace = context.getCimNamespace();

            // TODO fill EQ Model Description
            if (context.getCimVersion() == 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getEqModelDescription(), context);
            }

            writeSubstations(network, cimNamespace, writer);
            writeVoltageLevels(network, cimNamespace, writer);
            writeLoads(network, cimNamespace, writer);
            writeGenerators(network, cimNamespace, writer);
            writeShuntCompensators(network, cimNamespace, writer);
            writeStaticVarCompensators(network, cimNamespace, writer);
            writeLine(network, cimNamespace, writer);
            writeTwoWindingsTransformer(network, cimNamespace, writer);
            writeThreeWindingsTransformer(network, cimNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSubstations(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Map<Country, String> geographicalRegionIds = new HashMap<>();
        Set<String> geographicalTags = new HashSet<>();
        for (Substation substation : network.getSubstations()) {
            Country country = substation.getCountry().get();
            if (!geographicalRegionIds.containsKey(country)) {
                String geographicalRegionId = CgmesExportUtil.getUniqueId();
                geographicalRegionIds.put(country, geographicalRegionId);
                writeEqGeographicalRegion(geographicalRegionId, country.toString(), cimNamespace, writer);
            }
            writeEqSubstation(substation.getId(), substation.getNameOrId(), geographicalRegionIds.get(country), cimNamespace, writer);
        }
    }

    private static void writeEqGeographicalRegion(String id, String regionName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "GeographicalRegion");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(regionName);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeEqSubstation(String id, String substationName, String geographicalRegionId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "Substation");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(substationName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "Substation.Region");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + geographicalRegionId);
        writer.writeEndElement();
    }

    private static void writeVoltageLevels(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Map<Double, String> baseVoltageIds = new HashMap<>();
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            double nominalV = voltageLevel.getNominalV();
            if (!baseVoltageIds.containsKey(nominalV)) {
                String baseVoltageId = CgmesExportUtil.getUniqueId();
                baseVoltageIds.put(nominalV, baseVoltageId);
                writeEqBaseVoltages(baseVoltageId, nominalV, cimNamespace, writer);
            }
            writeEqVoltageLevel(voltageLevel.getId(), voltageLevel.getNameOrId(), voltageLevel.getSubstation().getId(), baseVoltageIds.get(voltageLevel.getNominalV()), cimNamespace, writer);
        }
    }

    private static void writeEqBaseVoltages(String id, double nominalV, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "BaseVoltage");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(CgmesExportUtil.format(nominalV));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_BASEVOLTAGE_NOMINALV);
        writer.writeCharacters(CgmesExportUtil.format(nominalV));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeEqVoltageLevel(String id, String voltageLevelName, String substationId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "VoltageLevel");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(voltageLevelName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "VoltageLevel.Substation");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + substationId);
        writer.writeEmptyElement(cimNamespace, "VoltageLevel.BaseVoltage");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + baseVoltageId);
        writer.writeEndElement();
    }

    private static void writeLoads(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            writeEqEnergyConsumer(load.getId(), load.getNameOrId(), load.getExtension(LoadDetail.class), load.getTerminal().getVoltageLevel().getId(), cimNamespace, writer);
        }
    }

    private static void writeEqEnergyConsumer(String id, String loadName, LoadDetail loadDetail, String equipmentContainer, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, loadClassName(loadDetail));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(loadName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "Equipment.EquipmentContainer");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + equipmentContainer);
        writer.writeEndElement();
    }

    private static String loadClassName(LoadDetail loadDetail) {
        if (loadDetail != null) {
            // Conform load if fixed part is zero and variable part is non-zero
            if (loadDetail.getFixedActivePower() == 0 && loadDetail.getFixedReactivePower() == 0
                    && (loadDetail.getVariableActivePower() != 0 || loadDetail.getVariableReactivePower() != 0)) {
                return "ConformLoad";
            }
            // NonConform load if fixed part is non-zero and variable part is all zero
            if (loadDetail.getVariableActivePower() == 0 && loadDetail.getVariableReactivePower() == 0
                    && (loadDetail.getFixedActivePower() != 0 || loadDetail.getFixedReactivePower() != 0)) {
                return "NonConformLoad";
            }
        }
        return "EnergyConsumer";
    }

    private static void writeGenerators(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Generator generator : network.getGenerators()) {
            String generatingUnit = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            if (generatingUnit == null) {
                generatingUnit = CgmesExportUtil.getUniqueId();
            }
            writeEqSynchronousMachine(generator.getId(), generator.getNameOrId(), generatingUnit, cimNamespace, writer);
            String generatingUnitName = "GEN_" + generator.getNameOrId();
            writeEqGeneratingUnit(generatingUnit, generatingUnitName, generator.getEnergySource(), generator.getMinP(), generator.getMaxP(), generator.getTargetP(), cimNamespace, writer);
        }
    }

    private static void writeEqSynchronousMachine(String id, String generatorName, String generatingUnit, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "SynchronousMachine");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(generatorName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "RotatingMachine.GeneratingUnit");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + generatingUnit);
        writer.writeEndElement();
    }

    private static void writeEqGeneratingUnit(String id, String generatingUnitName, EnergySource energySource, double minP, double maxP, double initialP, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, generatingUnitClassName(energySource));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(generatingUnitName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_GENERATINGUNIT_MINP);
        writer.writeCharacters(CgmesExportUtil.format(minP));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_GENERATINGUNIT_MAXP);
        writer.writeCharacters(CgmesExportUtil.format(maxP));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_GENERATINGUNIT_INITIALP);
        writer.writeCharacters(CgmesExportUtil.format(initialP));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeShuntCompensators(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            writeEqShuntCompensator(s.getId(), s.getNameOrId(), s.getSectionCount(), s.getMaximumSectionCount(), s.getModelType(), cimNamespace, writer);
        }
    }

    private static void writeEqShuntCompensator(String id, String shuntCompensatorName, int normalSections, int maximumSections, ShuntCompensatorModelType modelType, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, shuntCompensatorModelClassName(modelType));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(shuntCompensatorName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SHUNTCOMPENSATOR_NORMALSECTIONS);
        writer.writeCharacters(CgmesExportUtil.format(normalSections));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_SHUNTCOMPENSATOR_MAXIMUMSECTIONS);
        writer.writeCharacters(CgmesExportUtil.format(maximumSections));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String shuntCompensatorModelClassName(ShuntCompensatorModelType modelType) {
        if (ShuntCompensatorModelType.LINEAR.equals(modelType)) {
            return "LinearShuntCompensator";
        } else if (ShuntCompensatorModelType.NON_LINEAR.equals(modelType)) {
            return "NonLinearShuntCompensator";
        }
        return "LinearShuntCompensator";
    }

    private static void writeStaticVarCompensators(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            writeEqStaticVarCompensator(svc.getId(), svc.getNameOrId(), 1 / svc.getBmin(), 1 / svc.getBmax(), svc.getExtension(VoltagePerReactivePowerControl.class), svc.getRegulationMode(), svc.getVoltageSetpoint(), cimNamespace, writer);
        }
    }

    private static void writeEqStaticVarCompensator(String id, String svcName, double inductiveRating, double capacitiveRating, VoltagePerReactivePowerControl voltagePerReactivePowerControl, StaticVarCompensator.RegulationMode svcControlMode, double voltageSetPoint, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "StaticVarCompensator");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(svcName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_INDUCTIVERATING);
        writer.writeCharacters(CgmesExportUtil.format(inductiveRating));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_CAPACITIVERATING);
        writer.writeCharacters(CgmesExportUtil.format(capacitiveRating));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_SLOPE);
        writer.writeCharacters(CgmesExportUtil.format(voltagePerReactivePowerControl.getSlope()));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_SVCCONTROLMODE);
        writer.writeCharacters(regulationMode(svcControlMode));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_STATICVARCOMPENSATOR_VOLTAGESETPOINT);
        writer.writeCharacters(CgmesExportUtil.format(voltageSetPoint));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String regulationMode(StaticVarCompensator.RegulationMode svcControlMode) {
        if (StaticVarCompensator.RegulationMode.VOLTAGE.equals(svcControlMode)) {
            return "http://iec.ch/TC57/2013/CIM-schema-cim16#SVCControlMode.voltage";
        } else if (StaticVarCompensator.RegulationMode.REACTIVE_POWER.equals(svcControlMode)) {
            return "http://iec.ch/TC57/2013/CIM-schema-cim16#SVCControlMode.reactivePower";
        }
        return "";
    }

    private static String generatingUnitClassName(EnergySource energySource) {
        if (EnergySource.HYDRO.equals(energySource)) {
            return "HydroGeneratingUnit";
        } else if (EnergySource.NUCLEAR.equals(energySource)) {
            return "NuclearGeneratingUnit";
        } else if (EnergySource.THERMAL.equals(energySource)) {
            return "ThermalGeneratingUnit";
        } else if (EnergySource.WIND.equals(energySource)) {
            return "WindGeneratingUnit";
        } else if (EnergySource.SOLAR.equals(energySource)) {
            return "SolarGeneratingUnit";
        } else if (EnergySource.OTHER.equals(energySource)) {
            return "GeneratingUnit";
        }
        return "GeneratingUnit";
    }

    private static void writeLine(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Line line : network.getLines()) {
            writeEqAcLineSegment(line.getId(), line.getNameOrId(), line.getR(), line.getX(), line.getB1() + line.getB2(), cimNamespace, writer);
        }
    }

    private static void writeEqAcLineSegment(String id, String lineSegmentName, double r, double x, double bch, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "AcLineSegment");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(lineSegmentName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_ACLINESEGMENT_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_ACLINESEGMENT_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_ACLINESEGMENT_BCH);
        writer.writeCharacters(CgmesExportUtil.format(bch));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeTwoWindingsTransformer(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            writeEqPowerTransformer(twt.getId(), twt.getNameOrId(), cimNamespace, writer);
            writeEqPowerTransformerEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_1", twt.getId(), twt.getR(), twt.getX(), twt.getB(), cimNamespace, writer);
            writeEqPowerTransformerEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_2", twt.getId(), 0.0, 0.0, 0.0, cimNamespace, writer);
        }
    }

    private static void writeThreeWindingsTransformer(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            writeEqPowerTransformer(twt.getId(), twt.getNameOrId(), cimNamespace, writer);
            writeEqPowerTransformerEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_1", twt.getId(), twt.getLeg1().getR(), twt.getLeg1().getX(), twt.getLeg1().getB(), cimNamespace, writer);
            writeEqPowerTransformerEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_2", twt.getId(), twt.getLeg2().getR(), twt.getLeg2().getX(), twt.getLeg2().getB(), cimNamespace, writer);
            writeEqPowerTransformerEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_3", twt.getId(), twt.getLeg3().getR(), twt.getLeg3().getX(), twt.getLeg3().getB(), cimNamespace, writer);
        }
    }

    private static void writeEqPowerTransformer(String id, String transformerName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PowerTransformer");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(transformerName);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeEqPowerTransformerEnd(String id, String transformerEndName, String transformerId, double b, double r, double x, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PowerTransformerEnd");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(transformerEndName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TRANSFORMEREND_NAME);
        writer.writeCharacters(transformerEndName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private EquipmentExport() {
    }
}
