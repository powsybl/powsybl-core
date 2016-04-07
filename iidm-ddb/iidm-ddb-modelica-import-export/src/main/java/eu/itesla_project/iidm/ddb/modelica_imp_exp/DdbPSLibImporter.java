/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import eu.itesla_project.iidm.ddb.model.*;
import eu.itesla_project.iidm.ddb.modelica_imp_exp.utils.Utils;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ejbclient.EjbClientCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.*;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class DdbPSLibImporter
{
	public DdbPSLibImporter(String host, String remotinPort, String user, String password, String jndiName)
	{
		this.host = host;
		this.remotinPort = remotinPort;
		this.user = user;
		this.password = password;
	}

	public DdbPSLibImporter(String host, String remotinPort, String user, String password)
	{
		this.host = host;
		this.remotinPort = remotinPort;
		this.user = user;
		this.password = password;
	}

	public DdbPSLibImporter()
	{
		this.host = DEFAULT_HOST;
		this.remotinPort = DEFAULT_REMOTING_PORT;
		this.user = DEFAULT_USER;
		this.password = DEFAULT_PASSWORD;
	}

	protected EjbClientCtx newEjbClientEcx() throws NamingException
	{
		return new EjbClientCtx(host, remotinPort, user, password);
	}

	public void loadModelicaSource(File modelicaSource, File mappingFile, File elements, boolean isLibrary, boolean isRegulator)
	{
		List<File> files = getModelicaFiles(modelicaSource);
		if (files == null) return;

		try (EjbClientCtx ctx = newEjbClientEcx())
		{
			DDBManager ddbManager = ctx.connectEjb(DDBMANAGERJNDINAME);
			Map<String, List<String>> mapping = readModelMappingFile(mappingFile);
			SimulatorInst defaultModelicaSimulator = getOrCreateModelicaSimulatorInst(ddbManager, DEFAULT_MODELICA_VERSION);

			for (File f : files)
			{
				File modelicaSource1 = f;
				try
				{
					if (isLibrary)
					{
						emptyFolder(elements);
						splitModelicaLibFile(modelicaSource, elements);
						modelicaSource1 = elements;
					}
					loadModelicaTemplates(ddbManager, modelicaSource1, mapping, isRegulator, defaultModelicaSimulator);
				}
				catch (Throwable e)
				{
					log.error(e.getMessage() + " processing file " + modelicaSource1);
				}
			}
		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Parse the Power Systems Library File (using a simple line-based parser) and split it in files, one for every model
	 * 
	 * @param modelicaLibFile
	 * @param elementsFolder
	 * @return
	 * @throws Exception
	 */
	void splitModelicaLibFile(File modelicaLibFile, File elementsFolder) throws Exception
	{
		// An .mo library file is split in multiple files, one for each class defined
		// The name of the temp files is built using the complete hierarchy name of the output class

		log.info("Building Modelica file. Processing file: " + modelicaLibFile.getAbsolutePath());

		ModelicaParserEventHandler s = new ModelicaFileSplitter(elementsFolder);
		ModelicaSimpleParser p = new ModelicaSimpleParser(s);
		p.parse(modelicaLibFile);

	}

	void loadModelicaTemplates(DDBManager ddbManager, File elements, Map<String, List<String>> mapping, boolean isRegulator, SimulatorInst defaultModelicaSimulator)
	{
		try
		{
			List<File> files = getModelicaFiles(elements);
			if (files == null) return;

			ModelicaModelExtractor modelicaModelExtractor;
			if (isRegulator) modelicaModelExtractor = new ModelicaRegulatorModelExtractor(LIB_PACKAGE_NAME, REGULATORS_PACKAGE_NAME);
			else modelicaModelExtractor = new ModelicaModelExtractor();

			ModelicaSimpleParser modelicaParser = new ModelicaSimpleParser(modelicaModelExtractor);
			for (File file : files)
			{
				if (!file.isFile()) continue;
				log.info("Loading Modelica model. Processing file: " + file.getName());
				modelicaParser.parse(file);
				if (modelicaModelExtractor.getMainClassQualifiedName() == null)
				{
					log.warn("File " + file + " does not contain a main class");
					continue;
				}
				String modelName = modelicaModelExtractor.getMainClassQualifiedName();
				String modelComment = modelicaModelExtractor.getMainClassComment();
				List<ModelicaParameter> modelParams = modelicaModelExtractor.getParameters();
				String modelText = modelicaModelExtractor.getText();

				// This model could contain a reference to a explicit version of modelica simulator,
				// If no explicit reference is retrieved from the model, use the default simulator
				SimulatorInst modelicaSimulator = defaultModelicaSimulator;

				// Adjust string lengths
				if (modelComment == null) modelComment = "";
				if (modelComment.length() > 255) modelComment = modelComment.substring(0, 254);
				if (modelName.length() > 255) modelName = modelName.substring(0, 254);

				updateDatabase(ddbManager, modelName, modelComment, modelParams, modelText, mapping, modelicaSimulator);
			}
		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
		}
	}

	static List<File> getModelicaFiles(File d)
	{
		if (!d.exists())
		{
			log.error(d + " does not exist");
			return null;
		}
		List<File> files = new ArrayList<File>();
		if (d.isDirectory()) files = Arrays.asList(d.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.isFile() && f.getName().toLowerCase().endsWith(".mo");
			}
		}));
		else files.add(d);
		return files;
	}

	static void emptyFolder(File d)
	{
		if (!d.isDirectory()) return;

		File[] files = d.listFiles();
		if (files.length == 0) return;

		log.info("Deleting all files in folder " + d);
		for (File f : files)
			f.delete();
	}

	void updateDatabase(DDBManager ddbManager, String modelName, String modelComment, List<ModelicaParameter> modelParams, String modelText, Map<String, List<String>> mapping, SimulatorInst modelicaSimulator)
	{
		// Map Modelica model parameters to DDB parameters
		List<Parameter> ddbParams = new ArrayList<Parameter>();
		for (ModelicaParameter moParam : modelParams)
		{
			Parameter ddbParam = createParameter(moParam.type, moParam.name, moParam.value);
			if (ddbParam != null) ddbParams.add(ddbParam);
		}

		// Update all Model Template Containers that use this model
		log.info("\tModelica model = " + modelName);
		List<String> mtcIds = getModelTemplateContainerIdsRelatedTo(modelName, mapping);
		for (String mtcId : mtcIds)
		{
			ModelTemplateContainer mtc = ddbManager.findModelTemplateContainer(mtcId);
			if (mtc == null)
			{
				log.info("\tCreating Model Template Container " + mtcId);
				mtc = new ModelTemplateContainer(mtcId, "");
			}
			log.info("\tModel Template Container = " + mtc.getDdbId());

			ModelTemplate mt = findModelTemplate(mtc, modelicaSimulator);
			if (mt == null)
			{
				log.info("\tCreating Model Template " + modelName);
				mt = new ModelTemplate(modelicaSimulator, modelName, modelComment);
				mtc.getModelTemplates().add(mt);
			}

			mt.setData("mo", Utils.stringAsByteArrayUTF8(modelText));

			int paramSetNum = mt.getDefaultParameters() != null ? mt.getDefaultParameters().size() : 0;
			log.info("\tParam set number = " + paramSetNum);
			DefaultParameters defaultParams = new DefaultParameters(paramSetNum);
			defaultParams.setParameters(ddbParams);
			mt.getDefaultParameters().add(defaultParams);

			try
			{
				// In order to save long models, max_allowed_packet variable must be modified (on the server)
				ddbManager.save(mtc);
			}
			catch (Exception x)
			{
				log.error("MTC changes in " + mtc.getDdbId() + " not commited, reason: " + x.getMessage());
			}
		}
	}

	List<String> getModelTemplateContainerIdsRelatedTo(String modelName, Map<String, List<String>> mapping)
	{
		// One model template container with the whole modelica qualified name as id
		// And all names of other engine models that are related to this modelName
		List<String> other = mapping.get(modelName);
		List<String> related = new ArrayList<String>();
		related.add(MTC_PREFIX_NAME + modelName);
		if (other != null) for (String r : other)
			related.add(MTC_PREFIX_NAME + r);
		return related;
	}

	Parameter createParameter(String paramType, String paramName, String paramValue)
	{
		Parameter param = null;
		if (ModelicaGrammar.isRealType(paramType)) param = new ParameterFloat(paramName, Float.parseFloat(paramValue));
		else if (ModelicaGrammar.isIntegerType(paramType)) param = new ParameterInteger(paramName, Integer.parseInt(paramValue));
		else if (ModelicaGrammar.isStringType(paramType)) param = new ParameterString(paramName, paramValue);
		else if (ModelicaGrammar.isBooleanType(paramType)) param = new ParameterBoolean(paramName, Boolean.parseBoolean(paramValue));
		return param;
	}

	SimulatorInst getOrCreateModelicaSimulatorInst(DDBManager ddbmanager, String modelicaVersion)
	{
		SimulatorInst modelicaSim = ddbmanager.findSimulator(Simulator.MODELICA, modelicaVersion);
		if (modelicaSim == null)
		{
			log.debug("Creating Modelica simulator, version " + modelicaVersion);
			modelicaSim = new SimulatorInst(Simulator.MODELICA, modelicaVersion);
			modelicaSim = ddbmanager.save(modelicaSim);
		}
		return modelicaSim;
	}

	ModelTemplate findModelTemplate(ModelTemplateContainer mtc, SimulatorInst simulator)
	{
		for (ModelTemplate mt : mtc.getModelTemplates())
			if (mt.getSimulator().equals(simulator)) return mt;
		return null;
	}

	/**
	 * Read mapping between Modelica models and other Engine models from a csv file file (other Engines being [EUROSTAG, PSSE]) A single Modelica model may correspond to more than one source models (multiple Eurostag models implemented using a single
	 * Modelica model) Modelica model names can be fully qualified (PowerSystems.Electrical.Machines...) to avoid ambiguities
	 */
	static Map<String, List<String>> readModelMappingFile(File mappingFile) throws Exception
	{
		Map<String, List<String>> mapping = new HashMap<String, List<String>>();
		try (BufferedReader reader = new BufferedReader(new FileReader(mappingFile)))
		{
			String modelicaModelName;
			String otherEngineModelName;
			String line = reader.readLine();
			line = reader.readLine();
			while (line != null)
			{
				modelicaModelName = line.split(";")[0];
				otherEngineModelName = line.split(";")[1];

				if (!mapping.containsKey(modelicaModelName))
				{
					List<String> eurModels = new ArrayList<String>();
					eurModels.add(otherEngineModelName);
					mapping.put(modelicaModelName, eurModels);
				}
				else mapping.get(modelicaModelName).add(otherEngineModelName);

				line = reader.readLine();
			}
			return mapping;
		}
	}

	private String				host;
	private String				remotinPort;
	private String				user;
	private String				password;

	private static final String	MTC_PREFIX_NAME				= "MTC_";
	private static final String	DEFAULT_HOST				= "127.0.0.1";
	private static final String	DEFAULT_USER				= "user";
	private static final String	DEFAULT_PASSWORD			= "password";
	private static final String	DEFAULT_REMOTING_PORT		= "8080";
	private static final String	DDBMANAGERJNDINAME			= "ejb:iidm-ddb-ear/iidm-ddb-ejb-0.0.1-SNAPSHOT/DDBManagerBean!eu.itesla_project.iidm.ddb.service.DDBManager";
	private static final String	DEFAULT_MODELICA_VERSION	= "3.2";

	private static final String	REGULATORS_PACKAGE_NAME		= "Regulators";
	private static final String	LIB_PACKAGE_NAME			= "PowerSystems";

	private static final Logger log							= LoggerFactory.getLogger(DdbPSLibImporter.class);
}
