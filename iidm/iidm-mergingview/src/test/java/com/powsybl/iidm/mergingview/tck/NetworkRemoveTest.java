/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.mergingview.TestUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractNetworkRemoveTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class NetworkRemoveTest extends AbstractNetworkRemoveTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("test", "test");
        network.merge(EurostagTutorialExample1Factory.create());
        return network;
    }

    @Test
    public void removeLineTest() {
        TestUtil.notImplemented(super::removeLineTest);
    }

    @Test
    public void removeAll() {
        TestUtil.notImplemented(super::removeAll);
    }

    @Test
    public void removeBus() {
        TestUtil.notImplemented(super::removeBus);
    }

    @Test
    public void removeBusFailureBecauseOfSwitch() {
        TestUtil.notImplemented(super::removeBusFailureBecauseOfSwitch);
    }

    @Test
    public void removeSwitch() {
        try {
            super.removeSwitch();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
