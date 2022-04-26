/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.TopologyExport;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.export.ExportOptions;
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
import java.util.Collections;
import java.util.Properties;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class TopologyExportTest extends AbstractConverterTest {

    @Test
    public void smallGridHVDC() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreakerHvdcEqTp().dataSource());
    }

    @Test
    public void smallGridBusBranch() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranchEqTp().dataSource());
    }

    @Test
    public void smallGridNodeBreaker() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreakerEqTp().dataSource());
    }

    @Test
    public void smallGridNodeBreakerSsh() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreakerEqTpSsh().dataSource(), true);
    }

    private void test(ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        test(dataSource, false);
    }

    private void test(ReadOnlyDataSource dataSource, boolean importSsh) throws IOException, XMLStreamException {
        // Import original
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export TP
        Path exportedTp = tmpDir.resolve("exportedTp.xml");
        try (OutputStream os = Files.newOutputStream(exportedTp)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            TopologyExport.write(expected, writer, context);
        }

        // Zip with new TP
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_EQ.xml", Repackager::eq)
                .with("test_TP.xml", exportedTp)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        if (importSsh) {
            r.with("test_SSH.xml", Repackager::ssh);
        }
        r.zip(repackaged);

        // Import with new TP
        Network actual = Importers.loadNetwork(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        prepareNetworkForComparison(expected);
        prepareNetworkForComparison(actual);

        // Export original and with new TP
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        exportOptions.setSorted(true);
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }

    private void prepareNetworkForComparison(Network network) {
        network.getAliases().forEach(network::removeAlias);
        network.getIdentifiables().forEach(identifiable -> {
            identifiable.getAliases().forEach(identifiable::removeAlias);
            identifiable.removeProperty("v");
            identifiable.removeProperty("angle");
        });
        // As the network does not have SSH or SV data, the buses are not exported to the IIDM file,
        // in order to verify that the nodes that make up each bus are correct, Nomianl V are copied from the voltage level
        // to the bus.
        network.getBusView().getBuses().forEach(bus -> {
            bus.setV(bus.getVoltageLevel().getNominalV());
        });
    }
}
