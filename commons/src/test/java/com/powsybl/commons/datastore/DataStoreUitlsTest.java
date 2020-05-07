package com.powsybl.commons.datastore;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DataStoreUitlsTest {

    @Test
    public void testBasename() {
        assertEquals("test.xiidm", DataStoreUtil.getBasename("test.xiidm.gz"));
        assertEquals("", DataStoreUtil.getBasename(".test"));
    }

    @Test
    public void testExtension() {
        assertEquals("xiidm", DataStoreUtil.getExtension("test.xiidm"));
        assertEquals("xiidm", DataStoreUtil.getExtension("test.one.xiidm"));
        assertEquals("xiidm", DataStoreUtil.getExtension(".test.one.xiidm"));
        assertEquals("", DataStoreUtil.getExtension("test."));
    }
}
