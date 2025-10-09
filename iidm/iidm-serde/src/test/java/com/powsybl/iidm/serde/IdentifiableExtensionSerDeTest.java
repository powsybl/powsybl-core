/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.LoadZipModel;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import com.powsybl.iidm.network.test.TerminalMockExt;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtension;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtensionImpl;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertXmlEquals;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class IdentifiableExtensionSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        LoadZipModel zipModel = new LoadZipModel(load, 1, 2, 3, 4, 5, 6, 380);
        load.addExtension(LoadZipModel.class, zipModel);
        byte[] buffer;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkSerDe.write(network, new ExportOptions(), os);
            buffer = os.toByteArray();
        }
        // try to validate the schema with extensions
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            NetworkSerDe.validate(is);
        }
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            Network network2 = NetworkSerDe.read(is);
            LoadZipModel zipModel2 = network2.getLoad("LOAD").getExtension(LoadZipModel.class);
            assertNotNull(zipModel2);
            assertTrue(zipModel.getA1() == zipModel2.getA1()
                    && zipModel.getA2() == zipModel2.getA2()
                    && zipModel.getA3() == zipModel2.getA3()
                    && zipModel.getA4() == zipModel2.getA4()
                    && zipModel.getA5() == zipModel2.getA5()
                    && zipModel.getA6() == zipModel2.getA6()
                    && zipModel.getV0() == zipModel2.getV0()
            );
        }
    }

    @Test
    void testMultipleExtensions() throws IOException {
        allFormatsRoundTripTest(MultipleExtensionsTestNetworkFactory.create(), "multiple-extensions.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("multiple-extensions.xml");
    }

    // Define a network extension without XML serializer
    static class NetworkDummyExtension extends AbstractExtension<Network> {
        @Override
        public String getName() {
            return "networkDummy";
        }
    }

    @Test
    void testExtensionWithoutSerializerThrowsException() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        NetworkDummyExtension source = new NetworkDummyExtension();
        network.addExtension(NetworkDummyExtension.class, source);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try {
                NetworkSerDe.write(network, new ExportOptions(), os);
            } catch (PowsyblException x) {
                assertTrue(x.getMessage().contains("Provider not found for extension"));
            }
        }
    }

    @Test
    void testExtensionWithoutSerializerDoNotThrowException() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        NetworkDummyExtension source = new NetworkDummyExtension();
        network.addExtension(NetworkDummyExtension.class, source);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try {
                NetworkSerDe.write(network, new ExportOptions().setThrowExceptionIfExtensionNotFound(false), os);
            } catch (PowsyblException x) {
                assertTrue(x.getMessage().contains("Provider not found for extension"));
            }
        }
    }

    @Test
    void testNetworkSourceExtension() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        String sourceData = "eurostag-tutorial-example1-created-from-IIDM-API";
        NetworkSourceExtension source = new NetworkSourceExtensionImpl(sourceData);
        network.addExtension(NetworkSourceExtension.class, source);
        byte[] buffer;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkSerDe.write(network, new ExportOptions(), os);
            buffer = os.toByteArray();
        }
        // try to validate the schema with extensions
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            NetworkSerDe.validate(is);
        }
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            Network network2 = NetworkSerDe.read(is);
            NetworkSourceExtension source2 = network2.getExtension(NetworkSourceExtension.class);
            assertNotNull(source2);
            assertEquals(sourceData, source2.getSourceData());
        }
    }

    @Test
    void testTerminalExtension() throws IOException {
        Network network2 = allFormatsRoundTripTest(EurostagTutorialExample1Factory.createWithTerminalMockExt(), "eurostag-tutorial-example1-with-terminalMock-ext.xml", CURRENT_IIDM_VERSION);
        Load loadXml = network2.getLoad("LOAD");
        TerminalMockExt terminalMockExtXml = loadXml.getExtension(TerminalMockExt.class);
        assertNotNull(terminalMockExtXml);
        assertSame(loadXml.getTerminal(), terminalMockExtXml.getTerminal());

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("eurostag-tutorial-example1-with-terminalMock-ext.xml");
    }

    @Test
    void testNotLatestVersionTerminalExtension() throws IOException {
        // import XIIDM file with loadMock v1.2
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("eurostag-tutorial-example1-with-loadMockExt-1_2.xml", IidmVersion.V_1_1));

        MemDataSource dataSource = new MemDataSource();

        // properties specify that IIDM-XML network version to export is 1.1
        Properties properties = new Properties();
        properties.put("iidm.export.xml.version", "1.1");

        // XMLExporter here take default test configuration (cf. /resources/com/powsybl/config/test/config.yml)
        // this default test configuration asserts that loadMock should be exported in v1.1
        new XMLExporter().export(network, properties, dataSource);

        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            assertNotNull(is);
            // check that loadMock has been serialized in v1.1
            assertXmlEquals(getVersionedNetworkAsStream("eurostag-tutorial-example1-with-loadMockExt-1_1.xml", IidmVersion.V_1_1),
                    is);
        }
    }

    @Test
    void testThrowErrorIncompatibleExtensionVersion() {
        // should fail while trying to import a file in IIDM-XML network version 1.1 and loadMock in v1.0 (not compatible)
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.read(getVersionedNetworkAsStream("eurostag-tutorial-example1-with-bad-loadMockExt.xml", IidmVersion.V_1_1)));
        assertTrue(e.getMessage().contains("IIDM version of network (1.1)"
                + " is not compatible with the loadMock extension's namespace URI."));
    }

    @Test
    void testThrowErrorUnsupportedExtensionVersion1() {
        // should fail while trying to import a file with loadBar in v1.1 (does not exist, considered as not supported)
        ExportOptions options = new ExportOptions().addExtensionVersion("loadBar", "1.1");
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.write(MultipleExtensionsTestNetworkFactory.create(), options, tmpDir.resolve("throwError")));
        assertTrue(e.getMessage().contains("The version 1.1 of the loadBar extension's XML serializer is not supported."));
    }

    @Test
    void testThrowErrorUnsupportedExtensionVersion2() {
        // should fail while trying to import a file in IIDM-XML network version 1.1 (not supported by loadQux extension)
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.read(getVersionedNetworkAsStream("eurostag-tutorial-example1-with-bad-loadQuxExt.xml", IidmVersion.V_1_1)));
        assertTrue(e.getMessage().contains("IIDM version of network (1.1) is not supported by the loadQux extension's XML serializer."));
    }
}
