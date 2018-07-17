/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server.utils;

import com.powsybl.commons.config.PlatformConfig;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.security.Key;

/**
 * @author Ali Tahanout <ali.tahanout at rte-france.com>
 */
@Provider
@JwtTokenNeeded
@Priority(Priorities.AUTHENTICATION)
public class JwtTokenNeededFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenNeededFilter.class);

    @Inject
    private KeyGenerator keyGenerator;

    private boolean skipTokenValidityCheck;

    public JwtTokenNeededFilter() {
        this(PlatformConfig.defaultConfig());
    }

    public JwtTokenNeededFilter(PlatformConfig platformConfig) {
        skipTokenValidityCheck = SecurityConfig.load(platformConfig).isSkipTokenValidityCheck();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (skipTokenValidityCheck) {
            return;
        }

        // Get the HTTP Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        LOGGER.trace("#### authorizationHeader : {}", authorizationHeader);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();
        try {
            // Validate the token
            Key key = keyGenerator.generateKey();
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
        } catch (Exception eee) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
