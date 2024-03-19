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
import com.powsybl.action.PhaseTapChangerRegulationAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class PhaseTapChangerRegulationActionSerializer extends AbstractTapChangerRegulationActionSerializer<PhaseTapChangerRegulationAction> {

    public PhaseTapChangerRegulationActionSerializer() {
        super(PhaseTapChangerRegulationAction.class);
    }

    @Override
    public void serialize(PhaseTapChangerRegulationAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializeCommonAttributes(action, jsonGenerator);
        JsonUtil.writeOptionalEnum(jsonGenerator, "regulationMode", action.getRegulationMode());
        JsonUtil.writeOptionalDouble(jsonGenerator, "regulationValue", action.getRegulationValue());
        jsonGenerator.writeEndObject();
    }
}
