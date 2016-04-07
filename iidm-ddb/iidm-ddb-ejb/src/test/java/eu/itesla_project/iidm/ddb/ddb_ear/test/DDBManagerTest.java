/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.ddb_ear.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.itesla_project.iidm.ddb.model.DefaultParameters;
import eu.itesla_project.iidm.ddb.model.Equipment;
import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.ParameterBoolean;
import eu.itesla_project.iidm.ddb.model.ParameterFloat;
import eu.itesla_project.iidm.ddb.model.ParameterInteger;
import eu.itesla_project.iidm.ddb.model.ParameterString;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ddb.util.Resources;
import eu.itesla_project.iidm.ddb.util.Utils;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@RunWith(Arquillian.class)
public class DDBManagerTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		MavenDependencyResolver resolver = DependencyResolvers.use(
				MavenDependencyResolver.class).loadMetadataFromPom("pom.xml");

		return ShrinkWrap
				.create(WebArchive.class, "MTCManagerTest.war")
				.addPackage(ParameterString.class.getPackage())
				.addPackage(Resources.class.getPackage())
				.addPackage(DDBManager.class.getPackage())
				.addPackage(Utils.class.getPackage())
				.addAsResource("META-INF/test-persistence_h2.xml", "META-INF/persistence.xml")
				//.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
				.addAsResource("import.sql", "import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				// Deploy our test datasource
				 .addAsWebInfResource("test-ds_h2.xml", "test-ds.xml")
				//.addAsWebInfResource("test-ds.xml", "test-ds.xml")
				.addAsLibraries(
						resolver.artifacts("org.apache.commons:commons-lang3"
								,"org.apache.ws.security:wss4j"
								,"org.apache.cxf:cxf-rt-frontend-jaxws"
								,"org.apache.cxf:cxf-rt-transports-http"
								,"org.apache.cxf:cxf-rt-ws-security"
						// ,"eu.itesla_project:commons"
						).resolveAsFiles()

				);
	}

	//@Inject
	@EJB
	DDBManager dbmanager;

	@Inject
	Logger log;
	

	public void dumpCurrentDB() {
		log.info("//***************************************************************");
		log.info("//(start DDB contents)");
		log.info("//***************************************************************");
		log.info("//-----Model template containers: ");
		List<ModelTemplateContainer> mtclist = dbmanager
				.findModelTemplateContainerAll();
		for (ModelTemplateContainer modelTemplateContainer : mtclist) {
			log.info(" - " + modelTemplateContainer.getId() + ", "
					+ modelTemplateContainer.getDdbId());
			Utils.dump(modelTemplateContainer, log);
		}

		log.info("//-----Equipments: ");
		List<Equipment> eqlist = dbmanager.findEquipmentsAll();
		for (Equipment equipment : eqlist) {
			log.info(" - " + equipment.getId() + ", " + equipment.getCimId());
			Utils.dump(equipment, log);
		}

		log.info("//-----Internals: ");
		List<Internal> ilist = dbmanager.findInternalsAll();
		for (Internal internal : ilist) {
			log.info(" - " + internal.getId() + ", " + internal.getNativeId());
			Utils.dump(internal, log);
		}

		log.info("//*************************************************************");
		log.info("//(end DDB contents)");
		log.info("//*************************************************************");
	}

	@Test
	public void test_00() throws Exception {
		log.info("TEST_00");
		log.info("* check DDB for equipment _NGEN_00005 (there should be none)");
		Equipment leq = dbmanager.findEquipment("_NGEN_00005");
		assertTrue(leq == null);
		log.info("* ok");

		log.info("* check DDB for equipment _NGEN_00006 (there should be none)");
		leq = dbmanager.findEquipment("_NGEN_00006");
		assertTrue(leq == null);
		log.info("* ok");

		log.info("* creating equipment _NGEN_00005");
		Equipment eq1 = new Equipment("_NGEN_00005");
		eq1=dbmanager.save(eq1);
		//assertNotNull(eq1.getId());
		log.info("* ok");
		Utils.dump(eq1, log);

		// create and persist a second equipment
		log.info("* creating equipment _NGEN_00006");
		Equipment eq2 = new Equipment("_NGEN_00006");
		eq2=dbmanager.save(eq2);
		assertNotNull(eq2.getId());
		log.info("* ok");
		Utils.dump(eq2, log);
		
		SimulatorInst simInst=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(simInst);

		log.info("* creating modeltemplatecontainer MTC_GEN_00005");
		ModelTemplateContainer mtc1 = new ModelTemplateContainer(
				"MTC_GEN_00005", "MTC_GEN_00005 comment");
		ModelTemplate mt1 = new ModelTemplate(simInst,"mt1comment");
		
		mtc1.getModelTemplates().add(mt1);
		mtc1=dbmanager.save(mtc1);
		assertNotNull(mtc1.getId());
		log.info("* ok");
		Utils.dump(mtc1, log);

		log.info("* current ddb state: ");
		dumpCurrentDB();

		log.info("* setting eq1 modeltemplatecontainer ");
		eq1.setModelContainer(mtc1);
		// pequman.save(mtc1);
		eq1=dbmanager.save(eq1);

		log.info("* setting eq2 modeltemplatecontainer ");
		eq2.setModelContainer(mtc1);
		// pequman.save(mtc1);
		eq2=dbmanager.save(eq2);

		log.info("* current ddb state: ");
		dumpCurrentDB();

		log.info("* getting equipment _NGEN_00005 back from db and checking that it has linked the modeltemplatecontainer");
		Equipment leq2 = dbmanager.findEquipment("_NGEN_00005");
		assertTrue(leq2 != null);
		Utils.dump(leq2, log);
		assertTrue(leq2.getModelContainer().getDdbId().equals("MTC_GEN_00005"));
		log.info("* ok");

		log.info("* trying removing modeltemplatecontainer MTC_GEN_00005 from DDB (should throw an exception, since MTC_GEN_00005 is 'used' by two equipments)");
		try {
			dbmanager.delete(mtc1);
			assertTrue(false);
		} catch (Exception e) {
			log.info("* (exception handling) ok, removing is not possible: "
					+ e.getMessage());
		}

		log.info("* current ddb state: ");
		dumpCurrentDB();

		log.info("* removing equipment eq1");
		dbmanager.delete(eq1);

		log.info("* current ddb state: ");
		dumpCurrentDB();

		log.info("* removing equipment eq2");
		dbmanager.delete(eq2);

		log.info("* current ddb state: ");
		dumpCurrentDB();

		log.info("* removing modeltemplatecontainer mtc1");
		dbmanager.delete(mtc1);

		log.info("* current ddb state: ");
		dumpCurrentDB();

	}

	// test_equipment_creation
	@Test
	public void test_01() throws Exception {
		log.info("TEST_01");
		
		SimulatorInst simInst=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(simInst);
		
		log.info("* creating modeltemplate container MTC_GEN");
		ModelTemplateContainer mtc1 = new ModelTemplateContainer("MTC_GEN",
				"MTC_GEN comment");
		log.info("* creating eurostag modeltemplate for MTC_GEN");
		ModelTemplate mt1 = new ModelTemplate(simInst,"mt1comment");
		mt1.setData("data","M1 U".getBytes("UTF-8"));
		mtc1.getModelTemplates().add(mt1);
		mtc1=dbmanager.save(mtc1);

		ParametersContainer pc1 = new ParametersContainer("PC_GEN");
		Parameters pars = new Parameters(simInst);

		pars.addParameter(new ParameterString("machine.name", "GEN"));
		pars.addParameter(new ParameterFloat("rated.apparent.power", 1150));
		pars.addParameter(new ParameterFloat("turbine.power", 1000));
		pars.addParameter(new ParameterString("model.type", "full"));
		pars.addParameter(new ParameterBoolean("transformer.included", false));
		pc1.getParameters().add(pars);
		pc1=dbmanager.save(pc1);

		log.info("* creating equipment _NGEN_TN and assigning to it MTC_GEN and PC_GEN");
		Equipment eq1 = new Equipment("_NGEN_TN"); // ???

		eq1.setModelContainer(mtc1);
		eq1.setParametersContainer(pc1);

		eq1=dbmanager.save(eq1);
		assertNotNull(eq1.getId());

		log.info("* ok, Equipment (" + eq1.getCimId()
				+ ") was persisted with id " + eq1.getId());

		dumpCurrentDB();
	}

	// test_internal_creation1
	@Test
	public void test_02() throws Exception {
		log.info("TEST_02");
		
		SimulatorInst simInst=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(simInst);
		
		log.info("creating model template container AVR_1, with an eurostag model and persist it");
		ModelTemplateContainer mtc1 = new ModelTemplateContainer("MTC_AVR1",
				"MTC_AVR1 comment");
		ModelTemplate mt1 = new ModelTemplate(simInst,"mt1comment");
		mtc1.getModelTemplates().add(mt1);
		mtc1=dbmanager.save(mtc1);

		log.info("creating parameters container PC_AVR1, with eurostag parameters and persist it");
		ParametersContainer pc1 = new ParametersContainer("PC_AVR1");
		Parameters pars = new Parameters(simInst);

		pars.addParameter(new ParameterString("macroblock", "AVR1"));
		pars.getParameters().add(new ParameterString("machine.name", "GEN"));
		pars.getParameters().add(new ParameterInteger("paramset.index", 1));
		pc1.getParameters().add(pars);
		pc1=dbmanager.save(pc1);

		log.info("creating internal AVR_1");
		Internal it1 = new Internal("AVR_1");

		it1.setModelContainer(mtc1);
		it1.setParametersContainer(pc1);

		it1=dbmanager.save(it1);
		assertNotNull(it1.getId());

		log.info("Internal (" + it1.getNativeId() + ") was persisted with id "
				+ it1.getId());
		dumpCurrentDB();

	}

	// test_internal_creation2
	@Test
	public void test_03() throws Exception {
		log.info("TEST_03");
		
		SimulatorInst simInst=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(simInst);

		
		log.info("creating model template container MTC_GOVER1, with an eurostag model and persist it");
		ModelTemplateContainer mtc1 = new ModelTemplateContainer("MTC_GOVER1",
				"MTC_GOVER1 comment");
		ModelTemplate mt1 = new ModelTemplate(simInst,"mtc1comment");

		DefaultParameters dp = new DefaultParameters(1);
		dp.addParameter(new ParameterFloat("INTMAX", 1.1000f));
		dp.addParameter(new ParameterFloat("kdeltaf", 25f));
		dp.addParameter(new ParameterFloat("prophp", .300000f));
		dp.addParameter(new ParameterFloat("TRH", 10f));

		mt1.getDefaultParameters().add(dp);
		mtc1.getModelTemplates().add(mt1);
		mtc1=dbmanager.save(mtc1);

		ParametersContainer pc1 = new ParametersContainer("PC_GOVER1");
		Parameters pars = new Parameters(simInst);
		pars.addParameter(new ParameterString("macroblock", "GOVER1"));
		pars.addParameter(new ParameterString("machine.name", "GEN"));
		pars.addParameter(new ParameterInteger("paramset.index", 1));
		pc1.getParameters().add(pars);

		pc1=dbmanager.save(pc1);

		Internal it1 = new Internal("GOVER1");
		it1.setModelContainer(mtc1);
		it1.setParametersContainer(pc1);

		it1=dbmanager.save(it1);
		assertNotNull(it1.getId());

		log.info("Internal (" + it1.getNativeId() + ") was persisted with id "
				+ it1.getId());
		dumpCurrentDB();

	}

	// test creating multiple MTC with the same ddbid
	@Test
	public void test_04() throws Exception {
		log.info("TEST_04");
		log.info("creating multiple MTC with the same ddbid");
		
		SimulatorInst eurostagSimulator = dbmanager.findSimulator(
				Simulator.EUROSTAG, "5.1.1");
		assertNotNull(eurostagSimulator);

		ModelTemplateContainer mtc1 = new ModelTemplateContainer("MTC_10000",
				"MTC_10000 comment");
		mtc1=dbmanager.save(mtc1);

		try {

			ModelTemplateContainer mtc2 = new ModelTemplateContainer(
					"MTC_10000", "MTC_10000 comment");
			mtc2=dbmanager.save(mtc2);
			assertTrue(false);
		} catch (Exception e) {
			log.info("ok, exception in creating two MTC with the same ddbId.");
		}

		dumpCurrentDB();

	}

	// test creating multiple MTC with the same ddbid
	@Test
	public void test_05() throws Exception {
		log.info("TEST_05");
		log.info("creating multiple Parameter containers with the same ddbid");
		
		SimulatorInst eurostagSimulator = dbmanager.findSimulator(
				Simulator.EUROSTAG, "5.1.1");
		assertNotNull(eurostagSimulator);

		ParametersContainer pc1 = new ParametersContainer("PC_10000");
		pc1=dbmanager.save(pc1);

		try {

			ParametersContainer pc2 = new ParametersContainer("PC_10000");
			pc2=dbmanager.save(pc2);
			assertTrue(false);
		} catch (Exception e) {
			log.info("ok, exception in creating two parameters containers with the same ddbId.");
		}

		dumpCurrentDB();

	}


	
	// test creating multiple MTC
	@Test
	public void test_06() throws Exception {
		int NUMBEROFMTCS=10;
		
		log.info("TEST_06");
		log.info("creating multiple MTC: " + NUMBEROFMTCS);
		SimulatorInst eurostagSim=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(eurostagSim);
		
		for (int i=0;i<NUMBEROFMTCS; i++) {
			ModelTemplateContainer mtc1 = new ModelTemplateContainer("MTC_100"+i);
			ModelTemplate mt1 = new ModelTemplate(eurostagSim,"");
			DefaultParameters dp = new DefaultParameters(1);
			dp.addParameter(new ParameterFloat("INTMAX", 1.1000f));
//			dp.addParameter(new ParameterFloat("kdeltaf", 25f));
//			dp.addParameter(new ParameterFloat("prophp", .300000f));
//			dp.addParameter(new ParameterFloat("TRH", 10f));

			mt1.getDefaultParameters().add(dp);
			mtc1.getModelTemplates().add(mt1);

			
			mtc1=dbmanager.save(mtc1);
		}
		
		for (int i=0;i<NUMBEROFMTCS; i++) {
			ModelTemplateContainer mtcfound = dbmanager.findModelTemplateContainer("MTC_100"+i);
			assertNotNull(mtcfound);
		}

		for (int i=0;i<NUMBEROFMTCS; i++) {
			ModelTemplateContainer mtcfound = dbmanager.findModelTemplateContainer("MTC_100"+i);
			dbmanager.delete(mtcfound);
		}

		for (int i=0;i<NUMBEROFMTCS; i++) {
			ModelTemplateContainer mtcfound = dbmanager.findModelTemplateContainer("MTC_100"+i);
			assertTrue(mtcfound==null);
		}
		
		log.info("OK! multiple MTCs created, then deleted");
		
		//dumpCurrentDB();
	}

	
	// test retrieving parameters
	@Test
	public void test_07() throws Exception {
		log.info("TEST_07");
		
		SimulatorInst eurostagSim=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(eurostagSim);
		
		String equipmentID="_NGEN_TN";
		
		log.info("retrieving parameters for equipment ");
		Equipment eq1=dbmanager.findEquipment(equipmentID);
		assertTrue(eq1!=null);
		
		ModelTemplate modelTemplate=dbmanager.findModelTemplate(eq1, eurostagSim);
		assertNotNull(modelTemplate);
		log.info("Modeltemplate found: " + modelTemplate.getId()+ ", " + modelTemplate.getSimulator());
		//log.info("              data: " + modelTemplate.getData());
		
		
		//retrieving all parameters
		Parameters parameters=dbmanager.findParameters(eq1, eurostagSim);
		assertTrue(parameters!=null);
		List<Parameter> parList=parameters.getParameters();
		for (Parameter parameter : parList) {
			log.info("              ------    : " + parameter.getName() +", " +parameter.getValue());
		}

		//retrieving parameters by name and type
		String parName="turbine.power";
		Float parValueFloat=dbmanager.getFloatParameter(eq1, eurostagSim, parName);
		assertNotNull(parValueFloat);
		log.info("  Parameter "+parName+": " + parValueFloat);
		
		parName="model.type";
		String parValueString=dbmanager.getStringParameter(eq1, eurostagSim, parName);
		assertNotNull(parValueString);
		log.info("  Parameter "+parName+": " + parValueString);
		
		parName="transformer.included";
		Boolean parValueBoolean=dbmanager.getBooleanParameter(eq1, eurostagSim, parName);
		assertNotNull(parValueBoolean);
		log.info("  Parameter "+parName+": " + parValueBoolean);

		log.info("end - retrieving parameters");
		//dumpCurrentDB();
	}

	
	// test retrieving parameters
	@Test
	public void test_08() throws Exception {
		log.info("TEST_08");
		SimulatorInst eurostagSim=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(eurostagSim);
		
		String nativeID="AVR_1";
		
		log.info("retrieving parameters for internal: " + nativeID);
		Internal eq1=dbmanager.findInternal(nativeID);
		assertTrue(eq1 != null);
		
		ModelTemplate modelTemplate=dbmanager.findModelTemplate(eq1,eurostagSim);
		assertNotNull(modelTemplate);
		log.info("Modeltemplate found: " + modelTemplate.getId()+ ", " + modelTemplate.getSimulator());
		//log.info("              data: " + modelTemplate.getData());
		
		
		//retrieving all parameters
		Parameters parameters=dbmanager.findParameters(eq1,eurostagSim);
		assertTrue(parameters!=null);
		List<Parameter> parList=parameters.getParameters();
		for (Parameter parameter : parList) {
			log.info("              ------    : " + parameter.getName() +", " +parameter.getValue());
		}

		//retrieving parameters by name and type
		String parName="paramset.index";
		Integer parValueInt=dbmanager.getIntegerParameter(eq1,eurostagSim, parName);
		assertNotNull(parValueInt);
		log.info("  Parameter "+parName+": " + parValueInt);
		

		
		log.info("end - retrieving parameters");
		//dumpCurrentDB();
	}
	

	
	// test retrieving parameters
	@Test
	public void test_09() throws Exception {
		log.info("TEST_09");
		SimulatorInst eurostagSim=dbmanager.findSimulator(Simulator.EUROSTAG, "5.1.1");
		assertNotNull(eurostagSim);
		ModelTemplateContainer mtc1 = new ModelTemplateContainer("MTC_0002",
				"MTC_0002 comment");
		ModelTemplate mt1 = new ModelTemplate(eurostagSim,"");

		DefaultParameters dp = new DefaultParameters(1);
		dp.addParameter(new ParameterFloat("INTMAX", 1.1000f));
		dp.addParameter(new ParameterFloat("INTMAX2", 2.2000f));
		mt1.getDefaultParameters().add(dp);
		mtc1.getModelTemplates().add(mt1);
		mtc1=dbmanager.save(mtc1);

		ParametersContainer pc1 = new ParametersContainer("PC_0002");
		Parameters pars = new Parameters(eurostagSim);
		pars.setDefParamSetNum(1);
		pars.addParameter(new ParameterFloat("INTMAX2", 3.3000f));
		pc1.getParameters().add(pars);
		pc1=dbmanager.save(pc1);

		Internal it1 = new Internal("IT_0002");
		it1.setModelContainer(mtc1);
		it1.setParametersContainer(pc1);
		it1=dbmanager.save(it1);
		assertNotNull(it1.getId());

		log.info("Internal (" + it1.getNativeId() + ") was persisted with id "
				+ it1.getId());

		//default param 1.1000, no corresponding param in the parameters set
		Float flValue=dbmanager.getFloatParameter(it1, eurostagSim ,"INTMAX");
		assertTrue(flValue.compareTo(1.1000f) ==0);
		
		//default param 2.2000 should be overwritten by actual param (which value is 3.3000) in the parameters set
		Float flValue2=dbmanager.getFloatParameter(it1, eurostagSim, "INTMAX2");
		assertTrue(flValue2.compareTo(3.3000f) ==0);

		//no default param, nor actual param in the param set
		try{
			Float flValue3=dbmanager.getFloatParameter(it1, eurostagSim , "INTMAX3");
			//must not get here
			assertTrue(false);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
	}

	
	
	// test 
	@Test
	public void test_10() throws Exception {
		log.info("TEST_10");
		SimulatorInst eurostagSim451=dbmanager.findSimulator(Simulator.EUROSTAG, "4.5.1");
		assertNotNull(eurostagSim451);
		Internal it1 = new Internal("IT_0003");
		it1=dbmanager.save(it1);
		assertNotNull(it1.getId());
		log.info("Internal (" + it1.getNativeId() + ") was persisted with id "
				+ it1.getId());
		
	}

	
	/*
	 * @Test public void test_ZZZ() throws Exception { Thread.sleep(1000*60*3);
	 * }
	 */
	
}
