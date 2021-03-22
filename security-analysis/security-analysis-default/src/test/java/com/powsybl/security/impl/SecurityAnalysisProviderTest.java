/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.impl;

import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManagerFactory;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.security.*;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam<tadam at silicom.fr>
 */
public class SecurityAnalysisProviderTest {

    private static final String DEFAULT_PROVIDER_NAME = "DefaultSecurityAnalysis";

    private Network network;
    private LimitViolationDetector detector;
    private LimitViolationFilter filter;
    private ComputationManager computationManager;
    private SecurityAnalysisParameters parameters;
    private ContingenciesProvider contingenciesProvider;
    private List<SecurityAnalysisInterceptor> interceptors;

    @Before
    public void setUp() {
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
    }

    @Test
    public void testDefaultProvider() {
        SecurityAnalysis.Runner defaultSecurityAnalysisRunner = SecurityAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSecurityAnalysisRunner.getName());
        assertEquals("1.0", defaultSecurityAnalysisRunner.getVersion());
    }

    @Test
    public void testAsyncDefaultProvider() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisResult> result = SecurityAnalysis.runAsync(network, "v", detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncDefaultProviderWithFilter() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisResult> result = SecurityAnalysis.runAsync(network, filter, computationManager);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncDefaultProviderWithComputationManager() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisResult> result = SecurityAnalysis.runAsync(network, computationManager);
        assertNotNull(result.get());
    }

    @Test
    public void testAsyncDefaultProviderWithMiminumArguments() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisResult> result = SecurityAnalysis.runAsync(network);
        assertNotNull(result.get());
    }

    @Test
    public void testSyncDefaultProvider() {
        SecurityAnalysisResult result = SecurityAnalysis.run(network, "v", detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithFilter() {
        SecurityAnalysisResult result = SecurityAnalysis.run(network, filter, computationManager);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithComputationManager() {
        SecurityAnalysisResult result = SecurityAnalysis.run(network, computationManager);
        assertNotNull(result);
    }

    @Test
    public void testSyncDefaultProviderWithMiminumArguments() {
        SecurityAnalysisResult result = SecurityAnalysis.run(network);
        assertNotNull(result);
    }
}
