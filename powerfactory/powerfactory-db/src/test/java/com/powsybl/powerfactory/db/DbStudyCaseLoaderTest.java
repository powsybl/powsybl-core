/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.powerfactory.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DbStudyCaseLoaderTest {

    public static final String TEST_PROPERTIES = "test.properties";

    private FileSystem fileSystem;

    private Path testProperties;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        testProperties = fileSystem.getPath(TEST_PROPERTIES);
        Files.writeString(testProperties, "projectName=TestProject");
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private InputStream getActiveProjectConfigInputStream() {
        try {
            return Files.newInputStream(testProperties);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private StudyCase loadStudy(PlatformConfig platformConfig) {
        return PowerFactoryDataLoader.load(TEST_PROPERTIES,
            this::getActiveProjectConfigInputStream,
            StudyCase.class,
            List.of(new DbStudyCaseLoader(platformConfig, new TestDatabaseReader())))
                .orElseThrow();
    }

    private InMemoryPlatformConfig createPlatformConfig() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        var pfModule = platformConfig.createModuleConfig("power-factory");
        pfModule.setStringProperty("home-dir", "c:\\work");
        return platformConfig;
    }

    @Test
    public void readInstallFromConfigTest() {
        InMemoryPlatformConfig platformConfig = createPlatformConfig();
        StudyCase studyCase = loadStudy(platformConfig);
        assertEquals("TestProject - TestStudyCase", studyCase.getName());
    }

    @Test
    public void autodetectInstallTest() throws IOException {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        Path digSilentDir = fileSystem.getPath(PowerFactoryAppUtil.DIG_SILENT_DEFAULT_DIR);
        Files.createDirectories(digSilentDir.resolve("PowerFactory 2016 SP3"));
        Files.createDirectories(digSilentDir.resolve("PowerFactory 2022 SP1"));
        StudyCase studyCase = loadStudy(platformConfig);
        assertEquals("TestProject - TestStudyCase", studyCase.getName());
    }

    @Test
    public void noInstallTest() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        PowerFactoryException exception = assertThrows(PowerFactoryException.class, () -> loadStudy(platformConfig));
        assertEquals("PowerFactory installation not found", exception.getMessage());
    }

    @Test
    public void dbProjectLoaderTest() {
        InMemoryPlatformConfig platformConfig = createPlatformConfig();
        var projectLoader = new DbProjectLoader(platformConfig, new TestDatabaseReader());
        assertEquals("properties", projectLoader.getExtension());
        assertEquals(Project.class, projectLoader.getDataClass());
        assertTrue(projectLoader.test(getActiveProjectConfigInputStream()));
        var project = projectLoader.doLoad(TEST_PROPERTIES, getActiveProjectConfigInputStream());
        assertEquals("TestProject", project.getName());
        assertEquals("TestProject.IntPrj", project.getRootObject().getFullName());
    }
}
