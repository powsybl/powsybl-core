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
import com.powsybl.iidm.network.tck.AbstractHvdcLineTest;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class HvdcLineTest extends AbstractHvdcLineTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("test", "test");
        network.merge(HvdcTestNetwork.createLcc());
        return network;
    }

    @Test
    public void removeHvdcLine() {
        try {
            super.removeHvdcLine();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
