/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.AbstractTapChangerRegulationAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerRegulationActionSerializer<T extends AbstractTapChangerRegulationAction> extends StdSerializer<T> {

    protected AbstractTapChangerRegulationActionSerializer(Class<T> vc) {
        super(vc);
    }

    protected void serializeCommonAttributes(AbstractTapChangerRegulationAction action, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("transformerId", action.getTransformerId());
        jsonGenerator.writeBooleanField("regulating", action.isRegulating());
        JsonUtil.writeOptionalEnum(jsonGenerator, "side", action.getSide());
    }
}
