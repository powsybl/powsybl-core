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
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
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
            writeSvInjectionsForSlacks(network, cimNamespace, writer, context);
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
        List<TopologicalIsland> islands = buildIslands(network, context);
        String cimNamespace = context.getCim().getNamespace();
        for (TopologicalIsland island : islands) {
            if (!angleRefs.containsKey(island.key)) {
                Supplier<String> log = () -> String.format("Synchronous component  %s does not have a defined angle reference bus: it is ignored", island.key);
                LOG.info(log.get());
                continue;
            }
            String islandId = CgmesExportUtil.getUniqueId();
            CgmesExportUtil.writeStartIdName(CgmesNames.TOPOLOGICAL_ISLAND, islandId, islandId, cimNamespace, writer, context);
            CgmesExportUtil.writeReference("TopologicalIsland.AngleRefTopologicalNode", angleRefs.get(island.key), cimNamespace, writer, context);
            if (context.isExportLoadFlowStatus()) {
                writer.writeStartElement(cimNamespace, CgmesNames.IDENTIFIED_OBJECT_DESCRIPTION);
                writer.writeCharacters(island.loadFlowStatus);
                writer.writeEndElement();
            }
            for (String tn : island.topologicalNodes) {
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

    private static final class BusTools {
        private BusTools() {
            // Empty
        }

        static Optional<Bus> getBusViewBus(Bus bus) {
            if (bus.getVoltageLevel().getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
                return Optional.of(bus.getVoltageLevel().getBusView().getMergedBus(bus.getId()));
            } else {
                if (bus.getConnectedTerminalCount() > 0) {
                    return bus.getConnectedTerminalStream().map(t -> t.getBusView().getBus()).filter(Objects::nonNull).findFirst();
                } else {
                    return bus.getVoltageLevel().getBusView().getBusStream()
                            .filter(busViewBus -> bus.getVoltageLevel().getBusBreakerView().getBusesFromBusViewBusId(busViewBus.getId()).contains(bus))
                            .findFirst();
                }
            }
        }

        static boolean hasAnyFinite(Bus bus, Function<Terminal, Double> value) {
            return bus.getConnectedTerminalStream().map(value).anyMatch(Double::isFinite);
        }

        static double sum(Bus bus, Function<Terminal, Double> value) {
            return bus.getConnectedTerminalStream()
                    .map(value)
                    .filter(pq -> !Double.isNaN(pq))
                    .mapToDouble(Double::valueOf)
                    .sum();
        }

        static boolean isSlack(Bus bus) {
            SlackTerminal st = bus.getVoltageLevel().getExtension(SlackTerminal.class);
            return st != null && !st.isEmpty() && st.getTerminal().getBusView().getBus() == bus;
        }

        static void logDetail(Bus bus) {
            if (LOG.isDebugEnabled()) {
                bus.getConnectedTerminalStream().forEach(t -> LOG.debug(String.format("  %7.2f  %7.2f  %s %s %s",
                        t.getP(), t.getQ(),
                        t.getConnectable().getType(), t.getConnectable().getNameOrId(), t.getConnectable().getId())));
            }
        }
    }

    private static final class TopologicalIsland {
        static final String CONVERGED = "converged";
        static final String DIVERGED = "diverged";

        // The key can be a synchronous component number or a topological node identifier
        final String key;
        final List<String> topologicalNodes;
        // We need to export the load flow status for the island,
        // from QoCDC 3.3.1 rule TIConvergenceStatMissing:
        //      The cim:IdentifiedObject.description of cim:TopologicalIsland shall have one the
        //      following string values: “converged” and “diverged” which represents the
        //      convergence status of the cim:TopologicalIsland.
        // Consider the island is converged unless we add a non-converged bus
        String loadFlowStatus = CONVERGED;
        final double maxPMismatchConverged;
        final double maxQMismatchConverged;
        final Map<Bus, Boolean> checkedBusViewBuses = new HashMap<>();
        final boolean checkConvergedInAllBuses;

        private TopologicalIsland(String key, List<String> topologicalNodes, CgmesExportContext context) {
            this.key = key;
            this.checkConvergedInAllBuses = false;
            this.topologicalNodes = topologicalNodes;
            this.maxPMismatchConverged = context.getMaxPMismatchConverged();
            this.maxQMismatchConverged = context.getMaxQMismatchConverged();
        }

        static TopologicalIsland fromSynchronousComponent(String key, CgmesExportContext context) {
            return new TopologicalIsland(key, new ArrayList<>(), context);
        }

        static TopologicalIsland fromTopologicalNode(String topologicalNode, CgmesExportContext context) {
            return new TopologicalIsland(topologicalNode, Collections.singletonList(topologicalNode), context);
        }

        void addNode(String topologicalNode, Bus bus, boolean updateLoadFlowStatus) {
            topologicalNodes.add(topologicalNode);
            if (updateLoadFlowStatus) {
                updateLoadFlowStatus(bus);
            }
        }

        void updateLoadFlowStatus(Bus bus) {
            if (loadFlowStatus.equals(DIVERGED) && !checkConvergedInAllBuses) {
                return;
            }
            if (!(isValidVoltage(bus.getV()) && isValidAngle(bus.getAngle()) && isInAccordanceWithKirchhoffsFirstLaw(bus))) {
                loadFlowStatus = DIVERGED;
            }
        }

        boolean isValidVoltage(double v) {
            return v >= 0.01;
        }

        boolean isValidAngle(double a) {
            return Double.isFinite(a);
        }

        boolean isInAccordanceWithKirchhoffsFirstLaw(Bus bus) {
            // Instead of checking switch flows, we check that the corresponding bus view bus is balanced
            Optional<Bus> optionalBusViewBus = BusTools.getBusViewBus(bus);
            if (optionalBusViewBus.isEmpty()) {
                LOG.error("Can not check if bus is in accordance with Kirchhoff's first law. No BusView bus can be found for: {}", bus);
                return false;
            }
            Bus busViewBus = optionalBusViewBus.get();
            if (busViewBus.getConnectedTerminalCount() == 0
                    || BusTools.isSlack(busViewBus)) {
                return true;
            }
            // We do not check the same bus view bus more than once
            if (checkedBusViewBuses.containsKey(busViewBus)) {
                return checkedBusViewBuses.get(busViewBus);
            }

            boolean isInAccordance;
            if (BusTools.hasAnyFinite(busViewBus, Terminal::getP) && BusTools.hasAnyFinite(busViewBus, Terminal::getQ)) {
                double sumP = BusTools.sum(busViewBus, Terminal::getP);
                double sumQ = BusTools.sum(busViewBus, Terminal::getQ);
                isInAccordance = Math.abs(sumP) <= maxPMismatchConverged && Math.abs(sumQ) <= maxQMismatchConverged;
                if (!isInAccordance && LOG.isInfoEnabled()) {
                    LOG.info("Bus {} is not in accordance with Kirchhoff's first law. Mismatch = {}", bus, String.format("(%.4f, %.4f)", sumP, sumQ));
                    BusTools.logDetail(busViewBus);
                    LOG.debug(String.format("  %7.2f  %7.2f  Sum", sumP, sumQ));
                }
            } else {
                isInAccordance = false;
                LOG.info("Bus {} is not in accordance with Kirchhoff's first law. All connected terminals have invalid values", bus);
                BusTools.logDetail(busViewBus);
            }
            checkedBusViewBuses.put(busViewBus, isInAccordance);
            return isInAccordance;
        }
    }

    private static List<TopologicalIsland> buildIslands(Network network, CgmesExportContext context) {
        Map<String, TopologicalIsland> islands = new HashMap<>();
        for (Bus b : network.getBusBreakerView().getBuses()) {
            String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
            if (b.getSynchronousComponent() != null) {
                String key = String.valueOf(b.getSynchronousComponent().getNum());
                TopologicalIsland island = islands.computeIfAbsent(key, k -> TopologicalIsland.fromSynchronousComponent(k, context));
                island.addNode(topologicalNodeId, b, context.isExportLoadFlowStatus());
            } else {
                islands.put(topologicalNodeId, TopologicalIsland.fromTopologicalNode(topologicalNodeId, context));
            }
        }
        return islands.values().stream().toList();
    }

    private static void writeVoltagesForTopologicalNodes(Network network, CgmesExportContext context, XMLStreamWriter writer) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        for (Map.Entry<String, Bus> e : context.getTopologicalNodes(network).entrySet()) {
            writeVoltage(e.getKey(), e.getValue() != null ? e.getValue().getV() : 0.0, e.getValue() != null ? e.getValue().getAngle() : 0.0, cimNamespace, writer, context);
        }
    }

    private static void writeVoltagesForBoundaryNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : CgmesExportUtil.getBoundaryDanglingLines(network)) {
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
        for (TieLine l : network.getTieLines()) {
            String topologicalNode = l.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY)
                    .orElseGet(() -> l.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE + "_1"));
            if (topologicalNode != null) {
                writeVoltage(topologicalNode, l.getDanglingLine1().getBoundary().getV(), l.getDanglingLine1().getBoundary().getAngle(), cimNamespace, writer, context);
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

    private static void writeSvInjectionsForSlacks(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        if (context.isExportSvInjectionsForSlacks()) {
            for (VoltageLevel vl : network.getVoltageLevels()) {
                SlackTerminal st = vl.getExtension(SlackTerminal.class);
                if (st != null && !st.isEmpty()) {
                    Bus bus = st.getTerminal().getBusBreakerView().getBus();
                    Optional<Bus> optionalBusViewBus = BusTools.getBusViewBus(bus);
                    if (optionalBusViewBus.isPresent()) {
                        // The total mismatch of the slack (as busview bus) has been left in the bus/breaker bus labeled as slack.
                        // This is ensured by the calculation of flows through switches (SwitchesFlow).
                        // We compute the total mismatch left by power flow calculation in the busview bus and
                        // create an SvInjection that is assigned to the bus/breaker view bus.
                        Bus busViewBus = optionalBusViewBus.get();
                        double sumP = BusTools.sum(busViewBus, Terminal::getP);
                        double sumQ = BusTools.sum(busViewBus, Terminal::getQ);
                        if (Math.abs(sumP) > context.getMaxPMismatchConverged() || Math.abs(sumQ) > context.getMaxQMismatchConverged()) {
                            String topologicalNodeId = context.getNamingStrategy().getCgmesId(bus);
                            String svInjectionId = CgmesExportUtil.getUniqueId();
                            writeSvInjection(svInjectionId, -sumP, -sumQ, topologicalNodeId, cimNamespace, writer, context);
                        }
                    }
                }
            }
        }
    }

    private static void writePowerFlows(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getLoadStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getGeneratorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getBatteryStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getShuntCompensatorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, context, Network::getStaticVarCompensatorStream);

        // Fictitious loads are not exported as Equipment, they are just added to SV as SvInjection
        for (Load load : network.getLoads()) {
            if (load.isFictitious()) {
                writeSvInjection(load, cimNamespace, writer, context);
            }
        }

        Map<String, Double> equivalentInjectionTerminalP = new HashMap<>();
        Map<String, Double> equivalentInjectionTerminalQ = new HashMap<>();
        CgmesExportUtil.getBoundaryDanglingLines(network).stream().forEach(dl -> {
            // FIXME: the values (p0/q0) are wrong: these values are target and never updated, not calculated flows
            // DanglingLine's attributes will be created to store calculated flows on the boundary side
            if (context.exportBoundaryPowerFlows()) {
                writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary", dl.getBoundary().getP(), dl.getBoundary().getQ(), cimNamespace, writer, context);
            }
            writePowerFlowTerminalFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, dl.getTerminal().getP(), dl.getTerminal().getQ(), cimNamespace, writer, context);
            equivalentInjectionTerminalP.compute(context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -dl.getBoundary().getP() : v - dl.getBoundary().getP());
            equivalentInjectionTerminalQ.compute(context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -dl.getBoundary().getQ() : v - dl.getBoundary().getQ());
        });

        network.getTwoWindingsTransformerStream().forEach(b -> writeConnectableBranchPowerFlow(cimNamespace, writer, context, b));
        network.getLineStream().forEach(b -> writeConnectableBranchPowerFlow(cimNamespace, writer, context, b));

        network.getTieLineStream().forEach(b -> {
            writePowerFlowTieLineTerminalFromAlias(b.getDanglingLine1(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, b.getDanglingLine1().getTerminal(), cimNamespace, writer, context);
            writePowerFlowTieLineTerminalFromAlias(b.getDanglingLine2(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, b.getDanglingLine2().getTerminal(), cimNamespace, writer, context);
            if (context.exportBoundaryPowerFlows()) {
                writePowerFlowTerminalFromAlias(b.getDanglingLine1(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary", b.getDanglingLine1().getBoundary().getP(), b.getDanglingLine1().getBoundary().getQ(), cimNamespace, writer, context);
                writePowerFlowTerminalFromAlias(b.getDanglingLine2(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_Boundary", b.getDanglingLine2().getBoundary().getP(), b.getDanglingLine2().getBoundary().getQ(), cimNamespace, writer, context);
            }
            // Compute also equivalent injection values for boundary lines that have been left inside the CGM,
            // hey have been organised in tie lines
            String eit1 = b.getDanglingLine1().getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            if (eit1 == null) {
                LOG.warn("Missing equivalent injection terminal for dangling line {}. For proper export models must be combined in a Network with subnetworks instead of assembled", b.getDanglingLine1().getId());
            } else {
                equivalentInjectionTerminalP.compute(context.getNamingStrategy().getCgmesIdFromProperty(b.getDanglingLine1(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -b.getDanglingLine1().getBoundary().getP() : v - b.getDanglingLine1().getBoundary().getP());
                equivalentInjectionTerminalQ.compute(context.getNamingStrategy().getCgmesIdFromProperty(b.getDanglingLine1(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -b.getDanglingLine1().getBoundary().getQ() : v - b.getDanglingLine1().getBoundary().getQ());
            }
            String eit2 = b.getDanglingLine2().getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            if (eit2 == null) {
                LOG.warn("Missing equivalent injection terminal for dangling line {}. For proper export models must be combined in a Network with subnetworks instead of assembled", b.getDanglingLine2().getId());
            } else {
                equivalentInjectionTerminalP.compute(context.getNamingStrategy().getCgmesIdFromProperty(b.getDanglingLine2(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -b.getDanglingLine2().getBoundary().getP() : v - b.getDanglingLine2().getBoundary().getP());
                equivalentInjectionTerminalQ.compute(context.getNamingStrategy().getCgmesIdFromProperty(b.getDanglingLine2(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal"), (k, v) -> v == null ? -b.getDanglingLine2().getBoundary().getQ() : v - b.getDanglingLine2().getBoundary().getQ());
            }
        });
        equivalentInjectionTerminalP.keySet().forEach(eiId -> writePowerFlow(eiId, equivalentInjectionTerminalP.get(eiId), equivalentInjectionTerminalQ.get(eiId), cimNamespace, writer, context));

        network.getThreeWindingsTransformerStream().forEach(twt -> {
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, twt.getLeg1().getTerminal(), cimNamespace, writer, context);
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, twt.getLeg2().getTerminal(), cimNamespace, writer, context);
            writePowerFlowTerminalFromAlias(twt, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL3, twt.getLeg3().getTerminal(), cimNamespace, writer, context);
        });

        if (context.exportFlowsForSwitches()) {
            network.getVoltageLevelStream().forEach(vl -> {
                SlackTerminal st = vl.getExtension(SlackTerminal.class);
                Terminal slackTerminal = st != null && !st.isEmpty() ? st.getTerminal() : null;
                SwitchesFlow swflows = new SwitchesFlow(vl, slackTerminal);
                vl.getSwitches().forEach(sw -> {
                    if (context.isExportedEquipment(sw)) {
                        writePowerFlowTerminalFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, swflows.getP1(sw.getId()), swflows.getQ1(sw.getId()), cimNamespace, writer, context);
                        writePowerFlowTerminalFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, swflows.getP2(sw.getId()), swflows.getQ2(sw.getId()), cimNamespace, writer, context);
                    }
                });
            });
        }
    }

    private static void writeConnectableBranchPowerFlow(String cimNamespace, XMLStreamWriter writer, CgmesExportContext context, Branch b) {
        writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1, b.getTerminal1(), cimNamespace, writer, context);
        writePowerFlowTerminalFromAlias(b, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2, b.getTerminal2(), cimNamespace, writer, context);
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
            writePowerFlow(cgmesTerminal, terminal.getP(), terminal.getQ(), cimNamespace, writer, context);
        } else {
            LOG.error("No defined CGMES terminal for {}", terminal.getConnectable().getId());
        }
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

    private static void writePowerFlowTerminalFromAlias(Identifiable<?> c, String aliasTypeForTerminalId, Terminal t, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Export only if we have a terminal identifier
        writePowerFlowTerminalFromAlias(c, aliasTypeForTerminalId, t.getP(), t.getQ(), cimNamespace, writer, context);
    }

    private static void writePowerFlowTieLineTerminalFromAlias(DanglingLine danglingLine, String aliasTypeForTerminalId, Terminal t, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        writePowerFlowTerminalFromAlias(danglingLine, aliasTypeForTerminalId, t.getP(), t.getQ(), cimNamespace, writer, context);
    }

    private static void writePowerFlowTerminalFromAlias(Identifiable<?> c, String aliasTypeForTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        // Export only if we have a terminal identifier
        if (c.getAliasFromType(aliasTypeForTerminalId).isPresent()) {
            String cgmesTerminalId = context.getNamingStrategy().getCgmesIdFromAlias(c, aliasTypeForTerminalId);
            writePowerFlow(cgmesTerminalId, p, q, cimNamespace, writer, context);
        } else {
            LOG.error("Exporting CGMES SvPowerFlow. Missing alias for {} {}: {}", c.getType(), c.getId(), aliasTypeForTerminalId);
        }
    }

    private static void writePowerFlow(String cgmesTerminalId, double p, double q, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
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
        String svInjectionId = context.getNamingStrategy().getCgmesId(svInjection);
        writeSvInjection(svInjectionId, svInjection.getP0(), svInjection.getQ0(), topologicalNode, cimNamespace, writer, context);
    }

    private static void writeSvInjection(String svInjectionId, double p, double q, String topologicalNode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeStartId("SvInjection", svInjectionId, false, cimNamespace, writer, context);
            writer.writeStartElement(cimNamespace, "SvInjection.pInjection");
            writer.writeCharacters(CgmesExportUtil.format(p));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvInjection.qInjection");
            writer.writeCharacters(CgmesExportUtil.format(q));
            writer.writeEndElement();
            CgmesExportUtil.writeReference("SvInjection.TopologicalNode", topologicalNode, cimNamespace, writer, context);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeShuntCompensatorSections(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            if ("true".equals(s.getProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT))) {
                continue;
            }
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
            }
            if (twt.hasRatioTapChanger()) {
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
                }
                if (leg.hasRatioTapChanger()) {
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
                writeConnectableStatus(c, cimNamespace, writer, context, network);
            }
        });

        // RK: For dangling lines (boundaries), the AC Line Segment is considered in service if and only if it is connected on the network side.
        // If it is disconnected on the boundary side, it might not appear on the SV file.
    }

    private static void writeConnectableStatus(Connectable<?> connectable, String cimNamespace, XMLStreamWriter writer,
                                               CgmesExportContext context, Network network) {
        if (connectable instanceof DanglingLine dl && !network.isBoundaryElement(dl)) {
            // TODO(Luma) Export tie line components instead of a single equipment
            // If this dangling line is part of a tie line we will be exporting the tie line as a single equipment
            // We ignore dangling lines inside tie lines for now
            return;
        }
        if (CgmesExportUtil.isEquivalentShuntWithZeroSectionCount(connectable)) {
            // Equivalent shunts do not have a section count in SSH, SV profiles,
            // To make output consistent with IIDM section count == 0 we declare it out of service
            writeStatus(Boolean.toString(false), context.getNamingStrategy().getCgmesId(connectable), cimNamespace, writer, context);
            return;
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
