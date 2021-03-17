/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.EquipmentExport;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
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
import java.util.Properties;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class EquipmentExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2);
    }

//    @Test
//    public void smallGrid() throws IOException, XMLStreamException {
//        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4);
//    }

    private void test(ReadOnlyDataSource dataSource, int eqVersion) throws IOException, XMLStreamException {
        // Import original
        Properties properties = new Properties();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export EQ
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = Files.newOutputStream(exportedEq)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            //context.getEqModelDescription().setVersion(eqVersion);
            EquipmentExport.write(expected, writer, context);
        }

        // Zip with new EQ
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_EQ.xml", exportedEq)
                .with("test_TP.xml", Repackager::tp)
                .with("test_SV.xml", Repackager::sv)
                .with("test_SSH.xml", Repackager::ssh)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        r.zip(repackaged);

        // Import with new EQ
        Network actual = Importers.loadNetwork(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new EQ
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }
}
