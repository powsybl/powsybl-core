/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.extensions.Source;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;

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
        write(network, writer, new CgmesExportContext(network).setExportEquipment(false));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            String cimNamespace = context.getCim().getNamespace();
            CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);

            if (context.getCimVersion() >= 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getTpModelDescription(), context);
            }
            writeTopologicalNodes(network, cimNamespace, writer, context);
            // TODO: write ConnectivityNode-TopologicalNode association
            writeTerminals(network, cimNamespace, writer, context);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeBusTerminals(network, cimNamespace, writer, context);
        writeBoundaryTerminals(network, cimNamespace, writer);
        writeSwitchesTerminals(network, cimNamespace, writer, context);
        writeHvdcTerminals(network, cimNamespace, writer, context);
    }

    private static void writeBusTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusView().getBuses()) {
            for (Terminal t : b.getConnectedTerminals()) {
                if (context.isExportedEquipment(t.getConnectable())) {
                    writeTerminal(CgmesExportUtil.getTerminalId(t), topologicalNodeFromIidmBus(b, context), cimNamespace, writer);
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

    private static String topologicalNodeFromIidmBus(Bus b, CgmesExportContext context) {
        return b != null ? context.getTopologicalNodesByBusViewBus(b.getId()).stream().findFirst().orElseThrow(PowsyblException::new).getCgmesId() : null;
    }

    private static void writeSwitchesTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Switch sw : network.getSwitches()) {
            VoltageLevel vl = sw.getVoltageLevel();

            Bus bus1;
            Bus bus2;
            if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                bus1 = Optional.ofNullable(Networks.getEquivalentTerminal(vl, vl.getNodeBreakerView().getNode1(sw.getId()))).map(t -> t.getBusView().getBus()).orElse(null);
                bus2 = Optional.ofNullable(Networks.getEquivalentTerminal(vl, vl.getNodeBreakerView().getNode2(sw.getId()))).map(t -> t.getBusView().getBus()).orElse(null);
            } else {
                bus1 = vl.getBusView().getMergedBus(vl.getBusBreakerView().getBus1(sw.getId()).getId());
                bus2 = vl.getBusView().getMergedBus(vl.getBusBreakerView().getBus2(sw.getId()).getId());
            }

            String cgmesTerminal1 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL1);
            String cgmesTerminal2 = cgmesTerminalFromAlias(sw, CgmesNames.TERMINAL2);

            writeSwitchTerminal(bus1, sw.getVoltageLevel(), cgmesTerminal1, cimNamespace, writer, context);
            writeSwitchTerminal(bus2, sw.getVoltageLevel(), cgmesTerminal2, cimNamespace, writer, context);
        }
    }

    private static void writeSwitchTerminal(Bus bus, VoltageLevel voltageLevel, String cgmesTerminal, String cimNamespace,
                                            XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String tn = topologicalNodeFromIidmBus(bus, context);
        if (tn == null) {
            tn = CgmesExportUtil.getUniqueId();
            writeTopologicalNode(tn, tn, voltageLevel.getId(),
                    context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()).getId(), cimNamespace, writer);
        }
        writeTerminal(cgmesTerminal, tn, cimNamespace, writer);
    }

    private static void writeHvdcTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            writeHvdcBusTerminals(line, line.getConverterStation1(), b1, 1, cimNamespace, writer, context);

            Bus b2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            writeHvdcBusTerminals(line, line.getConverterStation2(), b2, 2, cimNamespace, writer, context);
        }
    }

    private static void writeHvdcBusTerminals(HvdcLine line, HvdcConverterStation<?> converter, Bus bus, int side, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String iidmId;
        if (bus == null) {
            iidmId = line.getId() + side;
        } else {
            iidmId = bus.getId();
        }
        for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(iidmId)) {
            String dcTopologicalNode = topologicalNode.getCgmesId() + "DC";
            String dcNode = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + side).orElseThrow(PowsyblException::new);
            writeDCNode(dcNode, dcTopologicalNode, cimNamespace, writer);
            String dcTerminal = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal" + side).orElseThrow(PowsyblException::new);
            writeDCTerminal(dcTerminal, dcTopologicalNode, cimNamespace, writer);
            String acdcConverterDcTerminal = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal").orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal, dcTopologicalNode, cimNamespace, writer);
        }
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

    private static void writeTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeBusTopologicalNodes(network, cimNamespace, writer, context);
        writeDanglingLineTopologicalNodes(network, cimNamespace, writer, context);
        writeHvdcTopologicalNodes(network, cimNamespace, writer, context);
    }

    private static void writeBusTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusView().getBuses()) {
            for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b.getId())) {
                if (topologicalNode.getSource().equals(Source.IGM)) {
                    String baseVoltage = context.getBaseVoltageByNominalVoltage(b.getVoltageLevel().getNominalV()).getId();
                    writeTopologicalNode(topologicalNode.getCgmesId(), topologicalNode.getName(), context.getNamingStrategy().getCgmesId(b.getVoltageLevel()), baseVoltage, cimNamespace, writer);
                }
            }
        }
    }

    private static void writeDanglingLineTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Optional<String> topologicalNodeId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNodeId.isPresent()) {
                CgmesIidmMapping.CgmesTopologicalNode cgmesTopologicalNode = context.getUnmappedTopologicalNode(topologicalNodeId.get());
                if (cgmesTopologicalNode != null && cgmesTopologicalNode.getSource().equals(Source.IGM)) {
                    String baseVoltage = context.getBaseVoltageByNominalVoltage(dl.getBoundary().getVoltageLevel().getNominalV()).getId();
                    writeTopologicalNode(cgmesTopologicalNode.getCgmesId(), dl.getNameOrId(), context.getNamingStrategy().getCgmesId(dl.getBoundary().getVoltageLevel()), baseVoltage, cimNamespace, writer);
                }
            }
        }
    }

    private static void writeHvdcTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Set<String> written = new HashSet<>();
        for (HvdcLine line : network.getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            if (b1 != null) {
                for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b1.getId())) {
                    if (!written.contains(topologicalNode.getCgmesId())) {
                        writeDCTopologicalNode(topologicalNode.getCgmesId() + "DC", line.getNameOrId() + 1, cimNamespace, writer);
                        written.add(topologicalNode.getCgmesId());
                    }
                }
            } else {
                String topologicalNode = CgmesExportUtil.getUniqueId();
                context.putTopologicalNode(line.getId() + 1, topologicalNode);
                writeDCTopologicalNode(topologicalNode + "DC", line.getNameOrId() + 1, cimNamespace, writer);
            }

            Bus b2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            if (b2 != null) {
                for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b2.getId())) {
                    if (!written.contains(topologicalNode.getCgmesId())) {
                        writeDCTopologicalNode(topologicalNode.getCgmesId() + "DC", line.getNameOrId() + 2, cimNamespace, writer);
                        written.add(topologicalNode.getCgmesId());
                    }
                }
            } else {
                String topologicalNode = CgmesExportUtil.getUniqueId();
                context.putTopologicalNode(line.getId() + 2, topologicalNode);
                writeDCTopologicalNode(topologicalNode + "DC", line.getNameOrId() + 2, cimNamespace, writer);
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
