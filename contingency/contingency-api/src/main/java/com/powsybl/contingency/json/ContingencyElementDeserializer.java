/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
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
            switch (parser.currentName()) {
                case "id" -> id = parser.nextTextValue();
                case "voltageLevelId" -> voltageLevelId = parser.nextTextValue();
                case "type" -> {
                    parser.nextToken();
                    type = JsonUtil.readValue(ctx, parser, ContingencyElementType.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        if (type != null) {
            return switch (type) {
                case BRANCH -> new BranchContingency(id, voltageLevelId);
                case GENERATOR -> new GeneratorContingency(id);
                case STATIC_VAR_COMPENSATOR -> new StaticVarCompensatorContingency(id);
                case SHUNT_COMPENSATOR -> new ShuntCompensatorContingency(id);
                case HVDC_LINE -> new HvdcLineContingency(id, voltageLevelId);
                case BUSBAR_SECTION -> new BusbarSectionContingency(id);
                case DANGLING_LINE -> new DanglingLineContingency(id);
                case LINE -> new LineContingency(id, voltageLevelId);
                case TWO_WINDINGS_TRANSFORMER -> new TwoWindingsTransformerContingency(id, voltageLevelId);
                case THREE_WINDINGS_TRANSFORMER -> new ThreeWindingsTransformerContingency(id);
                case LOAD -> new LoadContingency(id);
                case SWITCH -> new SwitchContingency(id);
                case BATTERY -> new BatteryContingency(id);
                case BUS -> new BusContingency(id);
                case TIE_LINE -> new TieLineContingency(id, voltageLevelId);
            };
        }

        return null;
    }
}
