/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityVariableSetTest {

    private static final double EPSILON_COMPARISON = 1e-5;

    @Test
    public void baseTest() {
        WeightedSensitivityVariable wsv = new WeightedSensitivityVariable("v1", 3.4);
        assertEquals("v1", wsv.getId());
        assertEquals(3.4, wsv.getWeight(), EPSILON_COMPARISON);
        assertEquals("WeightedSensitivityVariable(id='v1', weight=3.4)", wsv.toString());

        SensitivityVariableSet set = new SensitivityVariableSet("id", List.of(wsv));
        assertEquals("id", set.getId());
        assertSame(wsv, set.getVariables().get(0));
        assertEquals("SensitivityVariableSet(id='id', variables=[WeightedSensitivityVariable(id='v1', weight=3.4)])", set.toString());
    }
}
