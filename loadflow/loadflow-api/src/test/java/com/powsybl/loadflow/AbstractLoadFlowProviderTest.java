/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class AbstractLoadFlowProviderTest {

    @Test
    void test() throws IOException {
        var provider = new AbstractLoadFlowProvider() {
            @Override
            public CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingVariantId, LoadFlowParameters parameters, Reporter reporter) {
                return null;
            }

            @Override
            public String getName() {
                return "test";
            }

            @Override
            public String getVersion() {
                return "1";
            }
        };
        assertTrue(provider.getSpecificParametersClass().isEmpty());
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            assertTrue(provider.loadSpecificParameters(new InMemoryPlatformConfig(fileSystem)).isEmpty());
        }
        assertTrue(provider.loadSpecificParameters(Collections.emptyMap()).isEmpty());
        assertTrue(provider.getSpecificParametersSerializer().isEmpty());
        assertTrue(provider.createMapFromSpecificParameters(new JsonLoadFlowParametersTest.DummyExtension()).isEmpty());
        provider.updateSpecificParameters(new JsonLoadFlowParametersTest.DummyExtension(), Map.of());
        assertTrue(provider.getSpecificParameters().isEmpty());
    }
}
