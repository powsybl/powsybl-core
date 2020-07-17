/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class CandidateComputationsTest {

    private InMemoryPlatformConfig platformConfig;
    private FileSystem fileSystem;

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
        public void run(Network network, ComputationManager computationManager) {
            network.getGenerator("GEN").getTerminal().setP(126f);
        }
    }

    @Test
    public void runDummyComputation() {
        Network network = EurostagTutorialExample1Factory.create();

        CandidateComputation computation = CandidateComputations.getComputation("dummy").orElseGet(failure());
        assertNotNull(computation);
        assertEquals("dummy", computation.getName());

        computation.run(network, null);
        assertEquals(126f, network.getGenerator("GEN").getP(), 0f);
    }

    @Test
    public void listComputationsNames() {
        assertTrue(ImmutableSet.copyOf(CandidateComputations.getComputationsNames()).containsAll(ImmutableSet.of("dummy", "loadflow")));
    }

    @Test
    public void listComputations() {
        assertTrue(ImmutableSet.copyOf(CandidateComputations.getComputationsNames()).containsAll(ImmutableSet.of("dummy", "loadflow")));
        assertEquals(ImmutableSet.of("dummy", "loadflow"), ImmutableSet.copyOf(CandidateComputations.getComputationsNames()));
    }

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void runLoadFlowMock() {
        platformConfig.createModuleConfig("loadflow-validation").setStringProperty("load-flow-name", "LoadFlowMock");

        Network network = EurostagTutorialExample1Factory.create();

        CandidateComputation computation = CandidateComputations.getComputation("loadflow").orElseGet(failure());
        assertNotNull(computation);
        assertEquals("loadflow", computation.getName());

        computation.run(network, Mockito.mock(ComputationManager.class));
        assertEquals(92f, network.getGenerator("GEN").getP(), 0f);
    }
}
