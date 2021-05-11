/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.extensions.CgmesSvMetadataAdder;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.extensions.CimCharacteristicsAdder;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.powsybl.cgmes.model.CgmesNamespace.CIM_14_NAMESPACE;
import static com.powsybl.cgmes.model.CgmesNamespace.CIM_16_NAMESPACE;
import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportContextTest {

    @Test
    public void networkConstructor() {
        Network network = EurostagTutorialExample1Factory.create();

        CgmesExportContext context1 = new CgmesExportContext(network);

        assertEquals(16, context1.getCimVersion());
        assertEquals(CIM_16_NAMESPACE, context1.getCimNamespace());
        assertEquals(CgmesTopologyKind.BUS_BRANCH, context1.getTopologyKind());
        assertEquals(network.getCaseDate(), context1.getScenarioTime());
        assertEquals("SV Model", context1.getSvModelDescription().getDescription());
        assertEquals(1, context1.getSvModelDescription().getVersion());
        assertTrue(context1.getSvModelDescription().getDependencies().isEmpty());
        assertEquals("powsybl.org", context1.getSvModelDescription().getModelingAuthoritySet());

        network.newExtension(CimCharacteristicsAdder.class)
            .setCimVersion(14)
            .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
            .add();
        network.newExtension(CgmesSvMetadataAdder.class)
            .setDescription("test")
            .setSvVersion(2)
            .addDependency("powsybl.test.org")
            .addDependency("cgmes")
            .setModelingAuthoritySet("cgmes.org")
            .add();

        CgmesExportContext context2 = new CgmesExportContext(network);

        assertEquals(14, context2.getCimVersion());
        assertEquals(CIM_14_NAMESPACE, context2.getCimNamespace());
        assertEquals(CgmesTopologyKind.NODE_BREAKER, context2.getTopologyKind());
        assertEquals(network.getCaseDate(), context2.getScenarioTime());
        assertEquals("test", context2.getSvModelDescription().getDescription());
        assertEquals(3, context2.getSvModelDescription().getVersion());
        assertEquals(2, context2.getSvModelDescription().getDependencies().size());
        assertTrue(context2.getSvModelDescription().getDependencies().contains("powsybl.test.org"));
        assertTrue(context2.getSvModelDescription().getDependencies().contains("cgmes"));
        assertEquals("cgmes.org", context2.getSvModelDescription().getModelingAuthoritySet());
    }

    @Test
    public void emptyConstructor() {
        CgmesExportContext context = new CgmesExportContext();
        assertEquals(16, context.getCimVersion());
        assertEquals(CIM_16_NAMESPACE, context.getCimNamespace());
        assertEquals(CgmesTopologyKind.BUS_BRANCH, context.getTopologyKind());
        assertTrue(new Duration(DateTime.now(), context.getScenarioTime()).getStandardMinutes() < 1);
        assertEquals("SV Model", context.getSvModelDescription().getDescription());
        assertEquals(1, context.getSvModelDescription().getVersion());
        assertTrue(context.getSvModelDescription().getDependencies().isEmpty());
        assertEquals("powsybl.org", context.getSvModelDescription().getModelingAuthoritySet());
        assertFalse(context.exportBoundaryPowerFlows());
    }

    @Test
    public void getSet() {
        CgmesExportContext context = new CgmesExportContext()
            .setCimVersion(14)
            .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
            .setScenarioTime(DateTime.parse("2020-09-22T17:21:11.381+02:00"))
            .setExportBoundaryPowerFlows(true);
        context.getSvModelDescription()
            .setDescription("test")
            .setVersion(2)
            .addDependency("powsybl.test.org")
            .addDependency("cgmes")
            .setModelingAuthoritySet("cgmes.org");

        assertEquals(14, context.getCimVersion());
        assertEquals(CIM_14_NAMESPACE, context.getCimNamespace());
        assertEquals(CgmesTopologyKind.NODE_BREAKER, context.getTopologyKind());
        assertEquals(DateTime.parse("2020-09-22T17:21:11.381+02:00"), context.getScenarioTime());
        assertEquals("test", context.getSvModelDescription().getDescription());
        assertEquals(2, context.getSvModelDescription().getVersion());
        assertEquals(2, context.getSvModelDescription().getDependencies().size());
        assertTrue(context.getSvModelDescription().getDependencies().contains("powsybl.test.org"));
        assertTrue(context.getSvModelDescription().getDependencies().contains("cgmes"));
        assertEquals("cgmes.org", context.getSvModelDescription().getModelingAuthoritySet());
        assertTrue(context.exportBoundaryPowerFlows());

        List<String> dependencies = Arrays.asList("test1", "test2", "test3");
        context.getSvModelDescription().addDependencies(dependencies);
        assertEquals(5, context.getSvModelDescription().getDependencies().size());
        assertTrue(context.getSvModelDescription().getDependencies().containsAll(dependencies));

        context.getSvModelDescription().clearDependencies();
        assertTrue(context.getSvModelDescription().getDependencies().isEmpty());
    }

    @Test
    public void nodeBreakerBuildTNMappingError() throws IOException {
        // Instead of a generic NPE exception,
        // Check that a controlled exception is thrown explaining the problem

        // When a CgmesExportContext is built from a Network that has NOT been imported
        // with the option to create the mapping between buses and Topological Nodes
        // That is, the CgmesExportContext should be responsible for creating that mapping
        ReadOnlyDataSource ds = CgmesConformity1Catalog.smallNodeBreaker().dataSource();

        // Import without creating mapping between buses and Topological Nodes during import
        Properties ip = new Properties();
        ip.put("iidm.import.cgmes.create-cgmes-export-mapping", "false");
        Network n = new CgmesImport().importData(ds, NetworkFactory.findDefault(), ip);

        // Export SSH, SV files using only information from Network
        Properties ep = new Properties();
        ep.setProperty(CgmesExport.USING_ONLY_NETWORK, "true");
        String exportBaseName = "testNoNPE";
        ep.setProperty(CgmesExport.BASE_NAME, exportBaseName);
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
            String expectedMessage = "Node/breaker model without explicit mapping between IIDM buses and CGMES Topological Nodes. "
                + " To be able to export you must import the CGMES data with the parameter "
                + CgmesImport.CREATE_CGMES_EXPORT_MAPPING
                + " set to true";
            assertThrows(expectedMessage,
                PowsyblException.class,
                () -> new CgmesExport().export(n, ep, new FileDataSource(tmpDir, exportBaseName)));
            // TODO (Luma) After TP files are exported and exception is not thrown,
            // check that these file exists:
            // tmpDir.resolve(exportBaseName + "_SSH.xml")
            // tmpDir.resolve(exportBaseName + "_SV.xml")
        }

        // TODO (Luma) When TP files are exported,
        // We should be able to export with and without the mapping Bus-TN from the import,
        // and re-importing the exported data should give the same networks
    }
}
