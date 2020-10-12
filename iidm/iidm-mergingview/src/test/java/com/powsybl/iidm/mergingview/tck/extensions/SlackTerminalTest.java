/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck.extensions;

import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.mergingview.TestUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.extensions.AbstractSlackTerminalTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class SlackTerminalTest extends AbstractSlackTerminalTest {

    protected Network createNetwork() {
        return MergingView.create("test", "test");
    }

    protected Network createEurostag() {
        Network network = MergingView.create("merged", "test");
        network.merge(EurostagTutorialExample1Factory.create());
        return network;
    }

    @Test
    public void variantsCloneTest() {
        TestUtil.notImplemented(super::variantsCloneTest);
    }

    @Test
    public void test() {
        TestUtil.notImplemented(super::test);
    }

    @Test
    public void variantsResetTest() {
        TestUtil.notImplemented(super::variantsResetTest);
    }

    @Test
    public void vlErrorTest() {
        TestUtil.notImplemented(super::vlErrorTest);
    }
}
