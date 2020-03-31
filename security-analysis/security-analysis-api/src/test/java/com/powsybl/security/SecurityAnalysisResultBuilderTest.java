package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.interceptors.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

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
    public void completeResult() {
        SecurityAnalysisInterceptor securityAnalysisInterceptorMock = new MockInterceptor();
        MockViolationContextExt preVioContextExt = new MockViolationContextExt();

        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                new RunningContext(network, network.getVariantManager().getWorkingVariantId()), Collections.singleton(securityAnalysisInterceptorMock));

        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        vl.getBusView().getBusStream().forEach(b -> b.setV(410));

        SecurityAnalysisResultBuilder.PreContingencyResultBuilder preContingencyResultBuilder = builder.preContingency();
        SecurityAnalysisResultBuilder securityAnalysisResultBuilder = preContingencyResultBuilder.setComputationOk(true)
                .addViolationContextExtension(Security.checkLimits(network).get(0), preVioContextExt)
                .endPreContingency();
        SecurityAnalysisResult build = securityAnalysisResultBuilder.build();
        LimitViolationsResult preContingencyResult = build.getPreContingencyResult();
        assertEquals(1, preContingencyResult.getLimitViolations().size());
        assertEquals(1, preVioContextExt.calledCount);

        builder.preContingency()
                .setComputationOk(true)
                .addViolations(Security.checkLimits(network))
                .endPreContingency();

        vl.getBusView().getBusStream().forEach(b -> b.setV(380));

        MockContingencyContextExt contingencyContextExtMock = new MockContingencyContextExt();

        builder.contingency(new Contingency("contingency1"))
                .addContingencyContextExtension(contingencyContextExtMock)
                .setComputationOk(true)
                .addViolations(Security.checkLimits(network))
                .endContingency();
        assertEquals(1, contingencyContextExtMock.calledCount);
        vl.getBusView().getBusStream().forEach(b -> b.setV(520));
        List<LimitViolation> limitViolations = Security.checkLimits(network);
        MockViolationContextExt violationContextExt = new MockViolationContextExt();
        builder.contingency(new Contingency("contingency2"))
                .setComputationOk(true)
                .addViolations(limitViolations)
                .addViolationContextExtension(limitViolations.get(0), violationContextExt)
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
        assertEquals(1, violationContextExt.calledCount);
    }

    static class MockContingencyContextExt extends AbstractExtension<ContingencyContext> {

        private int calledCount = 0;

        @Override
        public String getName() {
            return "mock";
        }

        public void foo() {
            calledCount++;
        }
    }

    static class MockViolationContextExt extends AbstractExtension<ViolationContext> {

        private int calledCount = 0;

        @Override
        public String getName() {
            return "mockViolationContextExt";
        }

        public void bar() {
            calledCount++;
        }

    }

    static class MockInterceptor extends DefaultSecurityAnalysisInterceptor {

        @Override
        public void onPostContingencyResult(ContingencyContext context, PostContingencyResult postContingencyResult) {
            MockContingencyContextExt extension = context.getExtension(MockContingencyContextExt.class);
            if (extension != null) {
                assertEquals("contingency1", postContingencyResult.getContingency().getId());
                extension.foo();
            }
        }

        @Override
        public void onLimitViolation(ViolationContext context, LimitViolation limitViolation) {
            MockViolationContextExt ext = context.getExtension(MockViolationContextExt.class);
            if (ext != null) {
                ext.bar();
            }
        }
    }

}
