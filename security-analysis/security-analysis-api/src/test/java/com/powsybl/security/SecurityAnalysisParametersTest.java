/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import org.junit.Test;

import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisParametersTest {

    private static final double EPS = 10E-3;

    @Test
    public void testExtensions() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertTrue(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    public void testNoExtensions() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertFalse(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);

        assertEquals(0.1, parameters.getIncreasedFlowViolationsThreshold(), EPS);
        assertEquals(0.0, parameters.getIncreasedLowVoltageViolationsThreshold(), EPS);
        assertEquals(0.0, parameters.getIncreasedHighVoltageViolationsThreshold(), EPS);
        assertEquals(0.0, parameters.getIncreasedLowVoltageViolationsDelta(), EPS);
        assertEquals(0.0, parameters.getIncreasedHighVoltageViolationsDelta(), EPS);

        parameters.setIncreasedFlowViolationsThreshold(0.01);
        parameters.setIncreasedLowVoltageViolationsThreshold(0.1);
        parameters.setIncreasedHighVoltageViolationsThreshold(0.1);
        parameters.setIncreasedLowVoltageViolationsDelta(4.0);
        parameters.setIncreasedHighVoltageViolationsDelta(5.0);

        assertEquals(0.01, parameters.getIncreasedFlowViolationsThreshold(), EPS);
        assertEquals(0.1, parameters.getIncreasedLowVoltageViolationsThreshold(), EPS);
        assertEquals(0.1, parameters.getIncreasedHighVoltageViolationsThreshold(), EPS);
        assertEquals(4.0, parameters.getIncreasedLowVoltageViolationsDelta(), EPS);
        assertEquals(5.0, parameters.getIncreasedHighVoltageViolationsDelta(), EPS);
    }

    @Test
    public void testExtensionFromConfig() {
        SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load();

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    private static class DummyExtension extends AbstractExtension<SecurityAnalysisParameters> {

        @Override
        public String getName() {
            return "dummyExtension";
        }
    }

    @AutoService(SecurityAnalysisParameters.ConfigLoader.class)
    public static class DummyLoader implements SecurityAnalysisParameters.ConfigLoader<DummyExtension> {

        @Override
        public DummyExtension load(PlatformConfig platformConfig) {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return "dummyExtension";
        }

        @Override
        public String getCategoryName() {
            return "security-analysis-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

    @Test
    public void testLoadFromFile() {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("security-analysis-default-parameters");
        moduleConfig.setStringProperty("increasedFlowViolationsThreshold", "0.3");
        moduleConfig.setStringProperty("increasedLowVoltageViolationsThreshold", "0.4");
        moduleConfig.setStringProperty("increasedHighVoltageViolationsThreshold", "0.2");
        moduleConfig.setStringProperty("increasedLowVoltageViolationsDelta", "20");
        moduleConfig.setStringProperty("increasedHighVoltageViolationsDelta", "25");
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        SecurityAnalysisParameters.load(parameters, platformConfig);
        assertEquals(0.3, parameters.getIncreasedFlowViolationsThreshold(), EPS);
        assertEquals(0.4, parameters.getIncreasedLowVoltageViolationsThreshold(), EPS);
        assertEquals(0.2, parameters.getIncreasedHighVoltageViolationsThreshold(), EPS);
        assertEquals(20, parameters.getIncreasedLowVoltageViolationsDelta(), EPS);
        assertEquals(25, parameters.getIncreasedHighVoltageViolationsDelta(), EPS);
    }
}
