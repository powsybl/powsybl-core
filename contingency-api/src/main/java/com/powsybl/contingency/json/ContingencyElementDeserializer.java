/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.contingency.*;

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
        String voltageLevelId = null;
        ContingencyElementType type = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "id":
                    id = parser.nextTextValue();
                    break;

                case "voltageLevelId":
                    voltageLevelId = parser.nextTextValue();
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
                case BRANCH:
                    return new BranchContingency(id, voltageLevelId);

                case GENERATOR:
                    return new GeneratorContingency(id);

                case HVDC_LINE:
                    return new HvdcLineContingency(id, voltageLevelId);

                case BUSBAR_SECTION:
                    return new BusbarSectionContingency(id);

                default:
                    throw new AssertionError("Unexpected ContingencyElementType value: " + type);
            }
        }

        return null;
    }
}
