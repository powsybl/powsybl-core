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
import com.powsybl.iidm.network.tck.AbstractVoltageLevelExportTest;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class VoltageLevelExportTest extends AbstractVoltageLevelExportTest {

    @Override
    protected Network createNetwork(Supplier<Network> supplier) {
        Network network = MergingView.create("merge", "test");
        network.merge(supplier.get());
        return network;
    }

    @Test
    public void busBreakerTest() {
        TestUtil.notImplemented(() -> {
            try {
                super.busBreakerTest();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Test
    public void nodeBreakerTest() {
        TestUtil.notImplemented(() -> {
            try {
                super.nodeBreakerTest();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
