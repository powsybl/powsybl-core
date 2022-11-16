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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class TopologyExport extends AbstractCgmesExporter {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyExport.class);
    private static final String TOPOLOGICAL_NODE_CONNECTIVITY_NODE_CONTAINER = "TopologicalNode.ConnectivityNodeContainer";
    private static final String TOPOLOGICAL_NODE_BASE_VOLTAGE = "TopologicalNode.BaseVoltage";

    TopologyExport(CgmesExportContext context, XMLStreamWriter xmlWriter) {
        super(context, xmlWriter);
    }

    public void export() {
        try {
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), xmlWriter);
            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(xmlWriter, context.getTpModelDescription(), context);
            }

            writeTopologicalNodes();
            // FIXME(Luma) check if we have written ConnectivityNode-TopologicalNode association
            writeTerminals();
            xmlWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeTerminals() throws XMLStreamException {
        writeConnectableTerminals();
        writeBoundaryTerminals();
        writeSwitchesTerminals();
        writeHvdcTerminals();
    }

    private void writeConnectableTerminals() throws XMLStreamException {
        for (Connectable<?> c : context.getNetwork().getConnectables()) {
            if (context.isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    Bus b = t.getBusBreakerView().getBus();
                    if (b == null) {
                        b = t.getBusBreakerView().getConnectableBus();
                    }
                    String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
                    writeTerminal(CgmesExportUtil.getTerminalId(t, context), topologicalNodeId);
                }
            }
        }
    }

    private void writeBoundaryTerminals() throws XMLStreamException {
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            writeBoundaryTerminal(dl);
        }
    }

    private void writeSwitchesTerminals() throws XMLStreamException {
        for (Switch sw : context.getNetwork().getSwitches()) {
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
            writeTopologicalNode(tn1, tname1, vl);
            writeTopologicalNode(tn2, tname2, vl);

            String cgmesTerminal1 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
            String cgmesTerminal2 = context.getNamingStrategy().getCgmesIdFromAlias(sw, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2);

            writeSwitchTerminal(tn1, cgmesTerminal1);
            writeSwitchTerminal(tn2, cgmesTerminal2);
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

    private void writeTopologicalNode(String tn, String tname, VoltageLevel voltageLevel) throws XMLStreamException {
        if (context.isExportedTopologicalNode(tn)) {
            return;
        }
        context.exportedTopologicalNode(tn);
        writeTopologicalNode(tn, tname, context.getNamingStrategy().getCgmesId(voltageLevel),
            context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()).getId());
    }

    private void writeSwitchTerminal(String tn, String cgmesTerminal) throws XMLStreamException {
        writeTerminal(cgmesTerminal, tn);
    }

    private static Bus getBusForBusBreakerViewBus(VoltageLevel vl, int node) {
        Terminal terminal = vl.getNodeBreakerView().getTerminal(node);
        if (terminal == null) {
            return null;
        }

        return terminal.getBusBreakerView().getBus();
    }

    private void writeHvdcTerminals() throws XMLStreamException {
        for (HvdcLine line : context.getNetwork().getHvdcLines()) {
            writeHvdcBusTerminals(line, line.getConverterStation1(), 1);
            writeHvdcBusTerminals(line, line.getConverterStation2(), 2);
        }
    }

    private void writeHvdcBusTerminals(HvdcLine line, HvdcConverterStation<?> converter, int side) throws XMLStreamException {
        Bus bus = converter.getTerminal().getBusBreakerView().getBus();
        String dcTopologicalNode = context.getNamingStrategy().getCgmesId(bus) + "DC";
        String dcNode = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + side).orElseThrow(PowsyblException::new);
        writeDCNode(dcNode, dcTopologicalNode);
        String dcTerminal = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal" + side).orElseThrow(PowsyblException::new);
        writeDCTerminal(dcTerminal, dcTopologicalNode);
        String acdcConverterDcTerminal = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal").orElseThrow(PowsyblException::new);
        writeAcdcConverterDCTerminal(acdcConverterDcTerminal, dcTopologicalNode);
    }

    private void writeDCNode(String dcNode, String dcTopologicalNode) throws XMLStreamException {
        writeStartAbout("DCNode", dcNode);
        writeReference("DCNode.DCTopologicalNode", dcTopologicalNode);
        xmlWriter.writeEndElement();
    }

    private void writeAcdcConverterDCTerminal(String acdcConverterDcTerminal, String dcTopologicalNode) throws XMLStreamException {
        writeStartAbout("ACDCConverterDCTerminal", acdcConverterDcTerminal);
        writeReference("DCBaseTerminal.DCTopologicalNode", dcTopologicalNode);
        xmlWriter.writeEndElement();
    }

    private void writeDCTerminal(String dcTerminal, String dcTopologicalNode) throws XMLStreamException {
        writeStartAbout("DCTerminal", dcTerminal);
        writeReference("DCBaseTerminal.DCTopologicalNode", dcTopologicalNode);
        xmlWriter.writeEndElement();
    }

    private void writeBoundaryTerminal(DanglingLine dl) throws XMLStreamException {
        String boundaryId = context.getNamingStrategy().getCgmesIdFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary");
        String equivalentInjectionTerminalId = context.getNamingStrategy().getCgmesIdFromAlias(dl, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
        Optional<String> topologicalNode = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
        if (topologicalNode.isPresent()) {
            // Topological nodes of boundaries are published by external entities and should be ok,
            // we do not make an additional effort to ensure a valid CGMES id has been assigned
            if (boundaryId != null) {
                writeTerminal(boundaryId, topologicalNode.get());
            }
            if (equivalentInjectionTerminalId != null) {
                writeTerminal(equivalentInjectionTerminalId, topologicalNode.get());
            }
        }
    }

    private void writeTerminal(String terminalId, String topologicalNode) throws XMLStreamException {
        writeStartAbout("Terminal", terminalId);
        writeReference("Terminal.TopologicalNode", topologicalNode);
        xmlWriter.writeEndElement();
    }

    private void writeTopologicalNodes() throws XMLStreamException {
        writeBusTopologicalNodes();
        writeHvdcTopologicalNodes();
        // We create topological nodes for boundary side of dangling lines that are not mapped to an external boundary node
        writeDanglingLineTopologicalNodes();
    }

    private void writeDanglingLineTopologicalNodes() throws XMLStreamException {
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            Optional<String> topologicalNodeId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
            if (topologicalNodeId.isEmpty()) {
                // If no information about original boundary has been preserved in the IIDM model,
                // we will create a new TopologicalNode
                String baseVoltage = context.getBaseVoltageByNominalVoltage(dl.getTerminal().getVoltageLevel().getNominalV()).getId();
                // If the EQ has also been exported, a fictitious container should have been created
                String containerId = context.getFictitiousContainerFor(dl);
                if (containerId == null) {
                    // As a last resort, we create the TN in the same container of the dangling line
                    LOG.error("Dangling line {}{} is not connected to a topology node in boundaries files: EQ profile must be exported for consistent results." +
                                    " Dangling line {} is considered entirely inside voltage level {}",
                            dl.getId(), dl.getUcteXnodeCode() != null ? " linked to X-node " + dl.getUcteXnodeCode() : "", dl.getId(), dl.getTerminal().getVoltageLevel().getId());
                    containerId = context.getNamingStrategy().getCgmesId(dl.getTerminal().getVoltageLevel());
                }
                String fictTopologicalNodeId = CgmesExportUtil.getUniqueId();
                dl.addAlias(fictTopologicalNodeId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
                writeTopologicalNode(fictTopologicalNodeId, dl.getNameOrId() + "_NODE", containerId, baseVoltage);
            }
        }
    }

    private void writeBusTopologicalNodes() throws XMLStreamException {
        for (Bus b : context.getNetwork().getBusBreakerView().getBuses()) {
            String topologicalNodeId = context.getNamingStrategy().getCgmesId(b);
            writeTopologicalNode(topologicalNodeId, b.getNameOrId(), b.getVoltageLevel());
        }
    }

    private void writeHvdcTopologicalNodes() throws XMLStreamException {
        Set<String> written = new HashSet<>();
        for (HvdcLine line : context.getNetwork().getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusBreakerView().getBus();
            if (!written.contains(b1.getId())) {
                writeDCTopologicalNode(b1.getId() + "DC", line.getNameOrId() + 1);
                written.add(b1.getId());
            }

            Bus b2 = line.getConverterStation2().getTerminal().getBusBreakerView().getBus();
            if (!written.contains(b2.getId())) {
                writeDCTopologicalNode(b2.getId() + "DC", line.getNameOrId() + 2);
                written.add(b2.getId());
            }
        }
    }

    private void writeDCTopologicalNode(String dcTopologicalNode, String dcTopologicalNodeName) throws XMLStreamException {
        writeStartIdName("DCTopologicalNode", dcTopologicalNode, dcTopologicalNodeName);
        xmlWriter.writeEndElement();
    }

    private void writeTopologicalNode(String topologicalNode, String topologicalNodeName, String connectivityNodeContainerId, String baseVoltageId) throws XMLStreamException {
        writeStartIdName("TopologicalNode", topologicalNode, topologicalNodeName);
        writeReference(TOPOLOGICAL_NODE_CONNECTIVITY_NODE_CONTAINER, connectivityNodeContainerId);
        writeReference(TOPOLOGICAL_NODE_BASE_VOLTAGE, baseVoltageId);
        xmlWriter.writeEndElement();
    }
}
