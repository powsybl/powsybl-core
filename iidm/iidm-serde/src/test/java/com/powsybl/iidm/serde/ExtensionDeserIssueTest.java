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
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static com.powsybl.iidm.serde.AbstractTreeDataExporter.EXTENSIONS_FILTERED_LIST;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ExtensionDeserIssueTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        var load = network.getLoad("LOAD");
        var vlload = network.getVoltageLevel("VLLOAD");
        vlload.newExtension(SlackTerminalAdder.class)
                .withTerminal(load.getTerminal())
                .add();
        try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
            var file = fs.getPath("/work/test.xiidm");
            network.write("XIIDM", null, file);
            Network network2 = Network.read(file);
            var vlload2 = network2.getVoltageLevel("VLLOAD");
            var slackTerminal = vlload2.getExtension(SlackTerminal.class);
            assertNotNull(slackTerminal.getTerminal().getBusView().getBus());
        }
    }

    @Test
    void testIgnoreExtensionExport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        var load = network.getLoad("LOAD");
        var vlload = network.getVoltageLevel("VLLOAD");
        vlload.newExtension(SlackTerminalAdder.class)
                .withTerminal(load.getTerminal())
                .add();
        try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
            var file = fs.getPath("/work/test.xiidm");
            Properties exportParams = new Properties();
            exportParams.put(EXTENSIONS_FILTERED_LIST, "slackTerminal");
            network.write("XIIDM", exportParams, file);
            Network network2 = Network.read(file);
            var vlload2 = network2.getVoltageLevel("VLLOAD");
            var slackTerminal = vlload2.getExtension(SlackTerminal.class);
            assertNull(slackTerminal);
        }
    }

    @Test
    void testIgnoreExtensionImport() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        var load = network.getLoad("LOAD");
        var vlload = network.getVoltageLevel("VLLOAD");
        vlload.newExtension(SlackTerminalAdder.class)
                .withTerminal(load.getTerminal())
                .add();
        try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
            var file = fs.getPath("/work/test.xiidm");
            network.write("XIIDM", null, file);
            Properties importParams = new Properties();
            importParams.put(AbstractTreeDataImporter.EXTENSIONS_FILTERED_LIST, "slackTerminal");
            Network network2 = Network.read(file, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), importParams);
            var vlload2 = network2.getVoltageLevel("VLLOAD");
            var slackTerminal = vlload2.getExtension(SlackTerminal.class);
            assertNull(slackTerminal);
        }
    }

    @Test
    void testIgnoreExtensionImportExtensionAndFiltered() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        var load = network.getLoad("LOAD");
        var vlload = network.getVoltageLevel("VLLOAD");
        vlload.newExtension(SlackTerminalAdder.class)
                .withTerminal(load.getTerminal())
                .add();
        try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
            var file = fs.getPath("/work/test.xiidm");
            network.write("XIIDM", null, file);
            Properties importParams = new Properties();
            importParams.put(AbstractTreeDataImporter.EXTENSIONS_LIST, "identifiableShortCircuit,slackTerminal");
            // Filter has priority over positive import list
            importParams.put(AbstractTreeDataImporter.EXTENSIONS_FILTERED_LIST, "slackTerminal");
            Network network2 = Network.read(file, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), importParams);
            var vlload2 = network2.getVoltageLevel("VLLOAD");
            var slackTerminal = vlload2.getExtension(SlackTerminal.class);
            assertNull(slackTerminal);
        }
    }
}
