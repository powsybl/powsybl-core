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
import com.powsybl.iidm.network.tck.AbstractTerminalFinderTest;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class TerminalFinderTest extends AbstractTerminalFinderTest {

    @Override
    protected Network createNetwork(Supplier<Network> supplier) {
        Network network = MergingView.create("merge", "test");
        network.merge(supplier.get());
        return network;
    }

    @Test
    public void testLineTerminal1() {
        try {
            super.testLineTerminal1();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testLineTerminal2() {
        try {
            super.testLineTerminal2();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testBbsTerminal() {
        try {
            super.testBbsTerminal();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testNoTerminal() {
        try {
            super.testNoTerminal();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
