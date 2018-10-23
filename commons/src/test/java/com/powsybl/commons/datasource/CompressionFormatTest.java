/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class CompressionFormatTest {

    @Test
    public void test() {
        assertEquals(3, CompressionFormat.values().length);
        assertEquals("gz", CompressionFormat.GZIP.getExtension());
        assertEquals("bz2", CompressionFormat.BZIP2.getExtension());
        assertEquals("zip", CompressionFormat.ZIP.getExtension());

        List<String> formats = Arrays.asList(
            CompressionFormat.GZIP.name(),
            CompressionFormat.BZIP2.name(),
            CompressionFormat.ZIP.name());
        assertEquals(formats, CompressionFormat.getFormats());
    }
}
