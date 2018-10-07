/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.iidm.network.Country;
import com.powsybl.security.LimitViolation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionJsonSerializer.class)
public class SubjectInfoExtensionSerializer implements ExtensionJsonSerializer<LimitViolation, SubjectInfoExtension> {

    @Override
    public String getExtensionName() {
        return "SubjectInfo";
    }

    @Override
    public String getCategoryName() {
        return "security-analysis";
    }

    @Override
    public Class<SubjectInfoExtension> getExtensionClass() {
        return SubjectInfoExtension.class;
    }

    @Override
    public void serialize(SubjectInfoExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeFieldName("nominalVoltages");
        jsonGenerator.writeStartArray();
        for (double nominalVoltage : extension.getNominalVoltages()) {
            jsonGenerator.writeNumber(nominalVoltage);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeFieldName("countries");
        jsonGenerator.writeStartArray();
        for (Country country : extension.getCountries()) {
            jsonGenerator.writeString(country.name());
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }

    @Override
    public SubjectInfoExtension deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Set<Double> nominalVoltages = new HashSet<>();
        Set<Country> countries = new HashSet<>();

        JsonToken token;
        while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {
            if (token == JsonToken.VALUE_STRING) {
                countries.add(Country.valueOf(parser.getValueAsString()));
            } else if (token == JsonToken.VALUE_NUMBER_FLOAT ||
                    token == JsonToken.VALUE_NUMBER_INT) {
                nominalVoltages.add(parser.getDoubleValue());
            }
        }

        return new SubjectInfoExtension(nominalVoltages, countries);
    }
}
