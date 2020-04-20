/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.exceptions.UncheckedSaxException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.anonymizer.Anonymizer;
import com.powsybl.iidm.anonymizer.SimpleAnonymizer;
import com.powsybl.iidm.export.BusFilter;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.import_.ImportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.extensions.AbstractVersionableNetworkExtensionXmlSerializer;
import javanet.staxutils.IndentingXMLStreamWriter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.powsybl.iidm.xml.IidmXmlConstants.*;
import static com.powsybl.iidm.xml.XMLImporter.SUFFIX_MAPPING;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class NetworkXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkXml.class);

    private static final String EXTENSION_CATEGORY_NAME = "network";
    static final String NETWORK_ROOT_ELEMENT_NAME = "network";
    private static final String EXTENSION_ELEMENT_NAME = "extension";
    private static final String CASE_DATE = "caseDate";
    private static final String FORECAST_DISTANCE = "forecastDistance";
    private static final String SOURCE_FORMAT = "sourceFormat";
    private static final String ID = "id";

    // cache to improve performance
    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);
    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);

    private static final Supplier<ExtensionProviders<ExtensionXmlSerializer>> EXTENSIONS_SUPPLIER = Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionXmlSerializer.class, EXTENSION_CATEGORY_NAME));

    private NetworkXml() {
    }

    private static Set<String> getNetworkExtensions(Network n) {
        Set<String> extensions = new TreeSet<>();
        for (Identifiable<?> identifiable : n.getIdentifiables()) {
            for (Extension<? extends Identifiable<?>> extension : identifiable.getExtensions()) {
                extensions.add(extension.getName());
            }
        }
        return extensions;
    }

    private static void validate(Source xml, List<Source> additionalSchemas) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        int length = IidmXmlVersion.values().length;
        Source[] sources = new Source[additionalSchemas.size() + length];
        int i = 0;
        for (IidmXmlVersion version : IidmXmlVersion.values()) {
            sources[i] = new StreamSource(NetworkXml.class.getResourceAsStream("/xsd/" + version.getXsd()));
            i++;
        }
        for (int j = 0; j < additionalSchemas.size(); j++) {
            sources[j + length] = additionalSchemas.get(j);
        }
        try {
            Schema schema = factory.newSchema(sources);
            Validator validator = schema.newValidator();
            validator.validate(xml);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (SAXException e) {
            throw new UncheckedSaxException(e);
        }
    }

    public static void validate(InputStream is) {
        List<Source> additionalSchemas = new ArrayList<>();
        for (ExtensionXmlSerializer<?, ?> e : EXTENSIONS_SUPPLIER.get().getProviders()) {
            e.getXsdAsStreamList().forEach(xsd -> additionalSchemas.add(new StreamSource(xsd)));
        }
        validate(new StreamSource(is), additionalSchemas);
    }

    public static void validate(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            validate(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeExtensionNamespaces(Network n, ExportOptions options, XMLStreamWriter writer) throws XMLStreamException {
        Set<String> extensionUris = new HashSet<>();
        Set<String> extensionPrefixes = new HashSet<>();
        for (String extensionName : getNetworkExtensions(n)) {
            ExtensionXmlSerializer extensionXmlSerializer = getExtensionXmlSerializer(options, extensionName);
            if (extensionXmlSerializer == null) {
                continue;
            }

            String namespaceUri = getNamespaceUri(extensionXmlSerializer, options);

            if (extensionUris.contains(namespaceUri)) {
                throw new PowsyblException("Extension namespace URI collision");
            } else {
                extensionUris.add(namespaceUri);
            }
            if (extensionPrefixes.contains(extensionXmlSerializer.getNamespacePrefix())) {
                throw new PowsyblException("Extension namespace prefix collision");
            } else {
                extensionPrefixes.add(extensionXmlSerializer.getNamespacePrefix());
            }
            writer.setPrefix(extensionXmlSerializer.getNamespacePrefix(), namespaceUri);
            writer.writeNamespace(extensionXmlSerializer.getNamespacePrefix(), namespaceUri);
        }
    }

    private static void writeExtension(Extension<? extends Identifiable<?>> extension, NetworkXmlWriterContext context) throws XMLStreamException {
        XMLStreamWriter writer = context.getExtensionsWriter();
        ExtensionXmlSerializer extensionXmlSerializer = getExtensionXmlSerializer(context.getOptions(),
                extension.getName());

        if (extensionXmlSerializer == null) {
            if (context.getOptions().isThrowExceptionIfExtensionNotFound()) {
                throw new PowsyblException("XmlSerializer for" + extension.getName() + "not found");
            }
            return;
        }

        String namespaceUri = getNamespaceUri(extensionXmlSerializer, context.getOptions(), context.getVersion());

        if (extensionXmlSerializer.hasSubElements()) {
            writer.writeStartElement(namespaceUri, extension.getName());
        } else {
            writer.writeEmptyElement(namespaceUri, extension.getName());
        }
        context.getExtensionVersion(extension.getName()).ifPresent(extensionXmlSerializer::checkExtensionVersionSupported);
        extensionXmlSerializer.write(extension, context);
        if (extensionXmlSerializer.hasSubElements()) {
            writer.writeEndElement();
        }
    }

    private static ExtensionXmlSerializer getExtensionXmlSerializer(ExportOptions options, String extensionName) {
        ExtensionXmlSerializer extensionXmlSerializer = options.isThrowExceptionIfExtensionNotFound()
                ? EXTENSIONS_SUPPLIER.get().findProviderOrThrowException(extensionName)
                : EXTENSIONS_SUPPLIER.get().findProvider(extensionName);
        if (extensionXmlSerializer == null) {
            LOGGER.warn("No Extension XML Serializer for {}", extensionName);
        }
        return extensionXmlSerializer;
    }

    private static String getNamespaceUri(ExtensionXmlSerializer extensionXmlSerializer, ExportOptions options) {
        IidmXmlVersion networkVersion = options.getVersion() == null ? IidmXmlConstants.CURRENT_IIDM_XML_VERSION : IidmXmlVersion.of(options.getVersion(), ".");
        return getNamespaceUri(extensionXmlSerializer, options, networkVersion);
    }

    private static String getNamespaceUri(ExtensionXmlSerializer extensionXmlSerializer, ExportOptions options, IidmXmlVersion networkVersion) {
        if (extensionXmlSerializer instanceof AbstractVersionableNetworkExtensionXmlSerializer) {
            AbstractVersionableNetworkExtensionXmlSerializer networkExtensionXmlSerializer = (AbstractVersionableNetworkExtensionXmlSerializer) extensionXmlSerializer;
            return options.getExtensionVersion(networkExtensionXmlSerializer.getExtensionName())
                    .map(extensionVersion -> {
                        networkExtensionXmlSerializer.checkWritingCompatibility(extensionVersion, networkVersion);
                        return networkExtensionXmlSerializer.getNamespaceUri(extensionVersion);
                    })
                    .orElseGet(() -> networkExtensionXmlSerializer.getNamespaceUri(networkExtensionXmlSerializer.getVersion(networkVersion)));

        }
        return options.getExtensionVersion(extensionXmlSerializer.getExtensionName())
                .map(extensionXmlSerializer::getNamespaceUri)
                .orElseGet(extensionXmlSerializer::getNamespaceUri);
    }

    private static Set<String> getExtensionsName(Collection<? extends Extension<? extends Identifiable<?>>> extensions) {
        Set<String> extensionsSet = new HashSet<>();
        for (Extension<? extends Identifiable<?>> extension : extensions) {
            extensionsSet.add(extension.getName());
        }
        return extensionsSet;
    }

    private static void writeExtensions(Network n, NetworkXmlWriterContext context, ExportOptions options) throws XMLStreamException {

        for (Identifiable<?> identifiable : n.getIdentifiables()) {
            Collection<? extends Extension<? extends Identifiable<?>>> extensions = identifiable.getExtensions();
            if (!context.isExportedEquipment(identifiable) || extensions.isEmpty() || !options.hasAtLeastOneExtension(getExtensionsName(extensions))) {
                continue;
            }
            context.getExtensionsWriter().writeStartElement(context.getVersion().getNamespaceURI(), EXTENSION_ELEMENT_NAME);
            context.getExtensionsWriter().writeAttribute(ID, context.getAnonymizer().anonymizeString(identifiable.getId()));
            for (Extension<? extends Identifiable<?>> extension : extensions) {
                if (options.withExtension(extension.getName())) {
                    writeExtension(extension, context);
                }
            }
            context.getExtensionsWriter().writeEndElement();
        }
    }

    private static void writeMainAttributes(Network n, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(ID, n.getId());
        writer.writeAttribute(CASE_DATE, n.getCaseDate().toString());
        writer.writeAttribute(FORECAST_DISTANCE, Integer.toString(n.getForecastDistance()));
        writer.writeAttribute(SOURCE_FORMAT, n.getSourceFormat());
    }

    private static XMLStreamWriter initializeWriter(Network n, OutputStream os, ExportOptions options) throws XMLStreamException {
        IidmXmlVersion version = options.getVersion() == null ? CURRENT_IIDM_XML_VERSION : IidmXmlVersion.of(options.getVersion(), ".");
        XMLStreamWriter writer;
        writer = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(os, StandardCharsets.UTF_8.toString());
        if (options.isIndent()) {
            IndentingXMLStreamWriter indentingWriter = new IndentingXMLStreamWriter(writer);
            indentingWriter.setIndent(INDENT);
            writer = indentingWriter;
        }
        writer.writeStartDocument(StandardCharsets.UTF_8.toString(), "1.0");
        writer.setPrefix(IIDM_PREFIX, version.getNamespaceURI());
        writer.writeStartElement(version.getNamespaceURI(), NETWORK_ROOT_ELEMENT_NAME);
        writer.writeNamespace(IIDM_PREFIX, version.getNamespaceURI());
        if (!options.withNoExtension()) {
            writeExtensionNamespaces(n, options, writer);
        }
        writeMainAttributes(n, writer);
        return writer;
    }

    private static NetworkXmlWriterContext writeBaseNetwork(Network n, XMLStreamWriter writer, ExportOptions options) throws XMLStreamException {
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

    public static Anonymizer write(Network n, ExportOptions options, OutputStream os) {
        try {
            NetworkXmlWriterContext context = writeBaseNetwork(n, initializeWriter(n, os, options), options);
            // write extensions
            writeExtensions(n, context, options);
            context.getWriter().writeEndElement();
            context.getWriter().writeEndDocument();
            return context.getAnonymizer();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    public static Anonymizer write(Network n, OutputStream os) {
        return write(n, new ExportOptions(), os);
    }

    public static Anonymizer write(Network n, ExportOptions options, Path xmlFile) {
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(xmlFile))) {
            return write(n, options, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Anonymizer write(Network n, Path xmlFile) {
        return write(n, new ExportOptions(), xmlFile);
    }

    public static Anonymizer write(Network network, ExportOptions options, DataSource dataSource, String dataSourceExt) throws IOException {
        try (OutputStream osb = dataSource.newOutputStream("", dataSourceExt, false);
             BufferedOutputStream bosb = new BufferedOutputStream(osb)) {

            Anonymizer anonymizer = write(network, options, bosb);
            if (options.isAnonymized()) {
                try (BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(dataSource.newOutputStream("_mapping", "csv", false), StandardCharsets.UTF_8))) {
                    anonymizer.write(writer2);
                }
            }
            return anonymizer;
        }
    }

    public static Anonymizer writeAndValidate(Network n, Path xmlFile) {
        return writeAndValidate(n, new ExportOptions(), xmlFile);
    }

    public static Anonymizer writeAndValidate(Network n, ExportOptions options, Path xmlFile) {
        Anonymizer anonymizer = write(n, options, xmlFile);
        validate(xmlFile);
        return anonymizer;
    }

    public static Network read(InputStream is) {
        return read(is, new ImportOptions(), null);
    }

    public static Network read(InputStream is, ImportOptions config, Anonymizer anonymizer) {
        return read(is, config, anonymizer, NetworkFactory.findDefault());
    }

    public static Network read(InputStream is, ImportOptions config, Anonymizer anonymizer, NetworkFactory networkFactory) {
        try {
            XMLStreamReader reader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
            int state = reader.next();
            while (state == XMLStreamReader.COMMENT) {
                state = reader.next();
            }

            IidmXmlVersion version = IidmXmlVersion.fromNamespaceURI(reader.getNamespaceURI());

            String id = reader.getAttributeValue(null, ID);
            DateTime date = DateTime.parse(reader.getAttributeValue(null, CASE_DATE));
            int forecastDistance = XmlUtil.readOptionalIntegerAttribute(reader, FORECAST_DISTANCE, 0);
            String sourceFormat = reader.getAttributeValue(null, SOURCE_FORMAT);

            Network network = networkFactory.createNetwork(id, sourceFormat);
            network.setCaseDate(date);
            network.setForecastDistance(forecastDistance);

            NetworkXmlReaderContext context = new NetworkXmlReaderContext(anonymizer, reader, config, version);

            if (!config.withNoExtension()) {
                context.buildExtensionNamespaceUriList(EXTENSIONS_SUPPLIER.get().getProviders().stream());
            }

            Set<String> extensionNamesNotFound = new TreeSet<>();

            XmlUtil.readUntilEndElement(NETWORK_ROOT_ELEMENT_NAME, reader, () -> {
                switch (reader.getLocalName()) {
                    case PropertiesXml.PROPERTY:
                        PropertiesXml.read(network, context);
                        break;

                    case SubstationXml.ROOT_ELEMENT_NAME:
                        SubstationXml.INSTANCE.read(network, context);
                        break;

                    case LineXml.ROOT_ELEMENT_NAME:
                        LineXml.INSTANCE.read(network, context);
                        break;

                    case TieLineXml.ROOT_ELEMENT_NAME:
                        TieLineXml.INSTANCE.read(network, context);
                        break;

                    case HvdcLineXml.ROOT_ELEMENT_NAME:
                        HvdcLineXml.INSTANCE.read(network, context);
                        break;

                    case EXTENSION_ELEMENT_NAME:
                        String id2 = context.getAnonymizer().deanonymizeString(reader.getAttributeValue(null, "id"));
                        Identifiable identifiable = network.getIdentifiable(id2);
                        if (identifiable == null) {
                            throw new PowsyblException("Identifiable " + id2 + " not found");
                        }
                        readExtensions(identifiable, context, extensionNamesNotFound);
                        break;

                    default:
                        throw new AssertionError();
                }
            });

            checkExtensionsNotFound(context, extensionNamesNotFound);

            context.getEndTasks().forEach(Runnable::run);
            return network;
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void checkExtensionsNotFound(NetworkXmlReaderContext context, Set<String> extensionNamesNotFound) {
        if (!extensionNamesNotFound.isEmpty()) {
            if (context.getOptions().isThrowExceptionIfExtensionNotFound()) {
                throw new PowsyblException("Extensions " + extensionNamesNotFound + " " +
                        "not found !");
            } else {
                LOGGER.error("Extensions {} not found", extensionNamesNotFound);
            }
        }
    }

    public static Network read(Path xmlFile) {
        return read(xmlFile, new ImportOptions());
    }

    public static Network read(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, ImportOptions options, String dataSourceExt) throws IOException {
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
        return network;
    }

    public static Network read(Path xmlFile, ImportOptions options) {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            return read(is, options, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Network validateAndRead(Path xmlFile, ImportOptions options) {
        validate(xmlFile);
        return read(xmlFile, options);
    }

    public static Network validateAndRead(Path xmlFile) {
        return validateAndRead(xmlFile, new ImportOptions());
    }

    private static void readExtensions(Identifiable identifiable, NetworkXmlReaderContext context,
                                      Set<String> extensionNamesNotFound) throws XMLStreamException {

        XmlUtil.readUntilEndElementWithDepth(EXTENSION_ELEMENT_NAME, context.getReader(), elementDepth -> {
            // extensions root elements are nested directly in 'extension' element, so there is no need
            // to check for an extension to exist if depth is greater than zero. Furthermore in case of
            // missing extension serializer, we must not check for an extension in sub elements.
            if (elementDepth == 0) {
                String extensionName = context.getReader().getLocalName();
                if (!context.getOptions().withExtension(extensionName)) {
                    return;
                }

                ExtensionXmlSerializer extensionXmlSerializer = EXTENSIONS_SUPPLIER.get().findProvider(extensionName);
                if (extensionXmlSerializer != null) {
                    Extension<? extends Identifiable<?>> extension = extensionXmlSerializer.read(identifiable, context);
                    identifiable.addExtension(extensionXmlSerializer.getExtensionClass(), extension);
                } else {
                    extensionNamesNotFound.add(extensionName);
                }
            }
        });
    }

    public static void update(Network network, InputStream is) {
        try {
            XMLStreamReader reader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
            reader.next();

            final VoltageLevel[] vl = new VoltageLevel[1];

            XmlUtil.readUntilEndElement(NETWORK_ROOT_ELEMENT_NAME, reader, () -> {

                switch (reader.getLocalName()) {
                    case VoltageLevelXml.ROOT_ELEMENT_NAME:
                        updateVoltageLevel(reader, network, vl);
                        break;

                    case BusXml.ROOT_ELEMENT_NAME:
                        updateBus(reader, vl);
                        break;

                    case GeneratorXml.ROOT_ELEMENT_NAME:
                    case BatteryXml.ROOT_ELEMENT_NAME:
                    case LoadXml.ROOT_ELEMENT_NAME:
                    case ShuntXml.ROOT_ELEMENT_NAME:
                    case DanglingLineXml.ROOT_ELEMENT_NAME:
                    case LccConverterStationXml.ROOT_ELEMENT_NAME:
                    case VscConverterStationXml.ROOT_ELEMENT_NAME:
                        updateInjection(reader, network);
                        break;

                    case LineXml.ROOT_ELEMENT_NAME:
                    case TwoWindingsTransformerXml.ROOT_ELEMENT_NAME:
                        updateBranch(reader, network);
                        break;

                    case HvdcLineXml.ROOT_ELEMENT_NAME:
                        // Nothing to do
                        break;

                    case ThreeWindingsTransformerXml.ROOT_ELEMENT_NAME:
                        throw new AssertionError();

                    default:
                        throw new AssertionError("Unexpected element: " + reader.getLocalName());
                }
            });
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    public static void update(Network network, Path xmlFile) {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            update(network, is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void updateVoltageLevel(XMLStreamReader reader, Network network, VoltageLevel[] vl) {
        String id = reader.getAttributeValue(null, "id");
        vl[0] = network.getVoltageLevel(id);
        if (vl[0] == null) {
            throw new PowsyblException("Voltage level '" + id + "' not found");
        }
    }

    private static void updateBus(XMLStreamReader reader, VoltageLevel[] vl) {
        String id = reader.getAttributeValue(null, "id");
        double v = XmlUtil.readDoubleAttribute(reader, "v");
        double angle = XmlUtil.readDoubleAttribute(reader, "angle");
        Bus b = vl[0].getBusBreakerView().getBus(id);
        if (b == null) {
            b = vl[0].getBusView().getBus(id);
        }
        b.setV(v > 0 ? v : Double.NaN).setAngle(angle);
    }

    private static void updateInjection(XMLStreamReader reader, Network network) {
        String id = reader.getAttributeValue(null, "id");
        double p = XmlUtil.readOptionalDoubleAttribute(reader, "p");
        double q = XmlUtil.readOptionalDoubleAttribute(reader, "q");
        Injection inj = (Injection) network.getIdentifiable(id);
        inj.getTerminal().setP(p).setQ(q);
    }

    private static void updateBranch(XMLStreamReader reader, Network network) {
        String id = reader.getAttributeValue(null, "id");
        double p1 = XmlUtil.readOptionalDoubleAttribute(reader, "p1");
        double q1 = XmlUtil.readOptionalDoubleAttribute(reader, "q1");
        double p2 = XmlUtil.readOptionalDoubleAttribute(reader, "p2");
        double q2 = XmlUtil.readOptionalDoubleAttribute(reader, "q2");
        Branch branch = (Branch) network.getIdentifiable(id);
        branch.getTerminal1().setP(p1).setQ(q1);
        branch.getTerminal2().setP(p2).setQ(q2);
    }

    public static byte[] gzip(Network network) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzos = new GZIPOutputStream(bos)) {
            write(network, gzos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bos.toByteArray();
    }

    public static Network gunzip(byte[] networkXmlGz) {
        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(networkXmlGz))) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Deep copy of the network using XML converter.
     *
     * @param network the network to copy
     * @return the copy of the network
     */
    public static Network copy(Network network) {
        return copy(network, ForkJoinPool.commonPool());
    }

    public static Network copy(Network network, ExecutorService executor) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(executor);
        PipedOutputStream pos = new PipedOutputStream();
        try (InputStream is = new PipedInputStream(pos)) {
            executor.execute(() -> {
                try {
                    write(network, pos);
                } catch (Exception t) {
                    LOGGER.error(t.toString(), t);
                } finally {
                    try {
                        pos.close();
                    } catch (IOException e) {
                        LOGGER.error(e.toString(), e);
                    }
                }
            });
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
