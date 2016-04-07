/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package itesla.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComboBox.KeySelectionManager;


/*
 * Clase que parsea el *.par. Tiene los siguiente atributos:
 * modelName: nombre del modelo (del regulador)
 * nParameters: numero de parametros definidos en el .par
 * parNames: nombre de los parametros definidos en el .par
 * parTypes: hashmap que identifica cada parametro con su tipo. Hay dos tipos: 1) parameter Real 2) parameter Real[:] 
 * setIds: lista con los ids de parametros definidos (OJO, es posible que no esten ordenados o que se salten ids)
 * idset: hashmap que indica para cada id la correspondiente columna de parametros
 * linesxBlock: parametro interno necesario para el parseo correcto de los parametros. 
 * @author Marc Sabate <sabatem@aia.es>
 */
public class ParParser {
	
	private String modelName;
	private Integer nParameters;
	private Integer nSets;
	private List<String> parNames;
	private HashMap<String, String> parTypes;
	private List<Integer> setIds;
	private HashMap<Integer, Integer> idSet;
	private Integer linesxBlock;
	private File parFile;
	
	
	public ParParser(File parFile) {
		modelName = parFile.getName().split("\\.")[0].toUpperCase();
		this.parFile = parFile;
		ParConfiguration();
	}
	
	
	
	private void ParConfiguration() {
		//to be executed only once
		nParameters = 0;
		nSets = 0;
		parNames = new ArrayList<String>();
		idSet = new HashMap<Integer, Integer>();
		parTypes = new HashMap<String, String>();
		linesxBlock = 1;
		setIds = new ArrayList<Integer>();
		
		Boolean firstBlock = true;
		BufferedReader buffer;
		Integer lengthArrayParameter;
		try {
			List<Integer> listArrayLength = new ArrayList<Integer>();
			buffer = new BufferedReader(new FileReader(parFile));
			String sep = "\\s+";
			String line;
			String[] parLine;
			Boolean condition = false;
			while (!condition) {
				line=buffer.readLine();
                                if (line.trim().isEmpty()) {
                                    continue;
                                }
				parLine = line.trim().split(sep);
				if (parLine[0].equals(modelName)) {
					condition = true;
					nSets = parLine.length-1; 
					for (int i=1; i<parLine.length; ++i){
						idSet.put(Integer.parseInt(parLine[i]), i);
						setIds.add(Integer.parseInt(parLine[i]));
					}
				}
			}
		
			while((line=buffer.readLine())!=null){
				if (line.isEmpty()) {
					firstBlock = false;
				}
				if (firstBlock) {
					parLine = line.trim().split(sep);
					nParameters++;
					linesxBlock++;
					if (parLine[0].substring(0, 1).equals("%")){
						parNames.add(parLine[0].substring(1));
						parTypes.put(parLine[0].substring(1), "parameter Real[:]");
						//lengthArrayParameter = Integer.parseInt(parLine[1]);
						listArrayLength.clear();
						for (int i=1; i<(parLine.length-1); i++) {
							listArrayLength.add(Integer.parseInt(parLine[i]));
						}
						Collections.sort(listArrayLength);
						lengthArrayParameter = listArrayLength.get(listArrayLength.size()-1);
						
						for (int i=0; i<lengthArrayParameter*2; i++) {
							line = buffer.readLine();
							linesxBlock++;
						}
					} else {
						parNames.add(parLine[0]);
						parTypes.put(parLine[0], "parameter Real");
					}
				} else if (line.trim().split(sep)[0].equals(modelName)) {
					parLine = line.trim().split(sep);
					for (int i=1; i<parLine.length; ++i) {
						idSet.put(Integer.parseInt(parLine[i]), nSets + i);
						setIds.add(Integer.parseInt(parLine[i]));
					}
					nSets = nSets + parLine.length - 1;
				}
			}
			buffer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getModelName() {
		return(modelName);
	}
	
	public Integer getnParameters() {
		return(nParameters);
	}
	
	public Integer getnSets() {
		return(nSets);
	}
	
	public List<String> getParNames() {
		return(parNames);
	}
	
	public List<Integer> getSetIds() {
		return(setIds);
	}
	

	public HashMap<String, String> getParTypes() {
		return(parTypes);
	}
	
	
	
	public HashMap<String, String> getSetParameters(Integer instanceSet) {
		HashMap<String, String> output = new HashMap<String, String>();
		
		//output = modelName + " " + instanceName + "(";
		Integer n = idSet.get(instanceSet);
		Integer nQuotient = (n-1)/5;
		Integer nRemainder = ((n-1) % 5)+1;
		Integer lengthArrayParameter;
		String key, value;
		
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(parFile));
			String sep = "\\s+";
			String line;
			String[] parLine;
			String[] arrayLength;
			Integer maxDim;
			Boolean condition = false;
			List<Integer> listArrayLength = new ArrayList<Integer>();
			while (!condition) {
				line=buffer.readLine();
				parLine = line.trim().split(sep);
				if (parLine[0].equals(modelName)) {
					condition = true;
				}				
			}
			
			for (int i=0; i<nQuotient*(linesxBlock+2); i++) {
				line = buffer.readLine();
			}
			for (int i=0; i<nParameters; i++) {
				line = buffer.readLine();
				parLine = line.trim().split(sep);
				if (parLine[0].substring(0, 1).equals("%")){
					arrayLength=parLine;
					lengthArrayParameter = Integer.parseInt(parLine[nRemainder]);
					
					listArrayLength.clear();
					for (int j=1; j<(parLine.length-1); j++) {
						listArrayLength.add(Integer.parseInt(parLine[j]));
					}
					Collections.sort(listArrayLength);
					maxDim = listArrayLength.get(listArrayLength.size()-1);
					
					//output = output + parLine[0].substring(1) + "={";
					key = parLine[0].substring(1);
					value = "{";
					for (int j=0; j<lengthArrayParameter*2; j++) {
						line = buffer.readLine();
						parLine = line.trim().split(sep);
						//output = output + parLine[0];
						//value = value + parLine[0];
						value = value + parLine[getColumn(arrayLength, j+1, nRemainder)];
						if (j==lengthArrayParameter*2-1) {
							value = value+"}";
						} else {
							value = value + ",";
						}
					}
					
					if (lengthArrayParameter<maxDim) {
						for (int j=0; j<(maxDim*2-lengthArrayParameter*2); j++) {
							line = buffer.readLine();
						}
					}
					
					
					output.put(key, value);
				} else {
					key = parLine[0];
					value = parLine[nRemainder];
					//output = output + parLine[0] + "=" + parLine[nRemainder];
					output.put(key, value);
				}
			}
			
			buffer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output;
	}
	
	
	
	public HashMap<Integer, HashMap<String, String>> getParameters() {
		HashMap<Integer, HashMap<String, String>> output = new HashMap<Integer, HashMap<String,String>>();
		HashMap<String,String> setParameters; 
		for (Integer id : setIds) {
			setParameters = getSetParameters(id);
			output.put(id, setParameters);
		} 
		return output;
	}
	
	
	public String getMacroblockInstance(Integer instanceSet, String instanceName) {
		String output;
		output = modelName + " " + instanceName + "(";
		Integer n = idSet.get(instanceSet);
		Integer nQuotient = (n-1)/5;
		Integer nRemainder = ((n-1) % 5)+1;
		Integer lengthArrayParameter;
		
		
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(parFile));
			String sep = "\\s+";
			String line;
			String[] parLine;
			String[] arrayLength;
			Boolean condition = false;
			List<Integer> listArrayLength = new ArrayList<Integer>();
			Integer maxDim;
			
			while (!condition) {
				line=buffer.readLine();
				parLine = line.trim().split(sep);
				if (parLine[0].equals(modelName)) {
					condition = true;
				}				
			}
			
			for (int i=0; i<nQuotient*(linesxBlock+2); i++) {
				line = buffer.readLine();
			}
			for (int i=0; i<nParameters; i++) {
				line = buffer.readLine();
				parLine = line.trim().split(sep);
				if (parLine[0].substring(0, 1).equals("%")){
					
					listArrayLength.clear();
					for (int j=1; j<(parLine.length-1); j++) {
						listArrayLength.add(Integer.parseInt(parLine[j]));
					}
					Collections.sort(listArrayLength);
					maxDim = listArrayLength.get(listArrayLength.size()-1);
					
					arrayLength = parLine;
					lengthArrayParameter = Integer.parseInt(parLine[nRemainder]);
					output = output + parLine[0].substring(1) + "={";
					
					
					for (int j=0; j<lengthArrayParameter*2; j++) {
						line = buffer.readLine();
						parLine = line.trim().split(sep);
						output = output + parLine[getColumn(arrayLength, j, nRemainder)];
						if (j==lengthArrayParameter*2-1) {
							output = output+"}";
						} else {
							output = output + ",";
						}
					}
					
					
					if (lengthArrayParameter<maxDim) {
						for (int j=0; j<(maxDim-lengthArrayParameter); j++) {
							line = buffer.readLine();
						}
					}
					
					
				} else {
					output = output + parLine[0] + "=" + parLine[nRemainder];
				}
				if (i<nParameters-1) {
					output = output+",";
				} else {
					output = output + ");";
				}
			}
			
			buffer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output;
		
	}
	
	
	/*
	 * To handle the cases where different id sets have different parameter
	 * array lengths
	 */
	private Integer getColumn(String[] arrayLength, Integer nrow, Integer ncol) {
		Integer output=0;
		
		for (int i=1; i<=ncol; i++) {
			if (nrow<=2*Integer.parseInt(arrayLength[i])) {
				output=output+1;
			}
		}
		output = output-1;
		return output;
	}

}
