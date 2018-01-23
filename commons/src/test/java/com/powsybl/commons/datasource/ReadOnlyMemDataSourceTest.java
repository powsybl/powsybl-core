/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.it>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ReadOnlyMemDataSourceTest {

    protected final byte[] getUncompressedData() {
        return "data".getBytes(StandardCharsets.UTF_8);
    }

    protected byte[] getCompressedData() throws IOException {
        return getUncompressedData();
    }

    protected final ReadOnlyMemDataSource createDataSource(String extension) throws IOException {
        ReadOnlyMemDataSource dataSource;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(getCompressedData())) {
            dataSource = DataSourceUtil.createReadOnlyMemDataSource("data.xiidm" + extension, bis);
        }

        return dataSource;
    }


    protected ReadOnlyMemDataSource testDataSource(String extension) throws IOException {
        ReadOnlyMemDataSource dataSource = createDataSource(extension);
        assertNotNull(dataSource);
        assertTrue(dataSource.exists("data.xiidm"));
        assertTrue(dataSource.exists(null, "xiidm"));

        assertArrayEquals(getUncompressedData(), ByteStreams.toByteArray(dataSource.newInputStream("data.xiidm")));
        assertArrayEquals(getUncompressedData(), ByteStreams.toByteArray(dataSource.newInputStream(null, "xiidm")));

        return dataSource;
    }

    @Test
    public void test() throws IOException {
        testDataSource("");
    }
}
