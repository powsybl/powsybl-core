/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.io.IOException;

/**
 * @author Olivier Bretteville <olivier.bretteville at rte-france.com>
 */
class ApogeeLimitViolationDeserializer extends StdDeserializer<LimitViolation> {

    ApogeeLimitViolationDeserializer() {
        super(LimitViolation.class);
    }

    @Override
    public LimitViolation deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String subjectId = null;
        LimitViolationType limitType = null;
        String limitName = null;
        float limit = Float.NaN;
        float limitReduction = Float.NaN;
        float value = Float.NaN;
        float valueMW = Float.NaN;
        Branch.Side side = null;
        float valueBefore = Float.NaN;
        float valueBeforeMW = Float.NaN;
        int acceptableDuration = Integer.MAX_VALUE;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "subjectId":
                    subjectId = parser.nextTextValue();
                    break;

                case "limitType":
                    parser.nextToken();
                    limitType = parser.readValueAs(LimitViolationType.class);
                    break;

                case "limitName":
                    limitName = parser.nextTextValue();
                    break;

                case "limit":
                    parser.nextToken();
                    limit = parser.readValueAs(Float.class);
                    break;

                case "limitReduction":
                    parser.nextToken();
                    limitReduction = parser.readValueAs(Float.class);
                    break;

                case "value":
                    parser.nextToken();
                    value = parser.readValueAs(Float.class);
                    break;

                case "valueMW":
                    parser.nextToken();
                    valueMW = parser.readValueAs(Float.class);
                    break;

                case "isOrigin":
                    parser.nextToken();
                    boolean isOrigin = parser.readValueAs(Boolean.class);
                    if (isOrigin) {
                        side = Branch.Side.ONE;
                    } else {
                        side = Branch.Side.TWO;
                    }
                    break;

                case "valueBefore":
                    parser.nextToken();
                    valueBefore = parser.readValueAs(Float.class);
                    break;

                case "valueBeforeMW":
                    parser.nextToken();
                    valueBeforeMW = parser.readValueAs(Float.class);
                    break;

                case "limitDuration":
                    parser.nextToken();
                    acceptableDuration = parser.readValueAs(Integer.class);
                    break;

                case "country":
                case "countryOr":
                case "countryEx":
                case "region":
                case "regionOr":
                case "regionEx":
                case "substation":
                case "substationOr":
                case "substationEx":
                case "baseVoltage":
                case "baseVoltageOr":
                case "baseVoltageEx":
                    // Attributes of the network -> not read
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new LimitViolation(subjectId, limitType, limitName, limit, limitReduction, value, valueMW, side, valueBefore, valueBeforeMW, acceptableDuration);
    }
}
