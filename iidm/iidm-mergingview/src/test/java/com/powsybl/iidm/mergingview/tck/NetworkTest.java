/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractNetworkTest;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class NetworkTest extends AbstractNetworkTest {

    @Override
    protected Network createNetwork(String id, String format) {
        return MergingView.create(id, format);
    }

    @Override
    protected Network createNetwork(Supplier<Network> supplier) {
        Network network = MergingView.create("test", "test");
        network.merge(supplier.get());
        return network;
    }

    @Test
    public void testNetwork1() {
        try {
            super.testNetwork1();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testNetworkWithBattery() {
        try {
            super.testNetworkWithBattery();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
