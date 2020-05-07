package com.powsybl.commons.datastore;

import java.io.IOException;

public class Bzip2FileDataStoreTest extends AbstractDataStoreTest {

    @Override
    protected DataStore createDataStore() throws IOException {
        return DataStoreUtil.createDataStore(testDir.resolve("test.txt.bz2"));
    }

}
