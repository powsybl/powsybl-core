/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class AbstractSwitchBusBreakerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;

    @Before
    public void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
    }

    @Test
    public void addSwitchWithSameBusAtBothEnds() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Switch 'Sw1': same bus at both ends");
        Bus bus = voltageLevel.getBusBreakerView().getBus("NGEN");
        voltageLevel.getBusBreakerView().newSwitch()
            .setId("Sw1")
            .setBus1(bus.getId())
            .setBus2(bus.getId())
            .add();
    }
}
