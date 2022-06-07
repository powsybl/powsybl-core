/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.SwitchesFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class StateVariablesExport {

    private static final String SV_VOLTAGE_ANGLE = "SvVoltage.angle";
    private static final String SV_VOLTAGE_V = "SvVoltage.v";
    private static final String SV_VOLTAGE_TOPOLOGICAL_NODE = "SvVoltage.TopologicalNode";

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String cimNamespace = context.getCim().getNamespace();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getSvModelDescription(), context);
                writeTopologicalIslands(network, cimNamespace, writer, context);
                // Note: unmapped topological nodes (node breaker) & boundary topological nodes are not written in topological islands
            }

            writeVoltagesForTopologicalNodes(network, cimNamespace, writer, context);
            writeVoltagesForBoundaryNodes(network, cimNamespace, writer);
            for (CgmesIidmMapping.CgmesTopologicalNode tn : context.getUnmappedTopologicalNodes()) {
                writeVoltage(tn.getCgmesId(), 0.0, 0.0, cimNamespace, writer);
            }
            writePowerFlows(network, cimNamespace, writer, context);
            writeShuntCompensatorSections(network, cimNamespace, writer, context);
            writeTapSteps(network, cimNamespace, writer, context);
            writeStatus(network, cimNamespace, writer, context);
            writeConverters(network, cimNamespace, writer, context);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTopologicalIslands(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Map<String, String> angleRefs = buildAngleRefs(network, context);
        Map<String, List<String>> islands = buildIslands(network, context);
        for (Map.Entry<String, List<String>> island : islands.entrySet()) {
            if (!angleRefs.containsKey(island.getKey())) {
                Supplier<String> log = () -> String.format("Synchronous component  %s does not have a defined slack bus: it is ignored", island.getKey());
                LOG.info(log.get());
                continue;
            }
            String islandId = CgmesExportUtil.getUniqueId();
            CgmesExportUtil.writeStartId(CgmesNames.TOPOLOGICAL_ISLAND, islandId, false, cimNamespace, writer);
            writer.writeStartElement(cimNamespace, CgmesNames.NAME);
            writer.writeCharacters(islandId); // Use id as name
            writer.writeEndElement();
            CgmesExportUtil.writeReference("TopologicalIsland.AngleRefTopologicalNode", angleRefs.get(island.getKey()), cimNamespace, writer);
            for (String tn : island.getValue()) {
                CgmesExportUtil.writeReference("TopologicalIsland.TopologicalNodes", tn, cimNamespace, writer);
            }
            writer.writeEndElement();
        }
    }

    private static Map<String, String> buildAngleRefs(Network network, CgmesExportContext context) {
        Map<String, String> angleRefs = new HashMap<>();
        for (VoltageLevel vl : network.getVoltageLevels()) {
            SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
            buildAngleRefs(slackTerminal, angleRefs, context);
        }
        return angleRefs;
    }

    private static void buildAngleRefs(SlackTerminal slackTerminal, Map<String, String> angleRefs, CgmesExportContext context) {
        if (slackTerminal != null && slackTerminal.getTerminal() != null) {
            Bus bus = slackTerminal.getTerminal().getBusView().getBus();
            if (bus != null && bus.getSynchronousComponent() != null) {
                buildAngleRefs(bus.getSynchronousComponent().getNum(), bus.getId(), angleRefs, context);
            } else if (bus != null) {
                buildAngleRefs(bus.getId(), angleRefs, context);
            } else {
                Supplier<String> message = () -> String.format("Slack terminal at equipment %s is not connected and is not exported as slack terminal", slackTerminal.getTerminal().getConnectable().getId());
                LOG.info(message.get());
            }
        }
    }

    private static void buildAngleRefs(int synchronousComponentNum, String busId, Map<String, String> angleRefs, CgmesExportContext context) {
        String componentNum = String.valueOf(synchronousComponentNum);
        if (angleRefs.containsKey(componentNum)) {
            Supplier<String> log = () -> String.format("Several slack buses are defined for synchronous component %s: only first slack bus (%s) is taken into account",
                    componentNum, angleRefs.get(componentNum));
            LOG.info(log.get());
            return;
        }
        CgmesIidmMapping.CgmesTopologicalNode topologicalNode = context.getTopologicalNodesByBusViewBus(busId).iterator().next();
        angleRefs.put(componentNum, topologicalNode.getCgmesId());
    }

    private static void buildAngleRefs(String busId, Map<String, String> angleRefs, CgmesExportContext context) {
        CgmesIidmMapping.CgmesTopologicalNode topologicalNode = context.getTopologicalNodesByBusViewBus(busId).iterator().next();
        angleRefs.put(topologicalNode.getCgmesId(),
                topologicalNode.getCgmesId());
    }

    private static Map<String, List<String>> buildIslands(Network network, CgmesExportContext context) {
        Map<String, List<String>> islands = new HashMap<>();
        for (Bus b : network.getBusView().getBuses()) {
            if (b.getSynchronousComponent() != null) {
                int num = b.getSynchronousComponent().getNum();
                islands.computeIfAbsent(String.valueOf(num), i -> new ArrayList<>());
                islands.get(String.valueOf(num)).addAll(context.getTopologicalNodesByBusViewBus(b.getId()).stream().map(CgmesIidmMapping.CgmesTopologicalNode::getCgmesId).collect(Collectors.toSet()));
            } else {
                islands.put(b.getId(), Collections.singletonList(b.getId()));
            }
        }
        return islands;
    }

    private static void writeVoltagesForTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusView().getBuses()) {
            for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b.getId())) {
                writeVoltage(topologicalNode.getCgmesId(), b.getV(), b.getAngle(), cimNamespace, writer);
            }
        }
    }

    private static void writeVoltagesForBoundaryNodes(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Bus b = dl.getTerminal().getBusView().getBus();
            Optional<String> topologicalNode = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNode.isPresent()) {
                if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                    writeVoltage(topologicalNode.get(), Double.valueOf(dl.getProperty("v", "NaN")), Double.valueOf(dl.getProperty("angle", "NaN")), cimNamespace, writer);
                } else if (b != null) {
                    writeVoltage(topologicalNode.get(), dl.getBoundary().getV(), dl.getBoundary().getAngle(), cimNamespace, writer);
                } else {
                    writeVoltage(topologicalNode.get(), 0.0, 0.0, cimNamespace, writer);
                }
            }
        }
        // Voltages at inner nodes of Tie Lines
        // (boundary nodes that have been left inside CGM)
        for (Line l : network.getLines()) {
            if (!l.isTieLine()) {
                continue;
            }
            TieLine tieLine = (TieLine) l;
            String topologicalNode = tieLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE)
                    .orElseGet(() -> tieLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tieLine.getHalf1().getId() + "." + CgmesNames.TOPOLOGICAL_NODE));
            if (topologicalNode != null) {
                writeVoltage(topologicalNode, tieLine.getHalf1().getBoundary().getV(), tieLine.getHalf1().getBoundary().getAngle(), cimNamespace, writer);
            }
        }
    }

    private static void writeVoltage(String topologicalNode, double v, double angle, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartId("SvVoltage", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_ANGLE);
        writer.writeCharacters(CgmesExportUtil.format(angle));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_V);
        writer.writeCharacters(CgmesExportUtil.format(v));
        writer.writeEndElement();
        CgmesExportUtil.writeReference(SV_VOLTAGE_TOPOLOGICAL_NODE, topologicalNode, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writePowerFlows(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getLoadStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getGeneratorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getShuntCompensatorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getStaticVarCompensatorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getBatteryStream);

        // Fictitious loads are not exported as Equipment, they are just added to SV as SvInjection
        for (Load load : network.getLoads()) {
            if (load.isFictitious()) {
                writeSvInjection(load, cimNamespace, writer, context);
            }
        }

        network.getDanglingLineStream().forEach(dl -> {
            // FIXME: the values (p0/q0) are wrong: these values are target and never updated, not calculated flows
            // DanglingLine's attributes will be created to store calculated flows on the boundary side
            if (context.exportBoundaryPowerFlows()) {
                writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary", dl.getBoundary().getP(), dl.getBoundary().getQ(), cimNamespace, writer, context);
            }
            writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal", -dl.getBoundary().getP(), -dl.getBoundary().getQ(), cimNamespace, writer, context);
        });

        network.getBranchStream().forEach(b -> {
            writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, b.getTerminal1().getP(), b.getTerminal1().getQ(), cimNamespace, writer, context);
            writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, b.getTerminal2().getP(), b.getTerminal2().getQ(), cimNamespace, writer, context);
            if (b instanceof TieLine && context.exportBoundaryPowerFlows()) {
                TieLine tl = (TieLine) b;
                writePowerFlowTerminalFromProperty(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Network",
                        tl.getTerminal1().getP(), tl.getTerminal1().getQ(), cimNamespace, writer, context);
                writePowerFlowTerminalFromProperty(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Network",
                        tl.getTerminal2().getP(), tl.getTerminal2().getQ(), cimNamespace, writer, context);
                writePowerFlowTerminalFromAlias(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF1." + CgmesNames.TERMINAL + "_Boundary", tl.getHalf1().getBoundary().getP(), tl.getHalf1().getBoundary().getQ(), cimNamespace, writer, context);
                writePowerFlowTerminalFromAlias(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF2." + CgmesNames.TERMINAL + "_Boundary", tl.getHalf2().getBoundary().getP(), tl.getHalf2().getBoundary().getQ(), cimNamespace, writer, context);
                if (tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Boundary") != null) {
                    throw new PowsyblException("No need for property " + Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf1().getId() + ".Terminal_Boundary");
                }
                if (tl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Boundary") != null) {
                    throw new PowsyblException("No need for property " + Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + tl.getHalf2().getId() + ".Terminal_Boundary");
                }
            }
        });

        network.getThreeWindingsTransformerStream().forEach(twt -> {
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, twt.getLeg1().getTerminal().getP(), twt.getLeg1().getTerminal().getQ(), cimNamespace, writer, context);
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, twt.getLeg2().getTerminal().getP(), twt.getLeg2().getTerminal().getQ(), cimNamespace, writer, context);
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL3, twt.getLeg3().getTerminal().getP(), twt.getLeg3().getTerminal().getQ(), cimNamespace, writer, context);
        });

        if (context.exportFlowsForSwitches()) {
            network.getVoltageLevelStream().forEach(vl -> {
                SwitchesFlow swflows = new SwitchesFlow(vl);
                vl.getSwitches().forEach(sw -> {
                    if (swflows.hasFlow(sw.getId())) {
                        writePowerFlowTerminalFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, swflows.getP1(sw.getId()), swflows.getQ1(sw.getId()), cimNamespace, writer, context);
                        writePowerFlowTerminalFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, swflows.getP2(sw.getId()), swflows.getQ2(sw.getId()), cimNamespace, writer, context);
                    }
                });
            });
        }
    }

    private static <I extends Injection<I>> void writeInjectionsPowerFlows(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context, Function<Network, Stream<I>> getInjectionStream) {
        getInjectionStream.apply(network).forEach(i -> {
            if (context.isExportedEquipment(i)) {
                writePowerFlow(i.getTerminal(), cimNamespace, writer, context);
            }
        });
    }

    private static void writePowerFlow(Terminal terminal, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        String cgmesTerminal = CgmesExportUtil.getTerminalId(terminal, context);
        if (cgmesTerminal != null) {
            writePowerFlow(cgmesTerminal, getTerminalP(terminal), terminal.getQ(), cimNamespace, writer);
        } else {
            LOG.error("No defined CGMES terminal for {}", terminal.getConnectable().getId());
        }
    }

    private static double getTerminalP(Terminal terminal) {
        double p = terminal.getP();
        if (!Double.isNaN(p)) {
            return p;
        }
        // P is NaN
        if (Double.isNaN(terminal.getQ())) {
            return p;
        }
        // P is NaN and Q != NaN
        if (terminal.getConnectable() instanceof StaticVarCompensator) {
            return 0.0;
        }
        if (terminal.getConnectable() instanceof ShuntCompensator) {
            return 0.0;
        }
        return p;
    }

    private static void writeSvInjection(Load load, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Fictitious loads are created in IIDM to keep track of mismatches in the input case,
        // These mismatches are given by SvInjection CGMES objects
        // These loads have been taken into account as inputs for potential power flow analysis
        // They will be written back as SvInjection objects in the SV profile
        // We do not want to export them back as new objects in the EQ profile
        Bus bus = load.getTerminal().getBusView().getBus();
        if (bus == null) {
            LOG.warn("Fictitious load does not have a BusView bus. No SvInjection is written");
        } else {
            // SvInjection will be assigned to the first of the TNs mapped to the bus
            CgmesIidmMapping.CgmesTopologicalNode topologicalNode = context.getTopologicalNodesByBusViewBus(bus.getId()).iterator().next();
            writeSvInjection(load, topologicalNode.getCgmesId(), cimNamespace, writer, context);
        }
    }

    private static void writePowerFlowTerminalFromAlias(Identifiable<?> c, String aliasTypeForTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Export only if we have a terminal identifier
        if (c.getAliasFromType(aliasTypeForTerminalId).isPresent()) {
            String cgmesTerminalId = context.getNamingStrategy().getCgmesIdFromAlias(c, aliasTypeForTerminalId);
            writePowerFlow(cgmesTerminalId, p, q, cimNamespace, writer);
        }
    }

    private static void writePowerFlowTerminalFromProperty(Identifiable<?> c, String propertyNameForTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Export only if we have a terminal identifier
        String cgmesTerminalId = context.getNamingStrategy().getCgmesIdFromProperty(c, propertyNameForTerminalId);
        if (cgmesTerminalId != null) {
            writePowerFlow(cgmesTerminalId, p, q, cimNamespace, writer);
        }
    }

    private static void writePowerFlow(String cgmesTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer) {
        // Export only if flow is a number
        if (Double.isNaN(p) && Double.isNaN(q)) {
            return;
        }
        try {
            CgmesExportUtil.writeStartId("SvPowerFlow", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer);
            writer.writeStartElement(cimNamespace, "SvPowerFlow.p");
            writer.writeCharacters(CgmesExportUtil.format(p));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvPowerFlow.q");
            writer.writeCharacters(CgmesExportUtil.format(q));
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvPowerFlow.Terminal", cgmesTerminalId, cimNamespace, writer);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSvInjection(Load svInjection, String topologicalNode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeStartId("SvInjection", context.getNamingStrategy().getCgmesId(svInjection), false, cimNamespace, writer);
            writer.writeStartElement(cimNamespace, "SvInjection.pInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getP0()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvInjection.qInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getQ0()));
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvInjection.TopologicalNode", topologicalNode, cimNamespace, writer);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeShuntCompensatorSections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            CgmesExportUtil.writeStartId("SvShuntCompensatorSections", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer);
            CgmesExportUtil.writeReference("SvShuntCompensatorSections.ShuntCompensator",  context.getNamingStrategy().getCgmesId(s), cimNamespace, writer);
            writer.writeStartElement(cimNamespace, "SvShuntCompensatorSections.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeTapSteps(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            // For two-windings transformers we export tap changers always at end1, where IIDM has modelled them
            int endNumber = 1;
            if (twt.hasPhaseTapChanger()) {
                String ptcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber);
                writeSvTapStep(ptcId, twt.getPhaseTapChanger().getTapPosition(), cimNamespace, writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber);
                writeSvTapStep(rtcId, twt.getRatioTapChanger().getTapPosition(), cimNamespace, writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int endNumber = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber);
                    writeSvTapStep(ptcId, leg.getPhaseTapChanger().getTapPosition(), cimNamespace, writer);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber);
                    writeSvTapStep(rtcId, leg.getRatioTapChanger().getTapPosition(), cimNamespace, writer);
                }
                endNumber++;
            }
        }
    }

    private static void writeSvTapStep(String tapChangerId, int tapPosition, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartId("SvTapStep", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, "SvTapStep.position");
        writer.writeCharacters(CgmesExportUtil.format(tapPosition));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("SvTapStep.TapChanger", tapChangerId, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writeStatus(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // create SvStatus, iterate on Connectables, check Terminal status, add to SvStatus
        network.getConnectableStream().forEach(c -> {
            if (context.isExportedEquipment(c)) {
                writeConnectableStatus(c, cimNamespace, writer, context);
            }
        });

        // RK: For dangling lines (boundaries), the AC Line Segment is considered in service if and only if it is connected on the network side.
        // If it is disconnected on the boundary side, it might not appear on the SV file.
    }

    private static void writeConnectableStatus(Connectable<?> connectable, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        writeStatus(Boolean.toString(connectable.getTerminals().stream().anyMatch(Terminal::isConnected)), context.getNamingStrategy().getCgmesId(connectable), cimNamespace, writer);
    }

    private static void writeStatus(String inService, String conductingEquipmentId, String cimNamespace, XMLStreamWriter writer) {
        try {
            CgmesExportUtil.writeStartId("SvStatus", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer);
            writer.writeStartElement(cimNamespace, "SvStatus.inService");
            writer.writeCharacters(inService);
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvStatus.ConductingEquipment", conductingEquipmentId, cimNamespace, writer);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeConverters(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : network.getHvdcConverterStations()) {
            CgmesExportUtil.writeStartAbout(CgmesExportUtil.converterClassName(converterStation), context.getNamingStrategy().getCgmesId(converterStation), cimNamespace, writer);
            writer.writeStartElement(cimNamespace, "ACDCConverter.poleLossP");
            writer.writeCharacters(CgmesExportUtil.format(getPoleLossP(converterStation)));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "ACDCConverter.idc");
            writer.writeCharacters(CgmesExportUtil.format(0));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "ACDCConverter.uc");
            writer.writeCharacters(CgmesExportUtil.format(0));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "ACDCConverter.udc");
            writer.writeCharacters(CgmesExportUtil.format(0));
            writer.writeEndElement();
            if (converterStation instanceof LccConverterStation) {
                writer.writeStartElement(cimNamespace, "CsConverter.alpha");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "CsConverter.gamma");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
            } else if (converterStation instanceof VscConverterStation) {
                writer.writeStartElement(cimNamespace, "VsConverter.delta");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
                writer.writeStartElement(cimNamespace, "VsConverter.uf");
                writer.writeCharacters(CgmesExportUtil.format(0));
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    private static double getPoleLossP(HvdcConverterStation<?> converterStation) {
        double poleLoss;
        if (CgmesExportUtil.isConverterStationRectifier(converterStation)) {
            double p = converterStation.getTerminal().getP();
            if (Double.isNaN(p)) {
                p = converterStation.getHvdcLine().getActivePowerSetpoint();
            }
            poleLoss = p * converterStation.getLossFactor() / 100;
        } else {
            double p = converterStation.getTerminal().getP();
            if (Double.isNaN(p)) {
                p = converterStation.getHvdcLine().getActivePowerSetpoint();
            }
            double otherConverterStationLossFactor = converterStation.getOtherConverterStation().map(HvdcConverterStation::getLossFactor).orElse(0.0f);
            double pDCInverter = Math.abs(p) * (1 - otherConverterStationLossFactor / 100);
            poleLoss = pDCInverter * converterStation.getLossFactor() / 100;
        }
        return poleLoss;
    }

    private StateVariablesExport() {
    }
}
