/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class KafkaConfigTest {

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("kafka-app-file-system");
        moduleConfig.setStringProperty("kafka-brokers", "10.11.12.13:9092");
        moduleConfig.setStringProperty("client-id", "client");
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void loadTest() {
        KafkaConfig config = KafkaConfig.load(platformConfig);
        assertEquals("client", config.getClientId());
        assertEquals("10.11.12.13:9092", config.getKafkaBrokers());

        config.setClientId("newClient");
        assertEquals("newClient", config.getClientId());

        config.setKafkaBrokers("localhost:9092");
        assertEquals("localhost:9092", config.getKafkaBrokers());
    }

    @Test
    public void loadDefaultConfigTest() {
        KafkaConfig config = KafkaConfig.load(new InMemoryPlatformConfig(fileSystem));
        assertNotNull(config);

        assertEquals("client1", config.getClientId());
        assertEquals("localhost:9092", config.getKafkaBrokers());
    }
}

