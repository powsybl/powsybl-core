/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.contingency.list.criterion.*;
import com.powsybl.iidm.network.Country;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
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
            case SINGLE_COUNTRY:
                jsonGenerator.writeObjectField("countries", ((SingleCountryCriterion) criterion).getCountries().stream().map(Country::toString).collect(Collectors.toList()));
                break;
            case TWO_COUNTRY:
                jsonGenerator.writeObjectField("countries1", ((TwoCountriesCriterion) criterion).getCountries1().stream().map(Country::toString).collect(Collectors.toList()));
                jsonGenerator.writeObjectField("countries2", ((TwoCountriesCriterion) criterion).getCountries1().stream().map(Country::toString).collect(Collectors.toList()));
                break;
            case SINGLE_NOMINAL_VOLTAGE:
                SingleNominalVoltageCriterion singleNominalVoltageCriterion = (SingleNominalVoltageCriterion) criterion;
                if (!singleNominalVoltageCriterion.getVoltageInterval().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval", singleNominalVoltageCriterion.getVoltageInterval());
                }
                break;
            case TWO_NOMINAL_VOLTAGE:
                TwoNominalVoltageCriterion twoNominalVoltageCriterion = (TwoNominalVoltageCriterion) criterion;
                if (!twoNominalVoltageCriterion.getVoltageInterval1().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval1", ((TwoNominalVoltageCriterion) criterion).getVoltageInterval1());
                }
                if (!twoNominalVoltageCriterion.getVoltageInterval2().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval2", ((TwoNominalVoltageCriterion) criterion).getVoltageInterval2());
                }
                break;
            case THREE_NOMINAL_VOLTAGE:
                ThreeNominalVoltageCriterion threeNominalVoltageCriterion = (ThreeNominalVoltageCriterion) criterion;
                if (!threeNominalVoltageCriterion.getVoltageInterval1().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval1", threeNominalVoltageCriterion.getVoltageInterval1());
                }
                if (!threeNominalVoltageCriterion.getVoltageInterval2().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval2", threeNominalVoltageCriterion.getVoltageInterval2());
                }
                if (!threeNominalVoltageCriterion.getVoltageInterval3().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval3", threeNominalVoltageCriterion.getVoltageInterval3());
                }
                break;
            case PROPERTY:
                jsonGenerator.writeStringField("propertyKey", ((PropertyCriterion) criterion).getPropertyKey());
                jsonGenerator.writeObjectField("propertyValue", ((PropertyCriterion) criterion).getPropertyValues());
                break;
            case REGEX:
                jsonGenerator.writeStringField("regex", ((RegexCriterion) criterion).getRegex());
                break;
            default:
                throw new IllegalArgumentException("type " + criterion.getType().toString() + " not known");
        }
        jsonGenerator.writeEndObject();
    }
}
