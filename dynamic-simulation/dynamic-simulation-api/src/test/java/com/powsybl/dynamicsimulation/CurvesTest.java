/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CurvesTest {

    @Test
    public void test() {
        Curves curves = new Curves("busId", "variable1", "variable2");

        assertEquals("busId", curves.getModelId());

        Iterator<String> iterator = curves.getVariables().iterator();
        assertEquals("variable1", iterator.next());
        assertEquals("variable2", iterator.next());
    }
}
