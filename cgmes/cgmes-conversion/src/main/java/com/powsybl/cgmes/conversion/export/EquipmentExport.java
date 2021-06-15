/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.elements.*;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
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

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeRdfRoot(context.getCimVersion(), writer);
            String cimNamespace = context.getCimNamespace();

            // TODO fill EQ Model Description
            if (context.getCimVersion() == 16) {
                ModelDescriptionEq.write(writer, context.getEqModelDescription(), context);
            }

            Map <String, String> exportedNodes = new HashMap<>();
            Map <Terminal, String> exportedTerminals = new HashMap<>();
            writeConnectivity(network, exportedNodes, cimNamespace, writer);
            writeTerminals(network, exportedTerminals, exportedNodes, cimNamespace, writer);
            writeSwitches(network, exportedNodes, cimNamespace, writer);

            writeSubstations(network, cimNamespace, writer);
            writeVoltageLevels(network, cimNamespace, writer);
            writeLoads(network, cimNamespace, writer);
            writeGenerators(network, exportedTerminals, cimNamespace, writer);
            writeShuntCompensators(network, cimNamespace, writer);
            writeStaticVarCompensators(network, cimNamespace, writer);
            writeLines(network, exportedTerminals, cimNamespace, writer);
            writeTwoWindingsTransformer(network, exportedTerminals, cimNamespace, writer);
            writeThreeWindingsTransformer(network, exportedTerminals, cimNamespace, writer);
            Map <Boundary, String> danglingLineBoundaries = new HashMap<>();
            writeDanglingLines(network, exportedTerminals, danglingLineBoundaries, cimNamespace, writer);
            writeHvdcLines(network, exportedTerminals, exportedNodes, cimNamespace, writer);

            writeControlAreas(network, exportedTerminals, danglingLineBoundaries, cimNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeConnectivity(Network network, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                writeNodes(vl, new VoltageLevelAdjacency(vl), exportedNodes, cimNamespace, writer);
            } else {
                writeBuses(vl, exportedNodes, cimNamespace, writer);
            }
        }
    }

    private static void writeNodes(VoltageLevel vl, VoltageLevelAdjacency vlAdjacencies, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer)throws XMLStreamException {
        for (List<Integer> nodes : vlAdjacencies.getNodes()) {
            String node = CgmesExportUtil.getUniqueId();
            ConnectivityNodeEq.write(node, CgmesExportUtil.format(nodes.get(0)), vl.getId(), cimNamespace, writer);
            for (Integer nodeId : nodes) {
                exportedNodes.put(vl.getId() + nodeId, node);
            }
        }
    }

    private static void writeBuses(VoltageLevel vl, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer)throws XMLStreamException {
        for (Bus bus : vl.getBusBreakerView().getBuses()) {
            ConnectivityNodeEq.write(bus.getId(), bus.getNameOrId(), vl.getId(), cimNamespace, writer);
            exportedNodes.put(bus.getId(), bus.getId());
        }
    }

    private static void writeSwitches(Network network, Map <String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer)throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            VoltageLevel vl = sw.getVoltageLevel();
            SwitchEq.write(sw.getId(), sw.getNameOrId(), sw.getKind(), vl.getId(), cimNamespace, writer);
            String node1 = exportedNodes.get(getNode1Key(vl, sw));
            TerminalEq.write(CgmesExportUtil.getUniqueId(), sw.getId(), node1, 1, cimNamespace, writer);
            String node2 = exportedNodes.get(getNode2Key(vl, sw));
            TerminalEq.write(CgmesExportUtil.getUniqueId(), sw.getId(), node2, 2, cimNamespace, writer);
        }
    }

    private static String getNode1Key(VoltageLevel vl, Switch sw) {
        if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            return vl.getId() + vl.getNodeBreakerView().getNode1(sw.getId());
        } else {
            return vl.getBusBreakerView().getBus1(sw.getId()).getId();
        }
    }

    private static String getNode2Key(VoltageLevel vl, Switch sw) {
        if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            return vl.getId() + vl.getNodeBreakerView().getNode2(sw.getId());
        } else {
            return vl.getBusBreakerView().getBus2(sw.getId()).getId();
        }
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
                GeographicalRegionEq.write(geographicalRegionId, geoName, cimNamespace, writer);
                SubGeographicalRegionEq.write(subGeographicalRegionId, geoName, geographicalRegionId, cimNamespace, writer);
            }
            SubstationEq.write(substation.getId(), substation.getNameOrId(), geographicalRegionIds.get(geoName), cimNamespace, writer);
        }
    }

    private static void writeVoltageLevels(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Map<Double, String> baseVoltageIds = new HashMap<>();
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            double nominalV = voltageLevel.getNominalV();
            if (!baseVoltageIds.containsKey(nominalV)) {
                String baseVoltageId = CgmesExportUtil.getUniqueId();
                baseVoltageIds.put(nominalV, baseVoltageId);
                BaseVoltageEq.write(baseVoltageId, nominalV, cimNamespace, writer);
            }
            VoltageLevelEq.write(voltageLevel.getId(), voltageLevel.getNameOrId(), voltageLevel.getSubstation().getId(), baseVoltageIds.get(voltageLevel.getNominalV()), cimNamespace, writer);
        }
    }

    private static void writeLoads(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            EnergyConsumerEq.write(load.getId(), load.getNameOrId(), load.getExtension(LoadDetail.class), load.getTerminal().getVoltageLevel().getId(), cimNamespace, writer);
        }
    }

    private static void writeGenerators(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Generator generator : network.getGenerators()) {
            String generatingUnit = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            if (generatingUnit == null) {
                generatingUnit = CgmesExportUtil.getUniqueId();
            }
            String regulatingControlId = null;
            if (!generator.getRegulatingTerminal().equals(generator.getTerminal())) {
                regulatingControlId = CgmesExportUtil.getUniqueId();
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
                    ReactiveCapabilityCurveEq.write(reactiveLimitsId, reactiveCapabilityCurveName, cimNamespace, writer);
                    break;

                case MIN_MAX:
                    minQ = generator.getReactiveLimits(MinMaxReactiveLimits.class).getMinQ();
                    maxQ = generator.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ();
                    break;

                default:
                    throw new PowsyblException("Unexpected type of ReactiveLimits on the generator " + generator.getNameOrId());
            }
            SynchronousMachineEq.write(generator.getId(), generator.getNameOrId(), generatingUnit, regulatingControlId, reactiveLimitsId, minQ, maxQ, cimNamespace, writer);
            String generatingUnitName = "GEN_" + generator.getNameOrId();
            GeneratingUnitEq.write(generatingUnit, generatingUnitName, generator.getEnergySource(), generator.getMinP(), generator.getMaxP(), generator.getTargetP(), cimNamespace, writer);
        }
    }

    private static void writeShuntCompensators(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            double bPerSection = 0.0;
            if (s.getModelType().equals(ShuntCompensatorModelType.LINEAR)) {
                bPerSection = ((ShuntCompensatorLinearModel) s.getModel()).getBPerSection();
            }
            ShuntCompensatorEq.write(s.getId(), s.getNameOrId(), s.getSectionCount(), s.getMaximumSectionCount(), s.getTerminal().getVoltageLevel().getNominalV(), s.getModelType(), bPerSection, cimNamespace, writer);
            if (s.getModelType().equals(ShuntCompensatorModelType.NON_LINEAR)) {
                double b = 0.0;
                double g = 0.0;
                for (int section = 1; section <= s.getMaximumSectionCount(); section++) {
                    ShuntCompensatorEq.writePoint(CgmesExportUtil.getUniqueId(), s.getId(), section, s.getB(section) - b, s.getG(section) - g, cimNamespace, writer);
                    b = s.getB(section);
                    g = s.getG(section);
                }
            }
        }
    }

    private static void writeStaticVarCompensators(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            StaticVarCompensatorEq.write(svc.getId(), svc.getNameOrId(), 1 / svc.getBmin(), 1 / svc.getBmax(), svc.getExtension(VoltagePerReactivePowerControl.class), svc.getRegulationMode(), svc.getVoltageSetpoint(), cimNamespace, writer);
        }
    }

    private static void writeLines(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Line line : network.getLines()) {
            AcLineSegmentEq.write(line.getId(), line.getNameOrId(), line.getR(), line.getX(), line.getG1() + line.getG2(), line.getB1() + line.getB2(), cimNamespace, writer);
            writeBranchLimits(line, exportedTerminalId(exportedTerminals, line.getTerminal1()), exportedTerminalId(exportedTerminals, line.getTerminal2()), cimNamespace, writer);
        }
    }

    private static void writeTwoWindingsTransformer(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            PowerTransformerEq.write(twt.getId(), twt.getNameOrId(), cimNamespace, writer);
            String end1Id = CgmesExportUtil.getUniqueId();
            // structural ratio at end1
            double a0 = twt.getRatedU1() / twt.getRatedU2();
            // move structural ratio from end1 to end2
            double a02 = a0 * a0;
            double r = twt.getR() * a02;
            double x = twt.getX() * a02;
            double g = twt.getG() / a02;
            double b = twt.getB() / a02;
            PowerTransformerEq.writeEnd(end1Id, twt.getNameOrId() + "_1", twt.getId(), 1, r, x, g, b, twt.getRatedU1(), exportedTerminalId(exportedTerminals, twt.getTerminal1()), cimNamespace, writer);
            PowerTransformerEq.writeEnd(CgmesExportUtil.getUniqueId(), twt.getNameOrId() + "_2", twt.getId(), 2, 0.0, 0.0, 0.0, 0.0, twt.getRatedU2(), exportedTerminalId(exportedTerminals, twt.getTerminal2()), cimNamespace, writer);
            writePhaseTapChanger(twt.getPhaseTapChanger(), twt.getNameOrId(), end1Id, twt.getTerminal1().getVoltageLevel().getNominalV(), cimNamespace, writer);
            writeRatioTapChanger(twt.getRatioTapChanger(), twt.getNameOrId(), end1Id, cimNamespace, writer);
            writeBranchLimits(twt, exportedTerminalId(exportedTerminals, twt.getTerminal1()), exportedTerminalId(exportedTerminals, twt.getTerminal2()), cimNamespace, writer);
        }
    }

    private static void writeThreeWindingsTransformer(Network network, Map<Terminal, String> exportedTerminals, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            PowerTransformerEq.write(twt.getId(), twt.getNameOrId(), cimNamespace, writer);
            double ratedU0 = twt.getLeg1().getRatedU();
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_1", CgmesExportUtil.getUniqueId(), 1, twt.getLeg1(), ratedU0, exportedTerminalId(exportedTerminals, twt.getLeg1().getTerminal()), cimNamespace, writer);
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_2", CgmesExportUtil.getUniqueId(), 2, twt.getLeg2(), ratedU0, exportedTerminalId(exportedTerminals, twt.getLeg2().getTerminal()), cimNamespace, writer);
            writeThreeWindingsTransformerEnd(twt.getId(), twt.getNameOrId() + "_3", CgmesExportUtil.getUniqueId(), 3, twt.getLeg3(), ratedU0, exportedTerminalId(exportedTerminals, twt.getLeg3().getTerminal()), cimNamespace, writer);
        }
    }

    private static void writeThreeWindingsTransformerEnd(String twtId, String twtName, String endId, int endNumber, ThreeWindingsTransformer.Leg leg, double ratedU0, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // structural ratio at end1
        double a0 = leg.getRatedU() / ratedU0;
        // move structural ratio from end1 to end2
        double a02 = a0 * a0;
        double r = leg.getR() * a02;
        double x = leg.getX() * a02;
        double g = leg.getG() / a02;
        double b = leg.getB() / a02;
        PowerTransformerEq.writeEnd(endId, twtName, twtId, endNumber, r, x, g, b, leg.getRatedU(), terminalId, cimNamespace, writer);
        writePhaseTapChanger(leg.getPhaseTapChanger(), twtName, endId, leg.getTerminal().getVoltageLevel().getNominalV(), cimNamespace, writer);
        writeRatioTapChanger(leg.getRatioTapChanger(), twtName, endId, cimNamespace, writer);
        writeFlowsLimits(leg, terminalId, cimNamespace, writer);
    }

    private static void writePhaseTapChanger(PhaseTapChanger ptc, String twtName, String endId, double neutralU, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (ptc != null) {
            int neutralStep = getPhaseTapChangerNeutralStep(ptc);
            String phaseTapChangerTableId = CgmesExportUtil.getUniqueId();
            TapChangerEq.writePhase(CgmesExportUtil.getUniqueId(), twtName + "_PTC", endId, ptc.getLowTapPosition(), ptc.getHighTapPosition(), neutralStep, ptc.getTapPosition(), neutralU, false, phaseTapChangerTableId, cimNamespace, writer);
            TapChangerEq.writePhaseTable(phaseTapChangerTableId, twtName + "_TABLE", cimNamespace, writer);
            for (Map.Entry<Integer, PhaseTapChangerStep> step : ptc.getAllSteps().entrySet()) {
                TapChangerEq.writePhaseTablePoint(CgmesExportUtil.getUniqueId(), phaseTapChangerTableId, step.getValue().getR(), step.getValue().getX(), step.getValue().getG(), step.getValue().getB(), 1 / step.getValue().getRho(), -step.getValue().getAlpha(), step.getKey(), cimNamespace, writer);
            }
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

    private static void writeRatioTapChanger(RatioTapChanger rtc, String twtName, String endId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (rtc != null) {
            int neutralStep = getRatioTapChangerNeutralStep(rtc);
            double stepVoltageIncrement = 100.0 * (1.0 / rtc.getStep(rtc.getLowTapPosition()).getRho() - 1.0) / (rtc.getLowTapPosition() - neutralStep);
            String ratioTapChangerTableId = CgmesExportUtil.getUniqueId();
            TapChangerEq.writeRatio(CgmesExportUtil.getUniqueId(), twtName + "_RTC", endId, rtc.getLowTapPosition(), rtc.getHighTapPosition(), neutralStep, rtc.getTapPosition(), rtc.getTargetV(), rtc.hasLoadTapChangingCapabilities(), stepVoltageIncrement, ratioTapChangerTableId, cimNamespace, writer);
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

    private static void writeDanglingLines(Network network, Map<Terminal, String> exportedTerminals, Map<Boundary, String> danglingLineBoundaries, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (DanglingLine danglingLine : network.getDanglingLines()) {

            String substationId = writeDanglingLineSubstation(danglingLine, cimNamespace, writer);
            String baseVoltageId = writeDanglingLineBaseVoltage(danglingLine, cimNamespace, writer);
            String voltageLevelId = writeDanglingLineVoltageLevel(danglingLine, substationId, baseVoltageId, cimNamespace, writer);
            String connectivityNodeId = writeDanglingLineConnectivity(danglingLine, voltageLevelId, danglingLineBoundaries, cimNamespace, writer);

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
                ReactiveLimits reactiveLimits = danglingLine.getGeneration().getReactiveLimits();
                if (danglingLine.getGeneration().getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
                    minQ = danglingLine.getGeneration().getReactiveLimits(MinMaxReactiveLimits.class).getMinQ();
                    maxQ = danglingLine.getGeneration().getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ();
                } else {
                    throw new PowsyblException("Unexpected type of ReactiveLimits on the dangling line " + danglingLine.getNameOrId());
                }
            }
            String equivalentInjectionId = CgmesExportUtil.getUniqueId();
            EquivalentInjectionEq.write(equivalentInjectionId, danglingLine.getNameOrId() + "_EI", danglingLine.getGeneration() != null, danglingLine.getGeneration() != null, minP, maxP, minQ, maxQ, baseVoltageId, cimNamespace, writer);
            TerminalEq.write(CgmesExportUtil.getUniqueId(), equivalentInjectionId, connectivityNodeId, 1, cimNamespace, writer);

            // Cast the danglingLine to an AcLineSegment
            AcLineSegmentEq.write(danglingLine.getId(), danglingLine.getNameOrId(), danglingLine.getR(), danglingLine.getX(), danglingLine.getB(), danglingLine.getR(), cimNamespace, writer);
            writeFlowsLimits(danglingLine, exportedTerminalId(exportedTerminals, danglingLine.getTerminal()), cimNamespace, writer);
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

    private static String writeDanglingLineBaseVoltage(DanglingLine danglingLine, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // New VoltageLevel
        String baseVoltageId = CgmesExportUtil.getUniqueId();
        BaseVoltageEq.write(baseVoltageId, danglingLine.getTerminal().getVoltageLevel().getNominalV(), cimNamespace, writer);

        return baseVoltageId;
    }

    private static String writeDanglingLineVoltageLevel(DanglingLine danglingLine, String substationId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // New VoltageLevel
        String voltageLevelId = CgmesExportUtil.getUniqueId();
        VoltageLevelEq.write(voltageLevelId, danglingLine.getNameOrId() + "_VL", substationId, baseVoltageId, cimNamespace, writer);

        return voltageLevelId;
    }

    private static String writeDanglingLineConnectivity(DanglingLine danglingLine, String voltageLevelId, Map<Boundary, String> danglingLineBoundaries, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // New ConnectivityNode
        String connectivityNodeId = CgmesExportUtil.getUniqueId();
        ConnectivityNodeEq.write(connectivityNodeId, danglingLine.getNameOrId() + "_NODE", voltageLevelId, cimNamespace, writer);
        // New Terminal
        String terminalId = CgmesExportUtil.getUniqueId();
        TerminalEq.write(terminalId, danglingLine.getId(), connectivityNodeId, 2, cimNamespace, writer);
        danglingLineBoundaries.put(danglingLine.getBoundary(), terminalId);

        return connectivityNodeId;
    }

    private static void writeBranchLimits(Branch branch, String terminalId1, String terminalId2, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (branch.getActivePowerLimits1() != null) {
            writeLoadingLimits(branch.getActivePowerLimits1(), terminalId1, cimNamespace, writer);
        }
        if (branch.getActivePowerLimits2() != null) {
            writeLoadingLimits(branch.getActivePowerLimits2(), terminalId2, cimNamespace, writer);
        }
        if (branch.getApparentPowerLimits1() != null) {
            writeLoadingLimits(branch.getApparentPowerLimits1(), terminalId1, cimNamespace, writer);
        }
        if (branch.getApparentPowerLimits2() != null) {
            writeLoadingLimits(branch.getApparentPowerLimits2(), terminalId2, cimNamespace, writer);
        }
        if (branch.getCurrentLimits1() != null) {
            writeLoadingLimits(branch.getCurrentLimits1(), terminalId1, cimNamespace, writer);
        }
        if (branch.getCurrentLimits2() != null) {
            writeLoadingLimits(branch.getCurrentLimits2(), terminalId2, cimNamespace, writer);
        }
    }

    private static void writeFlowsLimits(FlowsLimitsHolder holder, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (holder.getActivePowerLimits() != null) {
            writeLoadingLimits(holder.getActivePowerLimits(), terminalId, cimNamespace, writer);
        }
        if (holder.getApparentPowerLimits() != null) {
            writeLoadingLimits(holder.getApparentPowerLimits(), terminalId, cimNamespace, writer);
        }
        if (holder.getCurrentLimits() != null) {
            writeLoadingLimits(holder.getCurrentLimits(), terminalId, cimNamespace, writer);
        }
    }

    private static void writeLoadingLimits(LoadingLimits limits, String terminalId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (!Double.isNaN(limits.getPermanentLimit())) {
            String operationalLimitTypeId = CgmesExportUtil.getUniqueId();
            OperationalLimitTypeEq.writePatl(operationalLimitTypeId, cimNamespace, writer);
            String operationalLimitSetId = CgmesExportUtil.getUniqueId();
            OperationalLimitSetEq.write(operationalLimitSetId, "operational limit patl", terminalId, cimNamespace, writer);
            LoadingLimitEq.write(CgmesExportUtil.getUniqueId(), limits.getClass(), "CurrentLimit", limits.getPermanentLimit(), operationalLimitTypeId, operationalLimitSetId, cimNamespace, writer);
        }
        if (!limits.getTemporaryLimits().isEmpty()) {
            Iterator<LoadingLimits.TemporaryLimit> iterator = limits.getTemporaryLimits().iterator();
            while (iterator.hasNext()) {
                LoadingLimits.TemporaryLimit temporaryLimit = iterator.next();
                String operationalLimitTypeId = CgmesExportUtil.getUniqueId();
                OperationalLimitTypeEq.writeTatl(operationalLimitTypeId, temporaryLimit.getName(), temporaryLimit.getAcceptableDuration(), cimNamespace, writer);
                String operationalLimitSetId = CgmesExportUtil.getUniqueId();
                OperationalLimitSetEq.write(operationalLimitSetId, "operational limit tatl", terminalId, cimNamespace, writer);
                LoadingLimitEq.write(CgmesExportUtil.getUniqueId(), limits.getClass(), "CurrentLimit", temporaryLimit.getValue(), operationalLimitTypeId, operationalLimitSetId, cimNamespace, writer);
            }
        }
    }

    private static void writeHvdcLines(Network network, Map<Terminal, String> exportedTerminals, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            String dcConverterUnit1 = CgmesExportUtil.getUniqueId();
            writeDCConverterUnit(dcConverterUnit1, line.getNameOrId() + "_1", line.getConverterStation1().getTerminal().getVoltageLevel().getSubstation().getId(), cimNamespace, writer);
            String dcNode1 = CgmesExportUtil.getUniqueId();
            writeDCNode(dcNode1, line.getNameOrId() + "_1", dcConverterUnit1, cimNamespace, writer);
            String dcConverterUnit2 = CgmesExportUtil.getUniqueId();
            writeDCConverterUnit(dcConverterUnit2, line.getNameOrId() + "_1", line.getConverterStation2().getTerminal().getVoltageLevel().getSubstation().getId(), cimNamespace, writer);
            String dcNode2 = CgmesExportUtil.getUniqueId();
            writeDCNode(dcNode2, line.getNameOrId() + "_2", dcConverterUnit2, cimNamespace, writer);
            writeDCTerminal(CgmesExportUtil.getUniqueId(), line.getId(), dcNode1, 1, cimNamespace, writer);
            writeDCTerminal(CgmesExportUtil.getUniqueId(), line.getId(), dcNode2, 2, cimNamespace, writer);
            HvdcConverterStation converter = line.getConverterStation1();
            writeTerminal(converter.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), converter.getId(), connectivityNodeId(exportedNodes, converter.getTerminal()), 1, cimNamespace, writer);
            writeAcdcConverterDCTerminal(CgmesExportUtil.getUniqueId(), converter.getId(), dcNode1, 2, cimNamespace, writer);
            converter = line.getConverterStation2();
            writeTerminal(converter.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), converter.getId(), connectivityNodeId(exportedNodes, converter.getTerminal()), 1, cimNamespace, writer);
            writeAcdcConverterDCTerminal(CgmesExportUtil.getUniqueId(), converter.getId(), dcNode2, 2, cimNamespace, writer);
            DCLineSegmentEq.write(line.getId(), line.getNameOrId(), line.getR(), cimNamespace, writer);
            writeHvdcConverterStation(network, line.getConverterStation1(), line.getNominalV(), dcConverterUnit1, cimNamespace, writer);
            writeHvdcConverterStation(network, line.getConverterStation2(), line.getNominalV(), dcConverterUnit2, cimNamespace, writer);
        }
    }

    private static void writeDCConverterUnit(String id, String dcConverterUnitName, String substationId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        DCConverterUnitEq.write(id, dcConverterUnitName, substationId, cimNamespace, writer);
    }

    private static void writeHvdcConverterStation(Network network, HvdcConverterStation<?> converterStation, double ratedUdc, String dcEquipmentContainerId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        HvdcConverterStationEq.write(converterStation.getId(), converterStation.getNameOrId(), converterStation.getHvdcType(), ratedUdc, dcEquipmentContainerId, cimNamespace, writer);
    }

    private static void writeDCNode(String id, String dcNodeName, String dcEquipmentContainerId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        DCNodeEq.write(id, dcNodeName, dcEquipmentContainerId, cimNamespace, writer);
    }

    private static void writeDCTerminal(String id, String conductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        DCTerminalEq.write(id, conductingEquipmentId, dcNodeId, sequenceNumber, cimNamespace, writer);
    }

    private static void writeAcdcConverterDCTerminal(String id, String conductingEquipmentId, String dcNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        AcdcConverterDCTerminalEq.write(id, conductingEquipmentId, dcNodeId, sequenceNumber, cimNamespace, writer);
    }

    private static void writeControlAreas(Network network, Map<Terminal, String> exportedTerminals, Map<Boundary, String> danglingLineBoundaries, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        if (cgmesControlAreas != null) {
            for (CgmesControlArea cgmesControlArea : cgmesControlAreas.getCgmesControlAreas()) {
                ControlAreaEq.write(cgmesControlArea.getId(), cgmesControlArea.getName(), cgmesControlArea.getEnergyIdentificationCodeEIC(), cimNamespace, writer);
                for (Terminal terminal : cgmesControlArea.getTerminals()) {
                    TieFlowEq.write(CgmesExportUtil.getUniqueId(), cgmesControlArea.getId(), exportedTerminalId(exportedTerminals, terminal), cimNamespace, writer);
                }
                for (Boundary boundary : cgmesControlArea.getBoundaries()) {
                    if (cgmesControlArea.getBoundaries().contains(boundary)) {
                        TieFlowEq.write(CgmesExportUtil.getUniqueId(), cgmesControlArea.getId(), danglingLineBoundaries.get(boundary), cimNamespace, writer);
                    }
                }
            }
        } else {
            String cgmesControlAreaId = CgmesExportUtil.getUniqueId();
            ControlAreaEq.write(cgmesControlAreaId, "Network", "Network--1", cimNamespace, writer);
            for (DanglingLine danglingLine : network.getDanglingLines()) {
                TieFlowEq.write(CgmesExportUtil.getUniqueId(), cgmesControlAreaId, exportedTerminalId(exportedTerminals, danglingLine.getTerminal()), cimNamespace, writer);
            }
        }
    }

    private static void writeTerminals(Network network, Map<Terminal, String> exportedTerminals, Map<String, String> exportedNodes, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            writeTerminal(load.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), load.getId(), connectivityNodeId(exportedNodes, load.getTerminal()), 1, cimNamespace, writer);
        }
        for (Generator generator : network.getGenerators()) {
            writeTerminal(generator.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), generator.getId(), connectivityNodeId(exportedNodes, generator.getTerminal()), 1, cimNamespace, writer);
        }
        for (ShuntCompensator s : network.getShuntCompensators()) {
            writeTerminal(s.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), s.getId(), connectivityNodeId(exportedNodes, s.getTerminal()), 1, cimNamespace, writer);
        }
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            writeTerminal(svc.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), svc.getId(), connectivityNodeId(exportedNodes, svc.getTerminal()), 1, cimNamespace, writer);
        }
        for (Line line : network.getLines()) {
            writeTerminal(line.getTerminal1(), exportedTerminals, CgmesExportUtil.getUniqueId(), line.getId(), connectivityNodeId(exportedNodes, line.getTerminal1()), 1, cimNamespace, writer);
            writeTerminal(line.getTerminal2(), exportedTerminals, CgmesExportUtil.getUniqueId(), line.getId(), connectivityNodeId(exportedNodes, line.getTerminal2()), 2, cimNamespace, writer);
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            writeTerminal(twt.getTerminal1(), exportedTerminals, CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getTerminal1()), 1, cimNamespace, writer);
            writeTerminal(twt.getTerminal2(), exportedTerminals, CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getTerminal2()), 2, cimNamespace, writer);
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            writeTerminal(twt.getLeg1().getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg1().getTerminal()), 1, cimNamespace, writer);
            writeTerminal(twt.getLeg2().getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg2().getTerminal()), 2, cimNamespace, writer);
            writeTerminal(twt.getLeg3().getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), twt.getId(), connectivityNodeId(exportedNodes, twt.getLeg3().getTerminal()), 3, cimNamespace, writer);
        }
        for (DanglingLine danglingLine : network.getDanglingLines()) {
            writeTerminal(danglingLine.getTerminal(), exportedTerminals, CgmesExportUtil.getUniqueId(), danglingLine.getId(), connectivityNodeId(exportedNodes, danglingLine.getTerminal()), 1, cimNamespace, writer);
        }
    }

    private static void writeTerminal(Terminal terminal, Map<Terminal, String> exportedTerminals, String id, String conductingEquipmentId, String connectivityNodeId, int sequenceNumber, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        if (!exportedTerminals.containsKey(terminal)) {
            TerminalEq.write(id, conductingEquipmentId, connectivityNodeId, sequenceNumber, cimNamespace, writer);
            exportedTerminals.put(terminal, id);
        }
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
            key = terminal.getVoltageLevel().getId() + terminal.getNodeBreakerView().getNode();
        } else {
            key = terminal.getBusBreakerView().getBus().getId();
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
                vl.getNodeBreakerView().getInternalConnections().forEach(ic -> computeInternalConnectionAdjacency(ic));
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
