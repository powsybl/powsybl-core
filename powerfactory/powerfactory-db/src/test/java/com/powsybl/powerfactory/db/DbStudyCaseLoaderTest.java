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
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DbStudyCaseLoaderTest {

    public static final String TEST_PROPERTIES = "test.properties";

    private static class TestDatabaseReader implements DatabaseReader {

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public void read(String powerFactoryHome, String projectName, DataObjectBuilder builder) {
            builder.createClass("ElmNet");
            builder.createAttribute("ElmNet", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
            builder.createAttribute("ElmNet", "aInt", DataAttributeType.INTEGER.ordinal(), "");
            builder.createAttribute("ElmNet", "aDouble", DataAttributeType.DOUBLE.ordinal(), "");
            builder.createObject(0L, "ElmNet");
            builder.setStringAttributeValue(0L, DataAttribute.LOC_NAME, "TestGrid");
            builder.setDoubleAttributeValue(0L, "aDouble", 3.34d);
            builder.setIntAttributeValue(0L, "aInt", 3);
            builder.createClass("ElmSubstat");
            builder.createObject(1L, "ElmSubstat");
            builder.setObjectParent(1L, 0L);
        }
    }

    private FileSystem fileSystem;

    private Path testProperties;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        testProperties = fileSystem.getPath(TEST_PROPERTIES);
        Files.writeString(testProperties, "projectName=Foo");
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private StudyCase loadStudy(PlatformConfig platformConfig) {
        return PowerFactoryDataLoader.load("test.properties",
            () -> {
                try {
                    return Files.newInputStream(testProperties);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            },
            StudyCase.class,
            List.of(new DbStudyCaseLoader(platformConfig, new TestDatabaseReader())))
                .orElseThrow();
    }

    @Test
    public void readInstallFromConfigTest() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        var pfModule = platformConfig.createModuleConfig("power-factory");
        pfModule.setStringProperty("home-dir", "c:\\work");
        StudyCase studyCase = loadStudy(platformConfig);
        assertEquals("???", studyCase.getName());
    }

    @Test
    public void autodetectInstallTest() throws IOException {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        Path digSilentDir = fileSystem.getPath(PowerFactoryAppUtil.DIG_SILENT_DEFAULT_DIR);
        Files.createDirectories(digSilentDir.resolve("PowerFactory 2016 SP3"));
        Files.createDirectories(digSilentDir.resolve("PowerFactory 2022 SP1"));
        StudyCase studyCase = loadStudy(platformConfig);
        assertEquals("???", studyCase.getName());
    }

    @Test
    public void noInstallTest() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        PowerFactoryException exception = assertThrows(PowerFactoryException.class, () -> loadStudy(platformConfig));
        assertEquals("PowerFactory installation not found", exception.getMessage());
    }
}
