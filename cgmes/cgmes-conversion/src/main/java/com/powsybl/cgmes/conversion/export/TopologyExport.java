/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_BUSBAR_SECTION_TERMINALS;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.DC_TOPOLOGICAL_NODE;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.TOPOLOGICAL_NODE;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class TopologyExport {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyExport.class);
    private static final String TOPOLOGICAL_NODE_CONNECTIVITY_NODE_CONTAINER = "TopologicalNode.ConnectivityNodeContainer";
    private static final String TOPOLOGICAL_NODE_BASE_VOLTAGE = "TopologicalNode.BaseVoltage";

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network).setExportEquipment(false));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String cimNamespace = context.getCim().getNamespace();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(network, CgmesSubset.TOPOLOGY, writer, context.getTpModelDescription(), context);
            }

            writeTopologicalNodes(network, cimNamespace, writer, context);
            writeTerminals(network, cimNamespace, writer, context);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeConnectableTerminals(network, cimNamespace, writer, context);
        writeBoundaryTerminals(network, cimNamespace, writer, context);
        writeSwitchesTerminals(network, cimNamespace, writer, context);
        writeDcTerminals(network, cimNamespace, writer, context);
        // Only if it is an updated export
        if (!context.isExportEquipment()) {
            writeBusbarSectionTerminalsFromBusBranchCgmesModel(network, cimNamespace, writer, context);
        }
    }

    private static void writeBusbarSectionTerminalsFromBusBranchCgmesModel(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
            String bbsTerminals = b.getProperty(PROPERTY_BUSBAR_SECTION_TERMINALS, "");
            if (!bbsTerminals.isEmpty()) {
                for (String bbsTerminal : bbsTerminals.split(",")) {
                    writeTerminal(bbsTerminal, topologicalNodeId, cimNamespace, writer, context);
                }
            }
        }
    }

    private static void writeConnectableTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Connectable<?> c : network.getConnectables()) { // TODO write boundary terminals for tie lines from CGMES
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    Bus b = t.getBusBreakerView().getBus();
                    if (b == null) {
                        b = t.getBusBreakerView().getConnectableBus();
                    }
                    // An isolated busbar section in a node/breaker model does not have even a connectable bus
                    if (b != null) {
                        String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
                        writeTerminal(CgmesExportUtil.getTerminalId(t, context), topologicalNodeId, cimNamespace, writer, context);
                    }
                }
            }
        }
    }

    private static void writeBoundaryTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        List<String> exported = new ArrayList<>();
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.ALL)) {
            writeBoundaryTerminal(dl, exported, cimNamespace, writer, context);
        }
    }

    private static void writeSwitchesTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            if (!context.isExportedEquipment(sw)) {
                continue;
            }
            VoltageLevel vl = sw.getVoltageLevel();

            String tn1;
            String tname1;
            String tn2;
            String tname2;
            Bus bus1;
            Bus bus2;
            if (vl.getTopologyKind().equals(TopologyKind.BUS_BREAKER)) {
                bus1 = vl.getBusBreakerView().getBus1(sw.getId());
                tn1 = context.getNamingStrategy().getCgmesId(bus1);
                tname1 = vl.getBusBreakerView().getBus1(sw.getId()).getNameOrId();
                bus2 = vl.getBusBreakerView().getBus2(sw.getId());
                tn2 = context.getNamingStrategy().getCgmesId(bus2);
                tname2 = vl.getBusBreakerView().getBus2(sw.getId()).getNameOrId();
            } else {
                int node1 = vl.getNodeBreakerView().getNode1(sw.getId());
                bus1 = getBusForBusBreakerViewBus(vl, node1);
                tn1 = bus1 == null ? findOrCreateTopologicalNode(vl, node1, context) : context.getNamingStrategy().getCgmesId(bus1);
                tname1 = bus1 == null ? tn1 : bus1.getNameOrId();

                int node2 = vl.getNodeBreakerView().getNode2(sw.getId());
                bus2 = getBusForBusBreakerViewBus(vl, node2);
                tn2 = bus2 == null ? findOrCreateTopologicalNode(vl, node2, context) : context.getNamingStrategy().getCgmesId(bus2);
                tname2 = bus2 == null ? tn2 : bus2.getNameOrId();
            }
            writeTopologicalNode(tn1, tname1, bus1, vl, cimNamespace, writer, context);
            writeTopologicalNode(tn2, tname2, bus2, vl, cimNamespace, writer, context);

            String cgmesTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
            String cgmesTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2);

            writeSwitchTerminal(tn1, cgmesTerminal1, cimNamespace, writer, context);
            writeSwitchTerminal(tn2, cgmesTerminal2, cimNamespace, writer, context);
        }
    }

    private static String findOrCreateTopologicalNode(VoltageLevel vl, int node, CgmesExportContext context) {
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
            return context.getNamingStrategy().getCgmesId(selectedBus.get());
        }

        String disconnectedBusId = nodeSet.stream()
                .sorted()
                .findFirst()
                .map(selectedNode -> vl.getId() + "_" + selectedNode)
                .orElseThrow(() -> new PowsyblException("nodeSet is never empty"));
        if (vl.getNetwork().getIdentifiable(disconnectedBusId) != null) { // can happen, particularly with busbar sections - must be distinct or mRIDs will be identical
            disconnectedBusId += "_TN";
        }
        return context.getNamingStrategy().getCgmesId(disconnectedBusId);
    }

    private static void writeTopologicalNode(String tn, String tname, Bus bus, VoltageLevel voltageLevel, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        if (context.containsTopologicalNode(tn)) {
            return;
        }
        context.putTopologicalNode(tn, bus);
        writeTopologicalNode(tn, tname, context.getNamingStrategy().getCgmesId(voltageLevel),
            context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()).getId(), cimNamespace, writer, context);
    }

    private static void writeSwitchTerminal(String tn, String cgmesTerminal, String cimNamespace,
                XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeTerminal(cgmesTerminal, tn, cimNamespace, writer, context);
    }

    private static Bus getBusForBusBreakerViewBus(VoltageLevel vl, int node) {
        Terminal terminal = vl.getNodeBreakerView().getTerminal(node);
        if (terminal == null) {
            return null;
        }

        return terminal.getBusBreakerView().getBus();
    }

    private static void writeDcTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            writeDcTerminals(line, line.getConverterStation1(), 1, cimNamespace, writer, context);
            writeDcTerminals(line, line.getConverterStation2(), 2, cimNamespace, writer, context);
        }
    }

    private static void writeDcTerminals(HvdcLine line, HvdcConverterStation<?> converter, int side, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Bus bus = converter.getTerminal().getBusBreakerView().getBus();
        if (bus == null) {
            bus = converter.getTerminal().getBusBreakerView().getConnectableBus();
        }
        if (bus == null) {
            return;
        }
        String dcTopologicalNode = context.getNamingStrategy().getCgmesId(refTyped(bus), DC_TOPOLOGICAL_NODE);
        String dcNode = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + side).orElseThrow(PowsyblException::new);
        writeDCNode(dcNode, dcTopologicalNode, cimNamespace, writer, context);
        String dcTerminal = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal" + side).orElseThrow(PowsyblException::new);
        writeDCTerminal(dcTerminal, dcTopologicalNode, cimNamespace, writer, context);
        String acdcConverterDcTerminal = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal").orElseThrow(PowsyblException::new);
        writeAcdcConverterDCTerminal(acdcConverterDcTerminal, dcTopologicalNode, cimNamespace, writer, context);
    }

    private static void writeDCNode(String dcNode, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("DCNode", dcNode, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("DCNode.DCTopologicalNode", dcTopologicalNode, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private static void writeAcdcConverterDCTerminal(String acdcConverterDcTerminal, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("ACDCConverterDCTerminal", acdcConverterDcTerminal, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("DCBaseTerminal.DCTopologicalNode", dcTopologicalNode, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private static void writeDCTerminal(String dcTerminal, String dcTopologicalNode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("DCTerminal", dcTerminal, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("DCBaseTerminal.DCTopologicalNode", dcTopologicalNode, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private static void writeBoundaryTerminal(DanglingLine dl, List<String> exported, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String boundaryId = context.getNamingStrategy().getCgmesIdFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary");
        String equivalentInjectionTerminalId = context.getNamingStrategy().getCgmesIdFromProperty(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
        String topologicalNode = dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
        // Topological nodes of boundaries are published by external entities and should be ok,
        // we do not make an additional effort to ensure a valid CGMES id has been assigned
        // If not defined it has already been created above so topological node is never null
        if (boundaryId != null) {
            writeTerminal(boundaryId, topologicalNode, cimNamespace, writer, context);
        }
        if (equivalentInjectionTerminalId != null && !exported.contains(equivalentInjectionTerminalId)) { // check if the equivalent injection terminal has already been written (if several dangling lines linked to same X-node)
            writeTerminal(equivalentInjectionTerminalId, topologicalNode, cimNamespace, writer, context);
            exported.add(equivalentInjectionTerminalId);
        }
    }

    private static void writeTerminal(String terminalId, String topologicalNode, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartAbout("Terminal", terminalId, cimNamespace, writer, context);
        CgmesExportUtil.writeReference("Terminal.TopologicalNode", topologicalNode, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private static void writeTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeBusTopologicalNodes(network, cimNamespace, writer, context);
        writeDcTopologicalNodes(network, cimNamespace, writer, context);
        // We create topological nodes for boundary side of dangling lines that are not mapped to an external boundary node
        writeDanglingLineTopologicalNodes(network, cimNamespace, writer, context);
    }

    private static void writeDanglingLineTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.ALL)) {
            String topologicalNodeId = dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
            if (topologicalNodeId == null) {
                // If no information about original boundary has been preserved in the IIDM model,
                // we will create a new TopologicalNode
                String baseVoltage = context.getBaseVoltageByNominalVoltage(dl.getTerminal().getVoltageLevel().getNominalV()).getId();
                // If the EQ has also been exported, a fictitious container should have been created
                String containerId = context.getFictitiousContainerFor(dl);
                if (containerId == null) {
                    // As a last resort, we create the TN in the same container of the dangling line
                    LOG.error("Dangling line {}{} is not connected to a topology node in boundaries files: EQ profile must be exported for consistent results." +
                                    " Dangling line {} is considered entirely inside voltage level {}",
                            dl.getId(), dl.getPairingKey() != null ? " linked to X-node " + dl.getPairingKey() : "", dl.getId(), dl.getTerminal().getVoltageLevel().getId());
                    containerId = context.getNamingStrategy().getCgmesId(dl.getTerminal().getVoltageLevel());
                }
                String fictTopologicalNodeId = context.getNamingStrategy().getCgmesId(refTyped(dl), TOPOLOGICAL_NODE);
                dl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY, fictTopologicalNodeId);
                writeTopologicalNode(fictTopologicalNodeId, dl.getNameOrId() + "_NODE", containerId, baseVoltage, cimNamespace, writer, context);
            }
        }
    }

    private static void writeBusTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
            writeTopologicalNode(topologicalNodeId, b.getNameOrId(), b, b.getVoltageLevel(), cimNamespace, writer, context);
        }
    }

    private static void writeDcTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Set<String> written = new HashSet<>();
        for (HvdcLine line : network.getHvdcLines()) {
            writeDCTopologicalNode(line, 1, line.getConverterStation1(), written, cimNamespace, writer, context);
            writeDCTopologicalNode(line, 2, line.getConverterStation2(), written, cimNamespace, writer, context);
        }
    }

    private static void writeDCTopologicalNode(HvdcLine line, int side, HvdcConverterStation<?> converter, Set<String> written, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Bus b = converter.getTerminal().getBusBreakerView().getBus();
        if (b == null) {
            b = converter.getTerminal().getBusBreakerView().getConnectableBus();
        }
        if (b != null && !written.contains(b.getId())) {
            String id = context.getNamingStrategy().getCgmesId(refTyped(b), DC_TOPOLOGICAL_NODE);
            String name = line.getNameOrId() + side;
            writeDCTopologicalNode(id, name, cimNamespace, writer, context);
            written.add(b.getId());
        }
    }

    private static void writeDCTopologicalNode(String id, String name, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("DCTopologicalNode", id, name, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private static void writeTopologicalNode(String topologicalNode, String topologicalNodeName, String connectivityNodeContainerId, String baseVoltageId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        CgmesExportUtil.writeStartIdName("TopologicalNode", topologicalNode, topologicalNodeName, cimNamespace, writer, context);
        CgmesExportUtil.writeReference(TOPOLOGICAL_NODE_CONNECTIVITY_NODE_CONTAINER, connectivityNodeContainerId, cimNamespace, writer, context);
        CgmesExportUtil.writeReference(TOPOLOGICAL_NODE_BASE_VOLTAGE, baseVoltageId, cimNamespace, writer, context);
        writer.writeEndElement();
    }

    private TopologyExport() {
    }
}
