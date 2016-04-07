/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ejbclient;

import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class EjbClientCtx implements AutoCloseable {

	static Logger LOGGER = LoggerFactory.getLogger(EjbClientCtx.class.getName());

	static Context context=null;

	public EjbClientCtx(String host, int port, String userName, String userPassword) throws NamingException {
		if (context==null) {
			synchronized (EjbClientCtx.class) {
				if (context==null) {
					context = EjbClientCtxUtils.createEjbContext(host, port, userName, userPassword);
				}
			}
		}
		LOGGER.info("opening context " + context.hashCode());
	}

	public EjbClientCtx(String host, String port, String userName, String userPassword) throws NamingException {
		this(host, Integer.parseInt(port), userName, userPassword);
	}

	public static EjbClientCtx newCtx(String host, int port, String userName, String userPassword)
			throws NamingException {
		return new EjbClientCtx(host, port, userName, userPassword);
	}

	public static EjbClientCtx newCtx(String host, String port, String userName, String userPassword)
			throws NamingException {
		return new EjbClientCtx(host, Integer.parseInt(port), userName, userPassword);
	}

	public <T> T connectEjb(String jndiName) throws NamingException {
		synchronized (EjbClientCtx.class) {
			return EjbClientCtxUtils.connectEjb(context, jndiName);
		}
	}

	@Override
	public void close() throws Exception {
//		LOGGER.info("closing context " + context.hashCode());
//		if(context!=null) try { context.close(); } catch (Throwable ex) { /* No Op */ }
	}

}
