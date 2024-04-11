/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.contingency.list.IdentifierContingencyList;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifierContingencyList;
import com.powsybl.iidm.network.identifiers.IdBasedNetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.VoltageLevelAndOrderNetworkElementIdentifier;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class NetworkElementIdentifierContingencyListJsonTest extends AbstractSerDeTest {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new ContingencyJsonModule());
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    private static IdentifierContingencyList create() {
        List<NetworkElementIdentifier> networkElementIdentifiers = new ArrayList<>();
        networkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("identifier", "contingencyId1"));
        networkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("identifier2"));
        networkElementIdentifiers.add(new VoltageLevelAndOrderNetworkElementIdentifier("vl1",
                "vl2", '1', "contingencyId2"));
        networkElementIdentifiers.add(new NetworkElementIdentifierContingencyList(Collections.singletonList(new
                IdBasedNetworkElementIdentifier("identifier")), "contingencyId3"));
        return new IdentifierContingencyList("list1", networkElementIdentifiers);
    }

    @Test
    void roundTripTest() throws IOException {
        roundTripTest(create(), NetworkElementIdentifierContingencyListJsonTest::write, NetworkElementIdentifierContingencyListJsonTest::readContingencyList,
                "/identifierContingencyList.json");
    }

    @Test
    void readVersion11() {
        ContingencyList contingencyList = NetworkElementIdentifierContingencyListJsonTest
                .readJsonInputStream(Objects.requireNonNull(getClass()
                        .getResourceAsStream("/identifierContingencyListv1_1.json")));
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            WRITER.writeValue(bos, contingencyList);
            ComparisonUtils.compareTxt(getClass().getResourceAsStream("/identifierContingencyListReferenceForLessThan1_2.json"), new ByteArrayInputStream(bos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void readVersion10() {
        ContingencyList contingencyList = NetworkElementIdentifierContingencyListJsonTest
                .readJsonInputStream(Objects.requireNonNull(getClass()
                        .getResourceAsStream("/identifierContingencyListv1_0.json")));
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            WRITER.writeValue(bos, contingencyList);
            ComparisonUtils.compareTxt(getClass().getResourceAsStream("/identifierContingencyListReferenceForLessThan1_2.json"), new ByteArrayInputStream(bos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            fail();
        }
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

    private static IdentifierContingencyList readJsonInputStream(InputStream is) {
        Objects.requireNonNull(is);
        try {
            return MAPPER.readValue(is, IdentifierContingencyList.class);
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
