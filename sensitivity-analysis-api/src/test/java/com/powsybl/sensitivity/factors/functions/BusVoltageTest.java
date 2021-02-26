/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.functions;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusVoltageTest {

    private static final String FUNCTION_ID = "Function ID";
    private static final String FUNCTION_NAME = "Function name";
    private static final String TERMINAL_ID = "Terminal ID";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullTerminalId() {
        exception.expect(NullPointerException.class);
        new BusVoltage(FUNCTION_ID, FUNCTION_NAME, null);
    }

    @Test
    public void getName() {
        BusVoltage busVoltage = new BusVoltage(FUNCTION_ID, FUNCTION_NAME, TERMINAL_ID);
        Assert.assertEquals(FUNCTION_NAME, busVoltage.getName());
    }

    @Test
    public void getId() {
        BusVoltage busVoltage = new BusVoltage(FUNCTION_ID, FUNCTION_NAME, TERMINAL_ID);
        Assert.assertEquals(FUNCTION_ID, busVoltage.getId());
    }

    @Test
    public void getBranchId() {
        BusVoltage busVoltage = new BusVoltage(FUNCTION_ID, FUNCTION_NAME, TERMINAL_ID);
        assertEquals(TERMINAL_ID, busVoltage.getTerminalId());
    }

}
