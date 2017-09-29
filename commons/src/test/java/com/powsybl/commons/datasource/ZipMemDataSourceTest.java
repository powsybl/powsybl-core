/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.it>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ZipMemDataSourceTest extends ReadOnlyMemDataSourceTest {

    private byte[] getExtraUncompressedData() {
        return "extra data".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected byte[] getCompressedData() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream os = new ZipOutputStream(bos)) {

            ZipEntry entry1 = new ZipEntry("data.xiidm");
            byte[] data = getUncompressedData();
            entry1.setSize(data.length);
            os.putNextEntry(entry1);
            os.write(data);
            os.closeEntry();

            ZipEntry entry2 = new ZipEntry("extra_data.xiidm");
            byte[] extraData = getExtraUncompressedData();
            entry2.setSize(extraData.length);
            os.putNextEntry(entry2);
            os.write(extraData);
            os.closeEntry();
        }

        return bos.toByteArray();
    }

    @Override
    protected ReadOnlyMemDataSource testDataSource(String extension) throws IOException {
        ReadOnlyMemDataSource dataSource = super.testDataSource(extension);

        assertTrue(dataSource.exists("extra_data.xiidm"));
        assertArrayEquals(getExtraUncompressedData(), ByteStreams.toByteArray(dataSource.newInputStream("extra_data.xiidm")));

        try {
            assertFalse(dataSource.exists("_data", "xiidm")); // baseName = data, data_data.xiidm does not exist
            assertArrayEquals(getExtraUncompressedData(), ByteStreams.toByteArray(dataSource.newInputStream("_data", "xiidm")));
            fail();
        } catch (IOException ignored) {
        }

        return dataSource;
    }

    @Test
    public void test() throws IOException {
        testDataSource(".zip");
    }
}
