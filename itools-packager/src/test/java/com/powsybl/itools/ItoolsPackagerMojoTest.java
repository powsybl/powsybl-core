/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.itools;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.eclipse.aether.DefaultRepositorySystemSession;

import java.io.File;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class ItoolsPackagerMojoTest extends AbstractMojoTestCase {

    private static final File BASEDIR = new File("src/test/resources/test-maven-project/");
    private static final File POM_XML = new File(BASEDIR, "pom.xml");
    private static final File TARGET = new File(BASEDIR, "target");
    public static final String DEFAULT_PACKAGE_NAME = "itools-packager-test-project-1.0.0-SNAPSHOT";

    ItoolsPackagerMojo mojo;
    PlexusConfiguration configuration;

    @Override
    protected void setUp()
            throws Exception {
        // required
        super.setUp();
        MavenProject project = readMavenProject();
        MavenSession session = newMavenSession(project);
        MojoExecution execution = newMojoExecution("package-zip");
        mojo = (ItoolsPackagerMojo) lookupConfiguredMojo(session, execution);
        configuration = new DefaultPlexusConfiguration("configuration");
    }

    @Override
    protected void tearDown()
            throws Exception {
        // required
        super.tearDown();
        FileUtils.deleteDirectory(TARGET); // cleanup
    }

    protected MavenProject readMavenProject() throws Exception {
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setBaseDirectory(BASEDIR);
        ProjectBuildingRequest projectBuildingRequest = request.getProjectBuildingRequest();
        projectBuildingRequest.setRepositorySession(new DefaultRepositorySystemSession());
        MavenProject project = lookup(ProjectBuilder.class).build(POM_XML, projectBuildingRequest).getProject();
        assertNotNull(project);
        return project;
    }

    public void testDefaultConfiguration() throws Exception {
        super.configureMojo(mojo, configuration);
        assertNotNull(mojo);
        mojo.execute();
        assertTrue(new File(TARGET, DEFAULT_PACKAGE_NAME + ".zip").exists());
    }

    public void testPackageTypeTgz() throws Exception {
        configuration.addChild("packageType", "tgz");
        super.configureMojo(mojo, configuration);
        assertNotNull(mojo);
        mojo.execute();
        assertTrue(new File(TARGET, DEFAULT_PACKAGE_NAME + ".tgz").exists());
    }
}
