/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.itools;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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
import java.time.OffsetDateTime;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    @Parameter(defaultValue = "${project.build.outputTimestamp}")
    private String outputTimestamp;

    public static class CopyTo {

        private File[] files;

        public File[] getFiles() {
            return files;
        }

        public void setFiles(File[] files) {
            this.files = files;
        }
    }

    @Parameter
    private CopyTo copyToBin;

    @Parameter
    private CopyTo copyToLib;

    @Parameter
    private CopyTo copyToEtc;

    private static void zip(Path dir, Path baseDir, Path zipFilePath, FileTime reproducibleFileTime)
            throws IOException {
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
                    if (reproducibleFileTime != null) {
                        entry.setTime(reproducibleFileTime);
                    }
                    zos.putArchiveEntry(entry);
                    Files.copy(file, zos);
                    zos.closeArchiveEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    ZipArchiveEntry entry = new ZipArchiveEntry(baseDir.relativize(dir).toString() + "/");
                    if (reproducibleFileTime != null) {
                        entry.setTime(reproducibleFileTime);
                    }
                    zos.putArchiveEntry(entry);
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

            getLog().info("Zip package");
            String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
            // non null if reproducible build
            FileTime reproducibleFileTime = parseOutputTimestamp();
            zip(packageDir, targetDir, targetDir.resolve(archiveNameNotNull + ".zip"), reproducibleFileTime);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //lifted from spring-boot-maven-plugin RepackageMojo.java (apache license)
    private FileTime parseOutputTimestamp() {
        // Maven ignores a single-character timestamp as it is "useful to override a full
        // value during pom inheritance"
        if (this.outputTimestamp == null || this.outputTimestamp.length() < 2) {
            return null;
        }
        return FileTime.from(getOutputTimestampEpochSeconds(), TimeUnit.SECONDS);
    }

    private long getOutputTimestampEpochSeconds() {
        try {
            return Long.parseLong(this.outputTimestamp);
        } catch (NumberFormatException ex) {
            return OffsetDateTime.parse(this.outputTimestamp).toInstant().getEpochSecond();
        }
    }

}
