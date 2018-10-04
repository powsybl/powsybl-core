package com.powsybl.triplestore.test;

/*
 * #%L
 * Triple stores for CGMES models
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.triplestore.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PropertyBagTest {
    @BeforeClass
    public static void setUp() {
        locals_ = new PropertyBag(Arrays.asList("id", "name", "enum"));
        boolean removeUnderscoreFromIdentifiers = true;
        locals = new PropertyBag(Arrays.asList("id", "name", "enum"),
                removeUnderscoreFromIdentifiers);
        numbers = new PropertyBag(Arrays.asList("value"));
        booleans = new PropertyBag(Arrays.asList("value"));
        ints = new PropertyBag(Arrays.asList("value"));

        locals.put("id", "http://example.com/#_id0-id1-id2");
        locals.put("name", "name0");
        locals.put("enum", "http://example.com/DataTypeEnum#enumValue0");

        locals_.put("id", "http://example.com/#_id0-id1-id2");
        locals_.put("name", "name0");
        locals_.put("enum", "http://example.com/DataTypeEnum#enumValue0");
    }

    @Test
    public void testLocals() {
        assertEquals("id0-id1-id2", locals.getId("id"));
        assertEquals("id0", locals.getId0("id"));
        assertEquals("_id0-id1-id2", locals.getLocal("id"));
        assertEquals("name0", locals.getLocal("name"));
        assertEquals("enumValue0", locals.getLocal("enum"));

        assertEquals("_id0-id1-id2", locals_.getId("id"));
        assertEquals("_id0", locals_.getId0("id"));
        assertEquals("_id0-id1-id2", locals_.getLocal("id"));
        assertEquals("name0", locals_.getLocal("name"));
        assertEquals("enumValue0", locals_.getLocal("enum"));
    }

    @Test
    public void testTabulateLocals() {
        StringBuffer s = new StringBuffer(100);
        s.append("locals");
        s.append("\n");
        s.append("    id   : _id0-id1-id2\n");
        s.append("    name : name0\n");
        s.append("    enum : enumValue0");
        String expected = s.toString();
        assertEquals(expected, locals.tabulateLocals("locals"));
    }

    @Test
    public void testTabulate() {
        StringBuffer s = new StringBuffer(100);
        s.append("locals");
        s.append("\n");
        s.append("    id   : http://example.com/#_id0-id1-id2\n");
        s.append("    name : name0\n");
        s.append("    enum : http://example.com/DataTypeEnum#enumValue0");
        String expected = s.toString();
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

    @Test(expected = NumberFormatException.class)
    public void testBadFloat0() {
        numbers.put("value", "bad0");
        numbers.asDouble("value", -1);
    }

    @Test(expected = NumberFormatException.class)
    public void testBadFloat1() {
        numbers.put("value", "bad 1");
        numbers.asDouble("value", -1);
    }

    @Test(expected = NumberFormatException.class)
    public void testBadFloat2() {
        numbers.put("value", "2 bad");
        numbers.asDouble("value", -1);
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
        ints.asDouble("value", -1);
    }

    @Test
    public void testBooleans() {
        booleans.put("value", "true");
        assertEquals(true, booleans.asBoolean("value", false));

        booleans.put("value", "false");
        assertEquals(false, booleans.asBoolean("value", true));

        booleans.put("value", "TRUE");
        assertEquals(true, booleans.asBoolean("value", false));

        booleans.put("value", "tRuE");
        assertEquals(true, booleans.asBoolean("value", false));

        booleans.put("value", "FALSE");
        assertEquals(false, booleans.asBoolean("value", true));

        booleans.put("value", "FaLsE");
        assertEquals(false, booleans.asBoolean("value", true));

        // No spaces allowed
        booleans.put("value", " true ");
        assertEquals(false, booleans.asBoolean("value", true));
    }

    private static PropertyBag locals_;
    private static PropertyBag locals;
    private static PropertyBag numbers;
    private static PropertyBag booleans;
    private static PropertyBag ints;
}
