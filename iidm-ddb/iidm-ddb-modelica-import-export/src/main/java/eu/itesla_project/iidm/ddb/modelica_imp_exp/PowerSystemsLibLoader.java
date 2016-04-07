/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import java.nio.file.Path;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class PowerSystemsLibLoader {
	public PowerSystemsLibLoader(Path libFilePath, Path mappingFilePath, Path elementsPath, boolean isLibrary, boolean isRegulator) {
		this.libFilePath = libFilePath;
		this.mappingFilePath = mappingFilePath;
		this.elementsPath = elementsPath;
		this.isLibrary = isLibrary;
		this.isRegulator = isRegulator;
	}
	
	public PowerSystemsLibLoader(Path libFilePath, Path mappingFilePath, Path elementsPath, String jbossHost, String jbossPort, String jbossUser, String jbossPassword, boolean isLibrary, boolean isRegulator) {
		this.libFilePath = libFilePath;
		this.mappingFilePath = mappingFilePath;
		this.elementsPath = elementsPath;
		this.jbossHost = jbossHost;
		this.jbossPort = jbossPort;
		this.jbossUser = jbossUser;
		this.jbossPassword = jbossPassword;
		this.isLibrary = isLibrary;
		this.isRegulator = isRegulator;
	}
	
	public void loadPowerSystemsLib() throws Exception {
		DdbPSLibImporter ddbLibImp = new DdbPSLibImporter(jbossHost, jbossPort, jbossUser, jbossPassword);
		ddbLibImp.loadModelicaSource(libFilePath.toFile(), mappingFilePath.toFile(), elementsPath.toFile(), isLibrary, isRegulator);
	}
	
    private String				jbossHost		= "127.0.0.1";
    private String				jbossPort		= "4447";
    private String				jbossUser		= "user";
    private String				jbossPassword	= "password";
    private Path				libFilePath		= null;
    private Path				mappingFilePath	= null;
    private Path				elementsPath	= null;
    private boolean				isLibrary		= false;
    private boolean				isRegulator;
}

