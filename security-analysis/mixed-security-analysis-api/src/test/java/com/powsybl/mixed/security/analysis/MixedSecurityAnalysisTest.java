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
import static org.mockito.ArgumentMatchers.anyString;
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
    void testRunWithSwitchOnNotConverged() {
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

    @Test
    void testSwitchOnSpsTriggered() {
        // 1. Load the network
        Network network = EurostagTutorialExample1Factory.create();

        // 2. Define a simple contingency that will be tested
        Contingency simpleContingency = Contingency.line("NHV1_NHV2_2");
        when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.singletonList(simpleContingency));

        // 3. Configure the mixed analysis to switch on the 'SPS_TRIGGERED' status
        MixedModeParametersExtension ext = new MixedModeParametersExtension();
        ext.setStaticSimulator("OpenLoadFlow");
        ext.setDynamicSimulator("dynaFlow");
        ext.setSwitchCriteria(Collections.singletonList("SPS_TRIGGERED"));

        // 4. Create the MixedSecurityAnalysis instance with the network and mock providers
        MixedSecurityAnalysis analysis = new MixedSecurityAnalysis(
                network, "InitialState", contingenciesProvider,
                runParameters, ext,
                Arrays.asList(staticProvider, dynamicProvider));

        // 5. Mock the static provider to return a result with 'SPS_TRIGGERED' status
        PostContingencyResult staticSpsResult = new PostContingencyResult(
                simpleContingency,
                PostContingencyComputationStatus.SPS_TRIGGERED,
                new LimitViolationsResult(Collections.emptyList()),
                NetworkResult.empty(),
                ConnectivityResult.empty(),
                Double.NaN
        );
        SecurityAnalysisReport staticReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(CONVERGED, new LimitViolationsResult(Collections.emptyList()), NetworkResult.empty(), Double.NaN),
                        Collections.singletonList(staticSpsResult), Collections.emptyList())
        );
        when(staticProvider.run(any(Network.class), anyString(), any(ContingenciesProvider.class), any(SecurityAnalysisRunParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(staticReport));

        // 6. Mock the dynamic provider to return a successful 'CONVERGED' result
        PostContingencyResult dynamicConvergedResult = createPostContingencyResult(simpleContingency, true, 0);
        SecurityAnalysisReport dynamicReport = new SecurityAnalysisReport(
                new SecurityAnalysisResult(new PreContingencyResult(CONVERGED, new LimitViolationsResult(Collections.emptyList()), NetworkResult.empty(), Double.NaN),
                        Collections.singletonList(dynamicConvergedResult), Collections.emptyList())
        );
        when(dynamicProvider.run(any(Network.class), anyString(), any(ContingenciesProvider.class), any(SecurityAnalysisRunParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(dynamicReport));

        // 7. Execute the mixed analysis
        SecurityAnalysisReport report = analysis.run().join();

        // 8. Verify the logic: both providers were called, and the final result is the converged one from the dynamic analysis
        verify(staticProvider).run(network, "InitialState", contingenciesProvider, runParameters);
        verify(dynamicProvider).run(any(Network.class), anyString(), any(ContingenciesProvider.class), any(SecurityAnalysisRunParameters.class));

        List<PostContingencyResult> results = report.getResult().getPostContingencyResults();
        assertEquals(1, results.size());
        PostContingencyResult finalResult = results.getFirst();
        assertEquals(simpleContingency.getId(), finalResult.getContingency().getId());
        assertSame(PostContingencyComputationStatus.CONVERGED, finalResult.getStatus());
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
