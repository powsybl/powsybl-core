/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class FileInformationTest {

    @Test
    void tests() {
        unitTest("dummy", "dummy", null, null, "");
        unitTest("dummy.iidm", "dummy", null, null, ".iidm");
        unitTest("dummy.tar.gz", "dummy", CompressionFormat.GZIP, ArchiveFormat.TAR, "");
        unitTest("dummy.xml.xz", "dummy", CompressionFormat.XZ, null, ".xml");

        // A zip file is a compressed archive
        unitTest("dummy.zip", "dummy", CompressionFormat.ZIP, ArchiveFormat.ZIP, "");

        // It can still specify the source format
        unitTest("dummy.jiidm.zip", "dummy", CompressionFormat.ZIP, ArchiveFormat.ZIP, ".jiidm");

        // If there is a usual format and additional extensions, those extensions go in the baseName
        unitTest("dummy.v3.xml.zst", "dummy.v3", CompressionFormat.ZSTD, null, ".xml");

        // If there are additional extensions but no usual format, the last extension becomes the source format
        unitTest("dummy.v3.bz2", "dummy", CompressionFormat.BZIP2, null, ".v3");

        // Hidden files
        unitTest(".dummy", ".dummy", null, null, "");
        unitTest(".iidm", ".iidm", null, null, "");
        unitTest(".zip", "", CompressionFormat.ZIP, ArchiveFormat.ZIP, "");
        unitTest(".dummy.tar.gz", ".dummy", CompressionFormat.GZIP, ArchiveFormat.TAR, "");
        unitTest(".tar.gz", "", CompressionFormat.GZIP, ArchiveFormat.TAR, "");
        unitTest(".dummy.jiidm.zip", ".dummy", CompressionFormat.ZIP, ArchiveFormat.ZIP, ".jiidm");

        PowsyblException exception = assertThrows(PowsyblException.class, () -> new FileInformation(""));
        assertEquals("File name cannot be empty nor just a dot", exception.getMessage());

        exception = assertThrows(PowsyblException.class, () -> new FileInformation("."));
        assertEquals("File name cannot be empty nor just a dot", exception.getMessage());
    }

    private void unitTest(String filename, String baseName,
                          CompressionFormat compressionFormat, ArchiveFormat archiveFormat, String sourceFormat) {
        // Create the file information object
        FileInformation fileInformation = new FileInformation(filename);

        // Check the information
        assertEquals(baseName, fileInformation.getBaseName());
        assertEquals(compressionFormat, fileInformation.getCompressionFormat());
        assertEquals(archiveFormat, fileInformation.getArchiveFormat());
        assertEquals(sourceFormat, fileInformation.getSourceFormatExtension());
    }

    @Test
    void testToString() {
        // Create the file information object
        FileInformation fileInformation = new FileInformation("foo.bar.tar.gz");

        assertEquals("FileInformation[baseName=foo, sourceFormatExtension=.bar, archiveFormat=TAR, compressionFormat=GZIP]", fileInformation.toString());
    }
}
