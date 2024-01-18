/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.binary.BinReader;
import com.powsybl.commons.binary.BinWriter;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.exceptions.UncheckedSaxException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.io.TreeDataHeader;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.json.JsonReader;
import com.powsybl.commons.json.JsonWriter;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.xml.XmlReader;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import com.powsybl.iidm.serde.anonymizer.SimpleAnonymizer;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.powsybl.iidm.serde.AbstractTreeDataImporter.SUFFIX_MAPPING;
import static com.powsybl.iidm.serde.IidmSerDeConstants.IIDM_PREFIX;
import static com.powsybl.iidm.serde.IidmSerDeConstants.INDENT;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class NetworkSerDe {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkSerDe.class);

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

    /** Magic number for binary iidm files ("Binary IIDM" in ASCII) */
    static final byte[] BIIDM_MAGIC_NUMBER = {0x42, 0x69, 0x6e, 0x61, 0x72, 0x79, 0x20, 0x49, 0x49, 0x44, 0x4d};

    private static final Supplier<ExtensionProviders<ExtensionSerDe>> EXTENSIONS_SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSerDe.class, EXTENSION_CATEGORY_NAME));

    private static final Supplier<Schema> SCHEMA_SUPPLIER = Suppliers.memoize(NetworkSerDe::createSchema);

    private NetworkSerDe() {
        ExtensionProviders.createProvider(ExtensionSerDe.class, EXTENSION_CATEGORY_NAME);
    }

    public static void validate(InputStream is) {
        Validator validator = SCHEMA_SUPPLIER.get().newValidator();
        try {
            validator.validate(new StreamSource(is));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (SAXException e) {
            throw new UncheckedSaxException(e);
        }
    }

    public static void validate(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            validate(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Schema createSchema() {
        List<Source> additionalSchemas = new ArrayList<>();
        for (ExtensionSerDe<?, ?> e : EXTENSIONS_SUPPLIER.get().getProviders()) {
            e.getXsdAsStreamList().forEach(xsd -> additionalSchemas.add(new StreamSource(xsd)));
        }
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            int length = IidmVersion.values().length + (int) Arrays.stream(IidmVersion.values())
                    .filter(IidmVersion::supportEquipmentValidationLevel).count();
            Source[] sources = new Source[additionalSchemas.size() + length];
            int i = 0;
            int j = 0;
            for (IidmVersion version : IidmVersion.values()) {
                sources[i] = new StreamSource(NetworkSerDe.class.getResourceAsStream("/xsd/" + version.getXsd()));
                if (version.supportEquipmentValidationLevel()) {
                    sources[j + IidmVersion.values().length] = new StreamSource(NetworkSerDe.class.getResourceAsStream("/xsd/" + version.getXsd(false)));
                    j++;
                }
                i++;
            }
            for (int k = 0; k < additionalSchemas.size(); k++) {
                sources[k + length] = additionalSchemas.get(k);
            }
            return factory.newSchema(sources);
        } catch (SAXException e) {
            throw new UncheckedSaxException(e);
        }
    }

    private static void throwExceptionIfOption(AbstractOptions<?> options, String message) {
        if (options.isThrowExceptionIfExtensionNotFound()) {
            throw new PowsyblException(message);
        } else {
            LOGGER.warn(message);
        }
    }

    private static void writeExtension(Extension<? extends Identifiable<?>> extension, NetworkSerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        ExtensionSerDe extensionSerDe = getExtensionSerializer(context.getOptions(), extension);
        if (extensionSerDe == null) {
            throw new IllegalStateException("Extension Serializer of " + extension.getName() + " should not be null");
        }
        String namespaceUri = getNamespaceUri(extensionSerDe, context.getOptions());
        writer.writeStartNode(namespaceUri, extension.getName());
        context.getExtensionVersion(extension.getName()).ifPresent(extensionSerDe::checkExtensionVersionSupported);
        extensionSerDe.write(extension, context);
        writer.writeEndNode();
    }

    private static ExtensionSerDe getExtensionSerializer(ExportOptions options, Extension<? extends Identifiable<?>> extension) {
        if (options.withExtension(extension.getName())) {
            ExtensionSerDe extensionSerDe = options.isThrowExceptionIfExtensionNotFound()
                    ? EXTENSIONS_SUPPLIER.get().findProviderOrThrowException(extension.getName())
                    : EXTENSIONS_SUPPLIER.get().findProvider(extension.getName());
            if (extensionSerDe == null) {
                String message = "XmlSerializer for " + extension.getName() + " not found";
                throwExceptionIfOption(options, message);
            } else if (!extensionSerDe.isSerializable(extension)) {
                return null;
            }
            return extensionSerDe;
        }

        return null;
    }

    private static String getNamespaceUri(ExtensionSerDe<?, ?> extensionSerDe, ExportOptions options) {
        String extensionVersion = getExtensionVersion(extensionSerDe, options);
        return extensionSerDe.getNamespaceUri(extensionVersion);
    }

    private static void writeVoltageAngleLimits(Network n, NetworkSerializerContext context) {
        if (n.getVoltageAngleLimitsStream().findAny().isPresent()) {
            context.getWriter().writeStartNodes();
            for (VoltageAngleLimit voltageAngleLimit : n.getVoltageAngleLimits()) {
                VoltageAngleLimitSerDe.write(voltageAngleLimit, context);
            }
            context.getWriter().writeEndNodes();
        }
    }

    private static void writeExtensions(Network n, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (Identifiable<?> identifiable : IidmSerDeUtil.sorted(n.getIdentifiables(), context.getOptions())) {
            if (!context.isExportedEquipment(identifiable) || !isElementWrittenInsideNetwork(identifiable, n, context)) {
                continue;
            }
            Collection<? extends Extension<? extends Identifiable<?>>> extensions = identifiable.getExtensions().stream()
                    .filter(e -> canTheExtensionBeWritten(getExtensionSerializer(context.getOptions(), e), context.getVersion(), context.getOptions()))
                    .collect(Collectors.toList());

            if (!extensions.isEmpty()) {
                context.getWriter().writeStartNode(context.getNamespaceURI(), EXTENSION_ROOT_ELEMENT_NAME);
                context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(identifiable.getId()));
                for (Extension<? extends Identifiable<?>> extension : IidmSerDeUtil.sortedExtensions(extensions, context.getOptions())) {
                    writeExtension(extension, context);
                }
                context.getWriter().writeEndNode();
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static boolean canTheExtensionBeWritten(ExtensionSerDe extensionSerDe, IidmVersion version, ExportOptions options) {
        if (extensionSerDe == null) {
            return false;
        }
        boolean versionExist = true;
        if (extensionSerDe instanceof AbstractVersionableNetworkExtensionSerDe<?, ?> networkExtensionSerializer) {
            versionExist = networkExtensionSerializer.versionExists(version);
        }
        if (!versionExist) {
            String message = String.format("Version %s does not support %s extension", version,
                    extensionSerDe.getExtensionName());
            throwExceptionIfOption(options, message);
        }
        return versionExist;
    }

    private static void writeMainAttributes(Network n, NetworkSerializerContext context) {
        context.getWriter().writeStringAttribute(ID, context.getAnonymizer().anonymizeString(n.getId()));
        context.getWriter().writeStringAttribute(CASE_DATE, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(n.getCaseDate()));
        context.getWriter().writeIntAttribute(FORECAST_DISTANCE, n.getForecastDistance());
        context.getWriter().writeStringAttribute(SOURCE_FORMAT, n.getSourceFormat());
    }

    private static XmlWriter createXmlWriter(Network n, OutputStream os, ExportOptions options) {
        try {
            String iidmNamespace = options.getVersion().getNamespaceURI(n.getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS);
            String indent = options.isIndent() ? INDENT : null;
            XmlWriter xmlWriter = new XmlWriter(os, indent, options.getCharset(), iidmNamespace, IIDM_PREFIX);

            Set<ExtensionSerDe<?, ?>> serializers = getExtensionSerializers(n, options);
            for (ExtensionSerDe<?, ?> extensionSerDe : serializers) {
                String extensionVersion = getExtensionVersion(extensionSerDe, options);
                xmlWriter.setExtensionNamespace(extensionSerDe.getName(), extensionSerDe.getNamespaceUri(extensionVersion), extensionSerDe.getNamespacePrefix());
            }

            // Ensure that there is no conflict in namespace prefixes and URIs
            checkNamespaceCollisions(options, serializers);

            return xmlWriter;
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static JsonWriter createJsonWriter(OutputStream os, ExportOptions options) {
        try {
            return new JsonWriter(os, options.isIndent(), options.getVersion().toString("."), createSingleNameToArrayNameMap(options));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static TreeDataWriter createBinWriter(OutputStream os, ExportOptions options) {
        LOGGER.warn("BETA feature, the resulting binary file is not guaranteed to still be readable in the next releases");
        return new BinWriter(os, BIIDM_MAGIC_NUMBER, options.getVersion().toString("."));
    }

    private static void writeRootElement(Network n, NetworkSerializerContext context) {
        IidmSerDeUtil.assertMinimumVersionIfNotDefault(n.getValidationLevel() != ValidationLevel.STEADY_STATE_HYPOTHESIS, NETWORK_ROOT_ELEMENT_NAME, MINIMUM_VALIDATION_LEVEL,
                IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_7, context.getVersion());
        context.getWriter().writeStartNode(context.getNamespaceURI(), NETWORK_ROOT_ELEMENT_NAME);
        writeMainAttributes(n, context);
    }

    private static Map<String, String> getExtensionVersions(Network n, ExportOptions options) {
        Map <String, String> extensionVersionsMap = new LinkedHashMap<>();
        for (ExtensionSerDe<?, ?> extensionSerDe : getExtensionSerializers(n, options)) {
            String version = getExtensionVersion(extensionSerDe, options);
            extensionVersionsMap.put(extensionSerDe.getExtensionName(), version);
        }
        return extensionVersionsMap;
    }

    private static String getExtensionVersion(ExtensionSerDe<?, ?> extensionSerDe, ExportOptions options) {
        Optional<String> specifiedVersion = options.getExtensionVersion(extensionSerDe.getExtensionName());
        if (extensionSerDe instanceof AbstractVersionableNetworkExtensionSerDe<?, ?> versionable) {
            return specifiedVersion
                    .filter(v -> versionable.checkWritingCompatibility(v, options.getVersion()))
                    .orElseGet(() -> versionable.getVersion(options.getVersion()));
        } else {
            return specifiedVersion.orElseGet(extensionSerDe::getVersion);
        }
    }

    /**
     * Gets the list of the serializers needed to export the current network
     */
    private static Set<ExtensionSerDe<?, ?>> getExtensionSerializers(Network n, ExportOptions options) {
        if (options.withNoExtension()) {
            return Collections.emptySet();
        }

        IidmVersion networkVersion = options.getVersion();
        return n.getIdentifiables().stream().flatMap(identifiable -> identifiable.getExtensions()
                        .stream()
                        .map(extension -> (ExtensionSerDe<?, ?>) getExtensionSerializer(options, extension))
                        .filter(exs -> canTheExtensionBeWritten(exs, networkVersion, options)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void checkNamespaceCollisions(ExportOptions options, Set<ExtensionSerDe<?, ?>> serializers) {
        Set<String> extensionUris = new HashSet<>();
        Set<String> extensionPrefixes = new HashSet<>();
        for (ExtensionSerDe<?, ?> extensionSerDe : serializers) {
            String namespaceUri = getNamespaceUri(extensionSerDe, options);
            if (extensionUris.contains(namespaceUri)) {
                throw new PowsyblException("Extension namespace URI collision");
            } else {
                extensionUris.add(namespaceUri);
            }

            if (extensionPrefixes.contains(extensionSerDe.getNamespacePrefix())) {
                throw new PowsyblException("Extension namespace prefix collision");
            } else {
                extensionPrefixes.add(extensionSerDe.getNamespacePrefix());
            }
        }
    }

    private static void writeBaseNetwork(Network n, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_7, context, () -> context.getWriter().writeEnumAttribute(MINIMUM_VALIDATION_LEVEL, n.getValidationLevel()));

        AliasesSerDe.write(n, NETWORK_ROOT_ELEMENT_NAME, context);
        PropertiesSerDe.write(n, context);

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_11, context, () -> writeSubnetworks(n, context));

        writeVoltageLevels(n, context);
        writeSubstations(n, context);
        writeLines(n, context);
        writeTieLines(n, context);
        writeHvdcLines(n, context);
    }

    private static void writeSubnetworks(Network n, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (Network subnetwork : IidmSerDeUtil.sorted(n.getSubnetworks(), context.getOptions())) {
            IidmSerDeUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, VoltageLevelSerDe.ROOT_ELEMENT_NAME,
                    IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_11, context);
            write(subnetwork, context);
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeVoltageLevels(Network n, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (VoltageLevel voltageLevel : IidmSerDeUtil.sorted(n.getVoltageLevels(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(voltageLevel, n, context) && voltageLevel.getSubstation().isEmpty()) {
                IidmSerDeUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, VoltageLevelSerDe.ROOT_ELEMENT_NAME,
                        IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_6, context);
                VoltageLevelSerDe.INSTANCE.write(voltageLevel, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeSubstations(Network n, NetworkSerializerContext context) {
        context.getWriter().writeStartNodes();
        for (Substation s : IidmSerDeUtil.sorted(n.getSubstations(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(s, n, context)) {
                SubstationSerDe.INSTANCE.write(s, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeLines(Network n, NetworkSerializerContext context) {
        BusFilter filter = context.getFilter();
        context.getWriter().writeStartNodes();
        for (Line l : IidmSerDeUtil.sorted(n.getLines(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(l, n, context) && filter.test(l)) {
                LineSerDe.INSTANCE.write(l, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeTieLines(Network n, NetworkSerializerContext context) {
        BusFilter filter = context.getFilter();
        context.getWriter().writeStartNodes();
        for (TieLine l : IidmSerDeUtil.sorted(n.getTieLines(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(l, n, context) && filter.test(l)) {
                TieLineSerDe.INSTANCE.write(l, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static void writeHvdcLines(Network n, NetworkSerializerContext context) {
        BusFilter filter = context.getFilter();
        context.getWriter().writeStartNodes();
        for (HvdcLine l : IidmSerDeUtil.sorted(n.getHvdcLines(), context.getOptions())) {
            if (isElementWrittenInsideNetwork(l, n, context) && filter.test(l.getConverterStation1()) && filter.test(l.getConverterStation2())) {
                HvdcLineSerDe.INSTANCE.write(l, n, context);
            }
        }
        context.getWriter().writeEndNodes();
    }

    private static TreeDataWriter createTreeDataWriter(Network n, ExportOptions options, OutputStream os) {
        return switch (options.getFormat()) {
            case XML -> createXmlWriter(n, os, options);
            case JSON -> createJsonWriter(os, options);
            case BIN -> createBinWriter(os, options);
        };
    }

    private static void write(Network network, NetworkSerializerContext context) {
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
            NetworkSerializerContext context = createContext(n, options, writer);
            writer.setVersions(getExtensionVersions(n, options));
            write(n, context);
            return context.getAnonymizer();
        }
    }

    /**
     * Return true if the given element has to be written in the given network, false otherwise
     */
    private static boolean isElementWrittenInsideNetwork(Identifiable<?> element, Network n, NetworkSerializerContext context) {
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

    private static boolean supportSubnetworksExport(NetworkSerializerContext context) {
        return context.getVersion().compareTo(IidmVersion.V_1_11) >= 0;
    }

    private static NetworkSerializerContext createContext(Network n, ExportOptions options, TreeDataWriter writer) {
        BusFilter filter = BusFilter.create(n, options);
        Anonymizer anonymizer = options.isAnonymized() ? new SimpleAnonymizer() : null;
        return new NetworkSerializerContext(anonymizer, writer, options, filter, options.getVersion(), n.getValidationLevel() == ValidationLevel.STEADY_STATE_HYPOTHESIS);
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

    public static Network read(InputStream is) {
        return read(is, new ImportOptions(), null);
    }

    public static Network read(InputStream is, ImportOptions config, Anonymizer anonymizer) {
        return read(is, config, anonymizer, NetworkFactory.findDefault(), Reporter.NO_OP);
    }

    public static Network read(InputStream is, ImportOptions config, Anonymizer anonymizer, NetworkFactory networkFactory, Reporter reporter) {
        try (TreeDataReader reader = createTreeDataReader(is, config)) {
            return read(reader, config, anonymizer, networkFactory, reporter);
        }
    }

    private static TreeDataReader createTreeDataReader(InputStream is, ImportOptions config) {
        return switch (config.getFormat()) {
            case XML -> createXmlReader(is, config);
            case JSON -> createJsonReader(is, config);
            case BIN -> new BinReader(is, BIIDM_MAGIC_NUMBER);
        };
    }

    private static TreeDataReader createJsonReader(InputStream is, ImportOptions config) {
        try {
            return new JsonReader(is, NETWORK_ROOT_ELEMENT_NAME, createArrayNameToSingleNameMap(config));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static TreeDataReader createXmlReader(InputStream is, ImportOptions config) {
        try {
            return new XmlReader(is, getNamespaceVersionMap(), config.withNoExtension() ? Collections.emptyList() : EXTENSIONS_SUPPLIER.get().getProviders());
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static Map<String, String> createSingleNameToArrayNameMap(ExportOptions config) {
        return createArrayNameSingleNameBiMap(!config.withNoExtension()).inverse();
    }

    private static Map<String, String> createArrayNameToSingleNameMap(ImportOptions config) {
        return createArrayNameSingleNameBiMap(!config.withNoExtension());
    }

    private static BiMap<String, String> createArrayNameSingleNameBiMap(boolean withExtensions) {
        Map<String, String> basicMap = Map.ofEntries(
                Map.entry(NETWORK_ARRAY_ELEMENT_NAME, NETWORK_ROOT_ELEMENT_NAME),
                Map.entry(EXTENSION_ARRAY_ELEMENT_NAME, EXTENSION_ROOT_ELEMENT_NAME),
                Map.entry(AbstractSwitchSerDe.ARRAY_ELEMENT_NAME, AbstractSwitchSerDe.ROOT_ELEMENT_NAME),
                Map.entry(AbstractTransformerSerDe.STEP_ARRAY_ELEMENT_NAME, AbstractTransformerSerDe.STEP_ROOT_ELEMENT_NAME),
                Map.entry(AliasesSerDe.ARRAY_ELEMENT_NAME, AliasesSerDe.ROOT_ELEMENT_NAME),
                Map.entry(BatterySerDe.ARRAY_ELEMENT_NAME, BatterySerDe.ROOT_ELEMENT_NAME),
                Map.entry(BusSerDe.ARRAY_ELEMENT_NAME, BusSerDe.ROOT_ELEMENT_NAME),
                Map.entry(BusbarSectionSerDe.ARRAY_ELEMENT_NAME, BusbarSectionSerDe.ROOT_ELEMENT_NAME),
                Map.entry(ConnectableSerDeUtil.TEMPORARY_LIMITS_ARRAY_ELEMENT_NAME, ConnectableSerDeUtil.TEMPORARY_LIMITS_ROOT_ELEMENT_NAME),
                Map.entry(DanglingLineSerDe.ARRAY_ELEMENT_NAME, DanglingLineSerDe.ROOT_ELEMENT_NAME),
                Map.entry(GeneratorSerDe.ARRAY_ELEMENT_NAME, GeneratorSerDe.ROOT_ELEMENT_NAME),
                Map.entry(HvdcLineSerDe.ARRAY_ELEMENT_NAME, HvdcLineSerDe.ROOT_ELEMENT_NAME),
                Map.entry(LccConverterStationSerDe.ARRAY_ELEMENT_NAME, LccConverterStationSerDe.ROOT_ELEMENT_NAME),
                Map.entry(LineSerDe.ARRAY_ELEMENT_NAME, LineSerDe.ROOT_ELEMENT_NAME),
                Map.entry(LoadSerDe.ARRAY_ELEMENT_NAME, LoadSerDe.ROOT_ELEMENT_NAME),
                Map.entry(NodeBreakerViewInternalConnectionSerDe.ARRAY_ELEMENT_NAME, NodeBreakerViewInternalConnectionSerDe.ROOT_ELEMENT_NAME),
                Map.entry(OverloadManagementSystemSerDe.ARRAY_ELEMENT_NAME, OverloadManagementSystemSerDe.ROOT_ELEMENT_NAME),
                Map.entry(PropertiesSerDe.ARRAY_ELEMENT_NAME, PropertiesSerDe.ROOT_ELEMENT_NAME),
                Map.entry(ReactiveLimitsSerDe.POINT_ARRAY_ELEMENT_NAME, ReactiveLimitsSerDe.POINT_ROOT_ELEMENT_NAME),
                Map.entry(ShuntSerDe.ARRAY_ELEMENT_NAME, ShuntSerDe.ROOT_ELEMENT_NAME),
                Map.entry(ShuntSerDe.SECTION_ARRAY_ELEMENT_NAME, ShuntSerDe.SECTION_ROOT_ELEMENT_NAME),
                Map.entry(StaticVarCompensatorSerDe.ARRAY_ELEMENT_NAME, StaticVarCompensatorSerDe.ROOT_ELEMENT_NAME),
                Map.entry(SubstationSerDe.ARRAY_ELEMENT_NAME, SubstationSerDe.ROOT_ELEMENT_NAME),
                Map.entry(ThreeWindingsTransformerSerDe.ARRAY_ELEMENT_NAME, ThreeWindingsTransformerSerDe.ROOT_ELEMENT_NAME),
                Map.entry(TieLineSerDe.ARRAY_ELEMENT_NAME, TieLineSerDe.ROOT_ELEMENT_NAME),
                Map.entry(TwoWindingsTransformerSerDe.ARRAY_ELEMENT_NAME, TwoWindingsTransformerSerDe.ROOT_ELEMENT_NAME),
                Map.entry(VoltageAngleLimitSerDe.ARRAY_ELEMENT_NAME, VoltageAngleLimitSerDe.ROOT_ELEMENT_NAME),
                Map.entry(VoltageLevelSerDe.ARRAY_ELEMENT_NAME, VoltageLevelSerDe.ROOT_ELEMENT_NAME),
                Map.entry(VoltageLevelSerDe.INJ_ARRAY_ELEMENT_NAME, VoltageLevelSerDe.INJ_ROOT_ELEMENT_NAME),
                Map.entry(VscConverterStationSerDe.ARRAY_ELEMENT_NAME, VscConverterStationSerDe.ROOT_ELEMENT_NAME),
                Map.entry(GroundSerDe.ARRAY_ELEMENT_NAME, GroundSerDe.ROOT_ELEMENT_NAME));

        Map<String, String> extensionsMap = new HashMap<>();
        if (withExtensions) {
            for (ExtensionSerDe<?, ?> e : EXTENSIONS_SUPPLIER.get().getProviders()) {
                extensionsMap.putAll(e.getArrayNameToSingleNameMap());
            }
        }

        BiMap<String, String> biMergedMap = HashBiMap.create();
        biMergedMap.putAll(basicMap);
        biMergedMap.putAll(extensionsMap);
        return biMergedMap;
    }

    private static Map<String, String> getNamespaceVersionMap() {
        Map<String, String> namespaceVersionMap = new HashMap<>();
        Arrays.stream(IidmVersion.values())
                .forEach(v -> namespaceVersionMap.put(v.getNamespaceURI(), v.toString(".")));
        Arrays.stream(IidmVersion.values())
                .filter(IidmVersion::supportEquipmentValidationLevel)
                .forEach(v -> namespaceVersionMap.put(v.getNamespaceURI(false), v.toString(".")));
        return namespaceVersionMap;
    }

    private static void readNetworkElement(String elementName, Deque<Network> networks, NetworkFactory networkFactory, NetworkDeserializerContext context,
                                           Set<String> extensionNamesImported, Set<String> extensionNamesNotFound) {
        switch (elementName) {
            case AliasesSerDe.ROOT_ELEMENT_NAME -> checkSupportedAndReadAlias(networks.peek(), context);
            case PropertiesSerDe.ROOT_ELEMENT_NAME -> PropertiesSerDe.read(networks.peek(), context);
            case NETWORK_ROOT_ELEMENT_NAME -> checkSupportedAndReadSubnetwork(networks, networkFactory, context, extensionNamesImported, extensionNamesNotFound);
            case VoltageLevelSerDe.ROOT_ELEMENT_NAME -> checkSupportedAndReadVoltageLevel(context, networks);
            case SubstationSerDe.ROOT_ELEMENT_NAME -> SubstationSerDe.INSTANCE.read(networks.peek(), context);
            case LineSerDe.ROOT_ELEMENT_NAME -> LineSerDe.INSTANCE.read(networks.peek(), context);
            case TieLineSerDe.ROOT_ELEMENT_NAME -> TieLineSerDe.INSTANCE.read(networks.peek(), context);
            case HvdcLineSerDe.ROOT_ELEMENT_NAME -> HvdcLineSerDe.INSTANCE.read(networks.peek(), context);
            case VoltageAngleLimitSerDe.ROOT_ELEMENT_NAME -> VoltageAngleLimitSerDe.read(networks.peek(), context);
            case EXTENSION_ROOT_ELEMENT_NAME -> findExtendableAndReadExtension(networks.getFirst(), context, extensionNamesImported, extensionNamesNotFound);
            default -> throw new PowsyblException("Unknown element name '" + elementName + "' in 'network'");
        }
    }

    private static void checkSupportedAndReadAlias(Network network, NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, AliasesSerDe.ROOT_ELEMENT_NAME, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
        AliasesSerDe.read(network, context);
    }

    private static void checkSupportedAndReadSubnetwork(Deque<Network> networks, NetworkFactory networkFactory, NetworkDeserializerContext context,
                                                        Set<String> extensionNamesImported, Set<String> extensionNamesNotFound) {
        IidmSerDeUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, NETWORK_ROOT_ELEMENT_NAME,
                IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_11, context);
        if (networks.size() > 1) {
            throw new PowsyblException("Only one level of subnetworks is currently supported.");
        }
        // Create a new subnetwork and push it in the deque to be used as the network to update
        Network subnetwork = initNetwork(networkFactory, context, context.getReader(), networks.peek());
        networks.push(subnetwork);
        // Read subnetwork content
        context.getReader().readChildNodes(
                elementName -> readNetworkElement(elementName, networks, networkFactory, context, extensionNamesImported, extensionNamesNotFound));
        // Pop the subnetwork. We will now work with its parent.
        networks.pop();
    }

    private static void checkSupportedAndReadVoltageLevel(NetworkDeserializerContext context, Deque<Network> networks) {
        IidmSerDeUtil.assertMinimumVersion(NETWORK_ROOT_ELEMENT_NAME, VoltageLevelSerDe.ROOT_ELEMENT_NAME,
                IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_6, context);
        VoltageLevelSerDe.INSTANCE.read(networks.peek(), context);
    }

    private static void findExtendableAndReadExtension(Network network, NetworkDeserializerContext context, Set<String> extensionNamesImported, Set<String> extensionNamesNotFound) {
        String id2 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
        Identifiable identifiable = network.getIdentifiable(id2);
        if (identifiable == null) {
            throw new PowsyblException("Identifiable " + id2 + " not found");
        }
        readExtensions(identifiable, context, extensionNamesImported, extensionNamesNotFound);
    }

    private static Network initNetwork(NetworkFactory networkFactory, NetworkDeserializerContext context, TreeDataReader reader, Network rootNetwork) {
        String id = context.getAnonymizer().deanonymizeString(reader.readStringAttribute(ID));
        ZonedDateTime date = ZonedDateTime.parse(reader.readStringAttribute(CASE_DATE));
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
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_7, context, () -> minValidationLevel[0] = reader.readEnumAttribute(MINIMUM_VALIDATION_LEVEL, ValidationLevel.class));

        IidmSerDeUtil.assertMinimumVersionIfNotDefault(minValidationLevel[0] != ValidationLevel.STEADY_STATE_HYPOTHESIS, NETWORK_ROOT_ELEMENT_NAME, MINIMUM_VALIDATION_LEVEL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_7, context);
        network.setMinimumAcceptableValidationLevel(minValidationLevel[0]);
        return network;
    }

    private static void logExtensionsImported(Reporter reporter, Set<String> extensionNamesImported) {
        DeserializerReports.importedExtension(reporter, extensionNamesImported);
    }

    private static void logExtensionsNotFound(Reporter reporter, Set<String> extensionNamesNotFound) {
        DeserializerReports.extensionNotFound(reporter, extensionNamesNotFound);
    }

    public static Network read(TreeDataReader reader, ImportOptions config, Anonymizer anonymizer,
                               NetworkFactory networkFactory, Reporter reporter) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(networkFactory);
        Objects.requireNonNull(reporter);

        TreeDataHeader header = reader.readHeader();
        IidmVersion iidmVersion = IidmVersion.of(header.rootVersion(), ".");
        NetworkDeserializerContext context = new NetworkDeserializerContext(anonymizer, reader, config, iidmVersion, header.extensionVersions());

        Network network = initNetwork(networkFactory, context, reader, null);
        network.getReporterContext().pushReporter(reporter);

        Set<String> extensionNamesImported = new TreeSet<>();
        Set<String> extensionNamesNotFound = new TreeSet<>();
        Deque<Network> networks = new ArrayDeque<>(2);
        networks.push(network);

        Reporter validationReporter = reporter.createSubReporter("validationWarnings", "Validation warnings");
        reader.readChildNodes(elementName ->
                readNetworkElement(elementName, networks, networkFactory, context, extensionNamesImported, extensionNamesNotFound));

        if (!extensionNamesImported.isEmpty()) {
            Reporter importedExtensionReporter = reporter.createSubReporter("importedExtensions", "Imported extensions");
            logExtensionsImported(importedExtensionReporter, extensionNamesImported);
        }
        if (!extensionNamesNotFound.isEmpty()) {
            Reporter extensionsNotFoundReporter = reporter.createSubReporter("extensionsNotFound", "Not found extensions");
            throwExceptionIfOption(context.getOptions(), "Extensions " + extensionNamesNotFound + " " + "not found !");
            logExtensionsNotFound(extensionsNotFoundReporter, extensionNamesNotFound);
        }

        context.executeEndTasks(network, validationReporter);

        return network;
    }

    public static Network read(Path xmlFile) {
        return read(xmlFile, new ImportOptions());
    }

    public static Network read(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, ImportOptions options, String dataSourceExt, Reporter reporter) throws IOException {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(reporter);
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
            network = NetworkSerDe.read(isb, options, anonymizer, networkFactory, reporter);
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
        if (options.getFormat() == TreeDataFormat.XML) {
            validate(xmlFile);
        } else {
            LOGGER.warn("Non-XML file {} (format {}) could not be validated", xmlFile, options.getFormat());
        }
        return read(xmlFile, options);
    }

    public static Network validateAndRead(Path xmlFile) {
        return validateAndRead(xmlFile, new ImportOptions());
    }

    private static void readExtensions(Identifiable identifiable, NetworkDeserializerContext context,
                                       Set<String> extensionNamesImported, Set<String> extensionNamesNotFound) {

        context.getReader().readChildNodes(extensionName -> {
            // extensions root elements are nested directly in 'extension' element, so there is no need
            // to check for an extension to exist if depth is greater than zero. Furthermore in case of
            // missing extension serializer, we must not check for an extension in sub elements.
            if (!context.getOptions().withExtension(extensionName)) {
                context.getReader().skipChildNodes();
            }

            ExtensionSerDe extensionXmlSerializer = EXTENSIONS_SUPPLIER.get().findProvider(extensionName);
            if (extensionXmlSerializer != null) {
                Extension<? extends Identifiable<?>> extension = extensionXmlSerializer.read(identifiable, context);
                identifiable.addExtension(extensionXmlSerializer.getExtensionClass(), extension);
                extensionNamesImported.add(extensionName);
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
            return read(is, new ImportOptions(), null, networkFactory, Reporter.NO_OP);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
