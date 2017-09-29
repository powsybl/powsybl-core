/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.it>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class GzMemDataSourceTest extends ReadOnlyMemDataSourceTest {

    @Override
    protected byte[] getCompressedData() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream os = new GZIPOutputStream(bos)) {
            os.write(getUncompressedData());
        }

        return bos.toByteArray();
    }

    @Test
    public void test() throws IOException {
        testDataSource(".gz");
    }
}
