/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class StringToIntMapperTest {

    private enum TestSubset implements IntCounter {

        TYPE(1);

        private final int initialValue;

        TestSubset(int initialValue) {
            this.initialValue = initialValue;
        }

        @Override
        public int getInitialValue() {
            return initialValue;
        }
    }

    @Test
    public void test() throws IOException {
        StringToIntMapper<TestSubset> mapper = new StringToIntMapper<>(TestSubset.class);
        testAddMapping(mapper);
        mapper.reset(TestSubset.TYPE);
        testAddMapping(mapper);
        try {
            mapper.reset(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void testAddMapping(StringToIntMapper<TestSubset> mapper) throws IOException {
        String value = "value1";
        assertFalse(mapper.isMapped(TestSubset.TYPE, value));
        try {
            mapper.getId(TestSubset.TYPE, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getInt(TestSubset.TYPE, value);
            fail();
        } catch (IllegalStateException ignored) {
        }

        int num = mapper.newInt(TestSubset.TYPE, value);
        assertTrue(mapper.isMapped(TestSubset.TYPE, value));
        assertEquals(1, num);
        assertEquals(value, mapper.getId(TestSubset.TYPE, 1));
        assertEquals(1, mapper.getInt(TestSubset.TYPE, value));

        value = "value2";
        num = mapper.newInt(TestSubset.TYPE, value);
        assertTrue(mapper.isMapped(TestSubset.TYPE, value));
        assertEquals(2, num);
        assertEquals(value, mapper.getId(TestSubset.TYPE, 2));
        assertEquals(2, mapper.getInt(TestSubset.TYPE, value));

        String content = String.join(System.lineSeparator(), "TYPE;value1;1", "TYPE;value2;2");
        try (Writer writer = new StringWriter()) {
            mapper.dump(writer);
            assertEquals(content, writer.toString().trim());
        }

        try {
            mapper.newInt(null, value);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.newInt(TestSubset.TYPE, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            mapper.getId(null, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(TestSubset.TYPE, 0);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(TestSubset.TYPE, 3);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            mapper.getInt(null, value);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getInt(TestSubset.TYPE, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

}
