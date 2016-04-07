/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;

import java.nio.file.Path;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DataMiningFacadeRestConfig {
	String serviceUser;
	String servicePassword;
	String restServiceUrl;
	
	Path tmpDir;
	
	private final boolean debug;
	
	public String getServiceUser() {
		return serviceUser;
	}
	public String getServicePassword() {
		return servicePassword;
	}
	public String getRestServiceUrl() {
		return restServiceUrl;
	}
	
	public Path getTmpDir() {
		return tmpDir;
	}
	
	public DataMiningFacadeRestConfig(String restServiceUrl,
			String serviceUser, String servicePassword, Path tmpDir, boolean debug) {
		super();
		this.restServiceUrl = restServiceUrl;
		this.serviceUser = serviceUser;
		this.servicePassword = servicePassword;
		this.tmpDir=tmpDir;
		this.debug=debug;
	}
	
	public DataMiningFacadeRestConfig(String restServiceUrl,
			String serviceUser, String servicePassword, Path tmpDir) {
		this(restServiceUrl, serviceUser, servicePassword, tmpDir, false);
	}
	
	public boolean isDebug() {
        return debug;
	}	

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(" [serviceUser=");
		builder.append(serviceUser);
		builder.append(", servicePassword=");
		//builder.append(servicePassword);
		builder.append("***");
		builder.append(", restServiceUrl=");
		builder.append(restServiceUrl);
		builder.append(", tmpDir=");
		builder.append(tmpDir);
		builder.append(", debug=");
		builder.append(debug);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
