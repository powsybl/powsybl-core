/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.TopologyExport;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class TopologyExportTest extends AbstractSerDeTest {

    @Test
    void smallGridHVDC() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerHvdcEqTp().dataSource()));
    }

    @Test
    void smallGridBusBranch() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallBusBranchEqTp().dataSource()));
    }

    @Test
    void smallGridNodeBreaker() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerEqTp().dataSource()));
    }

    @Test
    void smallGridNodeBreakerSsh() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerEqTpSsh().dataSource(), true));
    }

    private boolean test(ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return test(dataSource, false);
    }

    private boolean test(ReadOnlyDataSource dataSource, boolean importSsh) throws IOException, XMLStreamException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        // Import original
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);

        // Export TP
        Path exportedTp = tmpDir.resolve("exportedTp.xml");
        try (OutputStream os = Files.newOutputStream(exportedTp)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            TopologyExport.write(expected, writer, context);
        }

        // Zip with new TP
        Path repackaged = tmpDir.resolve("test.zip");
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
        Network actual = Network.read(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), importParams);

        prepareNetworkForComparison(expected);
        prepareNetworkForComparison(actual);

        // Export original and with new TP
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        exportOptions.setSorted(true);
        Path expectedPath = tmpDir.resolve("expected.xml");
        Path actualPath = tmpDir.resolve("actual.xml");
        NetworkSerDe.write(expected, exportOptions, expectedPath);
        NetworkSerDe.write(actual, exportOptions, actualPath);
        NetworkSerDe.validate(actualPath);

        // Compare
        return ExportXmlCompare.compareNetworks(expectedPath, actualPath);
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
        network.getBusView().getBuses().forEach(bus -> bus.setV(bus.getVoltageLevel().getNominalV()));
    }
}
