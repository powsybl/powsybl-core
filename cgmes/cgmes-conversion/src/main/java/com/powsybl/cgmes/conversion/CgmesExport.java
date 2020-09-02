/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.conversion.update.StateVariablesAdder;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;

import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    @Override
    public void export(Network network, Properties params, DataSource ds) {
        Objects.requireNonNull(network);
        CgmesModelExtension ext = network.getExtension(CgmesModelExtension.class);
        if (params != null && Boolean.valueOf(params.getProperty("cgmes.export.usingOnlyNetwork"))) {
            if (ext != null) {
                CgmesModel cgmesSource = ext.getCgmesModel();
                if (cgmesSource != null) {
                    throw new CgmesModelException("CGMES model should not be available as Network extension");
                }
            }
            exportUsingOnlyNetwork(network, ds);
        } else {
            if (ext == null) {
                throw new CgmesModelException("CGMES model is required and not found in Network extension");
            }
            exportUsingOriginalCgmesModel(network, params, ds, ext);
        }
    }

    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);
    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String CIM_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    private static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";
    private static final boolean INDENT = true;

    private void exportUsingOnlyNetwork(Network network, DataSource ds) {
        // We are assuming that we want CGM export when network is a MergingView
        // For a CGM export we want to obtain a single SV and individual SSH files
        exportStateVariables(network, ds);
        if (network instanceof MergingView) {
            ((MergingView) network).getNetworkStream().forEach(igm -> exportSteadyStateHypothesis(igm, ds));
        } else {
            exportSteadyStateHypothesis(network, ds);
        }
    }

    private void exportStateVariables(Network network, DataSource ds) {
        export(network, ds, "SV", CgmesExport::writeSV);
    }

    private void exportSteadyStateHypothesis(Network network, DataSource ds) {
        export(network, ds, "SSH", CgmesExport::writeSSH);
    }

    private void export(Network network, DataSource ds, String profileSuffix, BiConsumer<Network, XMLStreamWriter> exporter) {
        String baseName = network.getProperty("baseName");
        String filename = baseName + "_" + profileSuffix + ".xml";
        try (OutputStream os = ds.newOutputStream(filename, false); BufferedOutputStream bos = new BufferedOutputStream(os)) {
            try {
                XMLStreamWriter writer = initializeWriter(bos);
                exporter.accept(network, writer);
                writer.writeEndDocument();
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        } catch (IOException x) {
            throw new PowsyblException("Exporting to CGMES using only Network");
        }
    }

    private static void writeSV(Network network, XMLStreamWriter writer) {
        try {
            writeSvVoltages(network, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSSH(Network network, XMLStreamWriter writer) {
        // TODO(Luma) write updated power flow inputs
    }

    private static void writeSvVoltages(Network network, XMLStreamWriter writer) throws XMLStreamException {
        // FIXME(Luma)
        // Node-breaker: build topological nodes
        // Bus-branch: reuse topological nodes (stored in bus-breaker view buses)
        for (Bus b : network.getBusBreakerView().getBuses()) {
            // FIXME(Luma) for bus-branch the bus id in the bus-breaker view contains the topoNode id
            // For node-breaker, we can try to use aliases or properties???
            String topologicalNode = b.getId();
            writeSvVoltage(writer, topologicalNode, b.getV(), b.getAngle());
        }
        // Voltages at boundary nodes of dangling lines
        for (DanglingLine dl : network.getDanglingLines()) {
            // FIXME(Luma) Obtain from typed alias cgmes.topologicalNode
            String topologicalNode = dl.getAliases().iterator().next();
            writeSvVoltage(writer, topologicalNode, Double.valueOf(dl.getProperty("v", "NaN")), Double.valueOf(dl.getProperty("angle", "NaN")));
        }
        // Voltages at inner nodes of Tie Lines
        // (boundary nodes that have been left inside CGM)
        for (Line l : network.getLines()) {
            if (!l.isTieLine()) {
                continue;
            }
            TieLine tieLine = (TieLine) l;
            // FIXME(Luma) Obtain voltage at inner node
        }
    }

    private static void writeSvVoltage(XMLStreamWriter writer, String topologicalNode, double v, double angle) throws XMLStreamException {
        // FIXME(Luma) remove this reference block
        // <cim:SvVoltage rdf:ID="_d4548915-4f00-4507-ba54-350cafcee1af">
        // <cim:SvVoltage.angle>-18.20656</cim:SvVoltage.angle>
        // <cim:SvVoltage.v>128.575378</cim:SvVoltage.v>
        // <cim:SvVoltage.TopologicalNode rdf:resource="#_0471bd2a-c766-11e1-8775-005056c00008" />
        // </cim:SvVoltage>
        writer.writeStartElement("cim:SvVoltage");
        writer.writeAttribute("rdf:ID", getUniqueId());
        writer.writeStartElement("cim:SvVoltage.angle");
        writer.writeCharacters(Double.toString(angle));
        writer.writeEndElement();
        writer.writeStartElement("cim:SvVoltage.v");
        writer.writeCharacters(Double.toString(v));
        writer.writeEndElement();
        writer.writeEmptyElement("cim:SvVoltage.TopologicalNode");
        writer.writeAttribute("rdf:resource", "#" + topologicalNode);
        writer.writeEndElement();
    }

    private static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    private static XMLStreamWriter initializeWriter(OutputStream os) throws XMLStreamException {
        XMLStreamWriter writer;
        writer = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(os, StandardCharsets.UTF_8.toString());
        if (INDENT) {
            IndentingXMLStreamWriter indentingWriter = new IndentingXMLStreamWriter(writer);
            indentingWriter.setIndent("    ");
            writer = indentingWriter;
        }
        writer.writeStartDocument(StandardCharsets.UTF_8.toString(), "1.0");
        // FIXME(Luma) remove this reference line and consider adding entsoe namespace
        // <rdf:RDF xmlns:cim="http://iec.ch/TC57/2013/CIM-schema-cim16#"
        // xmlns:entsoe="http://entsoe.eu/CIM/SchemaExtension/3/1#"
        // xmlns:md="http://iec.ch/TC57/61970-552/ModelDescription/1#"
        // xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        writer.setPrefix("rdf", RDF_NAMESPACE);
        writer.setPrefix("cim", CIM_NAMESPACE);
        writer.setPrefix("md", MD_NAMESPACE);
        writer.writeStartElement(RDF_NAMESPACE, "RDF");
        writer.writeNamespace("rdf", RDF_NAMESPACE);
        writer.writeNamespace("cim", CIM_NAMESPACE);
        writer.writeNamespace("md", MD_NAMESPACE);
        return writer;
    }

    private void exportUsingOriginalCgmesModel(Network network, Properties params, DataSource ds, CgmesModelExtension ext) {
        CgmesUpdate cgmesUpdate = ext.getCgmesUpdate();
        CgmesModel cgmesSource = ext.getCgmesModel();
        CgmesModel cgmes = CgmesModelFactory.copy(cgmesSource);
        String variantId = network.getVariantManager().getWorkingVariantId();
        cgmesUpdate.update(cgmes, variantId);
        // Fill the State Variables data with the Network current state values
        StateVariablesAdder adder = new StateVariablesAdder(cgmes, network);
        adder.addStateVariablesToCgmes();
        cgmes.write(ds);
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }
}
