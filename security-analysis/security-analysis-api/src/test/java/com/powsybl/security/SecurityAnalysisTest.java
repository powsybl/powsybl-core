/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ComputationResourcesStatus;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptorMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisTest {

    private FileSystem fileSystem;

    private PlatformConfig platformConfig;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void run() {
        Network network = EurostagTutorialExample1Factory.create();
        ((Bus) network.getIdentifiable("NHV1")).setV(380.0);
        ((Bus) network.getIdentifiable("NHV2")).setV(380.0);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(560.0).setQ(550.0);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(560.0).setQ(550.0);
        network.getLine("NHV1_NHV2_1").newCurrentLimits1().setPermanentLimit(1500.0).add();
        network.getLine("NHV1_NHV2_1").newCurrentLimits2()
                .setPermanentLimit(1200.0)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1300.0)
                .endTemporaryLimit()
                .add();

        ComputationManager computationManager = createMockComputationManager();

        Contingency contingency = Contingency.builder("NHV1_NHV2_2_contingency")
                                             .addBranch("NHV1_NHV2_2")
                                             .build();
        contingency = Mockito.spy(contingency);
        Mockito.when(contingency.toTask()).thenReturn((network1, computationManager1) -> {
            network1.getLine("NHV1_NHV2_2").getTerminal1().disconnect();
            network1.getLine("NHV1_NHV2_2").getTerminal2().disconnect();
            network1.getLine("NHV1_NHV2_1").getTerminal2().setP(600.0);
        });
        ContingenciesProvider contingenciesProvider = Mockito.mock(ContingenciesProvider.class);
        Mockito.when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.singletonList(contingency));

        LimitViolationFilter filter = new LimitViolationFilter();
        LimitViolationDetector detector = new DefaultLimitViolationDetector();
        SecurityAnalysisInterceptorMock interceptorMock = new SecurityAnalysisInterceptorMock();
        List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();
        interceptors.add(interceptorMock);

        SecurityAnalysisResult result = SecurityAnalysis.run(network,
                VariantManagerConstants.INITIAL_VARIANT_ID,
                detector,
                filter,
                computationManager,
                SecurityAnalysisParameters.load(platformConfig),
                contingenciesProvider,
                interceptors);

        assertTrue(result.getPreContingencyResult().isComputationOk());
        assertEquals(0, result.getPreContingencyResult().getLimitViolations().size());
        PostContingencyResult postcontingencyResult = result.getPostContingencyResults().get(0);
        assertTrue(postcontingencyResult.getLimitViolationsResult().isComputationOk());
        assertEquals(1, postcontingencyResult.getLimitViolationsResult().getLimitViolations().size());
        LimitViolation violation = postcontingencyResult.getLimitViolationsResult().getLimitViolations().get(0);
        assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
        assertEquals("NHV1_NHV2_1", violation.getSubjectId());

        ActivePowerExtension extension1 = violation.getExtension(ActivePowerExtension.class);
        assertNotNull(extension1);
        assertEquals(560.0, extension1.getPreContingencyValue(), 0.0);
        assertEquals(600.0, extension1.getPostContingencyValue(), 0.0);

        CurrentExtension extension2 = violation.getExtension(CurrentExtension.class);
        assertNotNull(extension2);
        assertEquals(1192.5631358010583, extension2.getPreContingencyValue(), 0.0);

        assertEquals(1, interceptorMock.getOnPostContingencyResultCount());
        assertEquals(1, interceptorMock.getOnPreContingencyResultCount());
        assertEquals(1, interceptorMock.getOnSecurityAnalysisResultCount());
    }

    @Test
    public void runWithoutContingency() {
        Network network = EurostagTutorialExample1Factory.create();
        ComputationManager computationManager = createMockComputationManager();

        ContingenciesProvider contingenciesProvider = Mockito.mock(ContingenciesProvider.class);
        Mockito.when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.emptyList());

        List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();
        SecurityAnalysisInterceptorMock interceptorMock = new SecurityAnalysisInterceptorMock();
        interceptors.add(interceptorMock);

        SecurityAnalysisResult result = SecurityAnalysis.run(network,
                VariantManagerConstants.INITIAL_VARIANT_ID,
                new DefaultLimitViolationDetector(),
                new LimitViolationFilter(),
                computationManager,
                SecurityAnalysisParameters.load(platformConfig),
                contingenciesProvider,
                interceptors);

        assertTrue(result.getPreContingencyResult().isComputationOk());
        assertEquals(0, result.getPreContingencyResult().getLimitViolations().size());
        assertEquals(0, result.getPostContingencyResults().size());

        assertEquals(0, interceptorMock.getOnPostContingencyResultCount());
        assertEquals(1, interceptorMock.getOnPreContingencyResultCount());
        assertEquals(1, interceptorMock.getOnSecurityAnalysisResultCount());
    }

    private static ComputationManager createMockComputationManager() {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        Executor executor = Runnable::run;
        Mockito.when(computationManager.getExecutor()).thenReturn(executor);
        ComputationResourcesStatus computationResourcesStatus = Mockito.mock(ComputationResourcesStatus.class);
        Mockito.when(computationResourcesStatus.getAvailableCores()).thenReturn(4);
        Mockito.when(computationManager.getResourcesStatus()).thenReturn(computationResourcesStatus);
        return computationManager;
    }
}
