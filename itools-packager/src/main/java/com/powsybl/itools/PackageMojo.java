/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public class PackageMojo {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageMojo.class);

    public void createPackage(Path dir, final Path baseDir, String archiveName) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(baseDir);
        Objects.requireNonNull(archiveName);

        // create archive name with extension zip by default
        String newArchiveName = archiveName.lastIndexOf('.') < 0 ? archiveName + CompressionType.ZIP.getExtension() : archiveName;
        CompressionType compressionType = CompressionType.compareFileExtension(newArchiveName);
        Path packageFilePath = baseDir.resolve(newArchiveName);
        LOGGER.info("Generate package file: {}", newArchiveName);

        Files.deleteIfExists(packageFilePath);

        try (DataOutputStream os = new DataOutputStream(Files.newOutputStream(packageFilePath))) {
            switch (compressionType) {
                case ZIP:
                    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os)) {
                        BrowseFileTree.createArchiveZip(dir, baseDir, zos);
                    }
                    break;

                case GZIP:
                case BZIP2:
                    try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
                        TarArchiveOutputStream tos = null;
                        if (compressionType.equals(CompressionType.GZIP)) {
                            tos = new TarArchiveOutputStream(new GZIPOutputStream(bos));
                        } else { // case of BZIP2
                            tos = new TarArchiveOutputStream(new BZip2CompressorOutputStream(bos));
                        }
                        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to store big number in the archive.
                        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU); // to store long file names in the archive.

                        BrowseFileTree.createArchiveTar(dir, baseDir, tos);
                        tos.close();
                    }
                    break;

                default:
                    throw new AssertionError(compressionType.getExtension());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
