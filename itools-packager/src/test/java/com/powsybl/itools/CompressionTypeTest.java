/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.itools.CompressionType.compareFileExtension;
import static org.junit.Assert.*;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public class CompressionTypeTest {

    @Test
    public void test() {
        assertEquals(4, CompressionType.values().length);
        assertEquals(".zip", CompressionType.ZIP.getExtension());
        assertEquals(".tar.gz", CompressionType.GZIP.getExtension());
        assertEquals(".tar.bz2", CompressionType.BZIP2.getExtension());
        assertEquals("Unexpected Compression Format value", CompressionType.ZIPERROR.getExtension());

        assertEquals(CompressionType.ZIP, compareFileExtension(".zip"));
        assertEquals(CompressionType.GZIP, compareFileExtension(".tar.gz"));
        assertEquals(CompressionType.BZIP2, compareFileExtension(".tar.bz2"));
        assertEquals(CompressionType.ZIPERROR, compareFileExtension("Unexpected Compression Format value"));

        List<String> formats = Arrays.asList(
                CompressionType.ZIP.name(),
                CompressionType.GZIP.name(),
                CompressionType.BZIP2.name(),
                CompressionType.ZIPERROR.name());
        assertEquals(formats, CompressionType.getFormats());
    }
}
