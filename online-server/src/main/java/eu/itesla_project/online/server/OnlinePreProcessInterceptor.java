/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Provider
public class OnlinePreProcessInterceptor implements ContainerRequestFilter{

	@Override
	public void filter(ContainerRequestContext req) throws IOException {
		/*
		SecurityContext securityContext=req.getSecurityContext();
		Principal principal=securityContext.getUserPrincipal();
		
		if(principal==null || (!securityContext.isUserInRole("user" )&& !securityContext.isUserInRole("admin" )))
		{
			System.out.println("PReProcess NOT AUTHORIZED");
			ResponseBuilder rb=new ResponseBuilderImpl();
			rb.status(Status.UNAUTHORIZED);
			
			
			req.abortWith(rb.build());
		}
		if(req.getUriInfo().getPath().equals("/online/j_security_check")){
			System.out.println("PReProcess redirect j_security_check to index");
			ResponseBuilder rb=new ResponseBuilderImpl();
			rb.status(Status.TEMPORARY_REDIRECT);
			try {
				rb.location(new URI("/online/index.html"));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			req.abortWith(rb.build());
		}
		*/
		
	}

	

	

}
