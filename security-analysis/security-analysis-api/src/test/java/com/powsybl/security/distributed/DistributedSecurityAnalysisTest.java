/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.computation.CommandExecution;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class DistributedSecurityAnalysisTest {

    private FileSystem fileSystem;
    private Path workingDir;
    private final ComputationManager cm = mock(ComputationManager.class);

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workingDir = fileSystem.getPath("/working-dir");
        Files.createDirectory(workingDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static ContingenciesProvider newContingenciesProvider() {
        return new ContingenciesProvider() {
            @Override
            public List<Contingency> getContingencies(Network network) {
                return IntStream.range(1, 6)
                        .mapToObj(i -> new Contingency("contingency-" + i))
                        .toList();
            }

            @Override
            public String asScript() {
                return "";
            }
        };
    }

    private void checkInvocationOnExecutionHandler(Path workingDir) throws IOException {
        //Capture the execution handler
        ArgumentCaptor<ExecutionHandler> capt = ArgumentCaptor.forClass(ExecutionHandler.class);
        verify(cm, times(1)).execute(any(), capt.capture());

        //checks methods of the execution handler
        List<CommandExecution> cmd = capt.getValue().before(workingDir);
        assertEquals(1, cmd.size());
        assertEquals(5, cmd.get(0).getExecutionCount());
    }

    private void checkWorkingDirContent() {
        assertTrue(Files.exists(workingDir.resolve("network.xiidm")));
        assertTrue(Files.exists(workingDir.resolve("contingencies.groovy")));
        assertTrue(Files.exists(workingDir.resolve("parameters.json")));
    }

    /**
     * Checks config class.
     */
    @Test
    void testConfig() {
        ExternalSecurityAnalysisConfig config = new ExternalSecurityAnalysisConfig();
        assertFalse(config.isDebug());
        assertEquals("itools", config.getItoolsCommand());

        config = new ExternalSecurityAnalysisConfig(true, "/path/to/itools");
        assertTrue(config.isDebug());
        assertEquals("/path/to/itools", config.getItoolsCommand());

        assertThatNullPointerException().isThrownBy(() -> new ExternalSecurityAnalysisConfig(true, null));

        try {
            new ExternalSecurityAnalysisConfig(true, "");
            fail();
        } catch (Exception ignored) {
        }
    }

    /**
     * Checks config class read from config file.
     */
    @Test
    void testConfigFromFile() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

            ExternalSecurityAnalysisConfig config = ExternalSecurityAnalysisConfig.load(platformConfig);
            assertFalse(config.isDebug());
            assertEquals("itools", config.getItoolsCommand());

            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("external-security-analysis-config");
            moduleConfig.setStringProperty("debug", "true");
            moduleConfig.setStringProperty("itools-command", "/path/to/itools");
            config = ExternalSecurityAnalysisConfig.load(platformConfig);
            assertTrue(config.isDebug());
            assertEquals("/path/to/itools", config.getItoolsCommand());
        }
    }
}
