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
import java.time.Instant;
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
            builder.createClass("IntPrj");
            builder.createAttribute("IntPrj", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
            builder.createAttribute("IntPrj", "pCase", DataAttributeType.OBJECT.ordinal(), "");

            builder.createClass("IntPrjfolder");
            builder.createAttribute("IntPrjfolder", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");

            builder.createClass("IntCase");
            builder.createAttribute("IntCase", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
            builder.createAttribute("IntCase", "iStudyTime", DataAttributeType.INTEGER64.ordinal(), "");

            builder.createClass("ElmNet");
            builder.createAttribute("ElmNet", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
            builder.createAttribute("ElmNet", "aInt", DataAttributeType.INTEGER.ordinal(), "");
            builder.createAttribute("ElmNet", "aDouble", DataAttributeType.DOUBLE.ordinal(), "");

            builder.createClass("ElmSubstat");

            builder.createObject(0L, "IntPrj");
            builder.setStringAttributeValue(0L, DataAttribute.LOC_NAME, "TestProject");
            builder.setObjectAttributeValue(0L, "pCase", 2L);

            builder.createObject(1L, "IntPrjfolder");
            builder.setStringAttributeValue(1L, DataAttribute.LOC_NAME, "Study Cases");
            builder.setObjectParent(1L, 0L);

            builder.createObject(2L, "IntCase");
            builder.setStringAttributeValue(2L, DataAttribute.LOC_NAME, "TestStudyCase");
            Instant studyTime = Instant.parse("2021-10-30T09:35:25Z");
            builder.setLongAttributeValue(2L, "iStudyTime", studyTime.toEpochMilli());
            builder.setObjectParent(2L, 1L);

            builder.createObject(3L, "IntPrjfolder");
            builder.setStringAttributeValue(3L, DataAttribute.LOC_NAME, "Network Model");
            builder.setObjectParent(3L, 0L);

            builder.createObject(4L, "IntPrjfolder");
            builder.setStringAttributeValue(4L, DataAttribute.LOC_NAME, "Network Data");
            builder.setObjectParent(4L, 3L);

            builder.createObject(5L, "ElmNet");
            builder.setStringAttributeValue(5L, DataAttribute.LOC_NAME, "TestNetwork");
            builder.setDoubleAttributeValue(5L, "aDouble", 3.34d);
            builder.setIntAttributeValue(5L, "aInt", 3);
            builder.setObjectParent(5L, 4L);

            builder.createObject(6L, "ElmSubstat");
            builder.setObjectParent(6L, 5L);
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
}
