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
import com.powsybl.action.AbstractTapChangerTapPositionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerTapPositionActionSerializer<T extends AbstractTapChangerTapPositionAction> extends StdSerializer<T> {

    protected AbstractTapChangerTapPositionActionSerializer(Class<T> vc) {
        super(vc);
    }

    protected void serializeCommonAttributes(AbstractTapChangerTapPositionAction action, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("transformerId", action.getTransformerId());
        jsonGenerator.writeNumberField("tapPosition", action.getTapPosition());
        jsonGenerator.writeBooleanField("relativeValue", action.isRelativeValue());
        JsonUtil.writeOptionalEnum(jsonGenerator, "side", action.getSide());
    }
}
