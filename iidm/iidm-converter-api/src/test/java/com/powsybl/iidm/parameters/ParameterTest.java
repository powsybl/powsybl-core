/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.parameters;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ParameterTest {

    @Test
    public void possibleValuesTest() {
        Parameter p1 = new Parameter("p1", ParameterType.STRING, "a param", "a", List.of("a", "b", "c"));
        p1.addAdditionalNames("pone");
        assertEquals("p1", p1.getName());
        assertEquals(List.of("p1", "pone"), p1.getNames());
        assertEquals(ParameterType.STRING, p1.getType());
        assertEquals("a param", p1.getDescription());
        assertEquals("a", p1.getDefaultValue());
        assertEquals(List.of("a", "b", "c"), p1.getPossibleValues());
    }

    @Test
    public void defaultValueNotInPossibleValuesTest() {
        List<Object> possibleValues = List.of("a", "b", "c");
        var e = assertThrows(IllegalArgumentException.class, () -> new Parameter("p1", ParameterType.STRING, "a param", "d", possibleValues));
        assertEquals("Parameter possible values [a, b, c] should contain default value d", e.getMessage());
    }

    @Test(expected = Test.None.class)
    public void defaultValueWithStringListParamTest() {
        List<Object> possibleValues = List.of("a", "b", "c");
        new Parameter("p1", ParameterType.STRING_LIST, "a str list param", List.of("a", "c"), possibleValues);
    }
}
