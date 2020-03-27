package com.powsybl.commons.datastore;

import java.nio.file.Paths;

public class PathDataStoreTest extends AbstractDataStoreTest {

    @Override
    protected DataStore createDataStore() {
        return new PathDataStore(Paths.get(testDir.toUri()));
    }

}
