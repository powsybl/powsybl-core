/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SwitchTest extends AbstractXmlConverterTest {

    @Test
    public void readAndDiscardSwitchWithSameNodeAtBothEnds() {
        //Network n = NetworkXml.read(getVersionedNetworkAsStream("switchWithSameNodeAtBothEnds.xml", CURRENT_IIDM_XML_VERSION));
        Network n = NetworkXml.read(getNetworkAsStream("/switches/switchWithSameNodeAtBothEnds.xiidm"));

        // Check that the "looped-switch" has been discarded
        assertEquals(2, n.getSwitchCount());
        assertNull(n.getSwitch("looped-switch"));
    }

    @Test
    public void readAndDiscardSwitchWithSameBusAtBothEnds() {
        Network n = NetworkXml.read(getNetworkAsStream("/switches/switchWithSameBusAtBothEnds.xiidm"));

        // Check that the "looped-switch" has been discarded
        assertEquals(0, n.getSwitchCount());
        assertNull(n.getSwitch("looped-switch"));
    }
}
