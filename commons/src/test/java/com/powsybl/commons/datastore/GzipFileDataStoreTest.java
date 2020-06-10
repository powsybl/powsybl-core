package com.powsybl.commons.datastore;

import java.io.IOException;

public class GzipFileDataStoreTest extends AbstractDataStoreTest {

    @Override
    protected DataStore createDataStore() throws IOException {
        return DataStores.createDataStore(testDir.resolve("test.txt.gz"));
    }

}
