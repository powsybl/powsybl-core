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
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class ConnectivityResultSerializer extends StdSerializer<ConnectivityResult> {

    protected ConnectivityResultSerializer() {
        super(ConnectivityResult.class);
    }

    @Override
    public void serialize(ConnectivityResult connectivityResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("createdSynchronousComponentCount", connectivityResult.getCreatedSynchronousComponentCount());
        jsonGenerator.writeObjectField("createdConnectedComponentCount", connectivityResult.getCreatedConnectedComponentCount());
        jsonGenerator.writeObjectField("disconnectedLoadActivePower", connectivityResult.getDisconnectedLoadActivePower());
        jsonGenerator.writeObjectField("disconnectedGenerationActivePower", connectivityResult.getDisconnectedGenerationActivePower());
        jsonGenerator.writeObjectField("lostElements", connectivityResult.getLostElements());
        jsonGenerator.writeEndObject();

    }
}
