/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion;

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
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LoadFlowResultsCompletionParametersTest {

    private InMemoryPlatformConfig platformConfig;
    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testNoConfig() {
        LoadFlowResultsCompletionParameters parameters = LoadFlowResultsCompletionParameters.load(platformConfig);
        assertEquals(LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT, parameters.getEpsilonX(), 0f);
        assertFalse(parameters.isApplyReactanceCorrection());
    }

    @Test
    public void testConfig() {
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
