/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.security.json.JsonSecurityAnalysisParametersTest.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class SecurityAnalysisParametersTest {

    private static final double EPS = 10E-3;

    @Test
    void testExtensions() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName("dummy-extension") instanceof DummyExtension);
        assertTrue(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    void testNoExtensions() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName("dummy-extension") instanceof DummyExtension);
        assertFalse(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    void testExtensionFromConfig() {
        SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load();
        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName("dummy-extension") instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testLoadFromFile() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("security-analysis-default-parameters");
            moduleConfig.setStringProperty("increased-flow-violations-proportional-threshold", "0.3");
            moduleConfig.setStringProperty("increased-low-voltage-violations-proportional-threshold", "0.4");
            moduleConfig.setStringProperty("increased-high-voltage-violations-proportional-threshold", "0.2");
            moduleConfig.setStringProperty("increased-low-voltage-violations-absolute-threshold", "20");
            moduleConfig.setStringProperty("increased-high-voltage-violations-absolute-threshold", "25");
            SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load(platformConfig);
            assertEquals(0.3, parameters.getIncreasedViolationsParameters().getFlowProportionalThreshold(), EPS);
            assertEquals(0.4, parameters.getIncreasedViolationsParameters().getLowVoltageProportionalThreshold(), EPS);
            assertEquals(0.2, parameters.getIncreasedViolationsParameters().getHighVoltageProportionalThreshold(), EPS);
            assertEquals(20, parameters.getIncreasedViolationsParameters().getLowVoltageAbsoluteThreshold(), EPS);
            assertEquals(25, parameters.getIncreasedViolationsParameters().getHighVoltageAbsoluteThreshold(), EPS);
        }
    }

    @Test
    void testIncreasedViolationsParameters() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();

        assertEquals(0.1, parameters.getIncreasedViolationsParameters().getFlowProportionalThreshold(), EPS);
        assertEquals(0.0, parameters.getIncreasedViolationsParameters().getLowVoltageProportionalThreshold(), EPS);
        assertEquals(0.0, parameters.getIncreasedViolationsParameters().getHighVoltageProportionalThreshold(), EPS);
        assertEquals(0.0, parameters.getIncreasedViolationsParameters().getLowVoltageAbsoluteThreshold(), EPS);
        assertEquals(0.0, parameters.getIncreasedViolationsParameters().getHighVoltageAbsoluteThreshold(), EPS);

        parameters.getIncreasedViolationsParameters().setFlowProportionalThreshold(0.01);
        parameters.getIncreasedViolationsParameters().setLowVoltageProportionalThreshold(0.1);
        parameters.getIncreasedViolationsParameters().setHighVoltageProportionalThreshold(0.2);
        parameters.getIncreasedViolationsParameters().setLowVoltageAbsoluteThreshold(4.0);
        parameters.getIncreasedViolationsParameters().setHighVoltageAbsoluteThreshold(5.0);

        assertEquals(0.01, parameters.getIncreasedViolationsParameters().getFlowProportionalThreshold(), EPS);
        assertEquals(0.1, parameters.getIncreasedViolationsParameters().getLowVoltageProportionalThreshold(), EPS);
        assertEquals(0.2, parameters.getIncreasedViolationsParameters().getHighVoltageProportionalThreshold(), EPS);
        assertEquals(4.0, parameters.getIncreasedViolationsParameters().getLowVoltageAbsoluteThreshold(), EPS);
        assertEquals(5.0, parameters.getIncreasedViolationsParameters().getHighVoltageAbsoluteThreshold(), EPS);
    }
}
