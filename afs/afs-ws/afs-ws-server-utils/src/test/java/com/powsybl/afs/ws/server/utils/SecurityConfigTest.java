package com.powsybl.afs.ws.server.utils;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.FileSystem;

import static com.powsybl.commons.config.ConfigVersion.DEFAULT_CONFIG_VERSION;
import static org.junit.Assert.assertEquals;

public class SecurityConfigTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    FileSystem fileSystem;
    InMemoryPlatformConfig platformConfig;


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
    public void testNoConfig() {
        SecurityConfig securityConfig = SecurityConfig.load(platformConfig);
        checkValues(securityConfig, DEFAULT_CONFIG_VERSION, SecurityConfig.DEFAULT_TOKEN_VALIDITY, SecurityConfig.DEFAULT_SKIP_TOKEN_VALIDITY_CHECK);
    }

    @Test
    public void testPartConfig() {
        MapModuleConfig config = platformConfig.createModuleConfig("security");
        config.setStringProperty("token-validity", Long.toString(3600L * 60));
        SecurityConfig securityConfig = SecurityConfig.load(platformConfig);
        checkValues(securityConfig, DEFAULT_CONFIG_VERSION, 3600L * 60, SecurityConfig.DEFAULT_SKIP_TOKEN_VALIDITY_CHECK);

    }

    @Test
    public void testCompleteConfig() {
        MapModuleConfig config = platformConfig.createModuleConfig("security");
        config.setStringProperty("version", "1.1");
        config.setStringProperty("token-validity", Long.toString(3600L * 60));
        config.setStringProperty("skip-token-validity-check", Boolean.toString(false));
        SecurityConfig securityConfig = SecurityConfig.load(platformConfig);
        checkValues(securityConfig, "1.1", 3600L * 60, false);
    }

    @Test
    public void testSetters() {
        SecurityConfig securityConfig = SecurityConfig.load(platformConfig);
        securityConfig.setTokenValidity(3600L * 60);
        securityConfig.setSkipTokenValidityCheck(false);
        checkValues(securityConfig, DEFAULT_CONFIG_VERSION, 3600L * 60, false);
    }

    @Test
    public void testWrongConfig() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid token validity");
        new SecurityConfig(0, true);
    }

    private static void checkValues(SecurityConfig config, String version, long tokenValidity, boolean skipTokenValidityCheck) {
        assertEquals(version, config.getVersion());
        assertEquals(tokenValidity, config.getTokenValidity());
        assertEquals(skipTokenValidityCheck, config.isSkipTokenValidityCheck());
    }

}
