/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesReports;
import com.powsybl.cgmes.conversion.elements.dc.DCEquipment;
import com.powsybl.cgmes.conversion.export.elements.*;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.extensions.Source;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.triplestore.api.PropertyBags;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.elements.transformers.AbstractTransformerConversion.getClosestNeutralStep;
import static com.powsybl.cgmes.conversion.elements.transformers.AbstractTransformerConversion.getNormalStep;
import static com.powsybl.cgmes.conversion.export.CgmesExportUtil.*;
import static com.powsybl.cgmes.conversion.export.elements.LoadingLimitEq.loadingLimitClassName;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;
import static com.powsybl.cgmes.model.CgmesNames.NONCONFORM_LOAD;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class EquipmentExport {

    private static final String AC_DC_CONVERTER_DC_TERMINAL = "ACDCConverterDCTerminal";
    private static final String MONOPOLAR_GROUND_RETURN = "monopolarGroundReturn";
    private static final Logger LOG = LoggerFactory.getLogger(EquipmentExport.class);

    private final XMLStreamWriter writer;
    private final CgmesExportContext context;
    private final Network network;
    private final Map<String, String> mapNodeKey2NodeId;
    private final Set<String> regulatingControlsWritten;
    private final Set<String> exportedLimitTypes;
    private final LoadGroups loadGroups;
    private final Map<AcDcConverter<?>, DCConverterUnit> dcConvertersToUnit;
    private final Map<DcNode, DCConverterUnit> dcNodesToUnit;

    public EquipmentExport(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        this.writer = writer;
        this.context = context;
        this.network = network;
        mapNodeKey2NodeId = new HashMap<>();
        regulatingControlsWritten = new HashSet<>();
        exportedLimitTypes = new HashSet<>();
        loadGroups = new LoadGroups();
        dcConvertersToUnit = new HashMap<>();
        dcNodesToUnit = new HashMap<>();
    }

    public void write() {
        CgmesMetadataModel model = CgmesExport.initializeModelForExport(
                network, CgmesSubset.EQUIPMENT, context, true, false);
        write(model);
    }

    public void write(CgmesMetadataModel model) {
        try {
            CgmesExportUtil.writeRdfRoot(context, writer);
            CgmesExportUtil.writeModelDescription(network, CgmesSubset.EQUIPMENT, writer, model, context);

            writeConnectivityNodes();
            writeTerminals();
            writeSwitches();

            writeGeographicalRegions();
            writeSubGeographicalRegions();
            writeSubstations();
            writeBaseVoltages();
            writeVoltageLevels();
            writeBusbarSections();
            writeLoads();
            writeFictitiousInjections();
            String loadAreaId = writeLoadGroups();
            writeGenerators();
            writeBatteries();
            writeShuntCompensators();
            writeStaticVarCompensators();
            writeLines();
            writeTwoWindingsTransformers();
            writeThreeWindingsTransformers();

            writeBoundaryLines();
            writeHvdcLines();

            writeDcConverterUnits();
            writeDcNodes();
            writeDcSwitches();
            writeDcGrounds();
            writeDcLineSegments();
            writeAcDcConverters();

            writeControlAreas(loadAreaId);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeConnectivityNodes() throws XMLStreamException {
        // ConnectivityNodes are:
        // - always exported, preferably from nodes if present or buses otherwise in case of a node-breaker export
        // - exported from buses in case of a CIM 100 bus-branch export
        // - never exported in case of a CIM 16 bus-branch export
        if (!context.isCim16BusBranchExport()) {
            for (VoltageLevel vl : network.getVoltageLevels()) {
                String cgmesVlId = context.getNamingStrategy().getCgmesId(vl);
                if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER && !context.isBusBranchExport()) {
                    writeNodes(vl, cgmesVlId, getVoltageLevelAdjacencies(vl.getNodeBreakerView()));
                } else {
                    writeBuses(vl, cgmesVlId);
                }
            }
        }
    }

    private String buildNodeKey(VoltageLevel vl, int node) {
        return vl.getId() + "_" + node;
    }

    private String buildNodeKey(Bus bus) {
        return bus.getId();
    }

    private void writeNodes(VoltageLevel vl, String cgmesVlId, List<List<Integer>> vlAdjacencies) throws XMLStreamException {
        for (List<Integer> nodes : vlAdjacencies) {
            String cgmesNodeId = context.getNamingStrategy().getCgmesId(refTyped(vl), ref(nodes.getFirst()), CONNECTIVITY_NODE);
            ConnectivityNodeEq.write(cgmesNodeId, CgmesExportUtil.format(nodes.getFirst()), cgmesVlId, writer, context);
            for (Integer nodeNumber : nodes) {
                mapNodeKey2NodeId.put(buildNodeKey(vl, nodeNumber), cgmesNodeId);
            }
        }
    }

    private void writeBuses(VoltageLevel vl, String cgmesVlId) throws XMLStreamException {
        for (Bus bus : vl.getBusBreakerView().getBuses()) {
            String cgmesNodeId = context.getNamingStrategy().getCgmesId(refTyped(bus), Part.CONNECTIVITY_NODE);
            ConnectivityNodeEq.write(cgmesNodeId, bus.getNameOrId(), cgmesVlId, writer, context);
            mapNodeKey2NodeId.put(buildNodeKey(bus), cgmesNodeId);
        }
    }

    private void writeSwitches() throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                VoltageLevel vl = sw.getVoltageLevel();
                String switchType = sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS); // may be null
                // To ensure we do not violate rule SwitchTN1 of ENTSO-E QoCDC,
                // we only export as retained a switch if it will be exported with different TNs at both ends
                switch (switchType) {
                    case "ACLineSegment" -> {
                        String baseVoltageId = context.getBaseVoltageIdFromNominalV(vl.getNominalV());
                        AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(sw), sw.getNameOrId(), baseVoltageId, 0.0, 0.0,
                                0.0, 0.0, writer, context);
                    }
                    case "EquivalentBranch" -> {
                        String baseVoltageId = context.getBaseVoltageIdFromNominalV(vl.getNominalV());
                        EquivalentBranchEq.write(context.getNamingStrategy().getCgmesId(sw), sw.getNameOrId(), baseVoltageId, 0.0, 0.0, writer, context);
                    }
                    case "SeriesCompensator" -> {
                        String baseVoltageId = context.getBaseVoltageIdFromNominalV(vl.getNominalV());
                        SeriesCompensatorEq.write(context.getNamingStrategy().getCgmesId(sw), sw.getNameOrId(), baseVoltageId, 0.0, 0.0, false,
                                0.0, 0.0, writer, context);
                    }
                    case null, default -> {
                        boolean exportAsRetained = sw.isRetained() && hasDifferentTNsAtBothEnds(sw);
                        SwitchEq.write(context.getNamingStrategy().getCgmesId(sw), sw.getNameOrId(), switchType, sw.getKind(),
                                context.getNamingStrategy().getCgmesId(vl), sw.isOpen(), exportAsRetained, writer, context);
                    }
                }
            }
        }
    }

    private void fillSwitchNodeKeys(VoltageLevel vl, Switch sw, String[] nodeKeys) {
        if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER) && !context.isBusBranchExport()) {
            nodeKeys[0] = buildNodeKey(vl, vl.getNodeBreakerView().getNode1(sw.getId()));
            nodeKeys[1] = buildNodeKey(vl, vl.getNodeBreakerView().getNode2(sw.getId()));
        } else {
            nodeKeys[0] = buildNodeKey(vl.getBusBreakerView().getBus1(sw.getId()));
            nodeKeys[1] = buildNodeKey(vl.getBusBreakerView().getBus2(sw.getId()));
        }
    }

    private void writeGeographicalRegions() throws XMLStreamException {
        List<CgmesExportContext.Region> igmGeographicalRegions = context.getRegions().stream()
            .filter(r -> r.source() == Source.IGM)
            .sorted(Comparator.comparing(CgmesExportContext.Region::id))
            .toList();
        for (CgmesExportContext.Region igmGeographicalRegion : igmGeographicalRegions) {
            GeographicalRegionEq.write(igmGeographicalRegion.id(), igmGeographicalRegion.name(), writer, context);
        }
    }

    private void writeSubGeographicalRegions() throws XMLStreamException {
        for (CgmesExportContext.SubRegion subRegion : context.getSubRegions()) {
            SubGeographicalRegionEq.write(subRegion.id(), subRegion.name(), subRegion.regionId(), writer, context);
        }
    }

    private void writeSubstations() throws XMLStreamException {
        for (Substation substation : network.getSubstations()) {
            String subRegionId = context.getSubstationSubRegion(substation.getId());
            SubstationEq.write(context.getNamingStrategy().getCgmesId(substation), substation.getNameOrId(), subRegionId, writer, context);
        }
    }

    private void writeBaseVoltages() throws XMLStreamException {
        List<CgmesExportContext.BaseVoltageSource> igmBaseVoltageSources = context.getBaseVoltageSources().stream()
            .filter(bvs -> bvs.source() == Source.IGM)
            .sorted(Comparator.comparing(CgmesExportContext.BaseVoltageSource::id))
            .toList();
        for (CgmesExportContext.BaseVoltageSource igmBaseVoltageSource : igmBaseVoltageSources) {
            BaseVoltageEq.write(igmBaseVoltageSource.id(), igmBaseVoltageSource.nominalV(), writer, context);
        }
    }

    private void writeVoltageLevels() throws XMLStreamException {
        String fictSubstationId = null;
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            double nominalV = voltageLevel.getNominalV();
            String baseVoltageId = context.getBaseVoltageIdFromNominalV(nominalV);
            Optional<String> substationId = voltageLevel.getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s));
            if (substationId.isEmpty() && fictSubstationId == null) {
                // create a new fictitious substation inside this network
                fictSubstationId = writeFictitiousSubstationFor(network);
            }
            VoltageLevelEq.write(context.getNamingStrategy().getCgmesId(voltageLevel), voltageLevel.getNameOrId(), voltageLevel.getLowVoltageLimit(), voltageLevel.getHighVoltageLimit(),
                    substationId.orElse(fictSubstationId), baseVoltageId, writer, context);
        }
    }

    private void writeBusbarSections() throws XMLStreamException {
        for (BusbarSection bbs : network.getBusbarSections()) {
            BusbarSectionEq.write(context.getNamingStrategy().getCgmesId(bbs), bbs.getNameOrId(),
                    context.getNamingStrategy().getCgmesId(bbs.getTerminal().getVoltageLevel()),
                    context.getBaseVoltageIdFromNominalV(bbs.getTerminal().getVoltageLevel().getNominalV()), writer, context);
        }
    }

    // We may receive a warning if we define an empty load group,
    // So we will output only the load groups that have been found during export of loads
    // one load area and one sub load area is created in any case
    private String writeLoadGroups() throws XMLStreamException {
        // Write one load area and one sub load area for the whole network
        String loadAreaId = context.getNamingStrategy().getCgmesId(refTyped(network), LOAD_AREA);
        String subLoadAreaId = context.getNamingStrategy().getCgmesId(refTyped(network), SUB_LOAD_AREA);
        if (!context.isCim16BusBranchExport()) {
            String baseName = network.getNameOrId();
            LoadAreaEq.write(loadAreaId, baseName, writer, context);
            LoadAreaEq.writeSubArea(subLoadAreaId, loadAreaId, baseName, writer, context);
        }
        for (LoadGroup loadGroup : loadGroups.found()) {
            CgmesExportUtil.writeStartIdName(loadGroup.className, loadGroup.id, loadGroup.name, writer, context);
            if (!context.isCim16BusBranchExport()) {
                CgmesExportUtil.writeReference("LoadGroup.SubLoadArea", subLoadAreaId, writer, context);
            }
            writer.writeEndElement();
        }
        return loadAreaId;
    }

    private void writeFictitiousInjections() throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER && !context.isBusBranchExport()) {
                writeNodeBreakerFictitiousInjections(vl);
            } else {
                writeBusBranchFictitiousInjections(vl);
            }
        }
    }

    private void writeNodeBreakerFictitiousInjections(VoltageLevel vl) throws XMLStreamException {
        VoltageLevel.NodeBreakerView nb = vl.getNodeBreakerView();
        for (int node : nb.getNodes()) {
            double p = nb.getFictitiousP0(node);
            double q = nb.getFictitiousQ0(node);
            if (p != 0.0 || q != 0.0) {
                String loadId = context.getNamingStrategy().getCgmesId(refTyped(vl), FICTITIOUS, ref("NCL"), ref(node));
                String loadName = vl.getNameOrId() + "_FICT_NCL_" + node;
                String terminalId = context.getNamingStrategy().getCgmesId(refTyped(vl), FICTITIOUS, TERMINAL, ref(node));
                String cnId = mapNodeKey2NodeId.get(buildNodeKey(vl, node));

                writeFictitiousInjection(p, loadId, loadName, terminalId, cnId, vl);
            }
        }
    }

    private void writeBusBranchFictitiousInjections(VoltageLevel vl) throws XMLStreamException {
        for (Bus b : vl.getBusBreakerView().getBuses()) {
            double p = b.getFictitiousP0();
            double q = b.getFictitiousQ0();
            if (p != 0.0 || q != 0.0) {
                String loadId = context.getNamingStrategy().getCgmesId(refTyped(b), FICTITIOUS, ref("NCL"));
                String loadName = b.getNameOrId() + "_FICT_NCL";
                String terminalId = context.getNamingStrategy().getCgmesId(refTyped(b), FICTITIOUS, TERMINAL);
                String cnId = mapNodeKey2NodeId.get(buildNodeKey(b));

                writeFictitiousInjection(p, loadId, loadName, terminalId, cnId, vl);
            }
        }
    }

    private void writeFictitiousInjection(double p, String loadId, String loadName, String terminalId, String cnId, VoltageLevel vl) throws XMLStreamException {
        String equipmentContainerId = context.getNamingStrategy().getCgmesId(vl);
        if (p <= 0) {
            writeEnergySource(loadId, loadName, equipmentContainerId);
        } else {
            String loadGroup = loadGroups.groupFor(NONCONFORM_LOAD, context, network);
            EnergyConsumerEq.write(NONCONFORM_LOAD, loadId, loadName, loadGroup, equipmentContainerId, null, writer, context);
        }

        TerminalEq.write(terminalId, loadId, cnId, 1, writer, context);
    }

    private void writeLoads() throws XMLStreamException {
        for (Load load : network.getLoads()) {
            if (context.isExportedEquipment(load)) {
                String className = CgmesExportUtil.loadClassName(load);
                String loadId = context.getNamingStrategy().getCgmesId(load);
                switch (className) {
                    case CgmesNames.ASYNCHRONOUS_MACHINE ->
                        writeAsynchronousMachine(loadId, load.getNameOrId());
                    case CgmesNames.ENERGY_SOURCE -> writeEnergySource(loadId, load.getNameOrId(),
                        context.getNamingStrategy().getCgmesId(load.getTerminal().getVoltageLevel()));
                    case CgmesNames.ENERGY_CONSUMER, CgmesNames.CONFORM_LOAD, NONCONFORM_LOAD, CgmesNames.STATION_SUPPLY -> {
                        String loadGroup = loadGroups.groupFor(className, context, network);
                        String loadResponseCharacteristicId = writeLoadResponseCharacteristic(load);
                        EnergyConsumerEq.write(className, loadId, load.getNameOrId(), loadGroup,
                            context.getNamingStrategy().getCgmesId(load.getTerminal().getVoltageLevel()), loadResponseCharacteristicId, writer, context);
                    }
                    default -> throw new PowsyblException("Unexpected class name: " + className);
                }
            }
        }
    }

    private String writeLoadResponseCharacteristic(Load load) throws XMLStreamException {
        Optional<LoadModel> optionalLoadModel = load.getModel();

        if (optionalLoadModel.isEmpty()) {
            return null;
        }
        if (optionalLoadModel.get().getType() == LoadModelType.EXPONENTIAL) {
            ExponentialLoadModel exponentialLoadModel = (ExponentialLoadModel) optionalLoadModel.get();
            boolean exponentModel = exponentialLoadModel.getNp() != 0 || exponentialLoadModel.getNq() != 0;
            return writeLoadResponseCharacteristicModel(load, exponentModel,
                exponentialLoadModel.getNp(), exponentialLoadModel.getNq(),
                0.0, 0.0,
                0.0, 0.0,
                0.0, 0.0);
        } else if (optionalLoadModel.get().getType() == LoadModelType.ZIP) {
            ZipLoadModel zipLoadModel = (ZipLoadModel) optionalLoadModel.get();
            return writeLoadResponseCharacteristicModel(load, false,
                0.0, 0.0,
                zipLoadModel.getC0p(), zipLoadModel.getC0q(),
                zipLoadModel.getC1p(), zipLoadModel.getC1q(),
                zipLoadModel.getC2p(), zipLoadModel.getC2q());
        } else {
            return null;
        }
    }

    private String writeLoadResponseCharacteristicModel(Load load, boolean exponentModel,
                                                               double pVoltageExponent, double qVoltageExponent,
                                                               double pConstantPower, double qConstantPower,
                                                               double pConstantCurrent, double qConstantCurrent,
                                                               double pConstantImpedance, double qConstantImpedance) throws XMLStreamException {
        String loadResponseId = context.getNamingStrategy().getCgmesId(refTyped(load), LOAD_RESPONSE_CHARACTERISTICS);
        String loadResponseName = "LRC_" + load.getNameOrId();

        LoadResponseCharacteristicEq.write(loadResponseId, loadResponseName,
                exponentModel, pVoltageExponent, qVoltageExponent,
                pConstantPower, qConstantPower, pConstantCurrent,
                qConstantCurrent, pConstantImpedance, qConstantImpedance,
                writer, context);

        return loadResponseId;
    }

    private void writeAsynchronousMachine(String id, String name) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(CgmesNames.ASYNCHRONOUS_MACHINE, id, name, writer, context);
        writer.writeEndElement();
    }

    private void writeEnergySource(String id, String name, String equipmentContainer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName(CgmesNames.ENERGY_SOURCE, id, name, writer, context);
        CgmesExportUtil.writeReference("Equipment.EquipmentContainer", equipmentContainer, writer, context);
        writer.writeEndElement();
    }

    private void writeGenerators() throws XMLStreamException {
        // Multiple synchronous machines may be grouped in the same generating unit
        // We have to write each generating unit only once
        Set<String> generatingUnitsWritten = new HashSet<>();
        for (Generator generator : network.getGenerators()) {
            String cgmesOriginalClass = generator.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SYNCHRONOUS_MACHINE);
            String regulatingControlId;
            switch (cgmesOriginalClass) {
                case CgmesNames.EQUIVALENT_INJECTION:
                    String reactiveCapabilityCurveId = writeReactiveCapabilityCurve(generator);
                    String baseVoltageId = context.getBaseVoltageIdFromNominalV(generator.getTerminal().getVoltageLevel().getNominalV());
                    // Regulation capability is EQ data: it must not depend on the current regulating status
                    boolean regulationCapability = generator.getVoltageRegulation() != null;
                    EquivalentInjectionEq.write(context.getNamingStrategy().getCgmesId(generator), generator.getNameOrId(),
                        regulationCapability, generator.getMinP(), generator.getMaxP(), getNullableMinQ(generator), getNullableMaxQ(generator),
                            reactiveCapabilityCurveId, baseVoltageId,
                            writer, context);
                    break;
                case CgmesNames.EXTERNAL_NETWORK_INJECTION:
                    regulatingControlId = writeRegulatingControl(generator);
                    ExternalNetworkInjectionEq.write(context.getNamingStrategy().getCgmesId(generator), generator.getNameOrId(),
                            context.getNamingStrategy().getCgmesId(generator.getTerminal().getVoltageLevel()),
                            obtainGeneratorGovernorScd(generator), generator.getMaxP(), getMaxQ(generator), generator.getMinP(), getMinQ(generator),
                            regulatingControlId, writer, context);
                    break;
                case CgmesNames.SYNCHRONOUS_MACHINE:
                    regulatingControlId = writeRegulatingControl(generator);
                    writeSynchronousMachine(generator, generator.getMinP(), generator.getMaxP(), generator.getTargetP(),
                        generator.getRatedS(), generator.getEnergySource(), regulatingControlId, generatingUnitsWritten);
                    break;
                default:
                    throw new PowsyblException("Unexpected cgmes equipment " + cgmesOriginalClass);
            }
        }
    }

    private String writeRegulatingControl(Connectable<?> connectable) throws XMLStreamException {
        String regulatingControlId = null;
        if (connectable instanceof VoltageRegulationHolder<?> regulationHolder && regulationHolder.getVoltageRegulation() != null) {
            regulatingControlId = context.getNamingStrategy().getCgmesIdFromProperty(connectable, PROPERTY_REGULATING_CONTROL);
            Terminal regulatingTerminal = regulationHolder.getRegulatingTerminal();
            if (connectable instanceof Generator && context.isExportGeneratorsInLocalRegulationMode()) {
                regulatingTerminal = regulationHolder.getTerminal();
            }
            String regulatingTerminalId = getTerminalId(regulatingTerminal, context);
            String mode = getRegulatingControlMode(regulationHolder.getVoltageRegulation());
            if (!regulatingControlsWritten.contains(regulatingControlId)) {
                RegulatingControlEq.writeRegulatingControlEq(connectable, regulatingTerminalId, regulatingControlId, mode, writer, context);
                regulatingControlsWritten.add(regulatingControlId);
            }
        }
        return regulatingControlId;
    }

    private double obtainGeneratorGovernorScd(Generator generator) {
        String governorScd = generator.getProperty(PROPERTY_GOVERNOR_SCD);
        return governorScd == null ? 0.0 : Double.parseDouble(governorScd);
    }

    private void writeBatteries() throws XMLStreamException {
        // Multiple synchronous machines may be grouped in the same generating unit
        // We have to write each generating unit only once
        Set<String> generatingUnitsWritten = new HashSet<>();
        for (Battery battery : network.getBatteries()) {
            writeSynchronousMachine(battery, battery.getMinP(), battery.getMaxP(), battery.getTargetP(),
                Double.NaN, EnergySource.HYDRO, null, generatingUnitsWritten);
        }
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> void writeSynchronousMachine(I i, double minP, double maxP,
                                                                                         double targetP, double ratedS,
                                                                                         EnergySource energySource,
                                                                                         String regulatingControlId,
                                                                                         Set<String> generatingUnitsWritten)
        throws XMLStreamException {
        double defaultRatedS = computeDefaultRatedS(i, minP, maxP);

        String reactiveLimitsId = writeReactiveCapabilityCurve(i);
        String kind = obtainSynchronousMachineKind(i);
        String generatingUnit = "condenser".equals(kind) ? null : context.getNamingStrategy().getCgmesIdFromProperty(i, PROPERTY_GENERATING_UNIT);

        SynchronousMachineEq.write(context.getNamingStrategy().getCgmesId(i), i.getNameOrId(),
                context.getNamingStrategy().getCgmesId(i.getTerminal().getVoltageLevel()),
                generatingUnit, regulatingControlId, reactiveLimitsId, getNullableMinQ(i), getNullableMaxQ(i),
                ratedS, defaultRatedS, kind, writer, context);

        if (generatingUnit != null && !generatingUnitsWritten.contains(generatingUnit)) {

            String hydroPowerPlantId = generatingUnitWriteHydroPowerPlantAndFossilFuel(i, energySource, generatingUnit);
            String windGenUnitType = i.getProperty(PROPERTY_WIND_GEN_UNIT_TYPE, "onshore");  // considered onshore if property missing

            // We have not preserved the names of generating units
            // We name generating units based on the first machine found
            String generatingUnitName = "GU_" + i.getNameOrId();
            GeneratingUnitEq.write(generatingUnit, generatingUnitName, energySource, minP, maxP, targetP,
                    i.getTerminal().getVoltageLevel().getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s)).orElse(null),
                    hydroPowerPlantId, windGenUnitType, writer, context);
            generatingUnitsWritten.add(generatingUnit);
        }
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> String writeReactiveCapabilityCurve(I i) throws XMLStreamException {
        String reactiveLimitsId = null;
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            ReactiveCapabilityCurve curve = i.getReactiveLimits(ReactiveCapabilityCurve.class);
            if (curveMustBeWritten(curve)) {
                reactiveLimitsId = context.getNamingStrategy().getCgmesId(refTyped(i), REACTIVE_CAPABILITY_CURVE);
                int pointIndex = 0;
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    String pointId = context.getNamingStrategy().getCgmesId(refTyped(i), ref(pointIndex), REACTIVE_CAPABIILITY_CURVE_POINT);
                    CurveDataEq.write(pointId, point.getP(), point.getMinQ(), point.getMaxQ(), reactiveLimitsId, writer, context);
                    pointIndex++;
                }
                String reactiveCapabilityCurveName = "RCC_" + i.getNameOrId();
                ReactiveCapabilityCurveEq.write(reactiveLimitsId, reactiveCapabilityCurveName, i, writer, context);
            }
        }
        return reactiveLimitsId;
    }

    private boolean curveMustBeWritten(ReactiveCapabilityCurve curve) {
        return getCurveMaxQ(curve) > getCurveMinQ(curve);
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> Double getNullableMinQ(I i) {
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            ReactiveCapabilityCurve curve = i.getReactiveLimits(ReactiveCapabilityCurve.class);
            if (curveMustBeWritten(curve)) {
                return null;
            }
        }
        return getMinQ(i);
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> double getMinQ(I i) {
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            return getCurveMinQ(i.getReactiveLimits(ReactiveCapabilityCurve.class));
        } else if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
            return i.getReactiveLimits(MinMaxReactiveLimits.class).getMinQ();
        } else {
            throw new PowsyblException("Unexpected ReactiveLimits type in the generator " + i.getNameOrId());
        }
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> Double getNullableMaxQ(I i) {
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            ReactiveCapabilityCurve curve = i.getReactiveLimits(ReactiveCapabilityCurve.class);
            if (curveMustBeWritten(curve)) {
                return null;
            }
        }
        return getMaxQ(i);
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> double getMaxQ(I i) {
        if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE)) {
            return getCurveMaxQ(i.getReactiveLimits(ReactiveCapabilityCurve.class));
        } else if (i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
            return i.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ();
        } else {
            throw new PowsyblException("Unexpected ReactiveLimits type in the generator " + i.getNameOrId());
        }
    }

    private double getCurveMinQ(ReactiveCapabilityCurve curve) {
        return curve.getPoints()
                .stream()
                .map(ReactiveCapabilityCurve.Point::getMinQ)
                .min(Double::compareTo)
                .orElseThrow();
    }

    private double getCurveMaxQ(ReactiveCapabilityCurve curve) {
        return curve.getPoints()
                .stream()
                .map(ReactiveCapabilityCurve.Point::getMaxQ)
                .max(Double::compareTo)
                .orElseThrow();
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> String generatingUnitWriteHydroPowerPlantAndFossilFuel(I i,
                                                                                                                   EnergySource energySource,
                                                                                                                   String generatingUnit) throws XMLStreamException {
        String hydroPlantStorageType = i.getProperty(PROPERTY_HYDRO_PLANT_STORAGE_TYPE);
        String hydroPowerPlantId = null;
        if (hydroPlantStorageType != null && energySource.equals(EnergySource.HYDRO)) {
            String hydroPowerPlantName = i.getNameOrId();
            hydroPowerPlantId = context.getNamingStrategy().getCgmesId(ref(i), HYDRO_POWER_PLANT);
            writeHydroPowerPlant(hydroPowerPlantId, hydroPowerPlantName, hydroPlantStorageType);
        }

        String fossilFuelType = i.getProperty(PROPERTY_FOSSIL_FUEL_TYPE);
        if (fossilFuelType != null && !fossilFuelType.isEmpty() && energySource.equals(EnergySource.THERMAL)) {
            String[] fossilFuelTypeArray = fossilFuelType.split(";");
            for (int j = 0; j < fossilFuelTypeArray.length; j++) {
                String fossilFuelName = i.getNameOrId();
                String fossilFuelId = context.getNamingStrategy().getCgmesId(refTyped(i), ref(j), THERMAL_GENERATING_UNIT, FOSSIL_FUEL);
                writeFossilFuel(fossilFuelId, fossilFuelName, fossilFuelTypeArray[j], generatingUnit);
            }
        }

        return hydroPowerPlantId;
    }

    private void writeHydroPowerPlant(String id, String name, String hydroPlantStorageType) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartIdName("HydroPowerPlant", id, name, writer, context);
        writer.writeEmptyElement(cimNamespace, "HydroPowerPlant.hydroPlantStorageType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s", cimNamespace, "HydroPlantStorageKind." + hydroPlantStorageType));
        writer.writeEndElement();
    }

    private void writeFossilFuel(String id, String name, String fuelType, String generatingUnit) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeStartIdName("FossilFuel", id, name, writer, context);
        writer.writeEmptyElement(cimNamespace, "FossilFuel.fossilFuelType");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, String.format("%s%s", cimNamespace, "FuelType." + fuelType));
        CgmesExportUtil.writeReference("FossilFuel.ThermalGeneratingUnit", generatingUnit, writer, context);
        writer.writeEndElement();
    }

    private <I extends ReactiveLimitsHolder & Injection<I>> double computeDefaultRatedS(I i, double minP, double maxP) {
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
        return values.getLast();
    }

    private void writeShuntCompensators() throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            if ("true".equals(s.getProperty(PROPERTY_IS_EQUIVALENT_SHUNT))) {
                // Must have been mapped to a linear shunt compensator with 1 section
                EquivalentShuntEq.write(context.getNamingStrategy().getCgmesId(s), s.getNameOrId(),
                        s.getG(s.getMaximumSectionCount()), s.getB(s.getMaximumSectionCount()),
                        context.getNamingStrategy().getCgmesId(s.getTerminal().getVoltageLevel()),
                        writer, context);
            } else {
                // Shunt can only regulate voltage
                double bPerSection = 0.0;
                double gPerSection = Double.NaN;
                if (s.getModelType().equals(ShuntCompensatorModelType.LINEAR)) {
                    bPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getBPerSection();
                    gPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getGPerSection();
                }
                String regulatingControlId = writeRegulatingControl(s);
                ShuntCompensatorEq.write(context.getNamingStrategy().getCgmesId(s), s.getNameOrId(), s.getSectionCount(),
                    s.getMaximumSectionCount(), s.getTerminal().getVoltageLevel().getNominalV(), s.getModelType(), bPerSection, gPerSection, regulatingControlId,
                        context.getNamingStrategy().getCgmesId(s.getTerminal().getVoltageLevel()), writer, context);
                if (s.getModelType().equals(ShuntCompensatorModelType.NON_LINEAR)) {
                    double b = 0.0;
                    double g = 0.0;
                    for (int section = 1; section <= s.getMaximumSectionCount(); section++) {
                        String pointId = context.getNamingStrategy().getCgmesId(refTyped(s), ref(section), SHUNT_COMPENSATOR);
                        ShuntCompensatorEq.writePoint(pointId, context.getNamingStrategy().getCgmesId(s), section, s.getB(section) - b, s.getG(section) - g, writer, context);
                        b = s.getB(section);
                        g = s.getG(section);
                    }
                }
            }
        }
    }

    private void writeStaticVarCompensators() throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String regulatingControlId = writeRegulatingControl(svc);
            double inductiveRating = svc.getBmin() != 0 ? 1 / svc.getBmin() : 0;
            double capacitiveRating = svc.getBmax() != 0 ? 1 / svc.getBmax() : 0;
            RegulationMode regulationMode = svc.getVoltageRegulation() != null ? svc.getVoltageRegulation().getMode() : null;
            double slope = svc.getVoltageRegulation() != null ? svc.getVoltageRegulation().getSlope() : Double.NaN;
            StaticVarCompensatorEq.write(context.getNamingStrategy().getCgmesId(svc),
                svc.getNameOrId(),
                context.getNamingStrategy().getCgmesId(svc.getTerminal().getVoltageLevel()),
                regulatingControlId,
                inductiveRating,
                capacitiveRating,
                slope,
                regulationMode,
                svc.getRegulatingTargetV(),
                writer,
                context);
        }
    }

    private void writeLines() throws XMLStreamException {
        for (Line line : network.getLines()) {
            double baseVoltage = Math.max(line.getTerminal1().getVoltageLevel().getNominalV(), line.getTerminal2().getVoltageLevel().getNominalV());
            String baseVoltageId = context.getBaseVoltageIdFromNominalV(baseVoltage);
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(line), line.getNameOrId(), baseVoltageId, line.getR(), line.getX(),
                line.getG1() + line.getG2(), line.getB1() + line.getB2(), writer, context);
            writeBranchLimits(line, getTerminalId(line.getTerminal1(), context), getTerminalId(line.getTerminal2(), context));
        }
    }

    private void writeTwoWindingsTransformers() throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(),
                twt.getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s)).orElse(null), writer, context);
            String end1Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, ALIAS_TRANSFORMER_END1);

            // High voltage could be assigned to endNumber = 1 if parameterized.
            EndNumberAssignerForTwoWindingsTransformer endNumberAssigner = new EndNumberAssignerForTwoWindingsTransformer(twt, context.exportTransformersWithHighestVoltageAtEnd1());
            PowerTransformerEndsParameters p = new PowerTransformerEndsParameters(twt, endNumberAssigner.getEndNumberForSide1());

            String baseVoltage1Id = context.getBaseVoltageIdFromNominalV(twt.getTerminal1().getVoltageLevel().getNominalV());
            PowerTransformerEq.writeEnd(end1Id, twt.getNameOrId() + "_1", context.getNamingStrategy().getCgmesId(twt),
                endNumberAssigner.getEndNumberForSide1(), p.getEnd1R(), p.getEnd1X(), p.getEnd1G(), p.getEnd1B(),
                    twt.getRatedS(), twt.getRatedU1(), getTerminalId(twt.getTerminal1(), context), baseVoltage1Id, writer, context);
            String end2Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, ALIAS_TRANSFORMER_END2);
            String baseVoltage2Id = context.getBaseVoltageIdFromNominalV(twt.getTerminal2().getVoltageLevel().getNominalV());
            PowerTransformerEq.writeEnd(end2Id, twt.getNameOrId() + "_2", context.getNamingStrategy().getCgmesId(twt),
                endNumberAssigner.getEndNumberForSide2(), p.getEnd2R(), p.getEnd2X(), p.getEnd2G(), p.getEnd2B(),
                    twt.getRatedS(), twt.getRatedU2(), getTerminalId(twt.getTerminal2(), context), baseVoltage2Id, writer, context);

            // Export tap changers:
            // We are exporting the tap changer as it is modeled in IIDM, always at end 1
            int endNumber = 1;
            writePhaseTapChanger(twt, twt.getPhaseTapChanger(), twt.getNameOrId(), endNumber, end1Id, twt.getRatedU1());
            writeRatioTapChanger(twt, twt.getRatioTapChanger(), twt.getNameOrId(), endNumber, end1Id, twt.getRatedU1());
            writeBranchLimits(twt, getTerminalId(twt.getTerminal1(), context), getTerminalId(twt.getTerminal2(), context));
        }
    }

    private void writeThreeWindingsTransformers() throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(),
                twt.getSubstation().map(s -> context.getNamingStrategy().getCgmesId(s)).orElse(null), writer, context);
            double ratedU0 = twt.getRatedU0();

            EndNumberAssignerForThreeWindingsTransformer endNumberAssigner = new EndNumberAssignerForThreeWindingsTransformer(twt, context.exportTransformersWithHighestVoltageAtEnd1());

            String end1Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, ALIAS_TRANSFORMER_END1);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_1",
                end1Id, endNumberAssigner.getEndNumberForLeg1(), 1, twt.getLeg1(), ratedU0, getTerminalId(twt.getLeg1().getTerminal(), context));
            String end2Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, ALIAS_TRANSFORMER_END2);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_2",
                end2Id, endNumberAssigner.getEndNumberForLeg2(), 2, twt.getLeg2(), ratedU0, getTerminalId(twt.getLeg2().getTerminal(), context));
            String end3Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, ALIAS_TRANSFORMER_END3);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_3",
                end3Id, endNumberAssigner.getEndNumberForLeg3(), 3, twt.getLeg3(), ratedU0, getTerminalId(twt.getLeg3().getTerminal(), context));
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

    private void writeThreeWindingsTransformerEnd(ThreeWindingsTransformer twt, String twtId, String twtName, String endId,
                                                         int endNumber, int legNumber, ThreeWindingsTransformer.Leg leg, double ratedU0,
                                                         String terminalId) throws XMLStreamException {
        // structural ratio at end1
        double a0 = leg.getRatedU() / ratedU0;
        // move structural ratio from end1 to end2
        double a02 = a0 * a0;
        double r = leg.getR() * a02;
        double x = leg.getX() * a02;
        double g = leg.getG() / a02;
        double b = leg.getB() / a02;
        String baseVoltageId = context.getBaseVoltageIdFromNominalV(leg.getTerminal().getVoltageLevel().getNominalV());
        PowerTransformerEq.writeEnd(endId, twtName, twtId, endNumber, r, x, g, b, leg.getRatedS(), leg.getRatedU(), terminalId,
            baseVoltageId, writer, context);
        writePhaseTapChanger(twt, leg.getPhaseTapChanger(), twtName, legNumber, endId, leg.getRatedU());
        writeRatioTapChanger(twt, leg.getRatioTapChanger(), twtName, legNumber, endId, leg.getRatedU());
        writeFlowsLimits(leg, terminalId);
    }

    private <C extends Connectable<C>> void writePhaseTapChanger(C eq, PhaseTapChanger ptc, String twtName, int endNumber,
                                                                 String endId, double neutralU) throws XMLStreamException {
        if (ptc != null) {
            String aliasType = switch (endNumber) {
                case 1 -> ALIAS_PHASE_TAP_CHANGER1;
                case 2 -> ALIAS_PHASE_TAP_CHANGER2;
                case 3 -> ALIAS_PHASE_TAP_CHANGER3;
                default -> throw new IllegalStateException("Unexpected end number: " + endNumber);
            };
            if (eq instanceof TwoWindingsTransformer
                && eq.getAliasFromType(ALIAS_PHASE_TAP_CHANGER1).isEmpty()
                && eq.getAliasFromType(ALIAS_PHASE_TAP_CHANGER2).isPresent()) {
                // IIDM TwoWindingTransformer model has phase tap changer at end 1, and only at end 1.
                // whereas CGMES 2-winding PowerTransformer can have phase tap changer at one, the other or both ends.
                // - If the original CGMES contained only a tap changer at end 1, no issue, it is exported back with the same id.
                // - If the original CGMES contained both a tap changer at end 1 and at end 2, they have been combined,
                // and we only export back one tap changer with the id of the original one from end 1.
                // - If the original CGMES contained only a tap changer at end 2, it has been moved to end 1 during import
                // and is exported back at end 1, but with its original id.
                aliasType = ALIAS_PHASE_TAP_CHANGER2;
            }
            String tapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(eq, aliasType);
            String cgmesTapChangerId = eq.getAliasFromType(aliasType).orElse(null);
            int neutralStep = getClosestNeutralStep(ptc);
            int normalStep = getNormalStep(eq, cgmesTapChangerId).orElse(neutralStep);
            String tapChangerControlId = null;
            if (CgmesExportUtil.hasTapChangerControlCapability(ptc)) {
                tapChangerControlId = getTapChangerControlId(eq, PHASE_TAP_CHANGER, endNumber, cgmesTapChangerId, context);
                if (!regulatingControlsWritten.contains(tapChangerControlId)) {
                    String mode = RegulatingControlEq.REGULATING_CONTROL_ACTIVE_POWER;
                    String controlName = twtName + "_PTC_RC";
                    String terminalId = getTapChangerRegulatingTerminalId(eq, ptc.getRegulationTerminal(), endNumber, context);
                    if (ptc.getRegulationMode() == PhaseTapChanger.RegulationMode.CURRENT_LIMITER && !Double.isNaN(ptc.getRegulationValue())) {
                        // Log not supported regulation mode
                        CgmesReports.phaseTapChangerCurrentLimiterModeNotSupportedReport(context.getReportNode(), tapChangerId);

                        // Add a CurrentLimit with the PhaseTapChanger current limiter regulation value to the regulated terminal.
                        String operationalLimitSetId = context.getNamingStrategy().getCgmesId(ref(terminalId), ref(PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name()), OPERATIONAL_LIMIT_SET);
                        String operationalLimitSetName = twtName + "_PTC_" + PhaseTapChanger.RegulationMode.CURRENT_LIMITER;
                        OperationalLimitSetEq.write(operationalLimitSetId, operationalLimitSetName, terminalId, writer, context);

                        String className = "CurrentLimit";
                        String operationalLimitId = context.getNamingStrategy().getCgmesId(ref(operationalLimitSetId), ref(className), PATL, OPERATIONAL_LIMIT_VALUE);
                        String operationalLimitTypeId = context.getNamingStrategy().getCgmesId(PATL, OPERATIONAL_LIMIT_TYPE, ref(network));
                        LoadingLimitEq.write(operationalLimitId, className, "PATL", ptc.getRegulationValue(), operationalLimitTypeId, operationalLimitSetId, writer, context);

                        if (!exportedLimitTypes.contains(operationalLimitTypeId)) {
                            OperationalLimitTypeEq.writePatl(operationalLimitTypeId, writer, context);
                            exportedLimitTypes.add(operationalLimitTypeId);
                        }
                    }
                    TapChangerEq.writeControl(tapChangerControlId, controlName, mode, terminalId, writer, context);
                    regulatingControlsWritten.add(tapChangerControlId);
                }
            }
            String phaseTapChangerTableId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), PHASE_TAP_CHANGER_TABLE);
            // If we write the EQ, we will always write the Tap Changer as tabular
            String typeTabular = CgmesNames.PHASE_TAP_CHANGER_TABULAR;
            TapChangerEq.writePhase(typeTabular, tapChangerId, twtName + "_PTC", endId,
                ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, normalStep, neutralU, ptc.hasLoadTapChangingCapabilities(),
                phaseTapChangerTableId, tapChangerControlId, writer, context);
            TapChangerEq.writePhaseTable(phaseTapChangerTableId, twtName + "_TABLE", writer, context);
            for (Map.Entry<Integer, PhaseTapChangerStep> step : ptc.getAllSteps().entrySet()) {
                String stepId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), ref(step.getKey()), PHASE_TAP_CHANGER_STEP);
                TapChangerEq.writePhaseTablePoint(stepId, phaseTapChangerTableId, step.getValue().getR(), step.getValue().getX(),
                    step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), -step.getValue().getAlpha(),
                    step.getKey(), writer, context);
            }
        }
    }

    /**
     * A tap changer regulation without a terminal is controlled at the end of the tap changer.
     */
    private static String getTapChangerRegulatingTerminalId(Connectable<?> eq, Terminal regulatingTerminal, int endNumber, CgmesExportContext context) {
        Terminal terminal = regulatingTerminal != null ? regulatingTerminal : eq.getTerminals().get(endNumber - 1);
        return CgmesExportUtil.getTerminalId(terminal, context);
    }

    private <C extends Connectable<C>> void writeRatioTapChanger(C eq, RatioTapChanger rtc, String twtName, int endNumber,
                                                                 String endId, double neutralU) throws XMLStreamException {
        if (rtc != null) {
            String aliasType = switch (endNumber) {
                case 1 -> ALIAS_RATIO_TAP_CHANGER1;
                case 2 -> ALIAS_RATIO_TAP_CHANGER2;
                case 3 -> ALIAS_RATIO_TAP_CHANGER3;
                default -> throw new IllegalStateException("Unexpected end number: " + endNumber);
            };
            if (eq instanceof TwoWindingsTransformer
                && eq.getAliasFromType(ALIAS_RATIO_TAP_CHANGER1).isEmpty()
                && eq.getAliasFromType(ALIAS_RATIO_TAP_CHANGER2).isPresent()) {
                // IIDM TwoWindingTransformer model has ratio tap changer at end 1, and only at end 1.
                // whereas CGMES 2-winding PowerTransformer can have ratio tap changer at one, the other or both ends.
                // - If the original CGMES contained only a tap changer at end 1, no issue, it is exported back with the same id.
                // - If the original CGMES contained both a tap changer at end 1 and at end 2, they have been combined,
                // and we only export back one tap changer with the id of the original one from end 1.
                // - If the original CGMES contained only a tap changer at end 2, it has been moved to end 1 during import
                // and is exported back at end 1, but with its original id.
                aliasType = ALIAS_RATIO_TAP_CHANGER2;
            }
            String tapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(eq, aliasType);
            String cgmesTapChangerId = eq.getAliasFromType(aliasType).orElse(null);

            int neutralStep = getClosestNeutralStep(rtc);
            int normalStep = getNormalStep(eq, cgmesTapChangerId).orElse(neutralStep);
            double stepVoltageIncrement;
            if (rtc.getHighTapPosition() == rtc.getLowTapPosition()) {
                stepVoltageIncrement = 100;
            } else {
                stepVoltageIncrement = 100.0 * (1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0 / rtc.getStep(rtc.getHighTapPosition()).getRho()) /
                    (rtc.getLowTapPosition() - rtc.getHighTapPosition());
            }
            String ratioTapChangerTableId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), RATIO_TAP_CHANGER_TABLE);
            String controlMode = "volt";
            String tapChangerControlId = null;
            if (rtc.getVoltageRegulation() != null) {
                String controlName = twtName + "_RTC_RC";
                String terminalId = getTapChangerRegulatingTerminalId(eq, rtc.getRegulatingTerminal(), endNumber, context);
                tapChangerControlId = getTapChangerControlId(eq, RATIO_TAP_CHANGER, endNumber, cgmesTapChangerId, context);
                if (!regulatingControlsWritten.contains(tapChangerControlId)) {
                    String tccMode = getRegulatingControlMode(rtc.getVoltageRegulation());
                    if (tccMode.equals(RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER)) {
                        controlMode = "reactive";
                    }
                    TapChangerEq.writeControl(tapChangerControlId, controlName, tccMode, terminalId, writer, context);
                    regulatingControlsWritten.add(tapChangerControlId);
                }
            }
            TapChangerEq.writeRatio(tapChangerId, twtName + "_RTC", endId, rtc.getLowTapPosition(), rtc.getHighTapPosition(),
                neutralStep, normalStep, neutralU, rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement,
                    ratioTapChangerTableId, tapChangerControlId, controlMode, writer, context);
            TapChangerEq.writeRatioTable(ratioTapChangerTableId, twtName + "_TABLE", writer, context);
            for (Map.Entry<Integer, RatioTapChangerStep> step : rtc.getAllSteps().entrySet()) {
                String stepId = context.getNamingStrategy().getCgmesId(refTyped(eq), ref(endNumber), ref(step.getKey()), RATIO_TAP_CHANGER_STEP);
                TapChangerEq.writeRatioTablePoint(stepId, ratioTapChangerTableId, step.getValue().getR(), step.getValue().getX(),
                    step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), step.getKey(), writer, context);
            }

        }
    }

    private void writeBoundaryLines() throws XMLStreamException {
        List<String> exported = new ArrayList<>();

        for (BoundaryLine boundaryLine : network.getBoundaryLines(BoundaryLineFilter.UNPAIRED)) {
            writeUnpairedOrPairedBoundaryLines(Collections.singletonList(boundaryLine), exported);
        }

        Map<String, List<BoundaryLine>> boundaryLinesByPairingKey = network.getBoundaryLineStream(BoundaryLineFilter.PAIRED)
            .collect(Collectors.groupingBy(CgmesExportUtil::getEffectivePairingKey));
        for (Map.Entry<String, List<BoundaryLine>> entry : boundaryLinesByPairingKey.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            writeUnpairedOrPairedBoundaryLines(entry.getValue(), exported);
        }

    }

    private void writeUnpairedOrPairedBoundaryLines(List<BoundaryLine> boundaryLineList, List<String> exported) throws XMLStreamException {

        double nominalV = boundaryLineList.stream()
            .map(boundaryLine -> boundaryLine.getTerminal().getVoltageLevel().getNominalV())
            .collect(Collectors.toSet()).stream().sorted().findFirst().orElseThrow();
        String baseVoltageId = context.getBaseVoltageIdFromNominalV(nominalV);
        String connectivityNodeId = writeBoundaryLinesConnectivity(boundaryLineList, baseVoltageId);

        for (BoundaryLine boundaryLine : boundaryLineList) {
            // New Equivalent Injection
            writeBoundaryLineEquivalentInjection(boundaryLine, baseVoltageId, connectivityNodeId, exported);

            // Cast the boundaryLine to an AcLineSegment
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(boundaryLine), boundaryLine.getNameOrId(),
                    context.getBaseVoltageIdFromNominalV(boundaryLine.getTerminal().getVoltageLevel().getNominalV()),
                    boundaryLine.getR(), boundaryLine.getX(), boundaryLine.getG(), boundaryLine.getB(), writer, context);
            writeFlowsLimits(boundaryLine, getTerminalId(boundaryLine.getTerminal(), context));
        }
    }

    private void writeBoundaryLineEquivalentInjection(BoundaryLine boundaryLine, String baseVoltageId, String connectivityNodeId, List<String> exported) throws XMLStreamException {

        double minP = 0.0;
        double maxP = 0.0;
        double minQ = 0.0;
        double maxQ = 0.0;
        if (boundaryLine.getGeneration() != null) {
            minP = boundaryLine.getGeneration().getMinP();
            maxP = boundaryLine.getGeneration().getMaxP();
            if (boundaryLine.getGeneration().getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
                minQ = boundaryLine.getGeneration().getReactiveLimits(MinMaxReactiveLimits.class).getMinQ();
                maxQ = boundaryLine.getGeneration().getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ();
            } else {
                throw new PowsyblException("Unexpected type of ReactiveLimits on the boundary line " + boundaryLine.getNameOrId());
            }
        }
        String equivalentInjectionId = context.getNamingStrategy().getCgmesIdFromProperty(boundaryLine, PROPERTY_EQUIVALENT_INJECTION);
        // check if the equivalent injection has already been written (if several boundary lines linked to same X-node)
        if (equivalentInjectionId != null && !exported.contains(equivalentInjectionId)) {
            EquivalentInjectionEq.write(equivalentInjectionId, boundaryLine.getNameOrId() + "_EI", boundaryLine.getGeneration() != null,
                minP, maxP, minQ, maxQ, null, baseVoltageId, writer, context);
            exported.add(equivalentInjectionId);
        }
        String equivalentInjectionTerminalId = context.getNamingStrategy().getCgmesIdFromProperty(boundaryLine, PROPERTY_EQUIVALENT_INJECTION_TERMINAL);
        // check if the equivalent injection terminal has already been written (if several boundary lines linked to same X-node)
        if (equivalentInjectionTerminalId != null && !exported.contains(equivalentInjectionTerminalId)) {
            TerminalEq.write(equivalentInjectionTerminalId, equivalentInjectionId, connectivityNodeId, 1, writer, context);
            exported.add(equivalentInjectionTerminalId);
        }
    }

    private String writeBoundaryLinesConnectivity(List<BoundaryLine> boundaryLineList, String baseVoltageId) throws XMLStreamException {
        String connectivityNodeId = null;
        if (!context.isCim16BusBranchExport()) {
            connectivityNodeId = writeBoundaryLinesConnectivityNode(boundaryLineList, baseVoltageId);
        } else {
            writeBoundaryLinesFictitiousContainer(boundaryLineList, baseVoltageId);
        }

        for (BoundaryLine boundaryLine : boundaryLineList) {
            String terminalId = CgmesExportUtil.getBoundaryLineBoundaryTerminalId(boundaryLine, context);
            TerminalEq.write(terminalId, context.getNamingStrategy().getCgmesId(boundaryLine), connectivityNodeId, 2, writer, context);
        }
        return connectivityNodeId;
    }

    private String writeBoundaryLinesConnectivityNode(List<BoundaryLine> boundaryLineList, String baseVoltageId) throws XMLStreamException {

        Set<String> connectevityNodeIdSet = boundaryLineList.stream()
                .map(this::obtainConnectivityNodeId)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        String connectivityNodeId;
        if (connectevityNodeIdSet.size() > 1) { // Only in paired boundaryLines
            throw new PowsyblException("Paired boundaryLines with different connectivityNode on the boundarySide. ParingKey: " + boundaryLineList.getFirst().getPairingKey());
        } else if (connectevityNodeIdSet.size() == 1) {
            connectivityNodeId = connectevityNodeIdSet.iterator().next();
        } else {
            // If no information about original boundary has been preserved in the IIDM model,
            // we first try to find the ConnectivityNodeId in the reference data provider, and as a last option,
            // we create a new ConnectivityNode in a fictitious Substation and VoltageLevel.

            Optional<String> optionalConnectivityNodeId = findConnectivityNodeId(boundaryLineList.getFirst().getPairingKey());
            connectivityNodeId = optionalConnectivityNodeId.isPresent() ? optionalConnectivityNodeId.get() : createNewConnectivityNode(boundaryLineList, baseVoltageId);
        }
        return connectivityNodeId;
    }

    private Optional<String> findConnectivityNodeId(String pairingKey) {
        ReferenceDataProvider referenceDataProvider = context.getReferenceDataProvider();
        if (pairingKey == null || referenceDataProvider == null) {
            return Optional.empty();
        }
        PropertyBags boundaryNodes = referenceDataProvider.getBoundaryNodes();
        if (boundaryNodes == null) {
            return Optional.empty();
        }
        return boundaryNodes.stream()
                .filter(propertyBag -> pairingKey.equals(propertyBag.getId("name")))
                .map(propertyBag1 -> propertyBag1.getId("ConnectivityNode"))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private String createNewConnectivityNode(List<BoundaryLine> boundaryLineList, String baseVoltageId) throws XMLStreamException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Boundary line(s) not connected to a connectivity node in boundaries files: a fictitious substation and voltage level are created: {}", boundaryLinesId(boundaryLineList));
        }
        BoundaryLine boundaryLine = boundaryLineList.stream().min(Comparator.comparing(Identifiable::getId)).orElseThrow();
        String connectivityNodeId = context.getNamingStrategy().getCgmesId(refTyped(boundaryLine), CONNECTIVITY_NODE);

        String connectivityNodeContainerId = createFictitiousContainerFor(boundaryLineList, baseVoltageId);
        ConnectivityNodeEq.write(connectivityNodeId, boundaryLine.getNameOrId() + "_NODE", connectivityNodeContainerId, writer, context);

        return connectivityNodeId;
    }

    private Optional<String> obtainConnectivityNodeId(BoundaryLine boundaryLine) {
        return boundaryLine.hasProperty(PROPERTY_CONNECTIVITY_NODE_BOUNDARY)
                ? Optional.of(context.getNamingStrategy().getCgmesIdFromProperty(boundaryLine, PROPERTY_CONNECTIVITY_NODE_BOUNDARY))
                : Optional.empty();
    }

    private void writeBoundaryLinesFictitiousContainer(List<BoundaryLine> boundaryLineList, String baseVoltageId) throws XMLStreamException {

        Set<String> topologicalNodeIdSet = boundaryLineList.stream()
                .map(this::obtainTopologicalNodeId)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        if (topologicalNodeIdSet.size() > 1) { // Only in paired boundaryLines
            throw new PowsyblException("Paired boundaryLines with different topologicalNode on the boundarySide. ParingKey: " + boundaryLineList.getFirst().getPairingKey());
        } else if (topologicalNodeIdSet.isEmpty()) {
            // Also create a container if we will have to create a Topological Node for the boundary
            if (LOG.isInfoEnabled()) {
                LOG.info("Boundary line(s) not connected to a topology node in boundaries files: a fictitious substation and voltage level are created: {}", boundaryLinesId(boundaryLineList));
            }
            createFictitiousContainerFor(boundaryLineList, baseVoltageId);
        }
    }

    private Optional<String> obtainTopologicalNodeId(BoundaryLine boundaryLine) {
        return boundaryLine.hasProperty(PROPERTY_TOPOLOGICAL_NODE_BOUNDARY)
                ? Optional.of(boundaryLine.getProperty(PROPERTY_TOPOLOGICAL_NODE_BOUNDARY))
                : Optional.empty();
    }

    private String createFictitiousContainerFor(List<BoundaryLine> boundaryLineList, String baseVoltageId) throws XMLStreamException {
        BoundaryLine boundaryLine = boundaryLineList.stream().min(Comparator.comparing(Identifiable::getId)).orElseThrow();
        String substationId = writeFictitiousSubstationFor(boundaryLine);
        String containerId = writeFictitiousVoltageLevelFor(boundaryLine, substationId, baseVoltageId);
        boundaryLineList.forEach(bl -> context.setFictitiousContainerFor(bl, containerId));
        return containerId;
    }

    private String boundaryLinesId(List<BoundaryLine> boundaryLineList) {
        List<String> strings = new ArrayList<>();
        boundaryLineList.forEach(boundaryLine -> strings.add(boundaryLine.getId()));
        String string = String.join(", ", strings);
        return !boundaryLineList.isEmpty() && boundaryLineList.getFirst().getPairingKey() != null
                ? string + " linked to X-node " + boundaryLineList.getFirst().getPairingKey() : string;
    }

    private String writeFictitiousSubstationFor(Identifiable<?> identifiable) throws XMLStreamException {
        // New Substation
        // We avoid using only the name of the identifiable for the names of fictitious region and subregion
        // Because regions and subregions with the same name are merged
        // For names we put "fict" as a prefix instead of a suffix to avoid it being lost because name length limitations
        String baseName = identifiable.getNameOrId();
        String geographicalRegionId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, GEOGRAPHICAL_REGION);
        GeographicalRegionEq.write(geographicalRegionId, "fictGR_" + baseName, writer, context);
        String subGeographicalRegionId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, SUB_GEOGRAPHICAL_REGION);
        SubGeographicalRegionEq.write(subGeographicalRegionId, "fictSGR_" + baseName, geographicalRegionId, writer, context);
        String substationId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, SUBSTATION);
        SubstationEq.write(substationId, "fictS_" + baseName, subGeographicalRegionId, writer, context);
        return substationId;
    }

    private String writeFictitiousVoltageLevelFor(Identifiable<?> identifiable, String substationId, String baseVoltageId) throws XMLStreamException {
        // New VoltageLevel
        String voltageLevelId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), FICTITIOUS, VOLTAGE_LEVEL);
        VoltageLevelEq.write(voltageLevelId, identifiable.getNameOrId() + "_VL", Double.NaN, Double.NaN, substationId, baseVoltageId, writer, context);
        return voltageLevelId;
    }

    private void writeBranchLimits(Branch<?> branch, String terminalId1, String terminalId2) throws XMLStreamException {
        Collection<OperationalLimitsGroup> limitsGroups1 = new ArrayList<>();
        if (context.isExportAllLimitsGroup()) {
            limitsGroups1.addAll(branch.getOperationalLimitsGroups1());
        } else {
            limitsGroups1.addAll(branch.getAllSelectedOperationalLimitsGroups(TwoSides.ONE));
        }
        for (OperationalLimitsGroup limitsGroup : limitsGroups1) {
            writeLimitsGroup(limitsGroup, terminalId1);
        }

        Collection<OperationalLimitsGroup> limitsGroups2 = new ArrayList<>();
        if (context.isExportAllLimitsGroup()) {
            limitsGroups2.addAll(branch.getOperationalLimitsGroups2());
        } else {
            limitsGroups2.addAll(branch.getAllSelectedOperationalLimitsGroups(TwoSides.TWO));
        }
        for (OperationalLimitsGroup limitsGroup : limitsGroups2) {
            writeLimitsGroup(limitsGroup, terminalId2);
        }
    }

    private void writeFlowsLimits(FlowsLimitsHolder holder, String terminalId) throws XMLStreamException {
        Collection<OperationalLimitsGroup> limitsGroups = new ArrayList<>();
        if (context.isExportAllLimitsGroup()) {
            limitsGroups.addAll(holder.getOperationalLimitsGroups());
        } else {
            limitsGroups.addAll(holder.getAllSelectedOperationalLimitsGroups());
        }
        for (OperationalLimitsGroup limitsGroup : limitsGroups) {
            writeLimitsGroup(limitsGroup, terminalId);
        }
    }

    private void writeLimitsGroup(OperationalLimitsGroup limitsGroup, String terminalId) throws XMLStreamException {
        if (limitsGroup.getCurrentLimits().isEmpty() && context.isCim16BusBranchExport()) {
            // In CIM16, ActivePowerLimit and ApparentPowerLimit are part of EquipmentOperation profile
            return;
        }

        // Write the OperationalLimitSet
        String operationalLimitSetId;
        if (limitsGroup.hasProperty(PROPERTY_OPERATIONAL_LIMIT_SET_RDFID)) {
            operationalLimitSetId = limitsGroup.getProperty(PROPERTY_OPERATIONAL_LIMIT_SET_RDFID);
        } else {
            operationalLimitSetId = context.getNamingStrategy().getCgmesId(ref(terminalId), ref(limitsGroup.getId()), OPERATIONAL_LIMIT_SET);
        }
        String operationalLimitSetName;
        if (limitsGroup.hasProperty(PROPERTY_OPERATIONAL_LIMIT_SET_NAME)) {
            operationalLimitSetName = limitsGroup.getProperty(PROPERTY_OPERATIONAL_LIMIT_SET_NAME);
        } else {
            operationalLimitSetName = limitsGroup.getId();
        }
        OperationalLimitSetEq.write(operationalLimitSetId, operationalLimitSetName, terminalId, writer, context);

        // Write the OperationalLimit objects
        Optional<ActivePowerLimits> activePowerLimits = limitsGroup.getActivePowerLimits();
        if (activePowerLimits.isPresent()) {
            writeLoadingLimits(activePowerLimits.get(), operationalLimitSetId);
        }
        Optional<ApparentPowerLimits> apparentPowerLimits = limitsGroup.getApparentPowerLimits();
        if (apparentPowerLimits.isPresent()) {
            writeLoadingLimits(apparentPowerLimits.get(), operationalLimitSetId);
        }
        Optional<CurrentLimits> currentLimits = limitsGroup.getCurrentLimits();
        if (currentLimits.isPresent()) {
            writeLoadingLimits(currentLimits.get(), operationalLimitSetId);
        }
    }

    private void writeLoadingLimits(LoadingLimits limits, String operationalLimitSetId) throws XMLStreamException {
        if (!(limits instanceof CurrentLimits) && context.isCim16BusBranchExport()) {
            // In CIM16, ActivePowerLimit and ApparentPowerLimit are part of EquipmentOperation profile
            return;
        }

        // Write the permanent limit type (if not already written)
        String operationalLimitTypeId = context.getNamingStrategy().getCgmesId(PATL, OPERATIONAL_LIMIT_TYPE, ref(network));
        if (!exportedLimitTypes.contains(operationalLimitTypeId)) {
            OperationalLimitTypeEq.writePatl(operationalLimitTypeId, writer, context);
            exportedLimitTypes.add(operationalLimitTypeId);
        }

        // Write the permanent limit
        String className = loadingLimitClassName(limits);
        String operationalLimitId = context.getNamingStrategy().getCgmesId(ref(operationalLimitSetId), ref(className), PATL, OPERATIONAL_LIMIT_VALUE);
        LoadingLimitEq.write(operationalLimitId, className, "PATL", limits.getPermanentLimit(), operationalLimitTypeId,
            operationalLimitSetId, writer, context);

        if (!limits.getTemporaryLimits().isEmpty()) {
            for (LoadingLimits.TemporaryLimit temporaryLimit : limits.getTemporaryLimits()) {
                int acceptableDuration = temporaryLimit.getAcceptableDuration();

                // Write the temporary limit type (if not already written)
                operationalLimitTypeId = context.getNamingStrategy().getCgmesId(TATL, ref(acceptableDuration), OPERATIONAL_LIMIT_TYPE, ref(network));
                if (!exportedLimitTypes.contains(operationalLimitTypeId)) {
                    OperationalLimitTypeEq.writeTatl(operationalLimitTypeId, temporaryLimit.getAcceptableDuration(), writer, context);
                    exportedLimitTypes.add(operationalLimitTypeId);
                }

                // Write the temporary limit
                operationalLimitId = context.getNamingStrategy().getCgmesId(ref(operationalLimitSetId), ref(className), TATL, ref(acceptableDuration), OPERATIONAL_LIMIT_VALUE);
                String temporaryLimitName = temporaryLimit.getName().isEmpty() ?
                    "TATL " + temporaryLimit.getAcceptableDuration() : // If the temporary limit name is empty, write TATL and the acceptable duration
                    temporaryLimit.getName();
                LoadingLimitEq.write(operationalLimitId, className, temporaryLimitName, temporaryLimit.getValue(), operationalLimitTypeId, operationalLimitSetId, writer, context);
            }
        }
    }

    private void writeHvdcLines() throws XMLStreamException {
        NamingStrategy namingStrategy = context.getNamingStrategy();
        for (HvdcLine line : network.getHvdcLines()) {
            String lineId = context.getNamingStrategy().getCgmesId(line);
            String converter1Id = namingStrategy.getCgmesId(line.getConverterStation1());
            String converter2Id = namingStrategy.getCgmesId(line.getConverterStation2());
            String substation1Id = namingStrategy.getCgmesId(line.getConverterStation1().getTerminal().getVoltageLevel().getNullableSubstation());
            String substation2Id = namingStrategy.getCgmesId(line.getConverterStation2().getTerminal().getVoltageLevel().getNullableSubstation());

            // There are basically 4 DCNode:
            // - DCNode 1 is connected to the DCLineSegment side 1.
            // - DCNode 2 is connected to the DCLineSegment side 2.
            // - DCNode 1G is connected to a DCGround inside DCConverterUnit 1.
            // - DCNode 2G is connected to a DCGround inside DCConverterUnit 2.
            String dcConverterUnit1 = context.getNamingStrategy().getCgmesId(refTyped(line), DC_CONVERTER_UNIT, ref(1));
            writeDCConverterUnit(dcConverterUnit1, line.getNameOrId() + "_1", MONOPOLAR_GROUND_RETURN, substation1Id);
            String dcNode1 = context.getNamingStrategy().getCgmesId(refTyped(line), DCNODE, ref(1));
            writeDCNode(dcNode1, line.getNameOrId() + "_1", dcConverterUnit1);
            String dcNode1G = context.getNamingStrategy().getCgmesId(refTyped(line), DCNODE, ref("1G"));
            writeDCNode(dcNode1G, line.getNameOrId() + "_1G", dcConverterUnit1);

            String dcConverterUnit2 = context.getNamingStrategy().getCgmesId(refTyped(line), DC_CONVERTER_UNIT, ref(2));
            writeDCConverterUnit(dcConverterUnit2, line.getNameOrId() + "_2", MONOPOLAR_GROUND_RETURN, substation2Id);
            String dcNode2 = context.getNamingStrategy().getCgmesId(refTyped(line), DCNODE, ref(2));
            writeDCNode(dcNode2, line.getNameOrId() + "_2", dcConverterUnit2);
            String dcNode2G = context.getNamingStrategy().getCgmesId(refTyped(line), DCNODE, ref("2G"));
            writeDCNode(dcNode2G, line.getNameOrId() + "_2G", dcConverterUnit2);

            String ground1Id = context.getNamingStrategy().getCgmesId(refTyped(line), DC_GROUND, ref("1G"));
            writeDCGround(ground1Id, line.getNameOrId() + "_1G", 0.0);

            String ground2Id = context.getNamingStrategy().getCgmesId(refTyped(line), DC_GROUND, ref("2G"));
            writeDCGround(ground2Id, line.getNameOrId() + "_2G", 0.0);

            String dcTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(line, ALIAS_DC_TERMINAL1);
            writeDcTerminal(CgmesNames.DC_TERMINAL, dcTerminal1, line.getNameOrId() + " 1", lineId, dcNode1, 1);
            String dcTerminal1G = context.getNamingStrategy().getCgmesId(refTyped(line), DC_TERMINAL, ref("1G"));
            writeDcTerminal(CgmesNames.DC_TERMINAL, dcTerminal1G, line.getNameOrId() + " 1G", ground1Id, dcNode1G, 1);

            String dcTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(line, ALIAS_DC_TERMINAL2);
            writeDcTerminal(CgmesNames.DC_TERMINAL, dcTerminal2, line.getNameOrId() + " 2", lineId, dcNode2, 2);
            String dcTerminal2G = context.getNamingStrategy().getCgmesId(refTyped(line), DC_TERMINAL, ref("2G"));
            writeDcTerminal(CgmesNames.DC_TERMINAL, dcTerminal2G, line.getNameOrId() + " 2G", ground2Id, dcNode2G, 1);

            HvdcConverterStation<?> converter = line.getConverterStation1();
            String capabilityCurveId1 = writeVsCapabilityCurve(converter);
            String acdcConverterDcTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(converter, ALIAS_DC_TERMINAL1);
            writeDcTerminal(AC_DC_CONVERTER_DC_TERMINAL, acdcConverterDcTerminal1, converter.getNameOrId() + " 2", converter1Id, dcNode1, 2);
            String acdcConverterDcTerminal1G = context.getNamingStrategy().getCgmesIdFromAlias(converter, ALIAS_DC_TERMINAL2);
            writeDcTerminal(AC_DC_CONVERTER_DC_TERMINAL, acdcConverterDcTerminal1G, converter.getNameOrId() + " 3", converter1Id, dcNode1G, 3);

            converter = line.getConverterStation2();
            String capabilityCurveId2 = writeVsCapabilityCurve(converter);
            String acdcConverterDcTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(converter, ALIAS_DC_TERMINAL1);
            writeDcTerminal(AC_DC_CONVERTER_DC_TERMINAL, acdcConverterDcTerminal2, converter.getNameOrId() + " 2", converter2Id, dcNode2, 2);
            String acdcConverterDcTerminal2G = context.getNamingStrategy().getCgmesIdFromAlias(converter, ALIAS_DC_TERMINAL2);
            writeDcTerminal(AC_DC_CONVERTER_DC_TERMINAL, acdcConverterDcTerminal2G, converter.getNameOrId() + " 3", converter2Id, dcNode2G, 3);

            DCLineSegmentEq.write(lineId, line.getNameOrId(), line.getR(), writer, context);
            writeHvdcConverterStation(line.getConverterStation1(), line.getNominalV(), dcConverterUnit1, capabilityCurveId1);
            writeHvdcConverterStation(line.getConverterStation2(), line.getNominalV(), dcConverterUnit2, capabilityCurveId2);
        }
    }

    private String writeVsCapabilityCurve(Identifiable<?> identifiable) throws XMLStreamException {
        if (!(identifiable instanceof ReactiveLimitsHolder limitsHolder)) {
            return null;
        }
        ReactiveLimits reactiveLimits = limitsHolder.getReactiveLimits();
        if (reactiveLimits == null) {
            return null;
        }
        String reactiveLimitsId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), REACTIVE_CAPABILITY_CURVE);
        switch (reactiveLimits.getKind()) {
            case CURVE -> {
                ReactiveCapabilityCurve curve = limitsHolder.getReactiveLimits(ReactiveCapabilityCurve.class);
                int pointIndex = 0;
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    String pointId = context.getNamingStrategy().getCgmesId(refTyped(identifiable), ref(pointIndex), REACTIVE_CAPABIILITY_CURVE_POINT);
                    CurveDataEq.write(pointId, point.getP(), point.getMinQ(), point.getMaxQ(), reactiveLimitsId, writer, context);
                    pointIndex++;
                }
                String reactiveCapabilityCurveName = "RCC_" + identifiable.getNameOrId();
                ReactiveCapabilityCurveEq.write(reactiveLimitsId, reactiveCapabilityCurveName, limitsHolder, writer, context);
            }
            case MIN_MAX ->
                //Do not have to export anything
                reactiveLimitsId = null;
            default ->
                    throw new PowsyblException("Unexpected type of ReactiveLimits on the VsConverter " + identifiable.getNameOrId());
        }
        return reactiveLimitsId;
    }

    private void writeDCConverterUnit(String id, String dcConverterUnitName, String operationMode, String substationId) throws XMLStreamException {
        DCConverterUnitEq.write(id, dcConverterUnitName, operationMode, substationId, writer, context);
    }

    private void writeHvdcConverterStation(HvdcConverterStation<?> converterStation, double ratedUdc, String dcEquipmentContainerId,
                                                  String capabilityCurveId) throws XMLStreamException {
        String pccTerminal = getConverterStationPccTerminal(converterStation);
        String className = converterClassName(converterStation);
        AcDcConverterEq.write(context.getNamingStrategy().getCgmesId(converterStation), converterStation.getNameOrId(), className, ratedUdc,
                0.0, 0.0, 0.0, dcEquipmentContainerId, pccTerminal, capabilityCurveId, writer, context);
    }

    private String getConverterStationPccTerminal(HvdcConverterStation<?> converterStation) {
        if (converterStation instanceof VscConverterStation vsc && vsc.getRegulatingTerminal() != vsc.getTerminal()) {
            return getTerminalId(vsc.getRegulatingTerminal(), context);
        }
        return null;
    }

    private void writeDCNode(String id, String dcNodeName, String dcEquipmentContainerId) throws XMLStreamException {
        DCNodeEq.write(id, dcNodeName, dcEquipmentContainerId, writer, context);
    }

    private void writeDCGround(String id, String dcGroundName, double resistance) throws XMLStreamException {
        DCGroundEq.write(id, dcGroundName, resistance, writer, context);
    }

    private void writeDcTerminal(DcTerminal dcTerminal) throws XMLStreamException {
        int sequenceNumber = CgmesExportUtil.getDcTerminalSequenceNumber(dcTerminal);
        writeDcTerminal(dcTerminal.getDcConnectable(), dcTerminal.getDcNode(), sequenceNumber);
    }

    private void writeDcTerminal(Identifiable<?> dcIdentifiable, DcNode dcNode, int sequenceNumber) throws XMLStreamException {
        String dcIdentifiableId = context.getNamingStrategy().getCgmesId(dcIdentifiable);
        String dcNodeId = null;
        if (!context.isBusBranchExport()) {
            // node-breaker export
            dcNodeId = context.getNamingStrategy().getCgmesId(dcNode);
        } else if (!context.isCim16BusBranchExport()) {
            // CIM100 bus-branch export
            dcNodeId = context.getNamingStrategy().getCgmesId(dcNode.getDcBus());
        }
        String aliasType = sequenceNumber == 1 ? ALIAS_DC_TERMINAL1 : ALIAS_DC_TERMINAL2;
        String dcTerminalId = context.getNamingStrategy().getCgmesIdFromAlias(dcIdentifiable, aliasType);
        String className = dcIdentifiable instanceof AcDcConverter<?> ? AC_DC_CONVERTER_DC_TERMINAL : CgmesNames.DC_TERMINAL;
        writeDcTerminal(className, dcTerminalId, dcIdentifiable.getNameOrId() + " " + sequenceNumber, dcIdentifiableId, dcNodeId, sequenceNumber);
    }

    private void writeDcTerminal(String className, String id, String name, String conductingEquipmentId, String dcNodeId,
                                        int sequenceNumber) throws XMLStreamException {
        DCTerminalEq.write(className, id, name, conductingEquipmentId, dcNodeId, sequenceNumber, writer, context);
    }

    private void writeDcConverterUnits() throws XMLStreamException {
        // Build DCConverterUnit hierarchy
        getAcDcConvertersUnit();
        getDcNodesConverterUnit();

        // Build DCConverterUnit adjacency.
        // In order to properly populate the DCConverterUnit.operationMode attribute, DCConverterUnit shall not be
        // considered separately but with the possible presence of an adjacent one.
        Map<DCConverterUnit, List<DcNode>> dcNodesByConverterUnit = dcNodesToUnit.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        Map<DCConverterUnit, List<DCConverterUnit>> dcConverterUnitAdjacency = new HashMap<>();
        List<DCEquipment> dcEquipments = new ArrayList<>();
        dcEquipments.addAll(getDCEquipmentConverters());
        dcEquipments.addAll(getDCEquipmentSwitches());
        dcNodesByConverterUnit.forEach((converterUnit, dcNodes) ->
            // Retrieve adjacent units by checking node adjacency.
            dcConverterUnitAdjacency.put(converterUnit, dcNodes.stream()
                    .flatMap(n -> getAdjacentNodes(dcEquipments, n.getId()).stream())
                    .map(network::getDcNode)
                    .map(dcNodesToUnit::get)
                    .distinct()
                    .toList()));

        // Write DCConverterUnits
        for (DCConverterUnit dcConverterUnit : dcNodesByConverterUnit.keySet()) {
            Set<DcNode> adjacentNodes = dcConverterUnitAdjacency.get(dcConverterUnit).stream()
                    .flatMap(unit -> dcNodesByConverterUnit.get(unit).stream())
                    .collect(Collectors.toSet());
            String operationMode = getDcUnitOperationMode(adjacentNodes);

            writeDCConverterUnit(dcConverterUnit.id(), dcConverterUnit.name(), operationMode, dcConverterUnit.substation());
        }
    }

    private String getDcUnitOperationMode(Set<DcNode> dcNodes) {
        if (Stream.concat(network.getLineCommutatedConverterStream(), network.getVoltageSourceConverterStream())
                .filter(c -> dcNodes.contains(c.getDcTerminal1().getDcNode()) || dcNodes.contains(c.getDcTerminal2().getDcNode()))
                .count() > 1) {
            return "bipolar";
        } else if (network.getDcLineStream()
                .filter(l -> dcNodes.contains(l.getDcTerminal1().getDcNode()) || dcNodes.contains(l.getDcTerminal2().getDcNode()))
                .count() > 1) {
            return "monopolarMetallicReturn";
        }
        return MONOPOLAR_GROUND_RETURN;
    }

    private void writeDcNodes() throws XMLStreamException {
        // DCNodes are:
        // - exported from DcNodes in case of a node-breaker export
        // - exported from DcBuses in case of a CIM100 bus-branch export
        // - never exported in case of a CIM16 bus-branch export
        if (!context.isBusBranchExport()) {
            for (DcNode dcNode : network.getDcNodes()) {
                // node-breaker export
                String dcNodeId = context.getNamingStrategy().getCgmesId(dcNode);
                String dcConverterUnitId = dcNodesToUnit.containsKey(dcNode) ? dcNodesToUnit.get(dcNode).id() : null;
                writeDCNode(dcNodeId, dcNode.getNameOrId(), dcConverterUnitId);
            }
        } else if (!context.isCim16BusBranchExport()) {
            for (DcBus dcBus : network.getDcBuses()) {
                // CIM100 bus-branch export
                String dcBusId = context.getNamingStrategy().getCgmesId(dcBus);
                DcNode refDcNode = dcBus.getDcNodeStream().min(Comparator.comparing(DcNode::getId)).orElseThrow();
                String dcConverterUnitId = dcNodesToUnit.containsKey(refDcNode) ? dcNodesToUnit.get(refDcNode).id() : null;
                writeDCNode(dcBusId, dcBus.getNameOrId(), dcConverterUnitId);
            }
        }
    }

    private void writeDcSwitches() throws XMLStreamException {
        if (!context.isBusBranchExport()) {
            for (DcSwitch dcSwitch : network.getDcSwitches()) {
                String className = dcSwitch.getKind() == DcSwitchKind.BREAKER ? "DCBreaker" : "DCDisconnector";
                String dcSwitchId = context.getNamingStrategy().getCgmesId(dcSwitch);
                DCSwitchEq.write(className, dcSwitchId, dcSwitch.getNameOrId(), writer, context);

                writeDcTerminal(dcSwitch, dcSwitch.getDcNode1(), 1);
                writeDcTerminal(dcSwitch, dcSwitch.getDcNode2(), 2);
            }
        }
    }

    private void writeDcGrounds() throws XMLStreamException {
        for (DcGround dcGround : network.getDcGrounds()) {
            String dcGroundId = context.getNamingStrategy().getCgmesId(dcGround);
            DCGroundEq.write(dcGroundId, dcGround.getNameOrId(), dcGround.getR(), writer, context);

            writeDcTerminal(dcGround.getDcTerminal());
        }
    }

    private void writeDcLineSegments() throws XMLStreamException {
        for (DcLine dcLine : network.getDcLines()) {
            String dcLineId = context.getNamingStrategy().getCgmesId(dcLine);
            DCLineSegmentEq.write(dcLineId, dcLine.getNameOrId(), dcLine.getR(), writer, context);

            writeDcTerminal(dcLine.getDcTerminal1());
            writeDcTerminal(dcLine.getDcTerminal2());
        }
    }

    private void writeAcDcConverters() throws XMLStreamException {
        for (AcDcConverter<?> converter : Stream.concat(network.getLineCommutatedConverterStream(), network.getVoltageSourceConverterStream()).toList()) {
            String dcConverterUnitId = dcConvertersToUnit.get(converter).id();
            String converterId = context.getNamingStrategy().getCgmesId(converter);
            String className = converterClassName(converter);
            String pccTerminalId = null;
            if (converter.getPccTerminal() != converter.getTerminal1()) {
                pccTerminalId = getTerminalId(converter.getPccTerminal(), context);
            }
            String capabilityCurveId = writeVsCapabilityCurve(converter);
            AcDcConverterEq.write(converterId, converter.getNameOrId(), className, converter.getDcTerminal1().getDcNode().getNominalV(),
                    converter.getIdleLoss(), converter.getSwitchingLoss(), converter.getResistiveLoss(), dcConverterUnitId,
                    pccTerminalId, capabilityCurveId, writer, context);

            writeDcTerminal(converter.getDcTerminal1());
            writeDcTerminal(converter.getDcTerminal2());
        }
    }

    private void writeControlAreas(String energyAreaId) throws XMLStreamException {
        // Log a warning if we are a stand-alone network (an IGM) and no control area is being exported
        long numControlAreas = network.getAreaStream().filter(a -> a.getAreaType().equals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE)).count();
        int numSubnetworks = network.getSubnetworks().size();
        if (numControlAreas == 0 && numSubnetworks == 0) {
            LOG.warn("No control area of type interchange is being exported");
        }

        for (Area area : network.getAreas()) {
            if (area.getAreaType().equals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE)) {
                writeControlArea(area, energyAreaId);
            }
        }
    }

    private void writeControlArea(Area controlArea, String energyAreaId) throws XMLStreamException {
        // Original control area identifiers may not respect mRID rules, so we pass it through naming strategy
        // to obtain always valid mRID identifiers
        String controlAreaCgmesId = context.getNamingStrategy().getCgmesId(controlArea.getId());
        String energyIdentCodeEic = controlArea.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).orElse(null);
        ControlAreaEq.write(controlAreaCgmesId, controlArea.getNameOrId(), energyIdentCodeEic, energyAreaId, writer, context);
        for (AreaBoundary areaBoundary : controlArea.getAreaBoundaries()) {
            Optional<TieFlow> tieFlow = TieFlow.from(areaBoundary, context);
            if (tieFlow.isPresent()) {
                TieFlowEq.write(tieFlow.get().id(), controlAreaCgmesId, tieFlow.get().terminalId(), writer, context);
            }
        }
    }

    private record TieFlow(String id, String terminalId) {
        static Optional<TieFlow> from(AreaBoundary areaBoundary, CgmesExportContext context) {
            return areaBoundary.getTerminal().map(terminal -> from(terminal, context))
                    .orElseGet(() -> areaBoundary.getBoundary().flatMap(boundary -> from(boundary, context)));
        }

        static Optional<TieFlow> from(Terminal terminal, CgmesExportContext context) {
            return Optional.of(new TieFlow(
                    context.getNamingStrategy().getCgmesId(refTyped(terminal.getConnectable()), TIE_FLOW),
                    CgmesExportUtil.getTerminalId(terminal, context)));
        }

        static Optional<TieFlow> from(Boundary boundary, CgmesExportContext context) {
            String terminalId = CgmesExportUtil.getBoundaryLineBoundaryTerminalId(boundary.getBoundaryLine(), context);
            if (terminalId != null) {
                return Optional.of(new TieFlow(
                        context.getNamingStrategy().getCgmesId(ref(terminalId), TIE_FLOW),
                        terminalId));
            } else {
                return Optional.empty();
            }
        }
    }

    private void writeTerminals() throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) {
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    writeTerminal(t);
                }
            }
        }

        String[] switchNodesKeys = new String[2];
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                VoltageLevel vl = sw.getVoltageLevel();
                fillSwitchNodeKeys(vl, sw, switchNodesKeys);
                String nodeId1 = mapNodeKey2NodeId.get(switchNodesKeys[0]);
                String terminalId1 = context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL1);
                TerminalEq.write(terminalId1, context.getNamingStrategy().getCgmesId(sw), nodeId1, 1, writer, context);
                String nodeId2 = mapNodeKey2NodeId.get(switchNodesKeys[1]);
                String terminalId2 = context.getNamingStrategy().getCgmesIdFromAlias(sw, ALIAS_TERMINAL2);
                TerminalEq.write(terminalId2, context.getNamingStrategy().getCgmesId(sw), nodeId2, 2, writer, context);
            }
        }
    }

    private void writeTerminal(Terminal t) throws XMLStreamException {
        int sequenceNumber = getTerminalSequenceNumber(t);
        String terminalId = getTerminalId(t, context);
        String equipmentId = context.getNamingStrategy().getCgmesId(t.getConnectable());
        String nodeId = connectivityNodeId(t);

        TerminalEq.write(terminalId, equipmentId, nodeId, sequenceNumber, writer, context);
    }

    private String connectivityNodeId(Terminal terminal) {
        String key;
        if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER && !context.isBusBranchExport()) {
            key = buildNodeKey(terminal.getVoltageLevel(), terminal.getNodeBreakerView().getNode());
        } else {
            key = buildNodeKey(terminal.getBusBreakerView().getConnectableBus());
        }
        return mapNodeKey2NodeId.get(key);
    }

    private List<List<Integer>> getVoltageLevelAdjacencies(VoltageLevel.NodeBreakerView nodeBreakerView) {
        List<List<Integer>> adjacencies = new ArrayList<>();
        Set<Integer> visitedNodes = new HashSet<>();

        // Build a list of adjacent nodes.
        for (Integer node : nodeBreakerView.getNodes()) {
            if (!visitedNodes.contains(node)) {
                List<Integer> adjacentNodes = new ArrayList<>(1);
                visitedNodes.add(node);
                adjacentNodes.add(node);
                nodeBreakerView.traverse(node, (node1, sw, node2) -> {
                    // Nodes are adjacent if they are connected through an internal connection or non-exported switch.
                    if (sw == null || !context.isExportedEquipment(sw)) {
                        visitedNodes.add(node2);
                        adjacentNodes.add(node2);
                        return TraverseResult.CONTINUE;
                    }
                    return TraverseResult.TERMINATE_PATH;
                });

                adjacencies.add(adjacentNodes);
            }
        }

        // Sort adjacencies so that nodes that have been created first are exported first.
        adjacencies.sort(Comparator.comparing(Collections::min));

        return adjacencies;
    }

    private record DCConverterUnit(String id, String name, String substation) { }

    private void getAcDcConvertersUnit() {
        Stream.concat(network.getLineCommutatedConverterStream(), network.getVoltageSourceConverterStream())
                .forEach(converter -> {
                    // Multiple ACDCConverter can be contained in the same DCConverterUnit if this property value is common.
                    String unitId = converter.getProperty(PROPERTY_DC_CONVERTER_UNIT,
                            context.getNamingStrategy().getCgmesId(refTyped(converter), DC_CONVERTER_UNIT));

                    // Only create a new DCConverterUnit if it hasn't been created already for another AcDcConverter.
                    DCConverterUnit dcConverterUnit = dcConvertersToUnit.values().stream()
                            .filter(u -> u.id().equals(unitId))
                            .findFirst()
                            .orElseGet(() -> new DCConverterUnit(
                                    unitId,
                                    converter.getNameOrId() + " Unit",
                                    context.getNamingStrategy().getCgmesId(converter.getTerminal1().getVoltageLevel().getNullableSubstation())
                            ));

                    dcConvertersToUnit.put(converter, dcConverterUnit);
                });
    }

    private void getDcNodesConverterUnit() {
        // Build the DcNode to AcDcConverter associations by traversing the dc network
        // using breadth first search starting from ACDCConverter DCNodes.
        List<DCEquipment> converters = getDCEquipmentConverters();
        List<DCEquipment> dcSwitches = getDCEquipmentSwitches();
        Map<String, String> dcNodesConverter = new HashMap<>();
        Set<String> visitedDcNodes = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        converters.forEach(converter -> {
            if (!dcNodesConverter.containsKey(converter.node1())) {
                dcNodesConverter.put(converter.node1(), converter.id());
                queue.add(converter.node1());
            }
            if (!dcNodesConverter.containsKey(converter.node2())) {
                dcNodesConverter.put(converter.node2(), converter.id());
                queue.add(converter.node2());
            }
        });
        while (!queue.isEmpty()) {
            String node = queue.poll();
            String converter = dcNodesConverter.get(node);
            List<String> adjacentNodes = getAdjacentNodes(dcSwitches, node);
            for (String adjacentNode : adjacentNodes) {
                if (!visitedDcNodes.contains(adjacentNode)) {
                    visitedDcNodes.add(adjacentNode);
                    dcNodesConverter.put(adjacentNode, converter);
                    queue.add(adjacentNode);
                }
            }
        }

        // Build the DcNode to DCConverterUnit associations.
        dcNodesToUnit.putAll(dcNodesConverter.entrySet().stream()
                .collect(Collectors.toMap(e -> network.getDcNode(e.getKey()),
                        e -> dcConvertersToUnit.get(getAcDcConverter(e.getValue())))));
    }

    private AcDcConverter<?> getAcDcConverter(String converterId) {
        LineCommutatedConverter lcc = network.getLineCommutatedConverter(converterId);
        if (lcc != null) {
            return lcc;
        }
        return network.getVoltageSourceConverter(converterId);
    }

    private List<DCEquipment> getDCEquipmentConverters() {
        return Stream.concat(network.getLineCommutatedConverterStream(), network.getVoltageSourceConverterStream())
                .sorted(Comparator.comparing(AcDcConverter::getId))
                .map(c -> new DCEquipment(
                        c.getId(),
                        CgmesNames.ACDC_CONVERTER,
                        c.getDcTerminal1().getDcNode().getId(),
                        c.getDcTerminal2().getDcNode().getId()))
                .toList();
    }

    private List<DCEquipment> getDCEquipmentSwitches() {
        return network.getDcSwitchStream()
                .sorted(Comparator.comparing(DcSwitch::getId))
                .map(s -> new DCEquipment(
                        s.getId(),
                        CgmesNames.DC_SWITCH,
                        s.getDcNode1().getId(),
                        s.getDcNode2().getId()))
                .toList();
    }

    private List<String> getAdjacentNodes(List<DCEquipment> dcEquipments, String node) {
        // Get the list of nodes adjacent through the given dc equipments.
        return dcEquipments.stream()
                .filter(eq -> node.equals(eq.node1()) || node.equals(eq.node2()))
                .map(eq -> node.equals(eq.node1()) ? eq.node2() : eq.node1())
                .filter(Objects::nonNull)
                .toList();
    }
}
