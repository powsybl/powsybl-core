/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class TopologyExport {

    private static final String TOPOLOGICAL_NODE_CONNECTIVITY_NODE_CONTAINER = "TopologicalNode.ConnectivityNodeContainer";
    private static final String TOPOLOGICAL_NODE_BASE_VOLTAGE = "TopologicalNode.BaseVoltage";

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String cimNamespace = context.getCim().getNamespace();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getTpModelDescription(), context);
            }

            Set<String> addedTopologicalNodes = new HashSet<>();
            writeTopologicalNodes(network, addedTopologicalNodes, cimNamespace, writer, context);
            writeTerminals(network, addedTopologicalNodes, cimNamespace, writer, context);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTerminals(Network network, Set<String> addedTopologicalNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeConnectableTerminals(network, cimNamespace, writer);
        writeSwitchesTerminals(network, addedTopologicalNodes, cimNamespace, writer, context);
        writeHvdcTerminals(network, cimNamespace, writer);
    }

    private static void writeConnectableTerminals(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) {
            for (Terminal t : c.getTerminals()) {
                String terminalId;
                if (c instanceof DanglingLine) {
                    writeBoundaryTerminal((DanglingLine) c, cimNamespace, writer);
                    terminalId = cgmesTerminalFromAlias(c, "Terminal_Network");
                } else {
                    int side = CgmesExportUtil.getTerminalSide(t, c);
                    terminalId = cgmesTerminalFromAlias(c, CgmesNames.TERMINAL + side);
                }
                Bus b = t.getBusBreakerView().getBus();
                if (b == null) {
                    b = t.getBusBreakerView().getConnectableBus();
                }
                writeTerminal(terminalId, b.getId(), cimNamespace, writer);
            }
        }
    }

    private static String cgmesTerminalFromAlias(Identifiable<?> i, String aliasType0) {
        String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + aliasType0;
        Optional<String> cgmesTerminalId = i.getAliasFromType(aliasType);
        if (cgmesTerminalId.isEmpty()) {
            throw new PowsyblException("Missing CGMES terminal in aliases of " + i.getId() + ", aliasType " + aliasType);
        }
        return cgmesTerminalId.get();
    }

    private static void writeSwitchesTerminals(Network network, Set<String> addedTopologicalNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            VoltageLevel vl = sw.getVoltageLevel();

            int node1 = 0;
            int node2 = 0;
            Bus bus1;
            Bus bus2;
            if (vl.getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
                bus1 = vl.getBusBreakerView().getBus1(sw.getId());
                bus2 = vl.getBusBreakerView().getBus2(sw.getId());
            } else {
                node1 = getLowerNodeForBusBreakerViewBus(vl, vl.getNodeBreakerView().getNode1(sw.getId()));
                node2 = getLowerNodeForBusBreakerViewBus(vl, vl.getNodeBreakerView().getNode2(sw.getId()));
                //node1 = getFirstNodeForBusBreakerViewBus(vl, addedTopologicalNodes, vl.getNodeBreakerView().getNode1(sw.getId()));
                //node2 = getFirstNodeForBusBreakerViewBus(vl, addedTopologicalNodes, vl.getNodeBreakerView().getNode2(sw.getId()));
                bus1 = getBusForBusBreakerViewBus(vl, node1);
                bus2 = getBusForBusBreakerViewBus(vl, node2);
            }
            String tn1 = writeTopologicalNode(bus1, sw.getVoltageLevel(), node1, addedTopologicalNodes, cimNamespace, writer, context);
            String tn2 = writeTopologicalNode(bus2, sw.getVoltageLevel(), node2, addedTopologicalNodes, cimNamespace, writer, context);

            String cgmesTerminal1 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL1);
            String cgmesTerminal2 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL2);

            writeSwitchTerminal(tn1, cgmesTerminal1, cimNamespace, writer, context);
            writeSwitchTerminal(tn2, cgmesTerminal2, cimNamespace, writer, context);
        }
    }

    private static String writeTopologicalNode(Bus bus, VoltageLevel voltageLevel, int node, Set<String> addedTopologicalNodes, String cimNamespace,
                                               XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String tn = bus != null ? bus.getId() : null;
        if (tn == null) {
            tn = voltageLevel.getId() + "_" + node;
        }
        String tnName = bus != null ? bus.getNameOrId() : tn;
        if (!addedTopologicalNodes.contains(tn)) {
            addedTopologicalNodes.add(tn);
            writeTopologicalNode(tn, tnName, voltageLevel.getId(),
                    context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()).getId(), cimNamespace, writer);
        }

        return tn;
    }

    private static void writeSwitchTerminal(String tn, String cgmesTerminal, String cimNamespace,
                XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeTerminal(cgmesTerminal, tn, cimNamespace, writer);
    }

    private static Bus getBusForBusBreakerViewBus(VoltageLevel vl, int node) {
        Terminal terminal = vl.getNodeBreakerView().getTerminal(node);
        if (terminal == null) {
            return null;
        }

        return terminal.getBusBreakerView().getBus();
    }

    private static int getLowerNodeForBusBreakerViewBus(VoltageLevel voltageLevel, int node) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new IllegalArgumentException("The voltage level " + voltageLevel.getId() + " is not described in Node/Breaker topology");
        }

        Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(node);
        if (terminal != null) {
            return node;
        }
        Set nodeSet = new HashSet();
        nodeSet.add(node);

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw != null && (sw.isOpen() || sw.isRetained())) {
                return TraverseResult.TERMINATE_PATH;
            }
            if (voltageLevel.getNodeBreakerView().getTerminal(node2) != null) {
                nodeSet.clear();
                nodeSet.add(node2);
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            nodeSet.add(node2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(node, traverser);

        return (int) nodeSet.stream().sorted().findFirst().get();
    }

    private static int getFirstNodeForBusBreakerViewBus(VoltageLevel voltageLevel, Set<String> addedTopologicalNodes, int node) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new IllegalArgumentException("The voltage level " + voltageLevel.getId() + " is not described in Node/Breaker topology");
        }

        Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(node);
        if (terminal != null) {
            return node;
        }
        if (addedTopologicalNodes.contains(voltageLevel.getId() + "_" + node)) {
            return node;
        }

        Set nodeSet = new HashSet();
        nodeSet.add(node);

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw != null && (sw.isOpen() || sw.isRetained())) {
                return TraverseResult.TERMINATE_PATH;
            }
            if (voltageLevel.getNodeBreakerView().getTerminal(node2) != null) {
                nodeSet.clear();
                nodeSet.add(node2);
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            if (addedTopologicalNodes.contains(voltageLevel.getId() + "_" + node2)) {
                nodeSet.clear();
                nodeSet.add(node2);
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            nodeSet.add(node2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(node, traverser);

        return (int) nodeSet.stream().findFirst().get();
    }

    private static void writeHvdcTerminals(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            writeHvdcBusTerminals(line, line.getConverterStation1(), 1, cimNamespace, writer);
            writeHvdcBusTerminals(line, line.getConverterStation2(), 2, cimNamespace, writer);
        }
    }

    private static void writeHvdcBusTerminals(HvdcLine line, HvdcConverterStation<?> converter, int side, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Bus bus = converter.getTerminal().getBusBreakerView().getBus();
        String dcTopologicalNode = bus.getId() + "DC";
        String dcNode = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + side).orElseThrow(PowsyblException::new);
        writeDCNode(dcNode, dcTopologicalNode, cimNamespace, writer);
        String dcTerminal = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal" + side).orElseThrow(PowsyblException::new);
        writeDCTerminal(dcTerminal, dcTopologicalNode, cimNamespace, writer);
        String acdcConverterDcTerminal = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal").orElseThrow(PowsyblException::new);
        writeAcdcConverterDCTerminal(acdcConverterDcTerminal, dcTopologicalNode, cimNamespace, writer);
    }

    private static void writeDCNode(String dcNode, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("DCNode", dcNode, cimNamespace, writer);
        CgmesExportUtil.writeReference("DCNode.DCTopologicalNode", dcTopologicalNode, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writeAcdcConverterDCTerminal(String acdcConverterDcTerminal, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("ACDCConverterDCTerminal", acdcConverterDcTerminal, cimNamespace, writer);
        CgmesExportUtil.writeReference("DCBaseTerminal.DCTopologicalNode", dcTopologicalNode, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writeDCTerminal(String dcTerminal, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("DCTerminal", dcTerminal, cimNamespace, writer);
        CgmesExportUtil.writeReference("DCBaseTerminal.DCTopologicalNode", dcTopologicalNode, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writeBoundaryTerminal(DanglingLine dl, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        String boundaryId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary").orElseThrow(PowsyblException::new);
        String equivalentInjectionTerminalId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal").orElseThrow(PowsyblException::new);
        Optional<String> topologicalNode = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
        if (topologicalNode.isPresent()) {
            if (boundaryId != null) {
                writeTerminal(boundaryId, topologicalNode.get(), cimNamespace, writer);
            }
            if (equivalentInjectionTerminalId != null) {
                writeTerminal(equivalentInjectionTerminalId, topologicalNode.get(), cimNamespace, writer);
            }
        }
    }

    private static void writeTerminal(String terminalId, String topologicalNode, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("Terminal", terminalId, cimNamespace, writer);
        CgmesExportUtil.writeReference("Terminal.TopologicalNode", topologicalNode, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writeTopologicalNodes(Network network, Set<String> addedTopologicalNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeBusTopologicalNodes(network, addedTopologicalNodes, cimNamespace, writer, context);
        writeDanglingLineTopologicalNodes(network, cimNamespace, writer, context);
        writeHvdcTopologicalNodes(network, cimNamespace, writer);
    }

    private static void writeBusTopologicalNodes(Network network, Set<String> addedTopologicalNodes, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            writeTopologicalNode(b, b.getVoltageLevel(), 0, addedTopologicalNodes, cimNamespace, writer, context);
        }
    }

    private static void writeDanglingLineTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Optional<String> topologicalNodeId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNodeId.isPresent()) {
                String baseVoltage = context.getBaseVoltageByNominalVoltage(dl.getBoundary().getVoltageLevel().getNominalV()).getId();
                writeTopologicalNode(topologicalNodeId.get(), dl.getNameOrId(), dl.getBoundary().getVoltageLevel().getId(), baseVoltage, cimNamespace, writer);
            }
        }
    }

    private static void writeHvdcTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Set<String> written = new HashSet<>();
        for (HvdcLine line : network.getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusBreakerView().getBus();
            if (!written.contains(b1.getId())) {
                writeDCTopologicalNode(b1.getId() + "DC", line.getNameOrId() + 1, cimNamespace, writer);
                written.add(b1.getId());
            }

            Bus b2 = line.getConverterStation2().getTerminal().getBusBreakerView().getBus();
            if (!written.contains(b2.getId())) {
                writeDCTopologicalNode(b2.getId() + "DC", line.getNameOrId() + 2, cimNamespace, writer);
                written.add(b2.getId());
            }
        }
    }

    private static void writeDCTopologicalNode(String dcTopologicalNode, String dcTopologicalNodeName, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("DCTopologicalNode", dcTopologicalNode, dcTopologicalNodeName, cimNamespace, writer);
        writer.writeEndElement();
    }

    private static void writeTopologicalNode(String topologicalNode, String topologicalNodeName, String connectivityNodeContainerId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("TopologicalNode", topologicalNode, topologicalNodeName, cimNamespace, writer);
        CgmesExportUtil.writeReference(TOPOLOGICAL_NODE_CONNECTIVITY_NODE_CONTAINER, connectivityNodeContainerId, cimNamespace, writer);
        CgmesExportUtil.writeReference(TOPOLOGICAL_NODE_BASE_VOLTAGE, baseVoltageId, cimNamespace, writer);
        writer.writeEndElement();
    }

    private TopologyExport() {
    }
}
