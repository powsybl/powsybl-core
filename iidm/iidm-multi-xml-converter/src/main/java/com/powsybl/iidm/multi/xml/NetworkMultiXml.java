/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.multi.xml;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.anonymizer.Anonymizer;
import com.powsybl.iidm.anonymizer.SimpleAnonymizer;
import com.powsybl.iidm.export.BusFilter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.*;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static com.powsybl.iidm.xml.IidmXmlConstants.*;


/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class NetworkMultiXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkMultiXml.class);

    private static final String EXTENSION_ELEMENT_NAME = "extension";
    private static final String NETWORK = "network";
    private static final String SUFFIX_MAPPING = "_mapping";

    // cache XMLOutputFactory to improve performance
    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private static final Supplier<ExtensionProviders<ExtensionXmlSerializer>> EXTENSIONS_SUPPLIER = Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionXmlSerializer.class, NETWORK));

    private NetworkMultiXml() {
    }

    public static Network read(Path xmlFile, MultiXMLImportOptions options) throws IOException {
        DataSource dataSource = getDataSourceFromPath(xmlFile);
        String ext = getFileExtensionFromPath(xmlFile);
        return read(dataSource, NetworkFactory.findDefault(), options, ext);
    }

    public static Network read(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, MultiXMLImportOptions options, String dataSourceExt) throws IOException {
        Objects.requireNonNull(dataSource);
        Network network;
        Anonymizer anonymizer = null;

        if (dataSource.exists(SUFFIX_MAPPING, "csv")) {
            anonymizer = new SimpleAnonymizer();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(SUFFIX_MAPPING, "csv"), StandardCharsets.UTF_8))) {
                anonymizer.read(reader);
            }
        }
        //Read the base file with the extensions declared in the extensions list
        try (InputStream isb = dataSource.newInputStream(null, dataSourceExt)) {
            network = NetworkXml.read(isb, options, anonymizer, networkFactory);
        }
        if (!options.withNoExtension()) {
            switch (options.getMode()) {
                case EXTENSIONS_IN_ONE_SEPARATED_FILE:
                    // in this case we have to read all extensions from one  file
                    try (InputStream ise = dataSource.newInputStream("-ext", dataSourceExt)) {
                        readExtensions(network, ise, anonymizer, options);
                    } catch (IOException e) {
                        LOGGER.warn(String.format("the extensions file wasn't found while importing, please ensure that the file name respect the naming convention baseFileName-ext.%s", dataSourceExt));
                    }
                    break;
                case ONE_SEPARATED_FILE_PER_EXTENSION_TYPE:
                    String ext = dataSourceExt.isEmpty() ? "" : "." + dataSourceExt;
                    // here we'll read all extensions declared in the extensions set
                    readExtensions(network, dataSource, anonymizer, options, ext);
                    break;
                default:
                    throw new PowsyblException("Unexpected mode for multi-files IIDM-XML import: " + options.getMode());
            }
        }
        return network;
    }

    public static Network validateAndRead(Path xmlFile) {
        try {
            return validateAndRead(xmlFile, new MultiXMLImportOptions());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Network validateAndRead(Path xmlFile, MultiXMLImportOptions options) throws IOException {
        validate(xmlFile, options.getMode());
        return read(xmlFile, options);
    }

    public static Anonymizer write(Network n, MultiXMLExportOptions options, Path xmlFile) {
        try {
            return write(n, options, getDataSourceFromPath(xmlFile), getFileExtensionFromPath(xmlFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Anonymizer write(Network network, MultiXMLExportOptions options, DataSource dataSource, String dataSourceExt) throws IOException {
        try (OutputStream osb = dataSource.newOutputStream("", dataSourceExt, false);
             BufferedOutputStream bosb = new BufferedOutputStream(osb)) {
            NetworkXmlWriterContext context = writeBaseNetwork(network, bosb, options);
            XmlUtil.writeEndElement(context.getWriter());
            // write extensions
            if (!options.withNoExtension() && !NetworkXml.getNetworkExtensions(network).isEmpty()) {
                if (options.getMode() == IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE) {
                    try (OutputStream ose = dataSource.newOutputStream("-ext", dataSourceExt, false);
                         BufferedOutputStream bose = new BufferedOutputStream(ose)) {

                        final XMLStreamWriter extensionsWriter = NetworkXml.initializeWriter(network, bose, options);
                        context.setExtensionsWriter(extensionsWriter);
                        NetworkXml.writeExtensions(network, context, options);
                        XmlUtil.writeEndElement(extensionsWriter);
                    }
                } else if (options.getMode() == IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE) {
                    writeExtensionsInMultipleFile(network, context, dataSource, options, dataSourceExt);
                } else {
                    throw new PowsyblException("Unexpected mode for multi-files IIDM-XML export: " + options.getMode());
                }
            }

            if (options.isAnonymized()) {
                try (BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(dataSource.newOutputStream(SUFFIX_MAPPING, "csv", false), StandardCharsets.UTF_8))) {
                    context.getAnonymizer().write(writer2);
                }
            }
            return context.getAnonymizer();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    public static Anonymizer writeAndValidate(Network n, MultiXMLExportOptions options, Path xmlFile) throws IOException {
        Anonymizer anonymizer = write(n, options, xmlFile);
        validate(xmlFile, options.getMode());
        return anonymizer;
    }

    private static DataSource getDataSourceFromPath(Path xmlFile) {
        String fileBaseName = FilenameUtils.getBaseName(xmlFile.getFileName().toString());
        return new FileDataSource(xmlFile.getParent(), fileBaseName);
    }

    private static String getFileExtensionFromPath(Path xmlFile) {
        return FilenameUtils.getExtension(xmlFile.getFileName().toString());
    }

    private static Map<String, Set<String>> getIdentifiablesPerExtensionType(Network n) {
        Map<String, Set<String>> extensionsPerType = new HashMap<>();
        for (Identifiable<?> identifiable : n.getIdentifiables()) {
            for (Extension<? extends Identifiable<?>> extension : identifiable.getExtensions()) {
                extensionsPerType.computeIfAbsent(extension.getName(), key -> new HashSet<>()).add(identifiable.getId());
            }
        }
        return extensionsPerType;
    }

    private static XMLStreamWriter initializeBaseNetworkWriter(Network n, OutputStream os, MultiXMLExportOptions options) throws XMLStreamException {
        IidmXmlVersion version = options.getVersion() == null ? CURRENT_IIDM_XML_VERSION : IidmXmlVersion.of(options.getVersion(), ".");
        XMLStreamWriter writer = XmlUtil.writeStartAttributes(os, IIDM_PREFIX, version.getNamespaceURI(), NETWORK, INDENT, options.isIndent());
        NetworkXml.writeMainAttributes(n, writer);
        return writer;
    }

    private static XMLStreamWriter initializeExtensionFileWriter(Network n, OutputStream os, MultiXMLExportOptions options, String extensionName) throws XMLStreamException {
        IidmXmlVersion version = options.getVersion() == null ? CURRENT_IIDM_XML_VERSION : IidmXmlVersion.of(options.getVersion(), ".");
        XMLStreamWriter writer = XmlUtil.writeStartAttributes(os, IIDM_PREFIX, version.getNamespaceURI(), NETWORK, INDENT, options.isIndent());
        writeExtensionNamespace(options, writer, extensionName);
        NetworkXml.writeMainAttributes(n, writer);
        return writer;
    }

    // To read extensions from multiple extension files
    private static void readExtensions(Network network, ReadOnlyDataSource dataSource, Anonymizer anonymizer, MultiXMLImportOptions options, String ext) throws IOException {
        options.getExtensions().ifPresent(extensions -> {
            for (String extension : extensions) {
                try (InputStream ise = dataSource.newInputStream(dataSource.getBaseName() + "-" + extension + ext)) {
                    readExtensions(network, ise, anonymizer, options);
                } catch (IOException e) {
                    LOGGER.warn(String.format("the %s extension file is not found despite it was declared in the extensions list", extension));
                }
            }
        });

        if (!options.getExtensions().isPresent()) {
            Set<String> listNames = dataSource.listNames(".*" + ext);
            listNames.remove(dataSource.getBaseName() + ext);
            for (String fileName : listNames) {
                try (InputStream ise = dataSource.newInputStream(fileName)) {
                    readExtensions(network, ise, anonymizer, options);
                } catch (IOException e) {
                    LOGGER.warn(String.format("the %s file is not found ", fileName));
                }
            }
        }
    }

    // To read extensions from an extensions file
    private static Network readExtensions(Network network, InputStream ise, Anonymizer anonymizer, MultiXMLImportOptions options) {
        try {
            XMLStreamReader reader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(ise);
            int state = reader.next();
            while (state == XMLStreamReader.COMMENT) {
                state = reader.next();
            }
            String id = reader.getAttributeValue(null, "id");
            DateTime date = DateTime.parse(reader.getAttributeValue(null, "caseDate"));

            //verify that the extensions file matches with the same network
            if (!network.getId().equals(id) || !network.getCaseDate().equals(date)) {
                throw new PowsyblException("Extension file do not match with the base file !");
            }

            NetworkXmlReaderContext context = new NetworkXmlReaderContext(anonymizer, reader, options, CURRENT_IIDM_XML_VERSION);
            context.buildExtensionNamespaceUriList(EXTENSIONS_SUPPLIER.get().getProviders().stream());
            Set<String> extensionNamesNotFound = new TreeSet<>();

            XmlUtil.readUntilEndElement(NETWORK, reader, () -> {
                if (reader.getLocalName().equals(EXTENSION_ELEMENT_NAME)) {
                    String id2 = context.getAnonymizer().deanonymizeString(reader.getAttributeValue(null, "id"));
                    Identifiable identifiable = network.getIdentifiable(id2);
                    if (identifiable == null) {
                        throw new PowsyblException("Identifiable " + id2 + " not found");
                    }
                    NetworkXml.readExtensions(identifiable, context, extensionNamesNotFound);
                } else {
                    throw new PowsyblException("Unexpected element: " + reader.getLocalName());
                }
            });

            NetworkXml.checkExtensionsNotFound(context, extensionNamesNotFound);

            return network;
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void validate(Path xmlFile, IidmImportExportMode mode) throws IOException {
        DataSource dataSource = getDataSourceFromPath(xmlFile);
        String ext = getFileExtensionFromPath(xmlFile);

        if (mode == IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE) {
            try (InputStream ise = dataSource.newInputStream(dataSource.getBaseName() + ext)) {
                NetworkXml.validateWithExtensions(ise);
            }
            try (InputStream ise = dataSource.newInputStream(dataSource.getBaseName() + "-ext." + ext)) {
                NetworkXml.validateWithExtensions(ise);
            }
        } else {
            Set<String> listNames = dataSource.listNames(".*\\." + ext);
            for (String fileName : listNames) {
                try (InputStream ise = dataSource.newInputStream(fileName)) {
                    NetworkXml.validateWithExtensions(ise);
                }
            }
        }
    }

    private static NetworkXmlWriterContext writeBaseNetwork(Network n, OutputStream os, MultiXMLExportOptions options) throws XMLStreamException {
        // create the  writer of the base file
        XMLStreamWriter writer = initializeBaseNetworkWriter(n, os, options);
        BusFilter filter = BusFilter.create(n, options);
        Anonymizer anonymizer = options.isAnonymized() ? new SimpleAnonymizer() : null;
        IidmXmlVersion version = options.getVersion() == null ? IidmXmlConstants.CURRENT_IIDM_XML_VERSION : IidmXmlVersion.of(options.getVersion(), ".");
        NetworkXmlWriterContext context = new NetworkXmlWriterContext(anonymizer, writer, options, filter, version);
        // Consider the network has been exported so its extensions will be written also
        context.addExportedEquipment(n);

        PropertiesXml.write(n, context);

        for (Substation s : n.getSubstations()) {
            SubstationXml.INSTANCE.write(s, null, context);
        }
        for (Line l : n.getLines()) {
            if (!filter.test(l)) {
                continue;
            }
            if (l.isTieLine()) {
                TieLineXml.INSTANCE.write((TieLine) l, n, context);
            } else {
                LineXml.INSTANCE.write(l, n, context);
            }
        }
        for (HvdcLine l : n.getHvdcLines()) {
            if (!filter.test(l.getConverterStation1()) || !filter.test(l.getConverterStation2())) {
                continue;
            }
            HvdcLineXml.INSTANCE.write(l, n, context);
        }
        return context;
    }

    private static void writeExtensionsInMultipleFile(Network n, NetworkXmlWriterContext context, DataSource dataSource, MultiXMLExportOptions options, String dataSourceExt) throws XMLStreamException, IOException {
        //here we right one extension type  per file
        Map<String, Set<String>> m = getIdentifiablesPerExtensionType(n);
        String ext = dataSourceExt.isEmpty() ? "" : "." + dataSourceExt;

        for (Map.Entry<String, Set<String>> entry : m.entrySet()) {
            String name = entry.getKey();
            Set<String> ids = entry.getValue();
            if (options.withExtension(name)) {
                try (OutputStream os = dataSource.newOutputStream(dataSource.getBaseName() + "-" + name + ext, false);
                     BufferedOutputStream bos = new BufferedOutputStream(os)) {
                    XMLStreamWriter writer = initializeExtensionFileWriter(n, bos, options, name);
                    for (String id : ids) {
                        writer.writeStartElement(context.getVersion().getNamespaceURI(), EXTENSION_ELEMENT_NAME);
                        writer.writeAttribute("id", context.getAnonymizer().anonymizeString(id));
                        context.setExtensionsWriter(writer);
                        NetworkXml.writeExtension(n.getIdentifiable(id).getExtensionByName(name), context);
                        writer.writeEndElement();
                    }
                    XmlUtil.writeEndElement(writer);
                }
            }
        }
    }

    private static void writeExtensionNamespace(MultiXMLExportOptions options, XMLStreamWriter writer, String extensionName) throws XMLStreamException {
        ExtensionXmlSerializer extensionXmlSerializer = NetworkXml.getExtensionXmlSerializer(options, extensionName);
        if (extensionXmlSerializer == null) {
            return;
        }

        String namespaceUri = NetworkXml.getNamespaceUri(extensionXmlSerializer, options);

        writer.setPrefix(extensionXmlSerializer.getNamespacePrefix(), namespaceUri);
        writer.writeNamespace(extensionXmlSerializer.getNamespacePrefix(), namespaceUri);
    }
}
