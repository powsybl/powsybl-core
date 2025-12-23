/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.json;

import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.BeforeEach;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static com.powsybl.commons.json.JsonUtil.createJsonMapper;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    JsonMapper mapper;

    @BeforeEach
    void setup() {
        mapper = createJsonMapper();
    }

    @Test
    void testSkipStartObject() {
        String strJson = "{}";
        JsonParser parser = mapper.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_OBJECT, parser.currentToken(), "Should have skipped to END_OBJECT");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next START_OBJECT");
    }

    @Test
    void testSkipEndObject() {
        String strJson = "{}";
        JsonParser parser = mapper.createParser(strJson);
        parser.nextToken();
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_OBJECT, parser.currentToken(), "Should have skipped to END_OBJECT");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next END_OBJET");
    }

    @Test
    void testSkipStartArray() {
        String strJson = "[]";
        JsonParser parser = mapper.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_ARRAY, parser.currentToken(), "Should have skipped to END_ARRAY");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next START_ARRAY");
    }

    @Test
    void testSkipEndArray() {
        String strJson = "[]";
        JsonParser parser = mapper.createParser(strJson);
        parser.nextToken();
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_ARRAY, parser.currentToken(), "Should have skipped to END_ARRAY");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next END_ARRAY");
    }

    @Test
    void testSkipStringValue() {
        String strJson = "\"foo\"";
        JsonParser parser = mapper.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals(JsonToken.VALUE_STRING, parser.currentToken(), "Should have skipped to VALUE_STRING");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped when on next STRING_VALUE");
    }

    @Test
    void testCompareVersions() {
        assertTrue(JsonUtil.compareVersions("1.9", "1.10") < 0);
        assertTrue(JsonUtil.compareVersions("2.0", "1.9") > 0);

        assertTrue(JsonUtil.compareVersions("1.10", "1.10.1") < 0);
        assertTrue(JsonUtil.compareVersions("1.10.2", "1.10") > 0);

        assertEquals(0, JsonUtil.compareVersions("1.10.1", "1.10.1"));

        assertTrue(JsonUtil.compareVersions("A.2", "B.2") < 0);
        assertTrue(JsonUtil.compareVersions("B.2", "A.2") > 0);

        assertTrue(JsonUtil.compareVersions("3.2", "3.C") < 0);
        assertTrue(JsonUtil.compareVersions("3.C.2", "3.C") > 0);
    }

    @Test
    void testAssertSupportedVersion() {
        String contextName = "TestContext";
        String supportedVersion = "1.9";
        String unsupportedVersion = "1.11";
        String maxSupportedVersion = "1.10";
        assertDoesNotThrow(() -> JsonUtil.assertSupportedVersion(contextName, supportedVersion, maxSupportedVersion));
        String expectedException = String.format("%s. Unsupported version %s. Version should be <= %s %n", contextName, unsupportedVersion, maxSupportedVersion);
        assertThrows(PowsyblException.class, () -> JsonUtil.assertSupportedVersion(contextName, unsupportedVersion, maxSupportedVersion), expectedException);
    }
}
