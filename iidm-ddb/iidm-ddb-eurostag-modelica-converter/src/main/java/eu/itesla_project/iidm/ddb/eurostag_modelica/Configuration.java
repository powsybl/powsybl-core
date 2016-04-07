/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_modelica;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Configuration {
	
	static Logger log = LoggerFactory.getLogger(Configuration.class.getName());
	
	String configurationFileName = "converter.properties";
	String workingDirProperty = "workingDir";
	String workingDir;
	
	public Configuration() {
		this.workingDir = Utils.workingDirectory();
		try {
			readConfigurationFile();
		} catch (ConversionException e) {
		}
	}
	
	public String getWorkingDir() {
		return workingDir;
	}
	
	protected void readConfigurationFile() throws ConversionException {
		log.debug("Reading configuration file " + configurationFileName);
		URL configurationFileUrl = this.getClass().getResource("/" + configurationFileName);
		Properties properties = new Properties();
		InputStream configurationFileStream = null;
		try {
			configurationFileStream = configurationFileUrl.openStream();
			properties.load(configurationFileStream);
			workingDir = properties.getProperty(workingDirProperty, Utils.workingDirectory());
		} catch (FileNotFoundException e) {
			Utils.throwConverterException("Cannot read configuration file  " + configurationFileName + ": " + e.getMessage(), log);
		} catch (IOException e) {
			Utils.throwConverterException("Cannot read configuration file  " + configurationFileName + ": " + e.getMessage(), log);
		} finally {
			if ( configurationFileStream != null )
				try {
					configurationFileStream.close();
				} catch (IOException e) {
					log.error("Error closing configuration file stream: " + e.getMessage());
				}
		}
		
	}

}
