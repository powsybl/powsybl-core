/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.itesla_project.contingency.BranchContingency;
import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.contingency.ContingencyElementType;
import eu.itesla_project.contingency.GeneratorContingency;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ContingencyElementDeserializer extends StdDeserializer<ContingencyElement> {

    public ContingencyElementDeserializer() {
        super(ContingencyElement.class);
    }

    @Override
    public ContingencyElement deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String id = null;
        String substationId = null;
        ContingencyElementType type = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "id":
                    id = parser.nextTextValue();
                    break;

                case "substationId":
                    substationId = parser.nextTextValue();
                    break;

                case "type":
                    parser.nextToken();
                    type = parser.readValueAs(ContingencyElementType.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        if (type != null) {
            switch (type) {
                case LINE:
                case BRANCH:
                    return new BranchContingency(id, substationId);

                case GENERATOR:
                    return new GeneratorContingency(id);

                default:
                    throw new AssertionError();
            }
        }

        return null;
    }
}
