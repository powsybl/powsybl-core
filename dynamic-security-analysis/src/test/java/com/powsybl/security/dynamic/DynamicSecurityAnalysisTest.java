/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.security.SecurityAnalysisReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
class DynamicSecurityAnalysisTest {

    private static Network network;
    private static ContingenciesProvider contingenciesProvider;
    private static DynamicSecurityAnalysisRunParameters dynamicSecurityAnalysisRunParameters;

    @BeforeAll
    static void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        contingenciesProvider = Mockito.mock(ContingenciesProvider.class);
        dynamicSecurityAnalysisRunParameters = Mockito.mock(DynamicSecurityAnalysisRunParameters.class);
    }

    @Test
    void testDefaultOneProvider() {
        DynamicSecurityAnalysis.Runner defaultDynamicSA = DynamicSecurityAnalysis.find();
        assertEquals("DynamicSecurityAnalysisToolProviderMock", defaultDynamicSA.getName());
        assertEquals("1.0", defaultDynamicSA.getVersion());
        SecurityAnalysisReport report = defaultDynamicSA.run(network, Collections.emptyList(), Collections.emptyList());
        assertNotNull(report);
    }

    @Test
    void testAsyncNamedProvider() throws ExecutionException, InterruptedException {
        DynamicSecurityAnalysis.Runner defaultDynamicSA = DynamicSecurityAnalysis
                .find("DynamicSecurityAnalysisToolProviderMock");
        assertEquals("DynamicSecurityAnalysisToolProviderMock", defaultDynamicSA.getName());
        CompletableFuture<SecurityAnalysisReport> completableReport = defaultDynamicSA.runAsync(network, Collections.emptyList(), Collections.emptyList());
        assertNotNull(completableReport.get());
    }

    @Test
    void testProviderRunCombinations() {
        assertNotNull(DynamicSecurityAnalysis.run(network, Collections.emptyList(), Collections.emptyList()));
        assertNotNull(DynamicSecurityAnalysis.run(network, Collections.emptyList(), Collections.emptyList(), dynamicSecurityAnalysisRunParameters));
        assertNotNull(DynamicSecurityAnalysis.run(network, network.getVariantManager().getWorkingVariantId(), DynamicModelsSupplierMock.empty(), contingenciesProvider, dynamicSecurityAnalysisRunParameters));
    }

    @Test
    void testProviderAsyncCombinations() {
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, Collections.emptyList(), Collections.emptyList()));
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, Collections.emptyList(), Collections.emptyList(), dynamicSecurityAnalysisRunParameters));
        assertNotNull(DynamicSecurityAnalysis.runAsync(network, network.getVariantManager().getWorkingVariantId(), DynamicModelsSupplierMock.empty(), contingenciesProvider, dynamicSecurityAnalysisRunParameters));
    }
}
