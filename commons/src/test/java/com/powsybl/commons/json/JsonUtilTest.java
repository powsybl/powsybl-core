/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

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

    private abstract static class AbstractTestSerializer implements ExtensionJsonSerializer<Extendable<Object>, Extension<Extendable<Object>>> {

        private final String name;

        AbstractTestSerializer(String name) {
            this.name = name;
        }

        @Override
        public void serialize(Extension<Extendable<Object>> extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Extension<Extendable<Object>> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getExtensionName() {
            return name;
        }

        @Override
        public String getCategoryName() {
            return "test";
        }

        @Override
        public Class<? super Extension<Extendable<Object>>> getExtensionClass() {
            return Extension.class;
        }
    }

    private static final class FooSerializer extends AbstractTestSerializer {
        FooSerializer() {
            super("foo");
        }
    }

    private static final class OtherFooSerializer extends AbstractTestSerializer {
        OtherFooSerializer() {
            super("foo");
        }
    }

    private static final class BarSerializer extends AbstractTestSerializer {
        BarSerializer() {
            super("bar");
        }
    }

    @Test
    void testGetExtensionSerializersByName() {
        Map<String, ExtensionJsonSerializer> serializers = JsonUtil.getExtensionSerializersByName(Stream.of(new FooSerializer(), new BarSerializer(), new FooSerializer()));
        assertEquals(2, serializers.size());
        assertInstanceOf(FooSerializer.class, serializers.get("foo"));
        assertInstanceOf(BarSerializer.class, serializers.get("bar"));

        Stream<ExtensionJsonSerializer> duplicates = Stream.of(new FooSerializer(), new OtherFooSerializer());
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> JsonUtil.getExtensionSerializersByName(duplicates));
        assertEquals("Several serializers were found for extension 'foo'", e.getMessage());
    }
}
