/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.test;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.powsybl.commons.test.ComparisonUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ComparisonUtilsTest {

    @Test
    void test() {
        InputStream actual = getClass().getResourceAsStream("/test1.xml");
        assertNotNull(actual);
        InputStream expected = getClass().getResourceAsStream("/test2.xml");
        assertNotNull(expected);
        assertThrows(AssertionError.class, () -> assertMismatch(expected, actual));
    }

    @Test
    void testAssertXmlEquals() {
        InputStream expected = new ByteArrayInputStream("<root><a>1</a></root>\n".getBytes(StandardCharsets.UTF_8));
        InputStream actual = new ByteArrayInputStream("<root><a>1 </a></root>\n".getBytes(StandardCharsets.UTF_8));
         assertDoesNotThrow(() -> assertXmlEquals(expected, actual));
    }

    //assert Bytes
    @Test
    void assertBytesEqualsShouldSucceed() {
        InputStream expected = new ByteArrayInputStream(new byte[]{1, 2, 3});
        InputStream actual = new ByteArrayInputStream(new byte[]{1, 2, 3});
        assertDoesNotThrow(() -> assertBytesEquals(expected, actual));
    }

    @Test
    void assertBytesEqualsShouldDetectDiff() {
        InputStream expected = new ByteArrayInputStream(new byte[]{1, 2, 3});
        InputStream actual = new ByteArrayInputStream(new byte[]{1, 9, 3});
        assertThrows(AssertionError.class, () -> assertBytesEquals(expected, actual));
    }
}
