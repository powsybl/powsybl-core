/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Optional;

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

            if (context.getCimVersion() == 16) {
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
        writeBusTerminals(network, cimNamespace, writer, context);
        writeHvdcTerminals(network, cimNamespace, writer, context);
    }

    private static void writeBusTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusView().getBuses()) {
            for (Terminal t : b.getConnectedTerminals()) {
                Connectable<?> c = t.getConnectable();
                String terminalId;
                if (c instanceof DanglingLine) {
                    writeBoundaryTerminal((DanglingLine) c, cimNamespace, writer);
                    terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network").orElseThrow(PowsyblException::new);
                } else {
                    int sequenceNumber = CgmesExportUtil.getTerminalSide(t, c);
                    terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber).orElseThrow(PowsyblException::new);
                }
                for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b.getId())) {
                    writeTerminal(terminalId, topologicalNode.getCgmesId(), cimNamespace, writer);
                }
            }
        }
    }

    private static void writeHvdcTerminals(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            writeHvdcBusTerminals(line, b1, 1, cimNamespace, writer, context);

            Bus b2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            writeHvdcBusTerminals(line, b2, 2, cimNamespace, writer, context);
        }
    }

    private static void writeHvdcBusTerminals(HvdcLine line, Bus bus, int side, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(bus.getId())) {
            String dcTopologicalNode = topologicalNode.getCgmesId() + "DC";
            String dcNode = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode" + side).orElseThrow(PowsyblException::new);
            writeDCNode(dcNode, dcTopologicalNode, cimNamespace, writer);
            String dcTerminal = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal" + side).orElseThrow(PowsyblException::new);
            writeDCTerminal(dcTerminal, dcTopologicalNode, cimNamespace, writer);
            String acdcConverterDcTerminal = line.getConverterStation2().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal").orElseThrow(PowsyblException::new);
            writeAcdcConverterDCTerminal(acdcConverterDcTerminal, dcTopologicalNode, cimNamespace, writer);
        }
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
        writeHvdcTopologicalNodes(network, cimNamespace, writer, context);
    }

    private static void writeBusTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusView().getBuses()) {
            for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b.getId())) {
                if (topologicalNode.getSource().equals(CgmesIidmMapping.Source.IGM)) {
                    String baseVoltage = context.getBaseVoltageByNominalVoltage(b.getVoltageLevel().getNominalV()).getCgmesId();
                    writeTopologicalNode(topologicalNode.getCgmesId(), topologicalNode.getName(), b.getVoltageLevel().getId(), baseVoltage, cimNamespace, writer);
                }
            }
        }
    }

    private static void writeDanglingLineTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Optional<String> topologicalNodeId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNodeId.isPresent()) {
                CgmesIidmMapping.CgmesTopologicalNode cgmesTopologicalNode = context.getUnmappedTopologicalNode(topologicalNodeId.get());
                if (cgmesTopologicalNode != null && cgmesTopologicalNode.getSource().equals(CgmesIidmMapping.Source.IGM)) {
                    String baseVoltage = context.getBaseVoltageByNominalVoltage(dl.getBoundary().getVoltageLevel().getNominalV()).getCgmesId();
                    writeTopologicalNode(cgmesTopologicalNode.getCgmesId(), dl.getNameOrId(), dl.getBoundary().getVoltageLevel().getId(), baseVoltage, cimNamespace, writer);
                }
            }
        }
    }

    private static void writeHvdcTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (HvdcLine line : network.getHvdcLines()) {
            Bus b1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b1.getId())) {
                writeDCTopologicalNode(topologicalNode.getCgmesId() + "DC", line.getNameOrId() + 1, cimNamespace, writer);
            }

            Bus b2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            for (CgmesIidmMapping.CgmesTopologicalNode topologicalNode : context.getTopologicalNodesByBusViewBus(b2.getId())) {
                writeDCTopologicalNode(topologicalNode.getCgmesId() + "DC", line.getNameOrId() + 2, cimNamespace, writer);
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
