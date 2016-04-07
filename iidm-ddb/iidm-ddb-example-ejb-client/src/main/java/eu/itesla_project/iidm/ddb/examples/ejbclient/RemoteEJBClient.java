/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.examples.ejbclient;

import javax.naming.NamingException;
import java.util.List;
import eu.itesla_project.iidm.ddb.model.Connection;
import eu.itesla_project.iidm.ddb.model.ConnectionSchema;
import eu.itesla_project.iidm.ddb.model.DefaultParameters;
import eu.itesla_project.iidm.ddb.model.Equipment;
import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.ParameterFloat;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ejbclient.EjbClientCtx;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class RemoteEJBClient {
	String DEFAULT_HOST = "127.0.0.1";
	int DEFAULT_REMOTING_PORT = 8080;
	String DEFAULT_USER = "user";
	String DEFAULT_PASSWORD = "password";
	String DDBMANAGERJNDINAME = "ejb:iidm-ddb-ear/iidm-ddb-ejb-0.0.1-SNAPSHOT/DDBManagerBean!eu.itesla_project.iidm.ddb.service.DDBManager";

	String TESTSIMULATORVERSION ="test_5.1.1";

	String mtcPrefix="MTC";
	String parContainerPrefix="PC";
	String equipmentPrefix="EQ";
	String connectionSchemaPrefix="CS";

	String genName="GENERATOR";
	String mtcGenName=mtcPrefix+"_"+genName;
	String paramGenContainer=parContainerPrefix+"_"+genName;
	String nativeInternal1Name ="INTERNAL1";
	String mtcInternal1Name=mtcPrefix+"_"+ nativeInternal1Name;
	String paramInternal1ContainerName=parContainerPrefix+"_"+ nativeInternal1Name;
	String nativeInternal2Name ="INTERNAL2";
	String mtcInternal2Name=mtcPrefix+"_"+ nativeInternal2Name;
	String paramInternal2ContainerName=parContainerPrefix+"_"+ nativeInternal2Name;
	String connectionSchemaName=connectionSchemaPrefix+"_"+genName;
	String equipmentName=equipmentPrefix+"_"+genName;



	public EjbClientCtx newEjbClientEcx() throws NamingException{
		String jbHost = System.getProperty("JBHOST");
		if ((jbHost == null) || ("".equals(jbHost))) {
			jbHost = DEFAULT_HOST;
		}
		String jbPortS = System.getProperty("JBPORT");
		if ((jbPortS == null) || ("".equals(jbPortS))) {
			jbPortS = ""+DEFAULT_REMOTING_PORT;
		}
		int jbPort=Integer.parseInt(jbPortS);
		return new  EjbClientCtx(jbHost, jbPort, DEFAULT_USER, DEFAULT_PASSWORD);
	}

	/*
	  create a connection schema with 1 equipment and 2 internals
	 */
	private void createDataExample()  {

		try (EjbClientCtx ec=newEjbClientEcx()){
			System.out.println("Creating example data");
			
			DDBManager ddbmanager = ec.connectEjb(DDBMANAGERJNDINAME);
			
			System.out.println("* creating simulator");
			SimulatorInst simulator=ddbmanager.findSimulator(Simulator.EUROSTAG, TESTSIMULATORVERSION);
			if (simulator==null) {
				simulator=new SimulatorInst(Simulator.EUROSTAG, TESTSIMULATORVERSION);
				simulator=ddbmanager.save(simulator);
			} else {
				System.out.println("* simulator already defined, id: " + simulator.getId());
			}
			
			System.out.println("* creating generator Model Template Container");
			ModelTemplateContainer mtc1=ddbmanager.findModelTemplateContainer(mtcGenName);
			if (mtc1==null){
				mtc1 = new ModelTemplateContainer(mtcGenName,
						"example model template container");
				System.out.println("* creating a new Model Template for a specific simulator");
				ModelTemplate mt1 = new ModelTemplate();
				mt1.setSimulator(simulator);
				mt1.setComment("gen model template");
				mtc1.getModelTemplates().add(mt1);
				mtc1=ddbmanager.save(mtc1);
			} else {
				System.out.println("* modeltemplate container already defined, id: " + mtc1.getId());
			}
			
			System.out.println("* creating ParametersContainer container");
			ParametersContainer pc1=ddbmanager.findParametersContainer(paramGenContainer);
			if (pc1==null) {
				pc1 = new ParametersContainer(paramGenContainer);
				//create some parameters
				Parameters pars = new Parameters(simulator);
				pars.addParameter(new ParameterFloat("apparent.power", 1150));
				pars.addParameter(new ParameterFloat("active.power", 1100));
				pars.addParameter(new ParameterFloat("turbine.power", 1000));
				pc1.getParameters().add(pars);
				pc1=ddbmanager.save(pc1);
			}  else {
				System.out.println("* ParametersContainer container PC_GEN already defined, id: " + pc1.getId());
			}
			
			System.out.println("* creating equipment");
			Equipment eq1=ddbmanager.findEquipment(equipmentName);
			if (eq1==null) {
				eq1 = new Equipment(equipmentName);
				eq1.setModelContainer(mtc1);
				eq1.setParametersContainer(pc1);
				eq1=ddbmanager.save(eq1);
			} else {
				System.out.println("* equipment already defined, id: " + eq1.getId());
			}

			System.out.println("creating an Internal model template container");
			ModelTemplateContainer mtc2=ddbmanager.findModelTemplateContainer(mtcInternal1Name);
			if (mtc2==null) {
				mtc2 = new ModelTemplateContainer(mtcInternal1Name, " description");
				ModelTemplate mt1 = new ModelTemplate(simulator," ");
				mtc2.getModelTemplates().add(mt1);
				mtc2=ddbmanager.save(mtc2);
			} else {
				System.out.println("* Internal model template container already defined, id: " +  mtc2.getId());
			}

			System.out.println("creating parameters container for the given internal");
			ParametersContainer pc2 = ddbmanager.findParametersContainer(paramInternal1ContainerName);
			if (pc2==null) {
				pc2 = new ParametersContainer(paramInternal1ContainerName);
				Parameters pars = new Parameters(simulator);
				pars.addParameter(new ParameterFloat("param_1", 10));
				pars.setDefParamSetNum(1);
				pc2.getParameters().add(pars);
				pc2=ddbmanager.save(pc2);
			} else {
				System.out.println("* parameters container already defined, id: " + pc2.getId());
			}

			System.out.println("creating internal ");
			Internal it1 = ddbmanager.findInternal(nativeInternal1Name);
			if (it1==null) {
				it1 = new Internal(nativeInternal1Name);
				it1.setModelContainer(mtc2);
				it1.setParametersContainer(pc2);
				it1=ddbmanager.save(it1);
			} else {
				System.out.println("* Internal already defined, id: " + it1.getId());
			}

			System.out.println("creating an Internal model template container");
			ModelTemplateContainer mtc3=ddbmanager.findModelTemplateContainer(mtcInternal2Name);
			if (mtc3==null) {
				
				mtc3 = new ModelTemplateContainer(mtcInternal2Name, "description");
				ModelTemplate mt1 = new ModelTemplate(simulator," ");
        		DefaultParameters dp = new DefaultParameters(1);
        		dp.addParameter(new ParameterFloat("defparam", 1.1000f));
        		mt1.getDefaultParameters().add(dp);
				mtc3.getModelTemplates().add(mt1);
				mtc3=ddbmanager.save(mtc3);
			} else {
				System.out.println("* modeltemplate container already defined, id: " +  mtc3.getId());
			}

			System.out.println("creating parameters container");
			ParametersContainer pc3 = ddbmanager.findParametersContainer(paramInternal2ContainerName);
			if (pc3==null) {
				pc3 = new ParametersContainer(paramInternal2ContainerName);
				Parameters pars = new Parameters(simulator);
				pars.addParameter(new ParameterFloat("param2", 20));
				pars.setDefParamSetNum(1);
				pc3.getParameters().add(pars);
				pc3=ddbmanager.save(pc3);
			} else {
				System.out.println("* parameters container already defined, id: " + pc2.getId());
			}

			System.out.println("creating internal");
			Internal it2 = ddbmanager.findInternal(nativeInternal2Name);
			if (it2==null) {
				it2 = new Internal(nativeInternal2Name);
				it2.setModelContainer(mtc3);
				it2.setParametersContainer(pc3);
				it2=ddbmanager.save(it2);
			} else {
				System.out.println("* Internal already defined, id: " + it2.getId());
			}

    		// creating a connection schema, linking together the equipment and the internals
    		ConnectionSchema cs = ddbmanager.findConnectionSchema(connectionSchemaName, null);
			if (cs == null) {
				System.out.println("-- Creating Connection Schema");
				cs = new ConnectionSchema(connectionSchemaName, null);
				Connection newC1 = new Connection(connectionSchemaName, 0, nativeInternal1Name, 1, null,null, 0);
				cs.getConnections().add(newC1);
				Connection newC2 = new Connection(connectionSchemaName, 0, nativeInternal2Name, 1, null,null, 0);
				cs.getConnections().add(newC2);
				cs = ddbmanager.save(cs);
			} else {
				System.out.println("Connection schema already defined: " + cs.getCimId());
			}

			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private void retrieveDataExample()  {
		try (EjbClientCtx ec=newEjbClientEcx()){
			System.out.println("Retrieving some data, example");			

			DDBManager ddbmanager = ec.connectEjb(DDBMANAGERJNDINAME);

			System.out.println("retrieving parameters for internal: " + nativeInternal1Name);
			SimulatorInst simulator=ddbmanager.findSimulator(Simulator.EUROSTAG, TESTSIMULATORVERSION);
			System.out.println("simulator found: " + simulator);

			Internal internal1=ddbmanager.findInternal(nativeInternal1Name);
			System.out.println("Internal found: " + internal1);

			if ((internal1==null) || (simulator==null)) {
				return;
			}
			ModelTemplate modelTemplate=ddbmanager.findModelTemplate(internal1,simulator);
			System.out.println("Modeltemplate found: " + modelTemplate.getId()+ ", " + modelTemplate.getSimulator());

			//retrieving  internal parameters
			Parameters parameters=ddbmanager.findParameters(internal1,simulator);
			List<Parameter> parList=parameters.getParameters();
			for (Parameter parameter : parList) {
				System.out.println("              ------    : " + parameter.getName() +", " +parameter.getValue());
			}

			System.out.println("retrieving parameters for equipment ");
			Equipment equipment=ddbmanager.findEquipment(equipmentName);
			System.out.println("Equipment found: " + equipment.getCimId());
			
			modelTemplate=ddbmanager.findModelTemplate(equipment, simulator);
			System.out.println("Modeltemplate found: " + modelTemplate.getId()+ ", " + modelTemplate.getSimulator());
			System.out.println("              data: " + modelTemplate.getData("data"));
			
			System.out.println(" ParametersContainer ddbId: " + equipment.getParametersContainer().getDdbId());
			//retrieving all parameters
			parameters=ddbmanager.findParameters(equipment, simulator);
			parList=parameters.getParameters();
			for (Parameter parameter : parList) {
				System.out.println("              ------    : " + parameter.getName() +", " +parameter.getValue());
			}

			String parName;
			//retrieving parameters by name and type
			parName="turbine.power";
			Float parValueFloat2=ddbmanager.getFloatParameter(equipment, simulator, parName);
			System.out.println("  Parameter "+parName+": " + parValueFloat2);
			

			//retrieve default param set for a particular (internal,simulator)
			Internal internal2 = ddbmanager.findInternal(nativeInternal2Name);
			ParametersContainer pc3 = internal2.getParametersContainer();
			Parameters prs=ddbmanager.findParameters(pc3, simulator);
			ModelTemplate mts=ddbmanager.findModelTemplate(internal2, simulator);
			Parameters pars=ddbmanager.findParameters(internal2, simulator);
			DefaultParameters dpars=mts.defaultParametersBySetNum(prs.getDefParamSetNum());
			System.out.println("default parameters:");
			if (dpars!=null) {
				for (Parameter defpar : dpars.getParameters()) {
					System.out.println(" - " + defpar.getName() + " = " + defpar.getValue());
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
	}

	
	private void removeDataExample()  {
		try (EjbClientCtx ec=newEjbClientEcx()){
			System.out.println("Removing example data ...");

			DDBManager ddbmanager = ec.connectEjb(DDBMANAGERJNDINAME);

			SimulatorInst simulator=ddbmanager.findSimulator(Simulator.EUROSTAG, TESTSIMULATORVERSION);
			if (simulator==null)
				return;
			
    		ConnectionSchema cs = ddbmanager.findConnectionSchema(connectionSchemaName, null);
			if (cs != null) {
				ddbmanager.delete(cs);
			}		
			
			Internal internal1=ddbmanager.findInternal(nativeInternal1Name);
			if (internal1!=null) {
				ddbmanager.delete(internal1);
			}
			
			internal1=ddbmanager.findInternal(nativeInternal2Name);
			if (internal1!=null) {
				ddbmanager.delete(internal1);
			}

			Equipment eq1=ddbmanager.findEquipment(equipmentName);
			if (eq1!=null) {
				ddbmanager.delete(eq1);
			}
			
			ModelTemplateContainer mtc1=ddbmanager.findModelTemplateContainer(mtcInternal1Name);
			if (mtc1!=null) {
				ddbmanager.delete(mtc1);
			}
			
			mtc1=ddbmanager.findModelTemplateContainer(mtcGenName);
			if (mtc1!=null) {
				ddbmanager.delete(mtc1);
			}
			
			mtc1=ddbmanager.findModelTemplateContainer(mtcInternal2Name);
			if (mtc1!=null) {
				ddbmanager.delete(mtc1);
			}
			
			ParametersContainer pc1=ddbmanager.findParametersContainer(paramInternal1ContainerName);
			if (pc1!=null) {
				ddbmanager.delete(pc1);
			}

			pc1=ddbmanager.findParametersContainer(paramGenContainer);
			if (pc1!=null) {
				ddbmanager.delete(pc1);
			}
		
			pc1=ddbmanager.findParametersContainer(paramInternal2ContainerName);
			if (pc1!=null) {
				ddbmanager.delete(pc1);
			}
			
			if (simulator!=null) {
				ddbmanager.delete(simulator);
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}  
		System.out.println("Removing example data - completed");
	}

	public static void main(String[] args) {
		RemoteEJBClient demoClient = new RemoteEJBClient();
		demoClient.createDataExample();
		demoClient.retrieveDataExample();
		demoClient.removeDataExample();
	}

}
