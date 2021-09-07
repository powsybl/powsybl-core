/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class HvdcSetpointIncreaseTest {

    private static final String VARIABLE_ID = "Variable ID";
    private static final String VARIABLE_NAME = "Variable name";
    private static final String HVDC_ID = "HVDC ID";

    @Test
    public void checkFailsWhenNullInjection() {
        assertThrows(NullPointerException.class, () ->
            new HvdcSetpointIncrease(VARIABLE_ID, VARIABLE_NAME, null));
    }

    @Test
    public void getName() {
        HvdcSetpointIncrease branchIntensity = new HvdcSetpointIncrease(VARIABLE_ID, VARIABLE_NAME, HVDC_ID);
        assertEquals(VARIABLE_NAME, branchIntensity.getName());
    }

    @Test
    public void getId() {
        HvdcSetpointIncrease branchIntensity = new HvdcSetpointIncrease(VARIABLE_ID, VARIABLE_NAME, HVDC_ID);
        assertEquals(VARIABLE_ID, branchIntensity.getId());
    }

    @Test
    public void getLine() {
        HvdcSetpointIncrease branchIntensity = new HvdcSetpointIncrease(VARIABLE_ID, VARIABLE_NAME, HVDC_ID);
        assertEquals(HVDC_ID, branchIntensity.getHvdcId());
    }
}
