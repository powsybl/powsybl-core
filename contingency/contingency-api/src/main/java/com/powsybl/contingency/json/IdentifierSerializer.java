/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.contingency.list.identifier.*;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifierList;
import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierSerializer extends StdSerializer<NetworkElementIdentifier> {

    public IdentifierSerializer() {
        super(NetworkElementIdentifier.class);
    }

    @Override
    public void serialize(NetworkElementIdentifier networkElementIdentifier, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", networkElementIdentifier.getType().toString());
        switch (networkElementIdentifier.getType()) {
            case ID_BASED:
                jsonGenerator.writeStringField("identifier", ((IdBasedNetworkElementIdentifier) networkElementIdentifier).getIdentifier());
                break;
            case LIST:
                jsonGenerator.writeObjectField("identifierList", ((NetworkElementIdentifierList) networkElementIdentifier).getIdentifiers());
                break;
            case VOLTAGE_LEVELS_AND_ORDER:
                VoltageLevelAndOrderNetworkElementIdentifier ucteIdentifier = (VoltageLevelAndOrderNetworkElementIdentifier) networkElementIdentifier;
                jsonGenerator.writeStringField("voltageLevelId1", ucteIdentifier.getVoltageLevelId1());
                jsonGenerator.writeStringField("voltageLevelId2", ucteIdentifier.getVoltageLevelId2());
                jsonGenerator.writeStringField("order", Character.toString(ucteIdentifier.getOrder()));
                break;
        }
        jsonGenerator.writeEndObject();
    }
}
