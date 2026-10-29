/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.results.ConnectivityResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConnectivityResultSerializer extends StdSerializer<ConnectivityResult> {

    public ConnectivityResultSerializer() {
        super(ConnectivityResult.class);
    }

    @Override
    public void serialize(ConnectivityResult connectivityResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {

        jsonGenerator.writeStartObject();
        serializationContext.defaultSerializeProperty("createdSynchronousComponentCount", connectivityResult.getCreatedSynchronousComponentCount(), jsonGenerator);
        serializationContext.defaultSerializeProperty("createdConnectedComponentCount", connectivityResult.getCreatedConnectedComponentCount(), jsonGenerator);
        serializationContext.defaultSerializeProperty("disconnectedLoadActivePower", connectivityResult.getDisconnectedLoadActivePower(), jsonGenerator);
        serializationContext.defaultSerializeProperty("disconnectedGenerationActivePower", connectivityResult.getDisconnectedGenerationActivePower(), jsonGenerator);
        serializationContext.defaultSerializeProperty("disconnectedElements", connectivityResult.getDisconnectedElements(), jsonGenerator);
        jsonGenerator.writeEndObject();

    }
}
