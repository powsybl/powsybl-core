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

import static org.junit.Assert.assertSame;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityFactorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkNullFunctionThrows() {
        exception.expect(NullPointerException.class);
        new SensitivityFactor(null, Mockito.mock(SensitivityVariable.class));
    }

    @Test
    public void checkNullVariableThrows() {
        exception.expect(NullPointerException.class);
        new SensitivityFactor(Mockito.mock(SensitivityFunction.class), null);
    }

    @Test
    public void getFunction() {
        SensitivityFunction function = Mockito.mock(SensitivityFunction.class);
        SensitivityFactor factor = new SensitivityFactor(function, Mockito.mock(SensitivityVariable.class));
        assertSame(function, factor.getFunction());
    }

    @Test
    public void getVariable() {
        SensitivityVariable variable = Mockito.mock(SensitivityVariable.class);
        SensitivityFactor factor = new SensitivityFactor(Mockito.mock(SensitivityFunction.class), variable);
        assertSame(variable, factor.getVariable());
    }
}
