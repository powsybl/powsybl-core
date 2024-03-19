/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.action.PhaseTapChangerTapPositionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PhaseTapChangerTapPositionActionSerializer extends AbstractTapChangerTapPositionActionSerializer<PhaseTapChangerTapPositionAction> {
    public PhaseTapChangerTapPositionActionSerializer() {
        super(PhaseTapChangerTapPositionAction.class);
    }

    @Override
    public void serialize(PhaseTapChangerTapPositionAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializeCommonAttributes(action, jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
