/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Mojo(name = "package", requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
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

    @Parameter(defaultValue = "2")
    private Integer mpiTasks;

    @Parameter
    private String[] mpiHosts;

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

    public void createPackage(Path dir, final Path baseDir, String fileName, String packageName) throws IOException {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(baseDir);
        Objects.requireNonNull(fileName);

        String extension = compareFileExtension(fileName);
        String newFileName = fileName;
        // invalid extension
        if (extension.equals("")) {
            extension = ".zip";
            newFileName = packageName + extension;
        }

        Path packageFilePath = baseDir.resolve(newFileName);
        FileOutputStream fos = new FileOutputStream(String.valueOf(packageFilePath));

        getLog().info("Generate package file : " + newFileName);

        switch (extension) {
            case ".zip":
                try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {
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
                break;

            case ".tar.gz":
                try (TarArchiveOutputStream tos = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)))) {
                    tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to store big number in the archive.
                    tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU); // to store long file names in the archive.

                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            TarArchiveEntry entry = new TarArchiveEntry(new File(file + "/"));
                            entry.setName(baseDir.relativize(file).toString());
                            entry.setSize(file.toFile().length());
                            entry.setMode(0770);
                            tos.putArchiveEntry(entry);
                            Files.copy(file, tos);
                            tos.closeArchiveEntry();

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            tos.putArchiveEntry(new TarArchiveEntry(baseDir.relativize(dir).toString() + "/"));
                            tos.closeArchiveEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                break;

            case ".tar.bz2":
                try (TarArchiveOutputStream tos = new TarArchiveOutputStream(new BZip2CompressorOutputStream(new BufferedOutputStream(fos)))) {
                    tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to store big number in the archive.
                    tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU); // to store long file names in the archive.

                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            TarArchiveEntry entry = new TarArchiveEntry(new File(file + "/"));
                            entry.setName(baseDir.relativize(file).toString());
                            entry.setSize(file.toFile().length());
                            entry.setMode(0770);
                            tos.putArchiveEntry(entry);
                            Files.copy(file, tos);
                            tos.closeArchiveEntry();

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            tos.putArchiveEntry(new TarArchiveEntry(baseDir.relativize(dir).toString() + "/"));
                            tos.closeArchiveEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                break;
        }
    }

    private static String compareFileExtension(String name) {
        String ext = "";
        if (name.endsWith(".zip")) {
            ext = ".zip";
        } else if (name.endsWith(".tar.gz")) {
            ext = ".tar.gz";
        } else if (name.endsWith(".tar.bz2")) {
            ext = ".tar.bz2";
        }

        return ext;
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
        writer.write("mpi_tasks=");
        writer.write(Integer.toString(mpiTasks));
        writer.newLine();
        writer.write("mpi_hosts=");
        if (mpiHosts == null) {
            writer.write("localhost");
        } else {
            writer.write(Arrays.stream(mpiHosts).collect(Collectors.joining(",")));
        }
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
            for (String script : Arrays.asList("itools", "itools.bat", "powsyblsh", "tools-mpi-task.sh")) {
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
            try (BufferedWriter writer = Files.newBufferedWriter(etcDir.resolve("itools.conf"), StandardCharsets.UTF_8)) {
                writeItoolsConf(writer);
            }
            copyFiles(copyToEtc, etcDir);

            // create lib dir
            Path libDir = packageDir.resolve("lib");
            Files.createDirectories(libDir);
            copyFiles(copyToLib, libDir);

            // create package
            String archiveNameNotNull = archiveName != null ? archiveName : packageNameNotNull;
            int lastIndexOf = archiveNameNotNull.lastIndexOf(".");
            if (lastIndexOf < 0) {
                // file extension: zip by default
                archiveNameNotNull += ".zip";
            }

            createPackage(packageDir, targetDir, archiveNameNotNull, packageNameNotNull);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
