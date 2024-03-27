/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class GroovyScriptPostProcessorTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() throws IOException {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        Path script = platformConfig.getConfigDir().map(p -> p.resolve(GroovyScriptPostProcessor.DEFAULT_SCRIPT_NAME)).orElse(null);
        assertNotNull(script);
        Files.copy(getClass().getResourceAsStream("/import-post-processor.groovy"), script);
        test(platformConfig);

        // Test with a custom script name
        script = platformConfig.getConfigDir().map(p -> p.resolve("custom-script.groovy")).orElse(null);
        assertNotNull(script);
        Files.copy(getClass().getResourceAsStream("/import-post-processor.groovy"), script);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("groovy-post-processor");
        moduleConfig.setStringProperty("script", script.toAbsolutePath().toString());
        test(platformConfig);
    }

    @Test
    void testEurostagFactory() throws IOException {
        // Create configuration
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

        // Copy script
        Path script = platformConfig.getConfigDir().map(p -> p.resolve(GroovyScriptPostProcessor.DEFAULT_SCRIPT_NAME)).orElse(null);
        assertNotNull(script);
        Files.copy(getClass().getResourceAsStream("/script-eurostag.groovy"), script);

        // Create post-processor
        GroovyScriptPostProcessor processor = new GroovyScriptPostProcessor(platformConfig);

        // Create network
        Network network = EurostagTutorialExample1Factory.create();
        assertEquals(2, network.getVoltageLevelStream().filter(vl -> vl.getNominalV() > 300).count());

        network.getGenerator("GEN").getTerminal().disconnect();

        try { // Launch process
            processor.process(network, LocalComputationManager.getDefault());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Check processing results
        assertEquals(1, network.getVoltageLevelStream().filter(vl -> vl.getNominalV() > 300).count());
        assertEquals(280, network.getVoltageLevel("VLHV1").getNominalV(), 0.0);
        assertNotNull(network.getLoad("LOAD").getExtension(LoadDetail.class));
        assertEquals(100, network.getLoad("LOAD").getExtension(LoadDetail.class).getVariableActivePower(), 0.0);
        assertEquals(500, network.getLoad("LOAD").getExtension(LoadDetail.class).getFixedActivePower(), 0.0);
        assertNotNull(network.getGenerator("GEN").getExtension(GeneratorEntsoeCategory.class));
        assertEquals(4, network.getGenerator("GEN").getExtension(GeneratorEntsoeCategory.class).getCode());
        assertEquals(0, network.getBusView().getBus("VLLOAD_0").getAngle(), 0.0);
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());
    }

    @Test
    void testFourSubstationsFactory() throws IOException {
        // Create configuration
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

        // Copy script
        Path script = platformConfig.getConfigDir().map(p -> p.resolve(GroovyScriptPostProcessor.DEFAULT_SCRIPT_NAME)).orElse(null);
        assertNotNull(script);
        Files.copy(getClass().getResourceAsStream("/script-four-substations.groovy"), script);

        // Create post-processor
        GroovyScriptPostProcessor processor = new GroovyScriptPostProcessor(platformConfig);

        // Create network
        Network network = FourSubstationsNodeBreakerFactory.create();

        try { // Launch process
            processor.process(network, LocalComputationManager.getDefault());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Check processing results
        assertEquals(800, network.getGenerator("GEN2").getTargetP(), 0.0);
        for (ReactiveCapabilityCurve.Point point : network.getGenerator("GEN2").getReactiveLimits(ReactiveCapabilityCurve.class).getPoints()) {
            if (point.getP() == 200) {
                assertEquals(-350, point.getMinQ(), 0.0);
                assertEquals(350, point.getMaxQ(), 0.0);
            }
            if (point.getP() == 800) {
                assertEquals(-400, point.getMinQ(), 0.0);
                assertEquals(400, point.getMaxQ(), 0.0);
            }
        }
        assertTrue(network.getGenerator("GEN2").getTerminal().isConnected());
    }

    private void test(PlatformConfig platformConfig) {
        GroovyScriptPostProcessor processor = new GroovyScriptPostProcessor(platformConfig);
        assertEquals("groovyScript", processor.getName());

        try {
            processor.process(null, null);
            fail();
        } catch (Exception ignored) {
        }
    }
}
