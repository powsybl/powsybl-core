package com.powsybl.cgmes.conversion.test.update;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

public class CgmesUpdateUnsupportedTest {

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        CgmesImport cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        network = cgmesImport.importData(ds, NetworkFactory.findDefault(), null);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTwoWindingsTransformerRatedU() {
        NetworkChanges.modifyTwoWindingsTransformerRatedU(network);
        tryExport();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTwoWindingsTransformerRX() {
        NetworkChanges.modifyTwoWindingsTransformerRX(network);
        tryExport();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTwoWindingsTransformerGB() {
        NetworkChanges.modifyTwoWindingsTransformerGB(network);
        tryExport();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedGeneratorReactiveLimits() {
        NetworkChanges.modifyGeneratorReactiveLimits(network);
        tryExport();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedGeneratorRegulatingControl() {
        NetworkChanges.modifyGeneratorVoltageRegulation(network);
        tryExport();
    }

    private void tryExport() {
        DataSource tmpDataSource = new FileDataSource(fileSystem.getPath("/"), "");
        new CgmesExport().export(network, new Properties(), tmpDataSource);
    }

    private FileSystem fileSystem;
    private Network network;

}
