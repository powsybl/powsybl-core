/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools.plugin;

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
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Mojo(name = "package-zip", requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ItoolsMojo extends AbstractMojo {

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter
    private String bundleName;

    @Parameter(defaultValue = "8G")
    private String javaXmx;

    @Parameter
    private File[] binDirectories;

    @Parameter
    private File[] libDirectories;

    private static void zip(Path folder, Path zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(folder.relativize(dir).toString() + "/"));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void copyDir(Path srcDir, Path destDir) {
        try (Stream<Path> stream = Files.list(srcDir)) {
            stream.forEach(file -> {
                if (Files.isRegularFile(file)) {
                    try {
                        Files.copy(file, destDir.resolve(file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeItoolsConf(BufferedWriter writer) throws IOException {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(javaXmx);
        writer.write("#itools_cache_dir=");
        writer.newLine();
        writer.write("#itools_config_dir=");
        writer.newLine();
        writer.write("itools_config_name=config");
        writer.newLine();
        writer.write("java_xmx=");
        writer.write(javaXmx);
        writer.newLine();
        writer.write("mpi_tasks=3");
        writer.newLine();
        writer.write("mpi_hosts=localhost");
        writer.newLine();
    }

    @Override
    public void execute() {
        try {
            String bundleNameNotNull = bundleName != null ? bundleName : project.getBuild().getFinalName();
            Path targetDir = Paths.get(project.getBuild().getDirectory());
            Path bundleDir = targetDir.resolve(bundleName);

            // copy jars
            Path javaDir = bundleDir.resolve("share").resolve("java");
            Files.createDirectories(javaDir);
            for (Artifact artifact : project.getArtifacts()) {
                Path jar = artifact.getFile().toPath();
                getLog().info("Add jar " + jar + " to bundle");
                Files.copy(jar, javaDir.resolve(jar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }

            // create bin directory and add scripts
            Path binDir = bundleDir.resolve("bin");
            Files.createDirectories(binDir);
            for (String script : Arrays.asList("itools", "itools.bat", "powsyblsh")) {
                getLog().info("Add script " + script + " to bundle");
                Path file = binDir.resolve(script);
                Files.copy(ItoolsMojo.class.getResourceAsStream("/" + script), file, StandardCopyOption.REPLACE_EXISTING);
                Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ,
                                                            PosixFilePermission.OWNER_WRITE,
                                                            PosixFilePermission.OWNER_EXECUTE,
                                                            PosixFilePermission.GROUP_READ,
                                                            PosixFilePermission.GROUP_EXECUTE);
                Files.setPosixFilePermissions(file, perms);
            }
            if (binDirectories != null) {
                for (File dir : binDirectories) {
                    copyDir(dir.toPath(), binDir);
                }
            }

            // create etc directory
            Path etcDir = bundleDir.resolve("etc");
            Files.createDirectories(etcDir);
            Files.copy(ItoolsMojo.class.getResourceAsStream("/logback-itools.xml"), etcDir.resolve("logback-itools.xml"), StandardCopyOption.REPLACE_EXISTING);
            try (BufferedWriter writer = Files.newBufferedWriter(etcDir.resolve("itools.conf"), StandardCharsets.UTF_8)) {
                writeItoolsConf(writer);
            }

            // create lib dir
            Path libDir = bundleDir.resolve("lib");
            Files.createDirectories(libDir);
            if (libDirectories != null) {
                for (File dir : libDirectories) {
                    copyDir(dir.toPath(), libDir);
                }
            }

            getLog().info("Zip bundle");
            zip(bundleDir, targetDir.resolve(bundleNameNotNull + ".zip"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
