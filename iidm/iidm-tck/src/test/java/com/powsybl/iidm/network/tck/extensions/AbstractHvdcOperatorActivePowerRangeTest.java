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
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public abstract class AbstractHvdcOperatorActivePowerRangeTest {

    private Network network;

    @Before
    public void initNetwork() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    public void test() {
        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcOperatorActivePowerRange hopc = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        assertNull(hopc);

        hvdcLine.newExtension(HvdcOperatorActivePowerRangeAdder.class)
                .withOprFromCS2toCS1(1.0f)
                .withOprFromCS1toCS2(2.0f)
                .add();
        hopc = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
        assertNotNull(hopc);
        assertEquals(2.0f, hopc.getOprFromCS1toCS2(), 0f);
        assertEquals(1.0f, hopc.getOprFromCS2toCS1(), 0f);
        assertEquals("hvdcOperatorActivePowerRange", hopc.getName());

        hopc.setOprFromCS1toCS2(1.1f);
        hopc.setOprFromCS2toCS1(2.1f);
        assertEquals(1.1f, hopc.getOprFromCS1toCS2(), 0f);
        assertEquals(2.1f, hopc.getOprFromCS2toCS1(), 0f);
    }
}
