/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public final class BrowseFileTree {
    private BrowseFileTree() {
    }

    public static void createArchiveZip(Path dir, final Path baseDir, ZipArchiveOutputStream os) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                ZipArchiveEntry entry = new ZipArchiveEntry(baseDir.relativize(file).toString());
                if (Files.isExecutable(file)) {
                    entry.setUnixMode(0100770);
                } else {
                    entry.setUnixMode(0100660);
                }
                os.putArchiveEntry(entry);
                Files.copy(file, os);
                os.closeArchiveEntry();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                os.putArchiveEntry(new ZipArchiveEntry(baseDir.relativize(dir).toString() + File.separator));
                os.closeArchiveEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void createArchiveTar(Path dir, final Path baseDir, TarArchiveOutputStream os) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                TarArchiveEntry entry = new TarArchiveEntry(new File(file + File.separator));
                entry.setName(baseDir.relativize(file).toString());
                entry.setSize(file.toFile().length());
                if (Files.isExecutable(file)) {
                    entry.setMode(0770);
                } else {
                    entry.setMode(0660);
                }
                os.putArchiveEntry(entry);
                Files.copy(file, os);
                os.closeArchiveEntry();

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                os.putArchiveEntry(new TarArchiveEntry(baseDir.relativize(dir).toString() + File.separator));
                os.closeArchiveEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
