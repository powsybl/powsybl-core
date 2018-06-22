/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server;

import com.powsybl.afs.storage.AbstractAppStorageTest;
import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.ws.client.utils.ClientUtils;
import com.powsybl.afs.ws.client.utils.UserSession;
import com.powsybl.afs.ws.storage.RemoteAppStorage;
import com.powsybl.afs.ws.storage.RemoteListenableAppStorage;
import com.powsybl.commons.exceptions.UncheckedUriSyntaxException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class AppStorageServerTest extends AbstractAppStorageTest {

    @ArquillianResource
    private URL baseUrl;

    private UserSession userSession;

    @Deployment
    public static WebArchive createTestArchive() {
        File[] filesLib = Maven.configureResolver()
                               .useLegacyLocalRepo(true)
                               .withMavenCentralRepo(false)
                               .withClassPathResolution(true)
                               .loadPomFromFile("pom.xml")
                               .importRuntimeDependencies()
                               .resolve("org.mockito:mockito-all",
                                        "com.powsybl:powsybl-afs-mapdb")
                               .withTransitivity()
                               .asFile();

        return ShrinkWrap.create(WebArchive.class, "afs-ws-server-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(AppStorageServerTest.class.getPackage())
                .addAsLibraries(filesLib);
    }

    private URI getRestUri() {
        try {
            return baseUrl.toURI();
        } catch (URISyntaxException e) {
            throw new UncheckedUriSyntaxException(e);
        }
    }

    @Override
    public void setUp() throws Exception {
        userSession = ClientUtils.authenticate(getRestUri(), "", "");
        super.setUp();
    }

    @Override
    protected ListenableAppStorage createStorage() {
        URI restUri = getRestUri();
        RemoteAppStorage storage = new RemoteAppStorage(AppDataBeanMock.TEST_FS_NAME, restUri, userSession.getToken());
        return new RemoteListenableAppStorage(storage, restUri);
    }

    @Test
    public void getFileSystemNamesTest() {
        List<String> fileSystemNames = RemoteAppStorage.getFileSystemNames(getRestUri(), userSession.getToken());
        assertEquals(Collections.singletonList(AppDataBeanMock.TEST_FS_NAME), fileSystemNames);
    }
}
