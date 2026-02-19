/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.completion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImportPreProcessor;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.conversion.export.elements.*;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.extensions.CimCharacteristicsAdder;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.ZipArchiveDataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;

/**
 * <p>
 *     A CGMES pre-processor that defines missing containers in input data.
 * </p>
 *
 * <p>
 *     The pre-processor will analyze the input data and check if there are missing container definitions.
 *     It will then create a CIM-XML file with all required objects (voltage levels, substations, regions, ...)
 *     that will be used during CGMES import to allow the conversion to PowSyBl Network.
 * </p>
 *
 * <p>
 *     It is assumed that all containers missing are voltage levels.
 *     The user can specify the location folder of the output files using the parameter <code>iidm.import.cgmes.fixes-for-missing-containers-folder</code>.
 *     The CIM version of the output file will be the same detected for the input data.
 *     Because no information about voltage level is available, a default arbitrary value for nominal voltage is used.
 *     The user may edit the generated files and reuse them in successive imports.
 * </p>
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(CgmesImportPreProcessor.class)
public class CreateMissingContainersPreProcessor implements CgmesImportPreProcessor {

    public static final String NAME = "createMissingContainers";
    public static final String FIXES_FOLDER_NAME = "iidm.import.cgmes.fixes-for-missing-containers-folder";
    public static final double DEFAULT_NOMINAL_VALUE_FOR_MISSING_VOLTAGE_LEVELS = 1.2345;

    private static final Logger LOG = LoggerFactory.getLogger(CreateMissingContainersPreProcessor.class);
    private static final Parameter FIXES_FOLDER_NAME_PARAMETER = new Parameter(FIXES_FOLDER_NAME,
            ParameterType.STRING,
            "Folder where zip files containing fixes will be created: one zip for each imported network missing data",
            null);

    private final PlatformConfig platformConfig;
    private final ParameterDefaultValueConfig defaultValueConfig;

    public CreateMissingContainersPreProcessor(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        this.platformConfig = platformConfig;
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    public CreateMissingContainersPreProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    private static String nameFor(CgmesModel cgmes) {
        try {
            return new URI(cgmes.getBasename()).getAuthority();
        } catch (URISyntaxException e) {
            return cgmes.modelId();
        }
    }

    private static void prepareAndReadFixesUsingFolder(CgmesModel cgmes, String basename, Path fixesFolder) {
        if (!Files.isDirectory(fixesFolder)) {
            LOG.error("Output folder is not a directory {}. Skipping post processor.", fixesFolder);
            return;
        }
        Path fixesFile = fixesFolder.resolve(basename + ".zip");
        // Check the file will be writable
        try {
            Files.deleteIfExists(fixesFile);
            Files.createFile(fixesFile);
        } catch (IOException e) {
            LOG.error("Output file {} is not writable. Skipping post processor.", fixesFile);
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Execute {} pre processor on CGMES model {}. Output to file {}", NAME, cgmes.modelId(), fixesFile);
        }
        prepareAndReadFixesUsingZipFile(cgmes, basename, fixesFile);
    }

    private static void prepareAndReadFixesUsingZipFile(CgmesModel cgmes, String basename, Path fixesFile) {
        // Assume all containers missing are voltage levels and create proper objects for them (substation, regions, ...)
        Set<String> missingVoltageLevels = findMissingVoltageLevels(cgmes);
        LOG.info("Missing voltage levels: {}", missingVoltageLevels);
        if (!missingVoltageLevels.isEmpty()) {
            buildZipFileWithFixes(cgmes, missingVoltageLevels, fixesFile, basename);
            cgmes.read(new ZipArchiveDataSource(fixesFile), ReportNode.NO_OP);
        }
        Set<String> missingVoltageLevelsAfterFix = findMissingVoltageLevels(cgmes);
        if (!missingVoltageLevelsAfterFix.isEmpty()) {
            throw new IllegalStateException("Missing voltage levels after fix: " + missingVoltageLevelsAfterFix);
        }
        // The only containers without voltage level must be of type line
        LOG.info("After the fixes have been applied, the only node containers without voltage level must be of type Line.");
        LOG.info("Containers without voltage level that are not Lines will be reported as errors.");
        cgmes.connectivityNodeContainers().stream()
                .filter(c -> c.getId(CgmesNames.VOLTAGE_LEVEL) == null)
                .filter(c -> !c.getLocal("connectivityNodeContainerType").equals("Line"))
                .forEach(c -> LOG.error(c.getId(CgmesNames.CONNECTIVITY_NODE_CONTAINER)));
    }

    private static Set<String> findMissingVoltageLevels(CgmesModel cgmes) {
        // check missing CN containers
        Set<String> defined = cgmes.connectivityNodeContainers().stream().map(c -> c.getId(CgmesNames.CONNECTIVITY_NODE_CONTAINER)).collect(Collectors.toSet());
        Set<String> referred = cgmes.connectivityNodes().stream().map(c -> c.getId(CgmesNames.CONNECTIVITY_NODE_CONTAINER)).collect(Collectors.toSet());
        return referred.stream().filter(c -> !defined.contains(c)).collect(Collectors.toSet());
    }

    private static void buildZipFileWithFixes(CgmesModel cgmes, Set<String> missingVoltageLevels, Path fixesFile, String basename) {
        Network network = prepareEmptyNetworkForExport(cgmes);
        CgmesExportContext context = new CgmesExportContext(network);
        try (ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(fixesFile))) {
            zout.putNextEntry(new ZipEntry(basename + "_EQ.xml"));
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", zout);
            writeHeader(network, writer, context);
            RegionContainers regionContainers = writeRegionContainers(network, writer, context);
            for (String missingVoltageLevel : missingVoltageLevels) {
                writeMissingVoltageLevel(missingVoltageLevel, writer, context, regionContainers);
            }
            writer.writeEndDocument();
            zout.closeEntry();
        } catch (IOException | XMLStreamException x) {
            throw new PowsyblException("Building file containing fixes for missing data", x);
        }
    }

    private static Network prepareEmptyNetworkForExport(CgmesModel cgmes) {
        Network network = NetworkFactory.findDefault().createNetwork("empty", "CGMES");
        // We ensure that the fixes are exported to CGMES files with the same version of the input files
        // To achieve it, we set the CIM characteristics of the empty Network created
        if (cgmes instanceof CgmesModelTripleStore cgmesModelTripleStore) {
            network.newExtension(CimCharacteristicsAdder.class)
                    .setTopologyKind(cgmes.isNodeBreaker() ? CgmesTopologyKind.NODE_BREAKER : CgmesTopologyKind.BUS_BRANCH)
                    .setCimVersion(cgmesModelTripleStore.getCimVersion())
                    .add();
        }
        return network;
    }

    private static RegionContainers writeRegionContainers(Network network, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();

        // An alternative to replicate this code would be to make public the method
        // EquipmentExport::writeFictitiousSubstationFor and use it here.
        // We could group all missing voltage levels in the same (fictitious) substation
        RegionContainers regionContainers = new RegionContainers();
        regionContainers.subGeographicalRegionId = context.getNamingStrategy().getCgmesId(refTyped(network), ref("SubgeographicalRegionId"));
        String subGeographicalRegionName = "SGR fix for missing data";
        regionContainers.geographicalRegionId = context.getNamingStrategy().getCgmesId(refTyped(network), ref("GeographicalRegionId"));
        String geographicalRegionName = "GR fix for missing data";
        SubGeographicalRegionEq.write(regionContainers.subGeographicalRegionId, subGeographicalRegionName, regionContainers.geographicalRegionId, cimNamespace, writer, context);
        GeographicalRegionEq.write(regionContainers.geographicalRegionId, geographicalRegionName, cimNamespace, writer, context);
        return regionContainers;
    }

    private static void writeMissingVoltageLevel(String voltageLevelId, XMLStreamWriter writer, CgmesExportContext context, RegionContainers regionContainers) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();

        // In a first approach,
        // we do not have additional information about the voltage level,
        // we create a different substation and base voltage for every missing voltage level
        String voltageLevelName = voltageLevelId + " VL";
        String substationId = context.getNamingStrategy().getCgmesId(ref(voltageLevelId), ref("Substation"));
        String substationName = voltageLevelId + "SUB for missing VL " + voltageLevelId;
        String baseVoltageId = context.getNamingStrategy().getCgmesId(ref(voltageLevelId), ref("BaseVoltage"));

        VoltageLevelEq.write(voltageLevelId, voltageLevelName, Double.NaN, Double.NaN, substationId, baseVoltageId, cimNamespace, writer, context);
        SubstationEq.write(substationId, substationName, regionContainers.subGeographicalRegionId, cimNamespace, writer, context);
        BaseVoltageEq.write(baseVoltageId, DEFAULT_NOMINAL_VALUE_FOR_MISSING_VOLTAGE_LEVELS, cimNamespace, writer, context);
    }

    private static void writeHeader(Network network, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        String euNamespace = context.getCim().getEuNamespace();
        CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), euNamespace, writer);
        if (context.getCimVersion() >= 16) {
            CgmesMetadataModel eqModel = CgmesExport.initializeModelForExport(
                    network, CgmesSubset.EQUIPMENT, context, true, false);
            CgmesExportUtil.writeModelDescription(network, CgmesSubset.EQUIPMENT, writer, eqModel, context);
        }
    }

    private Path getFixesFolder() {
        String fixesFolderName = Parameter.readString("CGMES", null, FIXES_FOLDER_NAME_PARAMETER, defaultValueConfig);
        if (fixesFolderName == null) {
            LOG.error("Executing {} pre processor. Missing the folder name for the output of files containing required fixes. Use the parameter {}.", NAME, FIXES_FOLDER_NAME_PARAMETER.getName());
            return null;
        }
        Path fixesFolder = Paths.get(fixesFolderName);
        if (fixesFolder.isAbsolute()) {
            return fixesFolder;
        } else {
            Optional<Path> configDir = platformConfig.getConfigDir();
            if (configDir.isPresent()) {
                return configDir.get().resolve(fixesFolderName);
            } else {
                LOG.error("Executing {} pre processor. The folder name for the output of files containing required fixes is a relative path ({}), but the platform config dir is empty.", NAME, fixesFolderName);
                return null;
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(CgmesModel cgmes) {
        Objects.requireNonNull(cgmes);
        String basename = nameFor(cgmes);

        Path fixesFolder = getFixesFolder();
        if (fixesFolder != null) {
            prepareAndReadFixesUsingFolder(cgmes, basename, fixesFolder);
        }
    }

    private static final class RegionContainers {
        String subGeographicalRegionId;
        String geographicalRegionId;
    }
}
