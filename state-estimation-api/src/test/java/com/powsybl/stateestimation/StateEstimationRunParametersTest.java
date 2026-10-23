/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.computation.ComputationManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class StateEstimationRunParametersTest {

    @Test
    void fillsInWhatItWasNotGivenOnFirstUse() {
        StateEstimationRunParameters runParameters = new StateEstimationRunParameters();

        assertEquals(StateEstimationParameters.DEFAULT_RESIDUAL_FLAGGING_THRESHOLD,
                runParameters.getStateEstimationParameters().getResidualFlaggingThreshold());
        assertSame(runParameters.getStateEstimationParameters(), runParameters.getStateEstimationParameters(),
                "the settings are read once and kept");
        assertNotNull(runParameters.getComputationManager());
        assertSame(ReportNode.NO_OP, runParameters.getReportNode());
    }

    @Test
    void theDefaultsAreFilledInEagerly() {
        StateEstimationRunParameters runParameters = StateEstimationRunParameters.getDefault();

        assertNotNull(runParameters.getStateEstimationParameters());
        assertNotNull(runParameters.getComputationManager());
        assertSame(ReportNode.NO_OP, runParameters.getReportNode());
    }

    @Test
    void holdsWhatItWasGivenAndChains() {
        StateEstimationParameters parameters = new StateEstimationParameters();
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME)
                .withMessageTemplate("testStateEstimation")
                .withUntypedValue("variantId", "InitialState")
                .build();

        StateEstimationRunParameters runParameters = new StateEstimationRunParameters();
        StateEstimationRunParameters returned = runParameters
                .setParameters(parameters)
                .setComputationManager(computationManager)
                .setReportNode(reportNode);

        assertSame(runParameters, returned);
        assertSame(parameters, runParameters.getStateEstimationParameters());
        assertSame(computationManager, runParameters.getComputationManager());
        assertSame(reportNode, runParameters.getReportNode());
    }

}
