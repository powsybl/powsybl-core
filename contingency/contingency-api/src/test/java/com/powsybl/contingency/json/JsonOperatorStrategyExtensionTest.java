/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.OperatorStrategyList;
import com.powsybl.contingency.strategy.condition.TrueCondition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class JsonOperatorStrategyExtensionTest extends AbstractSerDeTest {

    @Test
    void testWrite() throws IOException {
        OperatorStrategy operatorStrategy = new OperatorStrategy("id1", ContingencyContext.specificContingency("contingencyId1"),
                new TrueCondition(), Arrays.asList("actionId1", "actionId2", "actionId3"));
        operatorStrategy.addExtension(DummyOperatorStrategyExtension.class, new DummyOperatorStrategyExtension());
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(Collections.singletonList(operatorStrategy));
        writeTest(operatorStrategyList, OperatorStrategyList::write, ComparisonUtils::assertTxtEquals,
                "/OperatorStrategyFileExtensionsTest.json");
    }

    @Test
    void testRoundTrip() throws IOException {
        OperatorStrategy operatorStrategy = new OperatorStrategy("id1", ContingencyContext.specificContingency("contingencyId1"),
                new TrueCondition(), Arrays.asList("actionId1", "actionId2", "actionId3"));
        operatorStrategy.addExtension(DummyOperatorStrategyExtension.class, new DummyOperatorStrategyExtension());
        OperatorStrategy operatorStrategy2 = new OperatorStrategy("id2", ContingencyContext.specificContingency("contingencyId2"),
                new TrueCondition(), Collections.singletonList("actionId4"));
        operatorStrategy2.addExtension(DummyOperatorStrategyExtension.class, new DummyOperatorStrategyExtension());
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(Arrays.asList(operatorStrategy, operatorStrategy2));
        roundTripTest(operatorStrategyList, OperatorStrategyList::write, OperatorStrategyList::read,
                "/OperatorStrategyFileExtensionsTest2.json");
    }

    public static class DummyOperatorStrategyExtension extends AbstractExtension<OperatorStrategy> {
        double parameterDouble;
        boolean parameterBoolean;
        String parameterString;

        DummyOperatorStrategyExtension() {
            super();
            this.parameterDouble = 20.0;
            this.parameterBoolean = true;
            this.parameterString = "Hello";
        }

        DummyOperatorStrategyExtension(DummyOperatorStrategyExtension another) {
            this.parameterDouble = another.parameterDouble;
            this.parameterBoolean = another.parameterBoolean;
            this.parameterString = another.parameterString;
        }

        boolean isParameterBoolean() {
            return parameterBoolean;
        }

        double getParameterDouble() {
            return parameterDouble;
        }

        String getParameterString() {
            return parameterString;
        }

        void setParameterBoolean(boolean parameterBoolean) {
            this.parameterBoolean = parameterBoolean;
        }

        void setParameterString(String parameterString) {
            this.parameterString = parameterString;
        }

        void setParameterDouble(double parameterDouble) {
            this.parameterDouble = parameterDouble;
        }

        @Override
        public String getName() {
            return "dummy-operator-strategy-extension";
        }
    }

    @AutoService(ExtensionJsonSerializer.class)
    public static class DummyOperatorStrategySerializer implements ExtensionJsonSerializer<OperatorStrategy, DummyOperatorStrategyExtension> {
        private interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            OperatorStrategy getExtendable();
        }

        private static ObjectMapper createMapper() {
            return JsonUtil.createObjectMapper()
                    .addMixIn(DummyOperatorStrategyExtension.class, DummyOperatorStrategySerializer.SerializationSpec.class);
        }

        @Override
        public void serialize(DummyOperatorStrategyExtension extension, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("parameterDouble", extension.parameterDouble);
            jsonGenerator.writeBooleanField("parameterBoolean", extension.parameterBoolean);
            jsonGenerator.writeStringField("parameterString", extension.parameterString);
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyOperatorStrategyExtension deserialize(JsonParser jsonParser,
                                                          DeserializationContext deserializationContext) throws IOException {
            DummyOperatorStrategyExtension dummyExtension = new DummyOperatorStrategyExtension();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                switch (jsonParser.currentName()) {
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
                        throw new PowsyblException("Unexpected field " + jsonParser.currentName());
                }
            }
            return dummyExtension;
        }

        @Override
        public DummyOperatorStrategyExtension deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext,
                                                                   DummyOperatorStrategyExtension parameters) throws IOException {
            ObjectMapper objectMapper = createMapper();
            ObjectReader objectReader = objectMapper.readerForUpdating(parameters);
            return objectReader.readValue(jsonParser, DummyOperatorStrategyExtension.class);
        }

        @Override
        public String getExtensionName() {
            return "dummy-operator-strategy-extension";
        }

        @Override
        public String getCategoryName() {
            return "security-analysis";
        }

        @Override
        public Class<? super DummyOperatorStrategyExtension> getExtensionClass() {
            return DummyOperatorStrategyExtension.class;
        }
    }
}
