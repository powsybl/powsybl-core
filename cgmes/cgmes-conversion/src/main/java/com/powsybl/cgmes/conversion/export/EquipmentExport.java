/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.powsybl.cgmes.model.CgmesNamespace.MD_NAMESPACE;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class EquipmentExport {

    private static final String ALIAS_TYPE_TERMINAL_1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1";
    private static final String ALIAS_TYPE_TERMINAL_2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2";

    private static final String EQ_BASEVOLTAGE_NOMINALV = "BaseVoltage.nominalVoltage";

    private static final String EQ_GENERATINGUNIT_MINP = "GeneratingUnit.minOperatingP";
    private static final String EQ_GENERATINGUNIT_MAXP = "GeneratingUnit.maxOperatingP";
    private static final String EQ_GENERATINGUNIT_INITIALP = "GeneratingUnit.initialP";

    private static final String EQ_SHUNTCOMPENSATOR_NORMALSECTIONS = "ShuntCompensator.normalSections";
    private static final String EQ_SHUNTCOMPENSATOR_MAXIMUMSECTIONS = "ShuntCompensator.maximumSections";
    private static final String EQ_SHUNTCOMPENSATOR_NOMU = "ShuntCompensator.nomU";

    private static final String EQ_LINEARSHUNTCOMPENSATOR_BPERSECTION = "LinearShuntCompensator.bPerSection";

    private static final String EQ_NONLINEARSHUNTCOMPENSATOR_SECTIONNUMBER = "NonlinearShuntCompensatorPoint.sectionNumber";
    private static final String EQ_NONLINEARSHUNTCOMPENSATOR_B = "NonlinearShuntCompensatorPoint.b";
    private static final String EQ_NONLINEARSHUNTCOMPENSATOR_G = "NonlinearShuntCompensatorPoint.g";

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

    private static final String EQ_TRANSFORMEREND_ENDNUMBER = "TransformerEnd.endNumber";
    private static final String EQ_TRANSFORMEREND_TERMINAL = "TransformerEnd.Terminal";

    private static final String EQ_POWERTRANSFORMEREND_POWERTRANSFORMER = "PowerTransformerEnd.PowerTransformer";
    private static final String EQ_POWERTRANSFORMEREND_R = "PowerTransformerEnd.r";
    private static final String EQ_POWERTRANSFORMEREND_X = "PowerTransformerEnd.x";
    private static final String EQ_POWERTRANSFORMEREND_B = "PowerTransformerEnd.b";
    private static final String EQ_POWERTRANSFORMEREND_RATEDU = "PowerTransformerEnd.ratedU";

    private static final String EQ_PHASETAPCHANGER_TRANSFORMEREND = "PhaseTapChanger.TransformerEnd";
    private static final String EQ_PHASETAPCHANGERTABULAR_PHASETAPCHANGERTABLE = "PhaseTapChangerTabular.PhaseTapChangerTable";

    private static final String EQ_RATIOTAPCHANGER_TRANSFORMEREND = "RatioTapChanger.TransformerEnd";
    private static final String EQ_RATIOTAPCHANGER_RATIOTAPCHANGERTABLE = "RatioTapChanger.RatioTapChangerTable";
    private static final String EQ_RATIOTAPCHANGER_SVI = "RatioTapChanger.stepVoltageIncrement";

    private static final String EQ_TAPCHANGER_LOWSTEP = "TapChanger.lowStep";
    private static final String EQ_TAPCHANGER_HIGHSTEP = "TapChanger.highStep";
    private static final String EQ_TAPCHANGER_NORMALSTEP = "TapChanger.normalStep";
    private static final String EQ_TAPCHANGER_NEUTRALSTEP = "TapChanger.neutralStep";
    private static final String EQ_TAPCHANGER_NEUTRALU = "TapChanger.neutralU";
    private static final String EQ_TAPCHANGER_LTCFLAG = "TapChanger.ltcFlag";

    private static final String EQ_TAPCHANGERTABLEPOINT_STEP = "TapChangerTablePoint.step";
    private static final String EQ_TAPCHANGERTABLEPOINT_R = "TapChangerTablePoint.r";
    private static final String EQ_TAPCHANGERTABLEPOINT_X = "TapChangerTablePoint.x";
    private static final String EQ_TAPCHANGERTABLEPOINT_G = "TapChangerTablePoint.g";
    private static final String EQ_TAPCHANGERTABLEPOINT_B = "TapChangerTablePoint.b";
    private static final String EQ_TAPCHANGERTABLEPOINT_RATIO = "TapChangerTablePoint.ratio";

    private static final String EQ_PHASETAPCHANGERTABLEPOINT_ANGLE = "PhaseTapChangerTablePoint.angle";
    private static final String EQ_PHASETAPCHANGERTABLEPOINT_PHASETAPCHANGERTABLE = "PhaseTapChangerTablePoint.PhaseTapChangerTable";

    private static final String EQ_RATIOTAPCHANGERTABLEPOINT_RATIOTAPCHANGERTABLE = "RatioTapChangerTablePoint.RatioTapChangerTable";

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeRdfRoot(context.getCimVersion(), writer);
            String cimNamespace = context.getCimNamespace();

            // TODO fill EQ Model Description
            if (context.getCimVersion() == 16) {
                writeModelDescription(writer, context.getEqModelDescription(), context);
            }

            Map <String, String> exportedNodes = new HashMap<>();
            writeConnectivity(network, exportedNodes, cimNamespace, writer);

            writeSubstations(network, cimNamespace, writer);
            writeVoltageLevels(network, cimNamespace, writer);
            writeLoads(network, exportedNodes, cimNamespace, writer);
            writeGenerators(network, exportedNodes, cimNamespace, writer);
            writeShuntCompensators(network, exportedNodes, cimNamespace, writer);
            writeStaticVarCompensators(network, exportedNodes, cimNamespace, writer);
            writeLines(network, exportedNodes, cimNamespace, writer);
            writeTwoWindingsTransformer(network, exportedNodes, cimNamespace, writer);
            writeThreeWindingsTransformer(network, exportedNodes, cimNamespace, writer);
            writeDanglingLines(network, exportedNodes, cimNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeModelDescription(XMLStreamWriter writer, CgmesExportContext.ModelDescription modelDescription, CgmesExportContext context) throws XMLStreamException {
        writer.writeStartElement(MD_NAMESPACE, "FullModel");
        writer.writeAttribute(RDF_NAMESPACE, "about", "urn:uuid:" + CgmesExportUtil.getUniqueId());
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.SCENARIO_TIME);
        writer.writeCharacters(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(context.getScenarioTime()));
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.CREATED);
        writer.writeCharacters(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(DateTime.now()));
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.DESCRIPTION);
        writer.writeCharacters(modelDescription.getDescription());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.VERSION);
        writer.writeCharacters(CgmesExportUtil.format(modelDescription.getVersion()));
        writer.writeEndElement();
        for (String dependency : modelDescription.getDependencies()) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.DEPENDENT_ON);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, dependency);
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
        writer.writeCharacters(modelDescription.getProfile());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
        writer.writeCharacters(CgmesNamespace.EQ_OPERATION_PROFILE);
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.MODELING_AUTHORITY_SET);
        writer.writeCharacters(modelDescription.getModelingAuthoritySet());
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeConnectivity(Network network, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer)throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                writeNodes(vl, exportedNodes, cimNamespace, writer);
            } else {
                writeBuses(vl, exportedNodes, cimNamespace, writer);
            }
        }
    }

    private static void writeNodes(VoltageLevel vl, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer)throws XMLStreamException {
        for (VoltageLevel.NodeBreakerView.InternalConnection ic : vl.getNodeBreakerView().getInternalConnections()) {
            String node1 = CgmesExportUtil.getUniqueId();
            writeEqConnectivityNode(node1, CgmesExportUtil.format(ic.getNode1()), vl.getId(), cimNamespace, writer);
            exportedNodes.put(vl.getId() + ic.getNode1(), node1);
            String node2 = CgmesExportUtil.getUniqueId();
            writeEqConnectivityNode(node2, CgmesExportUtil.format(ic.getNode2()), vl.getId(), cimNamespace, writer);
            exportedNodes.put(vl.getId() + ic.getNode2(), node2);
            String switchId = CgmesExportUtil.getUniqueId();
            writeEqSwitch(switchId, ic.getNode1() + "_" + ic.getNode2(), SwitchKind.BREAKER, vl.getId(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), switchId, node1, 1, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), switchId, node2, 2, cimNamespace, writer);
        }
    }

    private static void writeBuses(VoltageLevel vl, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer)throws XMLStreamException {
        for (Bus bus : vl.getBusBreakerView().getBuses()) {
            writeEqConnectivityNode(bus.getId(), bus.getNameOrId(), vl.getId(), cimNamespace, writer);
            exportedNodes.put(bus.getId(), bus.getId());
        }
        for (Switch sw : vl.getBusBreakerView().getSwitches()) {
            writeEqSwitch(sw.getId(), sw.getNameOrId(), sw.getKind(), vl.getId(), cimNamespace, writer);
        }
    }

    private static void writeEqConnectivityNode(String id, String nodeName, String connectivityNodeContainerId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "ConnectivityNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(nodeName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "ConnectivityNode.ConnectivityNodeContainer");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + connectivityNodeContainerId);
        writer.writeEndElement();
    }

    private static void writeEqTerminal(String id, String conductingEquipmentId, String connectivityNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, CgmesNames.TERMINAL);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeEmptyElement(cimNamespace, "Terminal.ConductingEquipment");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + conductingEquipmentId);
        writer.writeEmptyElement(cimNamespace, "Terminal.ConnectivityNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + connectivityNodeId);
        writer.writeStartElement(cimNamespace, "ACDCTerminal.sequenceNumber");
        writer.writeCharacters(CgmesExportUtil.format(sequenceNumber));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeSwitches(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            writeEqSwitch(sw.getId(), sw.getNameOrId(), sw.getKind(), sw.getVoltageLevel().getId(), cimNamespace, writer);
        }
    }

    private static void writeEqSwitch(String id, String switchName, SwitchKind switchKind, String equipmentContainer, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, switchClassname(switchKind));
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(switchName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "Equipment.EquipmentContainer");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + equipmentContainer);
        writer.writeEndElement();
    }

    private static String switchClassname(SwitchKind switchKind) {
        switch (switchKind) {
            case BREAKER:
                return "Breaker";
            case DISCONNECTOR:
                return "Disconnector";
            case LOAD_BREAK_SWITCH:
                return "LoadBreakSwitch";
        }
        return "Switch";
    }

    private static void writeSubstations(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Map<String, String> geographicalRegionIds = new HashMap<>();
        Set<String> geographicalTags = new HashSet<>();
        for (Substation substation : network.getSubstations()) {
            String geoName = substation.getCountry().isPresent() ? substation.getCountry().get().getName() : network.getNameOrId();
            if (!geographicalRegionIds.containsKey(geoName)) {
                String subGeographicalRegionId = CgmesExportUtil.getUniqueId();
                String geographicalRegionId = CgmesExportUtil.getUniqueId();
                geographicalRegionIds.put(geoName, subGeographicalRegionId);
                writeEqGeographicalRegion(geographicalRegionId, geoName, cimNamespace, writer);
                writeEqSubGeographicalRegion(subGeographicalRegionId, geoName, geographicalRegionId, cimNamespace, writer);
            }
            writeEqSubstation(substation.getId(), substation.getNameOrId(), geographicalRegionIds.get(geoName), cimNamespace, writer);
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

    private static void writeEqSubGeographicalRegion(String id, String subRegionName, String geographicalRegionId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "SubGeographicalRegion");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(subRegionName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "SubGeographicalRegion.Region");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + geographicalRegionId);
        writer.writeEndElement();
    }

    private static void writeEqSubstation(String id, String substationName, String subGeographicalRegionId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "Substation");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(substationName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "Substation.Region");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + subGeographicalRegionId);
        writer.writeEndElement();
    }

    private static void writeVoltageLevels(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Map<Double, String> baseVoltageIds = new HashMap<>();
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            double nominalV = voltageLevel.getNominalV();
            if (!baseVoltageIds.containsKey(nominalV)) {
                String baseVoltageId = CgmesExportUtil.getUniqueId();
                baseVoltageIds.put(nominalV, baseVoltageId);
                writeEqBaseVoltage(baseVoltageId, nominalV, cimNamespace, writer);
            }
            writeEqVoltageLevel(voltageLevel.getId(), voltageLevel.getNameOrId(), voltageLevel.getSubstation().getId(), baseVoltageIds.get(voltageLevel.getNominalV()), cimNamespace, writer);
        }
    }

    private static void writeEqBaseVoltage(String id, double nominalV, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
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

    private static void writeLoads(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            writeEqEnergyConsumer(load.getId(), load.getNameOrId(), load.getExtension(LoadDetail.class), load.getTerminal().getVoltageLevel().getId(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), load.getId(), connectivityNodeId(exportedNodes, load.getTerminal()), 1, cimNamespace, writer);
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

    private static void writeGenerators(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Generator generator : network.getGenerators()) {
            String generatingUnit = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            if (generatingUnit == null) {
                generatingUnit = CgmesExportUtil.getUniqueId();
            }
            writeEqSynchronousMachine(generator.getId(), generator.getNameOrId(), generatingUnit, cimNamespace, writer);
            String generatingUnitName = "GEN_" + generator.getNameOrId();
            writeEqGeneratingUnit(generatingUnit, generatingUnitName, generator.getEnergySource(), generator.getMinP(), generator.getMaxP(), generator.getTargetP(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), generator.getId(), connectivityNodeId(exportedNodes, generator.getTerminal()), 1, cimNamespace, writer);
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

    private static void writeShuntCompensators(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            double bPerSection = 0.0;
            if (s.getModelType().equals(ShuntCompensatorModelType.LINEAR)) {
                bPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getBPerSection();
            }
            writeEqShuntCompensator(s.getId(), s.getNameOrId(), s.getSectionCount(), s.getMaximumSectionCount(), s.getTerminal().getVoltageLevel().getNominalV(), s.getModelType(), bPerSection, cimNamespace, writer);
            if (s.getModelType().equals(ShuntCompensatorModelType.NON_LINEAR)) {
                for (int section = 1; section <= s.getMaximumSectionCount(); section++) {
                    writeEqNonlinearShuntCompensatorPoint(CgmesExportUtil.getUniqueId(), s.getId(), section, s.getB(section), s.getG(section), cimNamespace, writer);
                }
            }
            writeEqTerminal(CgmesExportUtil.getUniqueId(), s.getId(), connectivityNodeId(exportedNodes, s.getTerminal()), 1, cimNamespace, writer);
        }
    }

    private static void writeEqShuntCompensator(String id, String shuntCompensatorName, int normalSections, int maximumSections, double nomU, ShuntCompensatorModelType modelType, double bPerSection, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
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
        writer.writeStartElement(cimNamespace, EQ_SHUNTCOMPENSATOR_NOMU);
        writer.writeCharacters(CgmesExportUtil.format(nomU));
        writer.writeEndElement();
        if (modelType.equals(ShuntCompensatorModelType.LINEAR)) {
            writer.writeStartElement(cimNamespace, EQ_LINEARSHUNTCOMPENSATOR_BPERSECTION);
            writer.writeCharacters(CgmesExportUtil.format(bPerSection));
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private static void writeEqNonlinearShuntCompensatorPoint(String id, String shuntId, int sectionNumber, double b, double g, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "NonlinearShuntCompensatorPoint");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeEmptyElement(cimNamespace, "NonlinearShuntCompensatorPoint.NonlinearShuntCompensator");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + shuntId);
        writer.writeStartElement(cimNamespace, EQ_NONLINEARSHUNTCOMPENSATOR_SECTIONNUMBER);
        writer.writeCharacters(CgmesExportUtil.format(sectionNumber));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_NONLINEARSHUNTCOMPENSATOR_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_NONLINEARSHUNTCOMPENSATOR_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String shuntCompensatorModelClassName(ShuntCompensatorModelType modelType) {
        if (ShuntCompensatorModelType.LINEAR.equals(modelType)) {
            return "LinearShuntCompensator";
        } else if (ShuntCompensatorModelType.NON_LINEAR.equals(modelType)) {
            return "NonlinearShuntCompensator";
        }
        return "LinearShuntCompensator";
    }

    private static void writeStaticVarCompensators(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            writeEqStaticVarCompensator(svc.getId(), svc.getNameOrId(), 1 / svc.getBmin(), 1 / svc.getBmax(), svc.getExtension(VoltagePerReactivePowerControl.class), svc.getRegulationMode(), svc.getVoltageSetpoint(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), svc.getId(), connectivityNodeId(exportedNodes, svc.getTerminal()), 1, cimNamespace, writer);
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

    private static void writeLines(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Line line : network.getLines()) {
            writeEqAcLineSegment(line.getId(), line.getNameOrId(), line.getR(), line.getX(), line.getB1() + line.getB2(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), line.getId(), connectivityNodeId(exportedNodes, line.getTerminal1()), 1, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), line.getId(), connectivityNodeId(exportedNodes, line.getTerminal2()), 2, cimNamespace, writer);
        }
    }

    private static void writeEqAcLineSegment(String id, String lineSegmentName, double r, double x, double bch, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "ACLineSegment");
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

    private static void writeTwoWindingsTransformer(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            writeEqPowerTransformer(twt.getId(), twt.getNameOrId(), cimNamespace, writer);
            String end1Id = CgmesExportUtil.getUniqueId();
            String terminalId1 = CgmesExportUtil.getUniqueId();
            writeEqPowerTransformerEnd(end1Id, twt.getNameOrId() + "_1", twt.getId(), 1, twt.getR(), twt.getX(), twt.getB(), twt.getRatedU1(), terminalId1, cimNamespace, writer);
            String terminalId2 = CgmesExportUtil.getUniqueId();
            writeEqPowerTransformerEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_2", twt.getId(), 2, 0.0, 0.0, 0.0, twt.getRatedU2(), terminalId2, cimNamespace, writer);
            writeEqTerminal(terminalId1, twt.getId(), connectivityNodeId(exportedNodes, twt.getTerminal1()), 1, cimNamespace, writer);
            writeEqTerminal(terminalId2, twt.getId(), connectivityNodeId(exportedNodes, twt.getTerminal2()), 2, cimNamespace, writer);
            writePhaseTapChanger(twt.getPhaseTapChanger(), twt.getNameOrId(), end1Id, twt.getTerminal1().getVoltageLevel().getNominalV(), cimNamespace, writer);
            writeRatioTapChanger(twt.getRatioTapChanger(), twt.getNameOrId(), end1Id, cimNamespace, writer);
        }
    }

    private static void writeThreeWindingsTransformer(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            writeEqPowerTransformer(twt.getId(), twt.getNameOrId(), cimNamespace, writer);
            String terminalId1 = CgmesExportUtil.getUniqueId();
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_1", CgmesExportUtil.getUniqueId(), 1, twt.getLeg1(), terminalId1, cimNamespace, writer);
            String terminalId2 = CgmesExportUtil.getUniqueId();
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_2", CgmesExportUtil.getUniqueId(), 2, twt.getLeg2(), terminalId2, cimNamespace, writer);
            String terminalId3 = CgmesExportUtil.getUniqueId();
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_3", CgmesExportUtil.getUniqueId(), 3, twt.getLeg3(), terminalId3, cimNamespace, writer);
            writeEqTerminal(terminalId1, twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg1().getTerminal()), 1, cimNamespace, writer);
            writeEqTerminal(terminalId2, twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg2().getTerminal()), 2, cimNamespace, writer);
            writeEqTerminal(terminalId3, twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg3().getTerminal()), 3, cimNamespace, writer);
        }
    }

    private static void writeThreeWindingsTransformerEnd(String twtId, String twtName, String endId, int endNumber, ThreeWindingsTransformer.Leg leg, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writeEqPowerTransformerEnd(endId, twtName, twtId, endNumber, leg.getR(), leg.getX(), leg.getB(), leg.getRatedU(), terminalId, cimNamespace, writer);
        writePhaseTapChanger(leg.getPhaseTapChanger(), twtName, endId, leg.getTerminal().getVoltageLevel().getNominalV(), cimNamespace, writer);
        writeRatioTapChanger(leg.getRatioTapChanger(), twtName, endId, cimNamespace, writer);
    }

    private static void writePhaseTapChanger(PhaseTapChanger ptc, String twtName, String endId, double neutralU, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (ptc != null) {
            int neutralStep = getPhaseTapChangerNeutralStep(ptc);
            String phaseTapChangerTableId = CgmesExportUtil.getUniqueId();
            writeEqPhaseTapChanger(CgmesExportUtil.getUniqueId(), twtName + "_PTC", endId, ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, ptc.getTapPosition(), neutralU, false, phaseTapChangerTableId, cimNamespace, writer);
            writeEqPhaseTapChangerTable(phaseTapChangerTableId, twtName + "_TABLE", cimNamespace, writer);
            for (Map.Entry<Integer, PhaseTapChangerStep> step : ptc.getAllSteps().entrySet()) {
                writeEqPhaseTapChangerTablePoint(CgmesExportUtil.getUniqueId(), phaseTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), - step.getValue().getAlpha(), step.getKey(), cimNamespace, writer);
            }
        }
    }

    private static void writeRatioTapChanger(RatioTapChanger rtc, String twtName, String endId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (rtc != null) {
            int neutralStep = getRatioTapChangerNeutralStep(rtc);
            double stepVoltageIncrement = 100.0 * (1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0) / (rtc.getLowTapPosition() - neutralStep);
            String ratioTapChangerTableId = CgmesExportUtil.getUniqueId();
            writeEqRatioTapChanger(CgmesExportUtil.getUniqueId(), twtName + "_RTC", endId, rtc.getLowTapPosition(), rtc.getHighTapPosition(), neutralStep, rtc.getTapPosition(), rtc.getTargetV(), rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement, ratioTapChangerTableId, cimNamespace, writer);
            writeEqRatioTapChangerTable(ratioTapChangerTableId, twtName + "_TABLE", cimNamespace, writer);
            for (Map.Entry<Integer, RatioTapChangerStep> step : rtc.getAllSteps().entrySet()) {
                writeEqRatioTapChangerTablePoint(CgmesExportUtil.getUniqueId(), ratioTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), step.getKey(), cimNamespace, writer);
            }
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

    private static void writeEqPowerTransformerEnd(String id, String transformerEndName, String transformerId, int endNumber, double b, double r, double x, double ratedU, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PowerTransformerEnd");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(transformerEndName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TRANSFORMEREND_ENDNUMBER);
        writer.writeCharacters(CgmesExportUtil.format(endNumber));
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
        writer.writeStartElement(cimNamespace, EQ_POWERTRANSFORMEREND_RATEDU);
        writer.writeCharacters(CgmesExportUtil.format(ratedU));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_POWERTRANSFORMEREND_POWERTRANSFORMER);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + transformerId);
        writer.writeEmptyElement(cimNamespace, EQ_TRANSFORMEREND_TERMINAL);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + terminalId);
        writer.writeEndElement();
    }

    private static void writeEqPhaseTapChanger(String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep, double neutralU, boolean ltcFlag, String phaseTapChangerTableId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PhaseTapChangerTabular");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(tapChangerName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_PHASETAPCHANGER_TRANSFORMEREND);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + transformerEndId);
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_LOWSTEP);
        writer.writeCharacters(CgmesExportUtil.format(lowStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_HIGHSTEP);
        writer.writeCharacters(CgmesExportUtil.format(highStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NORMALSTEP);
        writer.writeCharacters(CgmesExportUtil.format(normalStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NEUTRALSTEP);
        writer.writeCharacters(CgmesExportUtil.format(neutralStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NEUTRALU);
        writer.writeCharacters(CgmesExportUtil.format(neutralU));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_LTCFLAG);
        writer.writeCharacters(CgmesExportUtil.format(ltcFlag));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_PHASETAPCHANGERTABULAR_PHASETAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + phaseTapChangerTableId);
        writer.writeEndElement();
    }

    private static void writeEqPhaseTapChangerTable(String id, String phaseTapChangerTableName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PhaseTapChangerTable");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(phaseTapChangerTableName);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeEqPhaseTapChangerTablePoint(String id, String phaseTapChangerTableId, double r, double x, double g, double b, double ratio, double angle, Integer step, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PhaseTapChangerTablePoint");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_STEP);
        writer.writeCharacters(CgmesExportUtil.format(step));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_RATIO);
        writer.writeCharacters(CgmesExportUtil.format(ratio));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_PHASETAPCHANGERTABLEPOINT_ANGLE);
        writer.writeCharacters(CgmesExportUtil.format(angle));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_PHASETAPCHANGERTABLEPOINT_PHASETAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + phaseTapChangerTableId);
        writer.writeEndElement();
    }

    private static int getPhaseTapChangerNeutralStep(PhaseTapChanger ptc) {
        int neutralStep = ptc.getTapPosition();
        while(ptc.getStep(neutralStep).getAlpha() != 0.0) {
            if (ptc.getStep(neutralStep).getAlpha() > 0.0) {
                neutralStep ++;
            } else {
                neutralStep --;
            }
        }
        return neutralStep;
    }

    private static void writeEqRatioTapChanger(String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep, double neutralU, boolean ltcFlag, double stepVoltageIncrement, String ratioTapChangerTableId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "RatioTapChanger");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(tapChangerName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_RATIOTAPCHANGER_TRANSFORMEREND);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + transformerEndId);
        writer.writeStartElement(cimNamespace, EQ_RATIOTAPCHANGER_SVI);
        writer.writeCharacters(CgmesExportUtil.format(stepVoltageIncrement));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_LOWSTEP);
        writer.writeCharacters(CgmesExportUtil.format(lowStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_HIGHSTEP);
        writer.writeCharacters(CgmesExportUtil.format(highStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NORMALSTEP);
        writer.writeCharacters(CgmesExportUtil.format(normalStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NEUTRALSTEP);
        writer.writeCharacters(CgmesExportUtil.format(neutralStep));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_NEUTRALU);
        writer.writeCharacters(CgmesExportUtil.format(neutralU));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGER_LTCFLAG);
        writer.writeCharacters(CgmesExportUtil.format(ltcFlag));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_RATIOTAPCHANGER_RATIOTAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + ratioTapChangerTableId);
        writer.writeEndElement();
    }

    private static void writeEqRatioTapChangerTable(String id, String ratioTapChangerTableName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "RatioTapChangerTable");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(ratioTapChangerTableName);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeEqRatioTapChangerTablePoint(String id, String ratioTapChangerTableId, double r, double x, double g, double b, double ratio, Integer step, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "RatioTapChangerTablePoint");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_R);
        writer.writeCharacters(CgmesExportUtil.format(r));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_X);
        writer.writeCharacters(CgmesExportUtil.format(x));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_G);
        writer.writeCharacters(CgmesExportUtil.format(g));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_B);
        writer.writeCharacters(CgmesExportUtil.format(b));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_STEP);
        writer.writeCharacters(CgmesExportUtil.format(step));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_TAPCHANGERTABLEPOINT_RATIO);
        writer.writeCharacters(CgmesExportUtil.format(ratio));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, EQ_RATIOTAPCHANGERTABLEPOINT_RATIOTAPCHANGERTABLE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + ratioTapChangerTableId);
        writer.writeEndElement();
    }

    private static int getRatioTapChangerNeutralStep(RatioTapChanger rtc) {
        int neutralStep = rtc.getTapPosition();
        while(rtc.getStep(neutralStep).getRho() != 1.0) {
            if (rtc.getStep(neutralStep).getRho() > 1.0) {
                neutralStep --;
            } else {
                neutralStep ++;
            }
        }
        return neutralStep;
    }

    private static void writeDanglingLines(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (DanglingLine line : network.getDanglingLines()) {
            // New Substation
            String geographicalRegionId = CgmesExportUtil.getUniqueId();
            writeEqGeographicalRegion(geographicalRegionId, line.getNameOrId() + "_GR", cimNamespace, writer);
            String subGeographicalRegionId = CgmesExportUtil.getUniqueId();
            writeEqSubGeographicalRegion(subGeographicalRegionId, line.getNameOrId() + "_SGR", geographicalRegionId, cimNamespace, writer);
            String substationId = CgmesExportUtil.getUniqueId();
            writeEqSubstation(substationId, line.getNameOrId() + "_SUBSTATION", subGeographicalRegionId, cimNamespace, writer);
            // New VoltageLevel
            String baseVoltageId = CgmesExportUtil.getUniqueId();
            writeEqBaseVoltage(baseVoltageId, line.getTerminal().getVoltageLevel().getNominalV(), cimNamespace, writer);
            String voltageLevelId = CgmesExportUtil.getUniqueId();
            writeEqVoltageLevel(voltageLevelId, line.getNameOrId() + "_VL", substationId, baseVoltageId, cimNamespace, writer);
            // New ConnectivityNode
            String connectivityNodeId = CgmesExportUtil.getUniqueId();
            writeEqConnectivityNode(connectivityNodeId, line.getNameOrId() + "_NODE", voltageLevelId, cimNamespace, writer);
            // New Load
            String loadId = CgmesExportUtil.getUniqueId();
            writeEqEnergyConsumer(loadId, line.getNameOrId() + "_LOAD", null, voltageLevelId, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), loadId, connectivityNodeId, 1, cimNamespace, writer);
            // New Generator if necessary
            if (line.getGeneration() != null) {
                String generatingUnit = CgmesExportUtil.getUniqueId();
                String generatingUnitName = "GEN_" + line.getNameOrId();
                writeEqGeneratingUnit(generatingUnit, generatingUnitName, EnergySource.OTHER, line.getGeneration().getMinP(), line.getGeneration().getMaxP(), line.getGeneration().getTargetP(), cimNamespace, writer);
                String generatorId = CgmesExportUtil.getUniqueId();
                writeEqSynchronousMachine(generatorId, line.getNameOrId() + "_GEN", generatingUnit, cimNamespace, writer);
                writeEqTerminal(CgmesExportUtil.getUniqueId(), generatorId, connectivityNodeId, 1, cimNamespace, writer);
            }
            writeEqAcLineSegment(line.getId(), line.getNameOrId(), line.getR(), line.getX(), line.getB(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), line.getId(), connectivityNodeId(exportedNodes, line.getTerminal()), 1, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), line.getId(), connectivityNodeId, 2, cimNamespace, writer);
        }
    }

    private static String connectivityNodeId(Map<String, String> exportedNodes, Terminal terminal) {
        String key;
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            key = terminal.getVoltageLevel().getId() + terminal.getNodeBreakerView().getNode();
        } else {
            key = terminal.getBusBreakerView().getBus().getId();
        }
        return exportedNodes.get(key);
    }

    private EquipmentExport() {
    }
}
