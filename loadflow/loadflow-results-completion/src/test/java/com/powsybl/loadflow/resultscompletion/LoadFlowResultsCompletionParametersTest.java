/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class LoadFlowResultsCompletionParametersTest {

    private InMemoryPlatformConfig platformConfig;
    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void testNoConfig() {
        LoadFlowResultsCompletionParameters parameters = LoadFlowResultsCompletionParameters.load(platformConfig);
        assertEquals(LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT, parameters.getEpsilonX(), 0f);
        assertFalse(parameters.isApplyReactanceCorrection());
    }

    @Test
    void testConfig() {
        float epsilonX = 0.5f;
        boolean applyReactanceCorrection = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("loadflow-results-completion-parameters");
        moduleConfig.setStringProperty("epsilon-x", Float.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        LoadFlowResultsCompletionParameters parameters = LoadFlowResultsCompletionParameters.load(platformConfig);
        assertEquals(epsilonX, parameters.getEpsilonX(), 0f);
        assertTrue(parameters.isApplyReactanceCorrection());
    }

}
