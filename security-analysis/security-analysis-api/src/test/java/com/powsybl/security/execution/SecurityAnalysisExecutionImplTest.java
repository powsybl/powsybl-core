/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class SecurityAnalysisExecutionImplTest {

    private static LimitViolationFilter filter;
    private static ContingenciesProvider contingencies;
    private static SecurityAnalysisParameters parameters;
    private static SecurityAnalysisExecution execution;
    private static Network network;
    private static ComputationManager computationManager;
    private static SecurityAnalysisExecutionInput input;

    @BeforeAll
    static void setUpClass() {
        filter = Mockito.mock(LimitViolationFilter.class);
        contingencies = Mockito.mock(ContingenciesProvider.class);
        parameters = Mockito.mock(SecurityAnalysisParameters.class);

        execution = new SecurityAnalysisExecutionImpl("ExecutionImplTestProvider",
            execInput -> new SecurityAnalysisInput(execInput.getNetworkVariant())
                    .setFilter(filter)
                    .setContingencies(contingencies)
                    .setParameters(parameters)
        );

        network = mock(Network.class);
        computationManager = mock(ComputationManager.class);
        input = new SecurityAnalysisExecutionInput();
        input.setNetworkVariant(network, "variantId");
    }

    @Test
    void checkExecutionCallAndArguments() {
        assertThrows(PowsyblException.class, () -> execution.execute(computationManager, input), "run");
    }

    @Test
    void checkExecutionWithLogCallAndArguments() {
        input.setWithLogs(true);
        assertThrows(PowsyblException.class, () -> execution.execute(computationManager, input), "run");
    }

    @AutoService(SecurityAnalysisProvider.class)
    public static class SecurityAnalysisProviderMock implements SecurityAnalysisProvider {

        @Override
        public CompletableFuture<SecurityAnalysisReport> run(Network network, String workingVariantId, ContingenciesProvider contingenciesProvider, SecurityAnalysisRunParameters runParameters) {
            assertSame(SecurityAnalysisExecutionImplTest.filter, runParameters.getFilter());
            assertSame(SecurityAnalysisExecutionImplTest.computationManager, runParameters.getComputationManager());
            assertSame(SecurityAnalysisExecutionImplTest.parameters, runParameters.getSecurityAnalysisParameters());
            assertSame(SecurityAnalysisExecutionImplTest.contingencies, contingenciesProvider);
            assertTrue(runParameters.getInterceptors().isEmpty());
            assertTrue(runParameters.getOperatorStrategies().isEmpty());
            assertTrue(runParameters.getActions().isEmpty());
            assertTrue(runParameters.getLimitReductions().isEmpty());
            throw new PowsyblException("run");
        }

        @Override
        public String getName() {
            return "ExecutionImplTestProvider";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }
    }
}
