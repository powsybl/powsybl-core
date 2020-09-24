/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Supplier;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.auto.service.AutoService;
import com.google.common.base.Suppliers;
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.conversion.update.StateVariablesAdder;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Network;

import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    @Override
    public void export(Network network, Properties params, DataSource ds) {

        // Right now the network must contain the original CgmesModel
        // In the future it should be possible to export to CGMES
        // directly from an IIDM Network,
        // without the need for the original CgmesModel
        CgmesModelExtension ext = network.getExtension(CgmesModelExtension.class);
        if (ext == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
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

    public static final String ENTSOE_NAMESPACE = "http://entsoe.eu/CIM/SchemaExtension/3/1#";
    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String CIM_NAMESPACE = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    public static final String MD_NAMESPACE = "http://iec.ch/TC57/61970-552/ModelDescription/1#";
    public static final String SV_PROFILE = "http://entsoe.eu/CIM/StateVariables/4/1";

    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);
    private static final boolean INDENT = true;

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

    // Avoid trailing zeros and format always using US locale

    private static final DecimalFormatSymbols DOUBLE_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##############", DOUBLE_FORMAT_SYMBOLS);

    public static String format(double value) {
        return DOUBLE_FORMAT.format(Double.isNaN(value) ? 0.0 : value);
    }

    public static String format(int value) {
        return String.valueOf(value);
    }

    public static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static XMLStreamWriter initializeWriter(OutputStream os) throws XMLStreamException {
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

}
