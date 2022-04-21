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

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

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
            CgmesExportUtil.writeRdfRoot(context.getCimVersion(), writer);
            String cimNamespace = context.getCimNamespace();

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getTpModelDescription(), context);
            }
            writeTopologicalNodes(network, cimNamespace, writer, context);
            writeTerminals(network, cimNamespace, writer, context);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeBusTerminals(network, cimNamespace, writer);
        writeSwitchesTerminals(network, cimNamespace, writer, context);
        writeHvdcTerminals(network, cimNamespace, writer);
    }

    private static void writeBusTerminals(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            for (Terminal t : b.getConnectedTerminals()) {
                Connectable<?> c = t.getConnectable();
                String terminalId;
                if (c instanceof DanglingLine) {
                    writeBoundaryTerminal((DanglingLine) c, cimNamespace, writer);
                    terminalId = cgmesTerminalFromAlias(c, "Terminal_Network");
                } else {
                    int side = CgmesExportUtil.getTerminalSide(t, c);
                    terminalId = cgmesTerminalFromAlias(c, CgmesNames.TERMINAL + side);
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

    private static void writeSwitchesTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Set<String> addedTopologicalNodes = new HashSet<>();
        for (Switch sw : network.getSwitches()) {
            VoltageLevel vl = sw.getVoltageLevel();

            int node1 = 0;
            int node2 = 0;
            Bus bus1;
            Bus bus2;
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                node1 = vl.getNodeBreakerView().getNode1(sw.getId());
                node2 = vl.getNodeBreakerView().getNode2(sw.getId());
                bus1 = Optional.ofNullable(getTerminalForBusBreakerViewBus(vl, node1)).map(t -> t.getBusBreakerView().getBus()).orElse(null);
                bus2 = Optional.ofNullable(getTerminalForBusBreakerViewBus(vl, node2)).map(t -> t.getBusBreakerView().getBus()).orElse(null);
            } else {
                bus1 = vl.getBusBreakerView().getBus1(sw.getId());
                bus2 = vl.getBusBreakerView().getBus2(sw.getId());
            }

            String cgmesTerminal1 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL1);
            String cgmesTerminal2 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL2);

            String tn1 = writeTopologicalNode(bus1, sw.getVoltageLevel(), node1, addedTopologicalNodes, cimNamespace, writer, context);
            String tn2 = tn1;
            if (node1 != node2) {
                tn2 = writeTopologicalNode(bus2, sw.getVoltageLevel(), node2, addedTopologicalNodes, cimNamespace, writer, context);
            }
            writeSwitchTerminal(tn1, cgmesTerminal1, cimNamespace, writer, context);
            writeSwitchTerminal(tn2, cgmesTerminal2, cimNamespace, writer, context);
        }
    }

    private static String writeTopologicalNode(Bus bus, VoltageLevel voltageLevel, int node, Set<String> addedTopologicalNodes, String cimNamespace,
                                               XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String tn = bus != null ? bus.getId() : null;
        if (tn == null) {
            tn = voltageLevel.getId() + "_" + node;
            if (!addedTopologicalNodes.contains(tn)) {
                addedTopologicalNodes.add(tn);
                writeTopologicalNode(tn, tn, voltageLevel.getId(),
                        context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()).getId(), cimNamespace, writer);
            }
        }

        return tn;
    }

    private static void writeSwitchTerminal(String tn, String cgmesTerminal, String cimNamespace,
                XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeTerminal(cgmesTerminal, tn, cimNamespace, writer);
    }

    public static Terminal getTerminalForBusBreakerViewBus(VoltageLevel voltageLevel, int node) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new IllegalArgumentException("The voltage level " + voltageLevel.getId() + " is not described in Node/Breaker topology");
        }

        Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(node);
        if ( terminal != null) {
            return terminal;
        }

        Terminal[] equivalentTerminal = new Terminal[1];

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw != null && (sw.isOpen() || sw.isRetained())) {
                return TraverseResult.TERMINATE_PATH;
            }
            Terminal t = voltageLevel.getNodeBreakerView().getTerminal(node2);
            if (t != null) {
                equivalentTerminal[0] = t;
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(node, traverser);

        return equivalentTerminal[0];
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
        writer.writeStartElement(cimNamespace, "DCNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, "#" + dcNode);
        writer.writeEmptyElement(cimNamespace, "DCNode.DCTopologicalNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dcTopologicalNode);
        writer.writeEndElement();
    }

    private static void writeAcdcConverterDCTerminal(String acdcConverterDcTerminal, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "ACDCConverterDCTerminal");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, "#" + acdcConverterDcTerminal);
        writer.writeEmptyElement(cimNamespace, "DCBaseTerminal.DCTopologicalNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dcTopologicalNode);
        writer.writeEndElement();
    }

    private static void writeDCTerminal(String dcTerminal, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "DCTerminal");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, "#" + dcTerminal);
        writer.writeEmptyElement(cimNamespace, "DCBaseTerminal.DCTopologicalNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + dcTopologicalNode);
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
        writer.writeStartElement(cimNamespace, "Terminal");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, "#" + terminalId);
        writer.writeEmptyElement(cimNamespace, "Terminal.TopologicalNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + topologicalNode);
        writer.writeEndElement();
    }

    private static void writeTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeBusTopologicalNodes(network, cimNamespace, writer, context);
        writeDanglingLineTopologicalNodes(network, cimNamespace, writer, context);
        writeHvdcTopologicalNodes(network, cimNamespace, writer);
    }

    private static void writeBusTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            if (b.getConnectedTerminalCount() == 0) {
                continue;
            }
            String baseVoltage = context.getBaseVoltageByNominalVoltage(b.getVoltageLevel().getNominalV()).getId();
            writeTopologicalNode(b.getId(), b.getNameOrId(), b.getVoltageLevel().getId(), baseVoltage, cimNamespace, writer);
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
        writer.writeStartElement(cimNamespace, "DCTopologicalNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, dcTopologicalNode);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(dcTopologicalNodeName);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeTopologicalNode(String topologicalNode, String topologicalNodeName, String connectivityNodeContainerId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "TopologicalNode");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, topologicalNode);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(topologicalNodeName);
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, TOPOLOGICAL_NODE_CONNECTIVITY_NODE_CONTAINER);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + connectivityNodeContainerId);
        writer.writeEmptyElement(cimNamespace, TOPOLOGICAL_NODE_BASE_VOLTAGE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + baseVoltageId);
        writer.writeEndElement();
    }

    private TopologyExport() {
    }
}
