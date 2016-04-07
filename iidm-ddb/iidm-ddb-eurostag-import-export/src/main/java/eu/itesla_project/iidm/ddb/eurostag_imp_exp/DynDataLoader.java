/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Feeds DDB with data
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DynDataLoader {
	static Logger log = LoggerFactory.getLogger(DynDataLoader.class.getName());

	Path dicoPath=null;
	Path ddPath=null;
	Path regsPath=null;

    String jbossHost="127.0.0.1";
    String jbossPort="4447";
    String jbossUser="user";
    String jbossPassword="password";
    String eurostagVersion="5.1.1";



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
	public DynDataLoader(Path dicoPath, Path ddPath, Path regsPath, String eurostagVersion ,String jbossHost, String jbossPort, String jbossUser, String jbossPassword) {
		this.dicoPath = dicoPath;
		this.ddPath = ddPath;
		this.regsPath = regsPath;
		this.eurostagVersion=eurostagVersion;
		this.jbossHost=jbossHost;
		this.jbossPort=jbossPort;
		this.jbossUser=jbossUser;
		this.jbossPassword=jbossPassword;
	}


	public void loadDynData() throws Exception {
		DdbDtaImpExp ddbImpExp = new DdbDtaImpExp(new DdbConfig(jbossHost, jbossPort, jbossUser, jbossPassword));
		ddbImpExp.loadEurostagData(ddPath, dicoPath, eurostagVersion, regsPath);
	}


	public static void main(String[] args) throws Exception {

		if (args.length !=  8) {
			System.out.println("DynDataLoader: required parameters: dicoPath ddPath regsPath eurostagVersion jbossHost jbossPort jbossuser jbosspassword");
			System.exit(0);
		}
        String dicoPath=args[0];
        String ddPath=args[1];
        String regsPath=args[2];
        String eurostagVersion=args[3];
        String jbossHost=args[4];
        String jbossPort=args[5];
        String jbossUser=args[6];
        String jbossPassword=args[7];

        System.out.println("dicoPath="+dicoPath);
        System.out.println("ddPath="+ddPath);
        System.out.println("regsPath="+regsPath);
        System.out.println("eurostagVersion="+eurostagVersion);
        System.out.println("jbossHost="+jbossHost);
        System.out.println("jbossPort="+jbossPort);
        System.out.println("jbossUser="+jbossUser);
        System.out.println("jbossPassword="+jbossPassword);

		DynDataLoader ex = new DynDataLoader(Paths.get(dicoPath),Paths.get(ddPath), Paths.get(regsPath), eurostagVersion, jbossHost, jbossPort, jbossUser, jbossPassword);

		ex.loadDynData();
	}

}
