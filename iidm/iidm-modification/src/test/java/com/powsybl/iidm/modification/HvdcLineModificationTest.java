/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class HvdcLineModificationTest {

    private Network network;

    @BeforeEach
    public void setUp() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    void testHasImpact() {
        NetworkModification modification1 = new HvdcLineModification("WRONG_ID", true, 12.0,
            HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, 0.0, 0.0, false);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new HvdcLineModification("L", true, 280.0,
            HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, 0.0, 0.0, false);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new HvdcLineModification("L", true, 280.0,
            null, null, null, null);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        NetworkModification modification4 = new HvdcLineModification("L", true, 0.0,
            HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, null, null, true);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        NetworkModification modification5 = new HvdcLineModification("L", true, 10.0,
            HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, null, null, true);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));

        NetworkModification modification6 = new HvdcLineModification("L", true, 0.0,
            HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, null, null, true);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification6.hasImpactOnNetwork(network));
    }
}
