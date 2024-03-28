/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.compress;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public class ZipPackager {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

    /**
     * If the path to file is null or not exists, the method does nothing.
     * If the file is in .gz(detected by last 3 characters) format, the method decompresses .gz file first.
     * @param path the file to add in zip bytes
     * @return a reference to this object
     */
    public ZipPackager addPath(@Nullable Path path) {
        if (path == null || !Files.exists(path)) {
            return this;
        }
        String filename = path.getFileName().toString();
        boolean isGzFile = filename.toLowerCase().endsWith(".gz");
        ZipArchiveEntry entry;
        if (isGzFile) {
            entry = new ZipArchiveEntry(filename.substring(0, filename.length() - 3));
        } else {
            entry = new ZipArchiveEntry(filename);
        }
        try {
            zos.putArchiveEntry(entry);
            try (InputStream inputStream = isGzFile ? new GZIPInputStream(Files.newInputStream(path))
                    : Files.newInputStream(path)) {
                IOUtils.copy(inputStream, zos);
            }
            zos.closeArchiveEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    /**
     * @param baseDir the base directory of all files
     * @param filenames all files to add in zip bytes
     * @return a reference to this object
     */
    public ZipPackager addPaths(Path baseDir, List<String> filenames) {
        Objects.requireNonNull(baseDir);
        Objects.requireNonNull(filenames);
        filenames.forEach(name -> addPath(baseDir.resolve(name)));
        return this;
    }

    /**
     * @param baseDir the base directory of all files
     * @param filenames all files to add in zip bytes
     * @return a reference to this object
     */
    public ZipPackager addPaths(Path root, String... filenames) {
        return addPaths(root, Arrays.asList(filenames));
    }

    /**
     * Key as zip entry name. Both key and content must not be null.
     * @param key
     * @param content
     * @return a reference to this object
     */
    public ZipPackager addString(String key, String content) {
        return addString(key, content, StandardCharsets.UTF_8);
    }

    /**
     * Key as zip entry name. Both key and content must not be null.
     * @param key
     * @param content
     * @param charset be used to encode content
     * @return a reference to this object
     */
    public ZipPackager addString(String key, String content, Charset charset) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(content);
        Objects.requireNonNull(charset);
        return addBytes(key, content.getBytes(charset));
    }

    /**
     * Key as zip entry name. Both key and bytes must not be null.
     * @param key
     * @param bytes
     * @return a reference to this object
     */
    public ZipPackager addBytes(String key, byte[] bytes) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(bytes);
        ZipArchiveEntry entry = new ZipArchiveEntry(key);
        try {
            zos.putArchiveEntry(entry);
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                IOUtils.copy(inputStream, zos);
            }
            zos.closeArchiveEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    /**
     * Should only be called once.
     * @return an array of zip bytes
     */
    public byte[] toZipBytes() {
        try {
            zos.close();
            baos.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }

    /**
     * If the file is in .gz(detected by last 3 characters) format, the method decompresses .gz file first.
     * @param baseDir the base directory contaions files to zip
     * @param fileNames the files to be added in zip
     * @return bytes in zip format
     */
    public static byte[] archiveFilesToZipBytes(Path baseDir, List<String> fileNames) {
        return new ZipPackager().addPaths(baseDir, fileNames).toZipBytes();
    }

    public static byte[] archiveFilesToZipBytes(Path workingDir, String... fileNames) {
        return archiveFilesToZipBytes(workingDir, Arrays.asList(fileNames));
    }

    /**
     * Generates a zip file's bytes
     * @param bytesByName map's {@literal key} as entry name, {@literal value} as content in the zip file
     * @return a zip file in bytes
     */
    public static byte[] archiveBytesByNameToZipBytes(Map<String, byte[]> bytesByName) {
        Objects.requireNonNull(bytesByName);
        ZipPackager zipPackager = new ZipPackager();
        bytesByName.forEach(zipPackager::addBytes);
        return zipPackager.toZipBytes();
    }
}
