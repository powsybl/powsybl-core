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

    private static final String EQ_PHASETAPCHANGER_TRANSFORMEREND = "PhaseTapChanger.TransformerEnd";

    private static final String EQ_RATIOTAPCHANGER_TRANSFORMEREND = "RatioTapChanger.TransformerEnd";
    private static final String EQ_RATIOTAPCHANGER_SVI = "RatioTapChanger.stepVoltageIncrement";

    private static final String EQ_TAPCHANGER_LOWSTEP = "TapChanger.lowStep";
    private static final String EQ_TAPCHANGER_HIGHSTEP = "TapChanger.highStep";
    private static final String EQ_TAPCHANGER_NORMALSTEP = "TapChanger.normalStep";
    private static final String EQ_TAPCHANGER_NEUTRALSTEP = "TapChanger.neutralStep";
    private static final String EQ_TAPCHANGER_NEUTRALU = "TapChanger.neutralU";
    private static final String EQ_TAPCHANGER_LTCFLAG = "TapChanger.ltcFlag";

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
            writeLine(network, exportedNodes, cimNamespace, writer);
            writeTwoWindingsTransformer(network, exportedNodes, cimNamespace, writer);
            writeThreeWindingsTransformer(network, exportedNodes, cimNamespace, writer);

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
        writer.writeStartElement(cimNamespace, "ACDCTerminal.sequenceNumbe");
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

    private static void writeShuntCompensators(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            writeEqShuntCompensator(s.getId(), s.getNameOrId(), s.getSectionCount(), s.getMaximumSectionCount(), s.getModelType(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), s.getId(), connectivityNodeId(exportedNodes, s.getTerminal()), 1, cimNamespace, writer);
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

    private static void writeLine(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
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
            writeEqPowerTransformerEnd(end1Id, twt.getNameOrId() + "_1", twt.getId(), twt.getR(), twt.getX(), twt.getB(), cimNamespace, writer);
            writeEqPowerTransformerEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_2", twt.getId(), 0.0, 0.0, 0.0, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getTerminal1()), 1, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getTerminal2()), 2, cimNamespace, writer);
            PhaseTapChanger ptc = twt.getPhaseTapChanger();
            if (ptc != null) {
                int neutralStep = ptc.getTapPosition();
                while(ptc.getStep(neutralStep).getAlpha() != 0.0) {
                    if (ptc.getStep(neutralStep).getAlpha() > 0.0) {
                        neutralStep ++;
                    } else {
                        neutralStep --;
                    }
                }
                writeEqPhaseTapChanger(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_PTC", end1Id, ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, ptc.getTapPosition(), twt.getTerminal1().getVoltageLevel().getNominalV(), false, cimNamespace, writer);
            }
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null) {
                int neutralStep = rtc.getTapPosition();
                while(rtc.getStep(neutralStep).getRho() != 1.0) {
                    if (rtc.getStep(neutralStep).getRho() > 1.0) {
                        neutralStep --;
                    } else {
                        neutralStep ++;
                    }
                }
                double stepVoltageIncrement = 100.0 * (rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0) / (rtc.getLowTapPosition() - neutralStep);
                writeEqRatioTapChanger(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_RTC", end1Id, rtc.getLowTapPosition(), rtc.getHighTapPosition(), neutralStep, rtc.getTapPosition(), rtc.getTargetV(), rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement, cimNamespace, writer);
            }
        }
    }

    private static void writeThreeWindingsTransformer(Network network, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            writeEqPowerTransformer(twt.getId(), twt.getNameOrId(), cimNamespace, writer);
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_1", CgmesExportUtil.getUniqueId(), twt.getLeg1(), cimNamespace, writer);
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_2", CgmesExportUtil.getUniqueId(), twt.getLeg2(), cimNamespace, writer);
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_3", CgmesExportUtil.getUniqueId(), twt.getLeg3(), cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg1().getTerminal()), 1, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg2().getTerminal()), 2, cimNamespace, writer);
            writeEqTerminal(CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg3().getTerminal()), 3, cimNamespace, writer);
        }
    }

    private static void writeThreeWindingsTransformerEnd(String twtId, String twtName, String endId, ThreeWindingsTransformer.Leg leg, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writeEqPowerTransformerEnd(endId, twtName, twtId, leg.getR(), leg.getX(), leg.getB(), cimNamespace, writer);
        PhaseTapChanger ptc = leg.getPhaseTapChanger();
        if (ptc != null) {
            int neutralStep = ptc.getTapPosition();
            while(ptc.getStep(neutralStep).getAlpha() != 0.0) {
                if (ptc.getStep(neutralStep).getAlpha() > 0.0) {
                    neutralStep ++;
                } else {
                    neutralStep --;
                }
            }
            writeEqPhaseTapChanger(CgmesExportUtil.getUniqueId(), twtName + "_PTC", endId, ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, ptc.getTapPosition(), leg.getTerminal().getVoltageLevel().getNominalV(), false, cimNamespace, writer);
        }
        RatioTapChanger rtc = leg.getRatioTapChanger();
        if (rtc != null) {
            int neutralStep = rtc.getTapPosition();
            while(rtc.getStep(neutralStep).getRho() != 1.0) {
                if (rtc.getStep(neutralStep).getRho() > 1.0) {
                    neutralStep --;
                } else {
                    neutralStep ++;
                }
            }
            double stepVoltageIncrement = 100.0 * (1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0) / (rtc.getLowTapPosition() - neutralStep);
            writeEqRatioTapChanger(CgmesExportUtil.getUniqueId(), twtName + "_RTC", endId, rtc.getLowTapPosition(), rtc.getHighTapPosition(), neutralStep, rtc.getTapPosition(), rtc.getTargetV(), rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement, cimNamespace, writer);
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

    private static void writeEqPhaseTapChanger(String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep, double neutralU, boolean ltcFlag, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "PhaseTapChanger");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(tapChangerName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_PHASETAPCHANGER_TRANSFORMEREND);
        writer.writeCharacters(transformerEndId);
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
        writer.writeEndElement();
    }

    private static void writeEqRatioTapChanger(String id, String tapChangerName, String transformerEndId, double lowStep, double highStep, double neutralStep, double normalStep, double neutralU, boolean ltcFlag, double stepVoltageIncrement, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "RatioTapChanger");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, id);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(tapChangerName);
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, EQ_RATIOTAPCHANGER_TRANSFORMEREND);
        writer.writeCharacters(transformerEndId);
        writer.writeEndElement();
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
        writer.writeEndElement();
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
