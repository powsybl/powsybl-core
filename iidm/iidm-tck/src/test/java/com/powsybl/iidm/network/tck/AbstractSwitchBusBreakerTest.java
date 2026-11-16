/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSwitchBusBreakerTest {

    private Network network;
    private VoltageLevel voltageLevel;

    @BeforeEach
    public void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
    }

    @Test
    public void addSwitchWithSameBusAtBothEnds() {
        Bus bus = voltageLevel.getBusBreakerView().getBus("NGEN");
        ValidationException e = assertThrows(ValidationException.class, () -> voltageLevel.getBusBreakerView().newSwitch()
            .setId("Sw1")
            .setBus1(bus.getId())
            .setBus2(bus.getId())
            .add());
        assertTrue(e.getMessage().contains("Switch 'Sw1': same bus at both ends"));
    }
}
