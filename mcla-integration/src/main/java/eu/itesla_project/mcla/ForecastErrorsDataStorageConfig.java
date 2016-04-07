/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsDataStorageConfig {
	
	/*
	 * example of forecastErrorsStorage.properties file.
	 *
	#path to the directory containing the forecast errors data
	forecastErrorsDir=/shared/allrw/ForecastErrors

	*/
	
	private final Path forecastErrorsDir;
	
	public ForecastErrorsDataStorageConfig(Path forecastErrorsDir) {
		Objects.requireNonNull(forecastErrorsDir,"forecast errors data directory is null");
		
		this.forecastErrorsDir = forecastErrorsDir;
	}
	
	public static ForecastErrorsDataStorageConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("forecastErrorsStorage");
        
        Path forecastErrorsDir = config.getPathProperty("forecastErrorsDir");

        return new ForecastErrorsDataStorageConfig(forecastErrorsDir);
	}
	

	public Path getForecastErrorsDir() {
		return forecastErrorsDir;
	}
	
	@Override
	public String toString() {
		return "ForecastErrorsDataStorageConfig ["+ "forecastErrorsDir=" + forecastErrorsDir + "]";
	}


}
