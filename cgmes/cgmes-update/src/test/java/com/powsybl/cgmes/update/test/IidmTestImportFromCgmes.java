package com.powsybl.cgmes.update.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class IidmTestImportFromCgmes {

    public IidmTestImportFromCgmes(TestGridModel gm) {
        this.gridModel = gm;
    }

    public Network importTestModelFromCgmes() throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = new Date();
        String impl = TripleStoreFactory.defaultImplementation();
        try (FileSystem fs = Jimfs.newFileSystem()) {
            PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
            CgmesImport i = new CgmesImport(platformConfig);
            Properties params = new Properties();
            params.put("storeCgmesModelAsNetwork", "true");
            params.put("powsyblTripleStore", impl);
            ReadOnlyDataSource ds = gridModel.dataSource();
            Network network = i.importData(ds, params);
            if (network.getSubstationCount() == 0) {
                fail("Model is empty");
            }
            network.setCaseDate(DateTime.parse(dateFormat.format(date)));
            LOG.info("Imported TestModel From Cgmes method on {}", network.getCaseDate());

            return network;
        }
    }

    private final TestGridModel gridModel;
    private static final Logger LOG = LoggerFactory.getLogger(IidmTestImportFromCgmes.class);
}
