/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import com.powsybl.commons.PowsyblException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class LinearGlskTest {

    private static final String VARIABLE_ID = "Variable ID";
    private static final String VARIABLE_NAME = "Variable name";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Map<String, Float> glskMap;

    @Before
    public void setUp() {
        glskMap = new HashMap<>();
        glskMap.put("Generator", 0.8f);
        glskMap.put("Load", 0.2f);
    }

    @Test
    public void checkFailsWhenNullGlskMap() {
        exception.expect(NullPointerException.class);
        new LinearGlsk(VARIABLE_ID, VARIABLE_NAME, null);
    }

    @Test
    public void checkFailsWhenEmptyGlskMap() {
        exception.expect(PowsyblException.class);
        new LinearGlsk(VARIABLE_ID, VARIABLE_NAME, Collections.emptyMap());
    }

    @Test
    public void getName() {
        LinearGlsk linearGlsk = new LinearGlsk(VARIABLE_ID, VARIABLE_NAME, glskMap);
        assertEquals(VARIABLE_NAME, linearGlsk.getName());
    }

    @Test
    public void getId() {
        LinearGlsk linearGlsk = new LinearGlsk(VARIABLE_ID, VARIABLE_NAME, glskMap);
        assertEquals(VARIABLE_ID, linearGlsk.getId());
    }

    @Test
    public void getGLSKs() {
        LinearGlsk linearGlsk = new LinearGlsk(VARIABLE_ID, VARIABLE_NAME, glskMap);
        assertEquals(2, linearGlsk.getGLSKs().size());
    }
}
