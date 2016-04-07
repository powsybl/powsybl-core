/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.service;

import java.util.List;

import eu.itesla_project.iidm.ddb.model.ConnectionSchema;
import eu.itesla_project.iidm.ddb.model.DefaultParameters;
import eu.itesla_project.iidm.ddb.model.Equipment;
import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.ParameterTable;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface DDBManager {
	
	public ModelTemplateContainer save(ModelTemplateContainer mtcInstance);
	public List<ModelTemplateContainer> findModelTemplateContainerAll();
	public int findModelTemplateContainerAllCount();
	public List<ModelTemplateContainer> findModelTemplateContainerAllMaxResults(int firstResult, int maxResult);
	public ModelTemplateContainer findModelTemplateContainer(String ddbId);
	public void delete(ModelTemplateContainer mtcInstance);
	
	public ParametersContainer save(ParametersContainer pcInstance);
	public ParametersContainer findParametersContainer(String ddbId);
	public List<ParametersContainer> findParametersContainerAll();
	public int findParametersContainerAllCount();
	public List<ParametersContainer> findParametersContainerAllMaxResults(int firstResult, int maxResult);
	public void delete(ParametersContainer pcInstance);
	
	
	public Parameters save(Parameters pars);
	public Parameter save(Parameter par);
	
	public DefaultParameters save(DefaultParameters defPars);


	public Equipment save(Equipment eqInstance);
	public Equipment findEquipment(String cimId);
	public List<Equipment> findEquipmentsAll();
	public int findEquipmentAllCount();
	public List<Equipment> findEquipmentsAllMaxResults(int firstResult, int maxResult);
	public void delete(Equipment eqInstance);
	
	public Internal save(Internal internalInstance);
	public Internal findInternal(String nativeId);
	public List<Internal> findInternalsAll();
	public int findInternalsAllCount();
	public List<Internal> findInternalsAllMaxResults(int firstResult, int maxResult);
	public void delete(Internal internalInstance);
	
	public SimulatorInst findSimulator(Simulator simulator, String version);
	public List<SimulatorInst> findSimulatorsAll();
	public int findSimulatorsAllCount();
	public SimulatorInst save(SimulatorInst simInst);
	public void delete(SimulatorInst simInst);

	//----------------
	
	public ModelTemplate findModelTemplate(Equipment eqInstance, SimulatorInst simulator);
	public Parameters findParameters(Equipment eqInstance, SimulatorInst simulator);
	public String getStringParameter(Equipment eqInstance, SimulatorInst simulator, String name);
	public Float getFloatParameter(Equipment eqInstance, SimulatorInst simulator, String name);
	public Integer getIntegerParameter(Equipment eqInstance, SimulatorInst simulator, String name);
	public Boolean getBooleanParameter(Equipment eqInstance, SimulatorInst simulator, String name);
	public ParameterTable getTableParameter(Equipment eqInstance, SimulatorInst simulator, String name);
	public byte[] getModelData(Equipment eqInstance, SimulatorInst simulator, String dname);

	public ModelTemplate findModelTemplate(Internal internalInstance, SimulatorInst simulator);
	public Parameters findParameters(Internal internalInstance, SimulatorInst simulator);
	public String getStringParameter(Internal internalInstance, SimulatorInst simulator, String name);
	public Float getFloatParameter(Internal internalInstance, SimulatorInst simulator, String name);
	public Integer getIntegerParameter(Internal internalInstance, SimulatorInst simulator, String name);
	public Boolean getBooleanParameter(Internal internalInstance, SimulatorInst simulator, String name);
	public ParameterTable getTableParameter(Internal internalInstance, SimulatorInst simulator, String name);
	public byte[] getModelData(Internal internalInstance, SimulatorInst simulator, String dname);
	
	public Parameters findParameters(ParametersContainer parametersContainer, SimulatorInst simulator);
	public String getStringParameter(ParametersContainer parametersContainer, SimulatorInst simulator, String name);
	public Float getFloatParameter(ParametersContainer parametersContainer, SimulatorInst simulator, String name);
	public Integer getIntegerParameter(ParametersContainer parametersContainer, SimulatorInst simulator, String name);
	public Boolean getBooleanParameter(ParametersContainer parametersContainer, SimulatorInst simulator, String name);
	public ParameterTable getTableParameter(ParametersContainer parametersContainer, SimulatorInst simulator, String name);
	
	
	public ConnectionSchema save(ConnectionSchema schema);
	public ConnectionSchema findConnectionSchema(String cimId,SimulatorInst simulator);
	public List<ConnectionSchema> findConnectionSchemasAll();
	public int findConnectionSchemasAllCount();
	public List<ConnectionSchema> findConnectionSchemasAllMaxResults(int firstResult, int maxResult);
	public void delete(ConnectionSchema schema);
	
	
}
