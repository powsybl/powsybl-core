/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.cases;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import eu.itesla_project.commons.config.InMemoryPlatformConfig;
import eu.itesla_project.commons.config.MapModuleConfig;
import eu.itesla_project.entsoe.util.EntsoeGeographicalCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCaseRepositoryConfigTest {

    private FileSystem fileSystem;
    private EntsoeCaseRepositoryConfig config;
    private MapModuleConfig moduleConfig;
    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path configDir = Files.createDirectory(fileSystem.getPath("/config"));
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
        moduleConfig.setStringListProperty("forbiddenFormats_FR", Collections.singletonList("CIM1"));
        moduleConfig.setStringListProperty("forbiddenFormats_BE", Collections.singletonList("CIM1"));
        config = EntsoeCaseRepositoryConfig.load(platformConfig, Arrays.asList("CIM1", "UCTE"));
        assertTrue(config.getRootDir().toString().equals("/config"));
        assertTrue(config.getForbiddenFormatsByGeographicalCode().size() == 2);
        assertTrue(config.getForbiddenFormatsByGeographicalCode().get(EntsoeGeographicalCode.FR).equals(Collections.singleton("CIM1")));
        assertTrue(config.getForbiddenFormatsByGeographicalCode().get(EntsoeGeographicalCode.BE).equals(Collections.singleton("CIM1")));
        assertTrue(config.getForbiddenFormatsByGeographicalCode().get(EntsoeGeographicalCode.D2).isEmpty());
    }

    @Test
    public void testUnsupportedFormatIssue() throws Exception {
        moduleConfig.setStringListProperty("forbiddenFormats_FR", Collections.singletonList("UCT"));
        try {
            config = EntsoeCaseRepositoryConfig.load(platformConfig, Collections.singletonList("UCTE"));
            fail();
        } catch (Exception ignored) {
        }
    }
}
