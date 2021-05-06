/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.StateVariablesExport;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class StateVariablesExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2);
    }

    @Test
    public void microGridAssembled() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), 4, r -> {
            addRepackagerFiles("NL", r);
            addRepackagerFiles("BE", r);
        });
    }

    @Test
    public void smallGridBusBranch() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4);
    }

    @Test
    public void smallGridNodeBreaker() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreaker().dataSource(), 4);
    }

    private static void addRepackagerFiles(String tso, Repackager repackager) {
        repackager.with("test_" + tso + "_EQ.xml", name -> name.contains(tso) && name.contains("EQ"))
                .with("test_" + tso + "_TP.xml", name -> name.contains(tso) && name.contains("TP"))
                .with("test_" + tso + "_SSH.xml", name -> name.contains(tso) && name.contains("SSH"));
    }

    private void test(ReadOnlyDataSource dataSource, int svVersion) throws IOException, XMLStreamException {
        test(dataSource, svVersion, r -> r
                .with("test_EQ.xml", Repackager::eq)
                .with("test_TP.xml", Repackager::tp)
                .with("test_SSH.xml", Repackager::ssh));
    }

    private void test(ReadOnlyDataSource dataSource, int svVersion, Consumer<Repackager> repackagerConsumer) throws XMLStreamException, IOException {
        // Import original
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.profile-used-for-initial-state-values", "SV");
        properties.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected0 = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Check the information stored in the extension before it is serialized
        CgmesIidmMapping iidmMapping = expected0.getExtension(CgmesIidmMapping.class);
        if (iidmMapping != null) {
            for (Line l : expected0.getLines()) {
                assertTrue(iidmMapping.getTopologicalNodes(l.getTerminal1().getBusView().getBus().getId()).contains(iidmMapping.getTopologicalNode(l.getId(), 1)));
                assertTrue(iidmMapping.getTopologicalNodes(l.getTerminal2().getBusView().getBus().getId()).contains(iidmMapping.getTopologicalNode(l.getId(), 2)));
            }
        }

        // Export to XIIDM and re-import to test serialization of CGMES-IIDM extension
        NetworkXml.write(expected0, tmpDir.resolve("temp.xiidm"));
        Network expected = NetworkXml.read(tmpDir.resolve("temp.xiidm"));

        // Export SV
        CgmesExportContext context = new CgmesExportContext(expected);
        Path exportedSv = tmpDir.resolve("exportedSv.xml");
        try (OutputStream os = Files.newOutputStream(exportedSv)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            context.getSvModelDescription().setVersion(svVersion);
            context.setExportBoundaryPowerFlows(true);
            StateVariablesExport.write(expected, writer, context);
        }

        // Zip with new SV
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_SV.xml", exportedSv)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        repackagerConsumer.accept(r);
        r.zip(repackaged);

        // Import with new SV
        Network actual = Importers.loadNetwork(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new SV
        // comparison without extensions, only Networks
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        NetworkXml.writeAndValidate(expected, exportOptions, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, exportOptions, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }
}
