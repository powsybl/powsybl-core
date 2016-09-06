/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class Utils {
	
	
	public static String cleanModel(String modelicaModel) {
		String cleanedModel = new String();
	
		Scanner scanner = new Scanner(modelicaModel);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.trim().startsWith("within")) continue;
			else if(line.trim().startsWith("package PowerSystems")) continue;
			else if(line.trim().startsWith("end PowerSystems")) continue;
			else cleanedModel = cleanedModel + System.getProperty("line.separator") + line;
		}
		scanner.close();
		return cleanedModel;
	}
	
	public static void readParametersConverterFile() throws Exception {
		
		
		
	}
	
	/**
	 * 
	 * @param buffer
	 * @return Map: key : integer
	 * 					0: parameters list
	 * 					1: pins list
	 * 				value List<String> list with parameters names or pins names
	 * @throws IOException
	 */
	public static Map<Integer, List<String>> parseModelParametersAndPins(BufferedReader buffer) throws IOException {
		Map<Integer, List<String>> paramsAndPins = new HashMap<Integer, List<String>>();
		List<String> paramsList = new ArrayList<String>();
		List<String> pinsList = new ArrayList<String>();
		String[] lineData;
		
		String line =buffer.readLine();
		
		while(!(line=buffer.readLine()).equals(StaticData.EQUATION)) {
			if(line.trim().startsWith(EurostagFixedData.PARAMETER)) {
				lineData = line.trim().split(StaticData.WHITE_SPACE);
				if(lineData.length >= 3) {
					String paramName = lineData[2].endsWith(StaticData.SEMICOLON) == true ? lineData[2].substring(0, lineData[2].length()-1) : lineData[2];
					 
					paramsList.add(paramName);
				}
			}
			else if(line.trim().startsWith(EurostagModDefaultTypes.PIN_TYPE)) {
				lineData = line.trim().split(StaticData.WHITE_SPACE);
				if(lineData.length >= 2) {
					String pinName = lineData[1].endsWith(StaticData.SEMICOLON) == true ? lineData[1].substring(0, lineData[1].length()-1) : lineData[1];
					pinsList.add(pinName);
				}
			}
			
			line = buffer.readLine();
		}
		
		paramsAndPins.put(0, paramsList);
		paramsAndPins.put(1, pinsList);
		
		return paramsAndPins;
	}
	
	public static List<String> parseModelPins(BufferedReader buffer) throws IOException {
		List<String> pinsList = new ArrayList<String>();
		String[] lineData;
		
		String line =buffer.readLine();
		while(line != null) {
			//if(line.trim().startsWith(EurostagModDefaultTypes.PIN_TYPE) && !line.contains("isInitValue")) {
			if((line.trim().startsWith(EurostagModDefaultTypes.PIN_TYPE) || line.trim().startsWith(EurostagModDefaultTypes.INPUT_PIN_TYPE) || line.trim().startsWith(EurostagModDefaultTypes.OUTPUT_PIN_TYPE)) && !line.contains("isInitValue")) { //Parseo de pines //21-8-2014 modificación && line.contains("isInitValue")
				lineData = line.trim().split(StaticData.WHITE_SPACE);
				if(lineData.length >= 2) {
					String pinName = lineData[1].endsWith(StaticData.SEMICOLON) == true ? lineData[1].substring(0, lineData[1].length()-1) : lineData[1];
					pinsList.add(pinName);
				}
			}
			
			line = buffer.readLine();
		}
		return pinsList;
	}
	
	public static List<String> getOtherRegVars(BufferedReader buffer) throws IOException {
		List<String> pinsList = new ArrayList<String>();
		String[] lineData;
		
		String line =buffer.readLine();
		while(line != null) {
			if(line.contains("isInitValue")) {
				lineData = line.trim().split(StaticData.WHITE_SPACE);
				if(lineData.length >= 2) {
					String pinName = lineData[1].endsWith(StaticData.SEMICOLON) == true ? lineData[1].substring(0, lineData[1].length()-1) : lineData[1];
					pinsList.add(pinName);
				}
			}
			
			line = buffer.readLine();
		}
		return pinsList;
	}
	
	
	public static List<String> parseRegInitVariables(BufferedReader buffer) throws IOException {
		List<String> initList = new ArrayList<String>();
		String[] lineData;
		
		String line =buffer.readLine();
		while(line != null) {
			if(line.trim().startsWith(EurostagFixedData.PARAMETER) && !line.contains("=")) {
				lineData = line.trim().split(StaticData.WHITE_SPACE);
				if(lineData.length >= 3) {
					String varName = lineData[2].endsWith(StaticData.SEMICOLON) == true ? lineData[2].substring(0, lineData[2].length()-1) : lineData[2];
					if(varName.startsWith(StaticData.INIT_VAR)) {
						initList.add(varName);
					}
				}
			}
			line = buffer.readLine();
		}
		return initList;
	}
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
}
