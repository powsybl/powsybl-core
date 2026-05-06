/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingencyscreening.security.analysis;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingencyscreening.security.analysis.parameters.ContingencyScreeningSecurityAnalysisParameters;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.*;
import com.powsybl.security.results.ConnectivityResult;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.results.PreContingencyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}*/
class ContingencyScreeningSecurityAnalysisHandlerTest {

    private Network network;
    private String workingVariantId;
    private ContingenciesProvider contingenciesProvider;
    private ContingencyScreeningSecurityAnalysisHandler contingencyScreeningSecurityAnalysisHandler;
    private Contingency contingency1;
    private Contingency contingency2;
    private SecurityAnalysisProvider firstProvider;
    private SecurityAnalysisProvider secondProvider;
    private SecurityAnalysisRunParameters runParameters;

    @BeforeEach
    void setUp() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withSeverity(TypedValue.TRACE_SEVERITY)
                .withMessageTemplate("test")
                .build();

        network = EurostagTutorialExample1Factory.create();

        runParameters = SecurityAnalysisRunParameters.getDefault();
        runParameters.setReportNode(reportNode);
        contingenciesProvider = mock(ContingenciesProvider.class);
        firstProvider = mock(SecurityAnalysisProvider.class);
        when(firstProvider.getName()).thenReturn("FirstProvider");
        secondProvider = mock(SecurityAnalysisProvider.class);
        when(secondProvider.getName()).thenReturn("SecondProvider");

        ContingencyScreeningSecurityAnalysisParameters extension = new ContingencyScreeningSecurityAnalysisParameters();
        extension.setFirstProviderName("FirstProvider");
        extension.setSecondProviderName("SecondProvider");
        contingency1 = new Contingency("contingency-1");
        contingency2 = new Contingency("contingency-2");

        workingVariantId = network.getVariantManager().getWorkingVariantId();
        contingencyScreeningSecurityAnalysisHandler = new ContingencyScreeningSecurityAnalysisHandler(network, workingVariantId, contingenciesProvider,
                runParameters, extension, firstProvider, secondProvider);
    }

    @Test
    void testSwitchOnLimitViolations() {
        when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.singletonList(contingency1));

        PostContingencyResult firstResult = createPostContingencyResult(contingency1, false, 0);
        SecurityAnalysisReport firstReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(),
                        Collections.singletonList(firstResult),
                        Collections.emptyList())
        );
        when(firstProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(firstReport));

        PostContingencyResult secondResult = createPostContingencyResult(contingency1, true, 2);
        SecurityAnalysisReport secondReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(),
                        Collections.singletonList(secondResult),
                        Collections.emptyList())
        );
        when(secondProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(secondReport));

        // 1. Launch the contingency screening analysis
        SecurityAnalysisReport report = contingencyScreeningSecurityAnalysisHandler.run().join();

        // 2. Test the result
        assertNotNull(report);
        assertNotNull(report.getResult());

        // Check that we have results for our contingency
        List<PostContingencyResult> postContingencyResults = report.getResult().getPostContingencyResults();
        assertEquals(1, postContingencyResults.size());

        // Verify the status of the first result (should be the second one)
        PostContingencyResult result = postContingencyResults.getFirst();
        assertEquals("contingency-1", result.getContingency().getId());
        assertEquals(2, result.getLimitViolationsResult().getLimitViolations().size());
        assertSame(PostContingencyComputationStatus.CONVERGED, result.getStatus());
    }

    @Test
    void testRunMergesResultsProperly() {
        List<Contingency> allContingencies = Arrays.asList(contingency1, contingency2);
        when(contingenciesProvider.getContingencies(network)).thenReturn(allContingencies);
        // contingency1: first FAILED, second CONVERGED (2 violations)
        PostContingencyResult firstResult1 = createPostContingencyResult(contingency1, false, 0);
        // contingency2: first CONVERGED
        PostContingencyResult firstResult2 = createPostContingencyResult(contingency2, true, 0);
        PreContingencyResult preResult = new PreContingencyResult();
        SecurityAnalysisReport firstReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(preResult,
                        Arrays.asList(firstResult1, firstResult2),
                        Collections.emptyList())
        );
        when(firstProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(firstReport));

        PostContingencyResult secondResult1 = createPostContingencyResult(contingency1, true, 2);
        SecurityAnalysisReport secondReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(preResult,
                        Collections.singletonList(secondResult1),
                        Collections.emptyList())
        );
        when(secondProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(secondReport));

        SecurityAnalysisReport report = contingencyScreeningSecurityAnalysisHandler.run().join();

        List<PostContingencyResult> results = report.getResult().getPostContingencyResults();
        assertEquals(2, results.size());
        PostContingencyResult result1 = results.stream()
                .filter(r -> "contingency-1".equals(r.getContingency().getId()))
                .findFirst()
                .orElseThrow();
        // second result overrides first: CONVERGED status and 2 violations win
        assertSame(PostContingencyComputationStatus.CONVERGED, result1.getStatus());
        assertEquals(2, result1.getLimitViolationsResult().getLimitViolations().size());

        PostContingencyResult result2 = results.stream()
                .filter(r -> "contingency-2".equals(r.getContingency().getId()))
                .findFirst()
                .orElseThrow();
        // first result kept for contingency2
        assertSame(PostContingencyComputationStatus.CONVERGED, result2.getStatus());
    }

    @Test
    void testRunHandlesException() {
        List<Contingency> allContingencies = Collections.singletonList(contingency1);
        when(contingenciesProvider.getContingencies(network)).thenReturn(allContingencies);
        when(firstProvider.run(any(), any(), any(), any())).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("first failure"))
        );

        SecurityAnalysisReport report = contingencyScreeningSecurityAnalysisHandler.run().join();

        assertNotNull(report);
        assertEquals(0, report.getResult().getPostContingencyResults().size());
    }

    @Test
    void testRunReturnsFirstPassWhenNoContingencyRequiresSecondPass() {
        when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.singletonList(contingency1));

        PostContingencyResult convergedResult = createPostContingencyResult(contingency1,
                PostContingencyComputationStatus.CONVERGED, 0);
        SecurityAnalysisReport firstReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(),
                        Collections.singletonList(convergedResult),
                        Collections.emptyList())
        );
        when(firstProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(firstReport));

        SecurityAnalysisReport report = contingencyScreeningSecurityAnalysisHandler.run().join();

        assertSame(firstReport, report);
        verify(secondProvider, never()).run(any(), any(), any(), any());
    }

    @Test
    void testRunSecondPassOnlyForDivergingContingencies() {
        List<Contingency> allContingencies = Arrays.asList(contingency1, contingency2);
        when(contingenciesProvider.getContingencies(network)).thenReturn(allContingencies);

        Contingency contingency3 = new Contingency("contingency-3");

        PostContingencyResult noImpactResult = createPostContingencyResult(contingency1,
                PostContingencyComputationStatus.NO_IMPACT, 0);
        PostContingencyResult failedResult = createPostContingencyResult(contingency2,
                PostContingencyComputationStatus.FAILED, 0);
        PostContingencyResult unknownContingencyResult = createPostContingencyResult(contingency3,
                PostContingencyComputationStatus.SOLVER_FAILED, 0);

        SecurityAnalysisReport firstReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(),
                        Arrays.asList(noImpactResult, failedResult, unknownContingencyResult),
                        Collections.emptyList())
        );
        when(firstProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(firstReport));

        PostContingencyResult secondPassResult = createPostContingencyResult(contingency2,
                PostContingencyComputationStatus.CONVERGED, 1);
        when(secondProvider.run(eq(network), eq(network.getVariantManager().getWorkingVariantId()), any(), any()))
                .thenAnswer(invocation -> {
                    ContingenciesProvider filteredProvider = invocation.getArgument(2);
                    assertEquals(Collections.singletonList(contingency2), filteredProvider.getContingencies(network));
                    return CompletableFuture.completedFuture(new SecurityAnalysisReport(
                            new SecurityAnalysisResult(new PreContingencyResult(),
                                    Collections.singletonList(secondPassResult),
                                    Collections.emptyList())
                    ));
                });

        SecurityAnalysisReport report = contingencyScreeningSecurityAnalysisHandler.run().join();

        List<PostContingencyResult> results = report.getResult().getPostContingencyResults();
        assertEquals(3, results.size());
        assertSame(PostContingencyComputationStatus.NO_IMPACT, results.get(0).getStatus());
        assertSame(PostContingencyComputationStatus.CONVERGED, results.get(1).getStatus());
        assertEquals(1, results.get(1).getLimitViolationsResult().getLimitViolations().size());
        assertSame(PostContingencyComputationStatus.SOLVER_FAILED, results.get(2).getStatus());
    }

    @Test
    void testConstructorWithServiceLoader() {
        ContingencyScreeningSecurityAnalysisParameters extension = createExtension("NonExistentProvider1", "NonExistentProvider2");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new ContingencyScreeningSecurityAnalysisHandler(network, workingVariantId,
                        contingenciesProvider, runParameters, extension));

        assertEquals("Security analysis provider 'NonExistentProvider1' not found by ServiceLoader.", exception.getMessage());
    }

    @Test
    void testConstructorRequiresProviders() {
        ContingencyScreeningSecurityAnalysisParameters extension = createExtension("FirstProvider", "SecondProvider");

        NullPointerException firstProviderException = assertThrows(NullPointerException.class, () ->
                new ContingencyScreeningSecurityAnalysisHandler(network, workingVariantId,
                        contingenciesProvider, runParameters, extension,
                        null, secondProvider));
        assertEquals("First provider is required", firstProviderException.getMessage());

        NullPointerException secondProviderException = assertThrows(NullPointerException.class, () ->
                new ContingencyScreeningSecurityAnalysisHandler(network, workingVariantId,
                        contingenciesProvider, runParameters, extension,
                        firstProvider, null));
        assertEquals("Second provider is required", secondProviderException.getMessage());
    }

    @Test
    void testMergeResultsWithNetworkMetadata() {
        when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.singletonList(contingency1));

        PostContingencyResult firstResult = createPostContingencyResult(contingency1, true, 0);
        SecurityAnalysisResult firstSar = new SecurityAnalysisResult(new PreContingencyResult(),
                Collections.singletonList(firstResult),
                Collections.emptyList());
        firstSar.setNetworkMetadata(mock(NetworkMetadata.class));
        SecurityAnalysisReport firstReport = new SecurityAnalysisReport(firstSar);

        when(firstProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(firstReport));

        SecurityAnalysisReport report = contingencyScreeningSecurityAnalysisHandler.run().join();
        assertNotNull(report.getResult().getNetworkMetadata());
    }

    @Test
    void testRunKeepsFirstPassLogBytesAfterMerge() {
        when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.singletonList(contingency1));

        byte[] logBytes = new byte[]{1, 2, 3};
        PostContingencyResult firstResult = createPostContingencyResult(contingency1,
                PostContingencyComputationStatus.FAILED, 0);
        SecurityAnalysisReport firstReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(),
                        Collections.singletonList(firstResult),
                        Collections.emptyList())
        ).setLogBytes(logBytes);
        when(firstProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(firstReport));

        PostContingencyResult secondResult = createPostContingencyResult(contingency1,
                PostContingencyComputationStatus.CONVERGED, 1);
        SecurityAnalysisReport secondReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(),
                        Collections.singletonList(secondResult),
                        Collections.emptyList())
        );
        when(secondProvider.run(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(secondReport));

        SecurityAnalysisReport report = contingencyScreeningSecurityAnalysisHandler.run().join();

        assertArrayEquals(logBytes, report.getLogBytes().orElseThrow());
    }

    private PostContingencyResult createPostContingencyResult(Contingency contingency,
                                                              boolean converged,
                                                              int violationCount) {
        PostContingencyComputationStatus status = converged
            ? PostContingencyComputationStatus.CONVERGED
            : PostContingencyComputationStatus.FAILED;
        return createPostContingencyResult(contingency, status, violationCount);
    }

    private static ContingencyScreeningSecurityAnalysisParameters createExtension(String firstProviderName, String secondProviderName) {
        ContingencyScreeningSecurityAnalysisParameters extension = new ContingencyScreeningSecurityAnalysisParameters();
        extension.setFirstProviderName(firstProviderName);
        extension.setSecondProviderName(secondProviderName);
        return extension;
    }

    private PostContingencyResult createPostContingencyResult(Contingency contingency,
                                                              PostContingencyComputationStatus status,
                                                              int violationCount) {
        LimitViolationsResult limitViolations = new LimitViolationsResult(
            new ArrayList<>(Collections.nCopies(violationCount, null))
        );
        return new PostContingencyResult(
            contingency,
            status,
            limitViolations,
            NetworkResult.empty(),
            ConnectivityResult.empty(),
            Double.NaN
        );
    }
}
