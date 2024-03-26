/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.iidm.criteria.*;
import com.powsybl.iidm.network.Country;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class CriterionSerializer extends StdSerializer<Criterion> {

    public CriterionSerializer() {
        super(Criterion.class);
    }

    @Override
    public void serialize(Criterion criterion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", criterion.getType().toString());
        switch (criterion.getType()) {
            case AT_LEAST_ONE_COUNTRY:
                serializerProvider.defaultSerializeField("countries",
                        ((AtLeastOneCountryCriterion) criterion).getCountries().stream().map(Country::toString).collect(Collectors.toList()),
                        jsonGenerator);
                break;
            case SINGLE_COUNTRY:
                serializerProvider.defaultSerializeField("countries",
                        ((SingleCountryCriterion) criterion).getCountries().stream().map(Country::toString).collect(Collectors.toList()),
                        jsonGenerator);
                break;
            case TWO_COUNTRY:
                serializerProvider.defaultSerializeField("countries1",
                        ((TwoCountriesCriterion) criterion).getCountries1().stream().map(Country::toString).collect(Collectors.toList()),
                        jsonGenerator);
                serializerProvider.defaultSerializeField("countries2",
                        ((TwoCountriesCriterion) criterion).getCountries2().stream().map(Country::toString).collect(Collectors.toList()),
                        jsonGenerator);
                break;
            case AT_LEAST_ONE_NOMINAL_VOLTAGE:
                AtLeastOneNominalVoltageCriterion atLeastOneNominalVoltageCriterion = (AtLeastOneNominalVoltageCriterion) criterion;
                serializerProvider.defaultSerializeField("voltageInterval", atLeastOneNominalVoltageCriterion.getVoltageInterval(), jsonGenerator);
                break;
            case SINGLE_NOMINAL_VOLTAGE:
                SingleNominalVoltageCriterion singleNominalVoltageCriterion = (SingleNominalVoltageCriterion) criterion;
                serializerProvider.defaultSerializeField("voltageInterval", singleNominalVoltageCriterion.getVoltageInterval(), jsonGenerator);
                break;
            case TWO_NOMINAL_VOLTAGE:
                serializeTwoWindingsVoltageCriterion((TwoNominalVoltageCriterion) criterion, jsonGenerator, serializerProvider);
                break;
            case THREE_NOMINAL_VOLTAGE:
                serializeThreeNominalVoltageCriterion((ThreeNominalVoltageCriterion) criterion, jsonGenerator, serializerProvider);
                break;
            case PROPERTY:
                jsonGenerator.writeStringField("propertyKey", ((PropertyCriterion) criterion).getPropertyKey());
                serializerProvider.defaultSerializeField("propertyValue",
                        ((PropertyCriterion) criterion).getPropertyValues(),
                        jsonGenerator);
                jsonGenerator.writeStringField("equipmentToCheck", ((PropertyCriterion) criterion).getEquipmentToCheck().toString());
                if (((PropertyCriterion) criterion).getSideToCheck() != null) {
                    jsonGenerator.writeStringField("sideToCheck", ((PropertyCriterion) criterion).getSideToCheck().toString());
                }
                break;
            case REGEX:
                jsonGenerator.writeStringField("regex", ((RegexCriterion) criterion).getRegex());
                break;
            default:
                throw new IllegalArgumentException("type " + criterion.getType().toString() + " not known");
        }
        jsonGenerator.writeEndObject();
    }

    private static void serializeTwoWindingsVoltageCriterion(TwoNominalVoltageCriterion twoNominalVoltageCriterion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Optional<SingleNominalVoltageCriterion.VoltageInterval> optInterval = twoNominalVoltageCriterion.getVoltageInterval1();
        if (optInterval.isPresent()) {
            serializerProvider.defaultSerializeField("voltageInterval1", optInterval.get(), jsonGenerator);
        }
        optInterval = twoNominalVoltageCriterion.getVoltageInterval2();
        if (optInterval.isPresent()) {
            serializerProvider.defaultSerializeField("voltageInterval2", optInterval.get(), jsonGenerator);
        }
    }

    private static void serializeThreeNominalVoltageCriterion(ThreeNominalVoltageCriterion threeNominalVoltageCriterion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Optional<SingleNominalVoltageCriterion.VoltageInterval> optInterval = threeNominalVoltageCriterion.getVoltageInterval1();
        if (optInterval.isPresent()) {
            serializerProvider.defaultSerializeField("voltageInterval1", optInterval.get(), jsonGenerator);
        }
        optInterval = threeNominalVoltageCriterion.getVoltageInterval2();
        if (optInterval.isPresent()) {
            serializerProvider.defaultSerializeField("voltageInterval2", optInterval.get(), jsonGenerator);
        }
        optInterval = threeNominalVoltageCriterion.getVoltageInterval3();
        if (optInterval.isPresent()) {
            serializerProvider.defaultSerializeField("voltageInterval3", optInterval.get(), jsonGenerator);
        }
    }
}
