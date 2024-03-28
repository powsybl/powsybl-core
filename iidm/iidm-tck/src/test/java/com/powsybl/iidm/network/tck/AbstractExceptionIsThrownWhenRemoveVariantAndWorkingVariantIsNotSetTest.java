/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractExceptionIsThrownWhenRemoveVariantAndWorkingVariantIsNotSetTest {

    @Test
    public void test() throws InterruptedException {
        Network network = EurostagTutorialExample1Factory.create();
        network.getVariantManager().allowVariantMultiThreadAccess(true);
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "s");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        boolean[] exceptionThrown = new boolean[1];
        exceptionThrown[0] = false;
        executorService.execute(() -> {
            try {
                network.getVariantManager().removeVariant("s");
            } catch (PowsyblException e) {
                exceptionThrown[0] = true;
            }
        });
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertFalse(exceptionThrown[0]);
    }

}
