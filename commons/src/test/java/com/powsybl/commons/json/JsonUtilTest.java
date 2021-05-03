/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonUtilTest {

    JsonFactory factory;

    @Before
    public void setup() {
        factory = new JsonFactory();
    }

    @Test
    public void testSkipStartObject() throws IOException {
        String strJson = "{}";
        JsonParser parser = factory.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals("Should have skipped to END_OBJECT", JsonToken.END_OBJECT, parser.currentToken());
        assertNull("Should return null because the whole json is skipped on next START_OBJECT", parser.nextToken());
    }

    @Test
    public void testSkipEndObject() throws IOException {
        String strJson = "{}";
        JsonParser parser = factory.createParser(strJson);
        parser.nextToken();
        JsonUtil.skip(parser);
        assertEquals("Should have skipped to END_OBJECT", JsonToken.END_OBJECT, parser.currentToken());
        assertNull("Should return null because the whole json is skipped on next END_OBJET", parser.nextToken());
    }

    @Test
    public void testSkipStartArray() throws IOException {
        String strJson = "[]";
        JsonParser parser = factory.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals("Should have skipped to END_ARRAY", JsonToken.END_ARRAY, parser.currentToken());
        assertNull("Should return null because the whole json is skipped on next START_ARRAY", parser.nextToken());
    }

    @Test
    public void testSkipEndArray() throws IOException {
        String strJson = "[]";
        JsonParser parser = factory.createParser(strJson);
        parser.nextToken();
        JsonUtil.skip(parser);
        assertEquals("Should have skipped to END_ARRAY", JsonToken.END_ARRAY, parser.currentToken());
        assertNull("Should return null because the whole json is skipped on next END_ARRAY", parser.nextToken());
    }

    @Test
    public void testSkipStringValue() throws IOException {
        String strJson = "\"foo\"";
        JsonParser parser = factory.createParser(strJson);
        JsonUtil.skip(parser);
        assertEquals("Should have skipped to VALUE_STRING", JsonToken.VALUE_STRING, parser.currentToken());
        assertNull("Should return null because the whole json is skipped when on next STRING_VALUE", parser.nextToken());
    }
}
