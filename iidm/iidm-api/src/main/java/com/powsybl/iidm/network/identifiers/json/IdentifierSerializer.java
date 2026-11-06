/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.identifiers.json;

import com.powsybl.iidm.network.identifiers.*;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierSerializer extends StdSerializer<NetworkElementIdentifier> {

    public IdentifierSerializer() {
        super(NetworkElementIdentifier.class);
    }

    @Override
    public void serialize(NetworkElementIdentifier networkElementIdentifier, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", networkElementIdentifier.getType().toString());
        Optional<String> optionalContingencyId = networkElementIdentifier.getContingencyId();
        if (optionalContingencyId.isPresent()) {
            jsonGenerator.writeStringProperty("contingencyId", optionalContingencyId.get());
        }
        switch (networkElementIdentifier.getType()) {
            case ID_BASED:
                jsonGenerator.writeStringProperty("identifier", ((IdBasedNetworkElementIdentifier) networkElementIdentifier).getIdentifier());
                break;
            case LIST:
                serializationContext.defaultSerializeProperty("identifierList",
                    ((NetworkElementIdentifierContingencyList) networkElementIdentifier).getNetworkElementIdentifiers(),
                    jsonGenerator);
                break;
            case VOLTAGE_LEVELS_AND_ORDER:
                VoltageLevelAndOrderNetworkElementIdentifier ucteIdentifier = (VoltageLevelAndOrderNetworkElementIdentifier) networkElementIdentifier;
                jsonGenerator.writeStringProperty("voltageLevelId1", ucteIdentifier.getVoltageLevelId1());
                jsonGenerator.writeStringProperty("voltageLevelId2", ucteIdentifier.getVoltageLevelId2());
                jsonGenerator.writeStringProperty("order", Character.toString(ucteIdentifier.getOrder()));
                break;
            case ID_WITH_WILDCARDS:
                IdWithWildcardsNetworkElementIdentifier identifier = (IdWithWildcardsNetworkElementIdentifier) networkElementIdentifier;
                jsonGenerator.writeStringProperty("identifier", identifier.getIdentifier());
                jsonGenerator.writeStringProperty("wildcard", identifier.getWildcardCharacter());
                break;
            case SUBSTATION_OR_VOLTAGE_LEVEL_EQUIPMENTS:
                SubstationOrVoltageLevelEquipmentsIdentifier substIdentifier = (SubstationOrVoltageLevelEquipmentsIdentifier) networkElementIdentifier;
                jsonGenerator.writeStringProperty("substationOrVoltageLevelId", substIdentifier.getSubstationOrVoltageLevelId());
                serializationContext.defaultSerializeProperty("voltageLevelIdentifiableTypes", substIdentifier.getVoltageLevelIdentifiableTypes(), jsonGenerator);
                break;
        }
        jsonGenerator.writeEndObject();
    }
}
