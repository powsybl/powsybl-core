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

import static org.junit.Assert.assertEquals;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityVariableTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void checkNullIdThrows() {
        exception.expect(NullPointerException.class);
        new SensitivityVariable(null, "Name");
    }

    @Test
    public void checkNullNameThrows() {
        exception.expect(NullPointerException.class);
        new SensitivityVariable("ID", null);
    }

    @Test
    public void getName() {
        SensitivityVariable variable = new SensitivityVariable("ID", "Name");
        assertEquals("Name", variable.getName());
    }

    @Test
    public void getId() {
        SensitivityVariable variable = new SensitivityVariable("ID", "Name");
        assertEquals("ID", variable.getId());
    }
}
