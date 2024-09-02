/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.VariantManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
class ShortCircuitAnalysisTest {

    @Test
    void shortCircuitAnalysisWithDummyProvider() {

        ShortCircuitAnalysisProvider provider = new ShortCircuitAnalysisProvider() {
            @Override
            public String getName() {
                return "DummyProvider";
            }

            @Override
            public String getVersion() {
                return "0.0";
            }

            @Override
            public CompletableFuture<ShortCircuitAnalysisResult> run(Network network,
                                                                     List<Fault> fault,
                                                                     ShortCircuitParameters parameters,
                                                                     ComputationManager computationManager,
                                                                     List<FaultParameters> faultParameters) {

                return CompletableFuture.supplyAsync(() -> new ShortCircuitAnalysisResult(Collections.emptyList()));
            }
        };

        ShortCircuitAnalysisResult res = provider.run(null, null, null, null, null).join();

        assertEquals(0, res.getFaultResults().size());
    }

    private static final String DEFAULT_PROVIDER_NAME = "ShortCircuitAnalysisMock";

    private Network network;
    private ComputationManager computationManager;
    private ShortCircuitParameters shortCircuitParameters;
    private List<Fault> faults;
    private List<FaultParameters> faultParameters;

    @BeforeEach
    void setUp() {
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        computationManager = Mockito.mock(ComputationManager.class);
        shortCircuitParameters = Mockito.mock(ShortCircuitParameters.class);
        faults = Mockito.mock(List.class);
        faultParameters = Mockito.mock(List.class);
    }

    @Test
    void test() {
        ShortCircuitAnalysisResult result = TestingResultFactory.createResult();
        assertNotNull(result.getFaultResult("Fault_ID_1"));
        assertEquals(2, result.getFaultResults("BusId").size());
        assertEquals(FaultResult.Status.SUCCESS, result.getFaultResult("Fault_ID_1").getStatus());
    }

    @Test
    void testDefaultProvider() {
        ShortCircuitAnalysis.Runner defaultShortCircuitAnalysisRunner = ShortCircuitAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, defaultShortCircuitAnalysisRunner.getName());
        assertEquals("1.0", defaultShortCircuitAnalysisRunner.getVersion());
    }

    @Test
    void testAsyncDefaultProvider() throws InterruptedException, ExecutionException {
        CompletableFuture<ShortCircuitAnalysisResult> result = ShortCircuitAnalysis.runAsync(network, faults, shortCircuitParameters, computationManager, faultParameters);
        assertNotNull(result.get());
    }

    @Test
    void testSyncDefaultProvider() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults, shortCircuitParameters, computationManager, faultParameters);
        assertNotNull(result);
    }

    @Test
    void testSyncDefaultProviderWithoutComputationManager() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults, shortCircuitParameters, faultParameters);
        assertNotNull(result);
    }

    @Test
    void testSyncDefaultProviderWithoutParameters() {
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults);
        assertNotNull(result);
    }

    @Test
    void testWithReportNode() {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("testReportShortCircuit", "Test mock short circuit").build();
        ShortCircuitAnalysisResult result = ShortCircuitAnalysis.run(network, faults, shortCircuitParameters, computationManager, faultParameters, reportNode);
        assertNotNull(result);
        List<ReportNode> children = reportNode.getChildren();
        assertEquals(1, children.size());

        ReportNode reportNodeChild = children.get(0);
        assertEquals("MockShortCircuit", reportNodeChild.getMessageKey());
        assertEquals("Running mock short circuit", reportNodeChild.getMessage());
        assertTrue(reportNodeChild.getChildren().isEmpty());
    }

    @Test
    void testFortescueTransformation() {
        // test based on a result given in degrees for both fortescue and phase
        double pi = Math.PI;
        FortescueValue fortescueValue = new FortescueValue(86.8086319, 0., 0., 1.83823431 * pi / 180, 0., 0.);
        FortescueValue.ThreePhaseValue threePhaseValue = fortescueValue.toThreePhaseValue();
        assertEquals(50.118988, threePhaseValue.getMagnitudeA(), 0.00001);
        assertEquals(50.118988, threePhaseValue.getMagnitudeB(), 0.00001);
        assertEquals(50.118988, threePhaseValue.getMagnitudeC(), 0.00001);
        assertEquals(1.83823431 * pi / 180, threePhaseValue.getAngleA(), 0.00001);
        assertEquals(-118.161751 * pi / 180, threePhaseValue.getAngleB(), 0.00001);
        assertEquals(121.838219 * pi / 180, threePhaseValue.getAngleC(), 0.00001);
    }

    @Test
    void testWithMissingVoltageRangesInParameters() {
        ShortCircuitParameters invalidShortCircuitParameter = new ShortCircuitParameters()
                .setInitialVoltageProfileMode(InitialVoltageProfileMode.CONFIGURED);
        Exception e0 = assertThrows(PowsyblException.class, () -> ShortCircuitAnalysis.run(network, faults, invalidShortCircuitParameter, computationManager, faultParameters));
        assertEquals("Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing.", e0.getMessage());
    }

    @Test
    void testWithFeederResultsWithoutSide() {
        ShortCircuitAnalysisResult result = TestingResultFactory.createWithFeederResults(null);
        MagnitudeFeederResult feederResult = (MagnitudeFeederResult) result.getFaultResult("id").getFeederResults().get(0);
        assertNotNull(feederResult);
        assertEquals("connectableId", feederResult.getConnectableId());
        assertEquals(1, feederResult.getCurrent());
        assertNull(feederResult.getSide());
    }

    @Test
    void testWithFeederResultsWithSide() {
        ShortCircuitAnalysisResult result = TestingResultFactory.createWithFeederResults(ThreeSides.ONE);
        MagnitudeFeederResult feederResult = (MagnitudeFeederResult) result.getFaultResult("id").getFeederResults().get(0);
        assertNotNull(feederResult);
        assertEquals("connectableId", feederResult.getConnectableId());
        assertEquals(1, feederResult.getCurrent());
        assertEquals(ThreeSides.ONE, feederResult.getSide());
        assertEquals(TwoSides.ONE, feederResult.getSideAsTwoSides());
    }
}
