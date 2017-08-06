/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.security.LimitViolationsResult;
import eu.itesla_project.security.PostContingencyResult;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class PostContingencyResultDeserializer extends StdDeserializer<PostContingencyResult> {

    PostContingencyResultDeserializer() {
        super(PostContingencyResult.class);
    }

    @Override
    public PostContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Contingency contingency = null;
        LimitViolationsResult limitViolationsResult = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "contingency":
                    parser.nextToken();
                    contingency = parser.readValueAs(Contingency.class);
                    break;

                case "limitViolationsResult":
                    parser.nextToken();
                    limitViolationsResult = parser.readValueAs(LimitViolationsResult.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new PostContingencyResult(contingency, limitViolationsResult);
    }
}
