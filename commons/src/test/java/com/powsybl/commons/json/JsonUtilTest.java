/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.json;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    JsonFactory factory;

    @BeforeEach
    void setup() {
        factory = new JsonFactory();
    }

    @Test
    void testSkipStartObject() throws IOException {
        String strJson = "{}";
        JsonParser parser = factory.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_OBJECT, parser.currentToken(), "Should have skipped to END_OBJECT");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next START_OBJECT");
    }

    @Test
    void testSkipEndObject() throws IOException {
        String strJson = "{}";
        JsonParser parser = factory.createParser(strJson);
        parser.nextToken();
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_OBJECT, parser.currentToken(), "Should have skipped to END_OBJECT");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next END_OBJET");
    }

    @Test
    void testSkipStartArray() throws IOException {
        String strJson = "[]";
        JsonParser parser = factory.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_ARRAY, parser.currentToken(), "Should have skipped to END_ARRAY");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next START_ARRAY");
    }

    @Test
    void testSkipEndArray() throws IOException {
        String strJson = "[]";
        JsonParser parser = factory.createParser(strJson);
        parser.nextToken();
        JsonUtil.skip(parser);
        assertEquals(JsonToken.END_ARRAY, parser.currentToken(), "Should have skipped to END_ARRAY");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped on next END_ARRAY");
    }

    @Test
    void testSkipStringValue() throws IOException {
        String strJson = "\"foo\"";
        JsonParser parser = factory.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals(JsonToken.VALUE_STRING, parser.currentToken(), "Should have skipped to VALUE_STRING");
        assertNull(parser.nextToken(), "Should return null because the whole json is skipped when on next STRING_VALUE");
    }
}
