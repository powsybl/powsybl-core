/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.exceptions.UncheckedNoSuchMethodException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DefaultComputationManagerConfigTest {

    static class FirstComputationManagerFactory implements ComputationManagerFactory {
        @Override
        public ComputationManager create() {
            return Mockito.mock(ComputationManager.class);
        }
    }

    static class SecondComputationManagerFactory implements ComputationManagerFactory {
        @Override
        public ComputationManager create() {
            return Mockito.mock(ComputationManager.class);
        }
    }

    static class ThirdComputationManagerFactory implements ComputationManagerFactory {
        ThirdComputationManagerFactory(String a, String b) {
        }

        @Override
        public ComputationManager create() {
            return Mockito.mock(ComputationManager.class);
        }
    }

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void test() {
        DefaultComputationManagerConfig config = new DefaultComputationManagerConfig(FirstComputationManagerFactory.class,
                                                                                     SecondComputationManagerFactory.class);
        assertEquals("DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=com.powsybl.computation.DefaultComputationManagerConfigTest$FirstComputationManagerFactory, longTimeExecutionComputationManagerFactoryClass=com.powsybl.computation.DefaultComputationManagerConfigTest$SecondComputationManagerFactory)", config.toString());
        assertNotNull(config.createShortTimeExecutionComputationManager());
        assertNotNull(config.createLongTimeExecutionComputationManager());

        config = new DefaultComputationManagerConfig(FirstComputationManagerFactory.class, null);
        assertEquals("DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=com.powsybl.computation.DefaultComputationManagerConfigTest$FirstComputationManagerFactory, longTimeExecutionComputationManagerFactoryClass=com.powsybl.computation.DefaultComputationManagerConfigTest$FirstComputationManagerFactory)", config.toString());
        assertNotNull(config.createShortTimeExecutionComputationManager());
        assertNotNull(config.createLongTimeExecutionComputationManager());

        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("default-computation-manager");
        moduleConfig.setClassProperty("short-time-execution-computation-manager-factory", FirstComputationManagerFactory.class);
        moduleConfig.setClassProperty("long-time-execution-computation-manager-factory", SecondComputationManagerFactory.class);
        config = DefaultComputationManagerConfig.load(platformConfig);
        assertEquals("DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=" + FirstComputationManagerFactory.class.getName()
                        + ", longTimeExecutionComputationManagerFactoryClass=" + SecondComputationManagerFactory.class.getName() + ")",
                config.toString());
    }

    @Test
    void testExceptions() {
        DefaultComputationManagerConfig config = new DefaultComputationManagerConfig(
            FirstComputationManagerFactory.class,
            ThirdComputationManagerFactory.class);
        assertThrows(UncheckedNoSuchMethodException.class, config::createLongTimeExecutionComputationManager);
    }
}
