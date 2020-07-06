/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class LoadDetailTest {

    static Network createTestNetwork() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B")
                .add();
        vl.newLoad()
                .setId("L")
                .setBus("B")
                .setConnectableBus("B")
                .setP0(100)
                .setQ0(50)
                .add();

        // extends load
        Load load = network.getLoad("L");
        assertNotNull(load);
        load.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(40f)
                .withFixedReactivePower(20f)
                .withVariableActivePower(60f)
                .withVariableReactivePower(30f)
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createTestNetwork();

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);
        assertEquals(40f, detail.getFixedActivePower(), 0f);
        assertEquals(20f, detail.getFixedReactivePower(), 0f);
        assertEquals(60f, detail.getVariableActivePower(), 0f);
        assertEquals(30f, detail.getVariableReactivePower(), 0f);

        detail.setFixedActivePower(60f)
                .setFixedReactivePower(30f)
                .setVariableActivePower(40f)
                .setVariableReactivePower(20f);
        assertEquals(60f, detail.getFixedActivePower(), 0f);
        assertEquals(30f, detail.getFixedReactivePower(), 0f);
        assertEquals(40f, detail.getVariableActivePower(), 0f);
        assertEquals(20f, detail.getVariableReactivePower(), 0f);
    }
}
