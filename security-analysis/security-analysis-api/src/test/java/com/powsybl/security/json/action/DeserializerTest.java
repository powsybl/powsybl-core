/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.commons.json.JsonUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class DeserializerTest {

    public static class SubObject {
        private final String value;

        public SubObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class WrapperOject {

        public WrapperOject(int version, SubObject subObject, List<SubObject> subObjects) {
            this.version = version;
            this.subObject = subObject;
            this.subObjects = subObjects;
        }

        public int getVersion() {
            return version;
        }

        private final int version;
        private final SubObject subObject;
        private final List<SubObject> subObjects;

        public SubObject getSubObject() {
            return subObject;
        }

        public List<SubObject> getSubObjects() {
            return subObjects;
        }
    }

    public static class WrapperDeserializer extends StdDeserializer<WrapperOject> {

        public WrapperDeserializer() {
            super(WrapperOject.class);
        }

        private static class Context {
            int version;
            SubObject subObject;
            List<SubObject> subObjects;
        }

        @Override
        public WrapperOject deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

            var ctxt = new Context();
            JsonUtil.parseObject(jsonParser, name -> {
                switch (name) {
                    case "version":
                        ctxt.version = jsonParser.nextIntValue(0);
                        System.out.println(ctxt.version);
                        deserializationContext.setAttribute("version", Objects.toString(ctxt.version));
                        return true;
                    case "subObject":
                        jsonParser.nextToken();
                        ctxt.subObject = deserializationContext.readValue(jsonParser, SubObject.class);
                        return true;
                    case "subObjects":
                        jsonParser.nextToken();
                        JavaType type = deserializationContext.getTypeFactory().
                                constructCollectionType(List.class, SubObject.class);
                        ctxt.subObjects = deserializationContext.readValue(jsonParser, type);
                        return true;
                }
                return false;
            });
            return new WrapperOject(ctxt.version, ctxt.subObject, ctxt.subObjects);
        }
    }

    public static class SubObjectDeserializer extends StdDeserializer<SubObject> {

        public SubObjectDeserializer() {
            super(WrapperOject.class);
        }

        private static class Context {
            String value;
        }

        @Override
        public SubObject deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            String version = (String) deserializationContext.getAttribute("version");
            System.out.println(version);
            var ctxt = new Context();
            JsonUtil.parseObject(jsonParser, name -> {
                switch (name) {
                    case "value":
                        ctxt.value = jsonParser.nextTextValue();
                        return true;
                }
                return false;
            });
            return null;
        }
    }

    @Test
    public void test() throws IOException {

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new SimpleModule()
                        .addDeserializer(SubObject.class, new SubObjectDeserializer())
                        .addDeserializer(WrapperOject.class, new WrapperDeserializer()));
        ObjectWriter writer = mapper.writerFor(WrapperOject.class);
        ObjectReader reader = mapper.readerFor(WrapperOject.class);

        var sub = new SubObject("test");
        var wrapper = new WrapperOject(1, sub, List.of(sub, sub));

        String serialized = writer.writeValueAsString(wrapper);
        reader.readValue(serialized);
    }
}
