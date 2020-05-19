/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;

import org.junit.Test;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datastore.DataPack;
import com.powsybl.commons.datastore.DataResolver;
import com.powsybl.commons.datastore.DirectoryDataStore;
import com.powsybl.commons.datastore.NonUniqueResultException;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class UcteDataFormatTest extends AbstractConverterTest {
    private final Properties props = new Properties();

    @Test
    public void test() throws IOException, NonUniqueResultException {

        DirectoryDataStore ds = new DirectoryDataStore(tmpDir);
        UcteDataFormat format = new UcteDataFormat();
        DataResolver resolver = format.getDataResolver();

        Optional<DataPack> dp = resolver.resolve(ds, "test.uct", props);
        assertEquals(false, dp.isPresent());

        try (OutputStream os = ds.newOutputStream("test.uct", false)) {
            dp = resolver.resolve(ds, "test.uct", props);
            assertEquals(true, dp.isPresent());
        }

        dp = resolver.resolve(ds, null, props);
        assertEquals(true, dp.isPresent());
        assertEquals("test.uct", dp.get().getMainEntry().get().getName());

        dp = resolver.resolve(ds, "wrong.uct", props);
        assertEquals(false, dp.isPresent());

        try (OutputStream os = ds.newOutputStream("test.txt", false)) {
            dp = resolver.resolve(ds, "test.txt", props);
            assertEquals(false, dp.isPresent());
        }

    }

    @Test(expected = NonUniqueResultException.class)
    public void testNonUnique() throws IOException, NonUniqueResultException {

        DirectoryDataStore ds = new DirectoryDataStore(tmpDir);
        UcteDataFormat format = new UcteDataFormat();
        DataResolver resolver = format.getDataResolver();

        try (OutputStream os = ds.newOutputStream("test.uct", false)) {
            Optional<DataPack> dp = resolver.resolve(ds, "test.uct", props);
            assertEquals(true, dp.isPresent());
        }

        try (OutputStream os = ds.newOutputStream("multi.uct", false)) {
            Optional<DataPack> dp = resolver.resolve(ds, null, props);
            assertEquals(true, dp.isPresent());
        }

        Optional<DataPack> dp = resolver.resolve(ds, "multi.uct", props);
        assertEquals(true, dp.isPresent());

    }

}
