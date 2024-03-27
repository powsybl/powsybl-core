/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class CompressionFormatTest {

    @Test
    void test() {
        assertEquals(5, CompressionFormat.values().length);
        assertEquals("bz2", CompressionFormat.BZIP2.getExtension());
        assertEquals("gz", CompressionFormat.GZIP.getExtension());
        assertEquals("xz", CompressionFormat.XZ.getExtension());
        assertEquals("zip", CompressionFormat.ZIP.getExtension());
        assertEquals("zst", CompressionFormat.ZSTD.getExtension());

        List<String> formats = Arrays.asList(
            CompressionFormat.BZIP2.name(),
            CompressionFormat.GZIP.name(),
            CompressionFormat.XZ.name(),
            CompressionFormat.ZIP.name(),
            CompressionFormat.ZSTD.name());
        assertEquals(formats, CompressionFormat.getFormats());
    }
}
