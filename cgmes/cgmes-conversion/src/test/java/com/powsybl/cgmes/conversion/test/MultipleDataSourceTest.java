/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MultipleDataSourceTest {

    @Test
    void oneZipPerProfileTest() throws Exception {
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

            // create a multiple data source with all zipped profiles
            List<ReadOnlyDataSource> files = profiles.stream()
                    .map(profile -> workDir.resolve(profile + ".zip"))
                    .map(DataSource::fromPath)
                    .collect(Collectors.toList());
            Properties importParams = new Properties();
            importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
            Network network = Network.read(files, importParams);
            assertNotNull(network);
        }
    }
}
