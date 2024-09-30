/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.cgmes.model.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.parameters.*;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.api.TripleStoreOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(Importer.class)
public class CgmesImport implements Importer {

    public enum FictitiousSwitchesCreationMode {
        ALWAYS,
        ALWAYS_EXCEPT_SWITCHES,
        NEVER
    }

    public enum SubnetworkDefinedBy {
        FILENAME,
        MODELING_AUTHORITY
    }

    public CgmesImport(PlatformConfig platformConfig, List<CgmesImportPreProcessor> preProcessors, List<CgmesImportPostProcessor> postProcessors) {
        this.platformConfig = platformConfig;
        this.defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
        this.preProcessors = Objects.requireNonNull(preProcessors).stream()
                .collect(Collectors.toMap(CgmesImportPreProcessor::getName, e -> e));
        this.postProcessors = Objects.requireNonNull(postProcessors).stream()
                .collect(Collectors.toMap(CgmesImportPostProcessor::getName, e -> e));
        String boundaryPath = platformConfig.getConfigDir()
                .map(dir -> dir.resolve(FORMAT).resolve("boundary"))
                .map(Path::toString)
                .orElse(null);
        // Boundary location parameter can not be static
        // because we want its default value
        // to depend on the received platformConfig
        boundaryLocationParameter = new Parameter(
                BOUNDARY_LOCATION,
                ParameterType.STRING,
                "The location of boundary files",
                boundaryPath,
                null,
                ParameterScope.TECHNICAL);
        preProcessorsParameter = new Parameter(
                PRE_PROCESSORS,
                ParameterType.STRING_LIST,
                "Pre processors",
                Collections.emptyList(),
                preProcessors.stream().map(CgmesImportPreProcessor::getName).collect(Collectors.toList()));
        postProcessorsParameter = new Parameter(
                POST_PROCESSORS,
                ParameterType.STRING_LIST,
                "Post processors",
                Collections.emptyList(),
                postProcessors.stream().map(CgmesImportPostProcessor::getName).collect(Collectors.toList()));
    }

    public CgmesImport(PlatformConfig platformConfig) {
        this(platformConfig,
                new ServiceLoaderCache<>(CgmesImportPreProcessor.class).getServices(),
                new ServiceLoaderCache<>(CgmesImportPostProcessor.class).getServices());
    }

    public CgmesImport(List<CgmesImportPreProcessor> preProcessors, List<CgmesImportPostProcessor> postProcessors) {
        this(PlatformConfig.defaultConfig(), preProcessors, postProcessors);
    }

    public CgmesImport() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> allParams = new ArrayList<>(STATIC_PARAMETERS);
        allParams.add(boundaryLocationParameter);
        allParams.add(postProcessorsParameter);
        return ConfiguredParameter.load(allParams, getFormat(), defaultValueConfig);
    }

    @Override
    public boolean exists(ReadOnlyDataSource ds) {
        CgmesOnDataSource cds = new CgmesOnDataSource(ds);
        try {
            if (cds.exists()) {
                return true;
            }
            // If we are configured to support CIM14,
            // check if there is this CIM14 data
            return IMPORT_CIM_14 && cds.existsCim14();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<String> getSupportedExtensions() {
        return List.of("xml");
    }

    @Override
    public Network importData(ReadOnlyDataSource ds, NetworkFactory networkFactory, Properties p, ReportNode reportNode) {
        Objects.requireNonNull(ds);
        Objects.requireNonNull(networkFactory);
        Objects.requireNonNull(reportNode);
        if (Parameter.readBoolean(getFormat(), p, IMPORT_CGM_WITH_SUBNETWORKS_PARAMETER, defaultValueConfig)) {
            SubnetworkDefinedBy separatingBy = SubnetworkDefinedBy.valueOf(Parameter.readString(getFormat(),
                            p, IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY_PARAMETER, defaultValueConfig));
            Set<ReadOnlyDataSource> dss = new MultipleGridModelChecker(ds).separate(separatingBy);
            if (dss.size() > 1) {
                return Network.merge(dss.stream()
                        .map(ds1 -> importData1(ds1, networkFactory, p, reportNode))
                        .toArray(Network[]::new));
            }
        }
        return importData1(ds, networkFactory, p, reportNode);
    }

    private Network importData1(ReadOnlyDataSource ds, NetworkFactory networkFactory, Properties p, ReportNode reportNode) {
        CgmesModel cgmes = readCgmes(ds, p, reportNode);
        ReportNode conversionReportNode = reportNode.newReportNode().withMessageTemplate("CGMESConversion", "Importing CGMES file(s)").add();
        return new Conversion(cgmes, config(p), activatedPreProcessors(p), activatedPostProcessors(p), networkFactory).convert(conversionReportNode);
    }

    static class FilteredReadOnlyDataSource implements ReadOnlyDataSource {
        private final ReadOnlyDataSource ds;
        private final Predicate<String> filter;

        FilteredReadOnlyDataSource(ReadOnlyDataSource ds, Predicate<String> filter) {
            this.ds = ds;
            this.filter = filter;
        }

        @Override
        public String getBaseName() {
            return ds.getBaseName();
        }

        @Override
        public boolean exists(String suffix, String ext) throws IOException {
            return ds.exists(suffix, ext) && filter.test(DataSourceUtil.getFileName(getBaseName(), suffix, ext));
        }

        @Override
        public boolean isDataExtension(String ext) {
            return ds.isDataExtension(ext);
        }

        @Override
        public boolean exists(String fileName) throws IOException {
            return ds.exists(fileName) && filter.test(fileName);
        }

        @Override
        public InputStream newInputStream(String suffix, String ext) throws IOException {
            if (filter.test(DataSourceUtil.getFileName(getBaseName(), suffix, ext))) {
                return ds.newInputStream(suffix, ext);
            }
            throw new IOException(DataSourceUtil.getFileName(getBaseName(), suffix, ext) + " not found");
        }

        @Override
        public InputStream newInputStream(String fileName) throws IOException {
            if (filter.test(fileName)) {
                return ds.newInputStream(fileName);
            }
            throw new IOException(fileName + " not found");
        }

        @Override
        public Set<String> listNames(String regex) throws IOException {
            return ds.listNames(regex).stream().filter(filter).collect(Collectors.toSet());
        }
    }

    static class MultipleGridModelChecker {
        private final ReadOnlyDataSource dataSource;
        private XMLInputFactory xmlInputFactory;

        MultipleGridModelChecker(ReadOnlyDataSource dataSource) {
            this.dataSource = dataSource;
        }

        Set<ReadOnlyDataSource> separate(SubnetworkDefinedBy separatingBy) {
            // If it is a CGM, create a filtered dataset for each IGM.
            // In the dataset for each IGM we must include:
            // - Its own files.
            // - The boundaries (we will read the boundaries multiple times, one for each IGM).
            // - Any other shared instance files (files that do not contain the name of any IGMs identified).
            // An example of shared file is the unique SV from a CGM solved case
            // Shared files will be also loaded multiple times, one for each IGM
            return switch (separatingBy) {
                case MODELING_AUTHORITY -> separateByModelingAuthority();
                case FILENAME -> separateByIgmName();
            };
        }

        private Set<ReadOnlyDataSource> separateByModelingAuthority() {
            xmlInputFactory = XMLInputFactory.newInstance();
            Map<String, List<String>> igmNames = new CgmesOnDataSource(dataSource).names().stream()
                    // We consider IGMs only the modeling authorities that have an EQ file
                    // The CGM SV should have the MA of the merging agent
                    .filter(CgmesSubset.EQUIPMENT::isValidName)
                    .map(name -> readModelingAuthority(name).map(ma -> Map.entry(ma, name)))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(List.of(e.getValue()))));
            if (!igmNames.isEmpty()) {
                LOGGER.info("IGM EQ files identified by Modeling Authority:");
                igmNames.forEach((k, v) -> LOGGER.info("  {} {}", k, v.get(0)));
            }
            // If we only have found one IGM there is no need to partition
            if (igmNames.size() == 1) {
                return Set.of(dataSource);
            }
            Set<String> shared = new HashSet<>();
            new CgmesOnDataSource(dataSource).names().stream()
                    // We read the modeling authorities present in the rest of instance files
                    // and mark the instance name as linked to an IGM or as shared
                    .filter(not(CgmesSubset.EQUIPMENT::isValidName))
                    .filter(not(MultipleGridModelChecker::isBoundary))
                    .forEach(name -> {
                        Optional<String> ma = readModelingAuthority(name);
                        if (ma.isPresent() && igmNames.containsKey(ma.get())) {
                            igmNames.get(ma.get()).add(name);
                        } else {
                            shared.add(name);
                        }
                    });
            // Build one data source for each IGM found
            if (!igmNames.isEmpty()) {
                LOGGER.info("IGM files identified by Modeling Authority:");
                igmNames.forEach((k, v) -> LOGGER.info("  {} {}", k, String.join(",", v)));
                if (!shared.isEmpty()) {
                    LOGGER.info("Shared files:");
                    shared.forEach(name -> LOGGER.info("  {}", name));
                }
                LOGGER.info("Boundaries:");
                try {
                    dataSource.listNames(".*").stream().filter(MultipleGridModelChecker::isBoundary).forEach(name -> LOGGER.info("  {}", name));
                } catch (IOException e) {
                    throw new PowsyblException(e);
                }
            }
            return igmNames.keySet().stream()
                    .map(ma -> new FilteredReadOnlyDataSource(dataSource,
                            name -> isBoundary(name) || igmNames.get(ma).contains(name) || shared.contains(name)))
                    .collect(Collectors.toSet());
        }

        private Optional<String> readModelingAuthority(String name) {
            String modellingAuthority = null;
            try (InputStream is = dataSource.newInputStream(name)) {
                XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(is);
                boolean stopReading = false;
                while (reader.hasNext() && !stopReading) {
                    int token = reader.next();
                    if (token == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(CgmesNames.MODELING_AUTHORITY_SET)) {
                        modellingAuthority = reader.getElementText();
                        stopReading = true;
                    } else if (token == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(CgmesNames.FULL_MODEL)) {
                        // Try to finish parsing the input file as soon as we can
                        // If we do not have found a modelling authority set inside the FullModel object, exit with unknown
                        stopReading = true;
                    }
                }
                reader.close();
            } catch (IOException | XMLStreamException e) {
                throw new PowsyblException(e);
            }
            return Optional.ofNullable(modellingAuthority);
        }

        private Set<ReadOnlyDataSource> separateByIgmName() {
            // Here we obtain the IGM name from the EQ filenames,
            // and rely on it to find related SSH, TP files,
            Set<String> igmNames = new CgmesOnDataSource(dataSource).names().stream()
                    .filter(CgmesSubset.EQUIPMENT::isValidName)
                    // We rely on the CIMXML pattern:
                    // <effectiveDateTime>_<businessProcess>_<sourcingActor>_<modelPart>_<fileVersion>
                    // we define igmName := sourcingActor
                    .map(name -> name.split("_")[2])
                    .collect(Collectors.toSet());
            return igmNames.stream()
                    .map(igmName -> new FilteredReadOnlyDataSource(dataSource, name -> name.contains(igmName)
                            || isBoundary(name)
                            || isShared(name, igmNames)))
                    .collect(Collectors.toSet());
        }

        private static boolean isBoundary(String name) {
            return CgmesSubset.EQUIPMENT_BOUNDARY.isValidName(name) || CgmesSubset.TOPOLOGY_BOUNDARY.isValidName(name);
        }

        private static boolean isShared(String name, Set<String> allIgmNames) {
            // The name does not contain the name of one the IGMs
            return allIgmNames.stream()
                    .filter(name::contains)
                    .findAny()
                    .isEmpty();
        }
    }

    public CgmesModel readCgmes(ReadOnlyDataSource ds, Properties p, ReportNode reportNode) {
        TripleStoreOptions options = new TripleStoreOptions();
        String sourceForIidmIds = Parameter.readString(getFormat(), p, SOURCE_FOR_IIDM_ID_PARAMETER, defaultValueConfig);
        if (sourceForIidmIds.equalsIgnoreCase(SOURCE_FOR_IIDM_ID_MRID)) {
            options.setRemoveInitialUnderscoreForIdentifiers(true);
        } else if (sourceForIidmIds.equalsIgnoreCase(SOURCE_FOR_IIDM_ID_RDFID)) {
            options.setRemoveInitialUnderscoreForIdentifiers(false);
        }
        options.decodeEscapedIdentifiers(Parameter.readBoolean(getFormat(), p, DECODE_ESCAPED_IDENTIFIERS_PARAMETER, defaultValueConfig));
        ReportNode tripleStoreReportNode = reportNode.newReportNode().withMessageTemplate("CGMESTriplestore", "Reading CGMES Triplestore").add();
        return CgmesModelFactory.create(ds, boundary(p), tripleStore(p), tripleStoreReportNode, options);
    }

    @Override
    public void copy(ReadOnlyDataSource from, DataSource to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        try {
            CgmesOnDataSource fromCgmes = new CgmesOnDataSource(from);
            for (String name : fromCgmes.names()) {
                copyStream(from, to, name, name);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ReadOnlyDataSource boundary(Properties p) {
        String location = Parameter.readString(
                getFormat(),
                p,
                boundaryLocationParameter,
                defaultValueConfig);
        if (location == null) {
            return null;
        }
        Path path = boundaryPath(location);
        if (path == null) {
            return null;
        }
        // Check that the Data Source has valid CGMES names
        ReadOnlyDataSource ds = new GenericReadOnlyDataSource(path);
        if ((new CgmesOnDataSource(ds)).names().isEmpty()) {
            return null;
        }
        return ds;
    }

    Path boundaryPath(String location) {
        Path path = null;
        // Check first if the location is present in the file system defined from the platform configuration
        Optional<Path> configDir = this.platformConfig.getConfigDir();
        if (configDir.isPresent()) {
            FileSystem configFileSystem = configDir.get().getFileSystem();
            if (configFileSystem != FileSystems.getDefault()) {
                path = configFileSystem.getPath(location);
                if (!Files.exists(path)) {
                    LOGGER.warn("Location of boundaries ({}) not found in config file system. An attempt to load boundaries from the default file system will be made", location);
                    path = null;
                }
            }
        }
        if (path == null) {
            path = Path.of(location);
            if (!Files.exists(path)) {
                LOGGER.warn("Location of boundaries ({}) not found in default file system. No attempt to load boundaries will be made", location);
            }
        }
        return path;
    }

    private String tripleStore(Properties p) {
        return Parameter.readString(
                getFormat(),
                p,
                POWSYBL_TRIPLESTORE_PARAMETER,
                defaultValueConfig);
    }

    private Conversion.Config config(Properties p) {
        Conversion.Config config = new Conversion.Config()
                .setConvertBoundary(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CONVERT_BOUNDARY_PARAMETER,
                                defaultValueConfig))
                .setConvertSvInjections(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CONVERT_SV_INJECTIONS_PARAMETER,
                                defaultValueConfig))
                .setCreateBusbarSectionForEveryConnectivityNode(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER,
                                defaultValueConfig))
                .setEnsureIdAliasUnicity(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                ENSURE_ID_ALIAS_UNICITY_PARAMETER,
                                defaultValueConfig))
                .setImportControlAreas(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                IMPORT_CONTROL_AREAS_PARAMETER,
                                defaultValueConfig))
                .setProfileForInitialValuesShuntSectionsTapPositions(
                        Parameter.readString(
                                getFormat(),
                                p,
                                PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS_PARAMETER,
                                defaultValueConfig))
                .setStoreCgmesModelAsNetworkExtension(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .setStoreCgmesConversionContextAsNetworkExtension(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .setCreateActivePowerControlExtension(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CREATE_ACTIVE_POWER_CONTROL_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .createFictitiousSwitchesForDisconnectedTerminalsMode(FictitiousSwitchesCreationMode.valueOf(
                        Parameter.readString(
                                getFormat(),
                                p,
                                CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE_PARAMETER,
                                defaultValueConfig)))
                .setImportNodeBreakerAsBusBreaker(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                IMPORT_NODE_BREAKER_AS_BUS_BREAKER_PARAMETER,
                                defaultValueConfig))
                .setDisconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED_PARAMETER,
                                defaultValueConfig))
                .setMissingPermanentLimitPercentage(
                        Parameter.readDouble(
                                getFormat(),
                                p,
                                MISSING_PERMANENT_LIMIT_PERCENTAGE_PARAMETER,
                                defaultValueConfig))
                .setCreateFictitiousVoltageLevelsForEveryNode(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE_PARAMETER,
                                defaultValueConfig));

        String namingStrategy = Parameter.readString(getFormat(), p, NAMING_STRATEGY_PARAMETER, defaultValueConfig);

        // Build the naming strategy with the default uuid namespace for creating name-based uuids
        // In fact, when using a naming strategy for CGMES import we should not need an uuid namespace,
        // because we won't be creating new UUIDs
        UUID uuidNamespace = CgmesExportContext.DEFAULT_UUID_NAMESPACE;
        config.setNamingStrategy(NamingStrategyFactory.create(namingStrategy, uuidNamespace));
        return config;
    }

    private List<CgmesImportPreProcessor> activatedPreProcessors(Properties p) {
        return Parameter
                .readStringList(getFormat(), p, preProcessorsParameter, defaultValueConfig)
                .stream()
                .filter(name -> {
                    boolean found = preProcessors.containsKey(name);
                    if (!found) {
                        LOGGER.warn("CGMES pre processor {} not found", name);
                    }
                    return found;
                })
                .map(preProcessors::get)
                .collect(Collectors.toList());
    }

    private List<CgmesImportPostProcessor> activatedPostProcessors(Properties p) {
        return Parameter
                .readStringList(getFormat(), p, postProcessorsParameter, defaultValueConfig)
                .stream()
                .filter(name -> {
                    boolean found = postProcessors.containsKey(name);
                    if (!found) {
                        LOGGER.warn("CGMES post processor {} not found", name);
                    }
                    return found;
                })
                .map(postProcessors::get)
                .collect(Collectors.toList());
    }

    private void copyStream(ReadOnlyDataSource from, DataSource to, String fromName, String toName) throws IOException {
        if (from.exists(fromName)) {
            try (InputStream is = from.newInputStream(fromName);
                 OutputStream os = to.newOutputStream(toName, false)) {
                ByteStreams.copy(is, os);
            }
        }
    }

    private static final String FORMAT = "CGMES";

    public static final String BOUNDARY_LOCATION = "iidm.import.cgmes.boundary-location";
    public static final String CONVERT_BOUNDARY = "iidm.import.cgmes.convert-boundary";
    public static final String CONVERT_SV_INJECTIONS = "iidm.import.cgmes.convert-sv-injections";
    public static final String CREATE_ACTIVE_POWER_CONTROL_EXTENSION = "iidm.import.cgmes.create-active-power-control-extension";
    public static final String CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE = "iidm.import.cgmes.create-busbar-section-for-every-connectivity-node";
    public static final String CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE = "iidm.import.cgmes.create-fictitious-switches-for-disconnected-terminals-mode";
    public static final String DECODE_ESCAPED_IDENTIFIERS = "iidm.import.cgmes.decode-escaped-identifiers";
    public static final String ENSURE_ID_ALIAS_UNICITY = "iidm.import.cgmes.ensure-id-alias-unicity";
    public static final String IMPORT_CONTROL_AREAS = "iidm.import.cgmes.import-control-areas";
    public static final String NAMING_STRATEGY = "iidm.import.cgmes.naming-strategy";
    public static final String PRE_PROCESSORS = "iidm.import.cgmes.pre-processors";
    public static final String POST_PROCESSORS = "iidm.import.cgmes.post-processors";
    public static final String POWSYBL_TRIPLESTORE = "iidm.import.cgmes.powsybl-triplestore";
    public static final String PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS = "iidm.import.cgmes.profile-for-initial-values-shunt-sections-tap-positions";
    public static final String SOURCE_FOR_IIDM_ID = "iidm.import.cgmes.source-for-iidm-id";
    public static final String STORE_CGMES_MODEL_AS_NETWORK_EXTENSION = "iidm.import.cgmes.store-cgmes-model-as-network-extension";
    public static final String STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION = "iidm.import.cgmes.store-cgmes-conversion-context-as-network-extension";
    public static final String IMPORT_NODE_BREAKER_AS_BUS_BREAKER = "iidm.import.cgmes.import-node-breaker-as-bus-breaker";
    public static final String DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED = "iidm.import.cgmes.disconnect-dangling-line-if-boundary-side-is-disconnected";
    public static final String MISSING_PERMANENT_LIMIT_PERCENTAGE = "iidm.import.cgmes.missing-permanent-limit-percentage";
    public static final String IMPORT_CGM_WITH_SUBNETWORKS = "iidm.import.cgmes.cgm-with-subnetworks";
    public static final String IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY = "iidm.import.cgmes.cgm-with-subnetworks-defined-by";
    public static final String CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE = "iidm.import.cgmes.create-fictitious-voltage-level-for-every-node";

    public static final String SOURCE_FOR_IIDM_ID_MRID = "mRID";
    public static final String SOURCE_FOR_IIDM_ID_RDFID = "rdfID";

    private static final Parameter CONVERT_BOUNDARY_PARAMETER = new Parameter(
            CONVERT_BOUNDARY,
            ParameterType.BOOLEAN,
            "Convert boundary during import",
            Boolean.FALSE)
            .addAdditionalNames("convertBoundary");
    private static final Parameter CONVERT_SV_INJECTIONS_PARAMETER = new Parameter(
            CONVERT_SV_INJECTIONS,
            ParameterType.BOOLEAN,
            "Convert SV injections during import",
            Boolean.TRUE);
    private static final Parameter CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER = new Parameter(
            CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE,
            ParameterType.BOOLEAN,
            "Create busbar section for every connectivity node",
            Boolean.FALSE)
            .addAdditionalNames("createBusbarSectionForEveryConnectivityNode");
    private static final Parameter ENSURE_ID_ALIAS_UNICITY_PARAMETER = new Parameter(
            ENSURE_ID_ALIAS_UNICITY,
            ParameterType.BOOLEAN,
            "Ensure IDs and aliases are unique",
            Boolean.FALSE);
    private static final Parameter NAMING_STRATEGY_PARAMETER = new Parameter(
            NAMING_STRATEGY,
            ParameterType.STRING,
            "Configure what type of naming strategy you want to use",
            NamingStrategyFactory.IDENTITY,
            new ArrayList<>(NamingStrategyFactory.LIST));
    private static final Parameter IMPORT_CONTROL_AREAS_PARAMETER = new Parameter(
            IMPORT_CONTROL_AREAS,
            ParameterType.BOOLEAN,
            "Import control areas",
            Boolean.TRUE);
    private static final Parameter POWSYBL_TRIPLESTORE_PARAMETER = new Parameter(
            POWSYBL_TRIPLESTORE,
            ParameterType.STRING,
            "The triplestore used during the import",
            TripleStoreFactory.defaultImplementation(),
            null,
            ParameterScope.TECHNICAL)
            .addAdditionalNames("powsyblTripleStore");
    private static final Parameter PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS_PARAMETER = new Parameter(
            PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS,
            ParameterType.STRING,
            "Profile used for initial state values",
            "SSH",
            List.of("SSH", "SV"))
            .addAdditionalNames("iidm.import.cgmes.profile-used-for-initial-state-values");
    private static final Parameter STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER = new Parameter(
            STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION,
            ParameterType.BOOLEAN,
            "Store the CGMES-IIDM terminal mapping as a network extension",
            Boolean.FALSE);
    private static final Parameter CREATE_ACTIVE_POWER_CONTROL_EXTENSION_PARAMETER = new Parameter(
            CREATE_ACTIVE_POWER_CONTROL_EXTENSION,
            ParameterType.BOOLEAN,
            "Create active power control extension during import",
            Boolean.TRUE);
    private static final Parameter CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE_PARAMETER = new Parameter(
            CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE,
            ParameterType.STRING,
            "Defines in which case fictitious switches for disconnected terminals are created (relevant for node-breaker models only): always, always except for switches or never",
            FictitiousSwitchesCreationMode.ALWAYS.name(),
            Arrays.stream(FictitiousSwitchesCreationMode.values()).map(Enum::name).collect(Collectors.toList()));
    private static final Parameter STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER = new Parameter(
            STORE_CGMES_MODEL_AS_NETWORK_EXTENSION,
            ParameterType.BOOLEAN,
            "Store the initial CGMES model as a network extension",
            Boolean.TRUE)
            .addAdditionalNames("storeCgmesModelAsNetworkExtension");
    private static final Parameter SOURCE_FOR_IIDM_ID_PARAMETER = new Parameter(
            SOURCE_FOR_IIDM_ID,
            ParameterType.STRING,
            "Source for IIDM identifiers",
            SOURCE_FOR_IIDM_ID_MRID,
            List.of(SOURCE_FOR_IIDM_ID_MRID, SOURCE_FOR_IIDM_ID_RDFID));
    private static final Parameter DECODE_ESCAPED_IDENTIFIERS_PARAMETER = new Parameter(
            DECODE_ESCAPED_IDENTIFIERS,
            ParameterType.BOOLEAN,
            "Decode escaped special characters in IDs",
            Boolean.TRUE);
    public static final Parameter IMPORT_NODE_BREAKER_AS_BUS_BREAKER_PARAMETER = new Parameter(
            IMPORT_NODE_BREAKER_AS_BUS_BREAKER,
            ParameterType.BOOLEAN,
            "Force import of CGMES node/breaker models as bus/breaker",
            Boolean.FALSE);
    public static final Parameter DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED_PARAMETER = new Parameter(
            DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED,
            ParameterType.BOOLEAN,
            "Force disconnection of dangling line network side if boundary side is disconnected",
            Boolean.TRUE);
    private static final Parameter IMPORT_CGM_WITH_SUBNETWORKS_PARAMETER = new Parameter(
            IMPORT_CGM_WITH_SUBNETWORKS,
            ParameterType.BOOLEAN,
            "Import CGM with subnetworks",
            Boolean.TRUE);
    private static final Parameter IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY_PARAMETER = new Parameter(
            IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY,
            ParameterType.STRING,
            "Choose how subnetworks from CGM must be imported: defined by filenames or by modeling authority",
            SubnetworkDefinedBy.MODELING_AUTHORITY.name(),
            Arrays.stream(SubnetworkDefinedBy.values()).map(Enum::name).collect(Collectors.toList()));

    public static final Parameter MISSING_PERMANENT_LIMIT_PERCENTAGE_PARAMETER = new Parameter(
            MISSING_PERMANENT_LIMIT_PERCENTAGE,
            ParameterType.DOUBLE,
            "Percentage applied to lowest TATL limit to use as PATL when PATL is missing",
            100.);

    private static final Parameter CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE_PARAMETER = new Parameter(
            CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE,
            ParameterType.BOOLEAN,
            "Create fictitious voltage level for every node",
            Boolean.TRUE)
            .addAdditionalNames("createFictitiousVoltageLevelForEveryNode");

    private static final List<Parameter> STATIC_PARAMETERS = List.of(
            CONVERT_BOUNDARY_PARAMETER,
            CONVERT_SV_INJECTIONS_PARAMETER,
            CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER,
            ENSURE_ID_ALIAS_UNICITY_PARAMETER,
            NAMING_STRATEGY_PARAMETER,
            IMPORT_CONTROL_AREAS_PARAMETER,
            POWSYBL_TRIPLESTORE_PARAMETER,
            PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS_PARAMETER,
            SOURCE_FOR_IIDM_ID_PARAMETER,
            STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER,
            STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER,
            CREATE_ACTIVE_POWER_CONTROL_EXTENSION_PARAMETER,
            DECODE_ESCAPED_IDENTIFIERS_PARAMETER,
            CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE_PARAMETER,
            IMPORT_NODE_BREAKER_AS_BUS_BREAKER_PARAMETER,
            DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED_PARAMETER,
            IMPORT_CGM_WITH_SUBNETWORKS_PARAMETER,
            IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY_PARAMETER,
            MISSING_PERMANENT_LIMIT_PERCENTAGE_PARAMETER,
            CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE_PARAMETER);

    private final Parameter boundaryLocationParameter;
    private final Parameter preProcessorsParameter;
    private final Parameter postProcessorsParameter;
    private final Map<String, CgmesImportPostProcessor> postProcessors;
    private final Map<String, CgmesImportPreProcessor> preProcessors;
    private final ParameterDefaultValueConfig defaultValueConfig;

    private final PlatformConfig platformConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesImport.class);

    // TODO Allow this property to be configurable
    // Parameters of importers are only passed to importData method,
    // but to decide if we are importers also for CIM 14 files
    // we must implement the exists method, that has not access to parameters
    private static final boolean IMPORT_CIM_14 = false;
}
