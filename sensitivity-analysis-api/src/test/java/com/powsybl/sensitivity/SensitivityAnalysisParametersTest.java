/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityAnalysisParametersTest {

    private static final String DUMMY_EXTENSION_NAME = "dummyExtension";

    @Test
    public void testExtensions() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName(DUMMY_EXTENSION_NAME) instanceof DummyExtension);
        assertTrue(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    public void testNoExtensions() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName(DUMMY_EXTENSION_NAME) instanceof DummyExtension);
        assertFalse(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    public void testExtensionFromConfig() {
        SensitivityAnalysisParameters parameters = SensitivityAnalysisParameters.load();
        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName(DUMMY_EXTENSION_NAME) instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    private static class DummyExtension extends AbstractExtension<SensitivityAnalysisParameters> {

        @Override
        public String getName() {
            return DUMMY_EXTENSION_NAME;
        }
    }

    @AutoService(SensitivityAnalysisParameters.ConfigLoader.class)
    public static class DummyLoader implements SensitivityAnalysisParameters.ConfigLoader<DummyExtension> {

        @Override
        public DummyExtension load(PlatformConfig platformConfig) {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return DUMMY_EXTENSION_NAME;
        }

        @Override
        public String getCategoryName() {
            return "sensitivity-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }
}
