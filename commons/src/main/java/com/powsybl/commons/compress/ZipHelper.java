/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.compress;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ZipHelper {

    public static byte[] archiveFilesToZipBytes(Path workingDir, List<String> fileNames) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            for (String file : fileNames) {
                ZipArchiveEntry entry = new ZipArchiveEntry(file);
                zos.putArchiveEntry(entry);
                Files.copy(workingDir.resolve(file), zos);
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
