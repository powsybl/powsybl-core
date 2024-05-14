/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.local.LocalComputationManagerFactory;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam<tadam at silicom.fr>
 */
class SecurityAnalysisProviderTest {

    private static final String DEFAULT_PROVIDER_NAME = "DefaultSecurityAnalysis";

    private Network network;
    private ContingenciesProvider contingenciesProvider;
    private SecurityAnalysisRunParameters runParameters;

    @BeforeEach
    void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        contingenciesProvider = new EmptyContingencyListProvider();
        runParameters = SecurityAnalysisRunParameters.getDefault()
                .setFilter(Mockito.mock(LimitViolationFilter.class))
                .setComputationManager(new LocalComputationManagerFactory().create());
    }

    @Test
    void testDefaultProvider() {
        SecurityAnalysis.Runner defaultSecurityAnalysisRunner = SecurityAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultSecurityAnalysisRunner.getName());
        assertEquals("1.0", defaultSecurityAnalysisRunner.getVersion());
    }

    @Test
    void testAsyncDefaultProvider() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisReport> report = SecurityAnalysis.runAsync(network, "v", contingenciesProvider, runParameters, ReportNode.NO_OP);
        assertNotNull(report.get());
    }

    @Test
    void testAsyncDefaultProviderWithRunParameters() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisReport> report = SecurityAnalysis.runAsync(network, Collections.emptyList(), runParameters);
        assertNotNull(report.get());
    }

    @Test
    void testAsyncDefaultProviderWithMinimumArguments() throws InterruptedException, ExecutionException {
        CompletableFuture<SecurityAnalysisReport> report = SecurityAnalysis.runAsync(network, Collections.emptyList());
        assertNotNull(report.get());
    }

    @Test
    void testSyncDefaultProvider() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, "v", contingenciesProvider, runParameters, ReportNode.NO_OP);
        assertNotNull(report);
    }

    @Test
    void testSyncDefaultProviderWithRunParameters() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, Collections.emptyList(), runParameters);
        assertNotNull(report);
    }

    @Test
    void testSyncDefaultProviderWithMinimumArguments() {
        SecurityAnalysisReport report = SecurityAnalysis.run(network, Collections.emptyList());
        assertNotNull(report);
    }
}
