/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.action.Action;
import com.powsybl.security.distributed.DistributedSecurityAnalysisExecution;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.distributed.ForwardedSecurityAnalysisExecution;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class SecurityAnalysisExecutionBuilderTest {

    private static AtomicReference<ContingenciesProvider> actualProvider;
    private SecurityAnalysisExecutionBuilder builder;
    private SecurityAnalysisExecutionInput input;

    @BeforeAll
    static void setUpClass() {
        actualProvider = new AtomicReference<>();
    }

    @BeforeEach
    void setUp() {

        Contingency contingency = new Contingency("cont");
        ContingenciesProvider provider = network -> Collections.nCopies(10, contingency);

        NetworkVariant networkVariant = mock(NetworkVariant.class);
        Network network = mock(Network.class);
        input = mock(SecurityAnalysisExecutionInput.class);
        when(input.getNetworkVariant()).thenReturn(networkVariant);
        when(networkVariant.getNetwork()).thenReturn(network);
        when(networkVariant.getVariantId()).thenReturn("mock");

        builder = new SecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::new,
            "ExecutionBuilderTestProvider",
            execInput -> new SecurityAnalysisInput(networkVariant)
                    .setContingencies(provider));
    }

    @Test
    void checkLocal() {
        SecurityAnalysisExecution execution = builder.build();
        assertTrue(execution instanceof SecurityAnalysisExecutionImpl);

        execution.execute(Mockito.mock(ComputationManager.class), input);

        assertNotNull(actualProvider.get());
        assertEquals(10, actualProvider.get().getContingencies(null).size());
    }

    @Test
    void checkForwarded() {
        builder.forward(true);
        assertTrue(builder.build() instanceof ForwardedSecurityAnalysisExecution);
    }

    @Test
    void checkDistributedForwarded() {
        builder.forward(true)
                .distributed(12);
        assertTrue(builder.build() instanceof ForwardedSecurityAnalysisExecution);
    }

    @Test
    void checkDistributed() {
        builder.distributed(12);
        assertTrue(builder.build() instanceof DistributedSecurityAnalysisExecution);
    }

    @Test
    void checkSubtaskHasOnly5Contingencies() {
        SecurityAnalysisExecution execution = builder.subTask(new Partition(1, 2)).build();
        assertTrue(execution instanceof SecurityAnalysisExecutionImpl);

        execution.execute(Mockito.mock(ComputationManager.class), input);

        assertNotNull(actualProvider.get());
        assertEquals(5, actualProvider.get().getContingencies(null).size());
    }

    @AutoService(SecurityAnalysisProvider.class)
    public static class SecurityAnalysisProviderMock implements SecurityAnalysisProvider {
        @Override
        public CompletableFuture<SecurityAnalysisReport> run(Network network, String workingVariantId, LimitViolationFilter filter, ComputationManager computationManager, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors, List<OperatorStrategy> operatorStrategies, List<Action> actions, List<StateMonitor> monitors, List<LimitReduction> limitReductions, ReportNode reportNode) {
            actualProvider.set(contingenciesProvider);
            return null;
        }

        @Override
        public String getName() {
            return "ExecutionBuilderTestProvider";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }
    }
}
