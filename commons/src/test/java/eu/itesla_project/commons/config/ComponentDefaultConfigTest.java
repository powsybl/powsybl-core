/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.config;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

interface A {
}

class B implements A {
}

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComponentDefaultConfigTest {

    private FileSystem fileSystem;
    private MapModuleConfig moduleConfig;
    private ComponentDefaultConfig config;

    @Before
    public void setUp() throws IOException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        moduleConfig = platformConfig.createModuleConfig("componentDefaultConfig");
        config = new ComponentDefaultConfig(moduleConfig);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void findFactoryImplClassTest() throws IOException {
        moduleConfig.setStringProperty(A.class.getSimpleName(), B.class.getName());
        assertEquals(B.class, config.findFactoryImplClass(A.class));
    }

    @Test
    public void findFactoryImplClassDefaultTest() throws IOException {
        assertEquals(B.class, config.findFactoryImplClass(A.class, B.class));
    }

    @Test
    public void newFactoryImplTest() throws IOException {
        moduleConfig.setStringProperty(A.class.getSimpleName(), B.class.getName());
        assertTrue(config.newFactoryImpl(A.class) instanceof B);
    }

    @Test
    public void newFactoryImplDefaultTest() throws IOException {
        assertTrue(config.newFactoryImpl(A.class, B.class) instanceof B);
    }
}
