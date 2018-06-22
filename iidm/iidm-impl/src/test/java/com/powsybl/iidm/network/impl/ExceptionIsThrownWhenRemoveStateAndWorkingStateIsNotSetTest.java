/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExceptionIsThrownWhenRemoveStateAndWorkingStateIsNotSetTest {

    @Test
    public void test() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();
        network.getStateManager().allowStateMultiThreadAccess(true);
        network.getStateManager().cloneState(StateManagerConstants.INITIAL_STATE_ID, "s");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        boolean[] exceptionThrown = new boolean[1];
        exceptionThrown[0] = false;
        executorService.execute(() -> {
            try {
                network.getStateManager().removeState("s");
            } catch (Throwable e) {
                exceptionThrown[0] = true;
            }
        });
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        Assert.assertFalse(exceptionThrown[0]);
    }

}
