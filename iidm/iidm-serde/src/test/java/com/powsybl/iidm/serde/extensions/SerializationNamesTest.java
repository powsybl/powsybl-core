/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.extensions.*;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.LoadFooExt;
import com.powsybl.iidm.network.test.LoadMockExt;
import com.powsybl.iidm.serde.*;
import com.powsybl.iidm.serde.extensions.util.ExtensionsSupplier;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtensionSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
        assertTrue(serializationNames.contains("networkSource"));
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
        assertTrue(serializationNames.containsAll(Set.of(LOAD_MOCK, LOAD_ELEMENT_MOCK, "loadEltMock")));
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
    void importXiidmWithAnotherPrefixTest() throws IOException {
        // Read a network with an old extension serialization name + a non-standard namespace prefix.
        String file = "/extensionName_0_1_otherPrefix.xml";
        try (InputStream is = getClass().getResourceAsStream(file)) {
            NetworkSerDe.validate(is);
        }
        Network network = NetworkSerDe.read(getClass().getResourceAsStream(file));
        assertNotNull(network.getLoad("Load1").getExtension(LoadMockExt.class));
    }

    @Test
    void indicateExtensionAtImportTest() {
        // Read a network with an old extension serialization name
        // To specify the extension to import, both the real extension name or the serialization name can be used.
        String file = "/extensionName_0_1_otherPrefix.xml";
        ImportOptions importOptions = new ImportOptions().addExtension("loadElementMock");
        Network network = NetworkSerDe.read(getClass().getResourceAsStream(file), importOptions, null);
        assertNotNull(network.getLoad("Load1").getExtension(LoadMockExt.class),
                "Using the serialization name as extension to load should work.");

        importOptions = new ImportOptions().addExtension("loadMock");
        network = NetworkSerDe.read(getClass().getResourceAsStream(file), importOptions, null);
        assertNotNull(network.getLoad("Load1").getExtension(LoadMockExt.class),
                "Using the real extension name as extension to load should work.");
    }

    @Test
    void ignoreOtherSerializationNameIfAlreadyUsed() {
        // No extension serde available
        // This assertion is used to test that the configuration mechanism is working
        Network network = loadNetworkWithGivenSerdes(List.of());
        assertTrue(network.getLoad("Load1").getExtensions().isEmpty(), "No extension should be loaded.");

        // The extension in the network ("loadElementMock") is only associated to the LoadMockExt extension,
        // and it is defined as an "other serialization name"
        List<ExtensionSerDe<?, ?>> serdes = List.of(new LoadMockSerDe());
        network = loadNetworkWithGivenSerdes(serdes);
        Load load1 = network.getLoad("Load1");
        assertNotNull(load1.getExtension(LoadMockExt.class), "The read extension should be a LoadMockExt.");
        assertEquals(1, load1.getExtensions().size(), "Only 1 extension should be loaded.");

        // The extension in the network corresponds to the real extension name of the dummy extension SerDe
        // and to an "other serialization name" of the LoadMockExt extension.
        // In this case, the dummy extension should have the precedence (real names before "other" names)
        // Note that the dummy SerDe loads a "LoadFooExt".
        serdes = List.of(new LoadMockSerDe(), createDummyExtensionSerDe());
        network = loadNetworkWithGivenSerdes(serdes);
        load1 = network.getLoad("Load1");
        assertNull(load1.getExtension(LoadMockExt.class), "No LoadMockExt should be loaded.");
        assertNotNull(load1.getExtension(LoadFooExt.class), "A LoadFooExt extension should be loaded.");
    }

    private Network loadNetworkWithGivenSerdes(List<ExtensionSerDe<?, ?>> serdes) {
        ExtensionProvidersLoader loader = new CustomExtensionProvidersLoader(serdes);
        ExtensionsSupplier extensionsSupplier = () -> ExtensionProviders.createProvider(ExtensionSerDe.class, "network", loader);

        return NetworkSerDe.read(getClass().getResourceAsStream("/extensionName_0_1_otherPrefix.xml"),
                new ImportOptions(), null, NetworkFactory.findDefault(), extensionsSupplier, ReportNode.NO_OP);
    }

    private static class CustomExtensionProvidersLoader extends DefaultExtensionProvidersLoader {
        private final List<ExtensionSerDe<?, ?>> serdes;

        public CustomExtensionProvidersLoader(List<ExtensionSerDe<?, ?>> serdes) {
            this.serdes = serdes;
        }

        @Override
        public <T extends ExtensionProvider> Stream<T> getServicesStream(Class<T> clazz) {
            return clazz == ExtensionSerDe.class ? serdes.stream().map(clazz::cast) :
                    new ServiceLoaderCache<>(clazz).getServices().stream();
        }
    }

    private static ExtensionSerDe<Load, LoadFooExt> createDummyExtensionSerDe() {
        return new AbstractExtensionSerDe<>("loadElementMock", "network", LoadFooExt.class,
                "", "", "") {
            @Override
            public void write(LoadFooExt extension, SerializerContext context) {
                // Do nothing
            }

            @Override
            public LoadFooExt read(Load extendable, DeserializerContext context) {
                LoadFooExt extension = new LoadFooExt(extendable);
                extendable.addExtension(LoadFooExt.class, extension);
                return extension;
            }
        };
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
}
