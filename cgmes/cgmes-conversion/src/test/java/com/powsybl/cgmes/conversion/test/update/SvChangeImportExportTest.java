package com.powsybl.cgmes.conversion.test.update;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

public class SvChangeImportExportTest {

    @Test
    public void testHops() throws IOException {
        importExport();
    }

    private void importExport() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ReadOnlyDataSource ds = new ResourceDataSource("20191009_1130_FO3_HR1",
                new ResourceSet("/20191009_1130_FO3_HR1/", "20191008T2230Z_1D_HOPS_EQ_001.xml",
                    "20191009T0930Z_1D_HOPS_SSH_001.xml",
                    "20191009T0930Z_1D_HOPS_SV_001.xml",
                    "20191009T0930Z_1D_HOPS_TP_001.xml"),
                new ResourceSet("/cgmes-boundaries/", "20190812T0000Z__ENTSOE_EQBD_001.xml",
                    "20190812T0000Z__ENTSOE_TPBD_001.xml"));

            CgmesImport i = new CgmesImport();

            Network n = i.importData(ds, NetworkFactory.findDefault(), importParameters("false"));

            CgmesExport e = new CgmesExport();
            Path exportFolder = Paths.get(".\\tmp\\", "testHops");
            try {
                FileUtils.cleanDirectory(exportFolder.toFile());
            } catch (IOException x) {
                LOG.info("directory does not exist, crating new ...");
            }
            Files.createDirectories(exportFolder);
            DataSource exportDataSource = new FileDataSource(exportFolder, "");
            e.export(n, new Properties(), exportDataSource);
        }
    }

    private Properties importParameters(String convert_boundary) {
        Properties importParameters = new Properties();
        importParameters.put(CgmesImport.CONVERT_BOUNDARY, convert_boundary);
        return importParameters;
    }

    private static final Logger LOG = LoggerFactory.getLogger(SvChangeImportExportTest.class);
}
