/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.mergingview.TestUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractConnectedComponentWithTieLineBugTest;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ConnectedComponentWithTieLineBugTest extends AbstractConnectedComponentWithTieLineBugTest {

    @Override
    protected Network createNetwork() {
        return MergingView.create("n", "test");
    }

    @Test
    public void test() {
        TestUtil.notImplemented(super::test);
    }
}
