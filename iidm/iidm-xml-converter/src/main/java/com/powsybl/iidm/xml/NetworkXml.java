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
import com.powsybl.commons.exceptions.UncheckedSaxException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionSerializerProvider;
import com.powsybl.commons.extensions.ExtensionSerializerProviders;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.iidm.network.*;
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
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class NetworkXml implements XmlConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkXml.class);

    static final String NETWORK_ROOT_ELEMENT_NAME = "network";
    private static final String EXTENSION_ELEMENT_NAME = "extension";
    private static final String IIDM_XSD = "iidm.xsd";

    // cache XMLOutputFactory to improve performance
    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private static final Supplier<ExtensionSerializerProvider<ExtensionXmlSerializer>> EXTENSIONS_SUPPLIER
        = Suppliers.memoize(() -> ExtensionSerializerProviders.createProvider(ExtensionXmlSerializer.class, "network"));

    private NetworkXml() {
    }

    private static XMLStreamWriter createXmlStreamWriter(XMLExportOptions options, OutputStream os) throws XMLStreamException {
        XMLStreamWriter writer = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(os, StandardCharsets.UTF_8.toString());
        if (options.isIndent()) {
            IndentingXMLStreamWriter indentingWriter = new IndentingXMLStreamWriter(writer);
            indentingWriter.setIndent(INDENT);
            writer = indentingWriter;
        }
        return writer;
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
        Source[] sources = new Source[additionalSchemas.size() + 1];
        sources[0] = new StreamSource(NetworkXml.class.getResourceAsStream("/xsd/" + IIDM_XSD));
        for (int i = 0; i < additionalSchemas.size(); i++) {
            sources[i + 1] = additionalSchemas.get(i);
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

    static void validate(InputStream is) {
        validate(new StreamSource(is), Collections.emptyList());
    }

    static void validate(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            validate(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void validateWithExtensions(InputStream is) {
        List<Source> additionalSchemas = EXTENSIONS_SUPPLIER.get().getSerializers().stream()
            .map(e -> new StreamSource(e.getXsdAsStream()))
            .collect(Collectors.toList());
        validate(new StreamSource(is), additionalSchemas);
    }

    static void validateWithExtensions(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            validateWithExtensions(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeExtensionNamespaces(Network n, XMLStreamWriter writer) throws XMLStreamException {
        Set<String> extensionUris = new HashSet<>();
        Set<String> extensionPrefixes = new HashSet<>();
        for (String extensionName : getNetworkExtensions(n)) {
            ExtensionXmlSerializer extensionXmlSerializer = EXTENSIONS_SUPPLIER.get().findSerializerOrThrowException(extensionName);
            if (extensionUris.contains(extensionXmlSerializer.getNamespaceUri())) {
                throw new PowsyblException("Extension namespace URI collision");
            } else {
                extensionUris.add(extensionXmlSerializer.getNamespaceUri());
            }
            if (extensionPrefixes.contains(extensionXmlSerializer.getNamespacePrefix())) {
                throw new PowsyblException("Extension namespace prefix collision");
            } else {
                extensionPrefixes.add(extensionXmlSerializer.getNamespacePrefix());
            }
            writer.setPrefix(extensionXmlSerializer.getNamespacePrefix(), extensionXmlSerializer.getNamespaceUri());
            writer.writeNamespace(extensionXmlSerializer.getNamespacePrefix(), extensionXmlSerializer.getNamespaceUri());
        }
    }

    private static void writeExtensions(Network n, XmlWriterContext context) throws XMLStreamException {
        for (Identifiable<?> identifiable : n.getIdentifiables()) {
            Collection<? extends Extension<? extends Identifiable<?>>> extensions = identifiable.getExtensions();
            if (!extensions.isEmpty()) {
                context.getWriter().writeStartElement(IIDM_URI, EXTENSION_ELEMENT_NAME);
                context.getWriter().writeAttribute("id", context.getAnonymizer().anonymizeString(identifiable.getId()));

                for (Extension<? extends Identifiable<?>> extension : identifiable.getExtensions()) {
                    ExtensionXmlSerializer extensionXmlSerializer = EXTENSIONS_SUPPLIER.get().findSerializerOrThrowException(extension.getName());
                    if (extensionXmlSerializer.hasSubElements()) {
                        context.getWriter().writeStartElement(extensionXmlSerializer.getNamespaceUri(), extension.getName());
                    } else {
                        context.getWriter().writeEmptyElement(extensionXmlSerializer.getNamespaceUri(), extension.getName());
                    }
                    extensionXmlSerializer.write(extension, context);
                    if (extensionXmlSerializer.hasSubElements()) {
                        context.getWriter().writeEndElement();
                    }
                }

                context.getWriter().writeEndElement();
            }

        }
    }

    public static Anonymizer write(Network n, XMLExportOptions options, OutputStream os) {
        try {
            final XMLStreamWriter writer = createXmlStreamWriter(options, os);
            writer.writeStartDocument(StandardCharsets.UTF_8.toString(), "1.0");

            writer.setPrefix(IIDM_PREFIX, IIDM_URI);
            writer.writeStartElement(IIDM_URI, NETWORK_ROOT_ELEMENT_NAME);
            writer.writeNamespace(IIDM_PREFIX, IIDM_URI);

            if (!options.isSkipExtensions()) {
                // add additional extension namespaces and check there is no collision between extensions
                writeExtensionNamespaces(n, writer);
            }

            writer.writeAttribute("id", n.getId());
            writer.writeAttribute("caseDate", n.getCaseDate().toString());
            writer.writeAttribute("forecastDistance", Integer.toString(n.getForecastDistance()));
            writer.writeAttribute("sourceFormat", n.getSourceFormat());
            BusFilter filter = BusFilter.create(n, options);
            Anonymizer anonymizer = options.isAnonymized() ? new SimpleAnonymizer() : null;
            XmlWriterContext context = new XmlWriterContext(anonymizer, writer, options, filter);
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
                if (!filter.test(l.getConverterStation1()) && filter.test(l.getConverterStation2())) {
                    continue;
                }
                HvdcLineXml.INSTANCE.write(l, n, context);
            }

            if (!options.isSkipExtensions()) {
                // write extensions
                writeExtensions(n, context);
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            return anonymizer;
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    public static Anonymizer write(Network n, OutputStream os) {
        return write(n, new XMLExportOptions(), os);
    }

    public static Anonymizer write(Network n, XMLExportOptions options, Path xmlFile) {
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(xmlFile))) {
            return write(n, options, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Anonymizer write(Network n, Path xmlFile) {
        return write(n, new XMLExportOptions(), xmlFile);
    }

    public static Anonymizer writeAndValidate(Network n, Path xmlFile) {
        Anonymizer anonymizer = write(n, xmlFile);
        validateWithExtensions(xmlFile);
        return anonymizer;
    }

    public static Network read(InputStream is) {
        return read(is, new XmlImportConfig(), null);
    }

    public static Network read(InputStream is, XmlImportConfig config, Anonymizer anonymizer) {
        try {
            XMLStreamReader reader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
            int state = reader.next();
            while (state == XMLStreamReader.COMMENT) {
                state = reader.next();
            }
            String id = reader.getAttributeValue(null, "id");
            DateTime date = DateTime.parse(reader.getAttributeValue(null, "caseDate"));
            int forecastDistance = XmlUtil.readOptionalIntegerAttribute(reader, "forecastDistance", 0);
            String sourceFormat = reader.getAttributeValue(null, "sourceFormat");

            Network network = NetworkFactory.create(id, sourceFormat);
            network.setCaseDate(date);
            network.setForecastDistance(forecastDistance);

            XmlReaderContext context = new XmlReaderContext(anonymizer, reader);

            Set<String> extensionNamesNotFound = new TreeSet<>();

            XmlUtil.readUntilEndElement(NETWORK_ROOT_ELEMENT_NAME, reader, () -> {
                switch (reader.getLocalName()) {
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
                        XmlUtil.readUntilEndElement(EXTENSION_ELEMENT_NAME, reader, new XmlUtil.XmlEventHandler() {

                            private boolean topLevel = true;

                            @Override
                            public void onStartElement() throws XMLStreamException {
                                if (topLevel) {
                                    String extensionName = reader.getLocalName();
                                    ExtensionXmlSerializer extensionXmlSerializer = EXTENSIONS_SUPPLIER.get().findSerializer(extensionName);
                                    if (extensionXmlSerializer != null) {
                                        Extension<? extends Identifiable<?>> extension = extensionXmlSerializer.read(identifiable, context);
                                        identifiable.addExtension(extensionXmlSerializer.getExtensionClass(), extension);
                                        topLevel = true;
                                    } else {
                                        extensionNamesNotFound.add(extensionName);
                                        topLevel = false;
                                    }
                                }
                            }
                        });
                        break;

                    default:
                        throw new AssertionError();
                }
            });

            context.getEndTasks().forEach(Runnable::run);

            if (!extensionNamesNotFound.isEmpty()) {
                if (config.isThrowExceptionIfExtensionNotFound()) {
                    throw new PowsyblException("Extensions " + extensionNamesNotFound + " not found");
                } else {
                    LOGGER.error("Extensions {} not found", extensionNamesNotFound);
                }
            }

            return network;
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    public static Network read(Path xmlFile) {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Network validateAndRead(Path xmlFile) {
        validateWithExtensions(xmlFile);
        return read(xmlFile);
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
        float v = XmlUtil.readFloatAttribute(reader, "v");
        float angle = XmlUtil.readFloatAttribute(reader, "angle");
        Bus b = vl[0].getBusBreakerView().getBus(id);
        if (b == null) {
            b = vl[0].getBusView().getBus(id);
        }
        b.setV(v > 0 ? v : Float.NaN).setAngle(angle);
    }

    private static void updateInjection(XMLStreamReader reader, Network network) {
        String id = reader.getAttributeValue(null, "id");
        float p = XmlUtil.readOptionalFloatAttribute(reader, "p");
        float q = XmlUtil.readOptionalFloatAttribute(reader, "q");
        Injection inj = (Injection) network.getIdentifiable(id);
        inj.getTerminal().setP(p).setQ(q);
    }

    private static void updateBranch(XMLStreamReader reader, Network network) {
        String id = reader.getAttributeValue(null, "id");
        float p1 = XmlUtil.readOptionalFloatAttribute(reader, "p1");
        float q1 = XmlUtil.readOptionalFloatAttribute(reader, "q1");
        float p2 = XmlUtil.readOptionalFloatAttribute(reader, "p2");
        float q2 = XmlUtil.readOptionalFloatAttribute(reader, "q2");
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
     * @param network the network to copy
     * @return the copy of the network
     */
    public static Network copy(Network network) {
        return gunzip(gzip(network));
    }
}
