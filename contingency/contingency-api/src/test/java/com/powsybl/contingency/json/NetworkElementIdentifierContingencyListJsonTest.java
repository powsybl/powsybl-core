/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.IdentifierContingencyList;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifierList;
import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.VoltageLevelAndOrderNetworkElementIdentifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
class NetworkElementIdentifierContingencyListJsonTest extends AbstractConverterTest {

    private static IdentifierContingencyList create() {
        List<NetworkElementIdentifier> networkElementIdentifiers = new ArrayList<>();
        networkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("identifier"));
        networkElementIdentifiers.add(new VoltageLevelAndOrderNetworkElementIdentifier("vl1", "vl2", '1'));
        networkElementIdentifiers.add(new NetworkElementIdentifierList(Collections.singletonList(new IdBasedNetworkElementIdentifier("identifier1"))));
        return new IdentifierContingencyList("list1", "LINE", networkElementIdentifiers);
    }

    @Test
    void roundTripTest() throws IOException {
        roundTripTest(create(), NetworkElementIdentifierContingencyListJsonTest::write, NetworkElementIdentifierContingencyListJsonTest::readContingencyList,
                "/identifierContingencyList.json");
    }

    private static IdentifierContingencyList readContingencyList(Path jsonFile) {
        return read(jsonFile, IdentifierContingencyList.class);
    }

    private static <T> T read(Path jsonFile, Class<T> clazz) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            objectMapper.registerModule(module);

            return (T) objectMapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> void write(T object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            ObjectMapper mapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            mapper.registerModule(module);

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
