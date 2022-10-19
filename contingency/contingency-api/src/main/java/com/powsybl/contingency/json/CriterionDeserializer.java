/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.contingency.contingency.list.criterion.*;
import com.powsybl.iidm.network.Country;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionDeserializer extends StdDeserializer<Criterion> {

    public CriterionDeserializer() {
        super(Criterion.class);
    }

    @Override
    public Criterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Criterion.CriterionType type = null;
        SingleNominalVoltageCriterion.VoltageInterval voltageInterval = null;
        SingleNominalVoltageCriterion.VoltageInterval voltageInterval1 = null;
        SingleNominalVoltageCriterion.VoltageInterval voltageInterval2 = null;
        SingleNominalVoltageCriterion.VoltageInterval voltageInterval3 = null;
        List<String> countries = Collections.emptyList();
        List<String> countries1 = Collections.emptyList();
        List<String> countries2 = Collections.emptyList();
        String propertyKey = null;
        String regex = null;
        List<String> propertyValues = Collections.emptyList();
        PropertyCriterion.EquipmentToCheck equipmentToCheck = null;
        PropertyCriterion.SideToCheck sideToCheck = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "type":
                    type = Criterion.CriterionType.valueOf(parser.nextTextValue());
                    break;
                case "voltageInterval":
                    parser.nextToken();
                    voltageInterval = parser.readValueAs(new TypeReference<SingleNominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;
                case "voltageInterval1":
                    parser.nextToken();
                    voltageInterval1 = parser.readValueAs(new TypeReference<SingleNominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;
                case "voltageInterval2":
                    parser.nextToken();
                    voltageInterval2 = parser.readValueAs(new TypeReference<SingleNominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;
                case "voltageInterval3":
                    parser.nextToken();
                    voltageInterval3 = parser.readValueAs(new TypeReference<SingleNominalVoltageCriterion.VoltageInterval>() {
                    });
                    break;
                case "countries":
                    parser.nextToken();
                    countries = parser.readValueAs(new TypeReference<ArrayList<String>>() {
                    });
                    break;
                case "countries1":
                    parser.nextToken();
                    countries1 = parser.readValueAs(new TypeReference<ArrayList<String>>() {
                    });
                    break;
                case "countries2":
                    parser.nextToken();
                    countries2 = parser.readValueAs(new TypeReference<ArrayList<String>>() {
                    });
                    break;
                case "propertyKey":
                    propertyKey = parser.nextTextValue();
                    break;
                case "regex":
                    regex = parser.nextTextValue();
                    break;
                case "propertyValue":
                    parser.nextToken();
                    propertyValues = parser.readValueAs(new TypeReference<ArrayList<String>>() {
                    });
                    break;
                case "equipmentToCheck":
                    equipmentToCheck = PropertyCriterion.EquipmentToCheck.valueOf(parser.nextTextValue());
                    break;
                case "sideToCheck":
                    sideToCheck = PropertyCriterion.SideToCheck.valueOf(parser.nextTextValue());
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("type of criterion can not be null");
        }
        switch (type) {
            case PROPERTY:
                return new PropertyCriterion(propertyKey, propertyValues, equipmentToCheck, sideToCheck);
            case SINGLE_COUNTRY:
                return new SingleCountryCriterion(countries.stream().map(Country::valueOf).collect(Collectors.toList()));
            case TWO_COUNTRY:
                return new TwoCountriesCriterion(countries1.stream().map(Country::valueOf).collect(Collectors.toList()),
                        countries2.stream().map(Country::valueOf).collect(Collectors.toList()));
            case SINGLE_NOMINAL_VOLTAGE:
                return new SingleNominalVoltageCriterion(voltageInterval);
            case TWO_NOMINAL_VOLTAGE:
                return new TwoNominalVoltageCriterion(voltageInterval1, voltageInterval2);
            case THREE_NOMINAL_VOLTAGE:
                return new ThreeNominalVoltageCriterion(voltageInterval1, voltageInterval2, voltageInterval3);
            case REGEX:
                return new RegexCriterion(regex);
            default:
                throw new IllegalArgumentException("type is not correct");
        }
    }
}
