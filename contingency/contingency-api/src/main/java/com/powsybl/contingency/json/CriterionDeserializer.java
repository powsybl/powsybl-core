/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.contingency.contingency.list.criterion.CountryCriterion;
import com.powsybl.contingency.contingency.list.criterion.Criterion;
import com.powsybl.contingency.contingency.list.criterion.NominalVoltageCriterion;
import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionDeserializer extends StdDeserializer<Criterion> {

    public CriterionDeserializer() {
        super(Criterion.class);
    }

    @Override
    public Criterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        Criterion.CriterionType type = null;
        NominalVoltageCriterion.VoltageInterval voltageInterval = null;
        NominalVoltageCriterion.VoltageInterval voltageInterval1 = null;
        NominalVoltageCriterion.VoltageInterval voltageInterval2 = null;
        NominalVoltageCriterion.VoltageInterval voltageInterval3 = null;
        String country = null;
        String country1 = null;
        String country2 = null;
        String propertyKey = null;
        String propertyValue = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "type":
                    type = Criterion.CriterionType.valueOf(parser.nextTextValue());
                    break;

                case "voltageInterval":
                    parser.nextToken();
                    voltageInterval = parser.readValueAs(new TypeReference<NominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;

                case "voltageInterval1":
                    parser.nextToken();
                    voltageInterval1 = parser.readValueAs(new TypeReference<NominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;

                case "voltageInterval2":
                    parser.nextToken();
                    voltageInterval2 = parser.readValueAs(new TypeReference<NominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;

                case "voltageInterval3":
                    parser.nextToken();
                    voltageInterval3 = parser.readValueAs(new TypeReference<NominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;

                case "country":
                    country = parser.nextTextValue();
                    break;

                case "country1":
                    country1 = parser.nextTextValue();
                    break;

                case "country2":
                    country2 = parser.nextTextValue();
                    break;

                case "propertyKey":
                    propertyKey = parser.nextTextValue();
                    break;

                case "propertyValue":
                    propertyValue = parser.nextTextValue();
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        switch (type) {
            case PROPERTY:
                return new PropertyCriterion(propertyKey, propertyValue);
            case COUNTRY:
                return new CountryCriterion(country, country1, country2);
            case NOMINAL_VOLTAGE:
                return new NominalVoltageCriterion(voltageInterval, voltageInterval1, voltageInterval2, voltageInterval3);
            default:
                throw new IllegalArgumentException("type is not correct");
        }
    }
}
