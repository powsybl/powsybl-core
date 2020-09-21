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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
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
import com.powsybl.cgmes.conversion.update.StateVariablesExport;
import com.powsybl.cgmes.conversion.update.SteadyStateHypothesisExport;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Network;

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
            exportUsingOriginalCgmesModel(network, ds, ext);
        }
    }

    public static final String CIM_VERSION = "CIM_version";

    public static final String ENTSOE_NAMESPACE = "http://entsoe.eu/CIM/SchemaExtension/3/1#";
    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String CIM_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";

    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);
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
        export(network, ds, "SV", StateVariablesExport::write);
    }

    private void exportSteadyStateHypothesis(Network network, DataSource ds) {
        export(network, ds, "SSH", SteadyStateHypothesisExport::write);
    }

    private void export(Network network, DataSource ds, String profileSuffix, BiConsumer<Network, XMLStreamWriter> exporter) {
        String baseName = network.getProperty("baseName");
        String filename = baseName + "_" + profileSuffix + ".xml";
        try (OutputStream os = ds.newOutputStream(filename, false); BufferedOutputStream bos = new BufferedOutputStream(os)) {
            export(network, bos, exporter);
        } catch (IOException x) {
            throw new PowsyblException("Exporting to CGMES using only Network");
        }
    }

    private void export(Network network, OutputStream os, BiConsumer<Network, XMLStreamWriter> exporter) {
        try {
            XMLStreamWriter writer = initializeWriter(os);
            exporter.accept(network, writer);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    public static void writeRdfRoot(XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix("entsoe", ENTSOE_NAMESPACE);
        writer.setPrefix("rdf", RDF_NAMESPACE);
        writer.setPrefix("cim", CIM_NAMESPACE);
        writer.setPrefix("md", MD_NAMESPACE);
        writer.writeStartElement(RDF_NAMESPACE, "RDF");
        writer.writeNamespace("entsoe", ENTSOE_NAMESPACE);
        writer.writeNamespace("rdf", RDF_NAMESPACE);
        writer.writeNamespace("cim", CIM_NAMESPACE);
        writer.writeNamespace("md", MD_NAMESPACE);
    }

    // Avoid trailing zeros
    public static String format(double value) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        return new DecimalFormat("0.##############", otherSymbols).format(Double.isNaN(value) ? 0.0 : value);
    }

    public static String format(int value) {
        return String.valueOf(value);
    }

    public static String getUniqueId() {
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
        return writer;
    }

    private void exportUsingOriginalCgmesModel(Network network, DataSource ds, CgmesModelExtension ext) {
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
