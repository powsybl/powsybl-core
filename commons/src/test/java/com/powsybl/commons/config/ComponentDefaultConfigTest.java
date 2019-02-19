/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    private FileSystem fileSystem;
    private MapModuleConfig moduleConfig;
    private ComponentDefaultConfig config;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        moduleConfig = platformConfig.createModuleConfig("componentDefaultConfig");
        config = new ComponentDefaultConfig.Impl(moduleConfig);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void findFactoryImplClassTest() {
        moduleConfig.setClassProperty(A.class.getSimpleName(), B.class);
        assertEquals(B.class, config.findFactoryImplClass(A.class));
    }

    @Test
    public void propertyNotSet() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Property B is not set");
        config.findFactoryImplClass(B.class);
    }

    @Test
    public void findFactoryImplClassDefaultTest() {
        assertEquals(B.class, config.findFactoryImplClass(A.class, B.class));
    }

    @Test
    public void newFactoryImplTest() {
        moduleConfig.setClassProperty(A.class.getSimpleName(), B.class);
        assertTrue(config.newFactoryImpl(A.class) instanceof B);
    }

    @Test
    public void newFactoryImplDefaultTest() {
        assertTrue(config.newFactoryImpl(A.class, B.class) instanceof B);
    }
}
