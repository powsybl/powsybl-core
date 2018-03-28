/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.FileSystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ResultsCompletionLoadFlowParametersTest {

    InMemoryPlatformConfig platformConfig;
    FileSystem fileSystem;

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
    public void testExtension() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        ResultsCompletionLoadFlowParametersExtension parametersExtension = new ResultsCompletionLoadFlowParametersExtension();
        parameters.addExtension(ResultsCompletionLoadFlowParametersExtension.class, parametersExtension);
        ResultsCompletionLoadFlowParametersExtension resultsCompletionLfParameters = parameters.getExtension(ResultsCompletionLoadFlowParametersExtension.class);
        assertEquals(ResultsCompletionLoadFlowParametersExtension.EPSILON_X_DEFAULT, resultsCompletionLfParameters.getEpsilonX(), 0f);
        assertFalse(resultsCompletionLfParameters.isApplyReactanceCorrection());
    }

    @Test
    public void testNoConfig() {
        LoadFlowParameters parameters = LoadFlowParameters.load(platformConfig);
        ResultsCompletionLoadFlowParametersExtension resultsCompletionLfParameters = parameters.getExtension(ResultsCompletionLoadFlowParametersExtension.class);
        assertEquals(ResultsCompletionLoadFlowParametersExtension.EPSILON_X_DEFAULT, resultsCompletionLfParameters.getEpsilonX(), 0f);
        assertFalse(resultsCompletionLfParameters.isApplyReactanceCorrection());
    }

    @Test
    public void testConfig() {
        float epsilonX = 0.5f;
        boolean applyReactanceCorrection = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("results-completion-loadflow-parameters");
        moduleConfig.setStringProperty("epsilon-x", Float.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        LoadFlowParameters parameters = LoadFlowParameters.load(platformConfig);
        ResultsCompletionLoadFlowParametersExtension resultsCompletionLfParameters = parameters.getExtension(ResultsCompletionLoadFlowParametersExtension.class);
        assertEquals(epsilonX, resultsCompletionLfParameters.getEpsilonX(), 0f);
        assertTrue(resultsCompletionLfParameters.isApplyReactanceCorrection());
    }

}
