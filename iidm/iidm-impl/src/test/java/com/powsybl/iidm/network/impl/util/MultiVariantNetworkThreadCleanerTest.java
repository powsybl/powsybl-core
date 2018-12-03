/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.commons.concurrent.CleanableExecutors;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiVariantNetworkThreadCleanerTest {

    @Test
    public void testAfterExecute() throws Exception {
        Network network = EurostagTutorialExample1Factory.create();
        VariantManager manager = network.getVariantManager();
        manager.allowVariantMultiThreadAccess(true);
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, manager.getWorkingVariantId());
        ExecutorService service = CleanableExecutors.newFixedThreadPool("TEST_POOL", 1, Collections.singleton(new MultiVariantNetworkThreadCleaner()));
        Thread[] threads = new Thread[1];
        service.submit(() -> {
            manager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
            threads[0] = Thread.currentThread();
        }).get();
        service.submit(() -> {
            assertSame(Thread.currentThread(), threads[0]); // just one thread in the pool
            try {
                // current working state should not be set even if it is the same thread than previously
                network.getGenerator("GEN").getTargetP();
                fail();
            } catch (Exception ignored) {
            }
        }).get();
        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }

}
