/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.itools;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@MojoTest
class ItoolsPackagerMojoTest {

    public static final String DEFAULT_PACKAGE_NAME = "itools-packager-test-project-1.0.0-SNAPSHOT";

    @Inject
    MavenProject project;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        Build build = new Build();
        build.setFinalName(DEFAULT_PACKAGE_NAME);
        String target = tempDir.resolve(UUID.randomUUID().toString()).toString();
        build.setDirectory(target);
        Mockito.when(project.getBuild()).thenReturn(build);
    }

    @Test
    @Basedir("src/test/resources/test-maven-project/")
    @InjectMojo(goal = "package-zip")
    void testDefaultConfiguration(ItoolsPackagerMojo mojo) {
        mojo.execute();
        String target = project.getBuild().getDirectory();
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + ".zip").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/LICENSE.txt").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/bin/itools").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/bin/itools.bat").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/bin/powsyblsh").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/etc/itools.conf").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/etc/logback-itools.xml").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/etc/logback-powsyblsh.xml").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/lib").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/lib").isDirectory());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/share/java").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/share/java").isDirectory());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/THIRD-PARTY.txt").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/file.txt").exists());
        assertFalse(new File(target, DEFAULT_PACKAGE_NAME + "/not_exists.txt").exists());
    }

    @Test
    @Basedir("src/test/resources/test-maven-project/")
    @InjectMojo(goal = "package-zip")
    @MojoParameter(name = "packageType", value = "tgz")
    void testTgzConfiguration(ItoolsPackagerMojo mojo) {
        mojo.execute();
        String target = project.getBuild().getDirectory();
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + ".tgz").exists());
    }

    @Test
    @Basedir("src/test/resources/test-maven-project/")
    @InjectMojo(goal = "package-zip")
    @MojoParameter(name = "packageType", value = "someUnsupportedValue")
    void testUnknownPackageType(ItoolsPackagerMojo mojo) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, mojo::execute);
        assertEquals("Unknown filetype 'someUnsupportedValue': should be either zip or tgz", e.getMessage());
    }

    @Test
    @Basedir("src/test/resources/test-maven-project/")
    @InjectMojo(goal = "package-zip")
    @MojoParameter(name = "packageName", value = "myOwnPackageName")
    void testPackageNameConfiguration(ItoolsPackagerMojo mojo) {
        mojo.execute();
        String target = project.getBuild().getDirectory();
        assertTrue(new File(target, "myOwnPackageName" + ".zip").exists());
    }

    @Test
    @Basedir("src/test/resources/test-maven-project-configured-lic-files/")
    @InjectMojo(goal = "package-zip")
    void testCustomLicenseConfiguration(ItoolsPackagerMojo mojo) {
        mojo.execute();
        String target = project.getBuild().getDirectory();
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + ".zip").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/myLic.txt").exists());
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/my3rdParties.txt").exists());
    }

    @Test
    @Basedir("src/test/resources/test-maven-project-lic-not-found/")
    @InjectMojo(goal = "package-zip")
    void testLicenseFilesNotFound(ItoolsPackagerMojo mojo) {
        mojo.execute();
        String target = project.getBuild().getDirectory();
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + ".zip").exists());
        assertFalse(new File(target, DEFAULT_PACKAGE_NAME + "/LICENSE.txt").exists());
        assertFalse(new File(target, DEFAULT_PACKAGE_NAME + "/THIRD-PARTY.txt").exists());
    }

    @Test
    @Basedir("src/test/resources/test-maven-project-lic-parent-dir/submodule/")
    @InjectMojo(goal = "package-zip")
    void testLicenseFilesInParent(ItoolsPackagerMojo mojo) {
        mojo.execute();
        String target = project.getBuild().getDirectory();
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + ".zip").exists());
        assertFalse(new File(target, DEFAULT_PACKAGE_NAME + "/LICENSE.txt").exists()); // wrong file specified in pom.xml
        assertTrue(new File(target, DEFAULT_PACKAGE_NAME + "/THIRD-PARTY.txt").exists());
    }

}
