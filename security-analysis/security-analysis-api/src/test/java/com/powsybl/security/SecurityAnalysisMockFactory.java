/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class SecurityAnalysisMockFactory implements SecurityAnalysisFactory {

    static SecurityAnalysis mock;

    @Override
    public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
        return mock == null ? mockSa() : mock;
    }

    @Override
    public SecurityAnalysis create(Network network, LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        return mock == null ? mockSa() : mock;
    }

    @Override
    public SecurityAnalysis create(Network network, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        return mock == null ? mockSa() : mock;
    }

    private SecurityAnalysis mockSa() {
        mock = mock(SecurityAnalysis.class);
        CompletableFuture<SecurityAnalysisResultWithLog> cfSarl = mock(CompletableFuture.class);
        CompletableFuture<SecurityAnalysisResult> cfSar = mock(CompletableFuture.class);
        SecurityAnalysisResult sar = mock(SecurityAnalysisResult.class);
        LimitViolationsResult preResult = mock(LimitViolationsResult.class);
        when(sar.getPreContingencyResult()).thenReturn(preResult);
        SecurityAnalysisResultWithLog sarl = new SecurityAnalysisResultWithLog(sar, "hi".getBytes());
        when(cfSarl.join()).thenReturn(sarl);
        when(cfSar.join()).thenReturn(sar);
        when(mock.runWithLog(any(), any(), any())).thenReturn(cfSarl);
        when(mock.run(any(), any(), any())).thenReturn(cfSar);
        return mock;
    }
}
