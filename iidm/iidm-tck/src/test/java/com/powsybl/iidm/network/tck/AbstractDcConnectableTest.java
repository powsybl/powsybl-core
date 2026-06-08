/**
 * Copyright (c) 2026, SuperGrid Institute (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Baptiste Perreyon {@literal <baptiste.perreyon at supergrid-institute.com>}
 */
public abstract class AbstractDcConnectableTest {

    @Test
    public void connectAndDisconnect() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();
        DcLine dcline = network.getDcLine("dcLinePos");

        // Check dcLine is fully connected
        assertTrue(dcline.getDcTerminal1().isConnected());
        assertTrue(dcline.getDcTerminal2().isConnected());

        // Disconnect dcLine, which should return true
        assertTrue(dcline.disconnectDc());
        assertFalse(dcline.getDcTerminal1().isConnected());
        assertFalse(dcline.getDcTerminal2().isConnected());

        // Disconnect dcLine again, which should return false
        assertFalse(dcline.disconnectDc());
        assertFalse(dcline.getDcTerminal1().isConnected());
        assertFalse(dcline.getDcTerminal2().isConnected());

        // Connect dcLine, which should return true
        assertTrue(dcline.connectDc());
        assertTrue(dcline.getDcTerminal1().isConnected());
        assertTrue(dcline.getDcTerminal2().isConnected());

        // Connect dcLine again, which should return false
        assertFalse(dcline.connectDc());
        assertTrue(dcline.getDcTerminal1().isConnected());
        assertTrue(dcline.getDcTerminal2().isConnected());

        // Partially disconnect DC side, connectDc should return true
        dcline.getDcTerminal1().setConnected(false);
        assertTrue(dcline.connectDc());
        assertTrue(dcline.getDcTerminal1().isConnected());
        assertTrue(dcline.getDcTerminal2().isConnected());

        // Partially disconnect DC side, disconnectDc should return true
        dcline.getDcTerminal1().setConnected(false);
        assertTrue(dcline.disconnectDc());
        assertFalse(dcline.getDcTerminal1().isConnected());
        assertFalse(dcline.getDcTerminal2().isConnected());
    }

    @Test
    public void connectAndDisconnectAcDcConverter() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("VscFr");

        // Check vsc is fully connected
        assertTrue(vsc.getTerminal1().isConnected());
        assertTrue(vsc.getDcTerminal1().isConnected());
        assertTrue(vsc.getDcTerminal2().isConnected());

        // Disconnect vsc, which should return true
        assertTrue(vsc.disconnectDc());
        assertTrue(vsc.getTerminal1().isConnected());  // AC side is not modified
        assertFalse(vsc.getDcTerminal1().isConnected());
        assertFalse(vsc.getDcTerminal2().isConnected());

        // Disconnect vsc again, which should return False
        assertFalse(vsc.disconnectDc());
        assertTrue(vsc.getTerminal1().isConnected());
        assertFalse(vsc.getDcTerminal1().isConnected());
        assertFalse(vsc.getDcTerminal2().isConnected());

        // Connect vsc, which should return true
        assertTrue(vsc.connectDc());
        assertTrue(vsc.getTerminal1().isConnected());
        assertTrue(vsc.getDcTerminal1().isConnected());
        assertTrue(vsc.getDcTerminal2().isConnected());

        // Connect vsc again, which should return False
        assertFalse(vsc.connectDc());
        assertTrue(vsc.getTerminal1().isConnected());
        assertTrue(vsc.getDcTerminal1().isConnected());
        assertTrue(vsc.getDcTerminal2().isConnected());

        // Partially disconnect DC side, connectDc should return true
        vsc.getDcTerminal1().setConnected(false);
        assertTrue(vsc.connectDc());
        assertTrue(vsc.getDcTerminal1().isConnected());
        assertTrue(vsc.getDcTerminal2().isConnected());

        // Partially disconnect DC side, disconnectDc should return true
        vsc.getDcTerminal1().setConnected(false);
        assertTrue(vsc.disconnectDc());
        assertFalse(vsc.getDcTerminal1().isConnected());
        assertFalse(vsc.getDcTerminal2().isConnected());
    }
}
