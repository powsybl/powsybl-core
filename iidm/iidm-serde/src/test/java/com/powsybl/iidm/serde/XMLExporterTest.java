/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.function.Supplier;

import static com.powsybl.commons.test.ComparisonUtils.assertXmlEquals;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Chamseddine BENHAMED  {@literal <chamseddine.benhamed at rte-france.com>}
 */

class XMLExporterTest extends AbstractIidmSerDeTest {
    private FileSystem fileSystem;

    void exporterTest(Network network, IidmVersion version, String xmlFileName, Properties properties) throws IOException {
        exporterTest(network, () -> getVersionedNetworkAsStream(xmlFileName, version), properties);
    }

    void exporterTest(Network network, Supplier<InputStream> refFileIs, Properties properties) throws IOException {
        properties.put(XMLExporter.ANONYMISED, "false");
        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, properties, dataSource);
        // check the exported file and compare it to iidm reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            assertXmlEquals(refFileIs.get(), is);
        }
    }

    @Test
    void exportTest() throws IOException {
        exporterTest(MultipleExtensionsTestNetworkFactory.create(), CURRENT_IIDM_VERSION, "multiple-extensions.xml", new Properties());
    }

    @Test
    void exportWithNamespacePrefixCollisionTest() throws IOException {
        Network network = MultipleExtensionsTestNetworkFactory.create();
        VoltageLevel vl = network.getVoltageLevel("VL");
        vl.addExtension(VoltageLevelFooExt.class, new VoltageLevelFooExt(vl));
        exporterTest(network, () -> getClass().getResourceAsStream("/namespace-prefix-collision.xml"), new Properties());
    }

    @Test
    void exportXiidmWithAnotherPrefixTest() throws IOException {
        Network network = NetworkFactory.findDefault().createNetwork("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2024-09-17T13:36:37.831Z"));
        Substation s1 = network.newSubstation().setId("S1").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(450).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Load load1 = vl1.newLoad().setId("Load1").setNode(0).setP0(10.).setQ0(20.).add();
        load1.addExtension(LoadMockExt.class, new LoadMockExt(load1));
        Properties properties = new Properties();
        properties.put(XMLExporter.VERSION, IidmVersion.V_1_0.toString("."));
        properties.put("iidm.export.xml.loadMock.version", "0.2");
        exporterTest(network, () -> getClass().getResourceAsStream("/extensionName_0_2.xml"), properties);
    }

    @Test
    void paramsTest() {
        var xmlExporter = new XMLExporter();
        assertEquals(11, xmlExporter.getParameters().size());
        assertEquals("IIDM XML v" + CURRENT_IIDM_VERSION.toString(".") + " exporter", xmlExporter.getComment());
    }

    @Test
    void exportOldVersionWithoutNewExtensions() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path workingDir = fileSystem.getPath("/working-dir");
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));
        String voltageLevelId = "VLHV2";
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        assertNotNull(vl);
        String busId = "NHV2";
        Bus bus = vl.getBusBreakerView().getBus(busId);
        assertNotNull(bus);
        Terminal t = bus.getConnectedTerminals().iterator().next();
        assertNotNull(t);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(t).add();

        Properties params = new Properties();
        params.setProperty(XMLExporter.VERSION, IidmVersion.V_1_2.toString("."));
        params.setProperty(XMLExporter.THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, "true");
        assertThrows(PowsyblException.class, () -> network.write("XIIDM", params,
                workingDir));
        try {
            network.write("XIIDM", params, workingDir);
            fail();
        } catch (PowsyblException exception) {
            assertEquals("Version V_1_2 does not support slackTerminal extension", exception.getMessage());
        }
    }

    @Test
    void testWriteXml() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));
        String voltageLevelId = "VLHV2";
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        assertNotNull(vl);
        String busId = "NHV2";
        Bus bus = vl.getBusBreakerView().getBus(busId);
        assertNotNull(bus);
        Terminal t = bus.getConnectedTerminals().iterator().next();
        assertNotNull(t);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(t).add();
        Properties params = new Properties();
        params.setProperty(XMLExporter.VERSION, IidmVersion.V_1_2.toString("."));
        exporterTest(network, IidmVersion.V_1_2, "extensionTooRecentExportTest.xml", params);
    }
}
