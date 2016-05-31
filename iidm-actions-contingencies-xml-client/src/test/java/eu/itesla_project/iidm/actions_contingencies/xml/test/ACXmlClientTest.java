/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.actions_contingencies.xml.test;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import eu.itesla_project.iidm.actions_contingencies.xml.XmlFileContingenciesAndActionsDatabaseClient;
import eu.itesla_project.iidm.actions_contingencies.xml.XmlFileContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;

/**
*
* @author Quinary <itesla@quinary.com>
*/
public class ACXmlClientTest {

	@Test
	public void test() {
		Path p1 = Paths.get("src\\test\\resources\\test-ac.xml");
		
		try {
			ContingenciesAndActionsDatabaseClient client = new XmlFileContingenciesAndActionsDatabaseClient(p1);
			client.getActionPlans();
			client.getZones();
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error "+e.getMessage());
		}
		
	}

}
