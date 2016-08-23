/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.initialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmodelica.javaomc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.*;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.modelica_export.records.*;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.Utils;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class Initialization {
	
	/**
	 * 
	 * @param omc
	 * @param generator
	 * @param generatoRecord
	 * @param ddbManager
	 * @param modelicaSim
	 * @param regRecords
	 * @param regInitModels
	 */
	public Initialization(JavaOMCAPI omc, DDBManager ddbManager, Path temporaryDir, List<InitializationData> initializationDataList) {
		this.omc = omc;
		this.tmpDir = temporaryDir;
		this.ddbManager = ddbManager;
		this.initializationDataList = initializationDataList;
		
		GeneratorInitData genInitData;
		List<RegulatorInitData> regsInitDataList;
		for(InitializationData initData : initializationDataList) {
			genInitData = createGeneratorInitData(initData);
			initData.setGeneratorInitData(genInitData);
			
			regsInitDataList = createRegulatorsInitData(initData);
			initData.setRegulatorsInitDataList(regsInitDataList);
		}
		
		this.fileName = "log/machines" + StaticData.MO_INIT_EXTENSION;
		File file = new File(this.fileName);
		if(!file.exists()) {
			file.getParentFile().mkdir();
		}
		
		try {
			this.writerMo = new FileWriter(file);
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}
	}
	
	private void writeInitRegulatorsModels() {
		StringWriter writer = new StringWriter();
		List<String> regsAdded = new ArrayList<String>();
		String modelData;
		String modelName;

		for(InitializationData initData : initializationDataList) {
			Map<Internal, RegulatorRecord> regRecordsMap = initData.getRegulatorRecordsMap();
			
			for(Internal internal : regRecordsMap.keySet()) {
				RegulatorRecord regRecord = regRecordsMap.get(internal);
				modelData = regRecord.getDataInit();
				modelName = searchModelName(modelData);
 
				if(!regsAdded.contains(modelName)) {
					if(modelData != null) {
						writer.append(modelData);
						writer.append(StaticData.NEW_LINE);
						
						regsAdded.add(modelName);
					}
				}
			}
		}

		String fileName = "log/macroblocks" + StaticData.MO_EXTENSION;
		FileWriter fileWriter;
		try {
			//Save file with regulator's init in order to load them in OMC
			Path filePath = Paths.get(fileName);
			fileWriter = new FileWriter(fileName);
			fileWriter.write(writer.toString());
			fileWriter.close();
			
			this.omc.loadFile(filePath.toAbsolutePath().toString());
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		} catch (ConnectException e) {
			_log.error(e.getMessage(), e);
		}
	}
	
	private void writeInitMachines() throws ConnectException {
		String modelName = null;
		
		//File header
        try {
			writerMo.append(StaticData.WITHIN);
	        writerMo.append(StaticData.NEW_LINE);
	        
			for(InitializationData initData : this.initializationDataList) {
				/*this.fileName = "log/";
				File file = new File(this.fileName);
				if(!file.exists()) {
					file.mkdir();
				}
				
				modelName = initData.getModelName();
				String fileInitName = "log/" + modelName + StaticData.MO_EXTENSION;
				try {
					this.writerMo = new FileWriter(fileInitName);
				} catch (IOException e) {
					_log.error(e.getMessage(), e);
				}
				
				writerMo.append(StaticData.WITHIN);
		        writerMo.append(StaticData.NEW_LINE);*/
				
				
				modelName = initData.getModelName();
				
		        writerMo.append(StaticData.MODEL + modelName);
		        writerMo.append(StaticData.NEW_LINE);
				
				//Initialization models
				writerMo.append("    " + initData.getGeneratorInitData().getGenModel() + StaticData.WHITE_SPACE + initData.getGeneratorRecord().getModelicaName());
				writerMo.append(StaticData.NEW_LINE);
				writerMo.append("   (");
				writerMo.append(StaticData.NEW_LINE);		
						
				//Add Generator Initialization parameters
				addGeneratorInitParameters(initData.getGeneratorInitData());
				
				writerMo.append("    " + EurostagFixedData.ANNOT);
				writerMo.append(StaticData.NEW_LINE);
				
				//Add Regulators Initialization parameters
				addRegulatorInitParameters(initData.getRegulatorsInitDataList());
				
				writerMo.append("    " + StaticData.EQUATION);
				writerMo.append(StaticData.NEW_LINE);
				
				//Connects between initialization models
				addConnections(initData.getGeneratorInitData(), initData.getRegulatorsInitDataList());
				
				writerMo.append(StaticData.END_MODEL + modelName + StaticData.SEMICOLON);
		        writerMo.append(StaticData.NEW_LINE);
				//writerMo.close();
			}
			writerMo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Creates the initialization Modelica file in order to initialize all machines.
	 * @throws IOException
	 */
	public void init() throws IOException, ConnectException {
		
		writeInitRegulatorsModels();
		
		writeInitMachines();
		
		InitializationRunner init = new InitializationRunner(omc, new File(this.fileName), initializationDataList);
		init.initializer();
		
		for(InitializationData initData : initializationDataList) {
			List<RegulatorInitData> regInitDataList = initData.getRegulatorsInitDataList();
			
			for(RegulatorInitData regInit : regInitDataList) {
				regInit.addRegRecordParameters(initData.getRegInitializedValues().get(regInit.getRegName()));
			}
			
			initData.getGeneratorInitData().addGenRecordParameters(initData.getGenInitializedValues());
		}
	}
	
	private void addGeneratorInitParameters(GeneratorInitData genInitData) throws IOException {
		int i=0;
		int size = genInitData.getParamsMap().entrySet().size();
		
		for(String param : genInitData.getParamsMap().keySet()) {
			String paramName = param;
			String paramValue = genInitData.getParamsMap().get(param);
			
			if(i < size-1)
				writerMo.append("\t" + paramName + " = " + paramValue + ",");
			else 
				writerMo.append("\t" + paramName + " = " + paramValue);
			writerMo.append(StaticData.NEW_LINE);
			i++;
		}
	}

	private void addRegulatorInitParameters(List<RegulatorInitData> regInitData) throws IOException {
		
		for(RegulatorInitData reg : regInitData) {
			writerMo.append("  " + reg.getRegModel() + StaticData.WHITE_SPACE + reg.getRegName());
			writerMo.append(StaticData.NEW_LINE);
			writerMo.append(" (");
			writerMo.append(StaticData.NEW_LINE);
			
			int i=0;
			int size = reg.getParamsMap().entrySet().size();			
					
			for(String param : reg.getParamsMap().keySet()) {
				String paramName = param;
				String paramValue = reg.getParamsMap().get(param);
				
				if(i < size-1)
					writerMo.append("\t" + paramName + " = " + paramValue + ",");
				else
					writerMo.append("\t" + paramName + " = " + paramValue);
				
				writerMo.append(StaticData.NEW_LINE);
				i++;
			}
			writerMo.append("  " + EurostagFixedData.ANNOT);
			writerMo.append(StaticData.NEW_LINE);
		}
	}
	
	private void addConnections(GeneratorInitData genInitData, List<RegulatorInitData> regInitData) throws IOException {
		List<String> connectedPins = new ArrayList<String>();
		String deviceFrom, deviceTo;
		
		// Connect between GEN_INIT and REG_INIT
		for(RegulatorInitData reg : regInitData) {
			for(String pinName : reg.getPinsList()) {
				if(genInitData.getPinsList().contains(pinName)) {
					deviceFrom = genInitData.getGenRecord().getModelicaName().concat(".").concat(pinName);
					deviceTo = reg.getRegName().concat(".").concat(pinName);
					
					if(connectedPins.contains(deviceFrom) && connectedPins.contains(deviceTo)) continue; 
					
					writerMo.append("\t" + EurostagFixedData.CONNECT);
					writerMo.append(deviceFrom);
					connectedPins.add(deviceFrom);
					writerMo.append(", ");
					writerMo.append(deviceTo);
					connectedPins.add(deviceTo);
					writerMo.append(StaticData.ANNOT_CONNECT);
					writerMo.append(StaticData.NEW_LINE);
				}
			}
		}
		
		// Connect between REG_INIT and REG_INIT
		String conName;
		for(RegulatorInitData reg : regInitData) {
			for(RegulatorInitData reg2 : regInitData) {
				if(reg != reg2) {
					for(String pin : reg.getPinsList()) {
						conName = findConnection(pin, reg2.getRegRecord().getModelData());

						if(conName != null && !genInitData.getPinsList().contains(conName)) {
							/*if(genInitData.getPinsList().contains(conName)) {
								writerMo.append("\t" + EurostagFixedData.CONNECT);
								
								writerMo.append(reg.getRegName());
								writerMo.append("." + pin);
								writerMo.append(", ");
								writerMo.append(genInitData.getGenRecord().getModelicaName());
								writerMo.append("." + conName);
								
								writerMo.append(EurostagFixedData.ANNOT_CONNECT);
								writerMo.append(StaticData.NEW_LINE);
							}
							else  {*/
								deviceFrom = reg.getRegName().concat(".").concat(pin);
								deviceTo = reg2.getRegName().concat(".").concat(conName);
								
								if(connectedPins.contains(deviceFrom) && connectedPins.contains(deviceTo)) continue; 
								
								writerMo.append("\t" + EurostagFixedData.CONNECT);							
								writerMo.append(deviceFrom);
								connectedPins.add(deviceFrom);
								writerMo.append(", ");
								writerMo.append(deviceTo);
								connectedPins.add(deviceTo);
								writerMo.append(EurostagFixedData.ANNOT_CONNECT);
								writerMo.append(StaticData.NEW_LINE);
							//}
						}
					}
				}
			}
		}
	}
	
	private String findConnection(String pin, String regModelData) {
		String conName = null;
		
		String line;
		String pinName;
		BufferedReader buffer = new BufferedReader(new StringReader(regModelData));
		
		try {
			while (!(line=buffer.readLine()).equals(StaticData.EQUATION)) {
				line = line.trim();
				//if (line.contains("Connectors")) {
				if (line.contains("RealOutput") || line.contains("RealInput")) {
					pinName = line.split(" ")[1];
					pinName = pinName.substring(0, pinName.length()-1);
					if (pinName.equals(pin) &&  line.split(" ").length>2) {
						conName = StaticData.PIN +  line.split(" ")[2].replaceAll("([\\W|[_]])+", "");
					} else if(pinName.equals(pin) &&  line.split(" ").length==2) {
						conName = pinName;
					}
				}
			}
			buffer.close();
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}

		return conName;
	}
	
	private GeneratorInitData createGeneratorInitData(InitializationData initData) {
		String genInitModelData = null;
		String genInitModel = null;
		ModelTemplate initializationMT = null;
		
		if(initData.getGeneratorRecord().getModelicaType().equals(EurostagModDefaultTypes.M1S_MACHINES))
			genInitModel = EurostagModDefaultTypes.M1S_INIT_MODEL;
		else if(initData.getGeneratorRecord().getModelicaType().equals(EurostagModDefaultTypes.M2S_MACHINES))
			genInitModel = EurostagModDefaultTypes.M2S_INIT_MODEL;
		
		ModelTemplateContainer mtc = ddbManager.findModelTemplateContainer(StaticData.MTC_PREFIX_NAME + genInitModel);
		
		for(ModelTemplate mt : mtc.getModelTemplates()) {
			if(mt.getTypeName().equals(genInitModel)) {
				initializationMT = mt;
				break;
			}
		}

		genInitModelData = new String(initializationMT.getData(StaticData.MO));
	
		return (new GeneratorInitData(initData.getGenerator(), initData.getGeneratorRecord(), initData.getGenerator().getName(), genInitModel, genInitModelData));
	}
	
	
	/**
	 * Creates .mo files with regulator's initialization models that they aren't presents in Power Systems Library
	 * @param regRecords
	 */
	private List<RegulatorInitData> createRegulatorsInitData(InitializationData initData) {
		String modelName = null;
		String modelData = null;
		String regName = null;
		
		Map<Internal, RegulatorRecord> regRecords = initData.getRegulatorRecordsMap();
		RegulatorInitData regulatorInit;
		List<RegulatorInitData> regInitData = new ArrayList<RegulatorInitData>();
		HashMap<String, List<String>> regulatorInitVarsByRegulator = new HashMap<String, List<String>>();
		
		//Cogemos toda la lista de reguladores y sus initVars asociadas.
		for(Internal reg : regRecords.keySet()) {
			RegulatorRecord regulatorRecord = regRecords.get(reg);
			modelData = regRecords.get(reg).getDataInit();
			modelName = searchModelName(modelData);
			regName = null;
			
			if(modelName != null)
				regName = modelName.substring(0, modelName.lastIndexOf("_")) + "_" + parseName(initData.getGenerator().getId().trim());
			
			//Calculamos las init variables de los REGULADORES (init_X)
			BufferedReader buffer = new BufferedReader(new StringReader(regulatorRecord.getModelData()));
			List<String> regulatorInitializationVars;
			
			try {
				regulatorInitializationVars = Utils.parseRegInitVariables(buffer);
				
				if(!regulatorInitVarsByRegulator.containsKey(regName)) {
					regulatorInitVarsByRegulator.put(regName, regulatorInitializationVars);
				}
				else {
					regulatorInitVarsByRegulator.get(regName).addAll(regulatorInitializationVars);
				}
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
			}
			
		}
		
		//Init variables data from gens, from regs, etc	
		Map<String, List<String>> regInitVarsFromGen = new HashMap<String, List<String>>();
		Map<String, List<String>> regInitVarsFromReg = new HashMap<String, List<String>>();
		Map<String, Map<String, List<String>>> regInitVarsFromOtherRegs = new HashMap<String, Map<String,List<String>>>();
		Map<String, List<String>> regInitOtherVars = new HashMap<String, List<String>>();
		
		for(Internal reg : regRecords.keySet()) {
			RegulatorRecord regulatorRecord = regRecords.get(reg);
			modelData = regRecords.get(reg).getDataInit();
			modelName = searchModelName(modelData);
			regName = null;
			
			if(modelName != null)
				regName = modelName.substring(0, modelName.lastIndexOf("_")) + "_" + parseName(initData.getGenerator().getId().trim());
			//Calculamos las init variables de los REGULADORES (init_X)
			BufferedReader buffer = new BufferedReader(new StringReader(regulatorRecord.getModelData()));
			HashMap<String, List<String>> regInitVarsInOthersTemp = new HashMap<String, List<String>>();
			List<String> regulatorInitializationVars;
			try {
				regulatorInitializationVars = Utils.parseRegInitVariables(buffer);
		
				buffer = new BufferedReader(new StringReader(initData.getGeneratorInitData().getGenModelData()));
				List<String> generatorInitializationVars = Utils.parseModelPins(buffer);
				
				for(String st : regulatorInitializationVars) {
					String varPin = st.replace(StaticData.INIT_VAR, StaticData.PIN);
					if(generatorInitializationVars.contains(varPin)) {
						if(!regInitVarsFromGen.containsKey(regName)) {
							List<String> pin = new ArrayList<String>();
							pin.add(st);
							regInitVarsFromGen.put(regName, pin);
						}
						else {
							regInitVarsFromGen.get(regName).add(st);
						}
					}
					else {
						if(regulatorInitializationVars.contains(varPin)) {
							if(!regInitVarsFromReg.containsKey(regName)) {
								List<String> pin = new ArrayList<String>();
								pin.add(st);
								regInitVarsFromReg.put(regName, pin);
							}
							else {
								regInitVarsFromReg.get(regName).add(st);
							}
						}
						else {
							for(String r : regulatorInitVarsByRegulator.keySet()) {
							//	if(!r.equals(regName)) {
									if(regulatorInitVarsByRegulator.get(r).contains(varPin)) {
										if(!regInitVarsFromOtherRegs.containsKey(regName)) {
											Map<String, List<String>> mapReg = new HashMap<>();
											List<String> pin = new ArrayList<>();
											pin.add(st);
											mapReg.put(r, pin);
											regInitVarsFromOtherRegs.put(regName, mapReg);
										}
										else {
											if(!regInitVarsFromOtherRegs.get(regName).containsKey(r)) {
												List<String> pin = new ArrayList<>();
												pin.add(st);
												regInitVarsFromOtherRegs.get(regName).put(r, pin);
											}
											else {
												regInitVarsFromOtherRegs.get(regName).get(r).add(st);
											}
										}
									}
									else {
										
										String initVar = st.replace(StaticData.PIN, StaticData.INIT_VAR);
										if(!regInitOtherVars.containsKey(regName)) {
											List<String> initVars = new ArrayList<>();
											initVars.add(initVar);
											regInitOtherVars.put(regName, initVars);
										}
										else {
											if(!regInitOtherVars.get(regName).contains(initVar)) {
												regInitOtherVars.get(regName).add(initVar);
											}
										}
									}
								//}
							}
						}
					}
				}
				
				regulatorInit = new RegulatorInitData(reg, regName, modelName, modelData, regulatorRecord);
				
				
				regInitData.add(regulatorInit);
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
			}
		}
		
		initData.setRegInitVarsFromGen(regInitVarsFromGen);
		initData.setRegInitVarsFromReg(regInitVarsFromReg);
		initData.setRegInitVarsFromOtherRegs(regInitVarsFromOtherRegs);
		initData.setRegInitOtherVars(regInitOtherVars);
		
		return regInitData;
	}
	
	private String searchModelName(String regInitModel) {
		String regex = "model [a-zA-Z0-9_]*";
		String patron = null;
		String name = null;
		
		if(regInitModel != null) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(regInitModel);
			boolean found = false;
			while ((matcher.find()) && (found == false)) { 
				found = true;
				patron = matcher.group();
			}		
			name = patron.split(StaticData.WHITE_SPACE)[1];
		}
		return name;
	}
	
	public String parseName(String name) {
		String parsedName = name.trim();
       	parsedName = parsedName.replaceAll(StaticData.WHITE_SPACE, "_");
       	parsedName = parsedName.replaceAll("\\.", "_");
       	parsedName = parsedName.replaceAll("\\-", "_");
       	parsedName = parsedName.replaceAll("/", "_");
       	parsedName = parsedName.replaceAll("\\+", "_");
        return parsedName;
	}

	
	public JavaOMCAPI getOmc() {
		return omc;
	}
	
	public String getFileName() {
		return fileName;
	}

	public List<RegulatorInitData> getRegInitData() {
		return regInitData;
	}
	
	protected List<InitializationData> initializationDataList;

	protected GeneratorInitData genInitData;
	protected List<RegulatorInitData> regInitData;
	
	protected Generator gen;
	protected GeneratorRecord genRecord;
	protected Map<Internal, String> regs;
	
	protected Map<String, String> initValues = new HashMap<String, String>();
	
	private DDBManager ddbManager;

	private JavaOMCAPI omc;//OMC IMPORTANTE hacer un clear al final!

	private Writer writerMo;
	private String fileName;
	
	//Tmp directory to save reg_init models in order to load them to OMC
	private Path tmpDir;
	
	private static final Logger _log	= LoggerFactory.getLogger(Initialization.class);
}
