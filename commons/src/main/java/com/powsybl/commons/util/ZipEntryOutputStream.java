/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.io.ForwardingOutputStream;

import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
import net.java.truevfs.comp.zip.ZipOutputStream;

/**
 * OutputStream wrapper to add an entry to a zip file.
 * The rewrite parameter specifies whether append the entry to the existing
 * file or rewrite the whole file.
 * Appending the entry to an existing file that already contains an entry with
 * the same name may produce redundant data in the resulting archive file.
 * Rewriting the zip file each time an entry is added may cause performance
 * issues
 *
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public final class ZipEntryOutputStream extends ForwardingOutputStream<ZipOutputStream> {

    private final Path zipFilePath;

    private final String fileName;

    private boolean closed;

    private boolean rewrite;

    /**
     * @param zipFilePath the path of the zip file
     * @param fileName the file name of the entry
     * @param rewrite flag to enable zip file rewriting
     * @throws IOException
     */
    public ZipEntryOutputStream(Path zipFilePath, String fileName, boolean rewrite) throws IOException {
        super(newZipOutputStream(zipFilePath, rewrite));

        this.zipFilePath = zipFilePath;
        this.fileName = fileName;
        this.closed = false;
        this.rewrite = rewrite;

        // create new entry
        os.putNextEntry(new ZipEntry(fileName));
    }

    private static ZipOutputStream newZipOutputStream(Path zipFilePath, boolean rewrite) throws IOException {
        if (rewrite) {
            return new ZipOutputStream(Files.newOutputStream(getTmpZipFilePath(zipFilePath)));
        }
        if (Files.exists(zipFilePath)) {
            return new ZipOutputStream(Files.newOutputStream(zipFilePath, StandardOpenOption.APPEND), new ZipFile(zipFilePath));
        } else {
            return new ZipOutputStream(Files.newOutputStream(zipFilePath));
        }
    }

    private static Path getTmpZipFilePath(Path zipFilePath) {
        return zipFilePath.getParent().resolve(zipFilePath.getFileName() + ".tmp");
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            // close new entry
            os.closeEntry();

            // copy existing entries
            if (rewrite && Files.exists(zipFilePath)) {
                try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                    Enumeration<? extends ZipEntry> e = zipFile.entries();
                    while (e.hasMoreElements()) {
                        ZipEntry zipEntry = e.nextElement();
                        if (!zipEntry.getName().equals(fileName)) {
                            os.putNextEntry(zipEntry);
                            try (InputStream zis = zipFile.getInputStream(zipEntry.getName())) {
                                ByteStreams.copy(zis, os);
                            }
                            os.closeEntry();
                        }
                    }
                }
            }

            // close zip
            super.close();

            if (rewrite) {
                // swap with tmp zip
                Path tmpZipFilePath = getTmpZipFilePath(zipFilePath);
                Files.move(tmpZipFilePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            closed = true;
        }
    }

}
