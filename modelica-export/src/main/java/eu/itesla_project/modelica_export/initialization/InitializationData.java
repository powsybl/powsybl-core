/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.initialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.modelica_export.records.GeneratorRecord;
import eu.itesla_project.modelica_export.records.RegulatorRecord;
import eu.itesla_project.modelica_export.util.StaticData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class InitializationData {
	
	private Generator generator;
	private GeneratorRecord generatorRecord;
	private Map<Internal, RegulatorRecord> regulatorRecordsMap;
	private GeneratorInitData generatorInitData;
	private List<RegulatorInitData> regulatorsInitDataList;
	private String modelName;
	
	//Init variables & others
	private Map<String, List<String>> regInitVarsFromGen = new HashMap<String, List<String>>();
	private Map<String, List<String>> regInitVarsFromReg = new HashMap<String, List<String>>();
	private Map<String, Map<String, List<String>>> regInitVarsFromOtherRegs = new HashMap<String, Map<String,List<String>>>();
	private Map<String, List<String>> regInitOtherVars = new HashMap<String, List<String>>();
	
	//Regulator's and generator's variables initialized
	private Map<String, String> genInitializedValues = new HashMap<String, String>();
	private Map<String, Map<String, String>> regInitializedValues = new HashMap<String, Map<String, String>>();
 
	
	public InitializationData(Generator generator, GeneratorRecord generatorRecord, Map<Internal, RegulatorRecord> regulatorRecordsMap) {
		this.generator = generator;
		this.generatorRecord = generatorRecord;
		this.regulatorRecordsMap = regulatorRecordsMap;
		this.modelName = generator.getId().trim() + StaticData.INITIALIZATION;
	}
	
	public Generator getGenerator() {
		return generator;
	}
	public void setGenerator(Generator generator) {
		this.generator = generator;
	}
	public GeneratorRecord getGeneratorRecord() {
		return generatorRecord;
	}
	public void setGeneratorRecord(GeneratorRecord generatorRecord) {
		this.generatorRecord = generatorRecord;
	}
	public Map<Internal, RegulatorRecord> getRegulatorRecordsMap() {
		return regulatorRecordsMap;
	}
	public void setRegulatorRecordsMap(
			Map<Internal, RegulatorRecord> regulatorRecordsMap) {
		this.regulatorRecordsMap = regulatorRecordsMap;
	}
	public GeneratorInitData getGeneratorInitData() {
		return generatorInitData;
	}
	public void setGeneratorInitData(GeneratorInitData generatorInitData) {
		this.generatorInitData = generatorInitData;
	}
	public List<RegulatorInitData> getRegulatorsInitDataList() {
		return regulatorsInitDataList;
	}
	public void setRegulatorsInitDataList(
			List<RegulatorInitData> regulatorsInitDataList) {
		this.regulatorsInitDataList = regulatorsInitDataList;
	}

	public Map<String, List<String>> getRegInitVarsFromGen() {
		return regInitVarsFromGen;
	}

	public void setRegInitVarsFromGen(Map<String, List<String>> regInitVarsFromGen) {
		this.regInitVarsFromGen = regInitVarsFromGen;
	}

	public Map<String, List<String>> getRegInitVarsFromReg() {
		return regInitVarsFromReg;
	}

	public void setRegInitVarsFromReg(Map<String, List<String>> regInitVarsFromReg) {
		this.regInitVarsFromReg = regInitVarsFromReg;
	}

	public Map<String, Map<String, List<String>>> getRegInitVarsFromOtherRegs() {
		return regInitVarsFromOtherRegs;
	}

	public void setRegInitVarsFromOtherRegs(
			Map<String, Map<String, List<String>>> regInitVarsFromOtherRegs) {
		this.regInitVarsFromOtherRegs = regInitVarsFromOtherRegs;
	}

	public Map<String, List<String>> getRegInitOtherVars() {
		return regInitOtherVars;
	}

	public void setRegInitOtherVars(Map<String, List<String>> regInitOtherVars) {
		this.regInitOtherVars = regInitOtherVars;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public Map<String, String> getGenInitializedValues() {
		return genInitializedValues;
	}

	public void setGenInitializedValues(Map<String, String> genInitializedValues) {
		this.genInitializedValues = genInitializedValues;
	}

	public Map<String, Map<String, String>> getRegInitializedValues() {
		return regInitializedValues;
	}

	public void setRegInitializedValues(
			Map<String, Map<String, String>> regInitializedValues) {
		this.regInitializedValues = regInitializedValues;
	}

}
