/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl.util;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiStateNetworkAwareExecutorsTest {

    @Test
    public void testAfterExecute() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();
        StateManager manager = network.getStateManager();
        manager.allowStateMultiThreadAccess(true);
        assertTrue(manager.getWorkingStateId().equals(StateManager.INITIAL_STATE_ID));
        ExecutorService service = MultiStateNetworkAwareExecutors.newFixedThreadPool(1);
        Thread[] threads = new Thread[1];
        service.submit(() -> {
            manager.setWorkingState(StateManager.INITIAL_STATE_ID);
            threads[0] = Thread.currentThread();
        }).get();
        service.submit(() -> {
            assertTrue(threads[0] == Thread.currentThread()); // just one thread in the pool
            try {
                // current working state should not be set even if it is the same thread than previously
                network.getGenerator("GEN").getTargetP();
                fail();
            } catch (Exception e) {
            }
        }).get();
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }

}