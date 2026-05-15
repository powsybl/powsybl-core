/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertXmlEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class SkipExtensionTest extends AbstractIidmSerDeTest {

    @Test
    void testSkipExtensionExport() throws IOException {
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("multiple-extensions.xml", IidmVersion.V_1_0));

        Properties properties = new Properties();
        properties.put(XMLExporter.EXTENSIONS_INCLUDED_LIST, "");
        properties.put(XMLExporter.VERSION, "1.0");

        // Write the file
        Path networkFile = tmpDir.resolve("noExtension.xiidm");
        network.write("XIIDM", properties, networkFile);

        // Compare
        assertXmlEquals(getVersionedNetworkAsStream("noExtension.xml", IidmVersion.V_1_0), Files.newInputStream(networkFile));
    }

    @Test
    void testSkipExtensionImport() throws IOException {
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
            importParams.put(AbstractTreeDataImporter.EXTENSIONS_INCLUDED_LIST, "");

            Network network2 = Network.read(file, LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), importParams);
            var vlload2 = network2.getVoltageLevel("VLLOAD");
            var slackTerminal = vlload2.getExtension(SlackTerminal.class);
            assertNull(slackTerminal);
        }
    }
}
