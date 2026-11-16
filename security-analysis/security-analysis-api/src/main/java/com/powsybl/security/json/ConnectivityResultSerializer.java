/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.results.ConnectivityResult;

import java.io.IOException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConnectivityResultSerializer extends StdSerializer<ConnectivityResult> {

    public ConnectivityResultSerializer() {
        super(ConnectivityResult.class);
    }

    @Override
    public void serialize(ConnectivityResult connectivityResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        serializerProvider.defaultSerializeField("createdSynchronousComponentCount", connectivityResult.getCreatedSynchronousComponentCount(), jsonGenerator);
        serializerProvider.defaultSerializeField("createdConnectedComponentCount", connectivityResult.getCreatedConnectedComponentCount(), jsonGenerator);
        serializerProvider.defaultSerializeField("disconnectedLoadActivePower", connectivityResult.getDisconnectedLoadActivePower(), jsonGenerator);
        serializerProvider.defaultSerializeField("disconnectedGenerationActivePower", connectivityResult.getDisconnectedGenerationActivePower(), jsonGenerator);
        serializerProvider.defaultSerializeField("disconnectedElements", connectivityResult.getDisconnectedElements(), jsonGenerator);
        jsonGenerator.writeEndObject();

    }
}
