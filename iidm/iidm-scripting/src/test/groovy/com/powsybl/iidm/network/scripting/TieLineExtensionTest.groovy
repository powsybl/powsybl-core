/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.Country
import com.powsybl.iidm.network.Network
import com.powsybl.iidm.network.TieLine
import com.powsybl.iidm.network.TieLineAdder
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class TieLineExtensionTest {

    @Test
    void newHalfLine1Test() {
        def tl = createTieLine()
        assertNotNull(tl.half1)
        assertNotNull(tl.half2)
    }

    static TieLine createTieLine() {
        def network = NoEquipmentNetworkFactory.create()
        network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .setVoltageLevel1("vl1")
                .setBus1("busA")
                .setConnectableBus1("busA")
                .setVoltageLevel2("vl2")
                .setBus2("busB")
                .setConnectableBus2("busB")
                .setUcteXnodeCode("ucte")
                .newHalfLine1()
                    .setId("hl1")
                    .setName("half1_name")
                    .setR(10.0)
                    .setX(20.0)
                    .setB1(40.0)
                    .setB2(45.0)
                    .setG1(30.0)
                    .setG2(35.0)
                    .setXnodeQ(60.0)
                    .setXnodeP(50.0)
                .add()
                .newHalfLine2()
                    .setId("hl2")
                    .setR(1.0)
                    .setX(2.0)
                    .setB1(140)
                    .setB2(145.0)
                    .setG1(130.0)
                    .setG2(135.0)
                    .setXnodeQ(60.0)
                    .setXnodeP(50.0)
                .add()
                .add();
    }
}
