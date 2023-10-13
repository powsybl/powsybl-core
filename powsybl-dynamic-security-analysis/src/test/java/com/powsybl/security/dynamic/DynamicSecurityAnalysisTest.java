/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.action.Action;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
class DynamicSecurityAnalysisTest {

    private static Network network;
    private static ComputationManager computationManager;
    private static ContingenciesProvider contingenciesProvider;
    private static LimitViolationFilter filter;
    private static LimitViolationDetector detector;
    private static DynamicSecurityAnalysisParameters parameters;
    private static List<SecurityAnalysisInterceptor> interceptors;
    private static List<OperatorStrategy> strategies;
    private static List<Action> actions;
    private static List<StateMonitor> monitors;
    private static Reporter reporter;

    @BeforeAll
    static void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
        contingenciesProvider = Mockito.mock(ContingenciesProvider.class);
        filter = Mockito.mock(LimitViolationFilter.class);
        detector = Mockito.mock(LimitViolationDetector.class);
        parameters = Mockito.mock(DynamicSecurityAnalysisParameters.class);
        interceptors = List.of(Mockito.mock(SecurityAnalysisInterceptor.class));
        strategies = List.of(Mockito.mock(OperatorStrategy.class));
        actions = List.of(Mockito.mock(Action.class));
        monitors = List.of(Mockito.mock(StateMonitor.class));
        reporter = Mockito.mock(Reporter.class);
    }

    @Test
    void testDefaultOneProvider() {
        // case with only one provider, no need for config
        DynamicSecurityAnalysis.Runner defaultDynamicSA = DynamicSecurityAnalysis.find();
        assertEquals("DynamicSecurityAnalysisMock", defaultDynamicSA.getName());
        assertEquals("1.0", defaultDynamicSA.getVersion());
        SecurityAnalysisReport result = defaultDynamicSA.run(network, DynamicModelsSupplierMock.empty(), Collections.emptyList());
        assertNotNull(result);
    }

    @Test
    void testAsyncNamedProvider()
            throws InterruptedException, ExecutionException {
        // case with only one provider, no need for config
        DynamicSecurityAnalysis.Runner defaultDynamicSA = DynamicSecurityAnalysis
                .find("DynamicSecurityAnalysisMock");
        assertEquals("DynamicSecurityAnalysisMock", defaultDynamicSA.getName());
        CompletableFuture<SecurityAnalysisReport> result = defaultDynamicSA.runAsync(network, DynamicModelsSupplierMock.empty(), Collections.emptyList());
        assertNotNull(result.get());
    }

    @Test
    void testProviderRunCombinations() {
        // case with only one provider, no need for config
        assertNotNull(DynamicSecurityAnalysis.run(network, DynamicModelsSupplierMock.empty(), Collections.emptyList()));
        assertNotNull(DynamicSecurityAnalysis.run(network, DynamicModelsSupplierMock.empty(), Collections.emptyList(), parameters));
        assertNotNull(DynamicSecurityAnalysis.run(network, DynamicModelsSupplierMock.empty(), contingenciesProvider, parameters, computationManager));
        assertNotNull(DynamicSecurityAnalysis.run(network, DynamicModelsSupplierMock.empty(), contingenciesProvider, parameters, computationManager, filter));
        assertNotNull(DynamicSecurityAnalysis.run(network, DynamicModelsSupplierMock.empty(), network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, detector, interceptors, strategies, actions));
        assertNotNull(DynamicSecurityAnalysis.run(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, detector, interceptors, strategies, actions, monitors, reporter));
    }

    @Test
    void testProviderAsyncCombinations() throws ExecutionException, InterruptedException {
        // case with only one provider, no need for config
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, DynamicModelsSupplierMock.empty(), Collections.emptyList()).get());
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, DynamicModelsSupplierMock.empty(), Collections.emptyList(), parameters).get());
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, DynamicModelsSupplierMock.empty(), contingenciesProvider, parameters, computationManager).get());
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, DynamicModelsSupplierMock.empty(), contingenciesProvider, parameters, computationManager, filter).get());
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, DynamicModelsSupplierMock.empty(), network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, detector, interceptors, strategies, actions).get());
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, DynamicModelsSupplierMock.empty(), EventModelsSupplier.empty(), network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, detector, interceptors, strategies, actions, monitors, reporter).get());
    }
}
