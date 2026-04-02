package com.powsybl.mixed.security.analysis;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.mixed.security.analysis.parameters.MixedModeParametersExtension;
import com.powsybl.security.*;
import com.powsybl.security.results.ConnectivityResult;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.results.PreContingencyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.powsybl.loadflow.LoadFlowResult.ComponentResult.Status.CONVERGED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MixedSecurityAnalysisTest {
    @Mock
    private Network network;
    @Mock
    private ContingenciesProvider contingenciesProvider;
    @Mock
    private SecurityAnalysisRunParameters runParameters;
    @Mock
    private SecurityAnalysisParameters securityAnalysisParameters;
    @Mock
    private LoadFlowParameters loadFlowParameters;
    @Mock
    private SecurityAnalysisProvider staticProvider;
    @Mock
    private SecurityAnalysisProvider dynamicProvider;
    private MixedSecurityAnalysis mixedAnalysis;
    private Contingency contingency1;
    private Contingency contingency2;

    @BeforeEach
    void setUp() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withSeverity(TypedValue.TRACE_SEVERITY)
                .withMessageTemplate("test")
                .build();
        MockitoAnnotations.openMocks(this);
        runParameters.setReportNode(reportNode);
        when(runParameters.getSecurityAnalysisParameters()).thenReturn(securityAnalysisParameters);
        when(securityAnalysisParameters.getLoadFlowParameters()).thenReturn(loadFlowParameters);
        when(staticProvider.getName()).thenReturn("OpenLoadFlow");
        when(dynamicProvider.getName()).thenReturn("dynaFlow");
        MixedModeParametersExtension extension = new MixedModeParametersExtension();
        extension.setStaticSimulator("OpenLoadFlow");
        extension.setDynamicSimulator("dynaFlow");
        extension.setSwitchCriteria(Collections.singletonList("FAILED"));
        contingency1 = mock(Contingency.class);
        when(contingency1.getId()).thenReturn("contingency-1");
        contingency2 = mock(Contingency.class);
        when(contingency2.getId()).thenReturn("contingency-2");
        mixedAnalysis = new MixedSecurityAnalysis(network, "main", contingenciesProvider,
                                                  runParameters, extension,
                                                  Arrays.asList(staticProvider, dynamicProvider));
    }

    @Test
    void testRunSuccessfulNoSwitchNeeded() {
        List<Contingency> allContingencies = Arrays.asList(contingency1, contingency2);
        when(contingenciesProvider.getContingencies(network)).thenReturn(allContingencies);
        PostContingencyResult staticResult1 = createPostContingencyResult(contingency1, true, 0);
        PostContingencyResult staticResult2 = createPostContingencyResult(contingency2, true, 0);
        PreContingencyResult preResult = new PreContingencyResult(CONVERGED,
            new LimitViolationsResult(Collections.emptyList()),
            NetworkResult.empty(), Double.NaN);
        SecurityAnalysisReport staticReport = new SecurityAnalysisReport(
            new SecurityAnalysisResult(preResult,
                Arrays.asList(staticResult1, staticResult2),
                Collections.emptyList())
        );
        when(staticProvider.run(any(), any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(staticReport));

        SecurityAnalysisReport report = mixedAnalysis.run().join();

        verify(staticProvider).run(network, "main", contingenciesProvider, runParameters);
        verify(dynamicProvider, never()).run(any(), any(), any(), any());
        assertEquals(2, report.getResult().getPostContingencyResults().size());
    }

    @Test
    void testRunWithSwitchTooDynamic() {
        List<Contingency> allContingencies = Arrays.asList(contingency1, contingency2);
        when(contingenciesProvider.getContingencies(network)).thenReturn(allContingencies);
        // contingency1 fails static → triggers switch to dynamic
        PostContingencyResult staticResult1 = createPostContingencyResult(contingency1, false, 0);
        PostContingencyResult staticResult2 = createPostContingencyResult(contingency2, true, 0);
        PreContingencyResult preResult = new PreContingencyResult(CONVERGED,
            new LimitViolationsResult(Collections.emptyList()),
            NetworkResult.empty(), Double.NaN);
        SecurityAnalysisReport staticReport = new SecurityAnalysisReport(
            new SecurityAnalysisResult(preResult,
                Arrays.asList(staticResult1, staticResult2),
                Collections.emptyList())
        );
        PostContingencyResult dynamicResult1 = createPostContingencyResult(contingency1, true, 0);
        SecurityAnalysisReport dynamicReport = new SecurityAnalysisReport(
            new SecurityAnalysisResult(preResult,
                Collections.singletonList(dynamicResult1),
                Collections.emptyList())
        );
        when(staticProvider.run(any(), any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(staticReport));
        when(dynamicProvider.run(any(), any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(dynamicReport));

        SecurityAnalysisReport report = mixedAnalysis.run().join();

        verify(staticProvider).run(network, "main", contingenciesProvider, runParameters);
        verify(dynamicProvider).run(any(), any(), any(), any());
        assertEquals(2, report.getResult().getPostContingencyResults().size());
    }

    @Test
    void testRunMergesResultsProperly() {
        List<Contingency> allContingencies = Arrays.asList(contingency1, contingency2);
        when(contingenciesProvider.getContingencies(network)).thenReturn(allContingencies);
        // contingency1: static FAILED (1 violation), dynamic CONVERGED (2 violations)
        PostContingencyResult staticResult1 = createPostContingencyResult(contingency1, false, 1);
        PostContingencyResult staticResult2 = createPostContingencyResult(contingency2, true, 0);
        PreContingencyResult preResult = new PreContingencyResult(CONVERGED,
            new LimitViolationsResult(Collections.emptyList()),
            NetworkResult.empty(), Double.NaN);
        SecurityAnalysisReport staticReport = new SecurityAnalysisReport(
            new SecurityAnalysisResult(preResult,
                Arrays.asList(staticResult1, staticResult2),
                Collections.emptyList())
        );
        PostContingencyResult dynamicResult1 = createPostContingencyResult(contingency1, true, 2);
        SecurityAnalysisReport dynamicReport = new SecurityAnalysisReport(
            new SecurityAnalysisResult(preResult,
                Collections.singletonList(dynamicResult1),
                Collections.emptyList())
        );
        when(staticProvider.run(any(), any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(staticReport));
        when(dynamicProvider.run(any(), any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(dynamicReport));

        SecurityAnalysisReport report = mixedAnalysis.run().join();

        List<PostContingencyResult> results = report.getResult().getPostContingencyResults();
        assertEquals(2, results.size());
        PostContingencyResult result1 = results.stream()
            .filter(r -> r.getContingency().getId().equals("contingency-1"))
            .findFirst()
            .orElseThrow();
        // dynamic result overrides static: CONVERGED status and 2 violations win
        assertSame(PostContingencyComputationStatus.CONVERGED, result1.getStatus());
        assertEquals(2, result1.getLimitViolationsResult().getLimitViolations().size());
    }

    @Test
    void testRunHandlesException() {
        List<Contingency> allContingencies = Collections.singletonList(contingency1);
        when(contingenciesProvider.getContingencies(network)).thenReturn(allContingencies);
        when(staticProvider.run(any(), any(), any(), any()))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Analysis failed")));

        SecurityAnalysisReport report = mixedAnalysis.run().join();

        assertNotNull(report);
        assertEquals(0, report.getResult().getPostContingencyResults().size());
    }

    /**
     * Integration test covering both workflow paths:
     * Phase 1 — real OpenLoadFlow, both contingencies converge, DynaFlow never called.
     * Phase 2 — mock providers, static FAILED triggers dynamic dispatch, results merged.
     */
    @Test
    void testSimpleMixedSecurityAnalysis() {
        Network network = EurostagTutorialExample1Factory.create();
        Contingency contingencyLine1 = Contingency.line("NHV1_NHV2_1");
        Contingency contingencyLine2 = Contingency.line("NHV1_NHV2_2");
        when(contingenciesProvider.getContingencies(network))
                .thenReturn(Arrays.asList(contingencyLine1, contingencyLine2));

        MixedModeParametersExtension ext = new MixedModeParametersExtension();
        ext.setStaticSimulator("OpenLoadFlow");
        ext.setDynamicSimulator("DynaFlow");
        ext.setSwitchCriteria(Collections.singletonList("FAILED"));

        SecurityAnalysisProvider mockDynaFlow = mock(SecurityAnalysisProvider.class);
        when(mockDynaFlow.getName()).thenReturn("DynaFlow");
        SecurityAnalysisRunParameters params = SecurityAnalysisRunParameters.getDefault();

        // Phase 1: real OpenLoadFlow — both contingencies converge, no dynamic switch
        SecurityAnalysisReport staticOnlyReport = new MixedSecurityAnalysis(
                network, "InitialState", contingenciesProvider, params, ext,
                Collections.singletonList(mockDynaFlow)).run().join();

        PreContingencyResult preResult = staticOnlyReport.getResult().getPreContingencyResult();
        assertNotNull(preResult);
        assertEquals(CONVERGED, preResult.getStatus());
        assertTrue(preResult.getLimitViolationsResult().getLimitViolations().isEmpty());

        List<PostContingencyResult> staticResults = staticOnlyReport.getResult().getPostContingencyResults();
        assertEquals(2, staticResults.size());
        staticResults.forEach(r -> {
            assertSame(PostContingencyComputationStatus.CONVERGED, r.getStatus());
            assertTrue(r.getLimitViolationsResult().getLimitViolations().isEmpty());
        });
        verify(mockDynaFlow, never()).run(any(), any(), any(), any());

        // Phase 2: static FAILED on line1 → DynaFlow dispatched for line1 only, results merged
        PreContingencyResult mockPreResult = new PreContingencyResult(
                CONVERGED, new LimitViolationsResult(Collections.emptyList()),
                NetworkResult.empty(), Double.NaN);

        PostContingencyResult staticLine1Failed = new PostContingencyResult(
                contingencyLine1, PostContingencyComputationStatus.FAILED,
                new LimitViolationsResult(Collections.emptyList()),
                NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        PostContingencyResult staticLine2Converged = new PostContingencyResult(
                contingencyLine2, PostContingencyComputationStatus.CONVERGED,
                new LimitViolationsResult(Collections.emptyList()),
                NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        PostContingencyResult dynamicLine1Converged = new PostContingencyResult(
                contingencyLine1, PostContingencyComputationStatus.CONVERGED,
                new LimitViolationsResult(Collections.emptyList()),
                NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);

        SecurityAnalysisProvider mockOpenLoadFlow = mock(SecurityAnalysisProvider.class);
        when(mockOpenLoadFlow.getName()).thenReturn("OpenLoadFlow");
        when(mockOpenLoadFlow.run(any(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(new SecurityAnalysisReport(new SecurityAnalysisResult(
                        mockPreResult, Arrays.asList(staticLine1Failed, staticLine2Converged),
                        Collections.emptyList()))));
        when(mockDynaFlow.run(any(), any(), any(), any())).thenReturn(
                CompletableFuture.completedFuture(new SecurityAnalysisReport(new SecurityAnalysisResult(
                        mockPreResult, Collections.singletonList(dynamicLine1Converged),
                        Collections.emptyList()))));

        SecurityAnalysisReport fullReport = new MixedSecurityAnalysis(
                network, "InitialState", contingenciesProvider, params, ext,
                Arrays.asList(mockOpenLoadFlow, mockDynaFlow)).run().join();

        verify(mockDynaFlow, times(1)).run(any(), any(), any(), any());

        List<PostContingencyResult> merged = fullReport.getResult().getPostContingencyResults();
        assertEquals(2, merged.size());

        PostContingencyResult mergedLine1 = merged.stream()
                .filter(r -> r.getContingency().getId().equals("NHV1_NHV2_1"))
                .findFirst().orElseThrow();
        PostContingencyResult mergedLine2 = merged.stream()
                .filter(r -> r.getContingency().getId().equals("NHV1_NHV2_2"))
                .findFirst().orElseThrow();
        // line1: dynamic (CONVERGED) overrides static (FAILED)
        assertSame(PostContingencyComputationStatus.CONVERGED, mergedLine1.getStatus());
        // line2: kept from static, DynaFlow was not invoked for it
        assertSame(PostContingencyComputationStatus.CONVERGED, mergedLine2.getStatus());
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