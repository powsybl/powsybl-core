/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.initialization;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmodelica.javaomc.JavaOMCAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;

import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * Class to run the dynamic simulation in Open Modelica.
 * @author Silvia Machado <machados@aia.es>
 */
public class InitializationRunner {
		public InitializationRunner(JavaOMCAPI omc, File filename, List<InitializationData> initializationDataList) {
		this.omc = omc;
		this.fileName = filename;
		this.initializationDataList = initializationDataList;
	}

	public void initializer() {
		try
		{
			Map<String, List<String>> regInitVarsFromGen;
			Map<String, List<String>> regInitVarsFromReg;
			Map<String, Map<String, List<String>>> regInitVarsFromOtherRegs;
			Map<String, List<String>> regInitOtherVars;
			
			omc.loadFile(this.fileName.getAbsolutePath());
			for(InitializationData initData : initializationDataList) {
				this.genInitValues = new HashMap<String, String>();
				this.regInitValues = new HashMap<String, Map<String, String>>();
				regInitVarsFromGen = initData.getRegInitVarsFromGen();
				regInitVarsFromReg = initData.getRegInitVarsFromReg();;
				regInitVarsFromOtherRegs = initData.getRegInitVarsFromOtherRegs();
				regInitOtherVars = initData.getRegInitOtherVars();
				
				this.modelName = initData.getModelName();
				String compString = omc.getComponents(this.modelName);
				String[] compList = compString.contains("},{") == true ? compString.split("\\},\\{") : null;
				String machine = compList != null ? compList[0].split(",")[1] : compString.split(",")[1];
				List<String> regsList = new ArrayList<>();
				List<String> componentsList = Arrays.asList(compList);
				for (int i = 1; i < componentsList.size(); i++)
				{
					String st = componentsList.get(i);
					st = st.substring(st.indexOf(",") + 1, st.length());
					String regulator = st.substring(0, st.indexOf(","));
					regsList.add(regulator);
				}

				/**
				 * Execute simulation in order to initialize system.
				 */
				long init = System.currentTimeMillis();
				omc.simulate(initData.getModelName(), "0", "1", "dassl");
				omc.getErrorString();
				_log.debug("Inicialización (ms) = " + (System.currentTimeMillis()-init));
				
				/**
				 * Getting initialized generator data in order to put it in the
				 * machine's model.
				 */
				String value;
				for (String st : EurostagFixedData.MACHINE_INIT_PAR)
				{
					value = omc.getValue(machine.concat(".").concat(st), "1");
					genInitValues.put(st, value.trim());
				}
				initData.setGenInitializedValues(genInitValues);
				/**
				 * Getting initialized regulator data in order to put it in the
				 * regulator's model
				 */
				List<String> variablesList;
				Map<String, String> valuesMap;
				String var;
				// At first we calculate which are the variables to get them from the initialization process: pin_X and regInit.
				//We get also the pin_X from the generators
				for (String reg : regsList)
				{
					//Getting the initialization variables from the generator.
					variablesList = regInitVarsFromGen.get(reg);
					if (variablesList != null && !variablesList.isEmpty())
					{
						valuesMap = new HashMap<String, String>();
						for (String st : variablesList)
						{
							var = st.replace(StaticData.INIT_VAR, StaticData.PIN);
							value = omc.getValue(machine.concat(".").concat(var), "1");
							valuesMap.put(st, value);
						}
						regInitValues.put(reg, valuesMap);
					}
					//Getting the initialization variables from the regulators.
					variablesList = regInitVarsFromReg.get(reg);
					if (variablesList != null && !variablesList.isEmpty())
					{
						valuesMap = new HashMap<String, String>();
						for (String st : variablesList)
						{
							var = st.replace(StaticData.INIT_VAR, StaticData.PIN);
							value = omc.getValue(reg.concat(".").concat(var), "1");
							valuesMap.put(st, value);
						}
						if (!regInitValues.containsKey(reg))
						{
							regInitValues.put(reg, valuesMap);
						} else
						{
							regInitValues.get(reg).putAll(valuesMap);
						}
					}
					//Getting the initialization variables that shouldn't be connected but we need its values.
					variablesList = regInitOtherVars.get(reg);
					if(variablesList != null && !variablesList.isEmpty())
					{
						valuesMap = new HashMap<String, String>();
						for (String st : variablesList)
						{
							var = st.replace(StaticData.INIT_VAR, StaticData.PIN);
							value = omc.getValue(reg.concat(".").concat(var), "1");
							valuesMap.put(st, value);
						}
						if (!regInitValues.containsKey(reg))
						{
							regInitValues.put(reg, valuesMap);
						} else
						{
							regInitValues.get(reg).putAll(valuesMap);
						}
					}
					//Gettint the initialization variables coming from other regulators.
					Map<String, List<String>> mapVarsOthers = regInitVarsFromOtherRegs.get(reg);
					if(mapVarsOthers != null && !mapVarsOthers.isEmpty())
					{
						for (String otherReg : mapVarsOthers.keySet())
						{
							variablesList = mapVarsOthers.get(otherReg);
							if (variablesList != null && !variablesList.isEmpty())
							{
								valuesMap = new HashMap<String, String>();
								for (String st : variablesList)
								{
									var = st.replace(StaticData.INIT_VAR, StaticData.PIN);
									value = omc.getValue(otherReg.concat(".").concat(var), "1");
									valuesMap.put(st, value);
								}
								if (!regInitValues.containsKey(reg))
								{
									regInitValues.put(reg, valuesMap);
								} else
								{
									regInitValues.get(reg).putAll(valuesMap);
								}
							}
						}
					}
				}
				initData.setRegInitializedValues(regInitValues);
				
				// Delete .xml, .c, .h, .o and other files created by JavaOMC API
				deleteInitFiles();
			}
			omc.clear();
//			// Delete .xml, .c, .h, .o and other files created by JavaOMC
//			deleteInitFiles();
		} catch (Exception ex)
		{
			_log.error(ex.getMessage(), ex);
		}
	}

	private void deleteInitFiles() throws IOException {
		String workingDir = System.getProperty("user.dir");
		System.out.println("Working directory = " + workingDir);
		Path dirPath = Paths.get(workingDir);
		File[] initFiles = dirPath.toFile().listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
				return (name.startsWith(modelName) && (!name.endsWith(".mat")));
			}
		});
		for (File f : initFiles)
		{
			try
			{
				boolean deleted = Files.deleteIfExists(Paths.get(f.getPath()));
				if (!deleted)
				{
					_log.error("File " + f + " has not been deleted.");
				}
				else
				{
					_log.info("Deleted: " + f.getName());
				}
			} catch (FileSystemException exc)
			{
				_log.error(exc.getMessage(), exc);
			}
		}
	}

	public Map<String, String> getGenInitValues() {
		return genInitValues;
	}

	public Map<String, Map<String, String>> getRegInitValues() {
		return regInitValues;
	}

	private List<InitializationData> initializationDataList;
	private File fileName;
	private String modelName;
	private JavaOMCAPI omc;
	private Map<String, String> genInitValues = new HashMap<String, String>();
	private Map<String, Map<String, String>> regInitValues = new HashMap<String, Map<String, String>>();

	private static final Logger _log = LoggerFactory.getLogger(InitializationRunner.class);
}
