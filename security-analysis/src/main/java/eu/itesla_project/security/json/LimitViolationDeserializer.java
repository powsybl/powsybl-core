/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.security.LimitViolationType;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class LimitViolationDeserializer extends StdDeserializer<LimitViolation> {

    LimitViolationDeserializer() {
        super(LimitViolation.class);
    }

    @Override
    public LimitViolation deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String subjectId = null;
        LimitViolationType limitType = null;
        float limit = Float.NaN;
        String limitName = null;
        float limitReduction = Float.NaN;
        float value = Float.NaN;
        Country country = null;
        float baseVoltage = Float.NaN;

        while (parser.nextToken()  != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "subjectId":
                    subjectId = parser.nextTextValue();
                    break;

                case "limitType":
                    parser.nextToken();
                    limitType = parser.readValueAs(LimitViolationType.class);
                    break;

                case "limit":
                    parser.nextToken();
                    limit = parser.readValueAs(Float.class);
                    break;

                case "limitName":
                    limitName = parser.nextTextValue();
                    break;

                case "limitReduction":
                    parser.nextToken();
                    limitReduction = parser.readValueAs(Float.class);
                    break;

                case "value":
                    parser.nextToken();
                    value = parser.readValueAs(Float.class);
                    break;

                case "country":
                    parser.nextToken();
                    country = parser.readValueAs(Country.class);
                    break;

                case "baseVoltage":
                    parser.nextToken();
                    baseVoltage = parser.readValueAs(Float.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new LimitViolation(subjectId, limitType, limit, limitName, limitReduction, value, country, baseVoltage);


    }
}
