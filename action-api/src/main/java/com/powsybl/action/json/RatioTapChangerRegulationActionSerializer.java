/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.RatioTapChangerRegulationAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class RatioTapChangerRegulationActionSerializer extends AbstractTapChangerRegulationActionSerializer<RatioTapChangerRegulationAction> {

    public RatioTapChangerRegulationActionSerializer() {
        super(RatioTapChangerRegulationAction.class);
    }

    @Override
    public void serialize(RatioTapChangerRegulationAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializeCommonAttributes(action, jsonGenerator);
        JsonUtil.writeOptionalDouble(jsonGenerator, "targetV", action.getTargetV());
        jsonGenerator.writeEndObject();
    }
}
