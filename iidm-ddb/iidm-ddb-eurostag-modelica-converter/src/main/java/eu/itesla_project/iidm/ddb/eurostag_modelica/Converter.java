/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_modelica;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.DefaultParameters;
import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.ParameterString;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Converter {
	
	static Logger log = LoggerFactory.getLogger(Converter.class.getName());
	static Configuration configuration = new Configuration();
	String workingDirectory;
	DDBManager ddbManager;
	SimulatorInst eurostagSimulator;
	SimulatorInst modelicaSimulator;
	
	public Converter(DDBManager ddbManager, String eurostagVersion, String modelicaVersion) throws ConversionException {
		Utils.checkNull(ddbManager, "DDB Manager", log);
		Utils.checkNull(eurostagVersion, "Eurostag version", log);
		Utils.checkNull(modelicaVersion, "Modelica version", log);
		
//		File workingDir = new File(configuration.workingDir);
//		workingDirectory = workingDir.getAbsolutePath();
		setWorkingDirectory(configuration.workingDir);
		
		this.ddbManager = ddbManager;
		eurostagSimulator=this.ddbManager.findSimulator(Simulator.EUROSTAG, eurostagVersion);
		if ( eurostagSimulator == null )
			Utils.throwConverterException("Unsupported Eurostag version " + eurostagVersion, log);
		modelicaSimulator=this.ddbManager.findSimulator(Simulator.MODELICA, modelicaVersion);
		if ( modelicaSimulator == null )
			Utils.throwConverterException("Unsupported Modelica version " + modelicaVersion, log);
	}
	
	public String getEurostagVersion() {
		return eurostagSimulator.getVersion();
	}
	
	public String getModelicaVersion() {
		return modelicaSimulator.getVersion();
	}
	
	public void setWorkingDirectory(String workingDirectory) throws ConversionException {
		Utils.checkNull(workingDirectory, "Working directory", log);

		File workingDir = new File(workingDirectory);
		if ( !workingDir.exists() ) {
			log.debug("Creating working directory " + workingDir.getAbsolutePath());
			try {
				workingDir.mkdirs();
			} catch (Throwable e) {
				Utils.throwConverterException("Cannot create working directory " + workingDir.getAbsolutePath() + ": " + e.getMessage(), log);
			}
		}
		this.workingDirectory = workingDir.getAbsolutePath();
	}
	
	public void convertAndSaveInternal(String nativeId, boolean overwrite) throws ConversionException {
		convertAndSaveInternal(nativeId, overwrite, true, true, true);
	}
	
	public void convertAndSaveInternal(String nativeId, boolean overwrite, boolean saveInitFile, boolean saveParametersSets, boolean saveDefaultParameterSet) throws ConversionException {
		Utils.checkNull(nativeId, "Internal Id", log);
		String frmFile = null;
		String friFile = null;
		String parFile = null;
		String modelicaFileName = null;
		String modelicaInitFileName = null;
		try {

			log.debug("Converting Internal " + nativeId + " from Eurostag v. " + eurostagSimulator.getVersion());
			log.debug("Working directory = " + workingDirectory);
	
			// get the internal
			Internal internal = ddbManager.findInternal(nativeId);
			// check if it does exist
			if ( internal == null )
				Utils.throwConverterException("No Internal with " + nativeId + " id in the DDB", log);
			log.debug("Found Internal " + internal.getNativeId());
			
			// check if data in modelica format is already in the ddb
			ModelTemplate modelTemplateModelica = ddbManager.findModelTemplate(internal, modelicaSimulator);
			if ( !overwrite ) {
				if ( modelTemplateModelica != null )
					Utils.throwConverterException("Internal " + nativeId + " already has data in Modelica format in the DDB", log);
			}
			
			// get the model template for eurostag
			ModelTemplate modelTemplateEurostag = ddbManager.findModelTemplate(internal, eurostagSimulator);
			// check if it does exist
			if ( modelTemplateEurostag == null )
				Utils.throwConverterException("No Eurostag v. " + eurostagSimulator.getVersion() + " ModelTemplate for Internal " + nativeId + " in the DDB", log);
			log.debug("Found Eurostag ModelTemplate " + modelTemplateEurostag.getId() + " for Internal " + nativeId);
			
			// get the macroblock name parameter
			String macroblockName = ddbManager.getStringParameter(internal, eurostagSimulator, "macroblock.name");
			// check if it does exist
			if ( macroblockName == null )
				Utils.throwConverterException("No macroblock name in Eurostag v. " + eurostagSimulator.getVersion() + " Parameters for Internal " + nativeId + " in the DDB", log);
			log.debug("Found Macroblock " + macroblockName  + " among Eurostag parameters of Internal " + nativeId);
			
			// save eurostag data files
			frmFile = saveDataFile(nativeId, modelTemplateEurostag, macroblockName, "frm");
			friFile = saveDataFile(nativeId, modelTemplateEurostag, macroblockName, "fri");
			parFile = saveDataFile(nativeId, modelTemplateEurostag, macroblockName, "par");
			
			// convert eurostag files
			modelicaFileName = macroblockName.toLowerCase() + ".mo";
			if ( saveInitFile )
				modelicaInitFileName = macroblockName.toLowerCase() + "_init.mo";
			HashMap<Integer, HashMap<String, String>> allParameterSets = null;
			try {
				itesla.converter.Converter conversor = new itesla.converter.Converter(frmFile, workingDirectory + File.separator, false); // must add separator to working dir to correctly run conversion
				conversor.convert2MO();
				if ( saveInitFile ) {
					itesla.converter.Converter initConversor = new itesla.converter.Converter(frmFile, workingDirectory + File.separator, true);
					initConversor.convert2MO();
				}
				if ( saveParametersSets )
					allParameterSets = conversor.parData.getParameters();
			} catch (Exception e) {
				Utils.throwConverterException("Error converting " + nativeId + ": " + e.getMessage(), log);
			}
			
			// read modelica data from file
			byte[] modelicaData = null;
			try {
				log.debug("Reading Modelica data for Internal " + nativeId + " from file " + modelicaFileName);
				modelicaData = Utils.readFile(workingDirectory, modelicaFileName);
			} catch (IOException e) {
				Utils.throwConverterException("Error reading Modelica data for Internal " + nativeId + " from file " + modelicaFileName + ": " + e.getMessage(), log);
			}
			
			// read modelica init data from file
			byte[] modelicaInitData = null;
			if ( saveInitFile ) {
				try {
					log.debug("Reading Modelica init data for Internal " + nativeId + " from file " + modelicaInitFileName);
					modelicaInitData = Utils.readFile(workingDirectory, modelicaInitFileName);
				} catch (IOException e) {
					Utils.throwConverterException("Error reading Modelica init data for Internal " + nativeId + " from file " + modelicaInitFileName + ": " + e.getMessage(), log);
				}
			}
			
			// store modelica data
			ModelTemplateContainer modelTemplateContainer = internal.getModelContainer();
			if ( modelTemplateModelica == null ) {
				modelTemplateModelica = new ModelTemplate(modelicaSimulator, macroblockName.toLowerCase(), macroblockName);
			} else { // in order to update the model template, I need to remove and then add it
				List<ModelTemplate> modelTemplates = modelTemplateContainer.getModelTemplates();
				ModelTemplate modelTemplateToRemove = null;
				for (ModelTemplate modelTemplate : modelTemplates) {
					if ( modelTemplate.getId().longValue() == modelTemplateModelica.getId().longValue() )
						modelTemplateToRemove = modelTemplate;
				}
				modelTemplates.remove(modelTemplateToRemove);
				modelTemplateContainer.setModelTemplates(modelTemplates);
			}
			modelTemplateModelica.setData("mo", modelicaData);
			if ( saveInitFile )
				modelTemplateModelica.setData("init_mo", modelicaInitData);
			if ( saveParametersSets ) {
				List<DefaultParameters> defaultParametersList = getDefaultParametersList(allParameterSets);
				modelTemplateModelica.setDefaultParameters(defaultParametersList);
			}
			modelTemplateContainer.getModelTemplates().add(modelTemplateModelica);
			modelTemplateContainer = ddbManager.save(modelTemplateContainer);
			log.debug("Stored Modelica ModelTemplate " + modelTemplateModelica.getId() + " for Internal " + internal.getNativeId());
			
			ParametersContainer parametersContainer = internal.getParametersContainer();
			Parameters parametersModelica = ddbManager.findParameters(internal, modelicaSimulator);
			if ( parametersModelica == null ) {
				parametersModelica = new Parameters(modelicaSimulator);				
			} else {
				List<Parameters> parametersList = parametersContainer.getParameters();
				Parameters parametersToRemove = null;
				for (Parameters parameters : parametersList) {
					if ( parameters.getId().longValue() == parametersModelica.getId().longValue() )
						parametersToRemove = parameters;
				}
				parametersList.remove(parametersToRemove);
				parametersContainer.setParameters(parametersList);
			}
			if ( saveDefaultParameterSet ) {
				Parameters parametersEurostag = ddbManager.findParameters(internal, eurostagSimulator);
				int defaultParameterSetNum = parametersEurostag.getDefParamSetNum();
				parametersModelica.setDefParamSetNum(defaultParameterSetNum);
			}
			parametersContainer.getParameters().add(parametersModelica);
			parametersContainer = ddbManager.save(parametersContainer);
			log.debug("Stored Modelica Parameters " + parametersModelica.getId() + " for Internal " + internal.getNativeId());
						
		} catch (Exception e) {
			throw e;
		} finally {
			// remove temporary files
			if ( frmFile != null) {
				try {
					Utils.deleteFile(frmFile);
				} catch (IOException e) {
					log.error("Error deleting temporary frm file " + frmFile + ": " + e.getMessage());
				}
			}
			if ( friFile != null) {
				try {
					Utils.deleteFile(friFile);
				} catch (IOException e) {
					log.error("Error deleting temporary fri file " + friFile + ": " + e.getMessage());
				}
			}
			if ( parFile != null) {
				try {
					Utils.deleteFile(parFile);
				} catch (IOException e) {
					log.error("Error deleting temporary par file " + parFile + ": " + e.getMessage());
				}
			}
			if ( modelicaFileName != null) {
				try {
					Utils.deleteFile(workingDirectory, modelicaFileName);
				} catch (Throwable e) {
					log.error("Error deleting temporary mo file " + modelicaFileName + ": " + e.getMessage());
				}
			}
			if ( modelicaInitFileName != null) {
				try {
					Utils.deleteFile(workingDirectory, modelicaInitFileName);
				} catch (Throwable e) {
					log.error("Error deleting temporary init mo file " + modelicaInitFileName + ": " + e.getMessage());
				}
			}
		}
	}
	
	private List<DefaultParameters> getDefaultParametersList(HashMap<Integer, HashMap<String, String>> allParameterSets) {
		List<DefaultParameters> defaultParametersList = new ArrayList<DefaultParameters>();
		if ( allParameterSets != null ) {
			for (Integer parameterSetNum : allParameterSets.keySet()) {
				DefaultParameters defaultParametersSet = new DefaultParameters(parameterSetNum.intValue());
				for (String parameterName : allParameterSets.get(parameterSetNum).keySet()) {
					String parameterValue = allParameterSets.get(parameterSetNum).get(parameterName);
					ParameterString parameter = new ParameterString(parameterName, parameterValue);
					defaultParametersSet.addParameter(parameter);
				}
				defaultParametersList.add(defaultParametersSet);
			}
		}
		return defaultParametersList;
	}
		
	public void addModelicaParamerToInternal(String nativeId) {
		addModelicaParamerToInternal(nativeId, true);
	}
	
	public void addModelicaParamerToInternal(String nativeId, boolean saveDefaultParameterSet) {
		//Utils.checkNull(nativeId, "Internal Id", log);

		log.debug("Adding parameter (modelica version " + modelicaSimulator.getVersion() + ") to internal " + nativeId);

		// get the internal
		Internal internal = ddbManager.findInternal(nativeId);
		
		// add modelica parameter to the internal
		ParametersContainer parametersContainer = internal.getParametersContainer();
		Parameters parametersModelica = ddbManager.findParameters(internal, modelicaSimulator);
		if ( parametersModelica == null ) {
			parametersModelica = new Parameters(modelicaSimulator);				
		} else {
			List<Parameters> parametersList = parametersContainer.getParameters();
			Parameters parametersToRemove = null;
			for (Parameters parameters : parametersList) {
				if ( parameters.getId().longValue() == parametersModelica.getId().longValue() )
					parametersToRemove = parameters;
			}
			parametersList.remove(parametersToRemove);
			parametersContainer.setParameters(parametersList);
		}
		if ( saveDefaultParameterSet ) {
			Parameters parametersEurostag = ddbManager.findParameters(internal, eurostagSimulator);
			int defaultParameterSetNum = parametersEurostag.getDefParamSetNum();
			parametersModelica.setDefParamSetNum(defaultParameterSetNum);
		}
		parametersContainer.getParameters().add(parametersModelica);
		parametersContainer = ddbManager.save(parametersContainer);
		log.debug("Stored Modelica Parameters " + parametersModelica.getId() + " for Internal " + internal.getNativeId());
	}
	
	private String saveDataFile(String nativeId, ModelTemplate modelTemplate, String macroblockName, String dataType) throws ConversionException {
		// get data from model template
		byte[] data = modelTemplate.getData(dataType);
		// check if it does exist
		if ( data == null )
			Utils.throwConverterException("No data for " + dataType + " file in the DDB for Internal " + nativeId, log);
		log.debug("Saving Eurostag " + dataType + " file for Internal " + nativeId);
		// save data into file
		String dataFileName = macroblockName.toLowerCase() + "." + dataType;
		try {
			dataFileName = Utils.saveFile(workingDirectory, dataFileName, data);
		} catch (IOException e) {
			Utils.throwConverterException("Error saving " + dataType + " data for Internal " + nativeId + " into file " + dataFileName + ": " + e.getMessage(), log);
		}
		log.debug("Saved " + dataFileName + " for internal " + nativeId);
		return dataFileName;
	}

}
