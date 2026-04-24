package com.powsybl.hybrid.security.analysis;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.hybrid.security.analysis.parameters.HybridModeParametersExtension;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author Riad Benradi {@literal <riad.benradi at rte-france.com>}*/
class HybridSecurityAnalysisTest {

    private Network network;
    private ContingenciesProvider contingenciesProvider;
    private HybridSecurityAnalysis hybridSecurityAnalysis;
    private Contingency contingency1;
    private Contingency contingency2;
    private SecurityAnalysisProvider firstProvider;
    private SecurityAnalysisProvider secondProvider;

    @BeforeEach
    void setUp() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withSeverity(TypedValue.TRACE_SEVERITY)
                .withMessageTemplate("test")
                .build();

        network = EurostagTutorialExample1Factory.create();

        SecurityAnalysisRunParameters runParameters = SecurityAnalysisRunParameters.getDefault();
        runParameters.setReportNode(reportNode);
        contingenciesProvider = mock(ContingenciesProvider.class);
        firstProvider = mock(SecurityAnalysisProvider.class);
        when(firstProvider.getName()).thenReturn("FirstProvider");
        secondProvider = mock(SecurityAnalysisProvider.class);
        when(secondProvider.getName()).thenReturn("SecondProvider");

        HybridModeParametersExtension extension = new HybridModeParametersExtension();
        extension.setFirstProviderName("FirstProvider");
        extension.setSecondProviderName("SecondProvider");
        contingency1 = mock(Contingency.class);

        when(contingency1.getId()).thenReturn("contingency-1");
        contingency2 = mock(Contingency.class);
        when(contingency2.getId()).thenReturn("contingency-2");

        hybridSecurityAnalysis = new HybridSecurityAnalysis(network, network.getVariantManager().getWorkingVariantId(), contingenciesProvider,
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

        // 1. Launch the hybrid analysis
        SecurityAnalysisReport report = hybridSecurityAnalysis.run().join();

        // 2. Test the result
        assertNotNull(report);
        assertNotNull(report.getResult());

        // Check that we have results for our contingency
        List<PostContingencyResult> postContingencyResults = report.getResult().getPostContingencyResults();
        assertEquals(1, postContingencyResults.size());

        // Verify the status of the first result (should be the second one)
        PostContingencyResult result = postContingencyResults.get(0);
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

        SecurityAnalysisReport report = hybridSecurityAnalysis.run().join();

        List<PostContingencyResult> results = report.getResult().getPostContingencyResults();
        assertEquals(2, results.size());
        PostContingencyResult result1 = results.stream()
                .filter(r -> r.getContingency().getId().equals("contingency-1"))
                .findFirst()
                .orElseThrow();
        // second result overrides first: CONVERGED status and 2 violations win
        assertSame(PostContingencyComputationStatus.CONVERGED, result1.getStatus());
        assertEquals(2, result1.getLimitViolationsResult().getLimitViolations().size());

        PostContingencyResult result2 = results.stream()
                .filter(r -> r.getContingency().getId().equals("contingency-2"))
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

        SecurityAnalysisReport report = hybridSecurityAnalysis.run().join();

        assertNotNull(report);
        assertEquals(0, report.getResult().getPostContingencyResults().size());
    }

    @Test
    void testConstructorWithServiceLoader() {
        // This test verifies that the constructor that uses ServiceLoader works.
        HybridModeParametersExtension extension = new HybridModeParametersExtension();
        extension.setFirstProviderName("NonExistentProvider1");
        extension.setSecondProviderName("NonExistentProvider2");

        assertThrows(IllegalArgumentException.class, () ->
            new HybridSecurityAnalysis(network, network.getVariantManager().getWorkingVariantId(),
                contingenciesProvider, SecurityAnalysisRunParameters.getDefault(), extension));
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

        SecurityAnalysisReport report = hybridSecurityAnalysis.run().join();
        assertNotNull(report.getResult().getNetworkMetadata());
    }

    private PostContingencyResult createPostContingencyResult(Contingency contingency,
                                                              boolean converged,
                                                              int violationCount) {
        PostContingencyComputationStatus status = converged
            ? PostContingencyComputationStatus.CONVERGED
            : PostContingencyComputationStatus.FAILED;
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
