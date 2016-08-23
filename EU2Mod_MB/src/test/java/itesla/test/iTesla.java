/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package itesla.test;

import itesla.converter.Converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class iTesla {
	private static Converter converter;
	private static Converter converter_init;

	@Test
	public void test() throws IOException {
		String pathFRM = getClass().getResource("/edftur1c.frm").toString();
		String pathOUT = getClass().getResource("/output").toString();
		converter = new Converter(pathFRM, pathOUT, false);
		converter_init = new Converter(pathFRM, pathOUT, true);
		try {
			converter.convert2MO();
			converter_init.convert2MO();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * Function that returns a specific set of parameters given by its reference id
		 */
		Integer idSet = 1;
		HashMap<String, String> nthParameterSet = converter.parData.getSetParameters(idSet);
		
		for (Map.Entry<String, String> entry : nthParameterSet.entrySet()) {
			System.out.println(entry.getKey() + "; " + entry.getValue());
		}
		
		HashMap<String, String> interfaceVariables = converter.getInterfaceVariables();
		for (Map.Entry<String, String> entry : interfaceVariables.entrySet()) {
			System.out.println(entry.getKey() + "; " + entry.getValue());
		}
		
		List<String> init_friParameters = converter.getInit_friParameters();
		for (String initParameter : init_friParameters) {
			System.out.println(initParameter);
		}
		
		System.out.println("-------------------");
		List<String> init_InterfaceParameters = converter.getInit_InterfaceParameters();
		for (String initInterfaceParameter : init_InterfaceParameters) {
			System.out.println(initInterfaceParameter);
		}
		System.out.println("THE END");		
	}
}
