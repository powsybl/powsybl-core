/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;

import static org.junit.Assert.*;

public class SensitivityComputationFactoryMockTest {

    @Test
    public void createMock() {
        SensitivityComputationFactory factory = new SensitivityComputationFactoryMock();
        SensitivityComputation computation = factory.create(Mockito.mock(Network.class), Mockito.mock(ComputationManager.class), 0);

        SensitivityFactorsProvider factorsProvider = new JsonSensitivityFactorsProvider(SensitivityComputationFactoryMockTest.class.getResourceAsStream("/sensitivityFactorsExample.json"));
        assertEquals("Sensitivity computation mock", computation.getName());
        SensitivityComputationResults results = computation.run(
                factorsProvider,
                "any",
                Mockito.mock(SensitivityComputationParameters.class)
        ).join();

        assertNotNull(results);
        assertTrue(results.isOk());
        assertFalse(results.contingenciesArePresent());
        assertEquals(factorsProvider.getFactors(Mockito.mock(Network.class)).size(), results.getSensitivityValues().size());
    }

    @Test
    public void createSystematicMock() throws IOException {
        SensitivityComputationFactory factory = new SensitivityComputationFactoryMock();
        SensitivityComputation computation = factory.create(Mockito.mock(Network.class), Mockito.mock(ComputationManager.class), 0);

        // Read sensitivity factors from a JSON file
        SensitivityFactorsProvider factorsProvider = new JsonSensitivityFactorsProvider(SensitivityComputationFactoryMockTest.class.getResourceAsStream("/sensitivityFactorsExample.json"));

        // Read one contingency from a JSON file
        ContingenciesProvider contingenciesProvider = network -> {
            try (InputStream is = SensitivityComputationFactoryMockTest.class.getResourceAsStream("/contingency.json")) {
                ObjectMapper objectMapper = JsonUtil.createObjectMapper();
                ContingencyJsonModule module = new ContingencyJsonModule();
                objectMapper.registerModule(module);

                return Collections.singletonList(objectMapper.readValue(is, Contingency.class));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };

        // Check the consistency of the results
        assertEquals("Sensitivity computation mock", computation.getName());
        SensitivityComputationResults results = computation.run(
                factorsProvider,
                contingenciesProvider,
                "any",
                Mockito.mock(SensitivityComputationParameters.class)
        ).join();

        assertNotNull(results);
        assertTrue(results.isOk());
        assertEquals(factorsProvider.getFactors(Mockito.mock(Network.class)).size(), results.getSensitivityValues().size());
        assertTrue(results.contingenciesArePresent());
        assertEquals(contingenciesProvider.getContingencies(Mockito.mock(Network.class)).size(), results.getSensitivityValuesContingencies().keySet().size());
    }
}
