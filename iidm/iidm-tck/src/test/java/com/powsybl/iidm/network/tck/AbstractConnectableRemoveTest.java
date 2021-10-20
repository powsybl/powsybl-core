/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractConnectableRemoveTest {

    private Network network;

    private final List<String> removedObjects = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        network = FourSubstationsNodeBreakerFactory.create();
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void afterRemoval(String id) {
                removedObjects.add(id);
            }
        });
    }

    @After
    public void tearDown() {
        removedObjects.clear();
    }

    @Test
    public void testRemove() {
        Load ld1 = network.getLoad("LD1");
        ld1.remove();
        assertEquals(List.of("LD1"), removedObjects);
    }

    @Test
    public void testRemoveAndClean() {
        Load ld1 = network.getLoad("LD1");
        ld1.remove(true);
        assertEquals(List.of("S1VL1_LD1_BREAKER", "LD1"), removedObjects);
    }
}
