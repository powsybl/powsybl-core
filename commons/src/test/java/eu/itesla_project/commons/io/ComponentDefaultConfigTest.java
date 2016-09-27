/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystems;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ComponentDefaultConfigTest {

    @Before
    public void setUp() throws IOException {
        InMemoryPlatformConfig config = new InMemoryPlatformConfig(FileSystems.getDefault());
        MapModuleConfig moduleConfig = config.createModuleConfig("componentDefaultConfig");
        moduleConfig.setStringProperty("Test", "org.junit.Test");

        PlatformConfig.setDefaultConfig(config);
    }

    @Test
    public void findFactoryImplClassTest() {
        ComponentDefaultConfig config = new ComponentDefaultConfig();
        Assert.assertEquals(Test.class, config.findFactoryImplClass(Test.class));
    }
}
