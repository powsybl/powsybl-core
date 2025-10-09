/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class Repackager {

    Repackager(ReadOnlyDataSource dataSource) {
        this.dataSource = dataSource;
    }

    Repackager with(String targetFilename, Predicate<String> dataSourceFileSelector) {
        dataSourceInputs.put(targetFilename, dataSourceFileSelector);
        return this;
    }

    Repackager with(String targetFilename, Path inputFile) {
        fileInputs.put(targetFilename, inputFile);
        return this;
    }

    void zip(Path outputPath) throws IOException {
        try (OutputStream fos = Files.newOutputStream(outputPath);
                ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            for (Map.Entry<String, Predicate<String>> e : dataSourceInputs.entrySet()) {
                String targetFilename = e.getKey();
                Predicate<String> dataSourceFileSelector = e.getValue();
                if (fileExists(dataSource, dataSourceFileSelector)) {
                    try (InputStream is = newInputStream(dataSource, dataSourceFileSelector)) {
                        zipFile(targetFilename, is, zipOut);
                    }
                }
            }
            for (Map.Entry<String, Path> e : fileInputs.entrySet()) {
                String targetFilename = e.getKey();
                Path inputFile = e.getValue();
                try (InputStream is = Files.newInputStream(inputFile)) {
                    zipFile(targetFilename, is, zipOut);
                }
            }
        }
    }

    static InputStream newInputStream(ReadOnlyDataSource ds, Predicate<String> file) throws IOException {
        return ds.newInputStream(getName(ds, file));
    }

    static String getName(ReadOnlyDataSource ds, Predicate<String> fileSelector) {
        return new CgmesOnDataSource(ds).names().stream().filter(fileSelector).findFirst().orElseThrow();
    }

    static boolean fileExists(ReadOnlyDataSource ds, Predicate<String> fileSelector) {
        return new CgmesOnDataSource(ds).names().stream().anyMatch(fileSelector);
    }

    static boolean eq(String name) {
        return !name.contains("_BD") && name.contains("_EQ");
    }

    static boolean tp(String name) {
        return !name.contains("_BD") && name.contains("_TP");
    }

    static boolean ssh(String name) {
        return name.contains("_SSH");
    }

    static boolean sv(String name) {
        return name.contains("_SV");
    }

    static boolean eqBd(String name) {
        return name.contains("_EQ") && name.contains("_BD");
    }

    static boolean tpBd(String name) {
        return name.contains("_TP") && name.contains("_BD");
    }

    private static void zipFile(String entryName, InputStream toZip, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = toZip.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
    }

    // For each file repackaged from data source,
    // store target filename and predicate for data source file selector
    private final Map<String, Predicate<String>> dataSourceInputs = new HashMap<>();
    // For each file repackaged from external file,
    // store target filename and external Path
    private final Map<String, Path> fileInputs = new HashMap<>();
    private final ReadOnlyDataSource dataSource;

}
