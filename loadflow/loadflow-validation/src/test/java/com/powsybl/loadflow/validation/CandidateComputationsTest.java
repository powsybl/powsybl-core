package com.powsybl.loadflow.validation;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class CandidateComputationsTest {

    private static <T> Supplier<T> failure() {
        return () -> {
            fail();
            return null;
        };
    }

    @Test
    public void loadFlowExists() {
        CandidateComputation computation = CandidateComputations.getComputation("loadflow")
                .orElseGet(failure());
        assertNotNull(computation);
        assertEquals("loadflow", computation.getName());
    }

    @AutoService(CandidateComputation.class)
    public static class DummyComputation implements CandidateComputation {

        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public void run(Network network, ComputationManager computationManager) throws Exception {
            network.getGenerator("GEN").getTerminal().setP(126f);
        }
    }

    @Test
    public void runDummyComputation() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();

        CandidateComputation computation = CandidateComputations.getComputation("dummy").orElseGet(failure());
        assertNotNull(computation);
        assertEquals("dummy", computation.getName());

        computation.run(network, null);
        assertEquals(126f, network.getGenerator("GEN").getTerminal().getP(), 0f);
    }

}
