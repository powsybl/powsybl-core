/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.timeseries.TimeSeriesMetadata;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesMetadataJsonSerializer extends StdSerializer<TimeSeriesMetadata> {

    public TimeSeriesMetadataJsonSerializer() {
        super(TimeSeriesMetadata.class);
    }

    @Override
    public void serialize(TimeSeriesMetadata metadata, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        metadata.writeJson(jsonGenerator);
    }
}
