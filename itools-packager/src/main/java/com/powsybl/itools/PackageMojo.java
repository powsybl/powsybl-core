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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */

public class PackageMojo {

    private static final String ZIP = ".zip";
    private static final String GZIP = ".tar.gz";
    private static final String BZIP2 = ".tar.bz2";

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageMojo.class);

    public void createPackage(Path dir, final Path baseDir, String archiveName) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(baseDir);
        Objects.requireNonNull(archiveName);

        // create archive name with extension zip by default
        String newArchiveName = archiveName;
        int lastIndexOf = archiveName.lastIndexOf('.');
        if (lastIndexOf < 0) {
            // file extension: ".zip" by default
            newArchiveName += ZIP;
        }

        String compressionType = compareFileExtension(newArchiveName);
        Path packageFilePath = baseDir.resolve(newArchiveName);
        LOGGER.info("Generate package file : " + newArchiveName);

        try (FileOutputStream fos = new FileOutputStream(String.valueOf(packageFilePath))) {
            switch (compressionType) {
                case ZIP:
                    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {
                        BrowseFileTree.createArchiveZip(dir, baseDir, zos);
                    }
                    break;

                case GZIP:
                case BZIP2:
                    try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        TarArchiveOutputStream tos = null;
                        if (compressionType.endsWith(GZIP)) {
                            tos = new TarArchiveOutputStream(new GZIPOutputStream(bos));
                        } else { // case of BZIP2
                            tos = new TarArchiveOutputStream(new BZip2CompressorOutputStream(bos));
                        }
                        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to store big number in the archive.
                        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU); // to store long file names in the archive.

                        BrowseFileTree.createArchiveTar(dir, baseDir, tos);
                    }
                    break;

                default:
                    throw new AssertionError(compressionType);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String compareFileExtension(String name) {
        String ext = "Unexpected Compression Format value : " + name;
        if (name.endsWith(ZIP)) {
            ext = ZIP;
        } else if (name.endsWith(GZIP)) {
            ext = GZIP;
        } else if (name.endsWith(BZIP2)) {
            ext = BZIP2;
        }

        return ext;
    }
}
