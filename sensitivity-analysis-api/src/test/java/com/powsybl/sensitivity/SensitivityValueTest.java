/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityValueTest {

    private static final double EPSILON_COMPARISON = 1e-5;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkNullFactorThrows() {
        exception.expect(NullPointerException.class);
        new SensitivityValue(null, 1.f, 2.f, 3.f);
    }

    @Test
    public void getFactor() {
        SensitivityFactor factor = Mockito.mock(SensitivityFactor.class);
        SensitivityValue sensitivityValue = new SensitivityValue(factor, 1.f, 2.f, 3.f);
        assertSame(factor, sensitivityValue.getFactor());
    }

    @Test
    public void getValue() {
        SensitivityValue sensitivityValue = new SensitivityValue(Mockito.mock(SensitivityFactor.class), 1.f, 2.f, 3.f);
        assertEquals(1.f, sensitivityValue.getValue(), EPSILON_COMPARISON);
    }

    @Test
    public void getFunctionReference() {
        SensitivityValue sensitivityValue = new SensitivityValue(Mockito.mock(SensitivityFactor.class), 1.f, 2.f, 3.f);
        assertEquals(2.f, sensitivityValue.getFunctionReference(), EPSILON_COMPARISON);
    }

    @Test
    public void getVariableReference() {
        SensitivityValue sensitivityValue = new SensitivityValue(Mockito.mock(SensitivityFactor.class), 1.f, 2.f, 3.f);
        assertEquals(3.f, sensitivityValue.getVariableReference(), EPSILON_COMPARISON);
    }
}
