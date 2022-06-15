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
public final class TopologyExport extends AbstractCgmesExporter {

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
            // TODO: write ConnectivityNode-TopologicalNode association
            writeTerminals();
            xmlWriter.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writeTerminals() throws XMLStreamException {
        writeBusTerminals();
        writeBoundaryTerminals();
        writeSwitchesTerminals();
        writeHvdcTerminals();
    }

    private void writeBusTerminals() throws XMLStreamException {
        for (Bus b : context.getNetwork().getBusView().getBuses()) {
            for (Terminal t : b.getConnectedTerminals()) {
                if (context.isExportedEquipment(t.getConnectable())) {
                    writeTerminal(CgmesExportUtil.getTerminalId(t), topologicalNodeFromIidmBus(b));
                }
            }
        }
    }

    private void writeBoundaryTerminals() throws XMLStreamException {
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            writeBoundaryTerminal(dl);
        }
    }

    // FIXME(Luma) check if this method is duplicated in export utils
    private static String cgmesTerminalFromAlias(Identifiable<?> i, String aliasType0) {
        String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + aliasType0;
        Optional<String> cgmesTerminalId = i.getAliasFromType(aliasType);
        if (cgmesTerminalId.isEmpty()) {
            throw new PowsyblException("Missing CGMES terminal in aliases of " + i.getId() + ", aliasType " + aliasType);
        }
        return cgmesTerminalId.get();
    }

    private String topologicalNodeFromIidmBus(Bus b) {
        return b != null ? context.getTopologicalNodesByBusViewBus(b.getId()).stream().findFirst().orElseThrow(PowsyblException::new).getCgmesId() : null;
    }

    private void writeSwitchesTerminals() throws XMLStreamException {
        for (Switch sw : context.getNetwork().getSwitches()) {
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

            writeSwitchTerminal(bus1, sw.getVoltageLevel(), cgmesTerminal1);
            writeSwitchTerminal(bus2, sw.getVoltageLevel(), cgmesTerminal2);
        }
    }

    private void writeSwitchTerminal(Bus bus, VoltageLevel voltageLevel, String cgmesTerminal) throws XMLStreamException {
        String tn = topologicalNodeFromIidmBus(bus);
        if (tn == null) {
            tn = CgmesExportUtil.getUniqueId();
            writeTopologicalNode(tn, tn, voltageLevel.getId(), context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()).getId());
        }
        writeTerminal(cgmesTerminal, tn);
    }

    private void writeHvdcTerminals() throws XMLStreamException {
        for (HvdcLine line : context.getNetwork().getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            writeHvdcBusTerminals(line, line.getConverterStation1(), b1, 1);

            Bus b2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            writeHvdcBusTerminals(line, line.getConverterStation2(), b2, 2);
        }
    }

    private void writeHvdcBusTerminals(HvdcLine line, HvdcConverterStation<?> converter, Bus bus, int side) throws XMLStreamException {
        String iidmId;
        if (bus == null) {
            iidmId = line.getId() + side;
        } else {
            iidmId = bus.getId();
        }
        for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(iidmId)) {
            String dcTopologicalNode = topologicalNode.getCgmesId() + "DC";
            String dcNode = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + side).orElseThrow(PowsyblException::new);
            writeDCNode(dcNode, dcTopologicalNode);
            String dcTerminal = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal" + side).orElseThrow(PowsyblException::new);
            writeDCTerminal(dcTerminal, dcTopologicalNode);
            String acdcConverterDcTerminal = converter.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal").orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal, dcTopologicalNode);
        }
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
        String boundaryId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary").orElseThrow(PowsyblException::new);
        String equivalentInjectionTerminalId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal").orElseThrow(PowsyblException::new);
        Optional<String> topologicalNode = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
        if (topologicalNode.isPresent()) {
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
        writeDanglingLineTopologicalNodes();
        writeHvdcTopologicalNodes();
    }

    private void writeBusTopologicalNodes() throws XMLStreamException {
        for (Bus b : context.getNetwork().getBusView().getBuses()) {
            for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b.getId())) {
                if (topologicalNode.getSource().equals(Source.IGM)) {
                    String baseVoltage = context.getBaseVoltageByNominalVoltage(b.getVoltageLevel().getNominalV()).getId();
                    writeTopologicalNode(topologicalNode.getCgmesId(), topologicalNode.getName(), context.getNamingStrategy().getCgmesId(b.getVoltageLevel()), baseVoltage);
                }
            }
        }
    }

    private void writeDanglingLineTopologicalNodes() throws XMLStreamException {
        for (DanglingLine dl : context.getNetwork().getDanglingLines()) {
            Optional<String> topologicalNodeId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNodeId.isPresent()) {
                CgmesIidmMapping.CgmesTopologicalNode cgmesTopologicalNode = context.getUnmappedTopologicalNode(topologicalNodeId.get());
                if (cgmesTopologicalNode != null && cgmesTopologicalNode.getSource().equals(Source.IGM)) {
                    String baseVoltage = context.getBaseVoltageByNominalVoltage(dl.getBoundary().getVoltageLevel().getNominalV()).getId();
                    writeTopologicalNode(cgmesTopologicalNode.getCgmesId(), dl.getNameOrId(), context.getNamingStrategy().getCgmesId(dl.getBoundary().getVoltageLevel()), baseVoltage);
                }
            }
        }
    }

    private void writeHvdcTopologicalNodes() throws XMLStreamException {
        Set<String> written = new HashSet<>();
        for (HvdcLine line : context.getNetwork().getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            if (b1 != null) {
                for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b1.getId())) {
                    if (!written.contains(topologicalNode.getCgmesId())) {
                        writeDCTopologicalNode(topologicalNode.getCgmesId() + "DC", line.getNameOrId() + 1);
                        written.add(topologicalNode.getCgmesId());
                    }
                }
            } else {
                String topologicalNode = CgmesExportUtil.getUniqueId();
                context.putTopologicalNode(line.getId() + 1, topologicalNode);
                writeDCTopologicalNode(topologicalNode + "DC", line.getNameOrId() + 1);
            }

            Bus b2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            if (b2 != null) {
                for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b2.getId())) {
                    if (!written.contains(topologicalNode.getCgmesId())) {
                        writeDCTopologicalNode(topologicalNode.getCgmesId() + "DC", line.getNameOrId() + 2);
                        written.add(topologicalNode.getCgmesId());
                    }
                }
            } else {
                String topologicalNode = CgmesExportUtil.getUniqueId();
                context.putTopologicalNode(line.getId() + 2, topologicalNode);
                writeDCTopologicalNode(topologicalNode + "DC", line.getNameOrId() + 2);
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
