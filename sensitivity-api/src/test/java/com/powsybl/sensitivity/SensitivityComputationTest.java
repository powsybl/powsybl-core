/*
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.sensitivity.mocks.AnotherSensitivityComputationProviderMock;
import com.powsybl.sensitivity.mocks.SensitivityComputationProviderMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SensitivityComputationTest {

    private PlatformConfig platformConfig;
    private Network network;
    private SensitivityFactorsProvider sensitivityFactorsProvider;
    private ContingenciesProvider contingenciesProvider;

    @Before
    public void setUp() {
        platformConfig = Mockito.mock(PlatformConfig.class);
        network = Mockito.mock(Network.class);
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn("v");
        sensitivityFactorsProvider = Mockito.mock(SensitivityFactorsProvider.class);
        contingenciesProvider = Mockito.mock(ContingenciesProvider.class);
    }

    @Test(expected = PowsyblException.class)
    public void testFindFailNoProvidersNoName() {
        SensitivityComputation.find(null, Collections.emptyList(), platformConfig);
    }

    @Test(expected = PowsyblException.class)
    public void testFindFailNoProvidersWithAName() {
        SensitivityComputation.find("sensitivity-computation-mock", Collections.emptyList(), platformConfig);
    }

    @Test
    public void testFindSuccessWithOneProviderNoName() {
        SensitivityComputation.SensitivityComputationRunner runner =
            SensitivityComputation.find(
                null,
                Collections.singletonList(new SensitivityComputationProviderMock()),
                platformConfig);

        assertNotNull(runner);
        assertEquals("sensitivity-computation-mock", runner.getName());
        assertEquals("1.0", runner.getVersion());
    }

    @Test(expected = PowsyblException.class)
    public void testFindFailWithTwoProvidersNoName() {
        SensitivityComputation.find(
                null,
                Arrays.asList(new SensitivityComputationProviderMock(), new AnotherSensitivityComputationProviderMock()),
                platformConfig);
    }

    @Test
    public void testFindSuccessWithTwoProvidersWithAName() {
        SensitivityComputation.SensitivityComputationRunner runner =
            SensitivityComputation.find(
                "another-sensitivity-computation-mock",
                Arrays.asList(new SensitivityComputationProviderMock(), new AnotherSensitivityComputationProviderMock()),
                platformConfig);

        assertNotNull(runner);
        assertEquals("another-sensitivity-computation-mock", runner.getName());
        assertEquals("2.0", runner.getVersion());
    }

    @Test(expected = PowsyblException.class)
    public void testFindFailWithTwoProvidersWithAName() {
        SensitivityComputation.find(
            "fake-sensitivity-computation",
            Arrays.asList(new SensitivityComputationProviderMock(), new AnotherSensitivityComputationProviderMock()),
            platformConfig);
    }

    @Test
    public void testRunWithoutParamsSuccess() {
        SensitivityComputation.SensitivityComputationRunner runner =
            SensitivityComputation.find(
                "another-sensitivity-computation-mock",
                Arrays.asList(new SensitivityComputationProviderMock(), new AnotherSensitivityComputationProviderMock()),
                platformConfig);
        SensitivityComputationResults results = runner.run(network, sensitivityFactorsProvider, contingenciesProvider);
        assertNotNull(results);
        assertTrue(results.isOk());
    }

    @Test
    public void testRunAsyncWithoutParamsSuccess() {
        SensitivityComputation.SensitivityComputationRunner runner =
            SensitivityComputation.find(
                "another-sensitivity-computation-mock",
                Arrays.asList(new SensitivityComputationProviderMock(), new AnotherSensitivityComputationProviderMock()),
                platformConfig);
        SensitivityComputationResults results = runner.runAsync(network, sensitivityFactorsProvider, contingenciesProvider).join();
        assertNotNull(results);
        assertTrue(results.isOk());
    }

    @Test
    public void testRunWithoutParamsSuccessNoContingencies() {
        SensitivityComputation.SensitivityComputationRunner runner =
            SensitivityComputation.find(
                "another-sensitivity-computation-mock",
                Arrays.asList(new SensitivityComputationProviderMock(), new AnotherSensitivityComputationProviderMock()),
                platformConfig);
        SensitivityComputationResults results = runner.run(network, sensitivityFactorsProvider);
        assertNotNull(results);
        assertTrue(results.isOk());
    }

    @Test
    public void testRunAsyncWithoutParamsSuccessNoContingencies() {
        SensitivityComputation.SensitivityComputationRunner runner =
            SensitivityComputation.find(
                "another-sensitivity-computation-mock",
                Arrays.asList(new SensitivityComputationProviderMock(), new AnotherSensitivityComputationProviderMock()),
                platformConfig);
        SensitivityComputationResults results = runner.runAsync(network, sensitivityFactorsProvider).join();
        assertNotNull(results);
        assertTrue(results.isOk());
    }
}
