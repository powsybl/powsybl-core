/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.NamingStrategy;
import com.powsybl.cgmes.conversion.export.elements.*;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class EquipmentExport {

    private static final String AC_DC_CONVERTER_DC_TERMINAL = "ACDCConverterDCTerminal";
    private static final String CONNECTIVITY_NODE_SUFFIX = "CN";
    private static final String PHASE_TAP_CHANGER_REGULATION_MODE_ACTIVE_POWER = "activePower";
    private static final String PHASE_TAP_CHANGER_REGULATION_MODE_CURRENT_FLOW = "currentFlow";
    private static final String RATIO_TAP_CHANGER_REGULATION_MODE_VOLTAGE = "voltage";
    private static final Logger LOG = LoggerFactory.getLogger(EquipmentExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        context.setExportEquipment(true);
        try {
            String cimNamespace = context.getCim().getNamespace();
            String euNamespace = context.getCim().getEuNamespace();
            String limitValueAttributeName = context.getCim().getLimitValueAttributeName();
            String limitTypeAttributeName = context.getCim().getLimitTypeAttributeName();
            String limitKindClassName = context.getCim().getLimitKindClassName();
            boolean writeInfiniteDuration = context.getCim().writeLimitInfiniteDuration();
            boolean writeInitialP = context.getCim().writeGeneratingUnitInitialP();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), euNamespace, writer);

            // TODO fill EQ Model Description
            if (context.getCimVersion() >= 16) {
                ModelDescriptionEq.write(writer, context.getEqModelDescription(), context);
            }

            Map<String, String> mapNodeKey2NodeId = new HashMap<>();
            Map<Terminal, String> mapTerminal2Id = new HashMap<>();
            Set<String> regulatingControlsWritten = new HashSet<>();
            Set<Double> exportedBaseVoltagesByNominalV = new HashSet<>();

            writeConnectivity(network, mapNodeKey2NodeId, cimNamespace, writer, context);
            writeTerminals(network, mapTerminal2Id, mapNodeKey2NodeId, cimNamespace, writer, context);
            writeSwitches(network, cimNamespace, writer, context);

            writeSubstations(network, cimNamespace, writer, context);
            writeVoltageLevels(network, cimNamespace, writer, context, exportedBaseVoltagesByNominalV);
            writeBusbarSections(network, cimNamespace, writer, context);
            writeLoads(network, cimNamespace, writer, context);
            writeGenerators(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, writeInitialP, writer, context);
            writeShuntCompensators(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, writer, context);
            writeStaticVarCompensators(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, writer, context);
            writeLines(network, mapTerminal2Id, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            writeTwoWindingsTransformers(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            writeThreeWindingsTransformers(network, mapTerminal2Id, regulatingControlsWritten, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);

            writeDanglingLines(network, mapTerminal2Id, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context, exportedBaseVoltagesByNominalV);
            writeHvdcLines(network, mapTerminal2Id, mapNodeKey2NodeId, cimNamespace, writer, context);

            writeControlAreas(network, mapTerminal2Id, cimNamespace, euNamespace, writer, context);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeConnectivity(Network network, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                writeNodes(vl, new VoltageLevelAdjacency(vl, context), mapNodeKey2NodeId, cimNamespace, writer, context);
            } else {
                writeBuses(vl, mapNodeKey2NodeId, cimNamespace, writer, context);
            }
            writeSwitchesConnectivity(vl, mapNodeKey2NodeId, cimNamespace, writer, context);
        }
        writeBusbarSectionsConnectivity(network, mapNodeKey2NodeId, cimNamespace, writer, context);
    }

    private static void writeSwitchesConnectivity(VoltageLevel vl, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        String[] nodeKeys = new String[2];
        for (Switch sw : vl.getSwitches()) {
            fillSwitchNodeKeys(vl, sw, nodeKeys);
            // We have to go through all switches, even if they are not exported as equipment,
            // to be sure that all required mappings between IIDM node number and CGMES Connectivity Node are created
            writeSwitchConnectivity(nodeKeys[0], vl, mapNodeKey2NodeId, cimNamespace, writer, context);
            writeSwitchConnectivity(nodeKeys[1], vl, mapNodeKey2NodeId, cimNamespace, writer, context);
        }
    }

    private static void writeSwitchConnectivity(String nodeKey, VoltageLevel vl, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        mapNodeKey2NodeId.computeIfAbsent(nodeKey, k -> {
            try {
                String node = CgmesExportUtil.getUniqueId();
                ConnectivityNodeEq.write(node, nodeKey, context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
                return node;
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    private static String buildNodeKey(VoltageLevel vl, int node) {
        return vl.getId() + "_" + node + "_" + CONNECTIVITY_NODE_SUFFIX;
    }

    private static String  buildNodeKey(Bus bus) {
        return bus.getId() + "_" + CONNECTIVITY_NODE_SUFFIX;
    }

    private static void writeBusbarSectionsConnectivity(Network network, Map <String, String> mapNodeKey2NodeId, String cimNamespace,
                                                        XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (BusbarSection bus : network.getBusbarSections()) {
            String connectivityNodeId = connectivityNodeId(mapNodeKey2NodeId, bus.getTerminal());
            if (connectivityNodeId == null) {
                VoltageLevel vl = bus.getTerminal().getVoltageLevel();
                String node = CgmesExportUtil.getUniqueId();
                ConnectivityNodeEq.write(node, bus.getNameOrId(), context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
                String key = buildNodeKey(vl, bus.getTerminal().getNodeBreakerView().getNode());
                mapNodeKey2NodeId.put(key, node);
            }
        }
    }

    private static void writeNodes(VoltageLevel vl, VoltageLevelAdjacency vlAdjacencies, Map <String, String> mapNodeKey2NodeId, String cimNamespace,
                                   XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (List<Integer> nodes : vlAdjacencies.getNodes()) {
            String cgmesNodeId = CgmesExportUtil.getUniqueId();
            ConnectivityNodeEq.write(cgmesNodeId, CgmesExportUtil.format(nodes.get(0)), context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
            for (Integer nodeNumber : nodes) {
                mapNodeKey2NodeId.put(buildNodeKey(vl, nodeNumber), cgmesNodeId);
            }
        }
    }

    private static void writeBuses(VoltageLevel vl, Map <String, String> mapNodeKey2NodeId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context)throws XMLStreamException {
        for (Bus bus : vl.getBusBreakerView().getBuses()) {
            String cgmesNodeId = context.getNamingStrategy().getCgmesId(bus, CONNECTIVITY_NODE_SUFFIX);
            ConnectivityNodeEq.write(cgmesNodeId, bus.getNameOrId(), context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
            mapNodeKey2NodeId.put(buildNodeKey(bus), cgmesNodeId);
        }
    }

    private static void writeSwitches(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context)throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            if (context.isExportedEquipment(sw)) {
                VoltageLevel vl = sw.getVoltageLevel();
                SwitchEq.write(context.getNamingStrategy().getCgmesId(sw), sw.getNameOrId(), sw.getKind(), context.getNamingStrategy().getCgmesId(vl), sw.isRetained(), cimNamespace, writer);
            }
        }
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
            writeGeographicalRegion(cgmesRegionId, context.getRegionName(geographicalRegionId), cimNamespace, writer);
        }
        List<String> writtenSubRegions = new ArrayList<>();
        for (Substation substation : network.getSubstations()) {
            String subGeographicalRegionId = context.getNamingStrategy().getCgmesIdFromProperty(substation, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId");
            String geographicalRegionId = context.getNamingStrategy().getCgmesIdFromProperty(substation, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId");
            if (!writtenSubRegions.contains(subGeographicalRegionId)) {
                writeSubGeographicalRegion(subGeographicalRegionId, context.getSubRegionName(subGeographicalRegionId), geographicalRegionId, cimNamespace, writer);
                writtenSubRegions.add(subGeographicalRegionId);
            }
            SubstationEq.write(context.getNamingStrategy().getCgmesId(substation), substation.getNameOrId(), subGeographicalRegionId, cimNamespace, writer);
        }
    }

    private static void writeGeographicalRegion(String geographicalRegionId, String geoName, String cimNamespace, XMLStreamWriter writer) {
        try {
            GeographicalRegionEq.write(geographicalRegionId, geoName, cimNamespace, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSubGeographicalRegion(String subGeographicalRegionId, String subGeographicalRegionName, String geographicalRegionId, String cimNamespace, XMLStreamWriter writer) {
        try {
            SubGeographicalRegionEq.write(subGeographicalRegionId, subGeographicalRegionName, geographicalRegionId, cimNamespace, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeVoltageLevels(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context, Set<Double> exportedBaseVoltagesByNominalV) throws XMLStreamException {
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            double nominalV = voltageLevel.getNominalV();
            BaseVoltageMapping.BaseVoltageSource baseVoltage = context.getBaseVoltageByNominalVoltage(nominalV);
            if (!exportedBaseVoltagesByNominalV.contains(nominalV) && baseVoltage.getSource().equals(Source.IGM)) {
                BaseVoltageEq.write(baseVoltage.getId(), nominalV, cimNamespace, writer);
                exportedBaseVoltagesByNominalV.add(nominalV);
            }
            VoltageLevelEq.write(context.getNamingStrategy().getCgmesId(voltageLevel), voltageLevel.getNameOrId(), voltageLevel.getLowVoltageLimit(), voltageLevel.getHighVoltageLimit(),
                    context.getNamingStrategy().getCgmesId(voltageLevel.getNullableSubstation()), baseVoltage.getId(), cimNamespace, writer);
        }
    }

    private static void writeBusbarSections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (BusbarSection bbs : network.getBusbarSections()) {
            BusbarSectionEq.write(context.getNamingStrategy().getCgmesId(bbs), bbs.getNameOrId(),
                    context.getNamingStrategy().getCgmesId(bbs.getTerminal().getVoltageLevel()),
                    context.getBaseVoltageByNominalVoltage(bbs.getTerminal().getVoltageLevel().getNominalV()).getId(), cimNamespace, writer);
        }
    }

    private static void writeLoads(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            if (context.isExportedEquipment(load)) {
                EnergyConsumerEq.write(context.getNamingStrategy().getCgmesId(load),
                    load.getNameOrId(), load.getExtension(LoadDetail.class),
                    context.getNamingStrategy().getCgmesId(load.getTerminal().getVoltageLevel()),
                    cimNamespace, writer);
            }
        }
    }

    private static void writeGenerators(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace, boolean writeInitialP,
                                        XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Multiple synchronous machines may be grouped in the same generating unit
        // We have to write each generating unit only once
        Set<String> generatingUnitsWritten = new HashSet<>();
        for (Generator generator : network.getGenerators()) {
            String generatingUnit = context.getNamingStrategy().getCgmesIdFromProperty(generator, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            String regulatingControlId = RegulatingControlEq.writeKindVoltage(generator, exportedTerminalId(mapTerminal2Id, generator.getRegulatingTerminal()), regulatingControlsWritten, cimNamespace, writer, context);
            String reactiveLimitsId = null;
            double minQ = 0.0;
            double maxQ = 0.0;
            switch (generator.getReactiveLimits().getKind()) {
                case CURVE:
                    reactiveLimitsId = CgmesExportUtil.getUniqueId();
                    ReactiveCapabilityCurve curve = generator.getReactiveLimits(ReactiveCapabilityCurve.class);
                    for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                        CurveDataEq.write(CgmesExportUtil.getUniqueId(), point.getP(), point.getMinQ(), point.getMaxQ(), reactiveLimitsId, cimNamespace, writer);
                    }
                    String reactiveCapabilityCurveName = "RCC_" + generator.getNameOrId();
                    ReactiveCapabilityCurveEq.write(reactiveLimitsId, reactiveCapabilityCurveName, generator, cimNamespace, writer);
                    break;

                case MIN_MAX:
                    minQ = generator.getReactiveLimits(MinMaxReactiveLimits.class).getMinQ();
                    maxQ = generator.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ();
                    break;

                default:
                    throw new PowsyblException("Unexpected type of ReactiveLimits on the generator " + generator.getNameOrId());
            }
            SynchronousMachineEq.write(context.getNamingStrategy().getCgmesId(generator), generator.getNameOrId(), generatingUnit, regulatingControlId, reactiveLimitsId, minQ, maxQ, generator.getRatedS(), cimNamespace, writer);
            if (!generatingUnitsWritten.contains(generatingUnit)) {
                // We have not preserved the names of generating units
                // We name generating units based on the first machine found
                String generatingUnitName = "GU_" + generator.getNameOrId();
                GeneratingUnitEq.write(generatingUnit, generatingUnitName, generator.getEnergySource(), generator.getMinP(), generator.getMaxP(), generator.getTargetP(), cimNamespace, writeInitialP, writer);
                generatingUnitsWritten.add(generatingUnit);
            }
        }
    }

    private static void writeShuntCompensators(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            double bPerSection = 0.0;
            double gPerSection = Double.NaN;
            if (s.getModelType().equals(ShuntCompensatorModelType.LINEAR)) {
                bPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getBPerSection();
                gPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getGPerSection();
            }
            String regulatingControlId = RegulatingControlEq.writeKindVoltage(s, exportedTerminalId(mapTerminal2Id, s.getRegulatingTerminal()), regulatingControlsWritten, cimNamespace, writer, context);
            ShuntCompensatorEq.write(context.getNamingStrategy().getCgmesId(s), s.getNameOrId(), s.getSectionCount(), s.getMaximumSectionCount(), s.getTerminal().getVoltageLevel().getNominalV(), s.getModelType(), bPerSection, gPerSection, regulatingControlId, cimNamespace, writer);
            if (s.getModelType().equals(ShuntCompensatorModelType.NON_LINEAR)) {
                double b = 0.0;
                double g = 0.0;
                for (int section = 1; section <= s.getMaximumSectionCount(); section++) {
                    ShuntCompensatorEq.writePoint(CgmesExportUtil.getUniqueId(), context.getNamingStrategy().getCgmesId(s), section, s.getB(section) - b, s.getG(section) - g, cimNamespace, writer);
                    b = s.getB(section);
                    g = s.getG(section);
                }
            }
        }
    }

    private static void writeStaticVarCompensators(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace,
                                                   XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String regulatingControlId = RegulatingControlEq.writeKindVoltage(svc, exportedTerminalId(mapTerminal2Id, svc.getRegulatingTerminal()), regulatingControlsWritten, cimNamespace, writer, context);
            StaticVarCompensatorEq.write(context.getNamingStrategy().getCgmesId(svc), svc.getNameOrId(), regulatingControlId, 1 / svc.getBmin(), 1 / svc.getBmax(), svc.getExtension(VoltagePerReactivePowerControl.class), svc.getRegulationMode(), svc.getVoltageSetpoint(), cimNamespace, writer);
        }
    }

    private static void writeLines(Network network, Map<Terminal, String> mapTerminal2Id, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Line line : network.getLines()) {
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(line), line.getNameOrId(), line.getR(), line.getX(), line.getG1() + line.getG2(), line.getB1() + line.getB2(), cimNamespace, writer);
            writeBranchLimits(line, exportedTerminalId(mapTerminal2Id, line.getTerminal1()), exportedTerminalId(mapTerminal2Id, line.getTerminal2()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static void writeTwoWindingsTransformers(Network network, Map<Terminal, String> mapTerminal2Id, Set<String> regulatingControlsWritten, String cimNamespace,
                                                    String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(), cimNamespace, writer);
            String end1Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 1);
            // structural ratio at end1
            double a0 = twt.getRatedU1() / twt.getRatedU2();
            // move structural ratio from end1 to end2
            double a02 = a0 * a0;
            double r = twt.getR() * a02;
            double x = twt.getX() * a02;
            double g = twt.getG() / a02;
            double b = twt.getB() / a02;
            PowerTransformerEq.writeEnd(end1Id, twt.getNameOrId() + "_1", context.getNamingStrategy().getCgmesId(twt), 1, r, x, g, b, twt.getRatedU1(), exportedTerminalId(mapTerminal2Id, twt.getTerminal1()), cimNamespace, writer);
            String end2Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 2);
            PowerTransformerEq.writeEnd(end2Id, twt.getNameOrId() + "_2", context.getNamingStrategy().getCgmesId(twt), 2, 0.0, 0.0, 0.0, 0.0, twt.getRatedU2(), exportedTerminalId(mapTerminal2Id, twt.getTerminal2()), cimNamespace, writer);

            // Export tap changers:
            // We are exporting the tap changer as it is modelled in IIDM, always at end 1
            int endNumber = 1;
            // IIDM model always has tap changers (ratio and/or phase) at end 1, and only at end 1.
            // We have to adjust the aliases for (potential) original tap changers coming from end 1, end 2.
            // Potential tc2 is always converted to a tc at end 1.
            // If both tc1 and tc2 were present, tc2 was combined during import (fixed at current step) with tc1. Steps from tc1 were kept.
            // If we only had tc2, it mas moved to end 1.
            //
            // When we had only tc2, the alias for tc1 if we do EQ export should contain the identifier of original tc2.
            // In the rest of situations, we keep the same id under alias for tc1.
            adjustTapChangerAliases2wt(twt, twt.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER);
            adjustTapChangerAliases2wt(twt, twt.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER);
            writePhaseTapChanger(twt, twt.getPhaseTapChanger(), twt.getNameOrId(), endNumber, end1Id, twt.getTerminal1().getVoltageLevel().getNominalV(), regulatingControlsWritten, cimNamespace, writer, context);
            writeRatioTapChanger(twt, twt.getRatioTapChanger(), twt.getNameOrId(), endNumber, end1Id, regulatingControlsWritten, cimNamespace, writer, context);
            writeBranchLimits(twt, exportedTerminalId(mapTerminal2Id, twt.getTerminal1()), exportedTerminalId(mapTerminal2Id, twt.getTerminal2()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static void adjustTapChangerAliases2wt(TwoWindingsTransformer transformer, TapChanger<?, ?> tc, String tapChangerKind) {
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
                                                      String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(), cimNamespace, writer);
            double ratedU0 = twt.getLeg1().getRatedU();
            String end1Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 1);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_1", end1Id, 1, twt.getLeg1(), ratedU0, exportedTerminalId(mapTerminal2Id, twt.getLeg1().getTerminal()), regulatingControlsWritten, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            String end2Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 2);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_2", end2Id, 2, twt.getLeg2(), ratedU0, exportedTerminalId(mapTerminal2Id, twt.getLeg2().getTerminal()), regulatingControlsWritten, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            String end3Id = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 3);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_3", end3Id, 3, twt.getLeg3(), ratedU0, exportedTerminalId(mapTerminal2Id, twt.getLeg3().getTerminal()), regulatingControlsWritten, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
        }
    }

    private static void writeThreeWindingsTransformerEnd(ThreeWindingsTransformer twt, String twtId, String twtName, String endId, int endNumber, ThreeWindingsTransformer.Leg leg, double ratedU0, String terminalId, Set<String> regulatingControlsWritten, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // structural ratio at end1
        double a0 = leg.getRatedU() / ratedU0;
        // move structural ratio from end1 to end2
        double a02 = a0 * a0;
        double r = leg.getR() * a02;
        double x = leg.getX() * a02;
        double g = leg.getG() / a02;
        double b = leg.getB() / a02;
        PowerTransformerEq.writeEnd(endId, twtName, twtId, endNumber, r, x, g, b, leg.getRatedU(), terminalId, cimNamespace, writer);
        writePhaseTapChanger(twt, leg.getPhaseTapChanger(), twtName, endNumber, endId, leg.getTerminal().getVoltageLevel().getNominalV(), regulatingControlsWritten, cimNamespace, writer, context);
        writeRatioTapChanger(twt, leg.getRatioTapChanger(), twtName, endNumber, endId, regulatingControlsWritten, cimNamespace, writer, context);
        writeFlowsLimits(leg, terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
    }

    private static <C extends Connectable<C>> void writePhaseTapChanger(C eq, PhaseTapChanger ptc, String twtName, int endNumber, String endId, double neutralU, Set<String> regulatingControlsWritten, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        if (ptc != null) {
            String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber;
            String tapChangerId = eq.getAliasFromType(aliasType).orElseThrow();
            String cgmesTapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(eq, aliasType);

            int neutralStep = getPhaseTapChangerNeutralStep(ptc);
            Optional<String> regulatingControlId = getTapChangerControlId(eq, tapChangerId);
            String cgmesRegulatingControlId = null;
            if (regulatingControlId.isPresent()) {
                String mode = getPhaseTapChangerRegulationMode(ptc);
                // Only export the regulating control if mode is valid
                if (mode != null) {
                    String controlName = twtName + "_PTC_RC";
                    String terminalId = CgmesExportUtil.getTerminalId(ptc.getRegulationTerminal(), context);
                    cgmesRegulatingControlId = context.getNamingStrategy().getCgmesId(regulatingControlId.get());
                    if (!regulatingControlsWritten.contains(cgmesRegulatingControlId)) {
                        TapChangerEq.writeControl(cgmesRegulatingControlId, controlName, mode, terminalId, cimNamespace, writer);
                        regulatingControlsWritten.add(cgmesRegulatingControlId);
                    }
                }
            }
            String phaseTapChangerTableId = CgmesExportUtil.getUniqueId();
            // If we write the EQ, we will always write the Tap Changer as tabular
            // We reset the phase tap changer type stored in the extensions
            String typeTabular = CgmesNames.PHASE_TAP_CHANGER_TABULAR;
            CgmesExportUtil.setCgmesTapChangerType(eq, tapChangerId, typeTabular);

            TapChangerEq.writePhase(typeTabular, cgmesTapChangerId, twtName + "_PTC", endId, ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, ptc.getTapPosition(), neutralU, false, phaseTapChangerTableId, cgmesRegulatingControlId, cimNamespace, writer);
            TapChangerEq.writePhaseTable(phaseTapChangerTableId, twtName + "_TABLE", cimNamespace, writer);
            for (Map.Entry<Integer, PhaseTapChangerStep> step : ptc.getAllSteps().entrySet()) {
                TapChangerEq.writePhaseTablePoint(CgmesExportUtil.getUniqueId(), phaseTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), -step.getValue().getAlpha(), step.getKey(), cimNamespace, writer);
            }
        }
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

    private static String getPhaseTapChangerRegulationMode(PhaseTapChanger ptc) {
        switch (ptc.getRegulationMode()) {
            case CURRENT_LIMITER:
                return PHASE_TAP_CHANGER_REGULATION_MODE_CURRENT_FLOW;
            case ACTIVE_POWER_CONTROL:
                return PHASE_TAP_CHANGER_REGULATION_MODE_ACTIVE_POWER;
            case FIXED_TAP:
            default:
                return null;
        }
    }

    private static int getPhaseTapChangerNeutralStep(PhaseTapChanger ptc) {
        int neutralStep = ptc.getLowTapPosition();
        while (ptc.getStep(neutralStep).getAlpha() != 0.0) {
            neutralStep++;
            if (neutralStep > ptc.getHighTapPosition()) {
                return ptc.getHighTapPosition();
            }
        }
        return neutralStep;
    }

    private static <C extends Connectable<C>> void writeRatioTapChanger(C eq, RatioTapChanger rtc, String twtName, int endNumber, String endId, Set<String> regulatingControlsWritten, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        if (rtc != null) {
            String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber;
            String tapChangerId = eq.getAliasFromType(aliasType).orElseThrow();
            String cgmesTapChangerId = context.getNamingStrategy().getCgmesIdFromAlias(eq, aliasType);

            int neutralStep = getRatioTapChangerNeutralStep(rtc);
            double stepVoltageIncrement = 100.0 * (1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0) / (rtc.getLowTapPosition() - neutralStep);
            String ratioTapChangerTableId = CgmesExportUtil.getUniqueId();
            Optional<String> regulatingControlId = getTapChangerControlId(eq, tapChangerId);
            String cgmesRegulatingControlId = null;
            if (regulatingControlId.isPresent()) {
                String controlName = twtName + "_RTC_RC";
                String terminalId = CgmesExportUtil.getTerminalId(rtc.getRegulationTerminal(), context);
                cgmesRegulatingControlId = context.getNamingStrategy().getCgmesId(regulatingControlId.get());
                if (!regulatingControlsWritten.contains(cgmesRegulatingControlId)) {
                    // Regulating control mode is always "voltage"
                    TapChangerEq.writeControl(cgmesRegulatingControlId, controlName, RATIO_TAP_CHANGER_REGULATION_MODE_VOLTAGE, terminalId, cimNamespace, writer);
                    regulatingControlsWritten.add(cgmesRegulatingControlId);
                }
            }
            TapChangerEq.writeRatio(cgmesTapChangerId, twtName + "_RTC", endId, rtc.getLowTapPosition(), rtc.getHighTapPosition(), neutralStep, rtc.getTapPosition(), rtc.getTargetV(), rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement, ratioTapChangerTableId, cgmesRegulatingControlId, cimNamespace, writer);
            TapChangerEq.writeRatioTable(ratioTapChangerTableId, twtName + "_TABLE", cimNamespace, writer);
            for (Map.Entry<Integer, RatioTapChangerStep> step : rtc.getAllSteps().entrySet()) {
                TapChangerEq.writeRatioTablePoint(CgmesExportUtil.getUniqueId(), ratioTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), step.getKey(), cimNamespace, writer);
            }

        }
    }

    private static int getRatioTapChangerNeutralStep(RatioTapChanger rtc) {
        int neutralStep = rtc.getLowTapPosition();
        while (rtc.getStep(neutralStep).getRho() != 1.0) {
            neutralStep++;
            if (neutralStep > rtc.getHighTapPosition()) {
                return rtc.getHighTapPosition();
            }
        }
        return neutralStep;
    }

    private static void writeDanglingLines(Network network, Map<Terminal, String> mapTerminal2Id, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName,
                                           String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context, Set<Double> exportedBaseVoltagesByNominalV) throws XMLStreamException {
        for (DanglingLine danglingLine : network.getDanglingLines()) {

            String substationId = writeDanglingLineSubstation(danglingLine, cimNamespace, writer);
            String baseVoltageId = writeDanglingLineBaseVoltage(danglingLine, cimNamespace, writer, context, exportedBaseVoltagesByNominalV);
            String voltageLevelId = writeDanglingLineVoltageLevel(danglingLine, substationId, baseVoltageId, cimNamespace, writer);
            String connectivityNodeId = writeDanglingLineConnectivity(danglingLine, voltageLevelId, cimNamespace, writer, context);

            // New Load
            String loadId = CgmesExportUtil.getUniqueId();
            EnergyConsumerEq.write(loadId, danglingLine.getNameOrId() + "_LOAD", null, voltageLevelId, cimNamespace, writer);
            TerminalEq.write(CgmesExportUtil.getUniqueId(), loadId, connectivityNodeId, 1, cimNamespace, writer);

            // New Equivalent Injection
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
            String equivalentInjectionId = context.getNamingStrategy().getCgmesIdFromAlias(danglingLine, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
            EquivalentInjectionEq.write(equivalentInjectionId, danglingLine.getNameOrId() + "_EI", danglingLine.getGeneration() != null, danglingLine.getGeneration() != null, minP, maxP, minQ, maxQ, baseVoltageId, cimNamespace, writer);
            String equivalentInjectionTerminalId = context.getNamingStrategy().getCgmesIdFromAlias(danglingLine, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            TerminalEq.write(equivalentInjectionTerminalId, equivalentInjectionId, connectivityNodeId, 1, cimNamespace, writer);

            // Cast the danglingLine to an AcLineSegment
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(danglingLine), danglingLine.getNameOrId() + "_DL", danglingLine.getR(), danglingLine.getX(), danglingLine.getG(), danglingLine.getB(), cimNamespace, writer);
            writeFlowsLimits(danglingLine, exportedTerminalId(mapTerminal2Id, danglingLine.getTerminal()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static String writeDanglingLineSubstation(DanglingLine danglingLine, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // New Substation
        String geographicalRegionId = CgmesExportUtil.getUniqueId();
        GeographicalRegionEq.write(geographicalRegionId, danglingLine.getNameOrId() + "_GR", cimNamespace, writer);
        String subGeographicalRegionId = CgmesExportUtil.getUniqueId();
        SubGeographicalRegionEq.write(subGeographicalRegionId, danglingLine.getNameOrId() + "_SGR", geographicalRegionId, cimNamespace, writer);
        String substationId = CgmesExportUtil.getUniqueId();
        SubstationEq.write(substationId, danglingLine.getNameOrId() + "_SUBSTATION", subGeographicalRegionId, cimNamespace, writer);

        return substationId;
    }

    private static String writeDanglingLineBaseVoltage(DanglingLine danglingLine, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context, Set<Double> exportedBaseVoltagesByNominalV) throws XMLStreamException {
        double nominalV = danglingLine.getTerminal().getVoltageLevel().getNominalV();
        BaseVoltageMapping.BaseVoltageSource baseVoltage = context.getBaseVoltageByNominalVoltage(nominalV);
        if (!exportedBaseVoltagesByNominalV.contains(nominalV) && baseVoltage.getSource().equals(Source.IGM)) {
            BaseVoltageEq.write(baseVoltage.getId(), nominalV, cimNamespace, writer);
            exportedBaseVoltagesByNominalV.add(nominalV);
        }

        return baseVoltage.getId();
    }

    private static String writeDanglingLineVoltageLevel(DanglingLine danglingLine, String substationId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // New VoltageLevel
        String voltageLevelId = CgmesExportUtil.getUniqueId();
        VoltageLevelEq.write(voltageLevelId, danglingLine.getNameOrId() + "_VL", Double.NaN, Double.NaN, substationId, baseVoltageId, cimNamespace, writer);

        return voltageLevelId;
    }

    private static String writeDanglingLineConnectivity(DanglingLine danglingLine, String voltageLevelId, String cimNamespace, XMLStreamWriter writer,
                                                        CgmesExportContext context) throws XMLStreamException {
        // New ConnectivityNode
        String connectivityNodeId = CgmesExportUtil.getUniqueId();
        ConnectivityNodeEq.write(connectivityNodeId, danglingLine.getNameOrId() + "_NODE", voltageLevelId, cimNamespace, writer);
        // New Terminal
        String terminalId = context.getNamingStrategy().getCgmesIdFromAlias(danglingLine, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary");
        TerminalEq.write(terminalId, context.getNamingStrategy().getCgmesId(danglingLine), connectivityNodeId, 2, cimNamespace, writer);

        return connectivityNodeId;
    }

    private static void writeBranchLimits(Branch<?> branch, String terminalId1, String terminalId2, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer) throws XMLStreamException {
        Optional<ActivePowerLimits> activePowerLimits1 = branch.getActivePowerLimits1();
        if (activePowerLimits1.isPresent()) {
            writeLoadingLimits(activePowerLimits1.get(), terminalId1, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        Optional<ActivePowerLimits> activePowerLimits2 = branch.getActivePowerLimits2();
        if (activePowerLimits2.isPresent()) {
            writeLoadingLimits(activePowerLimits2.get(), terminalId2, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        Optional<ApparentPowerLimits> apparentPowerLimits1 = branch.getApparentPowerLimits1();
        if (apparentPowerLimits1.isPresent()) {
            writeLoadingLimits(apparentPowerLimits1.get(), terminalId1, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        Optional<ApparentPowerLimits> apparentPowerLimits2 = branch.getApparentPowerLimits2();
        if (apparentPowerLimits2.isPresent()) {
            writeLoadingLimits(apparentPowerLimits2.get(), terminalId2, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        Optional<CurrentLimits> currentLimits1 = branch.getCurrentLimits1();
        if (currentLimits1.isPresent()) {
            writeLoadingLimits(currentLimits1.get(), terminalId1, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        Optional<CurrentLimits> currentLimits2 = branch.getCurrentLimits2();
        if (currentLimits2.isPresent()) {
            writeLoadingLimits(currentLimits2.get(), terminalId2, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static void writeFlowsLimits(FlowsLimitsHolder holder, String terminalId, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer) throws XMLStreamException {
        Optional<ActivePowerLimits> activePowerLimits = holder.getActivePowerLimits();
        if (activePowerLimits.isPresent()) {
            writeLoadingLimits(activePowerLimits.get(), terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        Optional<ApparentPowerLimits> apparentPowerLimits = holder.getApparentPowerLimits();
        if (apparentPowerLimits.isPresent()) {
            writeLoadingLimits(apparentPowerLimits.get(), terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        Optional<CurrentLimits> currentLimits = holder.getCurrentLimits();
        if (currentLimits.isPresent()) {
            writeLoadingLimits(currentLimits.get(), terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static void writeLoadingLimits(LoadingLimits limits, String terminalId, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer) throws XMLStreamException {
        if (!Double.isNaN(limits.getPermanentLimit())) {
            String operationalLimitTypeId = CgmesExportUtil.getUniqueId();
            OperationalLimitTypeEq.writePatl(operationalLimitTypeId, cimNamespace, euNamespace, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
            String operationalLimitSetId = CgmesExportUtil.getUniqueId();
            OperationalLimitSetEq.write(operationalLimitSetId, "operational limit patl", terminalId, cimNamespace, writer);
            LoadingLimitEq.write(CgmesExportUtil.getUniqueId(), limits.getClass(), "CurrentLimit", limits.getPermanentLimit(), operationalLimitTypeId, operationalLimitSetId, cimNamespace, valueAttributeName, writer);
        }
        if (!limits.getTemporaryLimits().isEmpty()) {
            Iterator<LoadingLimits.TemporaryLimit> iterator = limits.getTemporaryLimits().iterator();
            while (iterator.hasNext()) {
                LoadingLimits.TemporaryLimit temporaryLimit = iterator.next();
                String operationalLimitTypeId = CgmesExportUtil.getUniqueId();
                OperationalLimitTypeEq.writeTatl(operationalLimitTypeId, temporaryLimit.getName(), temporaryLimit.getAcceptableDuration(), cimNamespace, euNamespace, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
                String operationalLimitSetId = CgmesExportUtil.getUniqueId();
                OperationalLimitSetEq.write(operationalLimitSetId, "operational limit tatl", terminalId, cimNamespace, writer);
                LoadingLimitEq.write(CgmesExportUtil.getUniqueId(), limits.getClass(), "CurrentLimit", temporaryLimit.getValue(), operationalLimitTypeId, operationalLimitSetId, cimNamespace, valueAttributeName, writer);
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

            String dcConverterUnit1 = CgmesExportUtil.getUniqueId();
            writeDCConverterUnit(dcConverterUnit1, line.getNameOrId() + "_1", substation1Id, cimNamespace, writer);
            String dcNode1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode1").orElseThrow(PowsyblException::new);
            writeDCNode(dcNode1, line.getNameOrId() + "_1", dcConverterUnit1, cimNamespace, writer);

            String dcConverterUnit2 = CgmesExportUtil.getUniqueId();
            writeDCConverterUnit(dcConverterUnit2, line.getNameOrId() + "_1", substation2Id, cimNamespace, writer);
            String dcNode2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode2").orElseThrow(PowsyblException::new);
            writeDCNode(dcNode2, line.getNameOrId() + "_2", dcConverterUnit2, cimNamespace, writer);

            String dcTerminal1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal1").orElseThrow(PowsyblException::new);
            writeDCTerminal(dcTerminal1, lineId, dcNode1, 1, cimNamespace, writer);

            String dcTerminal2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal2").orElseThrow(PowsyblException::new);
            writeDCTerminal(dcTerminal2, lineId, dcNode2, 2, cimNamespace, writer);

            HvdcConverterStation<?> converter = line.getConverterStation1();
            writeTerminal(converter.getTerminal(), mapTerminal2Id, CgmesExportUtil.getUniqueId(), converter1Id, connectivityNodeId(mapNodeKey2NodeId, converter.getTerminal()), 1, cimNamespace, writer);
            String capabilityCurveId1 = writeVsCapabilityCurve(converter, cimNamespace, writer);
            String acdcConverterDcTerminal1 = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + AC_DC_CONVERTER_DC_TERMINAL).orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal1, converter1Id, dcNode1, 2, cimNamespace, writer);

            converter = line.getConverterStation2();
            writeTerminal(converter.getTerminal(), mapTerminal2Id, CgmesExportUtil.getUniqueId(), converter2Id, connectivityNodeId(mapNodeKey2NodeId, converter.getTerminal()), 1, cimNamespace, writer);
            String capabilityCurveId2 = writeVsCapabilityCurve(converter, cimNamespace, writer);
            String acdcConverterDcTerminal2 = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + AC_DC_CONVERTER_DC_TERMINAL).orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal2, converter2Id, dcNode2, 2, cimNamespace, writer);

            DCLineSegmentEq.write(lineId, line.getNameOrId(), line.getR(), cimNamespace, writer);
            writeHvdcConverterStation(line.getConverterStation1(), mapTerminal2Id, line.getNominalV(), dcConverterUnit1, capabilityCurveId1, cimNamespace, writer, context);
            writeHvdcConverterStation(line.getConverterStation2(), mapTerminal2Id, line.getNominalV(), dcConverterUnit2, capabilityCurveId2, cimNamespace, writer, context);
        }
    }

    private static String writeVsCapabilityCurve(HvdcConverterStation<?> converter, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (converter instanceof LccConverterStation) {
            return null;
        }
        VscConverterStation vscConverter = (VscConverterStation) converter;
        if (vscConverter.getReactiveLimits() == null) {
            return null;
        }
        String reactiveLimitsId = CgmesExportUtil.getUniqueId();
        switch (vscConverter.getReactiveLimits().getKind()) {
            case CURVE:
                ReactiveCapabilityCurve curve = vscConverter.getReactiveLimits(ReactiveCapabilityCurve.class);
                for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
                    CurveDataEq.write(CgmesExportUtil.getUniqueId(), point.getP(), point.getMinQ(), point.getMaxQ(), reactiveLimitsId, cimNamespace, writer);
                }
                String reactiveCapabilityCurveName = "RCC_" + vscConverter.getNameOrId();
                ReactiveCapabilityCurveEq.write(reactiveLimitsId, reactiveCapabilityCurveName, vscConverter, cimNamespace, writer);
                break;

            case MIN_MAX:
                //Do not have to export anything
                reactiveLimitsId = null;
                break;

            default:
                throw new PowsyblException("Unexpected type of ReactiveLimits on the VsConverter " + converter.getNameOrId());
        }
        return reactiveLimitsId;
    }

    private static void writeDCConverterUnit(String id, String dcConverterUnitName, String substationId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        DCConverterUnitEq.write(id, dcConverterUnitName, substationId, cimNamespace, writer);
    }

    private static void writeHvdcConverterStation(HvdcConverterStation<?> converterStation, Map<Terminal, String> mapTerminal2Id, double ratedUdc, String dcEquipmentContainerId,
                                                  String capabilityCurveId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String pccTerminal = getConverterStationPccTerminal(converterStation, mapTerminal2Id);
        HvdcConverterStationEq.write(context.getNamingStrategy().getCgmesId(converterStation), converterStation.getNameOrId(), converterStation.getHvdcType(), ratedUdc, dcEquipmentContainerId, pccTerminal, capabilityCurveId, cimNamespace, writer);
    }

    private static String getConverterStationPccTerminal(HvdcConverterStation<?> converterStation, Map<Terminal, String> mapTerminal2Id) {
        if (converterStation.getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
            return exportedTerminalId(mapTerminal2Id, ((VscConverterStation) converterStation).getRegulatingTerminal());
        }
        return null;
    }

    private static void writeDCNode(String id, String dcNodeName, String dcEquipmentContainerId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        DCNodeEq.write(id, dcNodeName, dcEquipmentContainerId, cimNamespace, writer);
    }

    private static void writeDCTerminal(String id, String conductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        DCTerminalEq.write("DCTerminal", id, conductingEquipmentId, dcNodeId, sequenceNumber, cimNamespace, writer);
    }

    private static void writeAcdcConverterDCTerminal(String id, String conductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        DCTerminalEq.write(AC_DC_CONVERTER_DC_TERMINAL, id, conductingEquipmentId, dcNodeId, sequenceNumber, cimNamespace, writer);
    }

    private static void writeControlAreas(Network network, Map<Terminal, String> mapTerminal2Id, String cimNamespace, String euNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        for (CgmesControlArea cgmesControlArea : cgmesControlAreas.getCgmesControlAreas()) {
            writeControlArea(cgmesControlArea, mapTerminal2Id, cimNamespace, euNamespace, writer, context);
        }
    }

    private static void writeControlArea(CgmesControlArea cgmesControlArea, Map<Terminal, String> mapTerminal2Id, String cimNamespace, String euNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Original control area identifiers may not respect mRID rules, so we pass it through naming strategy
        // to obtain always valid mRID identifiers
        String controlAreaCgmesId = context.getNamingStrategy().getCgmesId(cgmesControlArea.getId());
        ControlAreaEq.write(controlAreaCgmesId, cgmesControlArea.getName(), cgmesControlArea.getEnergyIdentificationCodeEIC(), cimNamespace, euNamespace, writer);
        for (Terminal terminal : cgmesControlArea.getTerminals()) {
            TieFlowEq.write(CgmesExportUtil.getUniqueId(), controlAreaCgmesId, exportedTerminalId(mapTerminal2Id, terminal), cimNamespace, writer);
        }
        for (Boundary boundary : cgmesControlArea.getBoundaries()) {
            String terminalId = getTieFlowBoundaryTerminal(boundary, context);
            if (terminalId != null) {
                TieFlowEq.write(CgmesExportUtil.getUniqueId(), controlAreaCgmesId, terminalId, cimNamespace, writer);
            }
        }
    }

    private static String getTieFlowBoundaryTerminal(Boundary boundary, CgmesExportContext context) {
        Connectable<?> c = boundary.getConnectable();
        if (c instanceof DanglingLine) {
            return context.getNamingStrategy().getCgmesIdFromAlias(c, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary");
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

            LOG.error("Unsupported tie flow at TieLine boundary {}", c.getId());
            return null;
        }
    }

    private static void writeTerminals(Network network, Map<Terminal, String> mapTerminal2Id, Map<String, String> mapNodeKey2NodeId,
                                       String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) {
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    writeTerminal(t, mapTerminal2Id, mapNodeKey2NodeId, cimNamespace, writer, context);
                }
            }
        }

        String[] switchNodesKeys = new String[2];
        for (Switch sw : network.getSwitches()) {
            VoltageLevel vl = sw.getVoltageLevel();
            fillSwitchNodeKeys(vl, sw, switchNodesKeys);
            String nodeId1 = mapNodeKey2NodeId.get(switchNodesKeys[0]);
            String terminalId1 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1);
            TerminalEq.write(terminalId1, context.getNamingStrategy().getCgmesId(sw), nodeId1, 1, cimNamespace, writer);
            String nodeId2 = mapNodeKey2NodeId.get(switchNodesKeys[1]);
            String terminalId2 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2);
            TerminalEq.write(terminalId2, context.getNamingStrategy().getCgmesId(sw), nodeId2, 2, cimNamespace, writer);
        }
    }

    private static void writeTerminal(Terminal t, Map<Terminal, String> mapTerminal2Id, Map<String, String> mapNodeKey2NodeId,
                                      String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        String equipmentId = context.getNamingStrategy().getCgmesId(t.getConnectable());
        writeTerminal(t, mapTerminal2Id, CgmesExportUtil.getTerminalId(t, context), equipmentId, connectivityNodeId(mapNodeKey2NodeId, t), CgmesExportUtil.getTerminalSequenceNumber(t), cimNamespace, writer);
    }

    private static void writeTerminal(Terminal terminal, Map<Terminal, String> mapTerminal2Id, String id, String conductingEquipmentId, String connectivityNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) {
        mapTerminal2Id.computeIfAbsent(terminal, k -> {
            try {
                TerminalEq.write(id, conductingEquipmentId, connectivityNodeId, sequenceNumber, cimNamespace, writer);
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
