/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api.test;

import com.powsybl.triplestore.api.PropertyBag;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PropertyBagTest {

    @BeforeClass
    public static void setUp() {
        localsWithUnderscore = new PropertyBag(Arrays.asList("id", "name", "enum"));
        locals = new PropertyBag(Arrays.asList("id", "name", "enum"), true);
        numbers = new PropertyBag(Collections.singletonList("value"));
        booleans = new PropertyBag(Collections.singletonList("value"));
        ints = new PropertyBag(Collections.singletonList("value"));

        locals.put("id", "http://example.com/#_id0-id1-id2");
        locals.put("name", "name0");
        locals.put("enum", "http://example.com/DataTypeEnum#enumValue0");

        localsWithUnderscore.put("id", "http://example.com/#_id0-id1-id2");
        localsWithUnderscore.put("name", "name0");
        localsWithUnderscore.put("enum", "http://example.com/DataTypeEnum#enumValue0");
    }

    @Test
    public void testHashCodeEquals() {
        // Same property names and different values
        assertNotEquals(locals.hashCode(), localsWithUnderscore.hashCode());
        assertNotEquals(locals, localsWithUnderscore);

        // Same property names and same elements
        PropertyBag locals1 = new PropertyBag(Arrays.asList("id", "name", "enum"), true);
        locals.entrySet().forEach(r -> locals1.put(r.getKey(), r.getValue()));
        assertEquals(locals.hashCode(), locals1.hashCode());
        assertEquals(locals, locals1);

        // Same elements but different property names
        PropertyBag locals2 = new PropertyBag(Arrays.asList("identifier", "name", "enum"), true);
        locals.entrySet().forEach(r -> locals2.put(r.getKey(), r.getValue()));
        assertNotEquals(locals.hashCode(), locals2.hashCode());
        assertNotEquals(locals, locals2);
    }

    @Test
    public void testLocals() {
        assertEquals("id0-id1-id2", locals.getId("id"));
        assertEquals("id0", locals.getId0("id"));
        assertEquals("_id0-id1-id2", locals.getLocal("id"));
        assertEquals("name0", locals.getLocal("name"));
        assertEquals("enumValue0", locals.getLocal("enum"));

        assertEquals("_id0-id1-id2", localsWithUnderscore.getId("id"));
        assertEquals("_id0", localsWithUnderscore.getId0("id"));
        assertEquals("_id0-id1-id2", localsWithUnderscore.getLocal("id"));
        assertEquals("name0", localsWithUnderscore.getLocal("name"));
        assertEquals("enumValue0", localsWithUnderscore.getLocal("enum"));
    }

    @Test
    public void testTabulateLocals() {
        String expected = String.join(System.lineSeparator(),
                "",
                "    id   : _id0-id1-id2",
                "    name : name0",
                "    enum : enumValue0");
        assertEquals("locals" + expected, locals.tabulateLocals("locals"));
        assertEquals(expected, locals.tabulateLocals());

        String base = "http://example.com/#";
        String baseEnum = "http://example.com/DataTypeEnum#";
        expected = String.join(System.lineSeparator(),
                "",
                "    id   : " + base + "_id0-id1-id2",
                "    name : name0",
                "    enum : " + baseEnum + "enumValue0");
        assertEquals(expected, locals.tabulate());
    }

    @Test
    public void testTabulate() {
        String expected = String.join(System.lineSeparator(),
                "locals",
                "    id   : http://example.com/#_id0-id1-id2",
                "    name : name0",
                "    enum : http://example.com/DataTypeEnum#enumValue0");
        assertEquals(expected, locals.tabulate("locals"));
    }

    @Test
    public void testValidFloats() {
        double defaultValue = -1.0;

        numbers.put("value", "0");
        assertEquals(0.0, numbers.asDouble("value", defaultValue), 1e-15);

        numbers.put("value", "1.0");
        assertEquals(1.0, numbers.asDouble("value", defaultValue), 1e-15);

        numbers.put("value", "-123.45");
        assertEquals(-123.45, numbers.asDouble("value", defaultValue), 1e-15);

        numbers.put("value", "NaN");
        assertEquals(Double.NaN, numbers.asDouble("value", defaultValue), 1e-15);

        numbers.put("value", "1e-2");
        assertEquals(0.01, numbers.asDouble("value", defaultValue), 1e-15);

        numbers.put("value", "1.0");
        assertEquals(1.0, numbers.asDouble("value"), 1e-15);

        assertEquals(Double.NaN, numbers.asDouble("missingValue"), 1e-15);
    }

    @Test
    public void testBadFloatValues() {
        // Bad values for doubles return a NaN instead of throwing a number format exception

        numbers.put("value", "nan(ind)");
        assertEquals(Double.NaN, numbers.asDouble("value", -1), 1e-15);

        numbers.put("value", "-nan(ind)");
        assertEquals(Double.NaN, numbers.asDouble("value", -1), 1e-15);

        numbers.put("value", "bad0");
        assertEquals(Double.NaN, numbers.asDouble("value", -1), 1e-15);

        numbers.put("value", "bad 1");
        assertEquals(Double.NaN, numbers.asDouble("value", -1), 1e-15);

        numbers.put("value", "2 bad");
        assertEquals(Double.NaN, numbers.asDouble("value", -1), 1e-15);
    }

    @Test
    public void testValidInts() {
        int defaultValue = -1;

        ints.put("value", "0");
        assertEquals(0, ints.asInt("value", defaultValue));
        assertEquals(0, ints.asInt("value"));

        ints.put("value", "1");
        assertEquals(1, ints.asInt("value", defaultValue));
        assertEquals(1, ints.asInt("value"));

        ints.put("value", "-123");
        assertEquals(-123, ints.asInt("value", defaultValue));
        assertEquals(-123, ints.asInt("value"));
    }

    @Test(expected = NumberFormatException.class)
    public void testBadInt0() {
        ints.put("value", "bad0");
        ints.asInt("value", -1);
    }

    @Test
    public void testBooleans() {
        booleans.put("value", "true");
        assertTrue(booleans.asBoolean("value", false));

        booleans.put("value", "false");
        assertFalse(booleans.asBoolean("value", true));

        booleans.put("value", "TRUE");
        assertTrue(booleans.asBoolean("value", false));

        booleans.put("value", "tRuE");
        assertTrue(booleans.asBoolean("value", false));

        booleans.put("value", "FALSE");
        assertFalse(booleans.asBoolean("value", true));

        booleans.put("value", "FaLsE");
        assertFalse(booleans.asBoolean("value", true));

        // No spaces allowed
        booleans.put("value", " true ");
        assertFalse(booleans.asBoolean("value", true));
    }

    @Test
    public void testCopy() {
        // Ensure we can change a value in the copy and the original is not modified
        PropertyBag locals1 = locals.copy();
        locals1.put("id", "http://example.com/#_id0-id1-id2-0");
        assertEquals("_id0-id1-id2", locals.getLocal("id"));
        assertEquals("_id0-id1-id2-0", locals1.getLocal("id"));
    }

    private static PropertyBag localsWithUnderscore;
    private static PropertyBag locals;
    private static PropertyBag numbers;
    private static PropertyBag booleans;
    private static PropertyBag ints;
}
