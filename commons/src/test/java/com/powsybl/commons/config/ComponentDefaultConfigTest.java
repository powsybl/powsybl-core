/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.exceptions.UncheckedNoSuchMethodException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ComponentDefaultConfigTest {

    interface A {
    }

    static class B implements A {
    }

    static class C implements A {
        C(String a, String b) { }
    }

    private FileSystem fileSystem;
    private MapModuleConfig moduleConfig;
    private ComponentDefaultConfig config;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        moduleConfig = platformConfig.createModuleConfig("componentDefaultConfig");
        config = new ComponentDefaultConfig.Impl(moduleConfig);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void findFactoryImplClassTest() throws IOException {
        moduleConfig.setClassProperty(A.class.getSimpleName(), B.class);
        assertEquals(B.class, config.findFactoryImplClass(A.class));
    }

    @Test
    void findFactoryImplClassDefaultTest() throws IOException {
        assertEquals(B.class, config.findFactoryImplClass(A.class, B.class));
    }

    @Test
    void newFactoryImplTest() throws IOException {
        moduleConfig.setClassProperty(A.class.getSimpleName(), B.class);
        assertTrue(config.newFactoryImpl(A.class) instanceof B);
    }

    @Test
    void newFactoryImplDefaultTest() throws IOException {
        assertTrue(config.newFactoryImpl(A.class, B.class) instanceof B);
    }

    @Test
    void newFactoryExceptionTest() {
        moduleConfig.setClassProperty(C.class.getSimpleName(), C.class);
        assertThrows(UncheckedNoSuchMethodException.class, () -> config.newFactoryImpl(C.class));
        assertThrows(UncheckedNoSuchMethodException.class, () -> config.newFactoryImpl(A.class, C.class));
    }
}
