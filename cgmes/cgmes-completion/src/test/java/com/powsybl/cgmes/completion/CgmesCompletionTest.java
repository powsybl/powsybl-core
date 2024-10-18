/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.completion;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesCompletionTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void miniGridNodeBreakerMissingVoltageLevel() throws IOException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.miniGridNodeBreakerMissingVoltageLevel().dataSource();
        Properties importParams = new Properties();
        importParams.put(CgmesImport.PRE_PROCESSORS, "createMissingContainers");

        // The only way to pass the output folder where we want the fixes to be written is to use a config file
        // Its contents must be:
        //
        //   import-export-parameters-default-value:
        //     iidm.import.cgmes.fixes-for-missing-containers-folder: "/user/working/area/fixes/..."
        //
        // When the folder is relative path,
        // we assume in the preprocessor that it is relative to the given PlatformConfig dir
        // To let the test platformConfig arrive at the preprocessor through the importer mechanism
        // we have to import data using explicitly a computationManager
        Network network;
        try (ComputationManager computationManager = new LocalComputationManager(platformConfig)) {
            network = Importers.importData("CGMES", dataSource, importParams, computationManager);
        }
        assertNotNull(network);

        // Check that a specific terminal has a voltage level, navigating the switches
        String terminalId = "4915762d-133e-4209-8545-2822d095d7cd";
        Switch sw = network.getSwitch(terminalId);
        if (sw == null || sw.getVoltageLevel().getId() == null) {
            fail("Missing voltage level for terminal " + terminalId);
        }
    }
}
