package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.interceptors.RunningContext;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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

        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder(new LimitViolationFilter(),
                new RunningContext(network, network.getVariantManager().getWorkingVariantId()));

        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        vl.getBusView().getBusStream().forEach(b -> b.setV(410));

        builder.preContingency()
                .setComputationOk(true)
                .addViolations(Security.checkLimits(network))
                .endPreContingency();

        vl.getBusView().getBusStream().forEach(b -> b.setV(380));

        builder.contingency(new Contingency("contingency1"))
                .setComputationOk(true)
                .addViolations(Security.checkLimits(network))
                .endContingency();

        vl.getBusView().getBusStream().forEach(b -> b.setV(520));
        builder.contingency(new Contingency("contingency2"))
                .setComputationOk(true)
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

}
