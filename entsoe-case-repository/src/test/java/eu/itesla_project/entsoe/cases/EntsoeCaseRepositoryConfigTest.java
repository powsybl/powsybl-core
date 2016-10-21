/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.cases;

import com.google.common.collect.Sets;
import eu.itesla_project.commons.config.InMemoryPlatformConfig;
import eu.itesla_project.commons.config.MapModuleConfig;
import eu.itesla_project.entsoe.util.EntsoeGeographicalCode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCaseRepositoryConfigTest {

    private FileSystem fileSystem;
    private Path configDir;
    private EntsoeCaseRepositoryConfig config;
    private MapModuleConfig moduleConfig;
    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);
        configDir = fileSystem.getPath("/config");
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        moduleConfig = platformConfig.createModuleConfig("entsoecaserepo");
        moduleConfig.setPathProperty("rootDir", configDir);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testLoad() throws Exception {
        moduleConfig.setStringListProperty("forbiddenFormats_FR", Arrays.asList("CIM1"));
        moduleConfig.setStringListProperty("forbiddenFormats_BE", Arrays.asList("CIM1"));
        config = EntsoeCaseRepositoryConfig.load(platformConfig, Arrays.asList("CIM1", "UCTE"));
        Assert.assertTrue(config.getRootDir().toString().equals("/config"));
        Assert.assertTrue(config.getForbiddenFormatsByGeographicalCode().size() == 2);
        Assert.assertTrue(config.getForbiddenFormatsByGeographicalCode().get(EntsoeGeographicalCode.FR).equals(Sets.newHashSet("CIM1")));
        Assert.assertTrue(config.getForbiddenFormatsByGeographicalCode().get(EntsoeGeographicalCode.BE).equals(Sets.newHashSet("CIM1")));
        Assert.assertTrue(config.getForbiddenFormatsByGeographicalCode().get(EntsoeGeographicalCode.D2).isEmpty());
    }

    @Test
    public void testUnsupportedFormatIssue() throws Exception {
        moduleConfig.setStringListProperty("forbiddenFormats_FR", Arrays.asList("UCT"));
        try {
            config = EntsoeCaseRepositoryConfig.load(platformConfig, Arrays.asList("UCTE"));
            Assert.fail();
        } catch (Exception e) {
        }
    }
}