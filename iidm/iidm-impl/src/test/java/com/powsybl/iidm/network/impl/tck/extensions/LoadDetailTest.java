/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl.tck.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractLoadDetailTest;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class LoadDetailTest extends AbstractLoadDetailTest {

    private static Network createNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
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
        return network;
    }

    @Test
    void checkVoltageConfigTest() {
        Network network = createNetwork();
        Load load = network.getLoad("L");
        assertNotNull(load);

        // Builder
        LoadDetailAdder loadDetailAdder = load.newExtension(LoadDetailAdder.class)
            .withFixedActivePower(40f)
            .withFixedReactivePower(20f)
            .withVariableActivePower(60f)
            .withVariableReactivePower(Double.NaN);

        // VariableReactivePower invalid
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, loadDetailAdder::add);
        assertEquals("Invalid variableReactivePower (NaN) for load L",
            e0.getMessage());
    }
}
