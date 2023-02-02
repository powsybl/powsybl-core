/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.reporter.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(CgmesImportPreProcessor.class)
public class DefineMissingContainersPreProcessor implements CgmesImportPreProcessor {

    public static final String NAME = "DefineMissingContainers";
    private static final Logger LOG = LoggerFactory.getLogger(DefineMissingContainersPreProcessor.class);

    public DefineMissingContainersPreProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public DefineMissingContainersPreProcessor(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(CgmesModel cgmes) {
        Objects.requireNonNull(cgmes);
        LOG.info("Execute {} post processor on CGMES model {}", getName(), cgmes.modelId());
        fixMissingContainers(cgmes);
    }

    private static void fixMissingContainers(CgmesModel cgmes) {
        Set<String> missingVoltageLevels = findMissingVoltageLevels(cgmes);
        Path fixesFile = Paths.get("/Users/zamarrenolm/Downloads/").resolve("kk-EQ-fixes.xml");
        if (!missingVoltageLevels.isEmpty()) {
            // FIXME(Luma) buildXmlWithMissingVoltageLevels(missingVoltageLevels, fixesFile);
            ((CgmesModelTripleStore) cgmes).read(new FileDataSource(Paths.get("/Users/zamarrenolm/Downloads/"), "kk-EQ-fixes"), Reporter.NO_OP);
        }
        Set<String> missingVoltageLevelsAfterFix = findMissingVoltageLevels(cgmes);
        if (!missingVoltageLevelsAfterFix.isEmpty()) {
            throw new IllegalStateException("Missing voltage levels after fix: " + missingVoltageLevelsAfterFix);
        }
        System.err.println("containers without voltage level:");
        cgmes.connectivityNodeContainers().stream().filter(c -> c.getId("VoltageLevel") == null).forEach(System.err::println);

        // Check now that a terminal has voltage level
        String terminalId = "4915762d-133e-4209-8545-2822d095d7cd";
        String voltageLevelId = cgmes.voltageLevel(cgmes.terminal(terminalId), cgmes.isNodeBreaker());
        if (voltageLevelId == null || voltageLevelId.isEmpty()) {
            throw new IllegalStateException("Missing voltage level for terminal " + terminalId);
        }
    }

    private static Set<String> findMissingVoltageLevels(CgmesModel cgmes) {
        // check missing CN containers
        Set<String> defined = cgmes.connectivityNodeContainers().stream().map(c -> c.getId("ConnectivityNodeContainer")).collect(Collectors.toSet());
        System.err.println("defined  : " + defined);
        Set<String> referred = cgmes.connectivityNodes().stream().map(c -> c.getId("ConnectivityNodeContainer")).collect(Collectors.toSet());
        System.err.println("referred : " + referred);
        Set<String> missing = referred.stream().filter(c -> !defined.contains(c)).collect(Collectors.toSet());
        System.err.println("missing  : " + missing);
        return missing;
    }

    private static void buildXmlWithMissingVoltageLevels(Set<String> missingVoltageLevels, Path fixesFile) {
        // Assume all containers missing are voltage levels and create proper objects for them (substation, regions, ...)
        // FIXME(Luma) write to "debug"/reusable XML file that could be specified as a parameter ?
    }
}
