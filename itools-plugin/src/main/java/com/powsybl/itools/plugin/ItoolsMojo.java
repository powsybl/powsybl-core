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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;
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

    @Override
    public void execute() {
        try {
            String bundleName = project.getBuild().getFinalName();
            Path targetDir = Paths.get(project.getBuild().getDirectory());
            Path bundleDir = targetDir.resolve(bundleName);

            // copy jars
            Path javaDir = bundleDir.resolve("share").resolve("java");
            Files.createDirectories(javaDir);
            for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
                Path jar = artifact.getFile().toPath();
                getLog().info("Add jar " + jar + " to bundle");
                Files.copy(jar, javaDir.resolve(jar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }

            // create bin directory and add scripts
            Path binDir = bundleDir.resolve("bin");
            Files.createDirectories(binDir);
            for (String script : Arrays.asList("itools", "itools.bat", "powsyblsh")) {
                getLog().info("Add script " + script + " to bundle");
                Files.copy(ItoolsMojo.class.getResourceAsStream("/" + script), binDir.resolve(script), StandardCopyOption.REPLACE_EXISTING);
            }

            getLog().info("Zip bundle");
            zip(bundleDir, targetDir.resolve(bundleName + ".zip"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
