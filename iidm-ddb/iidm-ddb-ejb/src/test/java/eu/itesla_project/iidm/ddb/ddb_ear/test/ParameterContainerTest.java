/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.ddb_ear.test;

import java.util.logging.Logger;

import org.junit.Test;

import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.ParameterFloat;
import eu.itesla_project.iidm.ddb.model.ParameterString;
import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.ParametersContainer;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ParameterContainerTest {

	Logger log=Logger.getLogger(ParameterContainerTest.class.getName());
	

	@Test
	public void test_00() throws Exception {
		log.info("TEST_00");

		ParametersContainer pc1=new ParametersContainer("PC_GEN");
			Parameters pars = new Parameters();
			pars.getParameters().add(new ParameterString("machine.name", "GEN"));
			pars.getParameters().add(new ParameterString("machine.name", "GENXXX"));
			pars.getParameters().add(new ParameterFloat("rated.apparent.power", 1150));
			pars.getParameters().add(new ParameterFloat("rated.generator.active.power", 1100));
			pc1.getParameters().add(pars);
		
			for (Parameter p : pars.getParameters()) {
				log.info(p.getName()+": "+ p.getValue());
			}
	}

	@Test
	public void test_01() throws Exception {
		log.info("TEST_01");

		ParametersContainer pc1=new ParametersContainer("PC_GEN");
			Parameters pars = new Parameters();
			try {
			pars.addParameter(new ParameterString("machine.name", "GEN"));
			pars.addParameter(new ParameterString("machine.name", "GEN"));
			pars.addParameter(new ParameterString("machine.name", "GENXXX"));
			assert(true==false);
			} catch (Throwable t) {
			  log.info(t.getMessage()); 
			}
			
			for (Parameter p : pars.getParameters()) {
				log.info(p.getName()+": "+ p.getValue());
			}
			
	}

	/*
	 * @Test public void test_ZZZ() throws Exception { Thread.sleep(1000*60*3);
	 * }
	 */
	
}
