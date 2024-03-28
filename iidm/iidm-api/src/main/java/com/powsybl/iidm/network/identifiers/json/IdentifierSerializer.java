/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.identifiers.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifierContingencyList;
import com.powsybl.iidm.network.identifiers.IdBasedNetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.VoltageLevelAndOrderNetworkElementIdentifier;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierSerializer extends StdSerializer<NetworkElementIdentifier> {

    public IdentifierSerializer() {
        super(NetworkElementIdentifier.class);
    }

    @Override
    public void serialize(NetworkElementIdentifier networkElementIdentifier, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", networkElementIdentifier.getType().toString());
        Optional<String> optionalContingencyId = networkElementIdentifier.getContingencyId();
        if (optionalContingencyId.isPresent()) {
            jsonGenerator.writeStringField("contingencyId", optionalContingencyId.get());
        }
        switch (networkElementIdentifier.getType()) {
            case ID_BASED:
                jsonGenerator.writeStringField("identifier", ((IdBasedNetworkElementIdentifier) networkElementIdentifier).getIdentifier());
                break;
            case LIST:
                serializerProvider.defaultSerializeField("identifierList",
                        ((NetworkElementIdentifierContingencyList) networkElementIdentifier).getNetworkElementIdentifiers(),
                        jsonGenerator);
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
