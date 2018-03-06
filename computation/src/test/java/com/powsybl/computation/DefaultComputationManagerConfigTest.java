/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultComputationManagerConfigTest {

    public static class FirstComputationManagerFactory implements ComputationManagerFactory {
        @Override
        public ComputationManager create() {
            return Mockito.mock(ComputationManager.class);
        }
    }

    public static class SecondComputationManagerFactory implements ComputationManagerFactory {
        @Override
        public ComputationManager create() {
            return Mockito.mock(ComputationManager.class);
        }
    }

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() {
        DefaultComputationManagerConfig config = new DefaultComputationManagerConfig(FirstComputationManagerFactory.class,
                                                                                     SecondComputationManagerFactory.class);
        assertEquals("DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=com.powsybl.computation.DefaultComputationManagerConfigTest$FirstComputationManagerFactory, longTimeExecutionComputationManagerFactoryClass=com.powsybl.computation.DefaultComputationManagerConfigTest$SecondComputationManagerFactory)", config.toString());
        assertNotNull(config.createShortTimeExecutionComputationManager());
        assertNotNull(config.createLongTimeExecutionComputationManager());

        config = new DefaultComputationManagerConfig(FirstComputationManagerFactory.class, null);
        assertEquals("DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=com.powsybl.computation.DefaultComputationManagerConfigTest$FirstComputationManagerFactory)", config.toString());
        assertNotNull(config.createShortTimeExecutionComputationManager());
        assertNull(config.createLongTimeExecutionComputationManager());

        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("default-computation-manager");
        moduleConfig.setClassProperty("short-time-execution-computation-manager-factory", FirstComputationManagerFactory.class);
        moduleConfig.setClassProperty("long-time-execution-computation-manager-factory", SecondComputationManagerFactory.class);
        config = DefaultComputationManagerConfig.load(platformConfig);
        assertEquals("DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=" + FirstComputationManagerFactory.class.getName()
                        + ", longTimeExecutionComputationManagerFactoryClass=" + SecondComputationManagerFactory.class.getName() + ")",
                config.toString());
    }
}
