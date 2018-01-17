/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.NetworkMetadata;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class NetworkMetadataSerializer extends StdSerializer<NetworkMetadata> {

    public NetworkMetadataSerializer() {
        super(NetworkMetadata.class);
    }

    @Override
    public void serialize(NetworkMetadata networkMetadata, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("id", networkMetadata.getId());
        jsonGenerator.writeStringField("sourceFormat", networkMetadata.getSourceFormat());
        jsonGenerator.writeStringField("caseDate", networkMetadata.getCaseDate().toString());
        jsonGenerator.writeNumberField("forecastDistance", networkMetadata.getForecastDistance());

        JsonUtil.writeExtensions(networkMetadata, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }
}
