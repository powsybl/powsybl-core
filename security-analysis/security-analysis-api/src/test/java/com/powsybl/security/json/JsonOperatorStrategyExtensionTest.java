/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.ComparisonUtils;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.condition.TrueCondition;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class JsonOperatorStrategyExtensionTest extends AbstractConverterTest {

    @Test
    public void testWrite() throws IOException {
        OperatorStrategy operatorStrategy = new OperatorStrategy("id1", "contingencyId1",
                new TrueCondition(), Arrays.asList("actionId1", "actionId2", "actionId3"));
        operatorStrategy.addExtension(DummyExtension.class, new DummyExtension());
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(Collections.singletonList(operatorStrategy));
        writeTest(operatorStrategyList, OperatorStrategyList::writeFile, ComparisonUtils::compareTxt,
                "/OperatorStrategyFileExtensionsTest.json");
    }

    @Test
    public void testRoundTrip() throws IOException {
        OperatorStrategy operatorStrategy = new OperatorStrategy("id1", "contingencyId1",
                new TrueCondition(), Arrays.asList("actionId1", "actionId2", "actionId3"));
        operatorStrategy.addExtension(DummyExtension.class, new DummyExtension());
        OperatorStrategy operatorStrategy2 = new OperatorStrategy("id2", "contingencyId2",
                new TrueCondition(), Collections.singletonList("actionId4"));
        operatorStrategy2.addExtension(DummyExtension.class, new DummyExtension());
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(Arrays.asList(operatorStrategy, operatorStrategy2));
        roundTripTest(operatorStrategyList, OperatorStrategyList::writeFile, OperatorStrategyList::readFile,
                "/OperatorStrategyFileExtensionsTest2.json");
    }

    public static class DummyExtension extends AbstractExtension<OperatorStrategy> {
        public double parameterDouble;
        public boolean parameterBoolean;
        public String parameterString;

        public DummyExtension() {
            super();
            this.parameterDouble = 20.0;
            this.parameterBoolean = true;
            this.parameterString = "Hello";
        }

        public DummyExtension(DummyExtension another) {
            this.parameterDouble = another.parameterDouble;
            this.parameterBoolean = another.parameterBoolean;
            this.parameterString = another.parameterString;
        }

        public boolean isParameterBoolean() {
            return parameterBoolean;
        }

        public double getParameterDouble() {
            return parameterDouble;
        }

        public String getParameterString() {
            return parameterString;
        }

        public void setParameterBoolean(boolean parameterBoolean) {
            this.parameterBoolean = parameterBoolean;
        }

        public void setParameterString(String parameterString) {
            this.parameterString = parameterString;
        }

        public void setParameterDouble(double parameterDouble) {
            this.parameterDouble = parameterDouble;
        }

        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(ExtensionJsonSerializer.class)
    public static class DummySerializer implements ExtensionJsonSerializer<OperatorStrategy, DummyExtension> {
        private interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            OperatorStrategy getExtendable();
        }

        private static ObjectMapper createMapper() {
            return JsonUtil.createObjectMapper()
                    .addMixIn(DummyExtension.class, DummySerializer.SerializationSpec.class);
        }

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("parameterDouble", extension.parameterDouble);
            jsonGenerator.writeBooleanField("parameterBoolean", extension.parameterBoolean);
            jsonGenerator.writeStringField("parameterString", extension.parameterString);
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser,
                                          DeserializationContext deserializationContext) throws IOException {
            DummyExtension dummyExtension = new DummyExtension();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                switch (jsonParser.getCurrentName()) {
                    case "parameterBoolean":
                        jsonParser.nextToken();
                        dummyExtension.setParameterBoolean(jsonParser.getValueAsBoolean());
                        break;
                    case "parameterDouble":
                        jsonParser.nextToken();
                        dummyExtension.setParameterDouble(jsonParser.getValueAsDouble());
                        break;
                    case "parameterString":
                        dummyExtension.setParameterString(jsonParser.nextTextValue());
                        break;
                    default:
                        throw new PowsyblException("Unexpected field " + jsonParser.getCurrentName());
                }
            }
            return dummyExtension;
        }

        @Override
        public DummyExtension deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext,
                                                   DummyExtension parameters) throws IOException {
            ObjectMapper objectMapper = createMapper();
            ObjectReader objectReader = objectMapper.readerForUpdating(parameters);
            DummyExtension updatedParameters = objectReader.readValue(jsonParser, DummyExtension.class);
            return updatedParameters;
        }

        @Override
        public String getExtensionName() {
            return "dummy-extension";
        }

        @Override
        public String getCategoryName() {
            return "security-analysis";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }
}
