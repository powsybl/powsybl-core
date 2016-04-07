/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DynDataUnloader {
	static Logger log = LoggerFactory.getLogger(DynDataUnloader.class.getName());

    String jbossHost="127.0.0.1";
    String jbossPort="4447";
    String jbossUser="user";
    String jbossPassword="password";



	/**
	 * @param dicoPath   mapping csv file (cimid, eurostagid)
	 * @param ddPath     file path or dir path to process (.dd and .dta files are considered)
	 * @param regsPath   dir path containing regulators
	 * @param eurostagVersion  i.e 5.1.1
	 * @param jbossHost
	 * @param jbossPort
	 * @param jbossUser
	 * @param jbossPassword
	 */
	public DynDataUnloader(String jbossHost, String jbossPort, String jbossUser, String jbossPassword) {;
		this.jbossHost=jbossHost;
		this.jbossPort=jbossPort;
		this.jbossUser=jbossUser;
		this.jbossPassword=jbossPassword;
	}


	public void unloadDynData() throws Exception {
		DdbDtaImpExp ddbImpExp = new DdbDtaImpExp(new DdbConfig(jbossHost, jbossPort, jbossUser, jbossPassword));
		ddbImpExp.unloadEurostagData();
	}

}
