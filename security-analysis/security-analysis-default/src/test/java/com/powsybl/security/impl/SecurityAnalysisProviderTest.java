/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManagerFactory;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.security.*;
import com.powsybl.action.Action;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.detectors.LimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam<tadam at silicom.fr>
 */
class SecurityAnalysisProviderTest {

    private static final String DEFAULT_PROVIDER_NAME = "DefaultSecurityAnalysis";

    private Network network;
    private LimitViolationDetector detector;
    private LimitViolationFilter filter;
    private ComputationManager computationManager;
    private SecurityAnalysisParameters parameters;
    private ContingenciesProvider contingenciesProvider;
    private List<SecurityAnalysisInterceptor> interceptors;
    private List<StateMonitor> monitors;
    private List<OperatorStrategy> operatorStrategies;
    private List<Action> actions;

    @BeforeEach
    void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        detector = new DefaultLimitViolationDetector();
        filter = Mockito.mock(LimitViolationFilter.class);
        computationManager = new LocalComputationManagerFactory().create();
        parameters = SecurityAnalysisParameters.load();
        contingenciesProvider = new EmptyContingencyListProvider();
        interceptors = Collections.emptyList();
        monitors = Collections.emptyList();
        operatorStrategies = Collections.emptyList();
        actions = Collections.emptyList();
    }

    @Test
    void testDefaultProvider() {
        SecurityAnalysis.Runner defaultSecurityAnalysisRunner = SecurityAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSecurityAnalysisRunner.getName());
        assertEquals("1.0", defaultSecurityAnalysisRunner.getVersion());
    }

    @Test
    void testAsyncDefaultProvider() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisReport> report = SecurityAnalysis.runAsync(network, "v", contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions, ReportNode.NO_OP);
        assertNotNull(report.get());
    }

    @Test
    void testAsyncDefaultProviderWithFilter() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisReport> report = SecurityAnalysis.runAsync(network, contingenciesProvider, parameters, computationManager, filter);
        assertNotNull(report.get());
    }

    @Test
    void testAsyncDefaultProviderWithComputationManager() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisReport> report = SecurityAnalysis.runAsync(network, contingenciesProvider, parameters, computationManager);
        assertNotNull(report.get());
    }

    @Test
    void testAsyncDefaultProviderWithMiminumArguments() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisReport> report = SecurityAnalysis.runAsync(network, Collections.emptyList());
        assertNotNull(report.get());
    }

    @Test
    void testSyncDefaultProvider() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, "v", contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions);
        assertNotNull(report);
    }

    @Test
    void testSyncDefaultProviderMonitor() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, "v", contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions, monitors, ReportNode.NO_OP);
        assertNotNull(report);
    }

    @Test
    void testSyncDefaultProviderWithFilter() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, contingenciesProvider, parameters, computationManager, filter);
        assertNotNull(report);
    }

    @Test
    void testSyncDefaultProviderWithComputationManager() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, contingenciesProvider, parameters, computationManager);
        assertNotNull(report);
    }

    @Test
    void testSyncDefaultProviderWithParameters() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, Collections.emptyList(), parameters);
        assertNotNull(report);
    }

    @Test
    void testSyncDefaultProviderWithMiminumArguments() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, Collections.emptyList());
        assertNotNull(report);
    }
}
