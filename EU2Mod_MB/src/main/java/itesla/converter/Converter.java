/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package itesla.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class Converter {
	public String pathfrm;
	public String pathOut;
	public InputStream CorrespondenceTable;
	public String pathPar;
	public String pathfri;
	public ParParser parData;
	private Boolean init;
	private ModelicaModel MO;
	private Boolean isEmpty;
	
	
	
	public Converter(String pathfrm, String pathOut, Boolean init) {
		this.pathfrm = pathfrm;
		this.pathOut = pathOut;
		//this.pathOut = pathOut;
		this.pathPar = pathfrm.replace(".frm", ".par");
		this.pathfri = pathfrm.replace(".frm", ".fri");
		this.CorrespondenceTable = getClass().getResourceAsStream("/CorrespondenceTable.csv");
		this.init = init;
		isEmpty = false;
		File parFile = new File(pathPar);
		parData = new ParParser(parFile);
		try {
			this.MO = EUparser();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private ModelicaModel EUparser() throws IOException{	
			

		String sep = ";";
		String line;
		Hashtable<Integer,Element> CT = new Hashtable<Integer,Element>(); //correspondance table
		
		String[] CTline;
		Integer CTidEu;
		String nameEu;
		String nameModelica;
		Integer nInputPins;
		List<String> param; 
		InputStreamReader reader = new InputStreamReader(CorrespondenceTable);
		BufferedReader bufferCT = new BufferedReader(reader);
		line = bufferCT.readLine(); //reads headings
		while((line=bufferCT.readLine())!=null){
			CTline = line.split(sep);
			CTidEu = Integer.parseInt(CTline[0]);
			nameEu = CTline[1];
			
			if (CTline.length>=3) {
				nInputPins = Integer.parseInt(CTline[2]);
				nameModelica = CTline[3].trim();
			} else {
				nameModelica = "";
				nInputPins = 0;
			}
			param = new ArrayList<String>();
			for (int i = 4; i<CTline.length; ++i) {
				param.add(CTline[i]);
			}
			Element elt = new Element(CTidEu, nameEu, nameModelica, param, nInputPins); 
			CT.put(CTidEu, elt);
			
		}
		bufferCT.close();

		//.frm or .fri file lecture
		File inputFile; 
		if (init) {
			inputFile = new File(pathfri);
		} else {
			inputFile = new File(pathfrm);
		}
		EU_MBparser EUfile = new EU_MBparser(inputFile);
		Integer nBlocks = EUfile.getnBlocks();
		ModelicaModel MO;
		File parFile = new File(pathPar);
		ParParser parData = new ParParser(parFile);
		if (nBlocks==0) {
			isEmpty=true;
			MO = new ModelicaModel(pathfrm,parData);
		} else {

			String[][] paramEu = EUfile.getParamEU();
			Integer[] GraphicalNumber = EUfile.getGraphicalNumber();
			String[][] entries = EUfile.getEntries();
			String[] Blocksoutput = EUfile.getBlocksoutput();
			Integer[] idEu = EUfile.getIdEu();
			Integer[][] link = EUfile.getLink();
			Integer nLinks = EUfile.getnLinks();
	
			
			//.par file lecture
//			File parFile = new File(pathPar);
//			ParParser parData = new ParParser(parFile);
			
			//creation of the n blocks
			//counter of the blocks of the same type
			Block[] Macroblock = new Block[nBlocks];
			Hashtable<Integer,Integer> CountIdBlock = new Hashtable<Integer, Integer>();
			for (int i = 0; i<nBlocks; i++){
				String[] paramBlock = new String[8];
				String[] entriesBlock = new String[5];
				if (CountIdBlock.containsKey(idEu[i])) {
					CountIdBlock.put(idEu[i], CountIdBlock.get(idEu[i])+1);
				} else {
					CountIdBlock.put(idEu[i], 1);
				}
				for (int j=0; j<8; j++) {
					paramBlock[j] = paramEu[j][i];
				}
				for (int j=0; j<5; j++){
					entriesBlock[j] = entries[j][i];
				}
				Macroblock[i] = new Block(paramBlock, entriesBlock, Blocksoutput[i], GraphicalNumber[i], idEu[i], CountIdBlock.get(idEu[i]), CT.get(idEu[i]).nInputPins);
			}
			MO = new ModelicaModel(Macroblock, link, pathfrm, CT, parData);
		}
			
		
		return MO;
	}
	
	
	public void convert2MO() throws IOException {
//		File parFile = new File(pathPar);
//		ParParser parData = new ParParser(parFile);
			
		
		File frm = new File(pathfrm);
		String nameModel = frm.getName().split("\\.")[0];
		if (init) nameModel = nameModel+"_init";
		File outFile = new	File(pathOut, nameModel + ".mo");
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

		if (init) {
			out.write(MO.outputHeading + "_init" + "\n");
		} else {
			out.write(MO.outputHeading + "\n");
		}
		
		
		if (!isEmpty) {
			List<Integer> setIds = parData.getSetIds();
			for (int i=0; i<setIds.size(); i++) {
				out.write("//" + parData.getMacroblockInstance(setIds.get(i), parData.getModelName() + "_" + setIds.get(i).toString()) + "\n");
			}
			
			for (int i=0; i<MO.outputParamInit.size(); i++){
				out.write(MO.outputParamInit.get(i) + "\n");
			}
			for (int i=0; i<MO.outputParamDeclaration.size(); i++){
				out.write(MO.outputParamDeclaration.get(i) + "\n");
			}
			for (int i=0; i<MO.outputBlocksDeclaration.size(); i++){
				out.write(MO.outputBlocksDeclaration.get(i) + "\n");
			}
			for (int i=0; i<MO.outputPositiveImPin.size(); i++){
				out.write(MO.outputPositiveImPin.get(i) + "\n");
			}
			for (int i=0; i<MO.outputNegativeImPin.size(); i++){
				out.write(MO.outputNegativeImPin.get(i) + "\n");
			}
	
			out.write("equation\n");
			//if (nLinks>0) {
			if (MO.outputConnection.size()>0) {
				for (int i=0; i<MO.outputConnection.size(); i++){
					out.write(MO.outputConnection.get(i) + "\n");
				}
			}
			if (!MO.outputInputConnection.isEmpty()) {
				for (int i=0; i<MO.outputInputConnection.size(); i++){
					out.write(MO.outputInputConnection.get(i) + "\n");
				}
			}
			for (int i=0; i<MO.outputOutputConnection.size(); i++){
				out.write(MO.outputOutputConnection.get(i) + "\n");
			}
			if (!MO.outputZeroPins.isEmpty()){
				for (int i=0; i<MO.outputZeroPins.size(); i++){
					out.write(MO.outputZeroPins.get(i) + "\n");
					
					
				}
			}
			
		} else {
			for (int i=0; i<MO.outputParamDeclaration.size(); i++){
				out.write(MO.outputParamDeclaration.get(i) + "\n");
			}
			out.write("equation\n");

		}
		if (init) {
			out.write(MO.outputEnd + "_init" + ";");
		} else {
			out.write(MO.outputEnd + ";");
		}
		out.close();
	}
	
	
	public void printLinkNames() throws IOException {

		File frm = new File(pathfrm);
		String nameModel = frm.getName().split("\\.")[0];
		File outFile = new	File(pathOut, "connections_"+nameModel + ".txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		for (int i=0; i<MO.NamedLinks.size(); i++) {
			out.write(MO.NamedLinks.get(i) + "\n");
		}
		out.close();
	}
	
	public List<String> getConnections() {
		return MO.NamedLinks;
	}
	
	public HashMap<String, String> getInterfaceVariables() {
		return MO.interfaceVariables;
	}
	
	public List<String> getInit_friParameters() {
		return MO.init_friParameters;
	}
	
	public List<String> getInit_InterfaceParameters() {
		return MO.init_InterfaceParameters;
	}
	
	
	
}
