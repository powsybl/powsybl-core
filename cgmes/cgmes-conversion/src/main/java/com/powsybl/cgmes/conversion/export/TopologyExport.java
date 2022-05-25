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
import java.util.Objects;
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
        writeConnectableTerminals(network, cimNamespace, writer, context);
        writeBoundaryTerminals(network, cimNamespace, writer);
        writeSwitchesTerminals(network, addedTopologicalNodes, cimNamespace, writer, context);
        writeHvdcTerminals(network, cimNamespace, writer, context);
    }

    private static void writeConnectableTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) {
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    Bus b = t.getBusBreakerView().getBus();
                    if (b == null) {
                        b = t.getBusBreakerView().getConnectableBus();
                    }
                    String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
                    writeTerminal(CgmesExportUtil.getTerminalId(t), topologicalNodeId, cimNamespace, writer);
                }
            }
        }
    }

    private static void writeBoundaryTerminals(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            writeBoundaryTerminal(dl, cimNamespace, writer);
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

            String tn1;
            String tname1;
            String tn2;
            String tname2;
            if (vl.getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
                tn1 = context.getNamingStrategy().getCgmesId(vl.getBusBreakerView().getBus1(sw.getId()));
                tname1 = vl.getBusBreakerView().getBus1(sw.getId()).getNameOrId();
                tn2 = context.getNamingStrategy().getCgmesId(vl.getBusBreakerView().getBus2(sw.getId()));
                tname2 = vl.getBusBreakerView().getBus2(sw.getId()).getNameOrId();
            } else {
                int node1 = vl.getNodeBreakerView().getNode1(sw.getId());
                Bus bus1 = getBusForBusBreakerViewBus(vl, node1);
                tn1 = bus1 == null ? findOrCreateTopologicalNode(vl, node1) : context.getNamingStrategy().getCgmesId(bus1);
                tname1 = bus1 == null ? tn1 : bus1.getNameOrId();

                int node2 = vl.getNodeBreakerView().getNode2(sw.getId());
                Bus bus2 = getBusForBusBreakerViewBus(vl, node2);
                tn2 = bus2 == null ? findOrCreateTopologicalNode(vl, node2) : context.getNamingStrategy().getCgmesId(bus2);
                tname2 = bus2 == null ? tn2 : bus2.getNameOrId();
            }
            writeTopologicalNode(tn1, tname1, vl, addedTopologicalNodes, cimNamespace, writer, context);
            writeTopologicalNode(tn2, tname2, vl, addedTopologicalNodes, cimNamespace, writer, context);

            String cgmesTerminal1 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL1);
            String cgmesTerminal2 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL2);

            writeSwitchTerminal(tn1, cgmesTerminal1, cimNamespace, writer);
            writeSwitchTerminal(tn2, cgmesTerminal2, cimNamespace, writer);
        }
    }

    private static String findOrCreateTopologicalNode(VoltageLevel vl, int node) {
        if (vl.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new IllegalArgumentException("The voltage level " + vl.getId() + " is not described in Node/Breaker topology");
        }
        Set<Integer> nodeSet = new HashSet<>();
        nodeSet.add(node);

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw != null && (sw.isOpen() || sw.isRetained())) {
                return TraverseResult.TERMINATE_PATH;
            }
            nodeSet.add(node2);
            return TraverseResult.CONTINUE;
        };

        vl.getNodeBreakerView().traverse(node, traverser);

        Optional<Bus> selectedBus = nodeSet.stream().map(n -> getBusForBusBreakerViewBus(vl, n)).filter(Objects::nonNull).findFirst();
        if (selectedBus.isPresent()) {
            return selectedBus.get().getId();
        }

        Optional<Integer> selectedNode = nodeSet.stream().sorted().findFirst();
        if (selectedNode.isEmpty()) {
            throw new PowsyblException("nodeSet never can be empty");
        }

        return vl.getId() + "_" + selectedNode.get();
    }

    private static void writeTopologicalNode(String tn, String tname, VoltageLevel voltageLevel, Set<String> addedTopologicalNodes,
        String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        if (addedTopologicalNodes.contains(tn)) {
            return;
        }
        addedTopologicalNodes.add(tn);
        writeTopologicalNode(tn, tname, context.getNamingStrategy().getCgmesId(voltageLevel),
            context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()).getId(), cimNamespace, writer);
    }

    private static void writeSwitchTerminal(String tn, String cgmesTerminal, String cimNamespace,
                XMLStreamWriter writer) throws XMLStreamException {
        writeTerminal(cgmesTerminal, tn, cimNamespace, writer);
    }

    private static Bus getBusForBusBreakerViewBus(VoltageLevel vl, int node) {
        Terminal terminal = vl.getNodeBreakerView().getTerminal(node);
        if (terminal == null) {
            return null;
        }

        return terminal.getBusBreakerView().getBus();
    }

    private static void writeHvdcTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            writeHvdcBusTerminals(line, line.getConverterStation1(), 1, cimNamespace, writer, context);
            writeHvdcBusTerminals(line, line.getConverterStation2(), 2, cimNamespace, writer, context);
        }
    }

    private static void writeHvdcBusTerminals(HvdcLine line, HvdcConverterStation<?> converter, int side, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Bus bus = converter.getTerminal().getBusBreakerView().getBus();
        String dcTopologicalNode = context.getNamingStrategy().getCgmesId(bus) + "DC";
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
            String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
            writeTopologicalNode(topologicalNodeId, b.getNameOrId(), b.getVoltageLevel(), addedTopologicalNodes, cimNamespace, writer, context);
        }
    }

    private static void writeDanglingLineTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Optional<String> topologicalNodeId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNodeId.isPresent()) {
                String baseVoltage = context.getBaseVoltageByNominalVoltage(dl.getBoundary().getVoltageLevel().getNominalV()).getId();
                writeTopologicalNode(topologicalNodeId.get(), dl.getNameOrId(), context.getNamingStrategy().getCgmesId(dl.getBoundary().getVoltageLevel()), baseVoltage, cimNamespace, writer);
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
