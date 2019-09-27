package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;

public class IidmImportFromCgmesTest {

    private String impl;
    FileSystem fileSystem;
    private CgmesImport i;
    private Properties importParameters;
    private TestGridModel testGridModel;

    public IidmImportFromCgmesTest(TestGridModel gm,String impl) {
        this.testGridModel = gm;
        this.impl = impl;
        this.fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    public boolean modelNotEmpty(Network network) {
        if (network.getSubstationCount() == 0) {
            fail("Model is empty");
            return false;
        } else {
            return true;
        }
    }
    
    public Network loadNetwork() {
        
        ReadOnlyDataSource ds = testGridModel.dataSource();
        i = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
        importParameters = new Properties();
        importParameters.put("powsyblTripleStore", impl);
        importParameters.put("storeCgmesModelAsNetworkExtension", "true");
        Network network = i.importData(ds, importParameters);
        
        return network;
    }

    public Network exportAndLoadNetworkUpdated(Network network) throws IOException {
        CgmesExport e = new CgmesExport();
//        Path exportFolder = fileSystem.getPath("impl-" + impl);
        Path exportFolder = Paths.get(".\\tmp\\", impl);
        Files.createDirectories(exportFolder);
        DataSource exportDataSource = new FileDataSource(exportFolder, "");
        e.export(network, new Properties(), exportDataSource);

        Network networkUpdated = i.importData(exportDataSource, importParameters);

        return networkUpdated;
    }

}
