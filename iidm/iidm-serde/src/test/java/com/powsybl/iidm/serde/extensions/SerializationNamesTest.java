/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.LoadMockExt;
import com.powsybl.iidm.serde.*;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtensionSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class SerializationNamesTest extends AbstractIidmSerDeTest {

    public static final String LOAD_MOCK = "loadMock";
    public static final String LOAD_ELEMENT_MOCK = "loadElementMock";

    @Test
    void unversionedExtensionTest() {
        // Use the default extension version => also the default serialization name
        AbstractExtensionSerDe<?, ?> serde = new NetworkSourceExtensionSerDe();
        assertEquals(serde.getExtensionName(), serde.getSerializationName(serde.getVersion()));
        Set<String> serializationNames = serde.getSerializationNames();
        assertEquals(1, serializationNames.size());
        assertEquals(serde.getExtensionName(), serializationNames.iterator().next());
    }

    @Test
    void getSerializationNameTest() {
        // Use an extension version which serialization name is not the default
        LoadMockSerDe serde = new LoadMockSerDe();
        assertEquals(LOAD_MOCK, serde.getExtensionName());
        assertEquals(LOAD_MOCK, serde.getSerializationName(serde.getVersion()));
        assertEquals(LOAD_ELEMENT_MOCK, serde.getSerializationName("0.1"));
        Set<String> serializationNames = serde.getSerializationNames();
        assertEquals(3, serializationNames.size());
        assertTrue(serializationNames.containsAll(Set.of(LOAD_MOCK, LOAD_ELEMENT_MOCK)));
    }

    @Test
    void realExtensionNameRoundTripTest() throws IOException {
        // Use the default extension version => also the default serialization name
        allFormatsRoundTripTest(getNetwork(), "/extensionName_1_2.xml", new ExportOptions().setVersion(IidmVersion.V_1_1.toString(".")));
    }

    @Test
    void oldNameRoundTripTest() throws IOException {
        // Use an extension version which serialization name is not the default
        ExportOptions exportOptions = new ExportOptions()
                .setVersion(IidmVersion.V_1_0.toString("."))
                .addExtensionVersion("loadMock", "0.1");
        allFormatsRoundTripTest(getNetwork(), "/extensionName_0_1.xml", exportOptions);
    }

    @Test
    void importXiidmWithAnotherPrefixTest() throws URISyntaxException {
        // Read a network with an old extension serialization name + a non-standard namespace prefix.
        Network network = NetworkSerDe.validateAndRead(Paths.get(
                Objects.requireNonNull(getClass().getResource("/extensionName_0_1_otherPrefix.xml")).toURI()));
        assertNotNull(network.getLoad("Load1").getExtension(LoadMockExt.class));
    }

    @Test
    void indicateExtensionAtImportTest() throws URISyntaxException {
        // Read a network with an old extension serialization name
        // To specify the extension to import, both the real extension name or the serialization name can be used.
        Path file = Paths.get(Objects.requireNonNull(getClass().getResource("/extensionName_0_1_otherPrefix.xml")).toURI());
        ImportOptions importOptions = new ImportOptions().addExtension("loadElementMock");
        Network network = NetworkSerDe.read(file, importOptions);
        assertNotNull(network.getLoad("Load1").getExtension(LoadMockExt.class),
                "Using the serialization name as extension to load should work.");

        importOptions = new ImportOptions().addExtension("loadMock");
        network = NetworkSerDe.read(file, importOptions);
        assertNotNull(network.getLoad("Load1").getExtension(LoadMockExt.class),
                "Using the real extension name as extension to load should work.");
    }

    private static Network getNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2024-09-17T13:36:37.831Z"));
        Substation s1 = network.newSubstation().setId("S1").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(450).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Load load1 = vl1.newLoad().setId("Load1").setNode(0).setP0(10.).setQ0(20.).add();
        load1.addExtension(LoadMockExt.class, new LoadMockExt(load1));
        return network;
    }

    //TODO list:
    // - handle serialization names collision
}
