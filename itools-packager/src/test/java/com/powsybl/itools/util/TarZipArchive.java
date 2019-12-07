/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools.util;

import com.powsybl.itools.CompressionType;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public class TarZipArchive {
    private static final Logger LOGGER = LoggerFactory.getLogger(TarZipArchive.class);

    private static String windowsSeparator = "\\";
    private static String jimfsSeparator;

    protected TarZipArchive() {
    }

    public static void setJimfsSeparator(String fs) {
        jimfsSeparator = fs;
    }

    public String getJimfsSeparator() {
        return this.jimfsSeparator;
    }

    public static void decompressFiles(Path zipFilePath, Path unzipPath, String jimfsSeparator) throws IOException {
        Objects.requireNonNull(zipFilePath);
        Objects.requireNonNull(unzipPath);

        setJimfsSeparator(jimfsSeparator);

        CompressionType compressionType = CompressionType.compareFileExtension(String.valueOf(zipFilePath));
        Objects.requireNonNull(compressionType);

        LOGGER.info("decompress '{}' file into '{}' directory :", zipFilePath, unzipPath);

        if (!(Files.exists(unzipPath))) {
            Files.createDirectories(unzipPath);
        }

        try (DataInputStream os = new DataInputStream(Files.newInputStream(zipFilePath))) {
            switch (compressionType) {
                case ZIP:
                    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(os)) {
                        new TarZipArchive().uncompressZip(unzipPath, zis);
                    }
                    break;

                case GZIP:
                case BZIP2:
                    try (BufferedInputStream bis = new BufferedInputStream(os)) {
                        TarArchiveInputStream tis = null;
                        if (compressionType.equals(CompressionType.GZIP)) {
                            tis = new TarArchiveInputStream(new GZIPInputStream(bis));
                        } else { // case of BZIP2
                            tis = new TarArchiveInputStream(new BZip2CompressorInputStream(bis));
                        }

                        new TarZipArchive().uncompressTar(unzipPath, tis);
                        tis.close();
                    }
                    break;

                default:
                    throw new AssertionError(compressionType.getExtension());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void uncompressZip(Path unzipLocation, ZipArchiveInputStream tis) throws IOException {
        ZipArchiveEntry entry = null;
        while ((entry = tis.getNextZipEntry()) != null) {
            String name = entry.getName();

            if (name != null && name.contains(windowsSeparator)) {
                name = name.replace(windowsSeparator, getJimfsSeparator());
            }
            Path filePath = unzipLocation.resolve(name);

            if (name.endsWith(getJimfsSeparator())) {
                Files.createDirectories(filePath);
            } else {
                Files.copy(tis, filePath);
            }
        }
    }

    public void uncompressTar(Path unzipLocation, TarArchiveInputStream tis) throws IOException {
        TarArchiveEntry entry = null;
        while ((entry = tis.getNextTarEntry()) != null) {
            String name = entry.getName();
            if (name != null && name.contains(windowsSeparator)) {
                name = name.replace(windowsSeparator, getJimfsSeparator());
            }
            Path filePath = unzipLocation.resolve(name);

            if (name.endsWith(getJimfsSeparator())) {
                Files.createDirectories(filePath);
            } else {
                Files.copy(tis, filePath);
            }
        }
    }

    // -Not used-
    public void findPackageFile(Path virtualTargetDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(virtualTargetDir, "*.zip")) {
            for (Path entry : stream) {
                System.out.println(String.format(" Found packageFile : '%s' (%db) in '%s' repertoire.",
                        entry.getFileName(), Files.readAllBytes(entry).length, virtualTargetDir.toAbsolutePath()));
            }
        }
    }
}
