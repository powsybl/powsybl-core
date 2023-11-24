/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util.criterion.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.contingency.list.criterion.*;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.util.criterion.*;

import java.io.IOException;
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
            case SINGLE_NOMINAL_VOLTAGE:
                SingleNominalVoltageCriterion singleNominalVoltageCriterion = (SingleNominalVoltageCriterion) criterion;
                if (!singleNominalVoltageCriterion.getVoltageInterval().isNull()) {
                    serializerProvider.defaultSerializeField("voltageInterval",
                            singleNominalVoltageCriterion.getVoltageInterval(),
                            jsonGenerator);
                }
                break;
            case TWO_NOMINAL_VOLTAGE:
                TwoNominalVoltageCriterion twoNominalVoltageCriterion = (TwoNominalVoltageCriterion) criterion;
                if (!twoNominalVoltageCriterion.getVoltageInterval1().isNull()) {
                    serializerProvider.defaultSerializeField("voltageInterval1",
                            ((TwoNominalVoltageCriterion) criterion).getVoltageInterval1(),
                            jsonGenerator);
                }
                if (!twoNominalVoltageCriterion.getVoltageInterval2().isNull()) {
                    serializerProvider.defaultSerializeField("voltageInterval2",
                            ((TwoNominalVoltageCriterion) criterion).getVoltageInterval2(),
                            jsonGenerator);
                }
                break;
            case THREE_NOMINAL_VOLTAGE:
                ThreeNominalVoltageCriterion threeNominalVoltageCriterion = (ThreeNominalVoltageCriterion) criterion;
                if (!threeNominalVoltageCriterion.getVoltageInterval1().isNull()) {
                    serializerProvider.defaultSerializeField("voltageInterval1",
                            threeNominalVoltageCriterion.getVoltageInterval1(),
                            jsonGenerator);
                }
                if (!threeNominalVoltageCriterion.getVoltageInterval2().isNull()) {
                    serializerProvider.defaultSerializeField("voltageInterval2",
                            threeNominalVoltageCriterion.getVoltageInterval2(),
                            jsonGenerator);
                }
                if (!threeNominalVoltageCriterion.getVoltageInterval3().isNull()) {
                    serializerProvider.defaultSerializeField("voltageInterval3",
                            threeNominalVoltageCriterion.getVoltageInterval3(),
                            jsonGenerator);
                }
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
}
