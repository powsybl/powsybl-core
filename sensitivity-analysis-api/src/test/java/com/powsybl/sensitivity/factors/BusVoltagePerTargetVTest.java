/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.sensitivity.factors.functions.BusVoltage;
import com.powsybl.sensitivity.factors.variables.TargetV;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusVoltagePerTargetVTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkFailsWhenNullFunction() {
        TargetV targetV = Mockito.mock(TargetV.class);
        exception.expect(NullPointerException.class);
        new BusVoltagePerTargetV(null, targetV);
    }

    @Test
    public void checkFailsWhenNullVariable() {
        BusVoltage busVoltage = Mockito.mock(BusVoltage.class);
        exception.expect(NullPointerException.class);
        new BusVoltagePerTargetV(busVoltage, null);
    }

    @Test
    public void testGetFunction() {
        BusVoltage busVoltage = Mockito.mock(BusVoltage.class);
        TargetV targetV = Mockito.mock(TargetV.class);
        BusVoltagePerTargetV factor = new BusVoltagePerTargetV(busVoltage, targetV);
        Assert.assertEquals(busVoltage, factor.getFunction());
    }

    @Test
    public void testGetVariable() {
        BusVoltage busVoltage = Mockito.mock(BusVoltage.class);
        TargetV targetV = Mockito.mock(TargetV.class);
        BusVoltagePerTargetV factor = new BusVoltagePerTargetV(busVoltage, targetV);
        Assert.assertEquals(targetV, factor.getVariable());
    }
}
