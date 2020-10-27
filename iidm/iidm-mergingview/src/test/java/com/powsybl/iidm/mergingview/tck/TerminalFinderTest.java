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
import com.powsybl.iidm.network.tck.AbstractTerminalFinderTest;
import org.junit.Test;

import java.util.function.Supplier;

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
        TestUtil.notImplemented(super::testLineTerminal1);
    }

    @Test
    public void testLineTerminal2() {
        TestUtil.notImplemented(super::testLineTerminal2);
    }

    @Test
    public void testBbsTerminal() {
        TestUtil.notImplemented(super::testBbsTerminal);
    }

    @Test
    public void testNoTerminal() {
        TestUtil.notImplemented(super::testNoTerminal);
    }
}
