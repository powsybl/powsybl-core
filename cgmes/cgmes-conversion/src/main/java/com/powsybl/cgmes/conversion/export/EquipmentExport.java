/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.conversion.export.elements.*;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.export.CgmesExportUtil.obtainSynchronousMachineKind;
import static com.powsybl.cgmes.conversion.export.elements.LoadingLimitEq.loadingLimitClassName;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class EquipmentExport {

    private static final String AC_DC_CONVERTER_DC_TERMINAL = "ACDCConverterDCTerminal";
    private static final String TERMINAL_BOUNDARY = "Terminal_Boundary";
    private static final Logger LOG = LoggerFactory.getLogger(EquipmentExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        CgmesMetadataModel model = CgmesExport.initializeModelForExport(
                network, CgmesSubset.EQUIPMENT, context, true, false);
        write(network, writer, context, model);
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context, CgmesMetadataModel model) {
        try {
            boolean writeConnectivityNodes = context.writeConnectivityNodes();

            String cimNamespace = context.getCim().getNamespace();
            String euNamespace = context.getCim().getEuNamespace();
            String limitValueAttributeName = context.getCim().getLimitValueAttributeName();
            String limitTypeAttributeName = context.getCim().getLimitTypeAttributeName();
            String limitKindClassName = context.getCim().getLimitKindClassName();
            boolean writeInfiniteDuration = context.getCim().writeLimitInfiniteDuration();
            boolean writeInitialP = context.getCim().writeGeneratingUnitInitialP();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), euNamespace, writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(network, CgmesSubset.EQUIPMENT, writer, model, context);
            }

            Map<String, String> mapNodeKey2NodeId = new HashMap<>();
            Map<Terminal, String> mapTerminal2Id = new HashMap<>();
            Set<String> regulatingControlsWritten = new HashSet<>();
            Set<Double> exportedBaseVoltagesByNominalV = new HashSet<>();
            Set<String> exportedLimitTypes = new HashSet<>();
            LoadGroups loadGroups = new LoadGroups();

            if (writeConnectivityNodes) {
                writeConnectivityNodes(network, mapNodeKey2NodeId, cimNamespace, writer, context);
            }
            writeTerminals(network, mapTerminal2Id, mapNodeKey2NodeId, cimNamespace, writer, context);
            writeSwitches(network, cimNamespace, writer, context);

            writeSubstations(network, cimNamespace, writer, context);
            writeVoltageLevels(network, cimNamespace, writer, context, exportedBaseVoltagesByNominalV);
            writeBusbarSections(network, cimNamespace, writer, context);
            writeLoads(network, loadGroups, cimNamespace, writer, context);
            String loadAreaId = writeLoadGroups(network, loadGroups.found(), cimNamespace, writer, context);
            writeGenerators(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, writeInitialP, writer, context);
            writeBatteries(network, cimNamespace, writeInitialP, writer, context);
            writeShuntCompensators(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, writer, context);
            writeStaticVarCompensators(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, writer, context);
            writeLines(network, mapTerminal2Id, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
            writeTwoWindingsTransformers(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
            writeThreeWindingsTransformers(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);

            writeDanglingLines(network, mapTerminal2Id, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context, exportedBaseVoltagesByNominalV);
            writeHvdcLines(network, mapTerminal2Id, mapNodeKey2NodeId, cimNamespace, writer, context);

            writeControlAreas(loadAreaId, network, cimNamespace, euNamespace, writer, context);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeConnectivityNodes(Network network, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            String cgmesVlId = context.getNamingStrategy().getCgmesId(vl);
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                writeNodes(vl, cgmesVlId, new VoltageLevelAdjacency(vl, context), mapNodeKey2NodeId, cimNamespace, writer, context);
            } else {
                writeBuses(vl, cgmesVlId, mapNodeKey2NodeId, cimNamespace, writer, context);
            }
            writeSwitchesConnectivity(vl, cgmesVlId, mapNodeKey2NodeId, cimNamespace, writer, context);
        }
        writeBusbarSectionsConnectivity(network, mapNodeKey2NodeId, cimNamespace, writer, context);
    }

    private static void writeSwitchesConnectivity(VoltageLevel vl, String cgmesVlId, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        String[] nodeKeys = new String[2];
        for (Switch sw : vl.getSwitches()) {
            fillSwitchNodeKeys(vl, sw, nodeKeys);
            // We have to go through all switches, even if they are not exported as equipment,
            // to be sure that all required mappings between IIDM node number and CGMES Connectivity Node are created
            writeSwitchConnectivity(nodeKeys[0], vl, cgmesVlId, mapNodeKey2NodeId, cimNamespace, writer, context);
            writeSwitchConnectivity(nodeKeys[1], vl, cgmesVlId, mapNodeKey2NodeId, cimNamespace, writer, context);
        }
    }

    private static void writeSwitchConnectivity(String nodeKey, VoltageLevel vl, String cgmesVlId, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        mapNodeKey2NodeId.computeIfAbsent(nodeKey, k -> {
            try {
                String node = context.getNamingStrategy().getCgmesId(refTyped(vl), ref(nodeKey), CONNECTIVITY_NODE);
                ConnectivityNodeEq.write(node, nodeKey, cgmesVlId, cimNamespace, writer, context);
                return node;
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    private static String buildNodeKey(VoltageLevel vl, int node) {
        return vl.getId() + "_" + node;
    }

    private static String buildNodeKey(Bus bus) {
        return bus.getId();
    }

    private static void writeBusbarSectionsConnectivity(Network network, Map <String, String> mapNodeKey2NodeId, String cimNamespace,
                                                        XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (BusbarSection bus : network.getBusbarSections()) {
            String connectivityNodeId = connectivityNodeId(mapNodeKey2NodeId, bus.getTerminal());
            if (connectivityNodeId == null) {
                VoltageLevel vl = bus.getTerminal().getVoltageLevel();
                String node = context.getNamingStrategy().getCgmesId(refTyped(bus), CONNECTIVITY_NODE);
                ConnectivityNodeEq.write(node, bus.getNameOrId(), context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer, context);
                String key = buildNodeKey(vl, bus.getTerminal().getNodeBreakerView().getNode());
                mapNodeKey2NodeId.put(key, node);
            }
        }
    }

    private static void writeNodes(VoltageLevel vl, String cgmesVlId, VoltageLevelAdjacency vlAdjacencies, Map <String, String> mapNodeKey2NodeId, String cimNamespace,
                                   XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (List<Integer> nodes : vlAdjacencies.getNodes()) {
            String cgmesNodeId = context.getNamingStrategy().getCgmesId(refTyped(vl), ref(nodes.get(0)), CONNECTIVITY_NODE);
            ConnectivityNodeEq.write(cgmesNodeId, CgmesExportUtil.format(nodes.get(0)), cgmesVlId, cimNamespace, writer, context);
            for (Integer nodeNumber : nodes) {
                mapNodeKey2NodeId.put(buildNodeKey(vl, nodeNumber), cgmesNodeId);
            }
        }
    }

    private static void writeBuses(VoltageLevel vl, String cgmesVlId, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus bus : vl.getBusBreakerView().getBuses()) {
            String cgmesNodeId = context.getNamingStrategy().getCgmesId(refTyped(bus), Part.CONNECTIVITY_NODE);
            ConnectivityNodeEq.write(cgmesNodeId, bus.getNameOrId(), cgmesVlId, cimNamespace, writer, context);
            mapNodeKey2NodeId.put(buildNodeKey(bus), cgmesNodeId);
        }
    }

    private static void writeSwitches(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                VoltageLevel vl = sw.getVoltageLevel();
                String switchType = sw.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "switchType"); // may be null
                // To ensure we do not violate rule SwitchTN1 of ENTSO-E QoCDC,
                // we only export as retained a switch if it will be exported with different TNs at both ends
                boolean exportAsRetained = sw.isRetained() && hasDifferentTNsAtBothEnds(sw);
                SwitchEq.write(context.getNamingStrategy().getCgmesId(sw), sw.getNameOrId(), switchType, sw.getKind(), context.getNamingStrategy().getCgmesId(vl), sw.isOpen(), exportAsRetained, cimNamespace, writer, context);
            }
        }
    }

    private static boolean hasDifferentTNsAtBothEnds(Switch sw) {
        // The exported Topological Nodes come from IIDM bus/breaker view buses
        Bus bus1 = sw.getVoltageLevel().getBusBreakerView().getBus1(sw.getId());
        Bus bus2 = sw.getVoltageLevel().getBusBreakerView().getBus2(sw.getId());
        return bus1 != bus2;
    }

    private static void fillSwitchNodeKeys(VoltageLevel vl, Switch sw, String[] nodeKeys) {
        if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            nodeKeys[0] = buildNodeKey(vl, vl.getNodeBreakerView().getNode1(sw.getId()));
            nodeKeys[1] = buildNodeKey(vl, vl.getNodeBreakerView().getNode2(sw.getId()));
        } else {
            nodeKeys[0] = buildNodeKey(vl.getBusBreakerView().getBus1(sw.getId()));
            nodeKeys[1] = buildNodeKey(vl.getBusBreakerView().getBus2(sw.getId()));
        }
    }

    private static void writeSubstations(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (String geographicalRegionId : context.getRegionsIds()) {
            // To ensure we always export valid mRIDs, even if input CGMES used invalid ones
            String cgmesRegionId = context.getNamingStrategy().getCgmesId(geographicalRegionId);
            writeGeographicalRegion(cgmesRegionId, context.getRegionName(geographicalRegionId), cimNamespace, writer, context);
        }
        List<String> writtenSubRegions = new ArrayList<>();
        for (Substation substation : network.getSubstations()) {
            String subGeographicalRegionId = context.getNamingStrategy().getCgmesIdFromProperty(substation, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId");
            String geographicalRegionId = context.getNamingStrategy().getCgmesIdFromProperty(substation, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId");
            if (!writtenSubRegions.contains(subGeographicalRegionId)) {
                writeSubGeographicalRegion(subGeographicalRegionId,
                        Optional.ofNullable(context.getSubRegionName(subGeographicalRegionId)).orElse("N/A"), // FIXME sub-regions can be non-unique (same name for different IDs)
                        geographicalRegionId, cimNamespace, writer, context);
                writtenSubRegions.add(subGeographicalRegionId);
            }
            SubstationEq.write(context.getNamingStrategy().getCgmesId(substation), substation.getNameOrId(), subGeographicalRegionId, cimNamespace, writer, context);
        }
    }

    private static void writeGeographicalRegion(String geographicalRegionId, String geoName, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            GeographicalRegionEq.write(geographicalRegionId, geoName, cimNamespace, writer, context);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSubGeographicalRegion(String subGeographicalRegionId, String subGeographicalRegionName, String geographicalRegionId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            SubGeographicalRegionEq.write(subGeographicalRegionId, subGeographicalRegionName, geographicalRegionId, cimNamespace, writer, context);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeVoltageLevels(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context, Set<Double> exportedBaseVoltagesByNominalV) throws XMLStreamException {
        String fictSubstationId = null;
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            double nominalV = voltageLevel.getNominalV();
            BaseVoltageMapping.BaseVoltageSource baseVoltage = context.getBaseVoltageByNominalVoltage(nominalV);
            if (!exportedBaseVoltagesByNominalV.contains(nominalV) && baseVoltage.getSource().equals(Source.IGM)) {
                BaseVoltageEq.write(baseVoltage.getId(), nominalV, cimNamespace, writer, context);
                exportedBaseVoltagesByNominalV.add(nominalV);
            }
            Optional<String> substationId = voltageLevel.getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s));
            if (substationId.isEmpty() && fictSubstationId == null) {
                // create a new fictitious substation inside this network
                fictSubstationId = writeFictitiousSubstationFor(network, cimNamespace, writer, context);
            }
            VoltageLevelEq.write(context.getNamingStrategy().getCgmesId(voltageLevel), voltageLevel.getNameOrId(), voltageLevel.getLowVoltageLimit(), voltageLevel.getHighVoltageLimit(),
                    substationId.orElse(fictSubstationId), baseVoltage.getId(), cimNamespace, writer, context);
        }
    }

    private static void writeBusbarSections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (BusbarSection bbs : network.getBusbarSections()) {
            BusbarSectionEq.write(context.getNamingStrategy().getCgmesId(bbs), bbs.getNameOrId(),
                    context.getNamingStrategy().getCgmesId(bbs.getTerminal().getVoltageLevel()),
                    context.getBaseVoltageByNominalVoltage(bbs.getTerminal().getVoltageLevel().getNominalV()).getId(), cimNamespace, writer, context);
        }
    }

    // We may receive a warning if we define an empty load group,
    // So we will output only the load groups that have been found during export of loads
    // one load area and one sub load area is created in any case
    private static String writeLoadGroups(Network network, Collection<LoadGroup> foundLoadGroups, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Write one load area and one sub load area for the whole network
        String baseName = network.getNameOrId();
        String loadAreaId = context.getNamingStrategy().getCgmesId(refTyped(network), LOAD_AREA);
        LoadAreaEq.write(loadAreaId, baseName, cimNamespace, writer, context);
        String subLoadAreaId = context.getNamingStrategy().getCgmesId(refTyped(network), SUB_LOAD_AREA);
        LoadAreaEq.writeSubArea(subLoadAreaId, loadAreaId, baseName, cimNamespace, writer, context);
        for (LoadGroup loadGroup : foundLoadGroups) {
            CgmesExportUtil.writeStartIdName(loadGroup.className, loadGroup.id, loadGroup.name, cimNamespace, writer, context);
            CgmesExportUtil.writeReference("LoadGroup.SubLoadArea", subLoadAreaId, cimNamespace, writer, context);
            writer.writeEndElement();
        }
        return loadAreaId;
    }

    private static void writeLoads(Network network, LoadGroups loadGroups, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            if (context.isExportedEquipment(load)) {
                String className = CgmesExportUtil.loadClassName(load);
                String loadId = context.getNamingStrategy().getCgmesId(load);
                switch (className) {
                    case CgmesNames.ASYNCHRONOUS_MACHINE ->
                            writeAsynchronousMachine(loadId, load.getNameOrId(), cimNamespace, writer, context);
                    case CgmesNames.ENERGY_SOURCE -> writeEnergySource(loadId, load.getNameOrId(), context.getNamingStrategy().getCgmesId(load.getTerminal().getVoltageLevel()), cimNamespace, writer, context);
                    case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, CgmesNames.NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY -> {
                        String loadGroup = loadGroups.groupFor(className, context);
                        String loadResponseCharacteristicId = writeLoadResponseCharacteristic(load, cimNamespace, writer, context);
                        EnergyConsumerEq.write(className, loadId, load.getNameOrId(), loadGroup, context.getNamingStrategy().getCgmesId(load.getTerminal().getVoltageLevel()), loadResponseCharacteristicId, cimNamespace, writer, context);
                    }
                    default -> throw new PowsyblException("Unexpected class name: " + className);
                }
            }
        }
    }

    private static String writeLoadResponseCharacteristic(Load load, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Optional<LoadModel> optionalLoadModel = load.getModel();

        if (optionalLoadModel.isEmpty()) {
            return null;
        }
        if (optionalLoadModel.get().getType() == LoadModelType.EXPONENTIAL) {
            ExponentialLoadModel exponentialLoadModel = (ExponentialLoadModel) optionalLoadModel.get();
            boolean exponentModel = exponentialLoadModel.getNp() != 0 || exponentialLoadModel.getNq() != 0;
            return writeLoadResponseCharacteristicModel(load, exponentModel,
                    exponentialLoadModel.getNp(),
                    exponentialLoadModel.getNq(),
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    cimNamespace, writer, context);
        } else if (optionalLoadModel.get().getType() == LoadModelType.ZIP) {
            ZipLoadModel zipLoadModel = (ZipLoadModel) optionalLoadModel.get();
            return writeLoadResponseCharacteristicModel(load, false, 0.0, 0.0,
                    zipLoadModel.getC0p(), zipLoadModel.getC0q(), zipLoadModel.getC1p(), zipLoadModel.getC1q(), zipLoadModel.getC2p(), zipLoadModel.getC2q(),
                    cimNamespace, writer, context);
        } else {
            return null;
        }
    }

    private static String writeLoadResponseCharacteristicModel(Load load,
                                                               boolean exponentModel, double pVoltageExponent, double qVoltageExponent,
                                                               double pConstantPower, double qConstantPower,
                                                               double pConstantCurrent, double qConstantCurrent,
                                                               double pConstantImpedance, double qConstantImpedance,
                                                               String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String loadResponseId = context.getNamingStrategy().getCgmesId(refTyped(load), LOAD_RESPONSE_CHARACTERISTICS);
        String loadResponseName = "LRC_" + load.getNameOrId();

        LoadResponseCharacteristicEq.write(loadResponseId, loadResponseName,
                exponentModel, pVoltageExponent, qVoltageExponent,
                pConstantPower, qConstantPower, pConstantCurrent,
                qConstantCurrent, pConstantImpedance, qConstantImpedance,
                cimNamespace, writer, context);

        return loadResponseId;
    }

    private static void writeAsynchronousMachine(String id, String name, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(CgmesNames.ASYNCHRONOUS_MACHINE, id, name, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    public static void writeEnergySource(String id, String name, String equipmentContainer, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(CgmesNames.ENERGY_SOURCE, id, name, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private static void writeGenerators(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace, boolean writeInitialP,
                                        XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Multiple synchronous machines may be grouped in the same generating unit
        // We have to write each generating unit only once
        Set<String> generatingUnitsWritten = new HashSet<>();
        for (Generator generator : network.getGenerators()) {
            String cgmesOriginalClass = generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);
            RemoteReactivePowerControl rrpc = generator.getExtension(RemoteReactivePowerControl.class);
            String mode = CgmesExportUtil.getGeneratorRegulatingControlMode(generator, rrpc);
            Terminal regulatingTerminal = mode.equals(RegulatingControlEq.REGULATING_CONTROL_VOLTAGE) ? generator.getRegulatingTerminal() : rrpc.getRegulatingTerminal();
            switch (cgmesOriginalClass) {
                case CgmesNames.EQUIVALENT_INJECTION:
                    String reactiveCapabilityCurveId = writeReactiveCapabilityCurve(generator, cimNamespace, writer, context);
                    String baseVoltageId = context.getBaseVoltageByNominalVoltage(generator.getTerminal().getVoltageLevel().getNominalV()).getId();
                    EquivalentInjectionEq.write(context.getNamingStrategy().getCgmesId(generator), generator.getNameOrId(),
                            generator.isVoltageRegulatorOn(), generator.getMinP(), generator.getMaxP(), obtainMinQ(generator), obtainMaxQ(generator),
                            reactiveCapabilityCurveId, baseVoltageId,
                            cimNamespace, writer, context);
                    break;
                case CgmesNames.EXTERNAL_NETWORK_INJECTION:
                    String regulatingControlId = RegulatingControlEq.writeRegulatingControlEq(generator, exportedTerminalId(mapTerminal2Id, regulatingTerminal), regulatingControlsWritten, mode, cimNamespace, writer, context);
                    ExternalNetworkInjectionEq.write(context.getNamingStrategy().getCgmesId(generator), generator.getNameOrId(),
                            context.getNamingStrategy().getCgmesId(generator.getTerminal().getVoltageLevel()),
                            obtainGeneratorGovernorScd(generator), generator.getMaxP(), obtainMaxQ(generator), generator.getMinP(), obtainMinQ(generator),
                            regulatingControlId, cimNamespace, writer, context);
                    break;
                case CgmesNames.SYNCHRONOUS_MACHINE:
                    regulatingControlId = RegulatingControlEq.writeRegulatingControlEq(generator, exportedTerminalId(mapTerminal2Id, regulatingTerminal), regulatingControlsWritten, mode, cimNamespace, writer, context);
                    writeSynchronousMachine(generator, cimNamespace, writeInitialP,
                            generator.getMinP(), generator.getMaxP(), generator.getTargetP(), generator.getRatedS(), generator.getEnergySource(),
                            regulatingControlId, writer, context, generatingUnitsWritten);
                    break;
                default:
                    throw new PowsyblException("Unexpected cgmes equipment " + cgmesOriginalClass);
            }
        }
    }

    private static double obtainGeneratorGovernorScd(Generator generator) {
        String governorScd = generator.getProperty(Conversion.PROPERTY_CGMES_GOVERNOR_SCD);
        return governorScd == null ? 0.0 : Double.parseDouble(governorScd);
    }

    private static void writeBatteries(Network network, String cimNamespace, boolean writeInitialP,
                                        XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Multiple synchronous machines may be grouped in the same generating unit
        // We have to write each generating unit only once
        Set<String> generatingUnitsWritten = new HashSet<>();
        for (Battery battery : network.getBatteries()) {
            writeSynchronousMachine(battery, cimNamespace, writeInitialP,
                    battery.getMinP(), battery.getMaxP(), battery.getTargetP(), Double.NaN, EnergySource.HYDRO, null,
                    writer, context, generatingUnitsWritten);
        }
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> void writeSynchronousMachine(I i, String cimNamespace, boolean writeInitialP,
                                                                                                double minP, double maxP, double targetP, double ratedS, EnergySource energySource, String regulatingControlId,
                                                                                                XMLStreamWriter writer, CgmesExportContext context, Set<String> generatingUnitsWritten) throws XMLStreamException {

        String generatingUnit = context.getNamingStrategy().getCgmesIdFromProperty(i, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
        double minQ = obtainMinQ(i);
        double maxQ = obtainMaxQ(i);
        double defaultRatedS = computeDefaultRatedS(i, minP, maxP);

        String reactiveLimitsId = writeReactiveCapabilityCurve(i, cimNamespace, writer, context);
        String kind = obtainSynchronousMachineKind(i, minP, maxP, CgmesExportUtil.obtainCurve(i));

        SynchronousMachineEq.write(context.getNamingStrategy().getCgmesId(i), i.getNameOrId(),
                context.getNamingStrategy().getCgmesId(i.getTerminal().getVoltageLevel()),
                generatingUnit, regulatingControlId, reactiveLimitsId, minQ, maxQ,
                ratedS, defaultRatedS, kind, cimNamespace, writer, context);

        if (generatingUnit != null && !generatingUnitsWritten.contains(generatingUnit)) {

            String hydroPowerPlantId = generatingUnitWriteHydroPowerPlantAndFossilFuel(i, cimNamespace, energySource, generatingUnit, writer, context);
            String windGenUnitType = i.getProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE, "onshore");  // considered onshore if property missing

            // We have not preserved the names of generating units
            // We name generating units based on the first machine found
            String generatingUnitName = "GU_" + i.getNameOrId();
            GeneratingUnitEq.write(generatingUnit, generatingUnitName, energySource, minP, maxP, targetP, cimNamespace, writeInitialP,
                    i.getTerminal().getVoltageLevel().getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s)).orElse(null),
                    hydroPowerPlantId, windGenUnitType, writer, context);
            generatingUnitsWritten.add(generatingUnit);
        }
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> String writeReactiveCapabilityCurve(I i, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String reactiveLimitsId = null;
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            ReactiveCapabilityCurve curve = i.getReactiveLimits(ReactiveCapabilityCurve.class);
            if (curveMustBeWritten(curve)) {
                reactiveLimitsId = context.getNamingStrategy().getCgmesId(refTyped(i), REACTIVE_CAPABILITY_CURVE);
                int pointIndex = 0;
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    String pointId = context.getNamingStrategy().getCgmesId(refTyped(i), ref(pointIndex), REACTIVE_CAPABIILITY_CURVE_POINT);
                    CurveDataEq.write(pointId, point.getP(), point.getMinQ(), point.getMaxQ(), reactiveLimitsId, cimNamespace, writer, context);
                    pointIndex++;
                }
                String reactiveCapabilityCurveName = "RCC_" + i.getNameOrId();
                ReactiveCapabilityCurveEq.write(reactiveLimitsId, reactiveCapabilityCurveName, i, cimNamespace, writer, context);
            }
        }
        return reactiveLimitsId;
    }

    private static boolean curveMustBeWritten(ReactiveCapabilityCurve curve) {
        double minQ = Double.min(curve.getMinQ(curve.getMinP()), curve.getMinQ(curve.getMaxP()));
        double maxQ = Double.max(curve.getMaxQ(curve.getMinP()), curve.getMaxQ(curve.getMaxP()));
        return maxQ > minQ;
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> double obtainMinQ(I i) {
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            ReactiveCapabilityCurve curve = i.getReactiveLimits(ReactiveCapabilityCurve.class);
            return Double.min(curve.getMinQ(curve.getMinP()), curve.getMinQ(curve.getMaxP()));
        } else if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
            return i.getReactiveLimits(MinMaxReactiveLimits.class).getMinQ();
        } else {
            throw new PowsyblException("Unexpected ReactiveLimits type in the generator " + i.getNameOrId());
        }
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> double obtainMaxQ(I i) {
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            ReactiveCapabilityCurve curve = i.getReactiveLimits(ReactiveCapabilityCurve.class);
            return Double.max(curve.getMaxQ(curve.getMinP()), curve.getMaxQ(curve.getMaxP()));
        } else if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
            return i.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ();
        } else {
            throw new PowsyblException("Unexpected ReactiveLimits type in the generator " + i.getNameOrId());
        }
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> String generatingUnitWriteHydroPowerPlantAndFossilFuel(I i, String cimNamespace, EnergySource energySource, String generatingUnit, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String hydroPlantStorageType = i.getProperty(Conversion.PROPERTY_HYDRO_PLANT_STORAGE_TYPE);
        String hydroPowerPlantId = null;
        if (hydroPlantStorageType != null && energySource.equals(EnergySource.HYDRO)) {
            String hydroPowerPlantName = i.getNameOrId();
            hydroPowerPlantId = context.getNamingStrategy().getCgmesId(ref(i), HYDRO_POWER_PLANT);
            writeHydroPowerPlant(hydroPowerPlantId, hydroPowerPlantName, hydroPlantStorageType, cimNamespace, writer, context);
        }

        String fossilFuelType = i.getProperty(Conversion.PROPERTY_FOSSIL_FUEL_TYPE);
        if (fossilFuelType != null && !fossilFuelType.isEmpty() && energySource.equals(EnergySource.THERMAL)) {
            String[] fossilFuelTypeArray = fossilFuelType.split(";");
            for (int j = 0; j < fossilFuelTypeArray.length; j++) {
                String fossilFuelName = i.getNameOrId();
                String fossilFuelId = context.getNamingStrategy().getCgmesId(refTyped(i), ref(j), THERMAL_GENERATING_UNIT, FOSSIL_FUEL);
                writeFossilFuel(fossilFuelId, fossilFuelName, fossilFuelTypeArray[j], generatingUnit, cimNamespace, writer, context);
            }
        }

        return hydroPowerPlantId;
    }

    private static void writeHydroPowerPlant(String id, String name, String hydroPlantStorageType, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("HydroPowerPlant", id, name, cimNamespace, writer, context);
        writer.writeEmptyElement(cimNamespace, "HydroPowerPlant.hydroPlantStorageType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s", cimNamespace, "HydroPlantStorageKind." + hydroPlantStorageType));
        writer.writeEndElement();
    }

    private static void writeFossilFuel(String id, String name, String fuelType, String generatingUnit, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("FossilFuel", id, name, cimNamespace, writer, context);
        writer.writeEmptyElement(cimNamespace, "FossilFuel.fossilFuelType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s", cimNamespace, "FuelType." + fuelType));
        CgmesExportUtil.writeReference("FossilFuel.ThermalGeneratingUnit", generatingUnit, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private static <I extends ReactiveLimitsHolder & Injection<I>> double computeDefaultRatedS(I i, double minP, double maxP) {
        List<Double> values = new ArrayList<>();
        values.add(Math.abs(minP));
        values.add(Math.abs(maxP));
        ReactiveLimits limits = i.getReactiveLimits();
        if (limits.getKind() == ReactiveLimitsKind.MIN_MAX) {
            values.add(Math.abs(i.getReactiveLimits(MinMaxReactiveLimits.class).getMinQ()));
            values.add(Math.abs(i.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ()));
        } else { // reactive capability curve
            ReactiveCapabilityCurve curve = i.getReactiveLimits(ReactiveCapabilityCurve.class);
            for (ReactiveCapabilityCurve.Point p : curve.getPoints()) {
                values.add(Math.abs(p.getP()));
                values.add(Math.abs(p.getMinQ()));
                values.add(Math.abs(p.getMaxQ()));
                values.add(Math.sqrt(p.getP() * p.getP() + p.getMinQ() * p.getMinQ()));
                values.add(Math.sqrt(p.getP() * p.getP() + p.getMaxQ() * p.getMaxQ()));
            }
        }
        values.sort(Double::compareTo);
        return values.get(values.size() - 1);
    }

    private static void writeShuntCompensators(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            if ("true".equals(s.getProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT))) {
                // Must have been mapped to a linear shunt compensator with 1 section
                EquivalentShuntEq.write(context.getNamingStrategy().getCgmesId(s), s.getNameOrId(),
                        s.getG(s.getMaximumSectionCount()), s.getB(s.getMaximumSectionCount()),
                        context.getNamingStrategy().getCgmesId(s.getTerminal().getVoltageLevel()),
                        cimNamespace, writer, context);
            } else {
                // Shunt can only regulate voltage
                String mode = RegulatingControlEq.REGULATING_CONTROL_VOLTAGE;
                double bPerSection = 0.0;
                double gPerSection = Double.NaN;
                if (s.getModelType().equals(ShuntCompensatorModelType.LINEAR)) {
                    bPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getBPerSection();
                    gPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getGPerSection();
                }
                String regulatingControlId = RegulatingControlEq.writeRegulatingControlEq(s, exportedTerminalId(mapTerminal2Id, s.getRegulatingTerminal()), regulatingControlsWritten, mode, cimNamespace, writer, context);
                ShuntCompensatorEq.write(context.getNamingStrategy().getCgmesId(s), s.getNameOrId(), s.getSectionCount(), s.getMaximumSectionCount(), s.getTerminal().getVoltageLevel().getNominalV(), s.getModelType(), bPerSection, gPerSection, regulatingControlId,
                        context.getNamingStrategy().getCgmesId(s.getTerminal().getVoltageLevel()), cimNamespace, writer, context);
                if (s.getModelType().equals(ShuntCompensatorModelType.NON_LINEAR)) {
                    double b = 0.0;
                    double g = 0.0;
                    for (int section = 1; section <= s.getMaximumSectionCount(); section++) {
                        String pointId = context.getNamingStrategy().getCgmesId(refTyped(s), ref(section), SHUNT_COMPENSATOR);
                        ShuntCompensatorEq.writePoint(pointId, context.getNamingStrategy().getCgmesId(s), section, s.getB(section) - b, s.getG(section) - g, cimNamespace, writer, context);
                        b = s.getB(section);
                        g = s.getG(section);
                    }
                }
            }
        }
    }

    private static void writeStaticVarCompensators(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace,
                                                   XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String mode = CgmesExportUtil.getSvcMode(svc);
            String regulatingControlId = RegulatingControlEq.writeRegulatingControlEq(svc, exportedTerminalId(mapTerminal2Id, svc.getRegulatingTerminal()), regulatingControlsWritten, mode, cimNamespace, writer, context);
            double inductiveRating = svc.getBmin() != 0 ? 1 / svc.getBmin() : 0;
            double capacitiveRating = svc.getBmax() != 0 ? 1 / svc.getBmax() : 0;
            StaticVarCompensatorEq.write(context.getNamingStrategy().getCgmesId(svc), svc.getNameOrId(), context.getNamingStrategy().getCgmesId(svc.getTerminal().getVoltageLevel()), regulatingControlId, inductiveRating, capacitiveRating, svc.getExtension(VoltagePerReactivePowerControl.class), svc.getRegulationMode(), svc.getVoltageSetpoint(), cimNamespace, writer, context);
        }
    }

    private static void writeLines(Network network, Map<Terminal, String> mapTerminal2Id, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Line line : network.getLines()) {
            String baseVoltage = null;
            if (line.getTerminal1().getVoltageLevel().getNominalV() == line.getTerminal2().getVoltageLevel().getNominalV()) {
                baseVoltage = context.getBaseVoltageByNominalVoltage(line.getTerminal1().getVoltageLevel().getNominalV()).getId();
            }
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(line), line.getNameOrId(), baseVoltage, line.getR(), line.getX(), line.getG1() + line.getG2(), line.getB1() + line.getB2(), cimNamespace, writer, context);
            writeBranchLimits(line, exportedTerminalId(mapTerminal2Id, line.getTerminal1()), exportedTerminalId(mapTerminal2Id, line.getTerminal2()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
    }

    private static void writeTwoWindingsTransformers(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace,
                                                    String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            CgmesExportUtil.addUpdateCgmesTapChangerExtension(twt, context);

            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(), twt.getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s)).orElse(null), cimNamespace, writer, context);
            String end1Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 1);

            // High voltage could be assigned to endNumber = 1 if parameterized.
            EndNumberAssignerForTwoWindingsTransformer endNumberAssigner = new EndNumberAssignerForTwoWindingsTransformer(twt, context.exportTransformersWithHighestVoltageAtEnd1());
            PowerTransformerEndsParameters p = new PowerTransformerEndsParameters(twt, endNumberAssigner.getEndNumberForSide1());

            BaseVoltageMapping.BaseVoltageSource baseVoltage1 = context.getBaseVoltageByNominalVoltage(twt.getTerminal1().getVoltageLevel().getNominalV());
            PowerTransformerEq.writeEnd(end1Id, twt.getNameOrId() + "_1", context.getNamingStrategy().getCgmesId(twt), endNumberAssigner.getEndNumberForSide1(), p.getEnd1R(), p.getEnd1X(), p.getEnd1G(), p.getEnd1B(),
                    twt.getRatedS(), twt.getRatedU1(), exportedTerminalId(mapTerminal2Id, twt.getTerminal1()), baseVoltage1.getId(), cimNamespace, writer, context);
            String end2Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 2);
            BaseVoltageMapping.BaseVoltageSource baseVoltage2 = context.getBaseVoltageByNominalVoltage(twt.getTerminal2().getVoltageLevel().getNominalV());
            PowerTransformerEq.writeEnd(end2Id, twt.getNameOrId() + "_2", context.getNamingStrategy().getCgmesId(twt), endNumberAssigner.getEndNumberForSide2(), p.getEnd2R(), p.getEnd2X(), p.getEnd2G(), p.getEnd2B(),
                    twt.getRatedS(), twt.getRatedU2(), exportedTerminalId(mapTerminal2Id, twt.getTerminal2()), baseVoltage2.getId(), cimNamespace, writer, context);

            // Export tap changers:
            // We are exporting the tap changer as it is modelled in IIDM, always at end 1
            int endNumber = 1;
            // IIDM model always has tap changers (ratio and/or phase) at end 1, and only at end 1.
            // We have to adjust the aliases for potential original tap changers coming from end 1 or end 2.
            // Potential tc2 is always converted to a tc at end 1.
            // If both tc1 and tc2 were present, tc2 was combined during import (fixed at current step) with tc1. Steps from tc1 were kept.
            // If we only had tc2, it was moved to end 1.
            //
            // When we had only tc2, the alias for tc1 if we do EQ export should contain the identifier of original tc2.
            // In the rest of situations, we keep the same id under alias for tc1.
            adjustTapChangerAliases2wt(twt, twt.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER);
            adjustTapChangerAliases2wt(twt, twt.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER);
            writePhaseTapChanger(twt, twt.getPhaseTapChanger(), twt.getNameOrId(), endNumber, end1Id, twt.getRatedU1(), regulatingControlsWritten, cimNamespace, writer, context);
            writeRatioTapChanger(twt, twt.getRatioTapChanger(), twt.getNameOrId(), endNumber, end1Id, twt.getRatedU1(), regulatingControlsWritten, cimNamespace, writer, context);
            writeBranchLimits(twt, exportedTerminalId(mapTerminal2Id, twt.getTerminal1()), exportedTerminalId(mapTerminal2Id, twt.getTerminal2()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
    }

    private static void adjustTapChangerAliases2wt(TwoWindingsTransformer transformer, TapChanger<?, ?, ?, ?> tc, String tapChangerKind) {
        // If we had alias only for tc1, is ok, we will export only tc1 at end 1
        // If we had alias for tc1 and tc2, is ok, tc2 has been moved to end 1 and combined with tc1, but we preserve id for tc1
        // Only if we had tc at end 2 has been moved to end 1 and its identifier must be preserved
        if (tc != null) {
            String aliasType1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tapChangerKind + 1;
            if (transformer.getAliasFromType(aliasType1).isEmpty()) {
                // At this point, if we have a tap changer,
                // the alias for type 2 should be non-empty, but we check it anyway
                String aliasType2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tapChangerKind + 2;
                Optional<String> tc2id = transformer.getAliasFromType(aliasType2);
                if (tc2id.isPresent()) {
                    transformer.removeAlias(tc2id.get());
                    transformer.addAlias(tc2id.get(), aliasType1);
                }
            }
        }
    }

    private static void writeThreeWindingsTransformers(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace,
                                                      String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            CgmesExportUtil.addUpdateCgmesTapChangerExtension(twt, context);

            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(), twt.getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s)).orElse(null), cimNamespace, writer, context);
            double ratedU0 = twt.getRatedU0();

            EndNumberAssignerForThreeWindingsTransformer endNumberAssigner = new EndNumberAssignerForThreeWindingsTransformer(twt, context.exportTransformersWithHighestVoltageAtEnd1());

            String end1Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 1);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_1", end1Id, endNumberAssigner.getEndNumberForLeg1(), 1, twt.getLeg1(), ratedU0, exportedTerminalId(mapTerminal2Id, twt.getLeg1().getTerminal()), regulatingControlsWritten, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
            String end2Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 2);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_2", end2Id, endNumberAssigner.getEndNumberForLeg2(), 2, twt.getLeg2(), ratedU0, exportedTerminalId(mapTerminal2Id, twt.getLeg2().getTerminal()), regulatingControlsWritten, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
            String end3Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 3);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_3", end3Id, endNumberAssigner.getEndNumberForLeg3(), 3, twt.getLeg3(), ratedU0, exportedTerminalId(mapTerminal2Id, twt.getLeg3().getTerminal()), regulatingControlsWritten, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
    }

    private static class EndNumberAssigner {
        private final List<Pair<Double, Integer>> sortedNominalVoltagesSide;

        EndNumberAssigner(double... nominalVoltagesSortedBySide) {
            sortedNominalVoltagesSide = new ArrayList<>();
            for (int k = 0; k < nominalVoltagesSortedBySide.length; k++) {
                sortedNominalVoltagesSide.add(Pair.of(nominalVoltagesSortedBySide[k], k + 1));
            }
            sortedNominalVoltagesSide.sort((Pair<Double, Integer> o1, Pair<Double, Integer> o2) -> {
                if (o1.getLeft() > o2.getLeft()) {
                    return -1;
                } else if (o1.getLeft().equals(o2.getLeft())) {
                    // If nominal values are equal, keep the original side order
                    return Integer.compare(o1.getRight(), o2.getRight());
                } else {
                    return 1;
                }
            });
        }

        int get(double nominalV, int side) {
            return sortedNominalVoltagesSide.indexOf(Pair.of(nominalV, side)) + 1;
        }
    }

    private static class EndNumberAssignerForTwoWindingsTransformer extends EndNumberAssigner {
        private final TwoWindingsTransformer twt;

        private final boolean sorted;

        EndNumberAssignerForTwoWindingsTransformer(TwoWindingsTransformer twt, boolean sorted) {
            super(twt.getTerminal1().getVoltageLevel().getNominalV(), twt.getTerminal2().getVoltageLevel().getNominalV());
            this.twt = twt;
            this.sorted = sorted;
        }

        private int getEndNumberForSide1() {
            return sorted ? get(twt.getTerminal1().getVoltageLevel().getNominalV(), 1) : 1;
        }

        private int getEndNumberForSide2() {
            return sorted ? get(twt.getTerminal2().getVoltageLevel().getNominalV(), 2) : 2;
        }
    }

    private static class EndNumberAssignerForThreeWindingsTransformer extends EndNumberAssigner {
        private final ThreeWindingsTransformer twt;

        private final boolean sorted;

        EndNumberAssignerForThreeWindingsTransformer(ThreeWindingsTransformer twt, boolean sorted) {
            super(twt.getLeg1().getTerminal().getVoltageLevel().getNominalV(),
                    twt.getLeg2().getTerminal().getVoltageLevel().getNominalV(),
                    twt.getLeg3().getTerminal().getVoltageLevel().getNominalV());
            this.twt = twt;
            this.sorted = sorted;
        }

        private int getEndNumberForLeg1() {
            return sorted ? get(twt.getLeg1().getTerminal().getVoltageLevel().getNominalV(), 1) : 1;
        }

        private int getEndNumberForLeg2() {
            return sorted ? get(twt.getLeg2().getTerminal().getVoltageLevel().getNominalV(), 2) : 2;
        }

        private int getEndNumberForLeg3() {
            return sorted ? get(twt.getLeg3().getTerminal().getVoltageLevel().getNominalV(), 3) : 3;
        }
    }

    private static final class PowerTransformerEndsParameters {

        private PowerTransformerEndsParameters(TwoWindingsTransformer twt, int endNumberForSide1) {
            this.twt = twt;
            this.endNumberForSide1 = endNumberForSide1;
            double a0 = twt.getRatedU1() / twt.getRatedU2();
            a02 = a0 * a0;
        }

        private double getEnd1R() {
            return endNumberForSide1 == 1 ? twt.getR() * a02 : 0;
        }

        private double getEnd1X() {
            return endNumberForSide1 == 1 ? twt.getX() * a02 : 0;
        }

        private double getEnd1G() {
            return endNumberForSide1 == 1 ? twt.getG() / a02 : 0;
        }

        private double getEnd1B() {
            return endNumberForSide1 == 1 ? twt.getB() / a02 : 0;
        }

        private double getEnd2R() {
            return endNumberForSide1 == 1 ? 0 : twt.getR();
        }

        private double getEnd2X() {
            return endNumberForSide1 == 1 ? 0 : twt.getX();
        }

        private double getEnd2G() {
            return endNumberForSide1 == 1 ? 0 : twt.getG();
        }

        private double getEnd2B() {
            return endNumberForSide1 == 1 ? 0 : twt.getB();
        }

        private final int endNumberForSide1;
        private final TwoWindingsTransformer twt;
        private final double a02;
    }

    private static void writeThreeWindingsTransformerEnd(ThreeWindingsTransformer twt, String twtId, String twtName, String endId, int endNumber, int legNumber, ThreeWindingsTransformer.Leg leg, double ratedU0, String terminalId, Set<String> regulatingControlsWritten, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // structural ratio at end1
        double a0 = leg.getRatedU() / ratedU0;
        // move structural ratio from end1 to end2
        double a02 = a0 * a0;
        double r = leg.getR() * a02;
        double x = leg.getX() * a02;
        double g = leg.getG() / a02;
        double b = leg.getB() / a02;
        BaseVoltageMapping.BaseVoltageSource baseVoltage = context.getBaseVoltageByNominalVoltage(leg.getTerminal().getVoltageLevel().getNominalV());
        PowerTransformerEq.writeEnd(endId, twtName, twtId, endNumber, r, x, g, b, leg.getRatedS(), leg.getRatedU(), terminalId, baseVoltage.getId(), cimNamespace, writer, context);
        writePhaseTapChanger(twt, leg.getPhaseTapChanger(), twtName, legNumber, endId, leg.getRatedU(), regulatingControlsWritten, cimNamespace, writer, context);
        writeRatioTapChanger(twt, leg.getRatioTapChanger(), twtName, legNumber, endId, leg.getRatedU(), regulatingControlsWritten, cimNamespace, writer, context);
        writeFlowsLimits(twt, leg, terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
    }

    private static <C extends Connectable<C>> void writePhaseTapChanger(C eq, PhaseTapChanger ptc, String twtName, int endNumber, String endId, double neutralU, Set<String> regulatingControlsWritten, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        if (ptc != null) {
            String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber;
            String tapChangerId = eq.getAliasFromType(aliasType).orElseThrow();
            String cgmesTapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(eq, aliasType);

            int neutralStep = getPhaseTapChangerNeutralStep(ptc);
            Optional<String> regulatingControlId = getTapChangerControlId(eq, tapChangerId);
            String cgmesRegulatingControlId = null;
            if (regulatingControlId.isPresent() && CgmesExportUtil.regulatingControlIsDefined(ptc)) {
                String mode = CgmesExportUtil.getPhaseTapChangerRegulationMode(ptc);
                String controlName = twtName + "_PTC_RC";
                String terminalId = CgmesExportUtil.getTerminalId(ptc.getRegulationTerminal(), context);
                cgmesRegulatingControlId = context.getNamingStrategy().getCgmesId(regulatingControlId.get());
                if (!regulatingControlsWritten.contains(cgmesRegulatingControlId)) {
                    TapChangerEq.writeControl(cgmesRegulatingControlId, controlName, mode, terminalId, cimNamespace, writer, context);
                    regulatingControlsWritten.add(cgmesRegulatingControlId);
                }
            }
            String phaseTapChangerTableId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), PHASE_TAP_CHANGER_TABLE);
            // If we write the EQ, we will always write the Tap Changer as tabular
            // We reset the phase tap changer type stored in the extensions
            String typeTabular = CgmesNames.PHASE_TAP_CHANGER_TABULAR;
            CgmesExportUtil.setCgmesTapChangerType(eq, tapChangerId, typeTabular);
            boolean ltcFlag = obtainPhaseTapChangerLtcFlag(ptc.getRegulationMode());
            TapChangerEq.writePhase(typeTabular, cgmesTapChangerId, twtName + "_PTC", endId, ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, ptc.getTapPosition(), neutralU, ltcFlag, phaseTapChangerTableId, cgmesRegulatingControlId, cimNamespace, writer, context);
            TapChangerEq.writePhaseTable(phaseTapChangerTableId, twtName + "_TABLE", cimNamespace, writer, context);
            for (Map.Entry<Integer, PhaseTapChangerStep> step : ptc.getAllSteps().entrySet()) {
                String stepId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), ref(step.getKey()), PHASE_TAP_CHANGER_STEP);
                TapChangerEq.writePhaseTablePoint(stepId, phaseTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), -step.getValue().getAlpha(), step.getKey(), cimNamespace, writer, context);
            }
        }
    }

    // During the cgmes import process the regulationMode is only set to ACTIVE_POWER_CONTROL or CURRENT_LIMITER
    // if ltcFlag is true. If ltcFlag is false the regulationMode is always imported as FIXED_TAP
    private static boolean obtainPhaseTapChangerLtcFlag(PhaseTapChanger.RegulationMode regulationMode) {
        return regulationMode == PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL || regulationMode == PhaseTapChanger.RegulationMode.CURRENT_LIMITER;
    }

    private static <C extends Connectable<C>> Optional<String> getTapChangerControlId(C eq, String tcId) {
        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tcId);
            if (cgmesTc != null) {
                return Optional.ofNullable(cgmesTc.getControlId());
            }
        }
        return Optional.empty();
    }

    private static int getPhaseTapChangerNeutralStep(PhaseTapChanger ptc) {
        int neutralStep = ptc.getLowTapPosition();
        double minAlpha = Math.abs(ptc.getStep(neutralStep).getAlpha());
        for (Map.Entry<Integer, PhaseTapChangerStep> step : ptc.getAllSteps().entrySet()) {
            double tempAlpha = Math.abs(step.getValue().getAlpha());
            if (tempAlpha < minAlpha) {
                minAlpha = tempAlpha;
                neutralStep = step.getKey();
            }
        }
        return neutralStep;
    }

    private static <C extends Connectable<C>> void writeRatioTapChanger(C eq, RatioTapChanger rtc, String twtName, int endNumber, String endId, double neutralU, Set<String> regulatingControlsWritten, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        if (rtc != null) {
            String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber;
            String tapChangerId = eq.getAliasFromType(aliasType).orElseThrow();
            String cgmesTapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(eq, aliasType);

            int neutralStep = getRatioTapChangerNeutralStep(rtc);
            double stepVoltageIncrement;
            if (rtc.getHighTapPosition() == rtc.getLowTapPosition()) {
                stepVoltageIncrement = 100;
            } else {
                stepVoltageIncrement = 100.0 * (1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0 / rtc.getStep(rtc.getHighTapPosition()).getRho()) / (rtc.getLowTapPosition() - rtc.getHighTapPosition());
            }
            String ratioTapChangerTableId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), RATIO_TAP_CHANGER_TABLE);
            Optional<String> regulatingControlId = getTapChangerControlId(eq, tapChangerId);
            String cgmesRegulatingControlId = null;
            String controlMode = "volt";
            if (regulatingControlId.isPresent() && CgmesExportUtil.regulatingControlIsDefined(rtc)) {
                String controlName = twtName + "_RTC_RC";
                String terminalId = CgmesExportUtil.getTerminalId(rtc.getRegulationTerminal(), context);
                cgmesRegulatingControlId = context.getNamingStrategy().getCgmesId(regulatingControlId.get());
                if (!regulatingControlsWritten.contains(cgmesRegulatingControlId)) {
                    String tccMode = CgmesExportUtil.getTcMode(rtc);
                    if (tccMode.equals(RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER)) {
                        controlMode = "reactive";
                    }
                    TapChangerEq.writeControl(cgmesRegulatingControlId, controlName, tccMode, terminalId, cimNamespace, writer, context);
                    regulatingControlsWritten.add(cgmesRegulatingControlId);
                }
            }
            TapChangerEq.writeRatio(cgmesTapChangerId, twtName + "_RTC", endId, rtc.getLowTapPosition(), rtc.getHighTapPosition(), neutralStep, rtc.getTapPosition(), neutralU, rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement,
                    ratioTapChangerTableId, cgmesRegulatingControlId, controlMode, cimNamespace, writer, context);
            TapChangerEq.writeRatioTable(ratioTapChangerTableId, twtName + "_TABLE", cimNamespace, writer, context);
            for (Map.Entry<Integer, RatioTapChangerStep> step : rtc.getAllSteps().entrySet()) {
                String stepId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), ref(step.getKey()), RATIO_TAP_CHANGER_STEP);
                TapChangerEq.writeRatioTablePoint(stepId, ratioTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), step.getKey(), cimNamespace, writer, context);
            }

        }
    }

    private static int getRatioTapChangerNeutralStep(RatioTapChanger rtc) {
        int neutralStep = rtc.getLowTapPosition();
        double minRatio = Math.abs(1 - rtc.getStep(neutralStep).getRho());
        for (Map.Entry<Integer, RatioTapChangerStep> step : rtc.getAllSteps().entrySet()) {
            double tempRatio = Math.abs(1 - step.getValue().getRho());
            if (tempRatio < minRatio) {
                minRatio = tempRatio;
                neutralStep = step.getKey();
            }
        }
        return neutralStep;
    }

    private static void writeDanglingLines(Network network, Map<Terminal, String> mapTerminal2Id, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName,
                                            String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context, Set<Double> exportedBaseVoltagesByNominalV) throws XMLStreamException {
        List<String> exported = new ArrayList<>();

        for (DanglingLine danglingLine : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
            writeUnpairedOrPairedDanglingLines(Collections.singletonList(danglingLine), mapTerminal2Id, cimNamespace, euNamespace,
                    valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer,
                    context, exportedBaseVoltagesByNominalV, exported);
        }

        Set<String> pairingKeys = network.getDanglingLineStream(DanglingLineFilter.PAIRED).map(DanglingLine::getPairingKey).collect(Collectors.toSet());
        for (String pairingKey : pairingKeys) {
            List<DanglingLine> danglingLineList = network.getDanglingLineStream(DanglingLineFilter.PAIRED).filter(danglingLine -> pairingKey.equals(danglingLine.getPairingKey())).toList();
            writeUnpairedOrPairedDanglingLines(danglingLineList, mapTerminal2Id, cimNamespace, euNamespace,
                    valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer,
                    context, exportedBaseVoltagesByNominalV, exported);
        }
    }

    private static void writeUnpairedOrPairedDanglingLines(List<DanglingLine> danglingLineList, Map<Terminal, String> mapTerminal2Id, String cimNamespace, String euNamespace,
                                                           String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer,
                                                           CgmesExportContext context, Set<Double> exportedBaseVoltagesByNominalV, List<String> exported) throws XMLStreamException {

        String baseVoltageId = writeDanglingLinesBaseVoltage(danglingLineList, cimNamespace, writer, context, exportedBaseVoltagesByNominalV);
        String connectivityNodeId = writeDanglingLinesConnectivity(danglingLineList, baseVoltageId, cimNamespace, writer, context);

        for (DanglingLine danglingLine : danglingLineList) {
            // New Equivalent Injection
            writeDanglingLineEquivalentInjection(danglingLine, cimNamespace, baseVoltageId, connectivityNodeId, exported, writer, context);

            // Cast the danglingLine to an AcLineSegment
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(danglingLine), danglingLine.getNameOrId(),
                    context.getBaseVoltageByNominalVoltage(danglingLine.getTerminal().getVoltageLevel().getNominalV()).getId(),
                    danglingLine.getR(), danglingLine.getX(), danglingLine.getG(), danglingLine.getB(), cimNamespace, writer, context);
            writeFlowsLimits(danglingLine, danglingLine, exportedTerminalId(mapTerminal2Id, danglingLine.getTerminal()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
            danglingLine.getAliasFromType("CGMES." + TERMINAL_BOUNDARY).ifPresent(terminalBdId -> {
                try {
                    writeFlowsLimits(danglingLine, danglingLine, terminalBdId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
        }
    }

    private static void writeDanglingLineEquivalentInjection(DanglingLine danglingLine, String cimNamespace,
        String baseVoltageId, String connectivityNodeId, List<String> exported, XMLStreamWriter writer,
        CgmesExportContext context) throws XMLStreamException {

        double minP = 0.0;
        double maxP = 0.0;
        double minQ = 0.0;
        double maxQ = 0.0;
        if (danglingLine.getGeneration() != null) {
            minP = danglingLine.getGeneration().getMinP();
            maxP = danglingLine.getGeneration().getMaxP();
            if (danglingLine.getGeneration().getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
                minQ = danglingLine.getGeneration().getReactiveLimits(MinMaxReactiveLimits.class).getMinQ();
                maxQ = danglingLine.getGeneration().getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ();
            } else {
                throw new PowsyblException("Unexpected type of ReactiveLimits on the dangling line " + danglingLine.getNameOrId());
            }
        }
        String equivalentInjectionId = context.getNamingStrategy().getCgmesIdFromProperty(danglingLine, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
        if (equivalentInjectionId != null && !exported.contains(equivalentInjectionId)) { // check if the equivalent injection has already been written (if several dangling lines linked to same X-node)
            EquivalentInjectionEq.write(equivalentInjectionId, danglingLine.getNameOrId() + "_EI", danglingLine.getGeneration() != null, minP, maxP, minQ, maxQ, null, baseVoltageId, cimNamespace, writer, context);
            exported.add(equivalentInjectionId);
        }
        String equivalentInjectionTerminalId = context.getNamingStrategy().getCgmesIdFromProperty(danglingLine, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
        if (equivalentInjectionTerminalId != null && !exported.contains(equivalentInjectionTerminalId)) { // check if the equivalent injection terminal has already been written (if several dangling lines linked to same X-node)
            TerminalEq.write(equivalentInjectionTerminalId, equivalentInjectionId, connectivityNodeId, 1, cimNamespace, writer, context);
            exported.add(equivalentInjectionTerminalId);
        }
    }

    private static String writeDanglingLinesBaseVoltage(List<DanglingLine> danglingLineList, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context, Set<Double> exportedBaseVoltagesByNominalV) throws XMLStreamException {
        double nominalV = danglingLineList.stream()
                .map(danglingLine -> danglingLine.getTerminal().getVoltageLevel().getNominalV())
                .collect(Collectors.toSet()).stream().sorted().findFirst().orElseThrow();

        BaseVoltageMapping.BaseVoltageSource baseVoltage = context.getBaseVoltageByNominalVoltage(nominalV);
        if (!exportedBaseVoltagesByNominalV.contains(nominalV) && baseVoltage.getSource().equals(Source.IGM)) {
            BaseVoltageEq.write(baseVoltage.getId(), nominalV, cimNamespace, writer, context);
            exportedBaseVoltagesByNominalV.add(nominalV);
        }
        return baseVoltage.getId();
    }

    private static String writeDanglingLinesConnectivity(List<DanglingLine> danglingLineList, String baseVoltageId, String cimNamespace, XMLStreamWriter writer,
                                                         CgmesExportContext context) throws XMLStreamException {
        String connectivityNodeId = null;
        if (context.writeConnectivityNodes()) {
            connectivityNodeId = writeDanglingLinesConnectivityNode(danglingLineList, baseVoltageId, cimNamespace, writer, context);
        } else {
            writeDanglingLinesFictitiousContainer(danglingLineList, baseVoltageId, cimNamespace, writer, context);
        }

        for (DanglingLine danglingLine : danglingLineList) {
            String terminalId = context.getNamingStrategy().getCgmesIdFromAlias(danglingLine, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY);
            TerminalEq.write(terminalId, context.getNamingStrategy().getCgmesId(danglingLine), connectivityNodeId, 2, cimNamespace, writer, context);
        }
        return connectivityNodeId;
    }

    private static String writeDanglingLinesConnectivityNode(List<DanglingLine> danglingLineList, String baseVoltageId, String cimNamespace, XMLStreamWriter writer,
                                                             CgmesExportContext context) throws XMLStreamException {

        Set<String> connectevityNodeIdSet = danglingLineList.stream()
                .map(danglingLine -> obtainConnectivityNodeId(danglingLine, context))
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        String connectivityNodeId;
        if (connectevityNodeIdSet.size() > 1) { // Only in paired danglingLines
            throw new PowsyblException("Paired danglingLines with different connectivityNode on the boundarySide. ParingKey: " + danglingLineList.get(0).getPairingKey());
        } else if (connectevityNodeIdSet.size() == 1) {
            connectivityNodeId = connectevityNodeIdSet.iterator().next();
            setDanglingLinesProperty(danglingLineList, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY, connectivityNodeId);
        } else {
            // If no information about original boundary has been preserved in the IIDM model,
            // we create a new ConnectivityNode in a fictitious Substation and Voltage Level

            if (LOG.isInfoEnabled()) {
                LOG.info("Dangling line(s) not connected to a connectivity node in boundaries files: a fictitious substation and voltage level are created: {}", danglingLinesId(danglingLineList));
            }
            DanglingLine danglingLine = danglingLineList.stream().min(Comparator.comparing(Identifiable::getId)).orElseThrow();
            connectivityNodeId = context.getNamingStrategy().getCgmesId(refTyped(danglingLine), CONNECTIVITY_NODE);

            String connectivityNodeContainerId = createFictitiousContainerFor(danglingLineList, baseVoltageId, cimNamespace, writer, context);
            ConnectivityNodeEq.write(connectivityNodeId, danglingLine.getNameOrId() + "_NODE", connectivityNodeContainerId, cimNamespace, writer, context);
            setDanglingLinesProperty(danglingLineList, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY, connectivityNodeId);
        }
        return connectivityNodeId;
    }

    private static Optional<String> obtainConnectivityNodeId(DanglingLine danglingLine, CgmesExportContext context) {
        return danglingLine.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY)
                ? Optional.of(context.getNamingStrategy().getCgmesIdFromProperty(danglingLine, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY))
                : Optional.empty();
    }

    private static void setDanglingLinesProperty(List<DanglingLine> danglingLineList, String propertyKey, String connectivityNodeId) {
        danglingLineList.forEach(danglingLine -> {
            if (!danglingLine.hasProperty(propertyKey)) {
                danglingLine.setProperty(propertyKey, connectivityNodeId);
            }
        });
    }

    private static void writeDanglingLinesFictitiousContainer(List<DanglingLine> danglingLineList, String baseVoltageId, String cimNamespace, XMLStreamWriter writer,
                                                              CgmesExportContext context) throws XMLStreamException {

        Set<String> topologicalNodeIdSet = danglingLineList.stream()
                .map(EquipmentExport::obtainTopologicalNodeId)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        if (topologicalNodeIdSet.size() > 1) { // Only in paired danglingLines
            throw new PowsyblException("Paired danglingLines with different topologicalNode on the boundarySide. ParingKey: " + danglingLineList.get(0).getPairingKey());
        } else if (topologicalNodeIdSet.size() == 1) {
            String topologicalNodeId = topologicalNodeIdSet.iterator().next();
            setDanglingLinesProperty(danglingLineList, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY, topologicalNodeId);
        } else {
            // Also create a container if we will have to create a Topological Node for the boundary
            if (LOG.isInfoEnabled()) {
                LOG.info("Dangling line(s) not connected to a topology node in boundaries files: a fictitious substation and voltage level are created: {}", danglingLinesId(danglingLineList));
            }
            createFictitiousContainerFor(danglingLineList, baseVoltageId, cimNamespace, writer, context);
        }
    }

    private static Optional<String> obtainTopologicalNodeId(DanglingLine danglingLine) {
        return danglingLine.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY)
                ? Optional.of(danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY))
                : Optional.empty();
    }

    private static String createFictitiousContainerFor(List<DanglingLine> danglingLineList, String baseVoltageId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        DanglingLine danglingLine = danglingLineList.stream().min(Comparator.comparing(Identifiable::getId)).orElseThrow();
        String substationId = writeFictitiousSubstationFor(danglingLine, cimNamespace, writer, context);
        String containerId = writeFictitiousVoltageLevelFor(danglingLine, substationId, baseVoltageId, cimNamespace, writer, context);
        danglingLineList.forEach(dl -> context.setFictitiousContainerFor(dl, containerId));
        return containerId;
    }

    private static String danglingLinesId(List<DanglingLine> danglingLineList) {
        List<String> strings = new ArrayList<>();
        danglingLineList.forEach(danglingLine -> strings.add(danglingLine.getId()));
        String string = String.join(", ", strings);
        return !danglingLineList.isEmpty() && danglingLineList.get(0).getPairingKey() != null
                ? string + " linked to X-node " + danglingLineList.get(0).getPairingKey() : string;
    }

    private static String writeFictitiousSubstationFor(Identifiable<?> identifiable, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // New Substation
        // We avoid using only the name of the identifiable for the names of fictitious region and subregion
        // Because regions and subregions with the same name are merged
        // For names we put "fict" as a prefix instead of a suffix to avoid it being lost because name length limitations
        String baseName = identifiable.getNameOrId();
        String geographicalRegionId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, GEOGRAPHICAL_REGION);
        GeographicalRegionEq.write(geographicalRegionId, "fictGR_" + baseName, cimNamespace, writer, context);
        String subGeographicalRegionId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, SUB_GEOGRAPHICAL_REGION);
        SubGeographicalRegionEq.write(subGeographicalRegionId, "fictSGR_" + baseName, geographicalRegionId, cimNamespace, writer, context);
        String substationId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, SUBSTATION);
        SubstationEq.write(substationId, "fictS_" + baseName, subGeographicalRegionId, cimNamespace, writer, context);
        return substationId;
    }

    private static String writeFictitiousVoltageLevelFor(Identifiable<?> identifiable, String substationId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // New VoltageLevel
        String voltageLevelId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, VOLTAGE_LEVEL);
        VoltageLevelEq.write(voltageLevelId, identifiable.getNameOrId() + "_VL", Double.NaN, Double.NaN, substationId, baseVoltageId, cimNamespace, writer, context);
        return voltageLevelId;
    }

    private static void writeBranchLimits(Branch<?> branch, String terminalId1, String terminalId2, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Collection<OperationalLimitsGroup> limitsGroups1 = new ArrayList<>();
        if (context.isExportAllLimitsGroup()) {
            limitsGroups1.addAll(branch.getOperationalLimitsGroups1());
        } else {
            branch.getSelectedOperationalLimitsGroup1().ifPresent(limitsGroups1::add);
        }
        for (OperationalLimitsGroup limitsGroup : limitsGroups1) {
            writeLimitsGroup(branch, limitsGroup, terminalId1, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }

        Collection<OperationalLimitsGroup> limitsGroups2 = new ArrayList<>();
        if (context.isExportAllLimitsGroup()) {
            limitsGroups2.addAll(branch.getOperationalLimitsGroups2());
        } else {
            branch.getSelectedOperationalLimitsGroup2().ifPresent(limitsGroups2::add);
        }
        for (OperationalLimitsGroup limitsGroup : limitsGroups2) {
            writeLimitsGroup(branch, limitsGroup, terminalId2, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
    }

    private static void writeFlowsLimits(Identifiable<?> identifiable, FlowsLimitsHolder holder, String terminalId, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Collection<OperationalLimitsGroup> limitsGroups = new ArrayList<>();
        if (context.isExportAllLimitsGroup()) {
            limitsGroups.addAll(holder.getOperationalLimitsGroups());
        } else {
            holder.getSelectedOperationalLimitsGroup().ifPresent(limitsGroups::add);
        }
        for (OperationalLimitsGroup limitsGroup : limitsGroups) {
            writeLimitsGroup(identifiable, limitsGroup, terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
    }

    private static void writeLimitsGroup(Identifiable<?> identifiable, OperationalLimitsGroup limitsGroup, String terminalId, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Write the OperationalLimitSet
        String operationalLimitSetId;
        String operationalLimitSetName;
        String propertyKey = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT_SET;
        if (identifiable.hasProperty(propertyKey)) {
            operationalLimitSetId = limitsGroup.getId();
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode propertyNode = mapper.readTree(identifiable.getProperty(propertyKey));
                JsonNode limitsGroupNode = propertyNode.get(operationalLimitSetId);
                operationalLimitSetName = limitsGroupNode.textValue();
            } catch (JsonProcessingException e) {
                operationalLimitSetName = operationalLimitSetId;
            }
        } else {
            operationalLimitSetId = context.getNamingStrategy().getCgmesId(ref(terminalId), ref(limitsGroup.getId()), OPERATIONAL_LIMIT_SET);
            operationalLimitSetName = limitsGroup.getId();
        }
        OperationalLimitSetEq.write(operationalLimitSetId, operationalLimitSetName, terminalId, cimNamespace, writer, context);

        // Write the OperationalLimit objects
        Optional<ActivePowerLimits> activePowerLimits = limitsGroup.getActivePowerLimits();
        if (activePowerLimits.isPresent()) {
            writeLoadingLimits(activePowerLimits.get(), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, operationalLimitSetId, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
        Optional<ApparentPowerLimits> apparentPowerLimits = limitsGroup.getApparentPowerLimits();
        if (apparentPowerLimits.isPresent()) {
            writeLoadingLimits(apparentPowerLimits.get(), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, operationalLimitSetId, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
        Optional<CurrentLimits> currentLimits = limitsGroup.getCurrentLimits();
        if (currentLimits.isPresent()) {
            writeLoadingLimits(currentLimits.get(), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, operationalLimitSetId, exportedLimitTypes, writeInfiniteDuration, writer, context);
        }
    }

    private static void writeLoadingLimits(LoadingLimits limits, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, String operationalLimitSetId, Set<String> exportedLimitTypes, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Write the permanent limit type (if not already written)
        String operationalLimitTypeId = context.getNamingStrategy().getCgmesId(PATL, OPERATIONAL_LIMIT_TYPE);
        if (!exportedLimitTypes.contains(operationalLimitTypeId)) {
            OperationalLimitTypeEq.writePatl(operationalLimitTypeId, cimNamespace, euNamespace, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            exportedLimitTypes.add(operationalLimitTypeId);
        }

        // Write the permanent limit
        String className = loadingLimitClassName(limits);
        String operationalLimitId = context.getNamingStrategy().getCgmesId(ref(operationalLimitSetId), ref(className), PATL, OPERATIONAL_LIMIT_VALUE);
        LoadingLimitEq.write(operationalLimitId, limits, "PATL", limits.getPermanentLimit(), operationalLimitTypeId, operationalLimitSetId, cimNamespace, valueAttributeName, writer, context);

        if (!limits.getTemporaryLimits().isEmpty()) {
            for (LoadingLimits.TemporaryLimit temporaryLimit : limits.getTemporaryLimits()) {
                int acceptableDuration = temporaryLimit.getAcceptableDuration();

                // Write the temporary limit type (if not already written)
                operationalLimitTypeId = context.getNamingStrategy().getCgmesId(TATL, ref(acceptableDuration), OPERATIONAL_LIMIT_TYPE);
                if (!exportedLimitTypes.contains(operationalLimitTypeId)) {
                    OperationalLimitTypeEq.writeTatl(operationalLimitTypeId, temporaryLimit.getAcceptableDuration(), cimNamespace, euNamespace, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
                    exportedLimitTypes.add(operationalLimitTypeId);
                }

                // Write the temporary limit
                operationalLimitId = context.getNamingStrategy().getCgmesId(ref(operationalLimitSetId), ref(className), TATL, ref(acceptableDuration), OPERATIONAL_LIMIT_VALUE);
                LoadingLimitEq.write(operationalLimitId, limits, temporaryLimit.getName(), temporaryLimit.getValue(), operationalLimitTypeId, operationalLimitSetId, cimNamespace, valueAttributeName, writer, context);
            }
        }
    }

    private static void writeHvdcLines(Network network, Map<Terminal, String> mapTerminal2Id, Map<String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        NamingStrategy namingStrategy = context.getNamingStrategy();
        for (HvdcLine line : network.getHvdcLines()) {
            String lineId = context.getNamingStrategy().getCgmesId(line);
            String converter1Id = namingStrategy.getCgmesId(line.getConverterStation1());
            String converter2Id = namingStrategy.getCgmesId(line.getConverterStation2());
            String substation1Id = namingStrategy.getCgmesId(line.getConverterStation1().getTerminal().getVoltageLevel().getNullableSubstation());
            String substation2Id = namingStrategy.getCgmesId(line.getConverterStation2().getTerminal().getVoltageLevel().getNullableSubstation());

            String dcConverterUnit1 = context.getNamingStrategy().getCgmesId(refTyped(line), DC_CONVERTER_UNIT, ref(1));
            writeDCConverterUnit(dcConverterUnit1, line.getNameOrId() + "_1", substation1Id, cimNamespace, writer, context);
            String dcNode1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode1").orElseThrow(PowsyblException::new);
            writeDCNode(dcNode1, line.getNameOrId() + "_1", dcConverterUnit1, cimNamespace, writer, context);

            String dcConverterUnit2 = context.getNamingStrategy().getCgmesId(refTyped(line), DC_CONVERTER_UNIT, ref(2));
            writeDCConverterUnit(dcConverterUnit2, line.getNameOrId() + "_1", substation2Id, cimNamespace, writer, context);
            String dcNode2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode2").orElseThrow(PowsyblException::new);
            writeDCNode(dcNode2, line.getNameOrId() + "_2", dcConverterUnit2, cimNamespace, writer, context);

            String dcTerminal1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal1").orElseThrow(PowsyblException::new);
            writeDCTerminal(dcTerminal1, lineId, dcNode1, 1, cimNamespace, writer, context);

            String dcTerminal2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal2").orElseThrow(PowsyblException::new);
            writeDCTerminal(dcTerminal2, lineId, dcNode2, 2, cimNamespace, writer, context);

            String terminalId;

            HvdcConverterStation<?> converter = line.getConverterStation1();
            terminalId = context.getNamingStrategy().getCgmesId(refTyped(line), refTyped(converter), CONVERTER_STATION, ref(1));
            writeTerminal(converter.getTerminal(), mapTerminal2Id, terminalId, converter1Id, connectivityNodeId(mapNodeKey2NodeId, converter.getTerminal()), 1, cimNamespace, writer, context);
            String capabilityCurveId1 = writeVsCapabilityCurve(converter, cimNamespace, writer, context);
            String acdcConverterDcTerminal1 = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + AC_DC_CONVERTER_DC_TERMINAL).orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal1, converter1Id, dcNode1, 2, cimNamespace, writer, context);

            converter = line.getConverterStation2();
            terminalId = context.getNamingStrategy().getCgmesId(refTyped(line), refTyped(converter), CONVERTER_STATION, ref(2));
            writeTerminal(converter.getTerminal(), mapTerminal2Id, terminalId, converter2Id, connectivityNodeId(mapNodeKey2NodeId, converter.getTerminal()), 1, cimNamespace, writer, context);
            String capabilityCurveId2 = writeVsCapabilityCurve(converter, cimNamespace, writer, context);
            String acdcConverterDcTerminal2 = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + AC_DC_CONVERTER_DC_TERMINAL).orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal2, converter2Id, dcNode2, 2, cimNamespace, writer, context);

            DCLineSegmentEq.write(lineId, line.getNameOrId(), line.getR(), cimNamespace, writer, context);
            writeHvdcConverterStation(line.getConverterStation1(), mapTerminal2Id, line.getNominalV(), dcConverterUnit1, capabilityCurveId1, cimNamespace, writer, context);
            writeHvdcConverterStation(line.getConverterStation2(), mapTerminal2Id, line.getNominalV(), dcConverterUnit2, capabilityCurveId2, cimNamespace, writer, context);
        }
    }

    private static String writeVsCapabilityCurve(HvdcConverterStation<?> converter, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        if (converter instanceof LccConverterStation) {
            return null;
        }
        VscConverterStation vscConverter = (VscConverterStation) converter;
        if (vscConverter.getReactiveLimits() == null) {
            return null;
        }
        String reactiveLimitsId = context.getNamingStrategy().getCgmesId(refTyped(vscConverter), REACTIVE_CAPABILITY_CURVE);
        switch (vscConverter.getReactiveLimits().getKind()) {
            case CURVE -> {
                ReactiveCapabilityCurve curve = vscConverter.getReactiveLimits(ReactiveCapabilityCurve.class);
                int pointIndex = 0;
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    String pointId = context.getNamingStrategy().getCgmesId(refTyped(vscConverter), ref(pointIndex), REACTIVE_CAPABIILITY_CURVE_POINT);
                    CurveDataEq.write(pointId, point.getP(), point.getMinQ(), point.getMaxQ(), reactiveLimitsId, cimNamespace, writer, context);
                    pointIndex++;
                }
                String reactiveCapabilityCurveName = "RCC_" + vscConverter.getNameOrId();
                ReactiveCapabilityCurveEq.write(reactiveLimitsId, reactiveCapabilityCurveName, vscConverter, cimNamespace, writer, context);
            }
            case MIN_MAX ->
                //Do not have to export anything
                    reactiveLimitsId = null;
            default ->
                    throw new PowsyblException("Unexpected type of ReactiveLimits on the VsConverter " + converter.getNameOrId());
        }
        return reactiveLimitsId;
    }

    private static void writeDCConverterUnit(String id, String dcConverterUnitName, String substationId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        DCConverterUnitEq.write(id, dcConverterUnitName, substationId, cimNamespace, writer, context);
    }

    private static void writeHvdcConverterStation(HvdcConverterStation<?> converterStation, Map<Terminal, String> mapTerminal2Id, double ratedUdc, String dcEquipmentContainerId,
                                                  String capabilityCurveId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String pccTerminal = getConverterStationPccTerminal(converterStation, mapTerminal2Id);
        HvdcConverterStationEq.write(context.getNamingStrategy().getCgmesId(converterStation), converterStation.getNameOrId(), converterStation.getHvdcType(), ratedUdc, dcEquipmentContainerId, pccTerminal, capabilityCurveId, cimNamespace, writer, context);
    }

    private static String getConverterStationPccTerminal(HvdcConverterStation<?> converterStation, Map<Terminal, String> mapTerminal2Id) {
        if (converterStation.getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
            return exportedTerminalId(mapTerminal2Id, ((VscConverterStation) converterStation).getRegulatingTerminal());
        }
        return null;
    }

    private static void writeDCNode(String id, String dcNodeName, String dcEquipmentContainerId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        DCNodeEq.write(id, dcNodeName, dcEquipmentContainerId, cimNamespace, writer, context);
    }

    private static void writeDCTerminal(String id, String conductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        DCTerminalEq.write("DCTerminal", id, conductingEquipmentId, dcNodeId, sequenceNumber, cimNamespace, writer, context);
    }

    private static void writeAcdcConverterDCTerminal(String id, String conductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        DCTerminalEq.write(AC_DC_CONVERTER_DC_TERMINAL, id, conductingEquipmentId, dcNodeId, sequenceNumber, cimNamespace, writer, context);
    }

    private static void writeControlAreas(String energyAreaId, Network network, String cimNamespace, String euNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        for (CgmesControlArea cgmesControlArea : cgmesControlAreas.getCgmesControlAreas()) {
            writeControlArea(cgmesControlArea, energyAreaId, cimNamespace, euNamespace, writer, context, network);
        }
    }

    private static void writeControlArea(CgmesControlArea cgmesControlArea, String energyAreaId, String cimNamespace, String euNamespace,
                                         XMLStreamWriter writer, CgmesExportContext context, Network network) throws XMLStreamException {
        // Original control area identifiers may not respect mRID rules, so we pass it through naming strategy
        // to obtain always valid mRID identifiers
        String controlAreaCgmesId = context.getNamingStrategy().getCgmesId(cgmesControlArea.getId());
        ControlAreaEq.write(controlAreaCgmesId, cgmesControlArea.getName(), cgmesControlArea.getEnergyIdentificationCodeEIC(), energyAreaId, cimNamespace, euNamespace, writer, context);
        for (Terminal terminal : cgmesControlArea.getTerminals()) {
            Connectable<?> c = terminal.getConnectable();
            if (c instanceof DanglingLine dl) {
                if (network.isBoundaryElement(dl)) {
                    String tieFlowId = context.getNamingStrategy().getCgmesId(refTyped(c), TIE_FLOW);
                    String terminalId = context.getNamingStrategy().getCgmesIdFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY);
                    TieFlowEq.write(tieFlowId, controlAreaCgmesId, terminalId, cimNamespace, writer, context);
                } else {
                    LOG.error("Unsupported tie flow at TieLine boundary {}", dl.getId());
                }
            } else {
                LOG.warn("Ignored tie flow at {}: should be a dangling line to retrieve boundary terminal", terminal.getConnectable().getId());
            }
        }
        for (Boundary boundary : cgmesControlArea.getBoundaries()) {
            String terminalId = getTieFlowBoundaryTerminal(boundary, context, network);
            if (terminalId != null) {
                String tieFlowId = context.getNamingStrategy().getCgmesId(ref(terminalId), TIE_FLOW);
                TieFlowEq.write(tieFlowId, controlAreaCgmesId, terminalId, cimNamespace, writer, context);
            }
        }
    }

    private static String getTieFlowBoundaryTerminal(Boundary boundary, CgmesExportContext context, Network network) {
        DanglingLine dl = boundary.getDanglingLine();
        if (network.isBoundaryElement(dl)) {
            return context.getNamingStrategy().getCgmesIdFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY);
        } else {
            // This means the boundary corresponds to a TieLine.
            // Because the network should not be a merging view,
            // the only way to have a TieLine in the model is that
            // the original data for the network contained both halves of the TieLine.
            // That is, the initial CGMES data contains the two ACLSs at each side of one boundary point.

            // Currently, we are exporting TieLines in the EQ as a single ACLS,
            // We are not exporting the individual halves of the tie line as separate equipment.
            // So we do not have terminals for the boundary points.

            // This error should be fixed exporting the two halves of the TieLine to the EQ,
            // with their corresponding terminals.
            // Also, the boundary node should not be exported but referenced,
            // as it should be defined in the boundary, not in the instance EQ file.

            LOG.error("Unsupported tie flow at TieLine boundary {}", dl.getId());
            return null;
        }
    }

    private static void writeTerminals(Network network, Map<Terminal, String> mapTerminal2Id, Map<String, String> mapNodeKey2NodeId,
                                       String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) { // TODO write boundary terminals for tie lines from CGMES
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    writeTerminal(t, mapTerminal2Id, mapNodeKey2NodeId, cimNamespace, writer, context);
                }
            }
        }

        String[] switchNodesKeys = new String[2];
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                VoltageLevel vl = sw.getVoltageLevel();
                fillSwitchNodeKeys(vl, sw, switchNodesKeys);
                String nodeId1 = mapNodeKey2NodeId.get(switchNodesKeys[0]);
                String terminalId1 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1);
                TerminalEq.write(terminalId1, context.getNamingStrategy().getCgmesId(sw), nodeId1, 1, cimNamespace, writer, context);
                String nodeId2 = mapNodeKey2NodeId.get(switchNodesKeys[1]);
                String terminalId2 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2);
                TerminalEq.write(terminalId2, context.getNamingStrategy().getCgmesId(sw), nodeId2, 2, cimNamespace, writer, context);
            }
        }
    }

    private static void writeTerminal(Terminal t, Map<Terminal, String> mapTerminal2Id, Map<String, String> mapNodeKey2NodeId,
                                      String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        String equipmentId = context.getNamingStrategy().getCgmesId(t.getConnectable());
        writeTerminal(t, mapTerminal2Id, CgmesExportUtil.getTerminalId(t, context), equipmentId, connectivityNodeId(mapNodeKey2NodeId, t),
                CgmesExportUtil.getTerminalSequenceNumber(t), cimNamespace, writer, context);
    }

    private static void writeTerminal(Terminal terminal, Map<Terminal, String> mapTerminal2Id, String id, String conductingEquipmentId, String connectivityNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        mapTerminal2Id.computeIfAbsent(terminal, k -> {
            try {
                TerminalEq.write(id, conductingEquipmentId, connectivityNodeId, sequenceNumber, cimNamespace, writer, context);
                return id;
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    private static String exportedTerminalId(Map<Terminal, String> mapTerminal2Id, Terminal terminal) {
        if (mapTerminal2Id.containsKey(terminal)) {
            return mapTerminal2Id.get(terminal);
        } else {
            throw new PowsyblException("Terminal has not been exported");
        }
    }

    private static String connectivityNodeId(Map<String, String> mapNodeKey2NodeId, Terminal terminal) {
        String key;
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            key = buildNodeKey(terminal.getVoltageLevel(), terminal.getNodeBreakerView().getNode());
        } else {
            key = buildNodeKey(terminal.getBusBreakerView().getConnectableBus());
        }
        return mapNodeKey2NodeId.get(key);
    }

    private static class VoltageLevelAdjacency {

        private final List<List<Integer>> voltageLevelNodes;

        VoltageLevelAdjacency(VoltageLevel vl, CgmesExportContext context) {
            voltageLevelNodes = new ArrayList<>();

            NodeAdjacency adjacency = new NodeAdjacency(vl, context);
            Set<Integer> visitedNodes = new HashSet<>();
            adjacency.get().keySet().forEach(node -> {
                if (visitedNodes.contains(node)) {
                    return;
                }
                List<Integer> adjacentNodes = computeAdjacentNodes(node, adjacency, visitedNodes);
                voltageLevelNodes.add(adjacentNodes);
            });
        }

        private List<Integer> computeAdjacentNodes(int nodeId, NodeAdjacency adjacency, Set<Integer> visitedNodes) {

            List<Integer> adjacentNodes = new ArrayList<>();
            adjacentNodes.add(nodeId);
            visitedNodes.add(nodeId);

            int k = 0;
            while (k < adjacentNodes.size()) {
                Integer node = adjacentNodes.get(k);
                if (adjacency.get().containsKey(node)) {
                    adjacency.get().get(node).forEach(adjacent -> {
                        if (visitedNodes.contains(adjacent)) {
                            return;
                        }
                        adjacentNodes.add(adjacent);
                        visitedNodes.add(adjacent);
                    });
                }
                k++;
            }
            return adjacentNodes;
        }

        List<List<Integer>> getNodes() {
            return voltageLevelNodes;
        }
    }

    private static class NodeAdjacency {

        private final Map<Integer, List<Integer>> adjacency;

        NodeAdjacency(VoltageLevel vl, CgmesExportContext context) {
            adjacency = new HashMap<>();
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                vl.getNodeBreakerView().getInternalConnections().forEach(this::addAdjacency);
                // When computing the connectivity nodes for the voltage level,
                // switches that are not exported as equipment (they are fictitious)
                // are equivalent to internal connections
                vl.getNodeBreakerView().getSwitchStream()
                        .filter(Objects::nonNull)
                        .filter(sw -> !context.isExportedEquipment(sw))
                        .forEach(this::addAdjacency);
            }
        }

        private void addAdjacency(VoltageLevel.NodeBreakerView.InternalConnection ic) {
            addAdjacency(ic.getNode1(), ic.getNode2());
        }

        private void addAdjacency(Switch sw) {
            addAdjacency(sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId()), sw.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId()));
        }

        private void addAdjacency(int node1, int node2) {
            adjacency.computeIfAbsent(node1, k -> new ArrayList<>()).add(node2);
            adjacency.computeIfAbsent(node2, k -> new ArrayList<>()).add(node1);
        }

        Map<Integer, List<Integer>> get() {
            return adjacency;
        }
    }

    private EquipmentExport() {
    }
}
