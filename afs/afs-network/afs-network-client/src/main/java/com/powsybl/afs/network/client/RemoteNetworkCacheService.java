/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.network.client;

import com.google.common.base.Supplier;
import com.powsybl.afs.AfsException;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ext.base.NetworkCacheService;
import com.powsybl.afs.ext.base.ProjectCase;
import com.powsybl.afs.ext.base.ProjectCaseListener;
import com.powsybl.afs.ext.base.ScriptType;
import com.powsybl.afs.ws.client.utils.ClientUtils;
import com.powsybl.afs.ws.client.utils.RemoteServiceConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.afs.ws.client.utils.ClientUtils.checkOk;
import static com.powsybl.afs.ws.client.utils.ClientUtils.readEntityIfOk;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class RemoteNetworkCacheService implements NetworkCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteNetworkCacheService.class);
    private static final String FILE_SYSTEM_NAME = "fileSystemName";
    private static final String NODE_ID = "nodeId";
    private static final String NODE_PATH = "fileSystems/{fileSystemName}/nodes/{nodeId}";

    private final Supplier<Optional<RemoteServiceConfig>> configSupplier;

    private final String token;

    RemoteNetworkCacheService(Supplier<Optional<RemoteServiceConfig>> configSupplier, String token) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.token = token;
    }

    private static WebTarget createWebTarget(Client client, URI baseUri) {
        return client.target(baseUri)
                .path("rest")
                .path("networkCache");
    }

    private RemoteServiceConfig getConfig() {
        return Objects.requireNonNull(configSupplier.get()).orElseThrow(() -> new AfsException("Remote service config is missing"));
    }

    @Override
    public <T extends ProjectFile & ProjectCase> Network getNetwork(T projectCase) {
        Objects.requireNonNull(projectCase);

        LOGGER.info("getNetwork(fileSystemName={}, nodeId={})", projectCase.getFileSystem().getName(),
                projectCase.getId());

        Client client = ClientUtils.createClient();
        try {
            WebTarget webTarget = createWebTarget(client, getConfig().getRestUri());

            Response response = webTarget.path(NODE_PATH)
                    .resolveTemplate(FILE_SYSTEM_NAME, projectCase.getFileSystem().getName())
                    .resolveTemplate(NODE_ID, projectCase.getId())
                    .request(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .get();
            try (InputStream is = readEntityIfOk(response, InputStream.class)) {
                return NetworkXml.read(is);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                response.close();
            }
        } finally {
            client.close();
        }
    }

    @Override
    public <T extends ProjectFile & ProjectCase> String queryNetwork(T projectCase, ScriptType scriptType, String scriptContent) {
        Objects.requireNonNull(projectCase);
        Objects.requireNonNull(scriptType);
        Objects.requireNonNull(scriptContent);

        LOGGER.info("queryNetwork(fileSystemName={}, nodeId={}, scriptType={}, scriptContent=...)",
                projectCase.getFileSystem().getName(), projectCase.getId(), scriptType);

        Client client = ClientUtils.createClient();
        try {
            WebTarget webTarget = createWebTarget(client, getConfig().getRestUri());

            Response response = webTarget.path(NODE_PATH)
                    .resolveTemplate(FILE_SYSTEM_NAME, projectCase.getFileSystem().getName())
                    .resolveTemplate(NODE_ID, projectCase.getId())
                    .queryParam("scriptType", scriptType.name())
                    .request(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .post(Entity.text(scriptContent));
            try {
                return readEntityIfOk(response, String.class);
            } finally {
                response.close();
            }
        } finally {
            client.close();
        }
    }

    @Override
    public <T extends ProjectFile & ProjectCase> void invalidateCache(T projectCase) {
        Objects.requireNonNull(projectCase);

        LOGGER.info("invalidateCache(fileSystemName={}, nodeId={})",
                projectCase.getFileSystem().getName(), projectCase.getId());

        Client client = ClientUtils.createClient();
        try {
            WebTarget webTarget = createWebTarget(client, getConfig().getRestUri());

            Response response = webTarget.path(NODE_PATH)
                    .resolveTemplate(FILE_SYSTEM_NAME, projectCase.getFileSystem().getName())
                    .resolveTemplate(NODE_ID, projectCase.getId())
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .delete();
            try {
                checkOk(response);
            } finally {
                response.close();
            }
        } finally {
            client.close();
        }
    }

    @Override
    public <T extends ProjectFile & ProjectCase> void addListener(T projectCase, ProjectCaseListener listener) {
        // TODO
    }

    @Override
    public <T extends ProjectFile & ProjectCase> void removeListener(T projectCase, ProjectCaseListener listener) {
        // TODO
    }
}
