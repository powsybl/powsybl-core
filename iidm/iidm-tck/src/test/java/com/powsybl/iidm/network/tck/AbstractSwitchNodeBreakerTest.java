/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public abstract class AbstractSwitchNodeBreakerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;

    @Before
    public void initNetwork() {
        network = FictitiousSwitchFactory.create();
        voltageLevel = network.getVoltageLevel("C");
    }

    @Test
    public void addSwitchWithSameNodeAtBothEnds() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Switch 'Sw1': same node at both ends");
        int newNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        voltageLevel.getNodeBreakerView().newSwitch()
            .setId("Sw1")
            .setNode1(newNode)
            .setNode2(newNode)
            .setKind(SwitchKind.BREAKER)
            .add();
    }

    @Test
    public void addSwitchWithNullKind() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Switch 'Sw1': kind is not set");
        int newNode1 = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int newNode2 = newNode1 + 1;
        voltageLevel.getNodeBreakerView().newSwitch()
            .setId("Sw1")
            .setNode1(newNode1)
            .setNode2(newNode2)
            .add();
    }
}
