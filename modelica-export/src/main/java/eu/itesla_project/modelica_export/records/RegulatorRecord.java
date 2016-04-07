/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.eurostag_modelica.Converter;
import eu.itesla_project.iidm.ddb.model.*;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.*;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.psse.PsseFixedData;
import eu.itesla_project.modelica_export.util.psse.PsseModDefaultTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class RegulatorRecord extends ModelicaRecord {

	public RegulatorRecord(GeneratorRecord genRecord, Internal internal, String eqName, SimulatorInst eurSimulator, SourceEngine sourceEngine) {
		this.genRecord = genRecord;
		this.regulator = internal;
		this.eqName = eqName.trim();
		this.sourceSim = eurSimulator;
		this.sourceEngine = sourceEngine;
	}

	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		String modelicaName = parseName(this.regulator.getNativeId());
		modelicaName = StaticData.PREF_REG + modelicaName;
		modContext.dictionary.add(this.regulator, modelicaName);
		super.setModelicaName(modelicaName);

		Internal in = ddbManager.findInternal(this.regulator.getNativeId());
		ModelTemplate model = null;
		if (in != null) {
			if (this.sourceEngine instanceof EurostagEngine) {
				convertEur2Mod(ddbManager, sourceSim, modelicaSim);

				in = ddbManager.findInternal(this.regulator.getNativeId());
				model = ddbManager.findModelTemplate(in, modelicaSim);

				List<Parameters> parameters = in.getParametersContainer().getParameters();
				Parameters modelicaParameters = null;
				for (Parameters param : parameters) {
					if (param.getSimulator().equals(modelicaSim))
						modelicaParameters = param;
				}

				if (model != null) {

					String data = new String(model.getData(StaticData.MO));
					dataInit = new String(model.getData(StaticData.MO_INIT));
					super.setModelData(data);

					if (modelicaParameters == null || modelicaParameters.getParameters().isEmpty()) {
						List<DefaultParameters> defSetParams = model.getDefaultParameters();
						List<Parameter> defParameters = new ArrayList<Parameter>();

						// Cogemos el setNum correspondiente // por defecto se
						// coge el primero
						int setNum = -1;
						if (modelicaParameters != null && modelicaParameters.getParameters().isEmpty())
							setNum = modelicaParameters.getDefParamSetNum();

						if (setNum != -1) {
							if ((defSetParams != null) && (!defSetParams.isEmpty())) {
								for (DefaultParameters dp : defSetParams) {
									if (dp.getSetNum() == setNum) {
										defParameters = dp.getParameters();
										break;
									}
								}
							} else
								_log.error("Regulator " + this.getModelicaName() + " has not default parameters.");

						}
						if (defParameters == null || defParameters.isEmpty())
							defParameters = defSetParams.get(0).getParameters();

						for (Parameter param : defParameters) {
							regParameters.add(param);
							addParamInMap(param.getName(), param.getValue().toString());
						}
					}
					super.setModelicaType(model.getTypeName());
				} else {
					_log.warn("MODELICA Model Template does not exist in DDB (1)");

					model = ddbManager.findModelTemplate(in, modelicaSim);

					if (model != null) {

						String data = new String(model.getData(StaticData.MO));
						if (this.sourceEngine instanceof EurostagEngine) {
							dataInit = new String(model.getData(StaticData.MO_INIT));
						}
						super.setModelData(data);

						parameters = in.getParametersContainer().getParameters();
						modelicaParameters = null;
						for (Parameters param : parameters) {
							if (param.getSimulator().equals(modelicaSim))
								modelicaParameters = param;
						}

						if (modelicaParameters == null) {
							List<DefaultParameters> defSetParams = model.getDefaultParameters();
							List<Parameter> defParameters = new ArrayList<Parameter>();
							if ((defSetParams != null) && (!defSetParams.isEmpty()))
								defParameters = defSetParams.get(0).getParameters();
							for (Parameter param : defParameters) {
								regParameters.add(param);
								addParamInMap(param.getName(), param.getValue().toString());
							}
						}
						super.setModelicaType(model.getTypeName());
					} else {
						super.setCorrect(false);
						_log.warn("MODELICA Model Template does not exist in DDB. (2)");
					}
				}
			} else if (this.sourceEngine instanceof PsseEngine) {
				model = ddbManager.findModelTemplate(in, modelicaSim);
				_log.info("REGULATOR MODELICA MODELI: " + in.getNativeId() + " in modelica sim " + modelicaSim + " model " + model.getTypeName());
				Parameters parameters = ddbManager.findParameters(in, modelicaSim);
				Parameters modelicaParameters = null;
				if (parameters != null) {
					modelicaParameters = parameters;
				} else {
					// Cogemos directamente los de PSSE (que en teoria tienen el
					// mismo nombre)
					modelicaParameters = ddbManager.findParameters(in, this.sourceSim);
				}
				
				if (model != null) {

					String data = new String(model.getData(StaticData.MO));
					super.setModelData(data);

					if (modelicaParameters == null || modelicaParameters.getParameters().isEmpty()) {
						List<DefaultParameters> defSetParams = model.getDefaultParameters();
						List<Parameter> defParameters = new ArrayList<Parameter>();

						// Cogemos el setNum correspondiente // por defecto se
						// coge el primero
						int setNum = -1;
						if (modelicaParameters != null && modelicaParameters.getParameters().isEmpty())
							setNum = modelicaParameters.getDefParamSetNum();

						if (setNum != -1) {
							if ((defSetParams != null) && (!defSetParams.isEmpty())) {
								for (DefaultParameters dp : defSetParams) {
									if (dp.getSetNum() == setNum) {
										defParameters = dp.getParameters();
										break;
									}
								}
							} else
								_log.error("Regulator " + this.getModelicaName() + " has not default parameters.");

						}
						if (defParameters == null || defParameters.isEmpty())
							defParameters = defSetParams.get(0).getParameters();

						for (Parameter param : defParameters) {
							regParameters.add(param);
							addParamInMap(param.getName(), param.getValue().toString());
						}
					} else {
						List<Parameter> parList = modelicaParameters.getParameters();
						for (Parameter p : parList) {
							regParameters.add(p);
						}
					}
					super.setModelicaType(model.getTypeName());
				} else {
					_log.warn("MODELICA Model Template does not exist in DDB (3)");

					model = ddbManager.findModelTemplate(in, modelicaSim);

					if (model != null) {

						String data = new String(model.getData(StaticData.MO));
						if (this.sourceEngine instanceof EurostagEngine) {
							dataInit = new String(model.getData(StaticData.MO_INIT));
						}
						super.setModelData(data);

						List<Parameters> parametersList = in.getParametersContainer().getParameters();
						modelicaParameters = null;
						for (Parameters param : parametersList) {
							if (param.getSimulator().equals(modelicaSim))
								modelicaParameters = param;
						}

						if (modelicaParameters == null) {
							List<DefaultParameters> defSetParams = model.getDefaultParameters();
							List<Parameter> defParameters = new ArrayList<Parameter>();
							if ((defSetParams != null) && (!defSetParams.isEmpty()))
								defParameters = defSetParams.get(0).getParameters();
							for (Parameter param : defParameters) {
								regParameters.add(param);
								addParamInMap(param.getName(), param.getValue().toString());
							}
						}
						super.setModelicaType(model.getTypeName());
					}
				}
			}
		}

		setParameters();
	}

	private void convertEur2Mod(DDBManager ddbManager, SimulatorInst eurSimulator, SimulatorInst modSimulator) {
		String nativeId = this.regulator.getNativeId();
		String eurostagVersion = eurSimulator.getVersion();
		String modelicaVersion = modSimulator.getVersion();
		try {
			Converter eurostagModelicaConverter = new Converter(ddbManager, eurostagVersion, modelicaVersion);
			eurostagModelicaConverter.convertAndSaveInternal(nativeId, true);
		} catch (Throwable e) {
			_log.error(e.getMessage(), e);
		}
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		// Nordic 32 specific case OELPSAT (XD & XQ parameters)
		if (super.getModelicaType().equals("oelpsat")) {
			IIDMParameter parameter = new IIDMParameter(EurostagFixedData.XD, this.genRecord.getGenParamsMap().get(EurostagFixedData.XD));
			this.iidmregParameters.add(parameter);
			addParamInMap(parameter.getName(), parameter.getValue().toString());

			parameter = new IIDMParameter(EurostagFixedData.XQ, this.genRecord.getGenParamsMap().get(EurostagFixedData.XQ));
			this.iidmregParameters.add(parameter);
			addParamInMap(parameter.getName(), parameter.getValue().toString());
		}
		// Nordic 32

		if (super.getModelicaType() != null)
			this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE);

		this.addValue(super.getModelicaName());
		this.addValue(" (");
		this.addValue(StaticData.NEW_LINE);

		// Remove not used parameters that comme from module psse-imp-exp
		Map<String, List<String>> paramsOutList = new HashMap<String, List<String>>();
		List<String> params = new ArrayList<String>();
		params.add("IM");
		params.add("IM1");

		paramsOutList.put("ESST1A", params);
		paramsOutList.put("IEEEST", params);

		params = new ArrayList<String>();
		params.add("Switch");
		paramsOutList.put("IEEEX1", params);
		paramsOutList.put("ESDC1A", params);
		paramsOutList.put("ESDC2A", params);

		params = new ArrayList<String>();
		params.add("ICS1");
		params.add("REMBUS1");
		params.add("ICS2");
		params.add("REMBUS2");
		paramsOutList.put("PSS2A", params);

		if ((iidmregParameters != null) && (!iidmregParameters.isEmpty())) {
			for (int i = 0; i < iidmregParameters.size() - 1; i++) {
				if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.ESST1A) && paramsOutList.get("ESST1A").contains(iidmregParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.IEEEST) && paramsOutList.get("IEEEST").contains(iidmregParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.PSS2A) && (paramsOutList.get("PSS2A").contains(iidmregParameters.get(i).getName()))) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.IEEEX1) && paramsOutList.get("IEEEX1").contains(iidmregParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.ESDC1A) && paramsOutList.get("ESDC1A").contains(iidmregParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.ESDC2A) && paramsOutList.get("ESDC2A").contains(iidmregParameters.get(i).getName())) {
					continue;
				}
				
				this.addValue("\t " + iidmregParameters.get(i).getName() + " = " + iidmregParameters.get(i).getValue() + ",");
				this.addValue(StaticData.NEW_LINE);
			}
			if ((regParameters != null) && (!regParameters.isEmpty())) {
				this.addValue("\t " + iidmregParameters.get(iidmregParameters.size() - 1).getName() + " = " + iidmregParameters.get(iidmregParameters.size() - 1).getValue() + ",");
			} else {
				this.addValue("\t " + iidmregParameters.get(iidmregParameters.size() - 1).getName() + " = " + iidmregParameters.get(iidmregParameters.size() - 1).getValue());
			}
			this.addValue(StaticData.NEW_LINE);
		}

		if (!regParameters.isEmpty()) {
			for (int i = 0; i < regParameters.size() - 1; i++) {
				if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.ESST1A) && paramsOutList.get("ESST1A").contains(regParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.IEEEST) && paramsOutList.get("IEEEST").contains(regParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.PSS2A) && (paramsOutList.get("PSS2A").contains(regParameters.get(i).getName()))) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.IEEEX1) && paramsOutList.get("IEEEX1").contains(regParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.ESDC1A) && paramsOutList.get("ESDC1A").contains(regParameters.get(i).getName())) {
					continue;
				} else if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.ESDC2A) && paramsOutList.get("ESDC2A").contains(regParameters.get(i).getName())) {
					continue;
				}
				
				if(this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.SCRX) && regParameters.get(i).getName().equalsIgnoreCase("C_SWITCH")) {
					boolean cswitch = regParameters.get(i).getValue().equals(new Double(1.0)) ? true : false; 
					this.addValue("\t " + regParameters.get(i).getName() + " = " + cswitch + ",");
					this.addValue(StaticData.NEW_LINE);
					continue;
				}
				
				this.addValue("\t " + regParameters.get(i).getName() + " = " + regParameters.get(i).getValue() + ",");
				this.addValue(StaticData.NEW_LINE);
			}
			this.addValue("\t " + regParameters.get(regParameters.size() - 1).getName() + " = " + regParameters.get(regParameters.size() - 1).getValue());
			this.addValue(StaticData.NEW_LINE);
		}

		this.addValue("\t " + EurostagFixedData.ANNOT);

		iidmregParameters = null;
		regParameters = null;
	}

	private void setParameters() {
		IIDMParameter parameter;
		iidmregParameters = new ArrayList<IIDMParameter>();

		if (this.sourceEngine instanceof EurostagEngine) {
			parameter = new IIDMParameter(StaticData.SNREF, StaticData.SNREF_VALUE);
			this.iidmregParameters.add(parameter);
			addParamInMap(parameter.getName(), parameter.getValue().toString());

			parameter = new IIDMParameter(EurostagFixedData.SN, this.genRecord.getGenParamsMap().get(EurostagFixedData.SN));
			this.iidmregParameters.add(parameter);
			addParamInMap(parameter.getName(), parameter.getValue().toString());

			parameter = new IIDMParameter(EurostagFixedData.PN, this.genRecord.getGenParamsMap().get(EurostagFixedData.PN));
			this.iidmregParameters.add(parameter);
			addParamInMap(parameter.getName(), parameter.getValue().toString());

			parameter = new IIDMParameter(EurostagFixedData.PNALT, this.genRecord.getGenParamsMap().get(EurostagFixedData.PNALT));
			this.iidmregParameters.add(parameter);
			addParamInMap(parameter.getName(), parameter.getValue().toString());
		} else if (this.sourceEngine instanceof PsseEngine) {
			if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.SEXS)) {
				parameter = new IIDMParameter(PsseFixedData.EC0, this.genRecord.getGenParamsMap().get(PsseFixedData.V_0.toUpperCase()));
				this.iidmregParameters.add(parameter);
				addParamInMap(parameter.getName(), parameter.getValue().toString());
			}
			if (this.getModelicaType().equalsIgnoreCase(PsseModDefaultTypes.SCRX)) {
				Bus bus = this.genRecord.getGenerator().getTerminal().getBusBreakerView().getBus();
				float busVoltage = bus.getV();
				double v0 = busVoltage / bus.getVoltageLevel().getNominalV();
				parameter = new IIDMParameter(PsseFixedData.V_0, v0);
				this.iidmregParameters.add(parameter);
				addParamInMap(parameter.getName(), parameter.getValue().toString());
				
				parameter = new IIDMParameter(PsseFixedData.V_c0, v0);
				this.iidmregParameters.add(parameter);
				addParamInMap(parameter.getName(), parameter.getValue().toString());
			}
		}
	}

	private void addParamInMap(String name, String value) {
		if (!this.regParamsMap.containsKey(name.toUpperCase())) {
			this.regParamsMap.put(name.toUpperCase(), value);
		}
	}

	/**
	 * In Regulator's case convert Regulator DDB nativeId to Modelica name
	 */
	@Override
	public String parseName(String name) { // example: flr flr_BLAYAI1
		String parsedName = name.trim();
		parsedName = parsedName.replaceAll("\\-", "_");
		eqName = eqName.replaceAll("\\-", "_");

		if (name.length() > eqName.length())
			parsedName = parsedName.substring(eqName.length());
		else if (eqName.length() > name.length())
			parsedName = eqName.substring(name.length());

		if (parsedName.trim().startsWith("_"))
			parsedName = parsedName.trim().replaceFirst("_", "").toLowerCase();

		parsedName = parsedName + "_" + eqName;
		parsedName = parsedName.replaceAll(StaticData.DOT, "_");

		return parsedName;
	}

	public String getDataInit() {
		return dataInit;
	}

	public List<Parameter> getRegParameters() {
		return regParameters;
	}

	public List<IIDMParameter> getIidmregParameters() {
		return iidmregParameters;
	}

	public Map<String, String> getRegParamsMap() {
		return regParamsMap;
	}

	public void setRegParamsMap(Map<String, String> regParamsMap) {
		this.regParamsMap = regParamsMap;
	}

	@Override
	public RegulatorRecord getClassName() {
		return this;
	}

	protected Internal regulator;
	private GeneratorRecord genRecord;
	private String eqName;
	private SimulatorInst sourceSim;
	private SourceEngine sourceEngine;
	private List<Parameter> regParameters = new ArrayList<Parameter>();
	private List<IIDMParameter> iidmregParameters = new ArrayList<IIDMParameter>();
	private Map<String, String> regParamsMap = new HashMap<String, String>();

	private String dataInit;

	private static final Logger _log = LoggerFactory.getLogger(RegulatorRecord.class);
}