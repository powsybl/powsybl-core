/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class LoadFlowBasedPhaseShifterOptimizerTest {

    private static DateTime dateTime = new DateTime(2009, 1, 9, 2, 54, 25);

    protected static class MyLoadFlowFactoryMock implements LoadFlowFactory {
        @Override
        public LoadFlow create(Network network, ComputationManager computationManager, int priority) {
            return new MyLoadFlowMock(network);
        }
    }

    protected static class MyLoadFlowMock implements LoadFlow {

        private Network network;

        MyLoadFlowMock(Network network) {
            this.network = network;
        }

        @Override
        public LoadFlowResult run(LoadFlowParameters parameters) throws Exception {
            return null;
        }

        @Override
        public LoadFlowResult run() throws Exception {

            LoadFlowResult result = new LoadFlowResult() {
                @Override
                public boolean isOk() {
                    int tapPos = network.getTwoWindingsTransformer("CI").getPhaseTapChanger().getTapPosition();

                    Terminal terminal = network.getTwoWindingsTransformer("CI").getTerminal1();
                    terminal.setQ(10.f);
                    Float activeP;
                    if (network.getCaseDate() != dateTime) {
                        activeP = tapPos * 15.0f;
                    } else {
                        activeP = (34 - tapPos) * 15.0f;
                    }
                    terminal.setP(activeP);
                    return true;
                }

                @Override
                public Map<String, String> getMetrics() {
                    return null;
                }

                @Override
                public String getLogs() {
                    return null;
                }
            };
            return result;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }
    }

    @Test
    public void testPhaseShifterOptimizer() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-based-phase-shifter-optimizer");
            moduleConfig.setClassProperty("load-flow-factory", LoadFlowFactoryMock.class);

            LoadFlowBasedPhaseShifterOptimizerConfig config = LoadFlowBasedPhaseShifterOptimizerConfig.load(platformConfig);
            config.setLoadFlowFactoryClass(MyLoadFlowFactoryMock.class);
            assertEquals(MyLoadFlowFactoryMock.class, config.getLoadFlowFactoryClass());

            Network network = FictitiousSwitchFactory.create();
            String phaseShifterId = "CI";
            TwoWindingsTransformer twoWindingsTransformerCI = network.getTwoWindingsTransformer(phaseShifterId);
            PhaseTapChanger phaseTapChanger = twoWindingsTransformerCI.getPhaseTapChanger();
            assertNotNull(phaseTapChanger);
            LoadFlowBasedPhaseShifterOptimizer optimizer = new LoadFlowBasedPhaseShifterOptimizer(new LocalComputationManager(), config);
            assertEquals(22, phaseTapChanger.getTapPosition());

            optimizer.findMaximalFlowTap(network, phaseShifterId);
            assertEquals(25, phaseTapChanger.getTapPosition());
            // overloaded already
            phaseTapChanger.setTapPosition(27);
            try {
                optimizer.findMaximalFlowTap(network, phaseShifterId);
                fail();
            } catch (Exception ignored) {
            }
            twoWindingsTransformerCI.getCurrentLimits1().setPermanentLimit(123456.0f);
            optimizer.findMaximalFlowTap(network, phaseShifterId);
            assertEquals(32, phaseTapChanger.getTapPosition());

        }
    }

}
