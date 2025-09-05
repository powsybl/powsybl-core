/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ConverterTrippingTest extends AbstractTrippingTest {

    @Test
    void converterTrippingTest() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        assertTrue(network.getVoltageSourceConverter("VscFr").getTerminal1().isConnected());
        assertTrue(network.getVoltageSourceConverter("VscFr").getDcTerminal1().isConnected());

        ConverterTripping tripping = new ConverterTripping("VscFr");
        tripping.apply(network);

        assertFalse(network.getVoltageSourceConverter("VscFr").getTerminal1().isConnected());
        assertFalse(network.getVoltageSourceConverter("VscFr").getDcTerminal1().isConnected());
    }

    @Test
    void unknownConverterTrippingTest() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();

        ConverterTripping tripping = new ConverterTripping("converter");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new ConverterTripping("ID");
        assertEquals("ConverterTripping", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();

        NetworkModification modification1 = new ConverterTripping("WRONG_ID");
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new ConverterTripping("VscFr");
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));
        modification2.apply(network);

        NetworkModification modification3 = new ConverterTripping("VscFr");
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));
    }
}
