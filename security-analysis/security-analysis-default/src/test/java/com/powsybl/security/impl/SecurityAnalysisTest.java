/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ComputationResourcesStatus;
import com.powsybl.contingency.*;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.security.*;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;
import com.powsybl.security.impl.interceptors.SecurityAnalysisInterceptorMock;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.results.BranchResult;
import com.powsybl.security.results.BusResults;
import com.powsybl.security.results.PostContingencyResult;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
        Contingency contingencyMock = Mockito.spy(contingency);
        Mockito.when(contingencyMock.toModification()).thenReturn(new NetworkModification() {
            @Override
            public void apply(Network network, ComputationManager computationManager) {
                apply(network);
            }

            @Override
            public void apply(Network network) {
                network.getLine("NHV1_NHV2_2").getTerminal1().disconnect();
                network.getLine("NHV1_NHV2_2").getTerminal2().disconnect();
                network.getLine("NHV1_NHV2_1").getTerminal2().setP(600.0);
            }
        });
        ContingenciesProvider contingenciesProvider = n -> Collections.singletonList(contingencyMock);

        LimitViolationFilter filter = new LimitViolationFilter();
        LimitViolationDetector detector = new DefaultLimitViolationDetector();
        SecurityAnalysisInterceptorMock interceptorMock = new SecurityAnalysisInterceptorMock();
        List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();
        interceptors.add(interceptorMock);

        SecurityAnalysisReport report = SecurityAnalysis.run(network,
                VariantManagerConstants.INITIAL_VARIANT_ID,
                contingenciesProvider, SecurityAnalysisParameters.load(platformConfig), computationManager, filter, detector,
                interceptors);

        SecurityAnalysisResult result = report.getResult();

        assertTrue(result.getPreContingencyLimitViolationsResult().isComputationOk());
        assertEquals(0, result.getPreContingencyLimitViolationsResult().getLimitViolations().size());
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

        Assert.assertEquals(1, interceptorMock.getOnPostContingencyResultCount());
        Assert.assertEquals(1, interceptorMock.getOnPreContingencyResultCount());
        Assert.assertEquals(1, interceptorMock.getOnSecurityAnalysisResultCount());
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

        SecurityAnalysisReport report = SecurityAnalysis.run(network,
                VariantManagerConstants.INITIAL_VARIANT_ID,
                contingenciesProvider, SecurityAnalysisParameters.load(platformConfig), computationManager, new LimitViolationFilter(), new DefaultLimitViolationDetector(),
                interceptors);
        SecurityAnalysisResult result = report.getResult();

        assertTrue(result.getPreContingencyLimitViolationsResult().isComputationOk());
        assertEquals(0, result.getPreContingencyLimitViolationsResult().getLimitViolations().size());
        assertEquals(0, result.getPostContingencyResults().size());

        Assert.assertEquals(0, interceptorMock.getOnPostContingencyResultCount());
        Assert.assertEquals(1, interceptorMock.getOnPreContingencyResultCount());
        Assert.assertEquals(1, interceptorMock.getOnSecurityAnalysisResultCount());
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

    @Test
    public void testStateMonitors() {
        Network network = EurostagTutorialExample1Factory.create();
        ((Bus) network.getIdentifiable("NHV1")).setV(380.0);
        ((Bus) network.getIdentifiable("NHV1")).setAngle(0.0);
        ((Bus) network.getIdentifiable("NHV2")).setV(380.0);
        ((Bus) network.getIdentifiable("NHV2")).setAngle(0.0);
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
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(600.0).setQ(500.0);
        network.getLine("NHV1_NHV2_2").getTerminal2().setP(600.0).setQ(500.0);
        network.getLine("NHV1_NHV2_2").newCurrentLimits1().setPermanentLimit(1500.0).add();
        network.getLine("NHV1_NHV2_2").newCurrentLimits2()
            .setPermanentLimit(1200.0)
            .beginTemporaryLimit()
            .setName("10'")
            .setAcceptableDuration(10 * 60)
            .setValue(1300.0)
            .endTemporaryLimit()
            .add();
        ComputationManager computationManager = createMockComputationManager();
        // Testing all contingencies at once
        ContingenciesProvider contingenciesProvider = n -> n.getBranchStream()
            .map(b -> new Contingency(b.getId(), new BranchContingency(b.getId())))
            .collect(Collectors.toList());

        SecurityAnalysisParameters saParameters = new SecurityAnalysisParameters();

        LimitViolationFilter filter = new LimitViolationFilter();
        LimitViolationDetector detector = new DefaultLimitViolationDetector();

        List<StateMonitor> monitors = new ArrayList<>();
        monitors.add(new StateMonitor(new ContingencyContext("NHV1_NHV2_1", ContingencyContextType.SPECIFIC),
            Collections.singleton("NHV1_NHV2_2"), Collections.singleton("VLHV2"), Collections.emptySet()));
        // this monitor will be filtered because the id of the branch of the state Monitor and the id of the branch of the contingency are the same
        monitors.add(new StateMonitor(new ContingencyContext("NHV1_NHV2_2", ContingencyContextType.SPECIFIC),
            Collections.singleton("NHV1_NHV2_2"), Collections.emptySet(), Collections.emptySet()));
        monitors.add(new StateMonitor(new ContingencyContext(null, ContingencyContextType.NONE),
                Set.of("NHV1_NHV2_1", "NOT_EXISTING_BRANCH"), Set.of("VLHV1", "NOT_EXISTING_VOLTAGE_LEVEL"), Collections.singleton("NOT_EXISTING_T3W"))); // ignore IDs of non existing equipment

        DefaultSecurityAnalysis defaultSecurityAnalysis = new DefaultSecurityAnalysis(network, detector, filter, computationManager, monitors, Reporter.NO_OP);
        SecurityAnalysisReport report = defaultSecurityAnalysis.run(network.getVariantManager().getWorkingVariantId(), saParameters, contingenciesProvider).join();
        SecurityAnalysisResult result = report.getResult();
        Assertions.assertThat(result.getPreContingencyResult().getPreContingencyBusResults()).containsExactly(new BusResults("VLHV1", "VLHV1_0", 380.0, 0.0));
        Assertions.assertThat(result.getPreContingencyResult().getPreContingencyBusResult("VLHV1_0")).isEqualToComparingOnlyGivenFields(new BusResults("VLHV1", "VLHV1_0", 380.0, 0.0));
        Assertions.assertThat(result.getPreContingencyResult().getPreContingencyBranchResults()).containsExactly(new BranchResult("NHV1_NHV2_1",  560.0, 550.0,  1192.5631358010583, 560.0,  550.0, 1192.5631358010583, 0.0));
        Assertions.assertThat(result.getPreContingencyResult().getPreContingencyBranchResult("NHV1_NHV2_1")).isEqualToComparingOnlyGivenFields(new BranchResult("NHV1_NHV2_1",  560.0, 550.0,  1192.5631358010583, 560.0,  550.0, 1192.5631358010583, 0.0));
        Assertions.assertThat(result.getPostContingencyResults().get(0).getBranchResults()).containsExactly(new BranchResult("NHV1_NHV2_2",  600.0, 500.0,  1186.6446717954987, 600.0,  500.0, 1186.6446717954987, 0.0));
        Assertions.assertThat(result.getPostContingencyResults().get(0).getBusResults()).containsExactly(new BusResults("VLHV2", "VLHV2_0", 380.0, 0.0));
    }
}
