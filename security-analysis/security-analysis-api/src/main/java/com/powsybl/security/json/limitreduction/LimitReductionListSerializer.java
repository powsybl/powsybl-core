/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitreduction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.limitreduction.LimitReductionList;

import java.io.IOException;

import static com.powsybl.security.limitreduction.LimitReductionList.VERSION;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitReductionListSerializer extends StdSerializer<LimitReductionList> {

    public LimitReductionListSerializer() {
        super(LimitReductionList.class);
    }

    @Override
    public void serialize(LimitReductionList limitReductionList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        serializerProvider.defaultSerializeField("limitReductions", limitReductionList.getLimitReductions(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
