/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ArrayIndexOutOfBoundsException fix test
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EmptyCalculatedBusBugTest {

    @Test
    public void test() {
        Network network = NetworkFactory.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().setNodeCount(2);
        vl.getNodeBreakerView().newBreaker()
                .setId("SW1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(false)
                .add();
        assertEquals(2, vl.getBusBreakerView().getBusStream().count());
    }
}
