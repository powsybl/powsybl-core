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
import com.powsybl.powerfactory.model.DataAttribute;
import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.StudyCase;
import com.powsybl.powerfactory.model.StudyCaseLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DbStudyCaseLoaderTest {

    private static class TestDatabaseReader implements DatabaseReader {

        @Override
        public void read(String powerFactoryHome, String projectName, DataObjectBuilder builder) {
            builder.createClass("ElmNet");
            builder.createAttribute("ElmNet", DataAttribute.LOC_NAME, DataAttributeType.STRING.ordinal(), "");
            builder.createObject(0L, "ElmNet", -1);
            builder.setStringAttributeValue(0L, DataAttribute.LOC_NAME, "TestGrid");
        }
    }

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void readFromConfigTest() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        var pfModule = platformConfig.createModuleConfig("power-factory");
        pfModule.setStringProperty("home-dir", "c:\\work");
        StudyCase studyCase = StudyCaseLoader.load("Test.IntPrj",
            () -> null,
            List.of(new DbStudyCaseLoader(platformConfig, new TestDatabaseReader())))
                .orElseThrow();
        assertEquals("???", studyCase.getName());
    }

    @Test
    public void autodetectTest() throws IOException {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        Path digSilentDir = fileSystem.getPath(DbStudyCaseLoader.DIG_SILENT_DEFAULT_DIR);
        Files.createDirectories(digSilentDir.resolve("PowerFactory 2016 SP3"));
        Files.createDirectories(digSilentDir.resolve("PowerFactory 2022 SP1"));
        StudyCase studyCase = StudyCaseLoader.load("Test.IntPrj",
            () -> null,
            List.of(new DbStudyCaseLoader(platformConfig, new TestDatabaseReader())))
                .orElseThrow();
        assertEquals("???", studyCase.getName());
    }
}
