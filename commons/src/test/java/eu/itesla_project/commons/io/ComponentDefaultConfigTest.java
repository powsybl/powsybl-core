/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io;

import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.config.InMemoryPlatformConfig;
import eu.itesla_project.commons.config.MapModuleConfig;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ComponentDefaultConfigTest {

    @Test
    public void findFactoryImplClassTest() throws IOException {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        try (FileSystem fileSystem = ShrinkWrapFileSystems.newFileSystem(archive)) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("componentDefaultConfig");
            moduleConfig.setStringProperty("Test", "org.junit.Test");

            ComponentDefaultConfig config = new ComponentDefaultConfig(moduleConfig);
            Assert.assertEquals(Test.class, config.findFactoryImplClass(Test.class));
        }
    }
}
