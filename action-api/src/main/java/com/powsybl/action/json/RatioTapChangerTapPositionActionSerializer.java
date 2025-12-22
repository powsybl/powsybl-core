/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.RatioTapChangerTapPositionAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class RatioTapChangerTapPositionActionSerializer extends AbstractTapChangerTapPositionActionSerializer<RatioTapChangerTapPositionAction> {
    public RatioTapChangerTapPositionActionSerializer() {
        super(RatioTapChangerTapPositionAction.class);
    }

    @Override
    public void serialize(RatioTapChangerTapPositionAction action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        serializeCommonAttributes(action, jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
