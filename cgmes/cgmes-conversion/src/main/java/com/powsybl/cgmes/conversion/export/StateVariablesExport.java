/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
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
        write(network, writer, new CgmesExportContext(network).setExportEquipment(false));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String cimNamespace = context.getCim().getNamespace();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getSvModelDescription(), context);
                writeTopologicalIslands(network, context, writer);
                // Note: unmapped topological nodes (node breaker) & boundary topological nodes are not written in topological islands
            }

            writeVoltagesForTopologicalNodes(network, context, writer);
            writeVoltagesForBoundaryNodes(network, cimNamespace, writer, context);
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

    private static void writeTopologicalIslands(Network network, CgmesExportContext context, XMLStreamWriter writer) throws XMLStreamException {
        Map<String, String> angleRefs = buildAngleRefs(network, context);
        Map<String, List<String>> islands = buildIslands(network, context);
        String cimNamespace = context.getCim().getNamespace();
        for (Map.Entry<String, List<String>> island : islands.entrySet()) {
            if (!angleRefs.containsKey(island.getKey())) {
                Supplier<String> log = () -> String.format("Synchronous component  %s does not have a defined slack bus: it is ignored", island.getKey());
                LOG.info(log.get());
                continue;
            }
            String islandId = CgmesExportUtil.getUniqueId();
            CgmesExportUtil.writeStartId(CgmesNames.TOPOLOGICAL_ISLAND, islandId, false, cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, CgmesNames.NAME);
            writer.writeCharacters(islandId); // Use id as name
            writer.writeEndElement();
            CgmesExportUtil.writeReference("TopologicalIsland.AngleRefTopologicalNode", angleRefs.get(island.getKey()), cimNamespace, writer, context);
            for (String tn : island.getValue()) {
                CgmesExportUtil.writeReference("TopologicalIsland.TopologicalNodes", tn, cimNamespace, writer, context);
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
            Bus bus = slackTerminal.getTerminal().getBusBreakerView().getBus();
            if (bus != null && bus.getSynchronousComponent() != null) {
                buildAngleRefs(bus.getSynchronousComponent().getNum(), bus, angleRefs, context);
            } else if (bus != null) {
                buildAngleRefs(bus, angleRefs, context);
            } else {
                Supplier<String> message = () -> String.format("Slack terminal at equipment %s is not connected and is not exported as slack terminal", slackTerminal.getTerminal().getConnectable().getId());
                LOG.info(message.get());
            }
        }
    }

    private static void buildAngleRefs(int synchronousComponentNum, Bus bus, Map<String, String> angleRefs, CgmesExportContext context) {
        String componentNum = String.valueOf(synchronousComponentNum);
        if (angleRefs.containsKey(componentNum)) {
            Supplier<String> log = () -> String.format("Several slack buses are defined for synchronous component %s: only first slack bus (%s) is taken into account",
                    componentNum, angleRefs.get(componentNum));
            LOG.info(log.get());
            return;
        }
        String topologicalNodeId = context.getNamingStrategy().getCgmesId(bus);
        angleRefs.put(componentNum, topologicalNodeId);
    }

    private static void buildAngleRefs(Bus bus, Map<String, String> angleRefs, CgmesExportContext context) {
        String topologicalNodeId = context.getNamingStrategy().getCgmesId(bus);
        angleRefs.put(topologicalNodeId, topologicalNodeId);
    }

    private static Map<String, List<String>> buildIslands(Network network, CgmesExportContext context) {
        Map<String, List<String>> islands = new HashMap<>();
        for (Bus b : network.getBusBreakerView().getBuses()) {
            String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
            if (b.getSynchronousComponent() != null) {
                int num = b.getSynchronousComponent().getNum();
                islands.computeIfAbsent(String.valueOf(num), i -> new ArrayList<>());
                islands.get(String.valueOf(num)).add(topologicalNodeId);
            } else {
                islands.put(topologicalNodeId, Collections.singletonList(topologicalNodeId));
            }
        }
        return islands;
    }

    private static void writeVoltagesForTopologicalNodes(Network network, CgmesExportContext context, XMLStreamWriter writer) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        for (Bus b : network.getBusBreakerView().getBuses()) {
            String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
            writeVoltage(topologicalNodeId, b.getV(), b.getAngle(), cimNamespace, writer, context);
        }
    }

    private static void writeVoltagesForBoundaryNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Bus b = dl.getTerminal().getBusView().getBus();
            String topologicalNode = dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
            if (topologicalNode != null) {
                if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                    writeVoltage(topologicalNode, Double.parseDouble(dl.getProperty("v", "NaN")), Double.parseDouble(dl.getProperty("angle", "NaN")), cimNamespace, writer, context);
                } else if (b != null) {
                    writeVoltage(topologicalNode, dl.getBoundary().getV(), dl.getBoundary().getAngle(), cimNamespace, writer, context);
                } else {
                    writeVoltage(topologicalNode, 0.0, 0.0, cimNamespace, writer, context);
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
            String topologicalNode = tieLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY)
                    .orElseGet(() -> tieLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE + "_1"));
            if (topologicalNode != null) {
                writeVoltage(topologicalNode, tieLine.getHalf1().getBoundary().getV(), tieLine.getHalf1().getBoundary().getAngle(), cimNamespace, writer, context);
            }
        }
    }

    private static void writeVoltage(String topologicalNode, double v, double angle, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId("SvVoltage", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_ANGLE);
        writer.writeCharacters(CgmesExportUtil.format(angle));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_V);
        writer.writeCharacters(CgmesExportUtil.format(v));
        writer.writeEndElement();
        CgmesExportUtil.writeReference(SV_VOLTAGE_TOPOLOGICAL_NODE, topologicalNode, cimNamespace, writer, context);
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

        Map<String, Double> equivalentInjectionTerminalP = new HashMap<>();
        Map<String, Double> equivalentInjectionTerminalQ = new HashMap<>();
        network.getDanglingLineStream().forEach(dl -> {
            // FIXME: the values (p0/q0) are wrong: these values are target and never updated, not calculated flows
            // DanglingLine's attributes will be created to store calculated flows on the boundary side
            if (context.exportBoundaryPowerFlows()) {
                writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary", dl.getBoundary().getP(), dl.getBoundary().getQ(), cimNamespace, writer, context);
            }
            writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal", dl.getTerminal().getP(), dl.getTerminal().getQ(), cimNamespace, writer, context);
            equivalentInjectionTerminalP.compute(context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -dl.getBoundary().getP() : v - dl.getBoundary().getP());
            equivalentInjectionTerminalQ.compute(context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -dl.getBoundary().getQ() : v - dl.getBoundary().getQ());
        });
        equivalentInjectionTerminalP.keySet().forEach(eiId -> writePowerFlow(eiId, equivalentInjectionTerminalP.get(eiId), equivalentInjectionTerminalQ.get(eiId), cimNamespace, writer, context));

        network.getBranchStream().forEach(b -> {
            writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, b.getTerminal1().getP(), b.getTerminal1().getQ(), cimNamespace, writer, context);
            writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, b.getTerminal2().getP(), b.getTerminal2().getQ(), cimNamespace, writer, context);
            if (b instanceof TieLine) {
                TieLine tl = (TieLine) b;
                if (context.exportBoundaryPowerFlows()) {
                    writePowerFlowTerminalFromAlias(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_1", tl.getHalf1().getBoundary().getP(), tl.getHalf1().getBoundary().getQ(), cimNamespace, writer, context);
                    writePowerFlowTerminalFromAlias(tl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_2", tl.getHalf2().getBoundary().getP(), tl.getHalf2().getBoundary().getQ(), cimNamespace, writer, context);
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
                    if (context.isExportedEquipment(sw) && swflows.hasFlow(sw.getId())) {
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
            writePowerFlow(cgmesTerminal, getTerminalP(terminal), terminal.getQ(), cimNamespace, writer, context);
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
        return p;
    }

    private static void writeSvInjection(Load load, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Fictitious loads are created in IIDM to keep track of mismatches in the input case,
        // These mismatches are given by SvInjection CGMES objects
        // These loads have been taken into account as inputs for potential power flow analysis
        // They will be written back as SvInjection objects in the SV profile
        // We do not want to export them back as new objects in the EQ profile
        Bus bus = load.getTerminal().getBusBreakerView().getBus();
        if (bus == null) {
            LOG.warn("Fictitious load does not have a BusView bus. No SvInjection is written");
        } else {
            // SvInjection will be assigned to the first of the TNs mapped to the bus
            writeSvInjection(load, bus.getId(), cimNamespace, writer, context);
        }
    }

    private static void writePowerFlowTerminalFromAlias(Identifiable<?> c, String aliasTypeForTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Export only if we have a terminal identifier
        if (c.getAliasFromType(aliasTypeForTerminalId).isPresent()) {
            String cgmesTerminalId = context.getNamingStrategy().getCgmesIdFromAlias(c, aliasTypeForTerminalId);
            writePowerFlow(cgmesTerminalId, p, q, cimNamespace, writer, context);
        }
    }

    private static void writePowerFlowTerminalFromProperty(Identifiable<?> c, String propertyNameForTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Export only if we have a terminal identifier
        String cgmesTerminalId = context.getNamingStrategy().getCgmesIdFromProperty(c, propertyNameForTerminalId);
        if (cgmesTerminalId != null) {
            writePowerFlow(cgmesTerminalId, p, q, cimNamespace, writer, context);
        }
    }

    private static void writePowerFlow(String cgmesTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Export only if flow is a number
        if (Double.isNaN(p) && Double.isNaN(q)) {
            return;
        }
        try {
            CgmesExportUtil.writeStartId("SvPowerFlow", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "SvPowerFlow.p");
            writer.writeCharacters(CgmesExportUtil.format(p));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvPowerFlow.q");
            writer.writeCharacters(CgmesExportUtil.format(q));
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvPowerFlow.Terminal", cgmesTerminalId, cimNamespace, writer, context);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSvInjection(Load svInjection, String topologicalNode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeStartId("SvInjection", context.getNamingStrategy().getCgmesId(svInjection), false, cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "SvInjection.pInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getP0()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvInjection.qInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getQ0()));
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvInjection.TopologicalNode", topologicalNode, cimNamespace, writer, context);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeShuntCompensatorSections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            CgmesExportUtil.writeStartId("SvShuntCompensatorSections", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer, context);
            CgmesExportUtil.writeReference("SvShuntCompensatorSections.ShuntCompensator", context.getNamingStrategy().getCgmesId(s), cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "SvShuntCompensatorSections.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeTapSteps(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            // For two-windings transformers tap changer may be at end number 1 or 2
            // If we have exported the EQ the tap changer may have been moved from end 2 to end 1, where IIDM has modelled it.
            // If we are exporting only the SV the tap changer alias to use is the one of the original location
            if (twt.hasPhaseTapChanger()) {
                int endNumber = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1).isPresent() ? 1 : 2;
                String ptcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber);
                writeSvTapStep(ptcId, twt.getPhaseTapChanger().getTapPosition(), cimNamespace, writer, context);
                writeSvTapStepHidden(twt, ptcId, cimNamespace, writer, context);
            } else if (twt.hasRatioTapChanger()) {
                int endNumber = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1).isPresent() ? 1 : 2;
                String rtcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber);
                writeSvTapStep(rtcId, twt.getRatioTapChanger().getTapPosition(), cimNamespace, writer, context);
                writeSvTapStepHidden(twt, rtcId, cimNamespace, writer, context);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int endNumber = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + endNumber);
                    writeSvTapStep(ptcId, leg.getPhaseTapChanger().getTapPosition(), cimNamespace, writer, context);
                    writeSvTapStepHidden(twt, ptcId, cimNamespace, writer, context);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = context.getNamingStrategy().getCgmesIdFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + endNumber);
                    writeSvTapStep(rtcId, leg.getRatioTapChanger().getTapPosition(), cimNamespace, writer, context);
                    writeSvTapStepHidden(twt, rtcId, cimNamespace, writer, context);
                }
                endNumber++;
            }
        }
    }

    private static <C extends Connectable<C>> void writeSvTapStepHidden(Connectable<C> eq, String tcId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        // If we are exporting equipment definitions the hidden tap changer will not be exported
        // because it has been included in the model for the only tap changer left in IIDM
        // If we are exporting only SSH, SV, ... we have to write the step we have saved for it
        if (cgmesTcs != null && !context.isExportEquipment()) {
            for (CgmesTapChanger cgmesTc : cgmesTcs.getTapChangers()) {
                if (cgmesTc.isHidden() && cgmesTc.getCombinedTapChangerId().equals(tcId)) {
                    int step = cgmesTc.getStep().orElseThrow(() -> new PowsyblException("Non null step expected for tap changer " + cgmesTc.getId()));
                    writeSvTapStep(cgmesTc.getId(), step, cimNamespace, writer, context);
                }
            }
        }
    }

    private static void writeSvTapStep(String tapChangerId, int tapPosition, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartId("SvTapStep", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, "SvTapStep.position");
        writer.writeCharacters(CgmesExportUtil.format(tapPosition));
        writer.writeEndElement();
        CgmesExportUtil.writeReference("SvTapStep.TapChanger", tapChangerId, cimNamespace, writer, context);
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
        if (connectable instanceof TieLine && connectable.hasProperty()
                && connectable.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_1")
                && connectable.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary_2")) { // TODO check aliases when merging of aliases is handled
            return; // FIXME ignore tie lines from CGMES for now. Will export as two AC line segments later
        }
        writeStatus(Boolean.toString(connectable.getTerminals().stream().anyMatch(Terminal::isConnected)), context.getNamingStrategy().getCgmesId(connectable), cimNamespace, writer, context);
    }

    private static void writeStatus(String inService, String conductingEquipmentId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeStartId("SvStatus", CgmesExportUtil.getUniqueId(), false, cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "SvStatus.inService");
            writer.writeCharacters(inService);
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvStatus.ConductingEquipment", conductingEquipmentId, cimNamespace, writer, context);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeConverters(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcConverterStation<?> converterStation : network.getHvdcConverterStations()) {
            CgmesExportUtil.writeStartAbout(CgmesExportUtil.converterClassName(converterStation), context.getNamingStrategy().getCgmesId(converterStation), cimNamespace, writer, context);
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
