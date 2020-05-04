package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.interceptors.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisResultBuilderTest {

    private final Network network = EurostagTutorialExample1Factory.createWithCurrentLimits();

    @Test
    public void failedResult() {

        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                new RunningContext(network, network.getVariantManager().getWorkingVariantId()));

        SecurityAnalysisResult res = builder.preContingency().setComputationOk(false).endPreContingency().build();

        assertFalse(res.getPreContingencyResult().isComputationOk());
        assertTrue(res.getPreContingencyResult().getLimitViolations().isEmpty());
        assertTrue(res.getPostContingencyResults().isEmpty());
    }

    @Test
    public void completeResultWithCustomContext() {
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
                .addViolations(Security.checkLimits(network), preVioContext)
                .endPreContingency();
        assertEquals(Security.checkLimits(network).size(), preVioContext.getCalledCount());
        assertEquals(1, preResultContext.getCalledCount());

        vl.getBusView().getBusStream().forEach(b -> b.setV(380));

        CustomContext postResultContext = new CustomContext(network, "post");
        CustomContext postViolationContext = new CustomContext(network, "post-vio");
        builder.contingency(new Contingency("contingency1"), postResultContext)
                .setComputationOk(true)
                .addViolations(Security.checkLimits(network), postViolationContext)
                .endContingency();
        assertEquals(Security.checkLimits(network).size(), postViolationContext.getCalledCount());
        assertEquals(1, postResultContext.getCalledCount());

        SecurityAnalysisResult result = builder.build();
        assertEquals(4, result.getPreContingencyResult().getLimitViolations().size());
        assertEquals(1, baseContext.getCalledCount());
    }

    @Test
    public void completeResult() {
        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                new RunningContext(network, network.getVariantManager().getWorkingVariantId()));

        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        vl.getBusView().getBusStream().forEach(b -> b.setV(410));

        builder.preContingency()
                .setComputationOk(true)
                .addViolations(Security.checkLimits(network))
                .endPreContingency();

        vl.getBusView().getBusStream().forEach(b -> b.setV(380));

        builder.contingency(new Contingency("contingency1")).setComputationOk(true)
                .addViolations(Security.checkLimits(network))
                .endContingency();

        vl.getBusView().getBusStream().forEach(b -> b.setV(520));
        builder.contingency(new Contingency("contingency2")).setComputationOk(true)
                .addViolations(Security.checkLimits(network))
                .endContingency();

        SecurityAnalysisResult res = builder.build();

        assertTrue(res.getPreContingencyResult().isComputationOk());
        assertEquals(4, res.getPreContingencyResult().getLimitViolations().size());
        assertEquals(2, res.getPostContingencyResults().size());

        PostContingencyResult res1 = res.getPostContingencyResults().get(0);
        assertEquals("contingency1", res1.getContingency().getId());
        assertEquals(2, res.getPostContingencyResults().size());

        List<LimitViolation> violations1 = res1.getLimitViolationsResult().getLimitViolations();
        assertEquals(4, violations1.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, violations1.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(0, violations1.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());

        PostContingencyResult res2 = res.getPostContingencyResults().get(1);
        assertEquals("contingency2", res2.getContingency().getId());
        assertEquals(2, res.getPostContingencyResults().size());

        List<LimitViolation> violations2 = res2.getLimitViolationsResult().getLimitViolations();
        assertEquals(3, violations2.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(0, violations2.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, violations2.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    static class MockContext extends DefaultSecurityAnalysisResultContext {

        private int calledCount = 0;

        public MockContext(Network network) {
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

        public CustomContext(Network network, String name) {
            super(network);
            this.name = Objects.requireNonNull(name);
        }

        public String getName() {
            return name;
        }
    }

    static class MockInterceptor extends DefaultSecurityAnalysisInterceptor {

        @Override
        public void onPreContingencyResult(LimitViolationsResult preContingencyResult, SecurityAnalysisResultContext context) {
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

}
