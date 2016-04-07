/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_modelica;

import java.util.HashMap;

import javax.naming.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ejbclient.EjbClientCtxUtils;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DDBConverter {
	
	private final String ddbManagerJNDIName = "ejb:iidm-ddb-ear/iidm-ddb-ejb-0.0.1-SNAPSHOT/DDBManagerBean!eu.itesla_project.iidm.ddb.service.DDBManager";
	private DDBManager ddbManager;
	private Context context = null;
	
	static Logger log = LoggerFactory.getLogger(DDBConverter.class.getName());
	
	
	public DDBConverter(String jbHost, String jbPort, String ddbUser, String ddbPassword) throws ConversionException {
		try {
			this.context = EjbClientCtxUtils.createEjbContext(jbHost, Integer.parseInt(jbPort), ddbUser, ddbPassword);
			this.ddbManager = EjbClientCtxUtils.connectEjb(context, ddbManagerJNDIName);;
		} catch (Throwable e) {
			Utils.throwConverterException(e.getMessage(), log);
		} 
	}
	
	public void convertDDB(String eurostagVersion, String modelicaVersion, boolean overwrite) throws ConversionException {
		HashMap<String, Boolean> modelTemplateContainers = new HashMap<String, Boolean>();
		Converter eurostagModelicaConverter = new Converter(this.ddbManager, eurostagVersion, modelicaVersion);
		for (Internal internal : this.ddbManager.findInternalsAll()) {
			log.info("Converting internal " + internal.getNativeId() + " [MTC_ID = " + internal.getModelContainer().getDdbId() + "]");
			if ( modelTemplateContainers.containsKey(internal.getModelContainer().getDdbId()) ) {
				log.debug("Internal " + internal.getNativeId() + " has a model template container " + internal.getModelContainer().getDdbId() + " already converted to modelica, just adding the modelica parameter");
				eurostagModelicaConverter.addModelicaParamerToInternal(internal.getNativeId());
			} else {
				modelTemplateContainers.put(internal.getModelContainer().getDdbId(), true);
				try {
					eurostagModelicaConverter.convertAndSaveInternal(internal.getNativeId(), overwrite);
				} catch (ConversionException e) {
					log.error("Error converting internal " + internal.getNativeId() + " [MTC_ID = " + internal.getModelContainer().getDdbId() + "]: " + e.getMessage());
				}
			}
		}
	}
	
	public void close() {
		if ( this.context != null ) {
			try {
				EjbClientCtxUtils.close(this.context);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
		}
	}
	
	public static void main(String[] args) { // throws Exception {
//		String jbHost = "127.0.0.1";
//		String jbPort = "4447";
//		String ddbUser = "user";
//		String ddbPassword = "password";
//		String eurostagVersion = "5.1.1";
//		String modelicaVersion = "3.3";
//		boolean overwrite = true;
		if (args.length != 7) {
            log.warn("required parameters: jbossHost jbossPort ddbUser ddbPassword eurostagVersion modelicaVersion overwrite");
            System.exit(0);
        }
		String jbHost = args[0];
		String jbPort = args[1];
		String ddbUser = args[2];
		String ddbPassword = args[3];
		String eurostagVersion = args[4];
		String modelicaVersion = args[5];
		boolean overwrite = Boolean.parseBoolean(args[6]);
		
		DDBConverter DDBConverter = null;
		try {
			DDBConverter = new DDBConverter(jbHost, jbPort, ddbUser, ddbPassword);
			DDBConverter.convertDDB(eurostagVersion, modelicaVersion, overwrite);
		} catch (ConversionException e) {
			log.error("Error converting DDB data from eurostag to modelica: " + e.getMessage());
		} finally {
			if ( DDBConverter != null )
				DDBConverter.close();
		}
		
	}

}
