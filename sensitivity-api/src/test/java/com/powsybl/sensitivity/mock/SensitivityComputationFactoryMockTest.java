package com.powsybl.sensitivity.mock;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.JsonSensitivityFactorsProvider;
import com.powsybl.sensitivity.SensitivityComputation;
import com.powsybl.sensitivity.SensitivityComputationFactory;
import com.powsybl.sensitivity.SensitivityComputationParameters;
import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.SensitivityFactorsProvider;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class SensitivityComputationFactoryMockTest {

    @Test
    public void createMock() {
        SensitivityComputationFactory factory = new SensitivityComputationFactoryMock();
        SensitivityComputation computation = factory.create(Mockito.mock(Network.class), Mockito.mock(ComputationManager.class), 0);

        SensitivityFactorsProvider provider = new JsonSensitivityFactorsProvider(SensitivityComputationFactoryMockTest.class.getResourceAsStream("/sensitivityFactorsExample.json"));
        assertEquals("Sensitivity computation mock", computation.getName());
        SensitivityComputationResults results = computation.run(
                provider,
                "any",
                Mockito.mock(SensitivityComputationParameters.class)
        ).join();

        assertNotNull(results);
        assertTrue(results.isOk());
        assertEquals(provider.getFactors(Mockito.mock(Network.class)).size(), results.getSensitivityValues().size());
    }
}
