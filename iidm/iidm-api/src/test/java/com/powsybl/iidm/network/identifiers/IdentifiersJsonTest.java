/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.identifiers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.identifiers.json.IdentifierDeserializer;
import com.powsybl.iidm.network.identifiers.json.IdentifierSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class IdentifiersJsonTest extends AbstractSerDeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new SimpleModule()
            .addSerializer(NetworkElementIdentifier.class, new IdentifierSerializer())
            .addDeserializer(NetworkElementIdentifier.class, new IdentifierDeserializer()));

    @Test
    void roundTripIdBasedNetworkElementIdentifier() throws IOException {
        NetworkElementIdentifier identifier = new IdBasedNetworkElementIdentifier("identifier0");
        roundTrip(identifier, "/identifier/idBasedNetworkElementIdentifier.json");
    }

    @Test
    void roundTripVoltageLevelAndOrderNetworkElementIdentifier() throws IOException {
        NetworkElementIdentifier identifier = new VoltageLevelAndOrderNetworkElementIdentifier("vlA", "vlB", '2');
        roundTrip(identifier, "/identifier/voltageLevelAndOrderNetworkElementIdentifier.json");
    }

    @Test
    void roundTripIdWithWildcardsNetworkElementIdentifier() throws IOException {
        NetworkElementIdentifier identifier = new IdWithWildcardsNetworkElementIdentifier("identifier?");
        roundTrip(identifier, "/identifier/idWithWildcardsNetworkElementIdentifierDefault.json");

        identifier = new IdWithWildcardsNetworkElementIdentifier("identifier¤", "¤", null);
        roundTrip(identifier, "/identifier/idWithWildcardsNetworkElementIdentifierCustom.json");
    }

    @Test
    void importIdWithWildcardsNetworkElementIdentifierLegacy() throws IOException {
        NetworkElementIdentifier identifier;
        try (InputStream is = getClass().getResourceAsStream("/identifier/idWithWildcardsNetworkElementIdentifierLegacy.json")) {
            identifier = MAPPER.readValue(is, NetworkElementIdentifier.class);
        }
        if (identifier instanceof IdWithWildcardsNetworkElementIdentifier i) {
            assertEquals("?", i.getWildcardCharacter());
        } else {
            fail();
        }
    }

    private void roundTrip(NetworkElementIdentifier identifier, String referenceFile) throws IOException {
        roundTripTest(identifier, (obj, jsonFile) -> JsonUtil.writeJson(jsonFile, obj, MAPPER),
                jsonFile -> JsonUtil.readJson(jsonFile, NetworkElementIdentifier.class, MAPPER), referenceFile);
    }
}
