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
                    PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("CI").getPhaseTapChanger();
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
            phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
            assertEquals(22, phaseTapChanger.getTapPosition());
            /**
             tap:0    P:0,000000    I:24,630026
             tap:1    P:15,000000    I:44,402412
             tap:2    P:30,000000    I:77,886978
             tap:3    P:45,000000    I:113,538811
             tap:4    P:60,000000    I:149,818604
             tap:5    P:75,000000    I:186,359970
             tap:6    P:90,000000    I:223,034378
             tap:7    P:105,000000    I:259,785492
             tap:8    P:120,000000    I:296,584778
             tap:9    P:135,000000    I:333,416321
             tap:10    P:150,000000    I:370,270477
             tap:11    P:165,000000    I:407,141113
             tap:12    P:180,000000    I:444,024109
             tap:13    P:195,000000    I:480,916626
             tap:14    P:210,000000    I:517,816650
             tap:15    P:225,000000    I:554,722656
             tap:16    P:240,000000    I:591,633545
             tap:17    P:255,000000    I:628,548401
             tap:18    P:270,000000    I:665,466675
             tap:19    P:285,000000    I:702,387695
             tap:20    P:300,000000    I:739,311157
             tap:21    P:315,000000    I:776,236694
             tap:22    P:330,000000    I:813,163940
             tap:23    P:345,000000    I:850,092773
             tap:24    P:360,000000    I:887,022949
             tap:25    P:375,000000    I:923,954346
             -------------------------------------- Limit:931.0f
             tap:26    P:390,000000    I:960,886719
             tap:27    P:405,000000    I:997,820068
             tap:28    P:420,000000    I:1034,754272
             tap:29    P:435,000000    I:1071,689209
             tap:30    P:450,000000    I:1108,624756
             tap:31    P:465,000000    I:1145,561035
             tap:32    P:480,000000    I:1182,497803
             */
            optimizer.findMaximalFlowTap(network, phaseShifterId);
            assertEquals(25, phaseTapChanger.getTapPosition());
            assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
            // overloaded already
            phaseTapChanger.setTapPosition(27);
            try {
                optimizer.findMaximalFlowTap(network, phaseShifterId);
                fail();
            } catch (Exception ignored) {
            }
            twoWindingsTransformerCI.getCurrentLimits1().setPermanentLimit(123456.0f);
            optimizer.findMaximalFlowTap(network, phaseShifterId);
            assertEquals(phaseTapChanger.getHighTapPosition(), phaseTapChanger.getTapPosition());


            // reverse order (tap bigger current smaller)
            /**
             tap:0    P:510,000000    I:1256,372803
             tap:1    P:495,000000    I:1219,435059
             tap:2    P:480,000000    I:1182,497803
             tap:3    P:465,000000    I:1145,561035
             tap:4    P:450,000000    I:1108,624756
             tap:5    P:435,000000    I:1071,689209
             tap:6    P:420,000000    I:1034,754272
             tap:7    P:405,000000    I:997,820068
             tap:8    P:390,000000    I:960,886719
             -------------------------------------- Limit:931.0f
             tap:9    P:375,000000    I:923,954346
             tap:10    P:360,000000    I:887,022949
             tap:11    P:345,000000    I:850,092773
             tap:12    P:330,000000    I:813,163940
             tap:13    P:315,000000    I:776,236694
             tap:14    P:300,000000    I:739,311157
             tap:15    P:285,000000    I:702,387695
             tap:16    P:270,000000    I:665,466675
             tap:17    P:255,000000    I:628,548401
             tap:18    P:240,000000    I:591,633545
             tap:19    P:225,000000    I:554,722656
             tap:20    P:210,000000    I:517,816650
             tap:21    P:195,000000    I:480,916626
             tap:22    P:180,000000    I:444,024109
             tap:23    P:165,000000    I:407,141113
             tap:24    P:150,000000    I:370,270477
             tap:25    P:135,000000    I:333,416321
             tap:26    P:120,000000    I:296,584778
             tap:27    P:105,000000    I:259,785492
             tap:28    P:90,000000    I:223,034378
             tap:29    P:75,000000    I:186,359970
             tap:30    P:60,000000    I:149,818604
             tap:31    P:45,000000    I:113,538811
             tap:32    P:30,000000    I:77,886978
             */
            network.setCaseDate(dateTime);
            twoWindingsTransformerCI.getCurrentLimits1().setPermanentLimit(123456.0f);
            optimizer.findMaximalFlowTap(network, phaseShifterId);
            assertEquals(phaseTapChanger.getLowTapPosition(), phaseTapChanger.getTapPosition());
            twoWindingsTransformerCI.getCurrentLimits1().setPermanentLimit(931.0f);
            // overloaded already
            try {
                optimizer.findMaximalFlowTap(network, phaseShifterId);
                fail();
            } catch (Exception ignored) {
            }
            phaseTapChanger.setTapPosition(30);
            optimizer.findMaximalFlowTap(network, phaseShifterId);
            assertEquals(9, phaseTapChanger.getTapPosition());
        }
    }

}
