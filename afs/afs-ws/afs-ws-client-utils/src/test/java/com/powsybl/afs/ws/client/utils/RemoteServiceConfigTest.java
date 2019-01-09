/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client.utils;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Optional;

import static com.powsybl.commons.config.ConfigVersion.DEFAULT_CONFIG_VERSION;
import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteServiceConfigTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private FileSystem fileSystem;
    private InMemoryPlatformConfig platformConfig;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() {
        RemoteServiceConfig config = new RemoteServiceConfig("host", "test", 443, true);
        assertEquals("https://host:443/test", config.getRestUri().toString());
        assertEquals("wss://host:443/test", config.getWsUri().toString());

        RemoteServiceConfig config2 = new RemoteServiceConfig("host", "test", 80, false);
        assertEquals("http://host:80/test", config2.getRestUri().toString());
        assertEquals("ws://host:80/test", config2.getWsUri().toString());
    }

    @Test
    public void testLoadWithoutConfig() {
        Optional<RemoteServiceConfig> remoteServiceConfig = RemoteServiceConfig.load(platformConfig);
        assertEquals(Optional.empty(), remoteServiceConfig);
    }

    @Test
    public void testLoadWithPartialConfig() {
        MapModuleConfig config = platformConfig.createModuleConfig("remote-service");
        config.setStringProperty("host-name", "host");
        config.setStringProperty("app-name", "test");
        Optional<RemoteServiceConfig> remoteServiceConfig = RemoteServiceConfig.load(platformConfig);
        assertTrue(remoteServiceConfig.isPresent());
        remoteServiceConfig.ifPresent(rsc -> {
            assertEquals(DEFAULT_CONFIG_VERSION, rsc.getVersion());
            assertEquals("https://host:443/test", rsc.getRestUri().toString());
            assertEquals("wss://host:443/test", rsc.getWsUri().toString());
        });
    }



    @Test
    public void testLoad() {

        //test without config
        Optional<RemoteServiceConfig> remoteServiceConfig = RemoteServiceConfig.load(platformConfig);
        assertEquals(Optional.empty(), remoteServiceConfig);

        //test with correct partial config
        MapModuleConfig config = platformConfig.createModuleConfig("remote-service");
        config.setStringProperty("host-name", "host");
        config.setStringProperty("app-name", "test");
        remoteServiceConfig = RemoteServiceConfig.load(platformConfig);
        assertTrue(remoteServiceConfig.isPresent());
        remoteServiceConfig.ifPresent(rsc -> {
            assertEquals(DEFAULT_CONFIG_VERSION, rsc.getVersion());
            assertEquals("https://host:443/test", rsc.getRestUri().toString());
            assertEquals("wss://host:443/test", rsc.getWsUri().toString());
        });

        config.setStringProperty("secure", "false");
        remoteServiceConfig = RemoteServiceConfig.load(platformConfig);
        assertTrue(remoteServiceConfig.isPresent());
        remoteServiceConfig.ifPresent(rsc -> {
            assertEquals(DEFAULT_CONFIG_VERSION, rsc.getVersion());
            assertEquals("http://host:80/test", rsc.getRestUri().toString());
            assertEquals("ws://host:80/test", rsc.getWsUri().toString());
        });

        //test with complete config
        config.setStringProperty("port", "8080");
        config.setStringProperty("version", "1.1");
        remoteServiceConfig = RemoteServiceConfig.load(platformConfig);
        assertTrue(remoteServiceConfig.isPresent());
        remoteServiceConfig.ifPresent(rsc -> {
            assertEquals("1.1", rsc.getVersion());
            assertEquals("http://host:8080/test", rsc.getRestUri().toString());
            assertEquals("ws://host:8080/test", rsc.getWsUri().toString());
        });
    }

    @Test
    public void testWrongLoad() {
        MapModuleConfig config = platformConfig.createModuleConfig("remote-service");
        config.setStringProperty("host-name", "host");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Property app-name is not set");
        RemoteServiceConfig.load(platformConfig);
    }
}
