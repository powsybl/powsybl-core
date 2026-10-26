/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ExtensionDeserIssueTest {

    public static final String XIIDM = "XIIDM";
    public static final String VLLOAD = "VLLOAD";
    public static final String SLACK_TERMINAL = "slackTerminal";

    private Network network;
    private FileSystem fs;
    private Path file;

    /**
     * Sets up the common test environment:
     * <pre>
     * 1. Creates the base Network.
     * 2. Adds the SlackTerminal extension.
     * 3. Creates the Jimfs filesystem and the test file path.
     * </pre>
     */
    @BeforeEach
    void setup() {
        // 1. Create the base Network
        network = EurostagTutorialExample1Factory.create();
        var load = network.getLoad("LOAD");
        var vlload = network.getVoltageLevel(VLLOAD);

        // 2. Add the SlackTerminal extension
        vlload.newExtension(SlackTerminalAdder.class)
                .withTerminal(load.getTerminal())
                .add();

        // 3. Create the Jimfs filesystem and the test file path
        fs = Jimfs.newFileSystem(Configuration.unix());
        // Use a consistent file name for both tests
        file = fs.getPath("/work/test.xiidm");
    }

    /**
     * Cleans up the Jimfs filesystem after each test.
     */
    @AfterEach
    void teardown() throws IOException {
        if (fs != null) {
            fs.close();
        }
    }

    @Test
    void test() {
        // 4. Write the network to the temporary XIIDM file without any parameters
        network.write(XIIDM, null, file);
        Network network2 = Network.read(file);
        var vlload2 = network2.getVoltageLevel(VLLOAD);
        var slackTerminal = vlload2.getExtension(SlackTerminal.class);
        assertNotNull(slackTerminal.getTerminal().getBusView().getBus());
    }

    @Test
    void testIgnoreExtensionExport() {
        Properties exportParams = new Properties();
        exportParams.put(AbstractTreeDataExporter.EXTENSIONS_EXCLUDED_LIST, SLACK_TERMINAL);
        network.write(XIIDM, exportParams, file);
        Network network2 = Network.read(file);
        var vlload2 = network2.getVoltageLevel(VLLOAD);
        var slackTerminal = vlload2.getExtension(SlackTerminal.class);
        assertNull(slackTerminal);
    }

    @Test
    void testIgnoreExtensionImport() {
        network.write(XIIDM, null, file);
        Properties importParams = new Properties();
        importParams.put(AbstractTreeDataImporter.EXTENSIONS_EXCLUDED_LIST, SLACK_TERMINAL);
        Network network2 = Network.read(file, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), importParams);
        var vlload2 = network2.getVoltageLevel(VLLOAD);
        var slackTerminal = vlload2.getExtension(SlackTerminal.class);
        assertNull(slackTerminal);
    }

    @Test
    void testIgnoreExtensionImportExtensionAndFiltered() {
        network.write(XIIDM, null, file);
        Properties importParams = new Properties();
        importParams.put(AbstractTreeDataImporter.EXTENSIONS_INCLUDED_LIST, "identifiableShortCircuit");
        importParams.put(AbstractTreeDataImporter.EXTENSIONS_EXCLUDED_LIST, SLACK_TERMINAL);
        ComputationManager localComputationManager = LocalComputationManager.getDefault();
        ImportConfig importConfig = ImportConfig.CACHE.get();
        assertThrows(ConfigurationException.class, () -> {
            Network.read(file, localComputationManager, importConfig, importParams);
        });
    }

    @Test
    void testExceptionWhenBothInclusionExclusionParametersAreDefined() {
        Properties exportParams = new Properties();
        exportParams.put(AbstractTreeDataExporter.EXTENSIONS_EXCLUDED_LIST, SLACK_TERMINAL);
        exportParams.put(AbstractTreeDataExporter.EXTENSIONS_INCLUDED_LIST, "loadBar");
        assertThrows(ConfigurationException.class, () -> {
            network.write(XIIDM, exportParams, file);
        });
    }
}
