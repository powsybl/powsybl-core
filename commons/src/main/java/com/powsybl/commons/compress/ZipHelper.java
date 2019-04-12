/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.compress;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ZipHelper {

    /**
     * If the file is in .gz(detected by last 3 characters) format, the method decompresses .gz file first.
     * @param baseDir the base directory contaions files to zip
     * @param fileNames the files to be added in zip
     * @return bytes in zip format
     */
    public static byte[] archiveFilesToZipBytes(Path baseDir, List<String> fileNames) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            for (String file : fileNames) {
                boolean isGzFile = file.endsWith(".GZ") || file.endsWith(".gz");
                ZipArchiveEntry entry;
                if (isGzFile) {
                    entry = new ZipArchiveEntry(file.substring(0, file.length() - 3));
                } else {
                    entry = new ZipArchiveEntry(file);
                }
                zos.putArchiveEntry(entry);
                try (InputStream inputStream = isGzFile ? new GZIPInputStream(Files.newInputStream(baseDir.resolve(file)))
                        : Files.newInputStream(baseDir.resolve(file))) {
                    IOUtils.copy(inputStream, zos);
                }
                zos.closeArchiveEntry();
            }
            zos.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            baos.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }

    public static byte[] archiveFilesToZipBytes(Path workingDir, String... fileNames) {
        return archiveFilesToZipBytes(workingDir, Arrays.asList(fileNames));
    }

    private ZipHelper() {
    }
}
