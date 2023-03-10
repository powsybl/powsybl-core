/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.completion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesImportPreProcessor;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.ZipFileDataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.reporter.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(CgmesImportPreProcessor.class)
public class DefineMissingContainersPreProcessor implements CgmesImportPreProcessor {

    public static final String NAME = "DefineMissingContainers";

    private static final Logger LOG = LoggerFactory.getLogger(DefineMissingContainersPreProcessor.class);

    public static final String FIXES_FOLDER_NAME = "iidm.import.cgmes.fixes-for-missing-containers-folder";

    private static final Parameter FIXES_FOLDER_NAME_PARAMETER = new Parameter(FIXES_FOLDER_NAME,
            ParameterType.STRING,
            "Folder where zip files containing fixes will be created: one zip for each imported network missing data",
            null);

    private final ParameterDefaultValueConfig defaultValueConfig;

    public DefineMissingContainersPreProcessor(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(Objects.requireNonNull(platformConfig));
    }

    public DefineMissingContainersPreProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(CgmesModel cgmes) {
        Objects.requireNonNull(cgmes);
        Path fixesFile = fixesFile(nameFor(cgmes));
        if (fixesFile == null) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Execute {} post processor on CGMES model {}. Output to file {}", getName(), cgmes.modelId(), fixesFile);
        }
        fixMissingContainers(cgmes, fixesFile);
    }

    private String nameFor(CgmesModel cgmes) {
        try {
            return new URI(cgmes.getBasename()).getAuthority();
        } catch (URISyntaxException e) {
            return cgmes.modelId();
        }
    }

    private Path fixesFile(String basename) {
        Path fixesFolder;
        String fixesFolderName = Parameter.readString("CGMES", null, FIXES_FOLDER_NAME_PARAMETER, defaultValueConfig);
        if (fixesFolderName != null) {
            fixesFolder = Path.of(fixesFolderName);
        } else {
            LOG.warn("Missing fixes folder parameter {}, a temp directory will be created.", FIXES_FOLDER_NAME_PARAMETER.getName());
            try {
                fixesFolder = Files.createTempDirectory("cgmes-fixes-for-missing-containers");
            } catch (IOException e) {
                LOG.warn("Failed to create a temp directory");
                return null;
            }
        }
        if (!Files.isDirectory(fixesFolder)) {
            LOG.error("Output folder is not a directory {}. Skipping post processor.", fixesFolder);
            return null;
        }
        Path fixesFile = fixesFolder.resolve(basename + ".zip");
        // Check the file will be writable
        try {
            Files.deleteIfExists(fixesFile);
            Files.createFile(fixesFile);
        } catch (IOException e) {
            LOG.error("Output file {} is not writable. Skipping post processor.", fixesFile);
            return null;
        }
        return fixesFile;
    }

    private static void fixMissingContainers(CgmesModel cgmes, Path fixesFile) {
        Set<String> missingVoltageLevels = findMissingVoltageLevels(cgmes);
        LOG.info("Missing voltage levels: {}", missingVoltageLevels);
        if (!missingVoltageLevels.isEmpty()) {
            buildXmlWithMissingVoltageLevels(missingVoltageLevels, fixesFile);
            cgmes.read(new ZipFileDataSource(fixesFile), Reporter.NO_OP);
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

    private static void buildXmlWithMissingVoltageLevels(Set<String> missingVoltageLevels, Path fixesFile) {
        // Assume all containers missing are voltage levels and create proper objects for them (substation, regions, ...)
        // FIXME(Luma) implement this
        fixmeCopyPreparedSolution(fixesFile);
    }

    private static void fixmeCopyPreparedSolution(Path fixesFile) {
        try {
            Files.copy(Path.of("/Users/zamarrenolm/Downloads/fixme-cgmes-completion-fixes.zip"), fixesFile, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
