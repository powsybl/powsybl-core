/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.naming.mock.MockNamingStrategy;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class MockNamingStrategyTest {

    @Test
    void testServiceLoaderAndFactoryWithMock() {
        NamingStrategiesServiceLoader loader = new NamingStrategiesServiceLoader();
        Optional<NamingStrategyProvider> found = loader.findProviderByName("mock");
        assertTrue(found.isPresent(), "Mock naming strategy provider should be discovered");

        UUID ns = UUID.nameUUIDFromBytes("powsybl.org".getBytes());
        NamingStrategy strategy = NamingStrategyFactory.create("mock", ns);
        assertEquals("mock", strategy.getName());
        assertInstanceOf(MockNamingStrategy.class, strategy);
    }

    @Test
    void testExportUsesMockNamingStrategy() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/mock-ns-test"));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "EQ");
            exportParams.put(CgmesExport.NAMING_STRATEGY, "mock");

            String baseName = "mockNaming";
            new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
            String eq = Files.readString(tmpDir.resolve(baseName + "_EQ.xml"));
            assertTrue(eq.contains("MOCK_"), "EQ export should contain IDs prefixed with 'MOCK_' when mock naming strategy is used");
        }
    }
}
