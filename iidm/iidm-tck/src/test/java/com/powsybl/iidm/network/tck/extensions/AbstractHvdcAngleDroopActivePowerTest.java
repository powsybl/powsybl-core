/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public abstract class AbstractHvdcAngleDroopActivePowerTest {

    private Network network;

    @Before
    public void initNetwork() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    public void test() {
        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcAngleDroopActivePowerControl hadpc = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        assertNull(hadpc);

        hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(200.0f)
                .withDroop(0.9f)
                .withEnabled(true)
                .add();
        hadpc = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        assertNotNull(hadpc);
        assertEquals(200.0f, hadpc.getP0(), 0f);
        assertEquals(0.9f, hadpc.getDroop(), 0f);
        assertTrue(hadpc.isEnabled());
        assertEquals("hvdcAngleDroopActivePowerControl", hadpc.getName());

        hadpc.setP0(300.0f);
        hadpc.setDroop(0.0f);
        hadpc.setEnabled(false);
        assertEquals(300.0f, hadpc.getP0(), 0f);
        assertEquals(0.0f, hadpc.getDroop(), 0f);
        assertFalse(hadpc.isEnabled());
    }
}
