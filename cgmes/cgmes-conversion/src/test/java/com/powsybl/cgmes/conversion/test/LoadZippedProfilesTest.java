/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at soft.it>}
 */
class LoadZippedProfilesTest {

    @Test
    void oneFolderAndOneZipPerProfileTest() throws Exception {
        var testDataSource = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        Set<String> profiles = testDataSource.listNames(".*");
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            // copy and compress each of the profile to the file system
            Path workDir = fileSystem.getPath("/work");
            for (String profile : profiles) {
                try (var is = testDataSource.newInputStream(profile);
                        var os = new ZipOutputStream(Files.newOutputStream(workDir.resolve(profile + ".zip")))) {
                    os.putNextEntry(new ZipEntry(profile));
                    ByteStreams.copy(is, os);
                    os.closeEntry();
                }
            }

            Network network = Network.read(DataSource.fromPath(workDir));
            assertNotNull(network);
        }
    }

    @Test
    void emptyZipErrorTest() throws Exception {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path workDir = fileSystem.getPath("/work");
            try (var os = new ZipOutputStream(Files.newOutputStream(workDir.resolve("empty.zip")))) {
                os.closeEntry();
            }

            ReadOnlyDataSource datasource = DataSource.fromPath(workDir);
            PowsyblException ex = assertThrows(PowsyblException.class, () -> Network.read(datasource));
            assertEquals("Unsupported file format or invalid file.", ex.getMessage());
        }
    }
}
