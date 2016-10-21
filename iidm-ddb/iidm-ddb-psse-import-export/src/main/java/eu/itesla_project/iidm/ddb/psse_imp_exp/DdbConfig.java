/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.psse_imp_exp;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DdbConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DdbConfig.class);

    // remote jboss IP and port
	private static final String DEFAULT_HOST = "127.0.0.1";

    //remote credentials (user must be defined in jboss security domain, ApplicationRealm; cfr. readme.txt)
	private static final String DEFAULT_USER = "user";

    private static final String DEFAULT_PASSWORD = "password";

    //remote port, default is 4447
	private static final String DEFAULT_REMOTING_PORT = "4447";

    private String jbossHost;

    private String jbossPort;

    private String jbossUser;

    private String jbossPassword;

    public static DdbConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("ddb");
        String jbossHost = config.getStringProperty("jbossHost");
        String jbossPort = config.getStringProperty("jbossPort");
        String jbossUserName = config.getStringProperty("jbossUserName");
        String jbossPassword = config.getStringProperty("jbossPassword");
        return new DdbConfig(jbossHost, jbossPort, jbossUserName, jbossPassword);
    }

    public DdbConfig() {
        this(DEFAULT_HOST, DEFAULT_REMOTING_PORT, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    public DdbConfig(String jbossHost, String jbossPort, String jbossUser, String jbossPassword) {
        this.jbossHost = jbossHost;
        this.jbossPort = jbossPort;
        this.jbossUser = jbossUser;
        this.jbossPassword = jbossPassword;
    }

    public String getJbossHost() {
        return jbossHost;
    }

    public String getJbossPort() {
        return jbossPort;
    }

    public String getJbossUser() {
        return jbossUser;
    }

    public String getJbossPassword() {
        return jbossPassword;
    }

    public void setJbossHost(String jbossHost) {
        this.jbossHost = jbossHost;
    }

    public void setJbossPort(String jbossPort) {
        this.jbossPort = jbossPort;
    }

    public void setJbossUser(String jbossUser) {
        this.jbossUser = jbossUser;
    }

    public void setJbossPassword(String jbossPassword) {
        this.jbossPassword = jbossPassword;
    }

}
