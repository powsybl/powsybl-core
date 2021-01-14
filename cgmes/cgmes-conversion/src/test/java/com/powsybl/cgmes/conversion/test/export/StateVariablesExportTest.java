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
import com.powsybl.cgmes.conversion.extensions.CgmesIidmMapping;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class StateVariablesExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2);
    }

    @Test
    public void smallGridBusBranch() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4);
    }

    @Test
    @Ignore("fails with error: TopologicalNode _048b86b5-c766-11e1-8775-005056c00008 is already mapped to another bus")
    public void smallGridNodeBreaker() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreaker().dataSource(), 4);
    }

    private void test(ReadOnlyDataSource dataSource, int svVersion) throws IOException, XMLStreamException {
        // Import original
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.profile-used-for-initial-state-values", "SV");
        properties.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export SV
        CgmesExportContext context = new CgmesExportContext(expected);
        CgmesIidmMapping cgmesIidmMapping = expected.getExtension(CgmesIidmMapping.class);
        if (cgmesIidmMapping != null) {
            context.setTopologicalNodeByBusBreakerBusMapping(expected.getExtension(CgmesIidmMapping.class).toMap());
            context.setTopologicalMappingUse(CgmesExportContext.TopologicalMappingUse.MAPPING_ONLY);
        }
        Path exportedSv = tmpDir.resolve("exportedSv.xml");
        try (OutputStream os = Files.newOutputStream(exportedSv)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            context.getSvModelDescription().setVersion(svVersion);
            StateVariablesExport.write(expected, writer, context);
        }

        // Zip with new SV
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
            .with("test_EQ.xml", Repackager::eq)
            .with("test_TP.xml", Repackager::tp)
            .with("test_SV.xml", exportedSv)
            .with("test_SSH.xml", Repackager::ssh)
            .with("test_EQ_BD.xml", Repackager::eqBd)
            .with("test_TP_BD.xml", Repackager::tpBd);
        r.zip(repackaged);

        // Import with new SV
        Network actual = Importers.loadNetwork(repackaged,
            DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new SV
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }
}
