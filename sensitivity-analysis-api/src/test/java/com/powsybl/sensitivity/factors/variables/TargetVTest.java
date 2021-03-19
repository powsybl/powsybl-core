/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class TargetVTest {

    private static final String VARIABLE_ID = "Variable ID";
    private static final String VARIABLE_NAME = "Variable name";
    private static final String EQUIPMENT_ID = "Regulating terminal ID";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkFailsWhenNullInjection() {
        exception.expect(NullPointerException.class);
        new TargetV(VARIABLE_ID, VARIABLE_NAME, null);
    }

    @Test
    public void getName() {
        TargetV targetV = new TargetV(VARIABLE_ID, VARIABLE_NAME, EQUIPMENT_ID);
        Assert.assertEquals(VARIABLE_NAME, targetV.getName());
    }

    @Test
    public void getId() {
        TargetV targetV = new TargetV(VARIABLE_ID, VARIABLE_NAME, EQUIPMENT_ID);
        Assert.assertEquals(VARIABLE_ID, targetV.getId());
    }

    @Test
    public void getLine() {
        TargetV targetV = new TargetV(VARIABLE_ID, VARIABLE_NAME, EQUIPMENT_ID);
        assertEquals(EQUIPMENT_ID, targetV.getEquipmentId());
    }
}
