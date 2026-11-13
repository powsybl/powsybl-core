/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class StateEstimationProviderTest {

    private FileSystem fileSystem;
    private InMemoryPlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    /** A provider that implements the three operations and nothing else, to reach the defaults. */
    private static StateEstimationProvider bareProvider() {
        return new StateEstimationProvider() {

            @Override
            public String getName() {
                return "Bare";
            }

            @Override
            public String getVersion() {
                return "1.0";
            }

            @Override
            public CompletableFuture<ObservabilityResult> analyseObservability(Network network, String workingVariantId,
                                                                               StateEstimationRunParameters runParameters) {
                return CompletableFuture.completedFuture(
                        new ObservabilityResultImpl(ObservabilityResult.Status.FAILED, null, List.of(), List.of()));
            }

            @Override
            public CompletableFuture<StateEstimationResult> estimate(Network network, String workingVariantId,
                                                                     StateEstimationRunParameters runParameters) {
                return CompletableFuture.completedFuture(StateEstimationResultImpl.failed(workingVariantId));
            }

            @Override
            public CompletableFuture<BadDataResult> detectBadData(Network network, StateEstimationResult estimate,
                                                                  ObservabilityResult observability,
                                                                  StateEstimationRunParameters runParameters) {
                return CompletableFuture.completedFuture(
                        new BadDataResultImpl(BadDataResult.Status.INCONCLUSIVE, null, List.of(), Set.of()));
            }
        };
    }

    @Test
    void findsEveryProviderOnTheClasspath() {
        List<StateEstimationProvider> providers = StateEstimationProvider.findAll();
        assertEquals(1, providers.size());
        assertEquals(StateEstimationProviderMock.NAME, providers.get(0).getName());
    }

    @Test
    void aProviderWithNoSettingsOfItsOwnAnswersEmpty() {
        StateEstimationProvider provider = bareProvider();

        assertTrue(provider.getSpecificParametersClass().isEmpty());
        assertTrue(provider.getSpecificParametersSerializer().isEmpty());
        assertTrue(provider.loadSpecificParameters(platformConfig).isEmpty());
        assertTrue(provider.loadSpecificParameters(Map.of()).isEmpty());
        assertTrue(provider.getRawSpecificParameters().isEmpty());
        assertTrue(provider.getSpecificParameters().isEmpty());
        assertTrue(provider.getSpecificParameters(platformConfig).isEmpty());
        assertTrue(provider.getModuleConfig(platformConfig).isEmpty());
        assertTrue(provider.createMapFromSpecificParameters(new MockSpecificParameters()).isEmpty());
        assertTrue(provider.checkParameters(StateEstimationRunParameters.getDefault()));

        MockSpecificParameters untouched = new MockSpecificParameters();
        provider.updateSpecificParameters(untouched, Map.of(StateEstimationProviderMock.TOLERANCE, "1e-9"));
        provider.updateSpecificParameters(untouched, platformConfig);
        assertEquals(MockSpecificParameters.DEFAULT_TOLERANCE, untouched.getTolerance());
    }

    @Test
    void appliesTheConfigurationToTheDefaultsItDeclares() {
        StateEstimationProvider provider = new StateEstimationProviderMock();

        List<Parameter> declared = provider.getSpecificParameters(platformConfig);
        assertEquals(MockSpecificParameters.DEFAULT_TOLERANCE, declared.get(0).getDefaultValue());
        assertEquals(MockSpecificParameters.DEFAULT_MAX_ITERATIONS, declared.get(1).getDefaultValue());

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(StateEstimationProviderMock.MODULE_CONFIG_NAME);
        moduleConfig.setStringProperty(StateEstimationProviderMock.TOLERANCE, "1e-7");

        List<Parameter> configured = provider.getSpecificParameters(platformConfig);
        assertEquals(1e-7, configured.get(0).getDefaultValue());
        assertEquals(MockSpecificParameters.DEFAULT_MAX_ITERATIONS, configured.get(1).getDefaultValue());
    }

    @Test
    void declaresItselfAsAPluginType() {
        StateEstimationProviderPluginInfo pluginInfo = new StateEstimationProviderPluginInfo();

        assertSame(StateEstimationProvider.class, pluginInfo.getPluginClass());
        assertEquals(StateEstimationProvider.class.getSimpleName(), pluginInfo.getPluginName());
    }

    @Test
    void theWriterSkipsWhatTheNetworkNoLongerHas() {
        Network network = EurostagTutorialExample1Factory.create();
        StateEstimationResult stale = new StateEstimationResultImpl(
                network.getVariantManager().getWorkingVariantId(),
                List.of(),
                List.of(new StateEstimationResultImpl.BusResultImpl("GONE_0", "GONE", 400.0, 0.0),
                        new StateEstimationResultImpl.BusResultImpl("ALSO_GONE", "VLHV1", 400.0, 0.0)),
                List.of(new StateEstimationResultImpl.BranchResultImpl("GONE_LINE", 1.0, 2.0, 3.0, 4.0)),
                List.of(), List.of(), List.of());

        assertDoesNotThrow(() -> bareProvider().writeResultsToNetwork(
                network, network.getVariantManager().getWorkingVariantId(), stale));
    }

    @Test
    void theWriterFindsABusInTheBusView() {
        Network network = EurostagTutorialExample1Factory.create();
        String busId = busViewBusId(network);

        StateEstimationResult result = new StateEstimationResultImpl(
                network.getVariantManager().getWorkingVariantId(),
                List.of(),
                List.of(new StateEstimationResultImpl.BusResultImpl(busId, "VLGEN", 24.5, 1.5)),
                List.of(), List.of(), List.of(), List.of());

        bareProvider().writeResultsToNetwork(network,
                network.getVariantManager().getWorkingVariantId(), result);

        Bus bus = network.getVoltageLevel("VLGEN").getBusView().getBus(busId);
        assertEquals(24.5, bus.getV(), 1e-12);
        assertEquals(1.5, bus.getAngle(), 1e-12);
    }

    @Test
    void theWriterIgnoresAnIdentifierFromTheOtherView() {
        Network network = EurostagTutorialExample1Factory.create();
        String busId = busViewBusId(network);
        assertNotEquals("NGEN", busId, "the two views must name the bus differently");

        StateEstimationResult result = new StateEstimationResultImpl(
                network.getVariantManager().getWorkingVariantId(),
                List.of(),
                List.of(new StateEstimationResultImpl.BusResultImpl("NGEN", "VLGEN", 24.5, 1.5)),
                List.of(), List.of(), List.of(), List.of());

        bareProvider().writeResultsToNetwork(network,
                network.getVariantManager().getWorkingVariantId(), result);

        assertTrue(Double.isNaN(network.getVoltageLevel("VLGEN").getBusView().getBus(busId).getV()));
    }

    @Test
    void theWriterFillsTheThreeLegsOfATransformer() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        StateEstimationResult result = new StateEstimationResultImpl(
                network.getVariantManager().getWorkingVariantId(),
                List.of(), List.of(), List.of(),
                List.of(new StateEstimationResultImpl.ThreeWindingsTransformerResultImpl(
                        "3WT", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0)),
                List.of(), List.of());

        bareProvider().writeResultsToNetwork(network,
                network.getVariantManager().getWorkingVariantId(), result);

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        assertEquals(1.0, twt.getLeg1().getTerminal().getP(), 1e-12);
        assertEquals(2.0, twt.getLeg1().getTerminal().getQ(), 1e-12);
        assertEquals(3.0, twt.getLeg2().getTerminal().getP(), 1e-12);
        assertEquals(6.0, twt.getLeg3().getTerminal().getQ(), 1e-12);
    }

    @Test
    void theWriterFillsAnInjectionTerminal() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        StateEstimationResult result = new StateEstimationResultImpl(
                network.getVariantManager().getWorkingVariantId(),
                List.of(), List.of(), List.of(), List.of(),
                List.of(new StateEstimationResultImpl.InjectionResultImpl("LOAD_33", 7.5, 2.5)),
                List.of());

        bareProvider().writeResultsToNetwork(network,
                network.getVariantManager().getWorkingVariantId(), result);

        assertEquals(7.5, network.getLoad("LOAD_33").getTerminal().getP(), 1e-12);
        assertEquals(2.5, network.getLoad("LOAD_33").getTerminal().getQ(), 1e-12);
    }

    @Test
    void theWriterTakesTheNetworkSideOfAnUnpairedBoundaryLine() {
        Network network = BoundaryLineNetworkFactory.create();
        StateEstimationResult result = new StateEstimationResultImpl(
                network.getVariantManager().getWorkingVariantId(),
                List.of(), List.of(),
                List.of(new StateEstimationResultImpl.BranchResultImpl("BL", 51.0, 31.0, -50.0, -30.0)),
                List.of(), List.of(), List.of());

        bareProvider().writeResultsToNetwork(network,
                network.getVariantManager().getWorkingVariantId(), result);

        assertEquals(51.0, network.getBoundaryLine("BL").getTerminal().getP(), 1e-12,
                "side 1 is the network side");
        assertEquals(31.0, network.getBoundaryLine("BL").getTerminal().getQ(), 1e-12);
    }

    @Test
    void theWriterRunsOnAThreadThatHasSelectedNoVariant() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();
        String variantId = network.getVariantManager().getWorkingVariantId();
        network.getVariantManager().allowVariantMultiThreadAccess(true);

        String busId = busViewBusId(network);
        StateEstimationResult result = new StateEstimationResultImpl(
                variantId, List.of(),
                List.of(new StateEstimationResultImpl.BusResultImpl(busId, "VLGEN", 24.5, 1.5)),
                List.of(), List.of(), List.of(), List.of());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> bareProvider().writeResultsToNetwork(network, variantId, result)).get();
        } finally {
            executor.shutdown();
        }

        network.getVariantManager().setWorkingVariant(variantId);
        assertEquals(24.5, network.getVoltageLevel("VLGEN").getBusView().getBus(busId).getV(), 1e-12);
    }

    private static String busViewBusId(Network network) {
        return network.getVoltageLevel("VLGEN").getBusView().getBuses().iterator().next().getId();
    }
}
