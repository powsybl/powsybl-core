/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.test.EurostagTutorialExample1Factory.VLLOAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public abstract class AbstractBusTerminalTest {

    public static final String NLOAD_2 = "NLOAD2";
    public static final String NLOAD_3 = "NLOAD3";

    @Test
    public void testSetInvalidConnectableBus() {
        Network network = EurostagTutorialExample1Factory.create();
        Terminal.BusBreakerView busBreakerView = network.getLoad("LOAD").getTerminal().getBusBreakerView();
        assertThrows(PowsyblException.class, () -> busBreakerView.setConnectableBus("UNKNOWN"));
    }

    @Test
    public void testConnectableBusVariantManagement() {
        Network network = EurostagTutorialExample1Factory.create();
        String initialVariantId = network.getVariantManager().getWorkingVariantId();
        VoltageLevel vlLoad = network.getVoltageLevel(VLLOAD);
        vlLoad.getBusBreakerView().newBus().setId(NLOAD_2).add();
        vlLoad.getBusBreakerView().newBus().setId(NLOAD_3).add();

        Load l = network.getLoad("LOAD");
        assertConnection(l, "NLOAD", true);

        // Change the connectable bus
        l.getTerminal().getBusBreakerView().setConnectableBus(NLOAD_2);
        assertConnection(l, NLOAD_2, true);

        // Create a new variant, "VARIANT 2", and use it
        network.getVariantManager().cloneVariant(initialVariantId, "VARIANT 2");
        network.getVariantManager().setWorkingVariant("VARIANT 2");
        assertConnection(l, NLOAD_2, true);

        // Change the connectable bus in "VARIANT 2"
        l.getTerminal().getBusBreakerView().setConnectableBus(NLOAD_3);
        assertConnection(l, NLOAD_3, true);

        // Disconnect the load in "VARIANT 2"
        l.disconnect();
        assertConnection(l, NLOAD_3, false);
        assertNotNull(l.getTerminal().getVoltageLevel());

        // Use initial variant
        network.getVariantManager().setWorkingVariant(initialVariantId);
        assertConnection(l, NLOAD_2, true);
    }

    private void assertConnection(Load load, String expectedConnectableBusId, boolean expectedConnected) {
        assertEquals(expectedConnectableBusId, load.getTerminal().getBusBreakerView().getConnectableBus().getId());
        assertEquals(expectedConnected, load.getTerminal().isConnected());
        if (expectedConnected) {
            assertEquals(expectedConnectableBusId, load.getTerminal().getBusBreakerView().getBus().getId());
        } else {
            assertNull(load.getTerminal().getBusBreakerView().getBus());
        }
    }
}
