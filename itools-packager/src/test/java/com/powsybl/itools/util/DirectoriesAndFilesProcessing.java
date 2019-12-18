/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools.util;

import com.powsybl.itools.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static com.powsybl.itools.util.TarZipArchive.decompressFiles;
import static java.nio.file.Files.exists;
import static org.junit.Assert.assertFalse;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public class DirectoriesAndFilesProcessing {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoriesAndFilesProcessing.class);

    private String uncompressedName = "uncompressed";
    private String jimfsSeparator;
    private static String windowsSeparator = "\\";

    public DirectoriesAndFilesProcessing(String fs) {
        this.jimfsSeparator = fs;
    }

    public boolean check(final Path virtualWorkDir, String archiveName, String packageNameNotNull) throws IOException {
        boolean booleanDelete = true;

        Objects.requireNonNull(virtualWorkDir, "name is null");

        // file extension: ".zip" by default
        String newArchiveName = archiveName.lastIndexOf('.') < 0 ? archiveName + CompressionType.ZIP.getExtension() : archiveName;

        // Decompress zip or gzip or bzip2 file
        Path zipFilePath = virtualWorkDir.resolve(newArchiveName);
        Path unzipPath = virtualWorkDir.resolve(uncompressedName);

        decompressFiles(zipFilePath, unzipPath, jimfsSeparator);

        // compare 2 directories: '/work/resources/powsybl' and '/work/resources/uncompressed/powsybl'
        boolean bool = new CompareDir().compareTwoVirtualDirectories(virtualWorkDir, unzipPath, packageNameNotNull);
        String message1 = virtualWorkDir + jimfsSeparator + packageNameNotNull;
        String message2 = unzipPath + jimfsSeparator + packageNameNotNull;
        if (bool) {
            LOGGER.info("Identical comparison of directories between '{}' and '{}'", message1, message2);
        } else {
            LOGGER.info("Failure : Different comparison of directories between '{}' and '{}'", message1, message2);
            return false;
        }

        // Do not delete the '/work/resources/powsybl' directory for further tests
        // Clean zip or gz or bz2 file
        LOGGER.info("Clean '{}' file and clean '{}' directory", zipFilePath.getFileName(), unzipPath);
        if (!deleteDirAndFiles(zipFilePath)) {
            return false;
        }

        // Clean '/work/resources/powsybl/uncompressed' directory
        if (!deleteDirAndFiles(unzipPath)) {
            return false;
        }

        return booleanDelete;
    }

    public boolean deleteDirAndFiles(Path files) throws IOException {
        boolean booleanDelete = true;

        Files.walkFileTree(files, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });

        try {
            Files.delete(files);
        } catch (NoSuchFileException ex) {
            LOGGER.info("'{}' is deleted.", files);
        } catch (DirectoryNotEmptyException ex) {
            LOGGER.error("%nFailure : {} not empty", files);
            booleanDelete = false;
        } catch (IOException ex) {
            // File permission problems
            LOGGER.error("{}", ex);
            booleanDelete = false;
        }

        assertFalse("Directory still exists", exists(files));
        return booleanDelete;
    }

    public static void copyIntoVirtualWorkDir(final Path fromPath, final Path toPath, String jimfsSeparator) throws IOException {
        LOGGER.info("Copying files and directories from '{}' to '{}' ", fromPath, toPath);

        Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
                String virtualWorkDir = fromPath.relativize(path).toString();

                if (virtualWorkDir != null && virtualWorkDir.contains(windowsSeparator)) {
                    virtualWorkDir = virtualWorkDir.replace(windowsSeparator, jimfsSeparator);
                }
                Path targetPath = toPath.resolve(virtualWorkDir);
                Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                String virtualWorkDir = fromPath.relativize(dir).toString();

                if (virtualWorkDir != null && virtualWorkDir.contains(windowsSeparator)) {
                    virtualWorkDir = virtualWorkDir.replace(windowsSeparator, jimfsSeparator);
                }
                Path targetPath = toPath.resolve(virtualWorkDir);

                if (!exists(targetPath)) {
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
