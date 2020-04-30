/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;

import org.junit.Test;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datastore.DataEntry;
import com.powsybl.commons.datastore.DataPack;
import com.powsybl.commons.datastore.DataResolver;
import com.powsybl.commons.datastore.NonUniqueResultException;
import com.powsybl.commons.datastore.DirectoryDataStore;
import com.powsybl.iidm.IidmImportExportMode;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class XmlDataFormatTest extends AbstractConverterTest {
    private final Properties props = new Properties();

    @Test
    public void test() throws IOException, NonUniqueResultException {

        DirectoryDataStore ds = new DirectoryDataStore(tmpDir);
        XmlDataFormat format = new XmlDataFormat();
        DataResolver resolver = format.getDataResolver();

        Optional<DataPack> dp = resolver.resolve(ds, "test.xiidm", props);
        assertEquals(false, dp.isPresent());

        try (OutputStream os = ds.newOutputStream("test.xiidm", false)) {
            dp = resolver.resolve(ds, "test.xiidm", props);
            assertEquals(true, dp.isPresent());
        }

        dp = resolver.resolve(ds, null, props);
        assertEquals(true, dp.isPresent());
        assertEquals("test.xiidm", dp.get().getMainEntry().get().getName());

        dp = resolver.resolve(ds, "wrong.xiidm", props);
        assertEquals(false, dp.isPresent());

        try (OutputStream os = ds.newOutputStream("test.txt", false)) {
            dp = resolver.resolve(ds, "test.txt", props);
            assertEquals(false, dp.isPresent());
        }

    }

    @Test(expected = NonUniqueResultException.class)
    public void testNonUnique() throws IOException, NonUniqueResultException {

        DirectoryDataStore ds = new DirectoryDataStore(tmpDir);
        XmlDataFormat format = new XmlDataFormat();
        DataResolver resolver = format.getDataResolver();

        try (OutputStream os = ds.newOutputStream("test.xiidm", false)) {
            Optional<DataPack> dp = resolver.resolve(ds, "test.xiidm", props);
            assertEquals(true, dp.isPresent());
        }

        try (OutputStream os = ds.newOutputStream("multi.xiidm", false)) {
            Optional<DataPack> dp = resolver.resolve(ds, null, props);
            assertEquals(true, dp.isPresent());
        }

        Optional<DataPack> dp = resolver.resolve(ds, "multi.xiidm", props);
        assertEquals(true, dp.isPresent());

    }

    @Test
    public void testExtensions() throws IOException, NonUniqueResultException {

        DirectoryDataStore ds = new DirectoryDataStore(tmpDir);
        XmlDataFormat format = new XmlDataFormat();
        DataResolver resolver = format.getDataResolver();

        try (OutputStream os = ds.newOutputStream("test.xiidm", false)) {
            Optional<DataPack> dp = resolver.resolve(ds, "test.xiidm", props);
            assertEquals(true, dp.isPresent());
        }

        try (OutputStream os = ds.newOutputStream("test-ext.xiidm", false)) {
            props.setProperty(XmlDataResolver.IMPORT_MODE, IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE.toString());
            Optional<DataPack> dp = resolver.resolve(ds, "test.xiidm", props);
            assertEquals(true, dp.isPresent());
            DataPack dpext = dp.get();
            assertEquals(2, dpext.getEntries().size());
            Optional<DataEntry> extop = dpext.getEntry("test-ext.xiidm");
            assertEquals(true, extop.isPresent());
            DataEntry ext = extop.get();
            assertEquals(true, ext.getTags().contains(DataPack.EXTENSION_TAG));

        }

        try (OutputStream os = ds.newOutputStream("test-loadFoo.xiidm", false); OutputStream os2 = ds.newOutputStream("test-loadBar.xiidm", false)) {
            props.setProperty(XmlDataResolver.IMPORT_MODE, IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE.toString());
            props.setProperty(XmlDataResolver.EXTENSIONS_LIST, "loadFoo,loadBar");

            Optional<DataPack> dp = resolver.resolve(ds, "test.xiidm", props);
            assertEquals(true, dp.isPresent());
            DataPack dpext = dp.get();
            assertEquals(3, dpext.getEntries().size());

            Optional<DataEntry> extop = dpext.getEntry("test-loadFoo.xiidm");
            assertEquals(true, extop.isPresent());
            DataEntry ext = extop.get();
            assertEquals(true, ext.getTags().contains(DataPack.EXTENSION_TAG));

            Optional<DataEntry> extop2 = dpext.getEntry("test-loadBar.xiidm");
            assertEquals(true, extop2.isPresent());
            DataEntry ext2 = extop2.get();
            assertEquals(true, ext2.getTags().contains(DataPack.EXTENSION_TAG));
        }
    }

}
