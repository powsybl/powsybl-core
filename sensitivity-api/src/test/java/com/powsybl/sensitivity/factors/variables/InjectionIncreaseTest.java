/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class InjectionIncreaseTest {

    private static final String VARIABLE_ID = "Variable ID";
    private static final String VARIABLE_NAME = "Variable name";
    private static final String INJECTION_ID = "Injection ID";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkFailsWhenNullInjection() {
        exception.expect(NullPointerException.class);
        new InjectionIncrease(VARIABLE_ID, VARIABLE_NAME, null);
    }

    @Test
    public void getName() {
        InjectionIncrease branchIntensity = new InjectionIncrease(VARIABLE_ID, VARIABLE_NAME, INJECTION_ID);
        assertEquals(VARIABLE_NAME, branchIntensity.getName());
    }

    @Test
    public void getId() {
        InjectionIncrease branchIntensity = new InjectionIncrease(VARIABLE_ID, VARIABLE_NAME, INJECTION_ID);
        assertEquals(VARIABLE_ID, branchIntensity.getId());
    }

    @Test
    public void getLine() {
        InjectionIncrease branchIntensity = new InjectionIncrease(VARIABLE_ID, VARIABLE_NAME, INJECTION_ID);
        assertEquals(INJECTION_ID, branchIntensity.getInjectionId());
    }
}
