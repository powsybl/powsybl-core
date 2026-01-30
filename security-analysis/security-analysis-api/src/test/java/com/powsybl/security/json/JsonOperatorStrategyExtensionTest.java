/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.security.condition.TrueCondition;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

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
        operatorStrategy.addExtension(DummyExtension.class, new DummyExtension());
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(Collections.singletonList(operatorStrategy));
        writeTest(operatorStrategyList, OperatorStrategyList::write, ComparisonUtils::assertTxtEquals,
                "/OperatorStrategyFileExtensionsTest.json");
    }

    @Test
    void testRoundTrip() throws IOException {
        OperatorStrategy operatorStrategy = new OperatorStrategy("id1", ContingencyContext.specificContingency("contingencyId1"),
                new TrueCondition(), Arrays.asList("actionId1", "actionId2", "actionId3"));
        operatorStrategy.addExtension(DummyExtension.class, new DummyExtension());
        OperatorStrategy operatorStrategy2 = new OperatorStrategy("id2", ContingencyContext.specificContingency("contingencyId2"),
                new TrueCondition(), Collections.singletonList("actionId4"));
        operatorStrategy2.addExtension(DummyExtension.class, new DummyExtension());
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(Arrays.asList(operatorStrategy, operatorStrategy2));
        roundTripTest(operatorStrategyList, OperatorStrategyList::write, OperatorStrategyList::read,
                "/OperatorStrategyFileExtensionsTest2.json");
    }

    static class DummyExtension extends AbstractExtension<OperatorStrategy> {
        double parameterDouble;
        boolean parameterBoolean;
        String parameterString;

        DummyExtension() {
            super();
            this.parameterDouble = 20.0;
            this.parameterBoolean = true;
            this.parameterString = "Hello";
        }

        DummyExtension(DummyExtension another) {
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

        private static JsonMapper createMapper() {
            return JsonUtil.createJsonMapperBuilder()
                .addMixIn(DummyExtension.class, DummySerializer.SerializationSpec.class)
                .build();
        }

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator,
                              SerializationContext serializationContext) throws JacksonException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberProperty("parameterDouble", extension.parameterDouble);
            jsonGenerator.writeBooleanProperty("parameterBoolean", extension.parameterBoolean);
            jsonGenerator.writeStringProperty("parameterString", extension.parameterString);
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser,
                                          DeserializationContext deserializationContext) throws JacksonException {
            DummyExtension dummyExtension = new DummyExtension();
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
                        dummyExtension.setParameterString(jsonParser.nextStringValue());
                        break;
                    default:
                        throw new PowsyblException("Unexpected field " + jsonParser.currentName());
                }
            }
            return dummyExtension;
        }

        @Override
        public DummyExtension deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext,
                                                   DummyExtension parameters) throws JacksonException {
            JsonMapper jsonMapper = createMapper();
            ObjectReader objectReader = jsonMapper.readerForUpdating(parameters);
            return objectReader.readValue(jsonParser);
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
