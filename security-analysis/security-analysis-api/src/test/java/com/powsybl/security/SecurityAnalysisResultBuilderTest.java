package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.condition.TrueCondition;
import com.powsybl.security.interceptors.*;
import com.powsybl.security.results.*;
import com.powsybl.security.strategy.OperatorStrategy;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class SecurityAnalysisResultBuilderTest {

    private final Network network = EurostagTutorialExample1Factory.createWithCurrentLimits();

    @Test
    void failedResult() {

        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                new RunningContext(network, network.getVariantManager().getWorkingVariantId()));

        SecurityAnalysisResult res = builder.preContingency().setStatus(LoadFlowResult.ComponentResult.Status.FAILED).endPreContingency().build();

        assertSame(LoadFlowResult.ComponentResult.Status.FAILED, res.getPreContingencyResult().getStatus());
        assertTrue(res.getPreContingencyLimitViolationsResult().getLimitViolations().isEmpty());
        assertTrue(res.getPostContingencyResults().isEmpty());
    }

    @Test
    void completeResultWithCustomContext() {
        SecurityAnalysisInterceptor securityAnalysisInterceptorMock = new MockInterceptor();
        CustomContext preResultContext = new CustomContext(network, "pre");
        CustomContext baseContext = new CustomContext(network, "all");
        CustomContext preVioContext = new CustomContext(network, "pre-vio");

        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                baseContext, Collections.singleton(securityAnalysisInterceptorMock));

        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        vl.getBusView().getBusStream().forEach(b -> b.setV(410));

        SecurityAnalysisResultBuilder.PreContingencyResultBuilder preContingencyResultBuilder = builder.preContingency(preResultContext);
        preContingencyResultBuilder
                .setStatus(LoadFlowResult.ComponentResult.Status.CONVERGED)
                .addViolations(Security.checkLimits(network), preVioContext)
                .endPreContingency();
        assertEquals(Security.checkLimits(network).size(), preVioContext.getCalledCount());
        assertEquals(1, preResultContext.getCalledCount());

        vl.getBusView().getBusStream().forEach(b -> b.setV(380));

        CustomContext postResultContext = new CustomContext(network, "post");
        CustomContext postViolationContext = new CustomContext(network, "post-vio");
        builder.contingency(new Contingency("contingency1"), postResultContext)
                .setStatus(PostContingencyComputationStatus.CONVERGED)
                .addViolations(Security.checkLimits(network), postViolationContext)
                .setConnectivityResult(new ConnectivityResult(1, 1, 0.0, 0.0, Collections.emptySet()))
                .endContingency();
        assertEquals(Security.checkLimits(network).size(), postViolationContext.getCalledCount());
        assertEquals(1, postResultContext.getCalledCount());

        SecurityAnalysisResult result = builder.build();
        assertEquals(4, result.getPreContingencyLimitViolationsResult().getLimitViolations().size());
        assertEquals(1, baseContext.getCalledCount());
    }

    @Test
    void completeResult() {
        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                new RunningContext(network, network.getVariantManager().getWorkingVariantId()));

        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        vl.getBusView().getBusStream().forEach(b -> b.setV(410));

        builder.preContingency()
                .addViolations(Security.checkLimits(network))
                .endPreContingency();

        vl.getBusView().getBusStream().forEach(b -> b.setV(380));

        builder.contingency(new Contingency("contingency1"))
                .setStatus(PostContingencyComputationStatus.CONVERGED)
                .addBranchResult(new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0))
                .addBusResult(new BusResult("voltageLevelId", "busId", 400, 3.14))
                .addThreeWindingsTransformerResult(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
                0, 0, 0, 0, 0, 0, 0, 0, 0))
                .addViolations(Security.checkLimits(network))
                .setConnectivityResult(new ConnectivityResult(1, 2, 10.0, 20.0, Set.of("branchId")))
                .endContingency();

        vl.getBusView().getBusStream().forEach(b -> b.setV(520));
        builder.contingency(new Contingency("contingency2"))
                .setStatus(PostContingencyComputationStatus.CONVERGED)
                .addViolations(Security.checkLimits(network))
                .setConnectivityResult(new ConnectivityResult(2, 4, 10.0, 15.0, Set.of("branchId", "branchId2")))
                .endContingency();

        SecurityAnalysisResult res = builder.build();

        assertSame(LoadFlowResult.ComponentResult.Status.CONVERGED, res.getPreContingencyResult().getStatus());
        assertEquals(4, res.getPreContingencyLimitViolationsResult().getLimitViolations().size());
        assertEquals(2, res.getPostContingencyResults().size());

        PostContingencyResult res1 = res.getPostContingencyResults().get(0);
        assertEquals("contingency1", res1.getContingency().getId());
        assertEquals(new BranchResult("branchId", 0, 0, 0, 0, 0, 0, 0), res1.getNetworkResult().getBranchResult("branchId"));
        assertEquals(new BusResult("voltageLevelId", "busId", 400, 3.14), res1.getNetworkResult().getBusResult("busId"));
        assertEquals(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0), res1.getNetworkResult().getThreeWindingsTransformerResult("threeWindingsTransformerId"));
        assertEquals(1, res1.getConnectivityResult().getCreatedSynchronousComponentCount());
        assertEquals(2, res1.getConnectivityResult().getCreatedConnectedComponentCount());
        assertEquals(10.0, res1.getConnectivityResult().getDisconnectedLoadActivePower(), 1e-3);
        assertEquals(20.0, res1.getConnectivityResult().getDisconnectedGenerationActivePower(), 1e-3);
        assertEquals(Set.of("branchId"), res1.getConnectivityResult().getDisconnectedElements());
        assertEquals(2, res.getPostContingencyResults().size());

        List<LimitViolation> violations1 = res1.getLimitViolationsResult().getLimitViolations();
        assertEquals(4, violations1.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, violations1.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(0, violations1.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());

        PostContingencyResult res2 = res.getPostContingencyResults().get(1);
        assertEquals("contingency2", res2.getContingency().getId());
        assertEquals(2, res2.getConnectivityResult().getCreatedSynchronousComponentCount());
        assertEquals(4, res2.getConnectivityResult().getCreatedConnectedComponentCount());
        assertEquals(10.0, res2.getConnectivityResult().getDisconnectedLoadActivePower(), 1e-3);
        assertEquals(15.0, res2.getConnectivityResult().getDisconnectedGenerationActivePower(), 1e-3);
        assertEquals(Set.of("branchId", "branchId2"), res2.getConnectivityResult().getDisconnectedElements());
        assertEquals(2, res.getPostContingencyResults().size());

        List<LimitViolation> violations2 = res2.getLimitViolationsResult().getLimitViolations();
        assertEquals(3, violations2.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(0, violations2.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, violations2.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    static class MockContext extends DefaultSecurityAnalysisResultContext {

        private int calledCount = 0;

        MockContext(Network network) {
            super(network);
        }

        final void foo() {
            calledCount++;
        }

        int getCalledCount() {
            return calledCount;
        }
    }

    static class CustomContext extends MockContext {

        private final String name;

        CustomContext(Network network, String name) {
            super(network);
            this.name = Objects.requireNonNull(name);
        }

        String getName() {
            return name;
        }
    }

    public static class MockInterceptor extends DefaultSecurityAnalysisInterceptor {

        @Override
        public void onPreContingencyResult(PreContingencyResult preContingencyResult, SecurityAnalysisResultContext context) {
            if (context instanceof CustomContext) {
                CustomContext customContext = (CustomContext) context;
                customContext.foo();
                assertEquals("pre", customContext.getName());
            }
        }

        @Override
        public void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context) {
            if (context instanceof CustomContext) {
                CustomContext customContext = (CustomContext) context;
                customContext.foo();
                assertEquals("post", customContext.getName());
            }
        }

        @Override
        public void onLimitViolation(LimitViolation limitViolation, SecurityAnalysisResultContext context) {
            if (context instanceof CustomContext) {
                CustomContext customContext = (CustomContext) context;
                assertEquals("pre-vio", customContext.getName());
                customContext.foo();
            }
        }

        @Override
        public void onLimitViolation(Contingency contingency, LimitViolation limitViolation, SecurityAnalysisResultContext context) {
            if (context instanceof CustomContext) {
                CustomContext customContext = (CustomContext) context;
                assertEquals("post-vio", customContext.getName());
                customContext.foo();
            }
        }

        @Override
        public void onSecurityAnalysisResult(SecurityAnalysisResult result, SecurityAnalysisResultContext context) {
            if (context instanceof CustomContext) {
                CustomContext customContext = (CustomContext) context;
                customContext.foo();
                assertEquals("all", customContext.getName());
            }
        }
    }

    @Test
    void operatorStrategyResultCreation() {
        //Build result with 1 operator strategy
        OperatorStrategy operatorStrategy = new OperatorStrategy("strat1", ContingencyContext.specificContingency("cont1"), new TrueCondition(),
                List.of("action1", "action2"));

        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                new RunningContext(network, network.getVariantManager().getWorkingVariantId()));

        LimitViolation violation = LimitViolations.highVoltage().subject("VLHV1").value(425).limit(420).build();
        BusResult busResult = new BusResult("VLHV2", "VLHV2_0", 426.2, 0.12);
        builder.operatorStrategy(operatorStrategy)
                .addViolation(violation)
                .addBusResult(busResult)
                .setStatus(PostContingencyComputationStatus.CONVERGED)
                .endOperatorStrategy();

        SecurityAnalysisResult result = builder.build();

        //Check content
        assertEquals(1, result.getOperatorStrategyResults().size());

        OperatorStrategyResult strategyResult = result.getOperatorStrategyResults().get(0);
        assertSame(operatorStrategy, strategyResult.getOperatorStrategy());
        OperatorStrategyResult.ConditionalActionsResult finalResult = strategyResult.getConditionalActionsResult().get(0);
        LimitViolationsResult violationsResult = finalResult.getLimitViolationsResult();
        assertSame(PostContingencyComputationStatus.CONVERGED, finalResult.getStatus());
        assertEquals(1, violationsResult.getLimitViolations().size());
        assertSame(violation, violationsResult.getLimitViolations().get(0));
        NetworkResult networkResult = finalResult.getNetworkResult();
        assertEquals(1, networkResult.getBusResults().size());
        assertEquals(busResult, networkResult.getBusResults().get(0));
        assertTrue(networkResult.getBranchResults().isEmpty());
        assertTrue(networkResult.getThreeWindingsTransformerResults().isEmpty());
    }
}
