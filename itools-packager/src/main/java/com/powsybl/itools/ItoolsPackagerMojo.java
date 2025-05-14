/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.itools;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@Mojo(name = "package-zip", requiresDependencyCollection = ResolutionScope.RUNTIME, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ItoolsPackagerMojo extends AbstractMojo {

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter
    private String packageName;

    @Parameter
    private String archiveName;

    @Parameter(defaultValue = "8G")
    private String javaXmx;

    @Parameter(defaultValue = "config")
    private String configName;

    public static class CopyTo {

        private File[] files;

        public File[] getFiles() {
            return files;
        }

        public void setFiles(File[] files) {
            this.files = files;
        }
    }

    @Parameter(defaultValue = "zip")
    private String packageType;

    @Parameter
    private CopyTo copyToBin;

    @Parameter
    private CopyTo copyToLib;

    @Parameter
    private CopyTo copyToEtc;

    private void zip(Path dir, Path baseDir, Path zipFilePath) throws IOException {
        getLog().info("Zip package");
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(Files.newOutputStream(zipFilePath))) {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    ZipArchiveEntry entry = new ZipArchiveEntry(baseDir.relativize(file).toString());
                    if (Files.isExecutable(file)) {
                        entry.setUnixMode(0100770);
                    } else {
                        entry.setUnixMode(0100660);
                    }
                    zos.putArchiveEntry(entry);
                    Files.copy(file, zos);
                    zos.closeArchiveEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putArchiveEntry(new ZipArchiveEntry(baseDir.relativize(dir).toString() + "/"));
                    zos.closeArchiveEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void targz(Path dir, Path baseDir, Path zipFilePath) throws IOException {
        getLog().info("Targz package");
        try (TarArchiveOutputStream zos =
                new TarArchiveOutputStream(new GzipCompressorOutputStream(Files.newOutputStream(zipFilePath)))) {
            zos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX); // allow long file paths
            zos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX); // allow large numbers (for instance a big GID)
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), baseDir.relativize(file).toString());
                    if (Files.isExecutable(file)) {
                        entry.setMode(0100770);
                    } else {
                        entry.setMode(0100660);
                    }
                    zos.putArchiveEntry(entry);
                    Files.copy(file, zos);
                    zos.closeArchiveEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putArchiveEntry(new TarArchiveEntry(baseDir.relativize(dir).toString() + "/"));
                    zos.closeArchiveEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }

    }

    private void copyFiles(CopyTo copyTo, Path destDir) {
        if (copyTo != null) {
            for (File file : copyTo.getFiles()) {
                Path path = file.toPath();
                if (Files.exists(path)) {
                    getLog().info("Copy file " + path + " to " + destDir);
                    try {
                        Files.copy(path, destDir.resolve(path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                } else {
                    getLog().warn("File " + path + " not found");
                }
            }
        }
    }

    private void writeItoolsConf(BufferedWriter writer) throws IOException {
        writer.write("#powsybl_config_dirs=");
        writer.newLine();
        writer.write("powsybl_config_name=");
        writer.write(configName);
        writer.newLine();
        writer.write("java_xmx=");
        writer.write(javaXmx);
        writer.newLine();
    }

    private void addLicenseFiles(Path packageDir) {
        // List of the license files to copy
        List<String> licenseFiles = Arrays.asList("LICENSE.txt", "NOTICE", "NOTICE.txt", "LICENSES.txt");

        // Get the root directory of the project
        File projectRoot = project.getBasedir();
        if (project.getParent() != null) {
            projectRoot = projectRoot.getParentFile();
        }

        // Copy each license file to the package directory
        for (String licenseFile : licenseFiles) {
            Path sourcePath = Paths.get(projectRoot.getPath(), licenseFile);
            if (Files.exists(sourcePath)) {
                try {
                    getLog().info("Copy license file " + sourcePath + " to " + packageDir);
                    Files.copy(sourcePath, packageDir.resolve(licenseFile), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    getLog().warn("Failed to copy license file " + sourcePath + ": " + e.getMessage());
                }
            } else {
                getLog().warn("License file " + sourcePath + " not found");
            }
        }
    }

    @Override
    public void execute() {
        try {
            String packageNameNotNull = packageName != null ? packageName : project.getBuild().getFinalName();
            Path targetDir = Paths.get(project.getBuild().getDirectory());
            Path packageDir = targetDir.resolve(packageNameNotNull);

            // copy jars
            Path javaDir = packageDir.resolve("share").resolve("java");
            Files.createDirectories(javaDir);
            for (Artifact artifact : project.getArtifacts()) {
                Path jar = artifact.getFile().toPath();
                getLog().info("Add jar " + jar + " to package");
                Files.copy(jar, javaDir.resolve(jar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }

            // create bin directory and add scripts
            Path binDir = packageDir.resolve("bin");
            Files.createDirectories(binDir);
            for (String script : Arrays.asList("itools", "itools.bat", "powsyblsh")) {
                getLog().info("Add script " + script + " to package");
                Path file = binDir.resolve(script);
                Files.copy(ItoolsPackagerMojo.class.getResourceAsStream("/" + script), file, StandardCopyOption.REPLACE_EXISTING);
                FileStore fileStore = Files.getFileStore(file);
                if (fileStore.supportsFileAttributeView(PosixFileAttributeView.class)) {
                    Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ,
                                                                PosixFilePermission.OWNER_WRITE,
                                                                PosixFilePermission.OWNER_EXECUTE,
                                                                PosixFilePermission.GROUP_READ,
                                                                PosixFilePermission.GROUP_EXECUTE);
                    Files.setPosixFilePermissions(file, perms);
                }
            }
            copyFiles(copyToBin, binDir);

            // create etc directory
            Path etcDir = packageDir.resolve("etc");
            Files.createDirectories(etcDir);
            Files.copy(ItoolsPackagerMojo.class.getResourceAsStream("/logback-itools.xml"), etcDir.resolve("logback-itools.xml"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(ItoolsPackagerMojo.class.getResourceAsStream("/logback-powsyblsh.xml"), etcDir.resolve("logback-powsyblsh.xml"), StandardCopyOption.REPLACE_EXISTING);
            try (BufferedWriter writer = Files.newBufferedWriter(etcDir.resolve("itools.conf"), StandardCharsets.UTF_8)) {
                writeItoolsConf(writer);
            }
            copyFiles(copyToEtc, etcDir);

            // create lib dir
            Path libDir = packageDir.resolve("lib");
            Files.createDirectories(libDir);
            copyFiles(copyToLib, libDir);

            // Add licenses
            addLicenseFiles(packageDir);

            String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
            if (packageType.equalsIgnoreCase("zip")) {
                zip(packageDir, targetDir, targetDir.resolve(archiveNameNotNull + ".zip"));
            } else if (packageType.equalsIgnoreCase("tgz")) {
                targz(packageDir, targetDir, targetDir.resolve(archiveNameNotNull + ".tgz"));
            } else {
                throw new IllegalArgumentException("Unknown filetype '" + packageType + "': should be either zip or tgz");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
