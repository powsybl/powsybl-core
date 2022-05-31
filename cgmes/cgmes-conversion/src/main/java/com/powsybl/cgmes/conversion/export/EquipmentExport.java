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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class EquipmentExport {

    private static final String ACDCCONVERTERDCTERMINAL = "ACDCConverterDCTerminal";
    private static final String CONNECTIVITY_NODE_SUFFIX = "CN";
    private static final String PHASE_TAP_CHANGER_REGULATION_MODE_ACTIVE_POWER = "activePower";
    private static final String PHASE_TAP_CHANGER_REGULATION_MODE_CURRENT_FLOW = "currentFlow";
    private static final String PHASE_TAP_CHANGER_REGULATION_MODE_VOLTAGE = "voltage";

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            boolean writeConnectivityNodes = context.isWriteConnectivityNodes();

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

            Map <String, String> exportedNodes = new HashMap<>();
            Map <Terminal, String> exportedTerminals = new HashMap<>();
            if (writeConnectivityNodes) {
                writeConnectivityNodes(network, exportedNodes, cimNamespace, writer, context);
            }
            writeTerminals(network, exportedTerminals, exportedNodes, cimNamespace, writer, context);
            writeSwitches(network, cimNamespace, writer, context);

            writeSubstations(network, cimNamespace, writer, context);
            writeVoltageLevels(network, cimNamespace, writer, context);
            writeBusbarSections(network, cimNamespace, writer, context);
            writeLoads(network, cimNamespace, writer, context);
            writeGenerators(network, exportedTerminals, cimNamespace, writeInitialP, writer, context);
            writeShuntCompensators(network, exportedTerminals, cimNamespace, writer, context);
            writeStaticVarCompensators(network, exportedTerminals, cimNamespace, writer, context);
            writeLines(network, exportedTerminals, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            writeTwoWindingsTransformers(network, exportedTerminals, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            writeThreeWindingsTransformers(network, exportedTerminals, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            Map <Boundary, String> danglingLineBoundaries = new HashMap<>();
            writeDanglingLines(network, exportedTerminals, danglingLineBoundaries, cimNamespace, euNamespace, limitValueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer, context);
            writeHvdcLines(network, exportedTerminals, exportedNodes, cimNamespace, writer, context);

            writeControlAreas(network, exportedTerminals, danglingLineBoundaries, cimNamespace, euNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeConnectivityNodes(Network network, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                writeNodes(vl, new VoltageLevelAdjacency(vl), exportedNodes, cimNamespace, writer, context);
            } else {
                writeBuses(vl, exportedNodes, cimNamespace, writer, context);
            }
            writeSwitchesConnectivity(vl, exportedNodes, cimNamespace, writer, context);
        }
        writeBusbarSectionsConnectivity(network, exportedNodes, cimNamespace, writer, context);
    }

    private static void writeSwitchesConnectivity(VoltageLevel vl, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        for (Switch sw : vl.getSwitches()) {
            String node1Key = getSwitchNode1Id(vl, sw);
            exportedNodes.computeIfAbsent(node1Key, k -> {
                try {
                    String node = CgmesExportUtil.getUniqueId();
                    ConnectivityNodeEq.write(node, node1Key, context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
                    return node;
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
            String node2Key = getSwitchNode2Id(vl, sw);
            exportedNodes.computeIfAbsent(node2Key, k -> {
                try {
                    String node = CgmesExportUtil.getUniqueId();
                    ConnectivityNodeEq.write(node, node2Key, context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
                    return node;
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            });
        }
    }

    private static void writeBusbarSectionsConnectivity(Network network, Map <String, String> exportedNodes, String cimNamespace,
                                                        XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (BusbarSection bus : network.getBusbarSections()) {
            String connectivityNodeId = connectivityNodeId(exportedNodes, bus.getTerminal());
            if (connectivityNodeId == null) {
                VoltageLevel vl = bus.getTerminal().getVoltageLevel();
                String node = CgmesExportUtil.getUniqueId();
                ConnectivityNodeEq.write(node, bus.getNameOrId(), context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
                String key = vl.getId() + bus.getTerminal().getNodeBreakerView().getNode() + CONNECTIVITY_NODE_SUFFIX; // We are already in NODE-BREAKER
                exportedNodes.put(key, node);
            }
        }
    }

    private static void writeNodes(VoltageLevel vl, VoltageLevelAdjacency vlAdjacencies, Map <String, String> exportedNodes, String cimNamespace,
                                   XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (List<Integer> nodes : vlAdjacencies.getNodes()) {
            String cgmesNodeId = CgmesExportUtil.getUniqueId();
            ConnectivityNodeEq.write(cgmesNodeId, CgmesExportUtil.format(nodes.get(0)), context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
            for (Integer nodeNumber : nodes) {
                exportedNodes.put(vl.getId() + nodeNumber + CONNECTIVITY_NODE_SUFFIX, cgmesNodeId);
            }
        }
    }

    private static void writeBuses(VoltageLevel vl, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context)throws XMLStreamException {
        for (Bus bus : vl.getBusBreakerView().getBuses()) {
            String cgmesNodeId = context.getNamingStrategy().getCgmesId(bus, CONNECTIVITY_NODE_SUFFIX);
            ConnectivityNodeEq.write(cgmesNodeId, bus.getNameOrId(), context.getNamingStrategy().getCgmesId(vl), cimNamespace, writer);
            exportedNodes.put(bus.getId() + CONNECTIVITY_NODE_SUFFIX, cgmesNodeId);
        }
    }

    private static void writeSwitches(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context)throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            VoltageLevel vl = sw.getVoltageLevel();
            SwitchEq.write(context.getNamingStrategy().getCgmesId(sw), sw.getNameOrId(), sw.getKind(), context.getNamingStrategy().getCgmesId(vl), sw.isRetained(), cimNamespace, writer);
        }
    }

    private static String getSwitchNode1Id(VoltageLevel vl, Switch sw) {
        if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            return vl.getId() + vl.getNodeBreakerView().getNode1(sw.getId()) + CONNECTIVITY_NODE_SUFFIX;
        } else {
            return vl.getBusBreakerView().getBus1(sw.getId()).getId() + CONNECTIVITY_NODE_SUFFIX;
        }
    }

    private static String getSwitchNode2Id(VoltageLevel vl, Switch sw) {
        if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            return vl.getId() + vl.getNodeBreakerView().getNode2(sw.getId()) + CONNECTIVITY_NODE_SUFFIX;
        } else {
            return vl.getBusBreakerView().getBus2(sw.getId()).getId() + CONNECTIVITY_NODE_SUFFIX;
        }
    }

    private static void writeSubstations(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (String geographicalRegionId : context.getRegionsIds()) {
            writeGeographicalRegion(geographicalRegionId, context.getRegionName(geographicalRegionId), cimNamespace, writer);
        }
        List<String> writtenSubRegions = new ArrayList<>();
        for (Substation substation : network.getSubstations()) {
            String subGeographicalRegionId = substation.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId");
            String geographicalRegionId = substation.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId");
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

    private static void writeVoltageLevels(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Set<Double> exportedBaseVoltagesByNominalV = new HashSet<>();
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

    private static void writeGenerators(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace, boolean writeInitialP,
                                        XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        // Multiple synchronous machines may be grouped in the same generating unit
        // We have to write each generating unit only once
        Set<String> generatingUnitsWritten = new HashSet<>();
        for (Generator generator : network.getGenerators()) {
            String generatingUnit = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            String regulatingControlId = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
            if (regulatingControlId != null) {
                String regulatingControlName = "RC_" + generator.getNameOrId();
                RegulatingControlEq.write(regulatingControlId, regulatingControlName, exportedTerminalId(exportedTerminals, generator.getRegulatingTerminal()), cimNamespace, writer);
            }
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

    private static void writeShuntCompensators(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            double bPerSection = 0.0;
            double gPerSection = Double.NaN;
            if (s.getModelType().equals(ShuntCompensatorModelType.LINEAR)) {
                bPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getBPerSection();
                gPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getGPerSection();
            }
            String regulatingControlId = s.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
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
            String regulatingControlName = "RC_" + s.getNameOrId();
            RegulatingControlEq.write(regulatingControlId, regulatingControlName, exportedTerminalId(exportedTerminals, s.getRegulatingTerminal()), cimNamespace, writer);
        }
    }

    private static void writeStaticVarCompensators(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace,
                                                   XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String regulatingControlId = svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
            if (regulatingControlId != null) {
                String regulatingControlName = "RC_" + svc.getNameOrId();
                RegulatingControlEq.write(regulatingControlId, regulatingControlName, exportedTerminalId(exportedTerminals, svc.getRegulatingTerminal()), cimNamespace, writer);
            }
            StaticVarCompensatorEq.write(context.getNamingStrategy().getCgmesId(svc), svc.getNameOrId(), regulatingControlId, 1 / svc.getBmin(), 1 / svc.getBmax(), svc.getExtension(VoltagePerReactivePowerControl.class), svc.getRegulationMode(), svc.getVoltageSetpoint(), cimNamespace, writer);
        }
    }

    private static void writeLines(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Line line : network.getLines()) {
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(line), line.getNameOrId(), line.getR(), line.getX(), line.getG1() + line.getG2(), line.getB1() + line.getB2(), cimNamespace, writer);
            writeBranchLimits(line, exportedTerminalId(exportedTerminals, line.getTerminal1()), exportedTerminalId(exportedTerminals, line.getTerminal2()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static void writeTwoWindingsTransformers(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace,
                                                    String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(), cimNamespace, writer);
            String end1Id = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 1).orElseThrow(PowsyblException::new);
            // structural ratio at end1
            double a0 = twt.getRatedU1() / twt.getRatedU2();
            // move structural ratio from end1 to end2
            double a02 = a0 * a0;
            double r = twt.getR() * a02;
            double x = twt.getX() * a02;
            double g = twt.getG() / a02;
            double b = twt.getB() / a02;
            PowerTransformerEq.writeEnd(end1Id, twt.getNameOrId() + "_1", context.getNamingStrategy().getCgmesId(twt), 1, r, x, g, b, twt.getRatedU1(), exportedTerminalId(exportedTerminals, twt.getTerminal1()), cimNamespace, writer);
            String end2Id = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 2).orElseThrow(PowsyblException::new);
            PowerTransformerEq.writeEnd(end2Id, twt.getNameOrId() + "_2", context.getNamingStrategy().getCgmesId(twt), 2, 0.0, 0.0, 0.0, 0.0, twt.getRatedU2(), exportedTerminalId(exportedTerminals, twt.getTerminal2()), cimNamespace, writer);

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
            writePhaseTapChanger(twt, twt.getPhaseTapChanger(), twt.getNameOrId(), endNumber, end1Id, twt.getTerminal1().getVoltageLevel().getNominalV(), cimNamespace, writer);
            writeRatioTapChanger(twt, twt.getRatioTapChanger(), twt.getNameOrId(), endNumber, end1Id, cimNamespace, writer);
            writeBranchLimits(twt, exportedTerminalId(exportedTerminals, twt.getTerminal1()), exportedTerminalId(exportedTerminals, twt.getTerminal2()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
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

    private static void writeThreeWindingsTransformers(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace,
                                                      String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            PowerTransformerEq.write(context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId(), cimNamespace, writer);
            double ratedU0 = twt.getLeg1().getRatedU();
            String end1Id = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 1).orElseThrow(PowsyblException::new);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_1", end1Id, 1, twt.getLeg1(), ratedU0, exportedTerminalId(exportedTerminals, twt.getLeg1().getTerminal()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
            String end2Id = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 2).orElseThrow(PowsyblException::new);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_2", end2Id, 2, twt.getLeg2(), ratedU0, exportedTerminalId(exportedTerminals, twt.getLeg2().getTerminal()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
            String end3Id = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + 3).orElseThrow(PowsyblException::new);
            writeThreeWindingsTransformerEnd(twt, context.getNamingStrategy().getCgmesId(twt), twt.getNameOrId() + "_3", end3Id, 3, twt.getLeg3(), ratedU0, exportedTerminalId(exportedTerminals, twt.getLeg3().getTerminal()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static void writeThreeWindingsTransformerEnd(ThreeWindingsTransformer twt, String twtId, String twtName, String endId, int endNumber, ThreeWindingsTransformer.Leg leg, double ratedU0, String terminalId, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer) throws XMLStreamException {
        // structural ratio at end1
        double a0 = leg.getRatedU() / ratedU0;
        // move structural ratio from end1 to end2
        double a02 = a0 * a0;
        double r = leg.getR() * a02;
        double x = leg.getX() * a02;
        double g = leg.getG() / a02;
        double b = leg.getB() / a02;
        PowerTransformerEq.writeEnd(endId, twtName, twtId, endNumber, r, x, g, b, leg.getRatedU(), terminalId, cimNamespace, writer);
        writePhaseTapChanger(twt, leg.getPhaseTapChanger(), twtName, endNumber, endId, leg.getTerminal().getVoltageLevel().getNominalV(), cimNamespace, writer);
        writeRatioTapChanger(twt, leg.getRatioTapChanger(), twtName, endNumber, endId,  cimNamespace, writer);
        writeFlowsLimits(leg, terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
    }

    private static <C extends Connectable<C>> void writePhaseTapChanger(C eq, PhaseTapChanger ptc, String twtName, int endNumber, String endId, double neutralU, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (ptc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber)
                    .orElseThrow(PowsyblException::new);

            int neutralStep = getPhaseTapChangerNeutralStep(ptc);
            Optional<String> regulatingControlId = getTapChangerControlId(eq, tapChangerId);
            String phaseTapChangerTableId = CgmesExportUtil.getUniqueId();

            // If we write the EQ, we will always write the Tap Changer as tabular
            // We reset the phase tap changer type stored in the extensions
            String typeTabular = CgmesNames.PHASE_TAP_CHANGER_TABULAR;
            CgmesExportUtil.setCgmesTapChangerType(eq, tapChangerId, typeTabular);

            TapChangerEq.writePhase(typeTabular, tapChangerId, twtName + "_PTC", endId, ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, ptc.getTapPosition(), neutralU, false, phaseTapChangerTableId, regulatingControlId, cimNamespace, writer);
            TapChangerEq.writePhaseTable(phaseTapChangerTableId, twtName + "_TABLE", cimNamespace, writer);
            for (Map.Entry<Integer, PhaseTapChangerStep> step : ptc.getAllSteps().entrySet()) {
                TapChangerEq.writePhaseTablePoint(CgmesExportUtil.getUniqueId(), phaseTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), -step.getValue().getAlpha(), step.getKey(), cimNamespace, writer);
            }

            if (regulatingControlId.isPresent()) {
                String mode = getPhaseTapChangerRegulationMode(ptc);
                // Only export the regulating control if mode is valid
                if (mode != null) {
                    String controlName = twtName + "_PTC_RC";
                    String terminalId = CgmesExportUtil.getTerminalId(ptc.getRegulationTerminal());
                    TapChangerEq.writeControl(regulatingControlId.get(), controlName, mode, terminalId, cimNamespace, writer);
                }
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

    private static <C extends Connectable<C>> void writeRatioTapChanger(C eq, RatioTapChanger rtc, String twtName, int endNumber, String endId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (rtc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber)
                    .orElseThrow(PowsyblException::new);

            int neutralStep = getRatioTapChangerNeutralStep(rtc);
            double stepVoltageIncrement = 100.0 * (1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0) / (rtc.getLowTapPosition() - neutralStep);
            Optional<String> regulatingControlId = getTapChangerControlId(eq, tapChangerId);
            String ratioTapChangerTableId = CgmesExportUtil.getUniqueId();

            TapChangerEq.writeRatio(tapChangerId, twtName + "_RTC", endId, rtc.getLowTapPosition(), rtc.getHighTapPosition(), neutralStep, rtc.getTapPosition(), rtc.getTargetV(), rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement, ratioTapChangerTableId, regulatingControlId, cimNamespace, writer);
            TapChangerEq.writeRatioTable(ratioTapChangerTableId, twtName + "_TABLE", cimNamespace, writer);
            for (Map.Entry<Integer, RatioTapChangerStep> step : rtc.getAllSteps().entrySet()) {
                TapChangerEq.writeRatioTablePoint(CgmesExportUtil.getUniqueId(), ratioTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), step.getKey(), cimNamespace, writer);
            }

            if (regulatingControlId.isPresent()) {
                String controlName = twtName + "_RTC_RC";
                String terminalId = CgmesExportUtil.getTerminalId(rtc.getRegulationTerminal());
                // Regulating control mode is always "voltage"
                TapChangerEq.writeControl(regulatingControlId.get(), controlName, PHASE_TAP_CHANGER_REGULATION_MODE_VOLTAGE, terminalId, cimNamespace, writer);
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

    private static void writeDanglingLines(Network network, Map<Terminal, String> exportedTerminals, Map<Boundary, String> danglingLineBoundaries, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine danglingLine : network.getDanglingLines()) {

            String substationId = writeDanglingLineSubstation(danglingLine, cimNamespace, writer);
            String baseVoltageId = writeDanglingLineBaseVoltage(danglingLine, cimNamespace, writer, context);
            String voltageLevelId = writeDanglingLineVoltageLevel(danglingLine, substationId, baseVoltageId, cimNamespace, writer);
            String connectivityNodeId = writeDanglingLineConnectivity(danglingLine, voltageLevelId, danglingLineBoundaries, cimNamespace, writer, context);

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
            String equivalentInjectionId = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection").orElseThrow(PowsyblException::new);
            EquivalentInjectionEq.write(equivalentInjectionId, danglingLine.getNameOrId() + "_EI", danglingLine.getGeneration() != null, danglingLine.getGeneration() != null, minP, maxP, minQ, maxQ, baseVoltageId, cimNamespace, writer);
            String equivalentInjectionTerminalId = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal").orElseThrow(PowsyblException::new);
            TerminalEq.write(equivalentInjectionTerminalId, equivalentInjectionId, connectivityNodeId, 1, cimNamespace, writer);

            // Cast the danglingLine to an AcLineSegment
            AcLineSegmentEq.write(context.getNamingStrategy().getCgmesId(danglingLine), danglingLine.getNameOrId() + "_DL", danglingLine.getR(), danglingLine.getX(), danglingLine.getG(), danglingLine.getB(), cimNamespace, writer);
            writeFlowsLimits(danglingLine, exportedTerminalId(exportedTerminals, danglingLine.getTerminal()), cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
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

    private static String writeDanglingLineBaseVoltage(DanglingLine danglingLine, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        double nominalV = danglingLine.getTerminal().getVoltageLevel().getNominalV();
        BaseVoltageMapping.BaseVoltageSource baseVoltage = context.getBaseVoltageByNominalVoltage(nominalV);
        if (baseVoltage.getSource().equals(Source.IGM)) {
            BaseVoltageEq.write(baseVoltage.getId(), nominalV, cimNamespace, writer);
        }

        return baseVoltage.getId();
    }

    private static String writeDanglingLineVoltageLevel(DanglingLine danglingLine, String substationId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // New VoltageLevel
        String voltageLevelId = CgmesExportUtil.getUniqueId();
        VoltageLevelEq.write(voltageLevelId, danglingLine.getNameOrId() + "_VL", Double.NaN, Double.NaN, substationId, baseVoltageId, cimNamespace, writer);

        return voltageLevelId;
    }

    private static String writeDanglingLineConnectivity(DanglingLine danglingLine, String voltageLevelId, Map<Boundary, String> danglingLineBoundaries,
                                                        String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String connectivityNodeId = null;
        // New Terminal
        String terminalId = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary").orElseThrow(PowsyblException::new);
        if (danglingLine.getTerminal().getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            // New ConnectivityNode
            connectivityNodeId = CgmesExportUtil.getUniqueId();
            ConnectivityNodeEq.write(connectivityNodeId, danglingLine.getNameOrId() + "_NODE", voltageLevelId, cimNamespace, writer);
            TerminalEq.write(terminalId, context.getNamingStrategy().getCgmesId(danglingLine), connectivityNodeId, 2, cimNamespace, writer);
        } else {
            TerminalEq.write(terminalId, context.getNamingStrategy().getCgmesId(danglingLine), null, 2, cimNamespace, writer);
        }
        danglingLineBoundaries.put(danglingLine.getBoundary(), terminalId);

        return connectivityNodeId;
    }

    private static void writeBranchLimits(Branch<?> branch, String terminalId1, String terminalId2, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer) throws XMLStreamException {
        if (branch.getActivePowerLimits1() != null) {
            writeLoadingLimits(branch.getActivePowerLimits1(), terminalId1, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        if (branch.getActivePowerLimits2() != null) {
            writeLoadingLimits(branch.getActivePowerLimits2(), terminalId2, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        if (branch.getApparentPowerLimits1() != null) {
            writeLoadingLimits(branch.getApparentPowerLimits1(), terminalId1, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        if (branch.getApparentPowerLimits2() != null) {
            writeLoadingLimits(branch.getApparentPowerLimits2(), terminalId2, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        if (branch.getCurrentLimits1() != null) {
            writeLoadingLimits(branch.getCurrentLimits1(), terminalId1, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        if (branch.getCurrentLimits2() != null) {
            writeLoadingLimits(branch.getCurrentLimits2(), terminalId2, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
    }

    private static void writeFlowsLimits(FlowsLimitsHolder holder, String terminalId, String cimNamespace, String euNamespace, String valueAttributeName, String limitTypeAttributeName, String limitKindClassName, boolean writeInfiniteDuration, XMLStreamWriter writer) throws XMLStreamException {
        if (holder.getActivePowerLimits() != null) {
            writeLoadingLimits(holder.getActivePowerLimits(), terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        if (holder.getApparentPowerLimits() != null) {
            writeLoadingLimits(holder.getApparentPowerLimits(), terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
        }
        if (holder.getCurrentLimits() != null) {
            writeLoadingLimits(holder.getCurrentLimits(), terminalId, cimNamespace, euNamespace, valueAttributeName, limitTypeAttributeName, limitKindClassName, writeInfiniteDuration, writer);
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

    private static void writeHvdcLines(Network network, Map<Terminal, String> exportedTerminals, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
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
            writeTerminal(converter.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), converter1Id, connectivityNodeId(exportedNodes, converter.getTerminal()), 1, cimNamespace, writer);
            String capabilityCurveId1 = writeVsCapabilityCurve(converter, cimNamespace, writer);
            String acdcConverterDcTerminal1 = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL).orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal1, converter1Id, dcNode1, 2, cimNamespace, writer);

            converter = line.getConverterStation2();
            writeTerminal(converter.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), converter2Id, connectivityNodeId(exportedNodes, converter.getTerminal()), 1, cimNamespace, writer);
            String capabilityCurveId2 = writeVsCapabilityCurve(converter, cimNamespace, writer);
            String acdcConverterDcTerminal2 = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL).orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal2, converter2Id, dcNode2, 2, cimNamespace, writer);

            DCLineSegmentEq.write(lineId, line.getNameOrId(), line.getR(), cimNamespace, writer);
            writeHvdcConverterStation(line.getConverterStation1(), exportedTerminals, line.getNominalV(), dcConverterUnit1, capabilityCurveId1, cimNamespace, writer, context);
            writeHvdcConverterStation(line.getConverterStation2(), exportedTerminals, line.getNominalV(), dcConverterUnit2, capabilityCurveId2, cimNamespace, writer, context);
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

    private static void writeHvdcConverterStation(HvdcConverterStation<?> converterStation, Map<Terminal, String> exportedTerminals, double ratedUdc, String dcEquipmentContainerId,
                                                  String capabilityCurveId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String pccTerminal = getConverterStationPccTerminal(converterStation, exportedTerminals);
        HvdcConverterStationEq.write(context.getNamingStrategy().getCgmesId(converterStation), converterStation.getNameOrId(), converterStation.getHvdcType(), ratedUdc, dcEquipmentContainerId, pccTerminal, capabilityCurveId, cimNamespace, writer);
    }

    private static String getConverterStationPccTerminal(HvdcConverterStation<?> converterStation, Map<Terminal, String> exportedTerminals) {
        if (converterStation.getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
            return exportedTerminalId(exportedTerminals, ((VscConverterStation) converterStation).getRegulatingTerminal());
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
        DCTerminalEq.write(ACDCCONVERTERDCTERMINAL, id, conductingEquipmentId, dcNodeId, sequenceNumber, cimNamespace, writer);
    }

    private static void writeControlAreas(Network network, Map<Terminal, String> exportedTerminals, Map<Boundary, String> danglingLineBoundaries, String cimNamespace, String euNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        for (CgmesControlArea cgmesControlArea : cgmesControlAreas.getCgmesControlAreas()) {
            writeControlArea(cgmesControlArea, exportedTerminals, danglingLineBoundaries, cimNamespace, euNamespace, writer);
        }
    }

    private static void writeControlArea(CgmesControlArea cgmesControlArea, Map<Terminal, String> exportedTerminals, Map<Boundary, String> danglingLineBoundaries, String cimNamespace, String euNamespace, XMLStreamWriter writer) throws XMLStreamException {
        ControlAreaEq.write(cgmesControlArea.getId(), cgmesControlArea.getName(), cgmesControlArea.getEnergyIdentificationCodeEIC(), cimNamespace, euNamespace, writer);
        for (Terminal terminal : cgmesControlArea.getTerminals()) {
            TieFlowEq.write(CgmesExportUtil.getUniqueId(), cgmesControlArea.getId(), exportedTerminalId(exportedTerminals, terminal), cimNamespace, writer);
        }
        for (Boundary boundary : cgmesControlArea.getBoundaries()) {
            if (cgmesControlArea.getBoundaries().contains(boundary)) {
                TieFlowEq.write(CgmesExportUtil.getUniqueId(), cgmesControlArea.getId(), danglingLineBoundaries.get(boundary), cimNamespace, writer);
            }
        }
    }

    private static void writeTerminals(Network network, Map<Terminal, String> exportedTerminals, Map<String, String> exportedNodes,
                                       String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) {
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    writeTerminal(t, exportedTerminals, exportedNodes, cimNamespace, writer, context);
                }
            }
        }

        for (Switch sw : network.getSwitches()) {
            VoltageLevel vl = sw.getVoltageLevel();
            String node1 = exportedNodes.get(getSwitchNode1Id(vl, sw));
            String terminalId1 = sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1).orElseThrow(PowsyblException::new);
            TerminalEq.write(terminalId1, context.getNamingStrategy().getCgmesId(sw), node1, 1, cimNamespace, writer);
            String node2 = exportedNodes.get(getSwitchNode2Id(vl, sw));
            String terminalId2 = sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2).orElseThrow(PowsyblException::new);
            TerminalEq.write(terminalId2, context.getNamingStrategy().getCgmesId(sw), node2, 2, cimNamespace, writer);
        }
    }

    private static void writeTerminal(Terminal t, Map<Terminal, String> exportedTerminals, Map<String, String> exportedNodes,
                                      String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        String equipmentId = context.getNamingStrategy().getCgmesId(t.getConnectable());
        writeTerminal(t, exportedTerminals, CgmesExportUtil.getTerminalId(t), equipmentId, connectivityNodeId(exportedNodes, t), CgmesExportUtil.getTerminalSequenceNumber(t), cimNamespace, writer);
    }

    private static void writeTerminal(Terminal terminal, Map<Terminal, String> exportedTerminals, String id, String conductingEquipmentId, String connectivityNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) {
        exportedTerminals.computeIfAbsent(terminal, k -> {
            try {
                TerminalEq.write(id, conductingEquipmentId, connectivityNodeId, sequenceNumber, cimNamespace, writer);
                return id;
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    private static String exportedTerminalId(Map<Terminal, String> exportedTerminals, Terminal terminal) {
        if (exportedTerminals.containsKey(terminal)) {
            return exportedTerminals.get(terminal);
        } else {
            throw new PowsyblException("Terminal has not been exported");
        }
    }

    private static String connectivityNodeId(Map<String, String> exportedNodes, Terminal terminal) {
        String key;
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            key = terminal.getVoltageLevel().getId() + terminal.getNodeBreakerView().getNode() + CONNECTIVITY_NODE_SUFFIX;
        } else {
            key = terminal.getBusBreakerView().getConnectableBus().getId() + CONNECTIVITY_NODE_SUFFIX;
        }
        return exportedNodes.get(key);
    }

    private static class VoltageLevelAdjacency {

        private final List<List<Integer>> voltageLevelNodes;

        VoltageLevelAdjacency(VoltageLevel vl) {
            voltageLevelNodes = new ArrayList<>();

            NodeAdjacency adjacency = new NodeAdjacency(vl);
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

        NodeAdjacency(VoltageLevel vl) {
            adjacency = new HashMap<>();
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                vl.getNodeBreakerView().getInternalConnections().forEach(this::computeInternalConnectionAdjacency);
            }
        }

        private void computeInternalConnectionAdjacency(VoltageLevel.NodeBreakerView.InternalConnection ic) {
            addAdjacency(ic.getNode1(), ic.getNode2());
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
