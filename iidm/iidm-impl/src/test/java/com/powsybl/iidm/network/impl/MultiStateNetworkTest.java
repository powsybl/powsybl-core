/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiStateNetworkTest {

    @Test
    public void singleThreadTest() {
        Network network = EurostagTutorialExample1Factory.create();
        final StateManager manager = network.getStateManager();
        manager.cloneState(StateManager.INITIAL_STATE_ID, "SecondState");
        manager.setWorkingState("SecondState");
        final Generator generator = network.getGenerator("GEN");
        generator.setVoltageRegulatorOn(false);
        assertTrue(!generator.isVoltageRegulatorOn());
        manager.setWorkingState(StateManager.INITIAL_STATE_ID);
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        final Network network = EurostagTutorialExample1Factory.create();
        final StateManager manager = network.getStateManager();
        manager.allowStateMultiThreadAccess(true);

        manager.cloneState(StateManager.INITIAL_STATE_ID, "SecondState");

        final Generator generator = network.getGenerator("GEN");

        manager.setWorkingState(StateManager.INITIAL_STATE_ID);
        generator.setVoltageRegulatorOn(true);

        manager.setWorkingState("SecondState");
        generator.setVoltageRegulatorOn(false);

        final boolean[] voltageRegulatorOnInitialState = new boolean[1];
        final boolean[] voltageRegulatorOnSecondState = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(2); // to sync threads after having set the working state
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.invokeAll(Arrays.asList(
            () -> {
                manager.setWorkingState(StateManager.INITIAL_STATE_ID);
                latch.countDown();
                latch.await();
                voltageRegulatorOnInitialState[0] = generator.isVoltageRegulatorOn();
                return null;
            },
            () -> {
                manager.setWorkingState("SecondState");
                latch.countDown();
                latch.await();
                voltageRegulatorOnSecondState[0] = generator.isVoltageRegulatorOn();
                return null;
            })
        );
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        assertTrue(voltageRegulatorOnInitialState[0]);
        assertTrue(!voltageRegulatorOnSecondState[0]);
    }

    @Test
    public void multiStateTopologyTest() throws InterruptedException {
        Network network = EurostagTutorialExample1Factory.create();
        StateManager manager = network.getStateManager();
        manager.cloneState(StateManager.INITIAL_STATE_ID, "NEW_STATE");
        VoltageLevel vlload = network.getVoltageLevel("VLLOAD");
        Bus nload = vlload.getBusBreakerView().getBus("NLOAD");
        Load newLoad = vlload.newLoad()
                .setId("NEW_LOAD")
                .setP0(10)
                .setQ0(10)
                .setBus("NLOAD")
                .setConnectableBus("NLOAD")
            .add();
        manager.setWorkingState("NEW_STATE");
        assertTrue(newLoad.getTerminal().getBusBreakerView().getBus() != null);
        assertTrue(Iterables.size(nload.getLoads()) == 2);
        newLoad.getTerminal().disconnect();
        assertTrue(newLoad.getTerminal().getBusBreakerView().getBus() == null);
        assertTrue(Iterables.size(vlload.getLoads()) == 2);
        assertTrue(Iterables.size(nload.getLoads()) == 1);
        manager.setWorkingState(StateManager.INITIAL_STATE_ID);
        assertTrue(newLoad.getTerminal().getBusBreakerView().getBus() != null);
        assertTrue(Iterables.size(vlload.getLoads()) == 2);
        assertTrue(Iterables.size(nload.getLoads()) == 2);
    }

    @Test
    public void stateNotSetTest() throws InterruptedException {
        Network network = EurostagTutorialExample1Factory.create();
        StateManager manager = network.getStateManager();
        manager.allowStateMultiThreadAccess(true);
        assertTrue(manager.getWorkingStateId().equals(StateManager.INITIAL_STATE_ID));
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                network.getGenerator("GEN").getTargetP();
                fail();
            } catch (Exception ignored) {
            }
        });
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void stateSetTest() throws InterruptedException {
        Network network = EurostagTutorialExample1Factory.create();
        StateManager manager = network.getStateManager();
        manager.allowStateMultiThreadAccess(true);
        assertTrue(manager.getWorkingStateId().equals(StateManager.INITIAL_STATE_ID));
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                manager.setWorkingState(StateManager.INITIAL_STATE_ID);
                network.getGenerator("GEN").getTargetP();
            } catch (Exception e) {
                fail();
            }
        });
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }

}
