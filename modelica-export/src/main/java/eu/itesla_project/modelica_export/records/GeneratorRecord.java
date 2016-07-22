/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.*;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.*;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;
import eu.itesla_project.modelica_export.util.psse.PsseFixedData;
import eu.itesla_project.modelica_export.util.psse.PsseModDefaultTypes;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * Create a Modelica Generator Record from IIDM Bus
 * @author Silvia Machado <machados@aia.es>
 */
public class GeneratorRecord extends ModelicaRecord {

	public GeneratorRecord(Generator generator, ConnectBusInfo busInfo, boolean isInyection, float SNREF, SimulatorInst eurSim, Map<String, Map<String, String>> paramsDictionary, SourceEngine sourceEngine) {
		this.generator = generator;
		this.isInyection = isInyection;
		this.sourceSim = eurSim;
		this.paramsDictionary = paramsDictionary;
		this.busInfo = busInfo;
		this.sourceEngine = sourceEngine;
		
		if (!isInyection)
		{
			if(this.sourceEngine instanceof EurostagEngine)
			{
				this.DEFAULT_GEN_TYPE = EurostagModDefaultTypes.DEFAULT_GEN_TYPE; 
			} else if(this.sourceEngine instanceof PsseEngine)
			{
				this.DEFAULT_GEN_TYPE = PsseModDefaultTypes.DEFAULT_GENROU_TYPE;
			}
			if (DEFAULT_GEN_TYPE.contains("."))
			{
				DEFAULT_GEN_PREFIX = DEFAULT_GEN_TYPE.substring(DEFAULT_GEN_TYPE.lastIndexOf(".") + 1);
			} else
			{
				DEFAULT_GEN_PREFIX = DEFAULT_GEN_TYPE;
			}
		} else
		{
			if(this.sourceEngine instanceof EurostagEngine)
			{
				this.DEFAULT_GEN_LOAD_TYPE = EurostagModDefaultTypes.DEFAULT_GEN_LOAD_TYPE;
			} else if(this.sourceEngine instanceof PsseEngine)
			{
				this.DEFAULT_GEN_LOAD_TYPE = PsseModDefaultTypes.DEFAULT_GEN_LOAD_TYPE;
			}
			if (DEFAULT_GEN_LOAD_TYPE.contains("."))
			{
				DEFAULT_GEN_LOAD_PREFIX = DEFAULT_GEN_LOAD_TYPE.substring(DEFAULT_GEN_LOAD_TYPE.lastIndexOf(".") + 1);
			} else
			{
				DEFAULT_GEN_LOAD_PREFIX = DEFAULT_GEN_LOAD_TYPE;
			}
			DEFAULT_GEN_LOAD_PREFIX = DEFAULT_GEN_LOAD_PREFIX + "_GEN";
		}
		setParameters(isInyection, SNREF);
	}

	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		String modelicaName;
		if (!isInyection)
		{
			modelicaName = DEFAULT_GEN_PREFIX + "_" + parseName(this.generator.getId());
		} else
		{
			modelicaName = DEFAULT_GEN_LOAD_PREFIX + "_" + parseName(this.generator.getId());
		}	
		modelicaName = StaticData.PREF_GEN + WordUtils.uncapitalize(modelicaName.substring(0, 1)) + modelicaName.substring(1);
		super.setModelicaName(modelicaName);

		Equipment eq = ddbManager.findEquipment(generator.getId());
		ModelTemplate model = null;
		if (eq != null)
		{
			if(this.generator.getEnergySource().name().equals("WIND"))
			{
				_log.info("Wind generator!");
			}
				
			model = ddbManager.findModelTemplate(eq, modelicaSim);
			if (model != null)
			{
				String data = new String(model.getData(StaticData.MO));
				super.setModelData(data);
				super.setModelicaType(model.getTypeName());

				if (super.getModelicaType().contains("."))
				{
					DEFAULT_GEN_PREFIX = super.getModelicaType().substring(super.getModelicaType().lastIndexOf(".") + 1);
				} else
				{
					DEFAULT_GEN_PREFIX = super.getModelicaType();
				}
				modelicaName = DEFAULT_GEN_PREFIX + "_" + parseName(this.generator.getId());
				modelicaName = StaticData.PREF_GEN + WordUtils.uncapitalize(modelicaName.substring(0, 1)) + modelicaName.substring(1);
				super.setModelicaName(modelicaName);

				if(this.sourceEngine instanceof EurostagEngine)
				{
					genParameters = getGeneratorParameters(ddbManager, modelicaSim, eq, model);
				} else if (this.sourceEngine instanceof PsseEngine)
				{
					genParameters = getPsseGeneratorParameters(ddbManager, modelicaSim, eq, model);
				}
			} else
			{
				_log.warn("MODELICA Model Template does not exist in DDB");
			}
		} else
		{
			if (!isInyection)
			{
				_log.info("Generator " + this.getModelicaName() + " does not exist in DDB (Equipment).");
			}
			String ddbid;
			if (!isInyection)
			{
				ddbid = StaticData.MTC_PREFIX_NAME + super.mtcMapper.get(DEFAULT_GEN_PREFIX);
			} else
			{
				ddbid = StaticData.MTC_PREFIX_NAME + super.mtcMapper.get(DEFAULT_GEN_LOAD_PREFIX);
			}
			ModelTemplateContainer mtc = ddbManager.findModelTemplateContainer(ddbid);
			String genType = null;
			if (mtc == null)
			{
				_log.warn("Source (Eurostag/PSSE) Model Template Container does not exist. Searching Default Modelica Model Template Container in DDB.");
				if (!isInyection)
				{
					genType = DEFAULT_GEN_TYPE;
					mtc = ddbManager.findModelTemplateContainer(StaticData.MTC_PREFIX_NAME + DEFAULT_GEN_TYPE);
				} else
				{
					genType = DEFAULT_GEN_LOAD_TYPE;
					mtc = ddbManager.findModelTemplateContainer(StaticData.MTC_PREFIX_NAME + DEFAULT_GEN_LOAD_TYPE);
				}
				super.setModelicaType(genType);
			}
			if (mtc != null)
			{
				for (ModelTemplate mt : mtc.getModelTemplates())
				{
					if (mt.getTypeName().equalsIgnoreCase(genType))
					{
						model = mt;
					}
				}
				if (model != null)
				{
					String data = new String(model.getData("mo"));
					super.setModelData(data);
					List<DefaultParameters> defSetParams = model.getDefaultParameters();
					List<Parameter> defParameters = defSetParams.get(0).getParameters();
					for (Parameter param : defParameters)
					{
						if (param.getValue() != null)
						{
							genParameters.add(param);
						} else
						{
							_log.warn("Paramater " + param.getName() + " doesn't have value.");
						}
					}
				} else
				{
					super.setCorrect(false);
					_log.warn("MODELICA Model Template does not exist in DDB");
				}
			} else
			{
				super.setCorrect(false);
				_log.error("MODELICA Model Template Container does not exist in DDB.");
			}
		}
		modContext.dictionary.add(this.generator, modelicaName);
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		if(!Float.isNaN(this.busInfo.getBus().getV()) && this.busInfo.isConnected())
		{
			if (super.isCorrect()){
				if(!busInfo.isConnected())
				{
					this.addValue(StaticData.COMMENT);
				}
				if (super.getModelicaType() != null)
				{
					this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE);
				} else
				{
					if (!isInyection)
					{
						this.addValue(DEFAULT_GEN_TYPE + StaticData.WHITE_SPACE);
					} else
					{
						this.addValue(DEFAULT_GEN_LOAD_TYPE + StaticData.WHITE_SPACE);
					}
				}
				this.addValue(super.getModelicaName());
				this.addValue(" (");
				this.addValue(StaticData.NEW_LINE);
				//If it is a generator or injection it will have different parameters
				if ((iidmgenParameters != null) && (!iidmgenParameters.isEmpty()))
				{
					for (int i = 0; i < iidmgenParameters.size() - 1; i++)
					{
						if(!busInfo.isConnected())
						{
							this.addValue(StaticData.COMMENT);
						}
						if(iidmgenParameters.get(i).getName().equals(PsseFixedData.Mbase) && this.changedMbse)
						{
							this.addValue("\t " + iidmgenParameters.get(i).getName() + " = " + iidmgenParameters.get(i).getValue() + ", // Mbase has been changed: Mbase > SQRT(P^2 + Q^2)");
						} else
						{
							this.addValue("\t " + iidmgenParameters.get(i).getName() + " = " + iidmgenParameters.get(i).getValue() + ", ");
						}
						this.addValue(StaticData.NEW_LINE);
					}
					if(!busInfo.isConnected())
					{
						this.addValue(StaticData.COMMENT);
					}
					if (isInyection)
					{
						this.addValue("\t " + iidmgenParameters.get(iidmgenParameters.size() - 1).getName() + " = " + iidmgenParameters.get(iidmgenParameters.size() - 1).getValue());
					} else if ((genParameters != null) && (!genParameters.isEmpty()))
					{
						if(iidmgenParameters.get(iidmgenParameters.size() - 1).getName().equals(PsseFixedData.Mbase) && this.changedMbse)
						{
							this.addValue("\t " + iidmgenParameters.get(iidmgenParameters.size() - 1).getName() + " = " + iidmgenParameters.get(iidmgenParameters.size() - 1).getValue() + ", // Mbase has been changed: Mbase > SQRT(P^2 + Q^2)");
						} else
						{
							this.addValue("\t " + iidmgenParameters.get(iidmgenParameters.size() - 1).getName() + " = " + iidmgenParameters.get(iidmgenParameters.size() - 1).getValue() + ",");
						}
					}
					this.addValue(StaticData.NEW_LINE);
				}
				if (!isInyection)
				{
					if ((genParameters != null) && (!genParameters.isEmpty()))
					{
						for (int i = 0; i < genParameters.size() - 1; i++)
						{
							if(!busInfo.isConnected())
							{
								this.addValue(StaticData.COMMENT);
							}
							this.addValue("\t " + genParameters.get(i).getName() + " = " + genParameters.get(i).getValue() + ",");
							this.addValue(StaticData.NEW_LINE);
						}
						if(!busInfo.isConnected())
						{
							this.addValue(StaticData.COMMENT);
						}
						this.addValue("\t " + genParameters.get(genParameters.size() - 1).getName() + " = " + genParameters.get(genParameters.size() - 1).getValue());
						this.addValue(StaticData.NEW_LINE);
					}
				}
				if(!this.busInfo.isConnected())
				{
					his.addValue(StaticData.COMMENT);
				}
				this.addValue("\t " + EurostagFixedData.ANNOT);
				genParameters = null;
				iidmgenParameters = null;
			} else
			{
				_log.error(this.getModelicaName() + " not added to grid model.");
			}
		} else
		{
			_log.warn("Generator " + this.getModelicaName() + " disconnected.");
			this.addValue(StaticData.COMMENT + " Generator " + this.getModelicaName() + " disconnected.");
		}
	}

	@Override
	public String parseName(String name) {
		String parsedName = name.trim();
		parsedName = parsedName.replaceAll("\\s", "_");
	       	parsedName = parsedName.replaceAll("\\.", "_");
	       	parsedName = parsedName.replaceAll("\\-", "_");
	       	parsedName = parsedName.replaceAll("/", "_");
	       	parsedName = parsedName.replaceAll("\\+", "_");
	        return parsedName;
	}
	
	private List<Parameter> getPsseGeneratorParameters(DDBManager ddbManager, SimulatorInst modelicaSim, Equipment eq, ModelTemplate modelTemplate) {
		List<Parameter> parametersList = new ArrayList<Parameter>();
		if (this.paramsDictionary.containsKey(modelTemplate.getTypeName()))
		{
			Parameters parameters = ddbManager.findParameters(eq, modelicaSim);
			if (parameters != null) //The equipment has parameters in Modelica
			{
				parametersList.addAll(parameters.getParameters());
			} else
			{
				parameters = ddbManager.findParameters(eq, sourceSim);			
				if (parameters != null) // The equipment has parameters in the source engine
				{
					parametersList.addAll(parameters.getParameters());
				} else
				{
					// Getting parameters by default in MODELICA.
					List<DefaultParameters> defSetParams = modelTemplate.getDefaultParameters();
					if ((defSetParams != null) && (!defSetParams.isEmpty()))
					{
						List<Parameter> defParameters = defSetParams.get(defSetParams.size()).getParameters();
						genParameters.addAll(defParameters);
						for(Parameter param : defParameters)
						{
							addParamInMap(param.getName(), param.getValue().toString());
						}
					} else
					{
						_log.error("Modelica model " + modelTemplate.getTypeName() + " doesn't have default parameters.");
					}
				}
			}
		} else
		{
			_log.error("Parameters dictionary doesn't have parameters for model " + modelTemplate.getTypeName());
		}
		return parametersList;
	}

	private List<Parameter> getGeneratorParameters(DDBManager ddbManager, SimulatorInst modelicaSim, Equipment eq, ModelTemplate modelTemplate) {
		List<Parameter> parametersList = new ArrayList<Parameter>();
		Map<String, String> dictMap;
		IIDMParameter parameter;
		if (this.paramsDictionary.containsKey(modelTemplate.getTypeName()))
		{
			dictMap = this.paramsDictionary.get(modelTemplate.getTypeName());
			Parameters parameters = ddbManager.findParameters(eq, modelicaSim);
			if (parameters != null) // The equipment has parameters in Modelica.
			{
				parametersList.addAll(parameters.getParameters());
			} else
			{
				parameters = ddbManager.findParameters(eq, sourceSim);
				if (parameters != null) // The equipment has parameters in the source engine
				{
					List<Parameter> paramsList = parameters.getParameters();
					Iterator<Entry<String, String>> it = dictMap.entrySet().iterator();
					String isTrafoIncluded = ddbManager.getStringParameter(eq, sourceSim, "transformer.included");
					if (isTrafoIncluded.equals(EurostagFixedData.TRAFO_INCLUDED))
					{
						trafoIncluded = true;
						parameter = new IIDMParameter(EurostagFixedData.TRAFOINCLUDED, "true");
						this.iidmgenParameters.add(parameter);
						addParamInMap(parameter.getName(), parameter.getValue().toString());
						
						parameter = new IIDMParameter(EurostagFixedData.V2, this.generator.getTerminal().getVoltageLevel().getNominalV()); 
						this.iidmgenParameters.add(parameter);
						addParamInMap(parameter.getName(), parameter.getValue().toString());
					} else
					{
						parameter = new IIDMParameter(EurostagFixedData.TRAFOINCLUDED, "false");
						this.iidmgenParameters.add(parameter);
						addParamInMap(parameter.getName(),  parameter.getValue().toString());
					}
					
					String isSaturated = ddbManager.getStringParameter(eq, sourceSim, "saturated");
					if(isSaturated.equals(EurostagFixedData.IS_SATURATED))
					{
						saturated = true;
						parameter = new IIDMParameter(EurostagFixedData.SATURATED, "true");
						this.iidmgenParameters.add(parameter);
						addParamInMap(parameter.getName(), parameter.getValue().toString());
					} else
					{
						saturated = false;
						parameter = new IIDMParameter(EurostagFixedData.SATURATED, "false");
						this.iidmgenParameters.add(parameter);
						addParamInMap(parameter.getName(), parameter.getValue().toString());
					}
					while (it.hasNext())
					{
						String modParName = it.next().getKey();
						String eurParName = dictMap.get(modParName);
						Parameter modParam = null;
						if ((!trafoIncluded) && (EurostagFixedData.TRAFO_GEN_PARAMS.contains(modParName)))
						{
							continue;
						}
						if ((eurParName != null) && (eurParName.equals("transformer.included")))
						{
							continue;
						}
						if ((eurParName != null) && (eurParName.equals("saturated")))
						{
							continue;
						}
						for (Parameter par : paramsList)
						{
							if (par.getName().equals(eurParName))
							{
								modParam = par;
								break;
							}
						}
						if (modParam != null)
						{
							if (modParam.getValue() != null)
							{
								modParam.setName(modParName);
								if(!saturated && EurostagFixedData.SATURATED_MACHINE.contains(modParName))
								{
									parameter = new IIDMParameter(modParam.getName(), 0); 
									this.iidmgenParameters.add(parameter);
									addParamInMap(parameter.getName(), parameter.getValue().toString());
								} else
								{
									parametersList.add(modParam);
									addParamInMap(modParam.getName(), modParam.getValue().toString());
								}
							} else
							{
								//If value is null for now we put a 0
								parameter = new IIDMParameter(modParName, 0);
								this.iidmgenParameters.add(parameter);
								addParamInMap(modParName, parameter.getValue().toString());
							}
						} else
						{
//							_log.error("Modelica parameter " + modParName + " doesn't exists in DDB.");
						}
					}
				} else
				{
					//Getting the parameters by default in Modelica.
					List<DefaultParameters> defSetParams = modelTemplate.getDefaultParameters();
					if ((defSetParams != null) && (!defSetParams.isEmpty()))
					{
						List<Parameter> defParameters = defSetParams.get(defSetParams.size()).getParameters();
						genParameters.addAll(defParameters);
						for(Parameter param : defParameters)
						{
							addParamInMap(param.getName(), param.getValue().toString());
						}
					} else
					{
						_log.error("Modelica model " + modelTemplate.getTypeName() + " doesn't have default parameters.");
					}
				}
			}
		} else
		{
			_log.error("Parameters dictionary doesn't have parameters for model " + modelTemplate.getTypeName());
		}
		return parametersList;
	}
	
	private void addParamInMap(String name, String value) {
		if(!this.genParamsMap.containsKey(name.toUpperCase()))
		{
			this.genParamsMap.put(name.toUpperCase(), value);
		}
	}

	public void setParameters(boolean isInyection, float SNREF) {
		IIDMParameter parameter;
		iidmgenParameters = new ArrayList<IIDMParameter>();
		float voltage = 0;
		float angle = 0;

		if (generator.getTerminal().getBusView().getBus() != null)
		{
			if (!Float.isNaN(generator.getTerminal().getBusView().getBus().getV()))
			{
				voltage = generator.getTerminal().getBusView().getBus().getV();
			}
			if (!Float.isNaN(generator.getTerminal().getBusView().getBus().getAngle()))
			{
				angle = generator.getTerminal().getBusView().getBus().getAngle();
			}
		}
		if(this.sourceEngine instanceof EurostagEngine)
		{
			if (!isInyection)
			{
				parameter = new IIDMParameter(StaticData.SNREF, StaticData.SNREF);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				float modulo = voltage / generator.getTerminal().getVoltageLevel().getNominalV();
				float angulo =  (float) (angle*Math.PI/180);
		
				double ur0 = modulo * Math.cos(angulo);
				double ui0 = modulo * Math.sin(angulo);
				
				parameter = new IIDMParameter(EurostagFixedData.UR0, ur0);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());

				parameter = new IIDMParameter(EurostagFixedData.UI0, ui0);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
			} else
			{
				parameter = new IIDMParameter(EurostagFixedData.V_0, voltage/generator.getTerminal().getVoltageLevel().getNominalV());
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				parameter = new IIDMParameter(EurostagFixedData.ANGLE_0, angle);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				//Before 2015-05-28 the sign of pelec and qelec was not changed but now we change the sign.
				float pelec = -this.generator.getTerminal().getP()/SNREF;
				parameter = new IIDMParameter(EurostagFixedData.P, pelec);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				float qelec = -this.generator.getTerminal().getQ()/SNREF;
				parameter = new IIDMParameter(EurostagFixedData.Q, qelec);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
			}
		} else if (this.sourceEngine instanceof PsseEngine)
		{
			if(!isInyection )
			{
				parameter = new IIDMParameter(PsseFixedData.V_0, voltage/generator.getTerminal().getVoltageLevel().getNominalV());
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				parameter = new IIDMParameter(PsseFixedData.ANGLE_0, angle);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				//Before 2015-05-28 the sign of pelec and qelec was not changed but now we change the sign.
				float pelec = -this.generator.getTerminal().getP();
				parameter = new IIDMParameter(PsseFixedData.P_0, pelec);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				float qelec = -this.generator.getTerminal().getQ();
				parameter = new IIDMParameter(PsseFixedData.Q_0, qelec);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
	
				double mbase = this.generator.getRatedS();
				double refValue = Math.sqrt(Math.pow(pelec, 2) + Math.pow(qelec, 2));
				if(mbase <= refValue)
				{
					mbase = 1.1*refValue;
					changedMbse = true;
				}
				parameter = new IIDMParameter(PsseFixedData.M_b, mbase);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
			} else
			{
				parameter = new IIDMParameter(PsseFixedData.V_0, voltage/generator.getTerminal().getVoltageLevel().getNominalV());
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				parameter = new IIDMParameter(PsseFixedData.ANGLE_0, angle);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				//Before 2015-05-28 the sign of pelec and qelec was not changed but now we change the sign.
				float pelec = -this.generator.getTerminal().getP();
				parameter = new IIDMParameter(PsseFixedData.P_0, pelec);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
				
				float qelec = -this.generator.getTerminal().getQ();
				parameter = new IIDMParameter(PsseFixedData.Q_0, qelec);
				this.iidmgenParameters.add(parameter);
				addParamInMap(parameter.getName(),  parameter.getValue().toString());
			}
		}
	}

	public Map<String, String> getGenParamsMap() {
		return genParamsMap;
	}

	public List<IIDMParameter> getIidmgenParameters() {
		return iidmgenParameters;
	}
	
	public Generator getGenerator() {
		return generator;
	}

	public void setGenerator(Generator generator) {
		this.generator = generator;
	}
	
	@Override
	public GeneratorRecord getClassName() {
		return this;
	}

	public boolean isChangedMbse() {
		return changedMbse;
	}

	public void setChangedMbse(boolean changedMbse) {
		this.changedMbse = changedMbse;
	}

	protected Generator generator;
	private ConnectBusInfo busInfo;
	
	private String DEFAULT_GEN_TYPE;
	private String DEFAULT_GEN_LOAD_TYPE;
	private String DEFAULT_GEN_PREFIX;
	private String DEFAULT_GEN_LOAD_PREFIX;

	private List<Parameter> genParameters = new ArrayList<Parameter>();
	private List<IIDMParameter> iidmgenParameters = new ArrayList<IIDMParameter>();
	private Map<String, String> genParamsMap = new HashMap<String, String>();
	private Map<String, Map<String, String>> paramsDictionary;

	public boolean isInyection;
	private SimulatorInst sourceSim;
	private boolean trafoIncluded = false;
	private boolean saturated = false;
	private boolean changedMbse = false;
	private SourceEngine sourceEngine;
	
	private static final Logger _log = LoggerFactory.getLogger(GeneratorRecord.class);
}
