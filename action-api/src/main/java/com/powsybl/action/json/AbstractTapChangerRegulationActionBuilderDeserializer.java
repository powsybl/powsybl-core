/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.AbstractTapChangerRegulationActionBuilder;
import com.powsybl.iidm.network.ThreeSides;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerRegulationActionBuilderDeserializer<T extends AbstractTapChangerRegulationActionBuilder> extends StdDeserializer<T> {

    protected AbstractTapChangerRegulationActionBuilderDeserializer(Class<T> vc) {
        super(vc);
    }

    protected boolean deserializeCommonAttributes(JsonParser jsonParser, T builder, String name) throws JacksonException {
        switch (name) {
            case "id":
                builder.withId(jsonParser.nextStringValue());
                return true;
            case "transformerId":
                builder.withTransformerId(jsonParser.nextStringValue());
                return true;
            case "side":
                builder.withSide(ThreeSides.valueOf(jsonParser.nextStringValue()));
                return true;
            case "regulating":
                jsonParser.nextToken();
                builder.withRegulating(jsonParser.getValueAsBoolean());
                return true;
            default:
                return false;
        }
    }
}
