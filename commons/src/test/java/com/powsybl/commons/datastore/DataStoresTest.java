package com.powsybl.commons.datastore;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DataStoresTest {

    @Test
    public void testBasename() {
        assertEquals("test.xiidm", DataStores.getBasename("test.xiidm.gz"));
        assertEquals("", DataStores.getBasename(".test"));
    }

    @Test
    public void testExtension() {
        assertEquals("xiidm", DataStores.getExtension("test.xiidm"));
        assertEquals("xiidm", DataStores.getExtension("test.one.xiidm"));
        assertEquals("xiidm", DataStores.getExtension(".test.one.xiidm"));
        assertEquals("", DataStores.getExtension("test."));
    }
}
