/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ejbclient;

import java.util.Objects;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class EjbClientCtxUtils {
	static Logger LOGGER = LoggerFactory.getLogger(EjbClientCtxUtils.class.getName());

	/*
	public static Context createEjbContext(String host, int port, String userName, String userPassword)
			throws NamingException {
		LOGGER.info("****WILDFLY**** host: " + host + ", port: " +port+ ", username: " + userName);
		Properties clientProperties = new Properties();
		clientProperties.put("remote.connections", "default");
		clientProperties.put("remote.connection.default.host", host);
		clientProperties.put("remote.connection.default.port", "" + port);
		clientProperties.put("remote.connection.default.username", userName);
		clientProperties.put("remote.connection.default.password", userPassword);
		clientProperties.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		clientProperties.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS",
				"false");
		clientProperties.put("org.jboss.ejb.client.scoped.context", "true");

		// TBD: needed?
		EJBClientConfiguration ejbClientConfiguration = new PropertiesBasedEJBClientConfiguration(clientProperties);
		ContextSelector<EJBClientContext> contextSelector = new ConfigBasedEJBClientContextSelector(
				ejbClientConfiguration);
		EJBClientContext.setSelector(contextSelector);

		// Properties properties = new Properties();
		clientProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		Context context = new InitialContext(clientProperties);
		Context newContext = (Context) context.lookup("ejb:");
		context.close();
		return newContext;
	}

	public static <T> T connectEjb(Context context, String jndiName) throws NamingException {
		Objects.requireNonNull(jndiName, "jndi is null");
		if (jndiName.isEmpty()) {
			throw new IllegalArgumentException("jndi is empty");
		}
		if (!jndiName.startsWith("ejb:")) {
			throw new IllegalArgumentException("jndi must start with ejb:");
		}
		@SuppressWarnings("unchecked")
		T retVal = (T) context.lookup(jndiName.substring("ejb:".length()));
		return retVal;
	}
	*/
	
	
	public static Context createEjbContext(String host, int port, String userName, String userPassword)
			throws NamingException {
		Properties p = new Properties();
		p.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,				
				"org.jboss.naming.remote.client.InitialContextFactory");
		p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED",
				false);
		
		//p.put("remote.connections", "default");
		
		p.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS",
				"true");
		p.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
		//p.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT","false");
		p.put("remote.connection.default.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS","JBOSS-LOCAL-USER");
		
		p.put("jboss.naming.client.ejb.context", true);
	
		
		p.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		//p.put("connect.timeout", "60000");
		
		

		String jbHost = System.getProperty("JBHOST");
		if ((jbHost == null) || ("".equals(jbHost))) {
			jbHost = host;
		}
		LOGGER.info("****JBOSS 8.x**** host: " + jbHost + ", port: " +port+ ", username: " + userName);
		
		p.put(javax.naming.Context.PROVIDER_URL, "http-remoting://" + jbHost + ":"
				+ port);
		
		
		p.put(javax.naming.Context.SECURITY_PRINCIPAL, userName);
		p.put(javax.naming.Context.SECURITY_CREDENTIALS, userPassword);
		//p.put("remote.connection.default.username",userName);
		//p.put("remote.connection.default.password",userPassword);
				
		

		return new InitialContext(p);

	}

	public static <T> T connectEjb(Context context, String jndiName) throws NamingException {
		Objects.requireNonNull(jndiName, "jndi is null");
		if (jndiName.isEmpty()) {
			throw new IllegalArgumentException("jndi is empty");
		}
		if (!jndiName.startsWith("ejb:")) {
			throw new IllegalArgumentException("jndi must start with ejb:");
		}
		@SuppressWarnings("unchecked")
		T retVal = (T) context.lookup(jndiName.substring("ejb:".length()));
		return retVal;
	}
	

	public static void close(Context context) throws Exception{
		context.close();
	}
}
