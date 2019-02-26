/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.compress;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class TarHelper {

    public static byte[] archiveFilesToBytes(Path workingDir, List<String> fileNames) {
        // maybe workingDir would be used repeatedly
        String randomId = UUID.randomUUID().toString();
        try {
            String tarFileName = archiveFiles(workingDir, randomId, fileNames);
            return convertTarFileToBytes(workingDir, tarFileName);
        } catch (IOException e) {
            throw new PowsyblException(e);
        }
    }

    public static byte[] archiveFilesToBytes(Path workingDir, String... fileNames) {
        return archiveFilesToBytes(workingDir, Arrays.asList(fileNames));
    }

    private static String archiveFiles(Path workingDir, String randomId, List<String> files) throws IOException {
        String tmgTarName = "tmp-tar-" + randomId + ".tar";
        try (TarArchiveOutputStream taos = new TarArchiveOutputStream(new FileOutputStream(workingDir.resolve(tmgTarName).toFile()))) {
            for (int i = 0; i < files.size(); i++) {
                addFile(taos, workingDir, files.get(i));
            }
            taos.flush();
            taos.finish();
        }
        return tmgTarName;
    }

    private static void addFile(TarArchiveOutputStream taos, Path workingDir, String fileName) throws IOException {
        Path path = workingDir.resolve(fileName);
        ArchiveEntry archiveEntry = taos.createArchiveEntry(path.toFile(), fileName);
        taos.putArchiveEntry(archiveEntry);
        try (InputStream is = Files.newInputStream(path)) {
            IOUtils.copy(is, taos);
        }
        taos.closeArchiveEntry();
    }

    private static byte[] convertTarFileToBytes(Path workingDir, String tarGzFilename) throws IOException {
        FileInputStream fis = new FileInputStream(workingDir.resolve(tarGzFilename).toFile());
        return ByteStreams.toByteArray(fis);
    }

    private TarHelper() {
    }
}
