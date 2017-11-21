/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.math.timeseries.ArrayChunk;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ArrayChunkJsonSerializer extends StdSerializer<ArrayChunk> {

    public ArrayChunkJsonSerializer() {
        super(ArrayChunk.class);
    }

    @Override
    public void serialize(ArrayChunk chunk, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        chunk.writeJson(jsonGenerator);
    }
}
