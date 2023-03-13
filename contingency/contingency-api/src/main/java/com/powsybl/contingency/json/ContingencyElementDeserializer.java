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
import com.powsybl.commons.json.JsonUtil;
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
                    type = JsonUtil.readValue(ctx, parser, ContingencyElementType.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }

        if (type != null) {
            switch (type) {
                case BRANCH:
                    return new BranchContingency(id, voltageLevelId);

                case GENERATOR:
                    return new GeneratorContingency(id);

                case STATIC_VAR_COMPENSATOR:
                    return new StaticVarCompensatorContingency(id);

                case SHUNT_COMPENSATOR:
                    return new ShuntCompensatorContingency(id);

                case HVDC_LINE:
                    return new HvdcLineContingency(id, voltageLevelId);

                case BUSBAR_SECTION:
                    return new BusbarSectionContingency(id);

                case DANGLING_LINE:
                    return new DanglingLineContingency(id);

                case LINE:
                    return new LineContingency(id, voltageLevelId);

                case TWO_WINDINGS_TRANSFORMER:
                    return new TwoWindingsTransformerContingency(id, voltageLevelId);

                case THREE_WINDINGS_TRANSFORMER:
                    return new ThreeWindingsTransformerContingency(id);

                case LOAD:
                    return new LoadContingency(id);

                case BUS:
                    return new BusContingency(id);

                default:
                    throw new IllegalStateException("Unexpected ContingencyElementType value: " + type);
            }
        }

        return null;
    }
}
