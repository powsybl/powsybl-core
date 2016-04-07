/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server;

import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Provider
public class SecurityInterceptor implements ContainerRequestFilter {

    private static final Response DENIED = Response.status(Response.Status.FORBIDDEN).entity("Permission Denied").build();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String username = requestContext.getHeaderString(OfflineApplicationSecurity.USERNAME_HTTP_PARAMETER);
        String password = requestContext.getHeaderString(OfflineApplicationSecurity.PASSWORD_HTTP_PARAMETER);
        if (!OfflineApplicationSecurity.check(username, password)) {
            requestContext.abortWith(DENIED);
        }
    }

}
