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
import com.powsybl.contingency.contingency.list.criterion.CountryCriterion;
import com.powsybl.contingency.contingency.list.criterion.Criterion;
import com.powsybl.contingency.contingency.list.criterion.NominalVoltageCriterion;
import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;

import java.io.IOException;

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
            case COUNTRY:
                CountryCriterion countryCriterion = (CountryCriterion) criterion;
                if (countryCriterion.getCountry() != null) {
                    jsonGenerator.writeStringField("country", countryCriterion.getCountry().toString());
                }
                if (countryCriterion.getCountry1() != null) {
                    jsonGenerator.writeStringField("country1", countryCriterion.getCountry1().toString());
                }
                if (countryCriterion.getCountry2() != null) {
                    jsonGenerator.writeStringField("country2", countryCriterion.getCountry2().toString());
                }
                break;
            case PROPERTY:
                jsonGenerator.writeStringField("propertyKey", ((PropertyCriterion) criterion).getPropertyKey());
                jsonGenerator.writeStringField("propertyValue", ((PropertyCriterion) criterion).getPropertyValue());
                break;
            case NOMINAL_VOLTAGE:
                NominalVoltageCriterion nominalVoltageCriterion = (NominalVoltageCriterion) criterion;
                if (!nominalVoltageCriterion.getVoltageInterval().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval", ((NominalVoltageCriterion) criterion).getVoltageInterval());
                }
                if (!nominalVoltageCriterion.getVoltageInterval1().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval1", ((NominalVoltageCriterion) criterion).getVoltageInterval1());
                }
                if (!nominalVoltageCriterion.getVoltageInterval2().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval2", ((NominalVoltageCriterion) criterion).getVoltageInterval2());
                }
                if (!nominalVoltageCriterion.getVoltageInterval3().isNull()) {
                    jsonGenerator.writeObjectField("voltageInterval3", ((NominalVoltageCriterion) criterion).getVoltageInterval3());
                }
                break;
        }
        jsonGenerator.writeEndObject();
    }
}
