/**
 * Copyright (c) 2026, SuperGrid Institute ((https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Baptiste Perreyon {@literal <baptiste.perreyon at supergrid-institute.com>}
 */
public abstract class AbstractDcConnectableTest {

    @Test
    public void connectAndDisconnect() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();
        DcLine dcLine = network.getDcLine("dcLinePos");

        // Check dcLine is fully connected
        assertDcLineStatus(true, dcLine);

        // Disconnect dcLine, which should return true
        assertTrue(dcLine.disconnectDc());
        assertDcLineStatus(false, dcLine);

        // Disconnect dcLine again, which should return false
        assertFalse(dcLine.disconnectDc());
        assertDcLineStatus(false, dcLine);

        // Connect dcLine, which should return true
        assertTrue(dcLine.connectDc());
        assertDcLineStatus(true, dcLine);

        // Connect dcLine again, which should return false
        assertFalse(dcLine.connectDc());
        assertDcLineStatus(true, dcLine);

        // Partially disconnect DC side, connectDc should return true
        dcLine.getDcTerminal1().setConnected(false);
        assertTrue(dcLine.connectDc());
        assertDcLineStatus(true, dcLine);

        // Partially disconnect DC side, disconnectDc should return true
        dcLine.getDcTerminal1().setConnected(false);
        assertTrue(dcLine.disconnectDc());
        assertDcLineStatus(false, dcLine);
    }

    private void assertDcLineStatus(boolean expectedStatus, DcLine dcLine) {
        assertEquals(expectedStatus, dcLine.getDcTerminal1().isConnected());
        assertEquals(expectedStatus, dcLine.getDcTerminal2().isConnected());
    }

    @Test
    public void connectAndDisconnectAcDcConverter() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("VscFr");

        // Check vsc is fully connected
        assertConverterStatus(true, vsc);

        // Disconnect vsc, which should return true
        assertTrue(vsc.disconnectDc());
        assertConverterStatus(false, vsc);

        // Disconnect vsc again, which should return False
        assertFalse(vsc.disconnectDc());
        assertConverterStatus(false, vsc);

        // Connect vsc, which should return true
        assertTrue(vsc.connectDc());
        assertConverterStatus(true, vsc);

        // Connect vsc again, which should return False
        assertFalse(vsc.connectDc());
        assertConverterStatus(true, vsc);

        // Partially disconnect DC side, connectDc should return true
        vsc.getDcTerminal1().setConnected(false);
        assertTrue(vsc.connectDc());
        assertConverterStatus(true, vsc);

        // Partially disconnect DC side, disconnectDc should return true
        vsc.getDcTerminal1().setConnected(false);
        assertTrue(vsc.disconnectDc());
        assertConverterStatus(false, vsc);
    }

    private void assertConverterStatus(boolean expectedDcStatus, VoltageSourceConverter vsc) {
        assertTrue(vsc.getTerminal1().isConnected()); // AC side is not modified
        assertEquals(expectedDcStatus, vsc.getDcTerminal1().isConnected());
        assertEquals(expectedDcStatus, vsc.getDcTerminal2().isConnected());
    }
}
