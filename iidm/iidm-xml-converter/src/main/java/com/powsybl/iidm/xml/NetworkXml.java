/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
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
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.json.JsonReader;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.json.JsonWriter;
import com.powsybl.commons.xml.XmlReader;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.anonymizer.Anonymizer;
import com.powsybl.iidm.xml.anonymizer.SimpleAnonymizer;
import com.powsybl.iidm.xml.extensions.AbstractVersionableNetworkExtensionXmlSerializer;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
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
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.powsybl.iidm.xml.IidmXmlConstants.*;
import static com.powsybl.iidm.xml.AbstractTreeDataImporter.SUFFIX_MAPPING;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class NetworkXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkXml.class);

    private static final String EXTENSION_CATEGORY_NAME = "network";
    static final String NETWORK_ROOT_ELEMENT_NAME = "network";
    static final String NETWORK_ARRAY_ELEMENT_NAME = "networks";
    private static final String EXTENSION_ROOT_ELEMENT_NAME = "extension";
    private static final String EXTENSION_ARRAY_ELEMENT_NAME = "extensions";
    private static final String CASE_DATE = "caseDate";
    private static final String FORECAST_DISTANCE = "forecastDistance";
    private static final String SOURCE_FORMAT = "sourceFormat";
    private static final String ID = "id";
    private static final String MINIMUM_VALIDATION_LEVEL = "minimumValidationLevel";

    private static final Supplier<ExtensionProviders<ExtensionXmlSerializer>> EXTENSIONS_SUPPLIER = Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionXmlSerializer.class, EXTENSION_CATEGORY_NAME));

    private NetworkXml() {
    }

    private static void validate(Source xml, List<Source> additionalSchemas) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            int length = IidmXmlVersion.values().length + (int) Arrays.stream(IidmXmlVersion.values())
                    .filter(IidmXmlVersion::supportEquipmentValidationLevel).count();
            Source[] sources = new Source[additionalSchemas.size() + length];
            int i = 0;
            int j = 0;
            for (IidmXmlVersion version : IidmXmlVersion.values()) {
                sources[i] = new StreamSource(NetworkXml.class.getResourceAsStream("/xsd/" + version.getXsd()));
                if (version.supportEquipmentValidationLevel()) {
                    sources[j + IidmXmlVersion.values().length] = new StreamSource(NetworkXml.class.getResourceAsStream("/xsd/" + version.getXsd(false)));
                    j++;
                }
                i++;
            }
            for (int k = 0; k < additionalSchemas.size(); k++) {
                sources[k + length] = additionalSchemas.get(k);
            }
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

    private static void throwExceptionIfOption(AbstractOptions<?> options, String message) {
        if (options.isThrowExceptionIfExtensionNotFound()) {
            throw new PowsyblException(message);
        } else {
            LOGGER.warn(message);
        }
    }

    private static void writeExtension(Extension<? extends Identifiable<?>> extension, NetworkXmlWriterContext context) {
        TreeDataWriter writer = context.getWriter();
        ExtensionXmlSerializer extensionXmlSerializer = getExtensionXmlSerializer(context.getOptions(), extension);
        if (extensionXmlSerializer == null) {
            throw new IllegalStateException("Extension XML Serializer of " + extension.getName() + " should not be null");
        }
        String namespaceUri = getNamespaceUri(extensionXmlSerializer, context.getOptions(), context.getVersion());
        writer.writeStartNode(namespaceUri, extension.getName());
        context.getExtensionVersion(extension.getName()).ifPresent(extensionXmlSerializer::checkExtensionVersionSupported);
        extensionXmlSerializer.write(extension, context);
        writer.writeEndNode();
    }

    private static ExtensionXmlSerializer getExtensionXmlSerializer(ExportOptions options, Extension<? extends Identifiable<?>> extension) {
        if (options.withExtension(extension.getName())) {
            ExtensionXmlSerializer extensionXmlSerializer = options.isThrowExceptionIfExtensionNotFound()
                    ? EXTENSIONS_SUPPLIER.get().findProviderOrThrowException(extension.getName())
                    : EXTENSIONS_SUPPLIER.get().findProvider(extension.getName());
            if (extensionXmlSerializer == null) {
                String message = "XmlSerializer for " + extension.getName() + " not found";
                throwExceptionIfOption(options, message);
            } else if (!extensionXmlSerializer.isSerializable(extension)) {
                return null;
            }
            return extensionXmlSerializer;
        }

        return null;
    }

    private static String getNamespaceUri(ExtensionXmlSerializer<?, ?> extensionXmlSerializer, ExportOptions options, IidmXmlVersion networkVersion) {
        String extensionVersion = getExtensionVersion(extensionXmlSerializer, options);
        return extensionXmlSerializer.getNamespaceUri(extensionVersion);
    }

    private static void writeVoltageAngleLimits(Network n, NetworkXmlWriterContext context) {
        if (n.getVoltageAngleLimitsStream().findAny().isPresent()) {
            context.getWriter().writeStartNodes(VoltageAngleLimitXml.ARRAY_ELEMENT_NAME);
            for (VoltageAngleLimit voltageAngleLimit : n.getVoltageAngleLimits()) {
                VoltageAngleLimitXml.write(voltageAngleLimit, context);
            }
            context.getWriter().writeEndNodes();
        }
    }

    private static void writeExtensions(Network n, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(EXTENSION_ARRAY_ELEMENT_NAME);
        for (Identifiable<?> identifiable : IidmXmlUtil.sorted(n.getIdentifiables(), context.getOptions())) {
            if (!context.isExportedEquipment(identifiable) || !isElementWrittenInsideNetwork(identifiable, n, context)) {
                continue;
            }
            Collection<? extends Extension<? extends Identifiable<?>>> extensions = identifiable.getExtensions().stream()
                    .filter(e -> canTheExtensionBeWritten(getExtensionXmlSerializer(context.getOptions(), e), context.getVersion(), context.getOptions()))
                    .collect(Collectors.toList());

            if (!extensions.isEmpty()) {
                context.getWriter().writeStartNode(context.getNamespaceURI(), EXTENSION_ROOT_ELEMENT_NAME);
                context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(identifiable.getId()));
                for (Extension<? extends Identifiable<?>> extension : IidmXmlUtil.sortedExtensions(extensions, context.getOptions())) {
                    writeExtension(extension, context);
                }
                context.getWriter().writeEndNode();
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static boolean canTheExtensionBeWritten(ExtensionXmlSerializer extensionXmlSerializer, IidmXmlVersion version, ExportOptions options) {
        if (extensionXmlSerializer == null) {
            return false;
        }
        boolean versionExist = true;
        if (extensionXmlSerializer instanceof AbstractVersionableNetworkExtensionXmlSerializer<?, ?> networkExtensionXmlSerializer) {
            versionExist = networkExtensionXmlSerializer.versionExists(version);
        }
        if (!versionExist) {
            String message = String.format("Version %s does not support %s extension", version,
                    extensionXmlSerializer.getExtensionName());
            throwExceptionIfOption(options, message);
        }
        return versionExist;
    }

    private static void writeMainAttributes(Network n, NetworkXmlWriterContext context) {
        context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(n.getId()));
        context.getWriter().writeStringAttribute(CASE_DATE, n.getCaseDate().toString());
        context.getWriter().writeIntAttribute(FORECAST_DISTANCE, n.getForecastDistance());
        context.getWriter().writeStringAttribute(SOURCE_FORMAT, n.getSourceFormat());
    }

    private static XmlWriter initializeXmlWriter(Network n, OutputStream os, ExportOptions options) {
        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(options.isIndent(), INDENT, os, options.getCharset());
            IidmXmlVersion iidmVersion = options.getVersion();
            String iidmNamespace = iidmVersion.getNamespaceURI(n.getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS);

            XmlWriter xmlWriter = new XmlWriter(writer, iidmNamespace, IIDM_PREFIX);

            Set<ExtensionXmlSerializer<?, ?>> serializers = getExtensionSerializers(n, options);
            for (ExtensionXmlSerializer<?, ?> extensionSerializer : serializers) {
                String extensionVersion = getExtensionVersion(extensionSerializer, options);
                xmlWriter.setExtensionNamespace(extensionSerializer.getName(), extensionSerializer.getNamespaceUri(extensionVersion), extensionSerializer.getNamespacePrefix());
            }

            // Ensure that there is no conflict in namespace prefixes and URIs
            checkNamespaceCollisions(options, serializers, iidmVersion);

            return xmlWriter;
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static JsonWriter initializeJsonWriter(OutputStream os, ExportOptions options) {
        JsonFactory jsonFactory = JsonUtil.createJsonFactory();
        try {
            JsonGenerator generator = jsonFactory.createGenerator(os);
            if (options.isIndent()) {
                generator = generator.useDefaultPrettyPrinter();
            }
            return new JsonWriter(generator, options.getVersion().toString("."));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeRootElement(Network n, NetworkXmlWriterContext context) {
        IidmXmlUtil.assertMinimumVersionIfNotDefault(n.getValidationLevel() != ValidationLevel.STEADY_STATE_HYPOTHESIS, NETWORK_ROOT_ELEMENT_NAME, MINIMUM_VALIDATION_LEVEL,
                IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_7, context.getVersion());
        context.getWriter().writeStartNode(context.getNamespaceURI(), NETWORK_ROOT_ELEMENT_NAME);
        writeMainAttributes(n, context);
    }

    private static Map<String, String> getExtensionVersions(Network n, ExportOptions options) {
        Map <String, String> extensionVersionsMap = new LinkedHashMap<>();
        for (ExtensionXmlSerializer<?, ?> extensionSerializer : getExtensionSerializers(n, options)) {
            String version = getExtensionVersion(extensionSerializer, options);
            extensionVersionsMap.put(extensionSerializer.getExtensionName(), version);
        }
        return extensionVersionsMap;
    }

    private static String getExtensionVersion(ExtensionXmlSerializer<?, ?> extensionSerializer, ExportOptions options) {
        Optional<String> specifiedVersion = options.getExtensionVersion(extensionSerializer.getExtensionName());
        if (extensionSerializer instanceof AbstractVersionableNetworkExtensionXmlSerializer<?, ?> versionable) {
            return specifiedVersion
                    .filter(v -> versionable.checkWritingCompatibility(v, options.getVersion()))
                    .orElseGet(() -> versionable.getVersion(options.getVersion()));
        } else {
            return specifiedVersion.orElseGet(extensionSerializer::getVersion);
        }
    }

    /**
     * Gets the list of the serializers needed to export the current network
     */
    private static Set<ExtensionXmlSerializer<?, ?>> getExtensionSerializers(Network n, ExportOptions options) {
        if (options.withNoExtension()) {
            return Collections.emptySet();
        }

        IidmXmlVersion networkVersion = options.getVersion();
        return n.getIdentifiables().stream().flatMap(identifiable -> identifiable.getExtensions()
                        .stream()
                        .map(extension -> (ExtensionXmlSerializer<?, ?>) getExtensionXmlSerializer(options, extension))
                        .filter(exs -> canTheExtensionBeWritten(exs, networkVersion, options)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void checkNamespaceCollisions(ExportOptions options, Set<ExtensionXmlSerializer<?, ?>> serializers, IidmXmlVersion networkVersion) {
        Set<String> extensionUris = new HashSet<>();
        Set<String> extensionPrefixes = new HashSet<>();
        for (ExtensionXmlSerializer<?, ?> extensionXmlSerializer : serializers) {
            String namespaceUri = getNamespaceUri(extensionXmlSerializer, options, networkVersion);
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
        }
    }

    private static void writeBaseNetwork(Network n, NetworkXmlWriterContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_7, context, () -> context.getWriter().writeEnumAttribute(MINIMUM_VALIDATION_LEVEL, n.getValidationLevel()));

        AliasesXml.write(n, NETWORK_ROOT_ELEMENT_NAME, context);
        PropertiesXml.write(n, context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_11, context, () -> writeSubnetworks(n, context));

        writeVoltageLevels(n, context);
        writeSubstations(n, context);
        writeLines(n, context);
        writeTieLines(n, context);
        writeHvdcLines(n, context);
    }

    private static void writeSubnetworks(Network n, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(NETWORK_ARRAY_ELEMENT_NAME);
        for (Network subnetwork : IidmXmlUtil.sorted(n.getSubnetworks(), context.getOptions())) {
            IidmXmlUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, VoltageLevelXml.ROOT_ELEMENT_NAME,
                    IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_11, context);
            write(subnetwork, context);
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeVoltageLevels(Network n, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(VoltageLevelXml.ARRAY_ELEMENT_NAME);
        for (VoltageLevel voltageLevel : IidmXmlUtil.sorted(n.getVoltageLevels(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(voltageLevel, n, context) && voltageLevel.getSubstation().isEmpty()) {
                IidmXmlUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, VoltageLevelXml.ROOT_ELEMENT_NAME,
                        IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_6, context);
                VoltageLevelXml.INSTANCE.write(voltageLevel, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeSubstations(Network n, NetworkXmlWriterContext context) {
        context.getWriter().writeStartNodes(SubstationXml.ARRAY_ELEMENT_NAME);
        for (Substation s : IidmXmlUtil.sorted(n.getSubstations(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(s, n, context)) {
                SubstationXml.INSTANCE.write(s, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeLines(Network n, NetworkXmlWriterContext context) {
        BusFilter filter = context.getFilter();
        context.getWriter().writeStartNodes(LineXml.ARRAY_ELEMENT_NAME);
        for (Line l : IidmXmlUtil.sorted(n.getLines(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(l, n, context) && filter.test(l)) {
                LineXml.INSTANCE.write(l, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeTieLines(Network n, NetworkXmlWriterContext context) {
        BusFilter filter = context.getFilter();
        context.getWriter().writeStartNodes(TieLineXml.ARRAY_ELEMENT_NAME);
        for (TieLine l : IidmXmlUtil.sorted(n.getTieLines(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(l, n, context) && filter.test(l)) {
                TieLineXml.INSTANCE.write(l, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeHvdcLines(Network n, NetworkXmlWriterContext context) {
        BusFilter filter = context.getFilter();
        context.getWriter().writeStartNodes(HvdcLineXml.ARRAY_ELEMENT_NAME);
        for (HvdcLine l : IidmXmlUtil.sorted(n.getHvdcLines(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(l, n, context) && filter.test(l.getConverterStation1()) && filter.test(l.getConverterStation2())) {
                HvdcLineXml.INSTANCE.write(l, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static TreeDataWriter createTreeDataWriter(Network n, ExportOptions options, OutputStream os) {
        return switch (options.getFormat()) {
            case XML -> initializeXmlWriter(n, os, options);
            case JSON -> initializeJsonWriter(os, options);
        };
    }

    private static void write(Network network, NetworkXmlWriterContext context) {
        // consider the network has been exported so its extensions will be written
        // (should be done before extensions are written)
        context.addExportedEquipment(network);
        writeRootElement(network, context);
        writeBaseNetwork(network, context);
        writeVoltageAngleLimits(network, context);
        writeExtensions(network, context);
        context.getWriter().writeEndNode();
    }

    public static Anonymizer write(Network n, ExportOptions options, OutputStream os) {
        try (TreeDataWriter writer = createTreeDataWriter(n, options, os)) {
            NetworkXmlWriterContext context = createContext(n, options, writer);
            writer.setVersions(getExtensionVersions(n, options));
            write(n, context);
            return context.getAnonymizer();
        }
    }

    /**
     * Return true if the given element has to be written in the given network, false otherwise
     */
    private static boolean isElementWrittenInsideNetwork(Identifiable<?> element, Network n, NetworkXmlWriterContext context) {
        // if subnetworks not supported, all elements need to be written in the root network (in that case this is only called with n being the root network)
        if (!supportSubnetworksExport(context)) {
            return true;
        }
        // corner case: if the element is the given network, it is considered as written within that network, as extensions have to be written within the network
        if (n.getId().equals(element.getId())) {
            return true;
        }
        // Main case: the element has to be written
        // - if the element is directly in the network (not in one of its subnetworks)
        // - and if it's not a network itself (linked to previous corner case)
        return element.getParentNetwork() == n && element.getType() != IdentifiableType.NETWORK;
    }

    private static boolean supportSubnetworksExport(NetworkXmlWriterContext context) {
        return context.getVersion().compareTo(IidmXmlVersion.V_1_11) >= 0;
    }

    private static NetworkXmlWriterContext createContext(Network n, ExportOptions options, TreeDataWriter writer) {
        BusFilter filter = BusFilter.create(n, options);
        Anonymizer anonymizer = options.isAnonymized() ? new SimpleAnonymizer() : null;
        return new NetworkXmlWriterContext(anonymizer, writer, options, filter, options.getVersion(), n.getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS);
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
        try (TreeDataReader reader = createTreeDataReader(is, config)) {
            return read(reader, config, anonymizer, networkFactory);
        }
    }

    private static TreeDataReader createTreeDataReader(InputStream is, ImportOptions config) {
        return switch (config.getFormat()) {
            case XML -> initializeXmlReader(is, config);
            case JSON -> initializeJsonReader(is, config);
        };
    }

    private static TreeDataReader initializeJsonReader(InputStream is, ImportOptions config) {
        try {
            JsonParser parser = JsonUtil.createJsonFactory().createParser(is);
            parser.nextToken();
            return new JsonReader(parser, NETWORK_ROOT_ELEMENT_NAME, createArrayNameToSingleNameMap(config));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, String> createArrayNameToSingleNameMap(ImportOptions config) {
        Map<String, String> basicMap = Map.ofEntries(
                Map.entry(NETWORK_ARRAY_ELEMENT_NAME, NETWORK_ROOT_ELEMENT_NAME),
                Map.entry(EXTENSION_ARRAY_ELEMENT_NAME, EXTENSION_ROOT_ELEMENT_NAME),
                Map.entry(AbstractSwitchXml.ARRAY_ELEMENT_NAME, AbstractSwitchXml.ROOT_ELEMENT_NAME),
                Map.entry(AbstractTransformerXml.STEP_ARRAY_ELEMENT_NAME, AbstractTransformerXml.STEP_ROOT_ELEMENT_NAME),
                Map.entry(AliasesXml.ARRAY_ELEMENT_NAME, AliasesXml.ROOT_ELEMENT_NAME),
                Map.entry(BatteryXml.ARRAY_ELEMENT_NAME, BatteryXml.ROOT_ELEMENT_NAME),
                Map.entry(BusXml.ARRAY_ELEMENT_NAME, BusXml.ROOT_ELEMENT_NAME),
                Map.entry(BusbarSectionXml.ARRAY_ELEMENT_NAME, BusbarSectionXml.ROOT_ELEMENT_NAME),
                Map.entry(DanglingLineXml.ARRAY_ELEMENT_NAME, DanglingLineXml.ROOT_ELEMENT_NAME),
                Map.entry(GeneratorXml.ARRAY_ELEMENT_NAME, GeneratorXml.ROOT_ELEMENT_NAME),
                Map.entry(HvdcLineXml.ARRAY_ELEMENT_NAME, HvdcLineXml.ROOT_ELEMENT_NAME),
                Map.entry(LccConverterStationXml.ARRAY_ELEMENT_NAME, LccConverterStationXml.ROOT_ELEMENT_NAME),
                Map.entry(LineXml.ARRAY_ELEMENT_NAME, LineXml.ROOT_ELEMENT_NAME),
                Map.entry(LoadXml.ARRAY_ELEMENT_NAME, LoadXml.ROOT_ELEMENT_NAME),
                Map.entry(NodeBreakerViewInternalConnectionXml.ARRAY_ELEMENT_NAME, NodeBreakerViewInternalConnectionXml.ROOT_ELEMENT_NAME),
                Map.entry(PropertiesXml.ARRAY_ELEMENT_NAME, PropertiesXml.ROOT_ELEMENT_NAME),
                Map.entry(ReactiveLimitsXml.POINT_ARRAY_ELEMENT_NAME, ReactiveLimitsXml.POINT_ROOT_ELEMENT_NAME),
                Map.entry(ShuntXml.ARRAY_ELEMENT_NAME, ShuntXml.ROOT_ELEMENT_NAME),
                Map.entry(ShuntXml.SECTION_ARRAY_ELEMENT_NAME, ShuntXml.SECTION_ROOT_ELEMENT_NAME),
                Map.entry(StaticVarCompensatorXml.ARRAY_ELEMENT_NAME, StaticVarCompensatorXml.ROOT_ELEMENT_NAME),
                Map.entry(SubstationXml.ARRAY_ELEMENT_NAME, SubstationXml.ROOT_ELEMENT_NAME),
                Map.entry(ThreeWindingsTransformerXml.ARRAY_ELEMENT_NAME, ThreeWindingsTransformerXml.ROOT_ELEMENT_NAME),
                Map.entry(TieLineXml.ARRAY_ELEMENT_NAME, TieLineXml.ROOT_ELEMENT_NAME),
                Map.entry(TwoWindingsTransformerXml.ARRAY_ELEMENT_NAME, TwoWindingsTransformerXml.ROOT_ELEMENT_NAME),
                Map.entry(VoltageAngleLimitXml.ARRAY_ELEMENT_NAME, VoltageAngleLimitXml.ROOT_ELEMENT_NAME),
                Map.entry(VoltageLevelXml.ARRAY_ELEMENT_NAME, VoltageLevelXml.ROOT_ELEMENT_NAME),
                Map.entry(VoltageLevelXml.INJ_ARRAY_ELEMENT_NAME, VoltageLevelXml.INJ_ROOT_ELEMENT_NAME),
                Map.entry(VscConverterStationXml.ARRAY_ELEMENT_NAME, VscConverterStationXml.ROOT_ELEMENT_NAME));

        Map<String, String> extensionsMap = new HashMap<>();
        if (!config.withNoExtension()) {
            for (ExtensionXmlSerializer<?, ?> e : EXTENSIONS_SUPPLIER.get().getProviders()) {
                extensionsMap.putAll(e.getArrayNameToSingleNameMap());
            }
        }

        if (extensionsMap.isEmpty()) {
            return basicMap;
        } else {
            Map<String, String> mergedMap = new HashMap<>();
            mergedMap.putAll(basicMap);
            mergedMap.putAll(extensionsMap);
            return mergedMap;
        }
    }

    private static TreeDataReader initializeXmlReader(InputStream is, ImportOptions config) {
        try {
            XMLStreamReader xmlReader = XmlReader.createXMLStreamReader(is);
            int state = xmlReader.next();
            while (state == XMLStreamConstants.COMMENT) {
                state = xmlReader.next();
            }
            return new XmlReader(xmlReader, getNamespaceVersionMap(),
                    config.withNoExtension() ? Collections.emptyList() : EXTENSIONS_SUPPLIER.get().getProviders());
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static Map<String, String> getNamespaceVersionMap() {
        Map<String, String> namespaceVersionMap = new HashMap<>();
        Arrays.stream(IidmXmlVersion.values())
                .forEach(v -> namespaceVersionMap.put(v.getNamespaceURI(), v.toString(".")));
        Arrays.stream(IidmXmlVersion.values())
                .filter(IidmXmlVersion::supportEquipmentValidationLevel)
                .forEach(v -> namespaceVersionMap.put(v.getNamespaceURI(false), v.toString(".")));
        return namespaceVersionMap;
    }

    private static void readElement(String elementName, Deque<Network> networks, NetworkFactory networkFactory, NetworkXmlReaderContext context,
                                    Set<String> extensionNamesNotFound) {
        switch (elementName) {
            case AliasesXml.ROOT_ELEMENT_NAME -> checkSupportedAndReadAlias(networks.peek(), context);
            case PropertiesXml.ROOT_ELEMENT_NAME -> PropertiesXml.read(networks.peek(), context);
            case NETWORK_ROOT_ELEMENT_NAME -> checkSupportedAndReadSubnetwork(networks, networkFactory, context, extensionNamesNotFound);
            case VoltageLevelXml.ROOT_ELEMENT_NAME -> checkSupportedAndReadVoltageLevel(context, networks);
            case SubstationXml.ROOT_ELEMENT_NAME -> SubstationXml.INSTANCE.read(networks.peek(), context);
            case LineXml.ROOT_ELEMENT_NAME -> LineXml.INSTANCE.read(networks.peek(), context);
            case TieLineXml.ROOT_ELEMENT_NAME -> TieLineXml.INSTANCE.read(networks.peek(), context);
            case HvdcLineXml.ROOT_ELEMENT_NAME -> HvdcLineXml.INSTANCE.read(networks.peek(), context);
            case VoltageAngleLimitXml.ROOT_ELEMENT_NAME -> VoltageAngleLimitXml.read(networks.peek(), context);
            case EXTENSION_ROOT_ELEMENT_NAME -> findExtendableAndReadExtension(networks.getFirst(), context, extensionNamesNotFound);
            default -> throw new IllegalStateException();
        }
    }

    private static void checkSupportedAndReadAlias(Network network, NetworkXmlReaderContext context) {
        IidmXmlUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, AliasesXml.ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
        AliasesXml.read(network, context);
    }

    private static void checkSupportedAndReadSubnetwork(Deque<Network> networks, NetworkFactory networkFactory, NetworkXmlReaderContext context, Set<String> extensionNamesNotFound) {
        IidmXmlUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, NETWORK_ROOT_ELEMENT_NAME,
                IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_11, context);
        if (networks.size() > 1) {
            throw new PowsyblException("Only one level of subnetworks is currently supported.");
        }
        // Create a new subnetwork and push it in the deque to be used as the network to update
        Network subnetwork = initNetwork(networkFactory, context, context.getReader(), networks.peek());
        networks.push(subnetwork);
        // Read subnetwork content
        context.getReader().readChildNodes(
                elementName -> readElement(elementName, networks, networkFactory, context, extensionNamesNotFound));
        // Pop the subnetwork. We will now work with its parent.
        networks.pop();
    }

    private static void checkSupportedAndReadVoltageLevel(NetworkXmlReaderContext context, Deque<Network> networks) {
        IidmXmlUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, VoltageLevelXml.ROOT_ELEMENT_NAME,
                IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_6, context);
        VoltageLevelXml.INSTANCE.read(networks.peek(), context);
    }

    private static void findExtendableAndReadExtension(Network network, NetworkXmlReaderContext context, Set<String> extensionNamesNotFound) {
        String id2 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
        Identifiable identifiable = network.getIdentifiable(id2);
        if (identifiable == null) {
            throw new PowsyblException("Identifiable " + id2 + " not found");
        }
        readExtensions(identifiable, context, extensionNamesNotFound);
    }

    private static Network initNetwork(NetworkFactory networkFactory, NetworkXmlReaderContext context, TreeDataReader reader, Network rootNetwork) {
        String id = context.getAnonymizer().deanonymizeString(reader.readStringAttribute(ID));
        DateTime date = DateTime.parse(reader.readStringAttribute(CASE_DATE));
        int forecastDistance = reader.readIntAttribute(FORECAST_DISTANCE, 0);
        String sourceFormat = reader.readStringAttribute(SOURCE_FORMAT);

        Network network;
        if (rootNetwork == null) {
            network = networkFactory.createNetwork(id, sourceFormat);
        } else {
            network = rootNetwork.createSubnetwork(id, id, sourceFormat);
        }
        network.setCaseDate(date);
        network.setForecastDistance(forecastDistance);

        ValidationLevel[] minValidationLevel = new ValidationLevel[1];
        minValidationLevel[0] = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_7, context, () -> minValidationLevel[0] = reader.readEnumAttribute(MINIMUM_VALIDATION_LEVEL, ValidationLevel.class));

        IidmXmlUtil.assertMinimumVersionIfNotDefault(minValidationLevel[0] != ValidationLevel.STEADY_STATE_HYPOTHESIS, NETWORK_ROOT_ELEMENT_NAME, MINIMUM_VALIDATION_LEVEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_7, context);
        network.setMinimumAcceptableValidationLevel(minValidationLevel[0]);
        return network;
    }

    public static Network read(TreeDataReader reader, ImportOptions config, Anonymizer anonymizer,
                               NetworkFactory networkFactory) {

        IidmXmlVersion iidmVersion = IidmXmlVersion.of(reader.readRootVersion(), ".");
        Map<String, String> extensionVersions = reader.readVersions();

        NetworkXmlReaderContext context = new NetworkXmlReaderContext(anonymizer, reader, config, iidmVersion, extensionVersions);

        Network network = initNetwork(networkFactory, context, reader, null);

        Set<String> extensionNamesNotFound = new TreeSet<>();
        Deque<Network> networks = new ArrayDeque<>(2);
        networks.push(network);

        reader.readChildNodes(elementName -> readElement(elementName, networks, networkFactory, context, extensionNamesNotFound));

        checkExtensionsNotFound(context, extensionNamesNotFound);

        context.getEndTasks().forEach(Runnable::run);

        return network;
    }

    private static void checkExtensionsNotFound(NetworkXmlReaderContext context, Set<String> extensionNamesNotFound) {
        if (!extensionNamesNotFound.isEmpty()) {
            throwExceptionIfOption(context.getOptions(), "Extensions " + extensionNamesNotFound + " " +
                    "not found !");
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
                                       Set<String> extensionNamesNotFound) {

        context.getReader().readChildNodes(extensionName -> {
            // extensions root elements are nested directly in 'extension' element, so there is no need
            // to check for an extension to exist if depth is greater than zero. Furthermore in case of
            // missing extension serializer, we must not check for an extension in sub elements.
            if (!context.getOptions().withExtension(extensionName)) {
                context.getReader().skipChildNodes();
            }

            ExtensionXmlSerializer extensionXmlSerializer = EXTENSIONS_SUPPLIER.get().findProvider(extensionName);
            if (extensionXmlSerializer != null) {
                Extension<? extends Identifiable<?>> extension = extensionXmlSerializer.read(identifiable, context);
                identifiable.addExtension(extensionXmlSerializer.getExtensionClass(), extension);
            } else {
                extensionNamesNotFound.add(extensionName);
                context.getReader().skipChildNodes();
            }
        });
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
        return copy(network, NetworkFactory.findDefault());
    }

    /**
     * Deep copy of the network using XML converter.
     *
     * @param network        the network to copy
     * @param networkFactory the network factory to use for the copy
     * @return the copy of the network
     */
    public static Network copy(Network network, NetworkFactory networkFactory) {
        return copy(network, networkFactory, ForkJoinPool.commonPool());
    }

    public static Network copy(Network network, NetworkFactory networkFactory, ExecutorService executor) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(networkFactory);
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
            return read(is, new ImportOptions(), null, networkFactory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
