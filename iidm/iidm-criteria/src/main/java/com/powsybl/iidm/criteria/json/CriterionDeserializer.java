/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.criteria.Criterion.CriterionType;
import com.powsybl.iidm.network.Country;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class CriterionDeserializer extends StdDeserializer<Criterion> {

    public CriterionDeserializer() {
        super(Criterion.class);
    }

    @Override
    public Criterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        CriterionType type = null;
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
                case "type" -> type = CriterionType.valueOf(parser.nextTextValue());
                case "voltageInterval" -> {
                    parser.nextToken();
                    voltageInterval = JsonUtil.readValue(deserializationContext, parser,
                            SingleNominalVoltageCriterion.VoltageInterval.class);
                }
                case "voltageInterval1" -> {
                    parser.nextToken();
                    voltageInterval1 = JsonUtil.readValue(deserializationContext, parser,
                            SingleNominalVoltageCriterion.VoltageInterval.class);
                }
                case "voltageInterval2" -> {
                    parser.nextToken();
                    voltageInterval2 = JsonUtil.readValue(deserializationContext, parser,
                            SingleNominalVoltageCriterion.VoltageInterval.class);
                }
                case "voltageInterval3" -> {
                    parser.nextToken();
                    voltageInterval3 = JsonUtil.readValue(deserializationContext, parser,
                            SingleNominalVoltageCriterion.VoltageInterval.class);
                }
                case "countries" -> {
                    parser.nextToken();
                    countries = JsonUtil.readList(deserializationContext, parser, String.class);
                }
                case "countries1" -> {
                    parser.nextToken();
                    countries1 = JsonUtil.readList(deserializationContext, parser, String.class);
                }
                case "countries2" -> {
                    parser.nextToken();
                    countries2 = JsonUtil.readList(deserializationContext, parser, String.class);
                }
                case "propertyKey" -> propertyKey = parser.nextTextValue();
                case "regex" -> regex = parser.nextTextValue();
                case "propertyValue" -> {
                    parser.nextToken();
                    propertyValues = JsonUtil.readList(deserializationContext, parser, String.class);
                }
                case "equipmentToCheck" ->
                        equipmentToCheck = PropertyCriterion.EquipmentToCheck.valueOf(parser.nextTextValue());
                case "sideToCheck" -> sideToCheck = PropertyCriterion.SideToCheck.valueOf(parser.nextTextValue());
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("type of criterion can not be null");
        }
        return switch (type) {
            case PROPERTY -> new PropertyCriterion(propertyKey, propertyValues, equipmentToCheck, sideToCheck);
            case AT_LEAST_ONE_COUNTRY -> new AtLeastOneCountryCriterion(countries.stream().map(Country::valueOf).toList());
            case SINGLE_COUNTRY -> new SingleCountryCriterion(countries.stream().map(Country::valueOf).toList());
            case TWO_COUNTRY -> new TwoCountriesCriterion(countries1.stream().map(Country::valueOf).toList(),
                    countries2.stream().map(Country::valueOf).toList());
            case AT_LEAST_ONE_NOMINAL_VOLTAGE -> new AtLeastOneNominalVoltageCriterion(voltageInterval);
            case SINGLE_NOMINAL_VOLTAGE -> new SingleNominalVoltageCriterion(voltageInterval);
            case TWO_NOMINAL_VOLTAGE -> new TwoNominalVoltageCriterion(voltageInterval1, voltageInterval2);
            case THREE_NOMINAL_VOLTAGE ->
                    new ThreeNominalVoltageCriterion(voltageInterval1, voltageInterval2, voltageInterval3);
            case REGEX -> new RegexCriterion(regex);
        };
    }
}
