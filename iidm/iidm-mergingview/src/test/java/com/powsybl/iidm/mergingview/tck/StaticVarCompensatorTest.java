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
import com.powsybl.iidm.network.tck.AbstractStaticVarCompensatorTest;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class StaticVarCompensatorTest extends AbstractStaticVarCompensatorTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("merge", "test");
        network.merge(SvcTestCaseFactory.create());
        return network;
    }

    @Test
    public void removeTest() {
        try {
            super.removeTest();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void regulatingTerminalTest() {
        try {
            super.regulatingTerminalTest();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
