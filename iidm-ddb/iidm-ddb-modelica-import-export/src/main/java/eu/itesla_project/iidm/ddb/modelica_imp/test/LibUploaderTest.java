/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp.test;

import java.nio.file.Path;
import java.nio.file.Paths;

import eu.itesla_project.iidm.ddb.modelica_imp_exp.PowerSystemsLibLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class LibUploaderTest {

	public static void main(String[] args) {
		if(args.length > 0) {			
			libFilePath = Paths.get(args[0]);
			elementsPath = Paths.get(args[1]);
			mappingFilePath = Paths.get(args[2]);
			jbossHost = args[3];
	        jbossPort = args[4];
	        jbossUser = args[5];
	        jbossPassword = args[6];
	        isLibrary = Boolean.parseBoolean(args[7]);
			isRegulator = Boolean.parseBoolean(args[8]);
			
		}
		else {
		   	isLibrary = true;
		   	isRegulator = false;
		}
		
		_log.info("Power Systems Library Dir = " + libFilePath);
		_log.info("Models mapper Dir = " + mappingFilePath);
		_log.info("Element Dir = " + elementsPath);

		PowerSystemsLibLoader pwLibLoader = new PowerSystemsLibLoader(libFilePath, mappingFilePath, elementsPath, jbossHost, jbossPort, jbossUser, jbossPassword, isLibrary, isRegulator);
		//new PowerSystemsLibLoader(libFilePath, mappingFilePath, elementsPath, true);

		try {
			pwLibLoader.loadPowerSystemsLib();
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
	}

	private static Path				libFilePath		= null;
	private static Path				mappingFilePath	= null;
	private static Path				elementsPath	= null;
    private static String			jbossHost		= "127.0.0.1";
    private static String			jbossPort		= "4447";
    private static String			jbossUser		= "user";
    private static String			jbossPassword	= "password";
    private static boolean			isLibrary		= false;
    private static boolean			isRegulator		= false;
 
	private static final Logger _log				= LoggerFactory.getLogger(LibUploaderTest.class);
}
