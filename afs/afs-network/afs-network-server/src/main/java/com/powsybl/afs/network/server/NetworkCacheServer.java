/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.network.server;

import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ext.base.ProjectCase;
import com.powsybl.afs.ext.base.ScriptType;
import com.powsybl.afs.ws.server.utils.JwtTokenNeeded;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.afs.ws.server.utils.AppDataBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Named
@ApplicationScoped
@Path("networkCache")
@JwtTokenNeeded
public class NetworkCacheServer {

    @Inject
    private AppDataBean appDataBean;

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("fileSystems/{fileSystemName}/nodes/{nodeId}")
    public Response getNetwork(@PathParam("fileSystemName") String fileSystemName,
                               @PathParam("nodeId") String nodeId) {
        Network network = appDataBean.getProjectFile(fileSystemName, nodeId, ProjectFile.class, ProjectCase.class)
                .getNetwork();
        StreamingOutput streamingOutput = output -> NetworkXml.write(network, output);
        return Response.ok(streamingOutput).build();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("fileSystems/{fileSystemName}/nodes/{nodeId}")
    public Response queryNetwork(@PathParam("fileSystemName") String fileSystemName,
                                 @PathParam("nodeId") String nodeId,
                                 @QueryParam("scriptType") ScriptType scriptType,
                                 String scriptContent) {
        String resultJson = appDataBean.getProjectFile(fileSystemName, nodeId, ProjectFile.class, ProjectCase.class)
                .queryNetwork(scriptType, scriptContent);
        return Response.ok(resultJson).build();
    }

    @DELETE
    @Path("fileSystems/{fileSystemName}/nodes/{nodeId}")
    public Response invalidateCache(@PathParam("fileSystemName") String fileSystemName,
                                    @PathParam("nodeId") String nodeId) {
        appDataBean.getProjectFile(fileSystemName, nodeId, ProjectFile.class, ProjectCase.class)
                .invalidateNetworkCache();
        return Response.ok().build();
    }
}
