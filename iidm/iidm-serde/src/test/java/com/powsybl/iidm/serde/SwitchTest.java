/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SwitchTest extends AbstractIidmSerDeTest {

    @Test
    void readAndDiscardSwitchWithSameNodeAtBothEnds() {
        //Network n = NetworkXml.read(getVersionedNetworkAsStream("switchWithSameNodeAtBothEnds.xml", CURRENT_IIDM_XML_VERSION));
        Network n = NetworkSerDe.read(getNetworkAsStream("/switches/switchWithSameNodeAtBothEnds.xiidm"));

        // Check that the "looped-switch" has been discarded
        assertEquals(2, n.getSwitchCount());
        assertNull(n.getSwitch("looped-switch"));
    }

    @Test
    void readAndDiscardSwitchWithSameBusAtBothEnds() {
        Network n = NetworkSerDe.read(getNetworkAsStream("/switches/switchWithSameBusAtBothEnds.xiidm"));

        // Check that the "looped-switch" has been discarded
        assertEquals(0, n.getSwitchCount());
        assertNull(n.getSwitch("looped-switch"));
    }
}
