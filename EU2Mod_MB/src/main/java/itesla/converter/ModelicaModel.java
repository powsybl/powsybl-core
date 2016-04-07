/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package itesla.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Marc Sabate <sabatem@aia.es>
 */
public class ModelicaModel {
	private Block[] Blocks;
	private Integer[][] Link;
	private String pathEu;
	private Hashtable<Integer,Element> CT;
	//private List<String> namePar;
	public String outputHeading;
	public String outputEnd;
	public List<String> outputPositiveImPin;
	public List<String> outputNegativeImPin;
	public List<String> outputParamInit;
	public List<String> outputParamDeclaration;
	public List<String> outputBlocksDeclaration;
	public List<String> outputZeroPins;
	public List<String> outputConnection;
	public List<String> outputInputConnection;
	public List<String> outputOutputConnection;
	public List<String> NamedLinks;
	public HashMap<String, String> interfaceVariables;
	public List<String> init_friParameters;
	public List<String> init_InterfaceParameters;
	private ParParser parData;
	//private Boolean PNdeclared;
	
	
	
	/*
	 * Clase que define el modelo en Modelica:
	 * parData: lista parametros definidos dentro del archivo .par
	 * outputHeading: la cabecera de un modelo de modelica (e.g: model PwLine)
	 * outputEnd: end de un modelo de Modelica (e.g: end model;)
	 * outputPositiveImPin: lista de strings donde cada elemento es la declaración de cada input pin del modelo
	 * outputNegativeImPin: lista de strings donde cada elemento es la declaración de cada output pin del modelo
	 * outputParamInit: lista de strings donde cada elemento es la declaración de los parámetros de inicialización de los modelos que tengan parámetro de inicialización
	 * outputParamDeclaration: lista de strings donde cada elemento es la declaración de los parámetros dentro del *.par (e.g. parameter Real T1;)
	 * outputBlocksDeclaration: lista de strings donde cada elemento es la declaración de la instancia de un bloque dentro del macrobloque (e.g. PowerSystems.Math.ImSetPoint setPoint(V=1);)
	 * outputZeroPins: los pines de entrada de algunos bloques que no se usan (como por ejemplo, en un bloque suma, si solo se usan 3 pines de entrada, 2 quedan sin usar), entonces se ponen a 0 en las ecuaciones, para tener un sistema determinado.
	 * outputConnection: lista de strings con la conexión entre bloques
	 * outputInputConnection: lista de strings con la conexión de los bloques con los inputpins del macrobloque (e.g. connect(pin_CM,suma.p1))
	 * outputOutputConnection: lista de strings con la conexión de los bloques con los outputpins del macrobloque
	 * NamedLinks: lista de strings con los links que tienen nombre
	 * interfaceVariables: lista de strings con los nombres de las Interface Variables
	 * init_friParameters: lista de strings con los nombres de los paramatros calculados en el .fri
	 * init_InterfaceParameters: lista de strings con los nombres de las variables de inicialización de las interface variables
	 */
	public ModelicaModel (Block[] Blocks, Integer[][] Link, String pathEu , Hashtable<Integer,Element> CT, ParParser parData) {
		this.Blocks = Blocks;
		this.Link = Link;
		this.pathEu = pathEu;
		this.CT = CT;
		this.parData = parData;
		this.outputHeading=""; // = new ArrayList<String>();
		this.outputEnd = "";
		this.outputPositiveImPin = new ArrayList<String>();
		this.outputNegativeImPin = new ArrayList<String>();
		this.outputParamInit = new ArrayList<String>();
		this.outputParamDeclaration = new ArrayList<String>();
		this.outputBlocksDeclaration = new ArrayList<String>();
		this.outputZeroPins = new ArrayList<String>();
		this.outputConnection = new ArrayList<String>();
		this.outputInputConnection = new ArrayList<String>();
		this.outputOutputConnection = new ArrayList<String>();
		this.NamedLinks = new ArrayList<String>();
		this.interfaceVariables = new HashMap<String, String>();
		this.init_friParameters = new ArrayList<String>();
		this.init_InterfaceParameters = new ArrayList<String>();
		//PNdeclared = false;
		
		Heading();
		paramDeclaration();
		BlocksDeclaration();
		PositiveImPin();
		NegativeImPin();
		Connection();
		InputConnection();
		OutputConnection();
		ZeroPins();
		paramInit();
		End();
	}
	
	
	public ModelicaModel(String pathEu,ParParser parData) {
//		this.Blocks = Blocks;
//		this.Link = Link;
		this.Blocks = new Block[0];
		this.pathEu = pathEu;
		this.outputParamDeclaration = new ArrayList<String>();
//		this.CT = CT;
		this.parData = parData;
		this.outputHeading=""; // = new ArrayList<String>();
		this.outputEnd = "";
		//this.outputPositiveImPin = new ArrayList<String>();
		//this.outputNegativeImPin = new ArrayList<String>();
		//this.outputParamInit = new ArrayList<String>();
		//this.outputParamDeclaration = new ArrayList<String>();
		//this.outputBlocksDeclaration = new ArrayList<String>();
		//this.outputZeroPins = new ArrayList<String>();
		//this.outputConnection = new ArrayList<String>();
		//this.outputInputConnection = new ArrayList<String>();
		//this.outputOutputConnection = new ArrayList<String>();
		//this.NamedLinks = new ArrayList<String>();
		//PNdeclared = false;
		
		Heading();
		paramDeclaration();
		BlocksDeclaration();
		End();
		
	}
	


	public void Heading() {
		File EuFile = new File(pathEu);
		String name = EuFile.getName().split("\\.")[0];
		String output = "model " + name;
		outputHeading=output;
		//return output;
	}
	
	public void End() {
		File EuFile = new File(pathEu);
		String name = EuFile.getName().split("\\.")[0];
		String output = "end " + name;
		outputEnd=output;
	}
	
	public void PositiveImPin() {
		Boolean TerminalVoltage = false;
		Boolean FieldCurrent = false;
		Boolean ActivePower = false;
		String name;
		String base;
		for (int i=0; i<Blocks.length; i++) {
			if (Blocks[i].idEu == 27) {
				TerminalVoltage = true;
				//NamedLinks.add("TerminalVoltage");
				if(!NamedLinks.contains("TerminalVoltage")) {
					NamedLinks.add("TerminalVoltage");
					outputPositiveImPin.add("  PowerSystems.Connectors.ImPin pin_TerminalVoltage;"); //Terminal Voltage");
				}
			} else if (Blocks[i].idEu == 50) {
				FieldCurrent = true;
				//NamedLinks.add("FieldCurrent");
				if(!NamedLinks.contains("FieldCurrent")) {
					NamedLinks.add("FieldCurrent");
					outputPositiveImPin.add("  PowerSystems.Connectors.ImPin pin_FieldCurrent;"); //Field Current");
				}
			} else if (Blocks[i].idEu == 28) {
				ActivePower = true;
				base = Blocks[i].param[2].replaceAll("([\\W|[_]])+", "");
				//NamedLinks.add("ActivePower"+base);
				if(!NamedLinks.contains("ActivePower"+base)) {
					NamedLinks.add("ActivePower"+base);
					outputPositiveImPin.add("  PowerSystems.Connectors.ImPin pin_ActivePower"+base+";");// //Active Power");
				}
			} else if (Blocks[i].idEu==31) {
				base = Blocks[i].param[2].replaceAll("([\\W|[_]])+", "");
				//NamedLinks.add("ReactivePower"+base);
				if(!NamedLinks.contains("ReactivePower"+base)) {
					NamedLinks.add("ReactivePower"+base);
					outputPositiveImPin.add("  PowerSystems.Connectors.ImPin pin_ReactivePower"+base+";");// //Active Power");
				}
			}  else if (Blocks[i].idEu==49) {
				base = Blocks[i].param[2].replaceAll("([\\W|[_]])+", "");
				//NamedLinks.add("ReactivePower"+base);
				if(!NamedLinks.contains("FRZ"+base)) {
					NamedLinks.add("FRZ"+base);
					outputPositiveImPin.add("  PowerSystems.Connectors.ImPin pin_FRZ"+base+";");// //frequency");
				}
			} else if (Blocks[i].idEu==60) {
				//NamedLinks.add("Current"+base);
				if(!NamedLinks.contains("Current")) {
					NamedLinks.add("Current");
					outputPositiveImPin.add("  PowerSystems.Connectors.ImPin pin_Current;");// //Current");
				}
			
			} else {
				for (int j=0; j<Blocks[i].entries.length; j++) {
					name = Blocks[i].entries[j].replaceAll("([\\W|[_]])+", "");
					if (!Blocks[i].entries[j].equals("?") && !NamedLinks.contains(name)) {
						NamedLinks.add(name);
						outputPositiveImPin.add("  PowerSystems.Connectors.ImPin pin_" + name + ";");// + " //" + name);
					}
				}
			}
		}

	}
	
	public void NegativeImPin() {
		String namePin;
		String nameInitPin;
		Boolean isInitValue;
		for (int i=0; i<Blocks.length; i++){
			nameInitPin = "";
			if (Blocks[i].output.length()>1){
				if (Blocks[i].output.contains("^")) {
					isInitValue = true;
				} else {
					isInitValue = false;
				}
				namePin = Blocks[i].output.replaceAll("([\\W|[_]])+", "");
				if (!Blocks[i].param[7].equals("?")) {
					if (Blocks[i].param[7].contains("_") || Blocks[i].param[7].contains("^")) {
						nameInitPin = Blocks[i].param[7].replaceAll("([\\W|[_]])+", "");
					}
				} 
				if (!NamedLinks.contains(namePin)) {
					NamedLinks.add(namePin);
					
					//outputNegativeImPin.add("  PowerSystems.Connectors.ImPin pin_" + namePin +"; //" + namePin);
					if (!nameInitPin.equals("")) {
						interfaceVariables.put(namePin, nameInitPin);
						outputNegativeImPin.add("  PowerSystems.Connectors.ImPin pin_" + namePin +"; //" + nameInitPin);
					} else if (isInitValue) {
						outputNegativeImPin.add("  PowerSystems.Connectors.ImPin pin_" + namePin +"; //" + "isInitValue");
					} else {
						outputNegativeImPin.add("  PowerSystems.Connectors.ImPin pin_" + namePin +";");
					}
				} else if (!nameInitPin.equals("")){
					interfaceVariables.put(namePin, nameInitPin);
					outputNegativeImPin.remove("  PowerSystems.Connectors.ImPin pin_" + namePin +";");
					outputPositiveImPin.remove("  PowerSystems.Connectors.ImPin pin_" + namePin +";");
					outputNegativeImPin.add("  PowerSystems.Connectors.ImPin pin_" + namePin +"; //" + nameInitPin);
					
				}
			}
		}
	}
		
	public void paramInit(){
		String aux;
		Boolean found;
		for (int i=0; i<Blocks.length; i++){
			if (!Blocks[i].param[7].equals("?")) {
				if (Blocks[i].param[7].substring(0, 1).equals("^") || Blocks[i].param[7].substring(0, 1).equals("_")){
					aux = "init_"+Blocks[i].param[7].substring(1);
					found = false;
					if (Blocks[i].param[7].substring(0, 1).equals("^")) {
						if (!init_friParameters.contains(Blocks[i].param[7].substring(1))) {
							init_friParameters.add(Blocks[i].param[7].substring(1));
						}
					} else {
						if (!init_InterfaceParameters.contains(Blocks[i].param[7].substring(1))) {
							init_InterfaceParameters.add(Blocks[i].param[7].substring(1));
						}
					}
					for (int j=0; j<outputParamDeclaration.size(); j++) {
						if (outputParamDeclaration.get(j).contains("parameter Real " + aux + ";") || outputParamDeclaration.get(j).contains("parameter Real " + aux + "=")){
							found = true;
							//outputParamDeclaration.set(j, "  parameter Real " + aux + "=init_" + Blocks[i].GraphicalNumber.toString() + ";");
						}
					}
					if (!found) {
						//outputParamDeclaration.add("  parameter Real " + aux + "=init_" + Blocks[i].GraphicalNumber.toString() + ";");
						outputParamDeclaration.add("  parameter Real " + aux + ";");
					}
					outputParamInit.add("  parameter Real init_" + Blocks[i].GraphicalNumber.toString() + "=" + aux + ";");
				} else {
					aux = Blocks[i].param[7];
					outputParamInit.add("  parameter Real init_" + Blocks[i].GraphicalNumber.toString() + "=" + aux +";");
				}
				//outputParamInit.add("  parameter Real init_" + Blocks[i].GraphicalNumber.toString() + "=" + aux +";");
				//outputParamInit.add("  parameter Real init_" + Blocks[i].GraphicalNumber.toString() + ";");
			}
		}
	}

	
	public void paramDeclaration(){
		String line;
		List<String> parNames = parData.getParNames();
		HashMap<String, String> parTypes = parData.getParTypes();
		for (int i=0; i<parNames.size(); i++) {
			if (!parNames.get(i).substring(0, 1).equals("\u00a7")) {
				line = "  " + parTypes.get(parNames.get(i)) + " " + parNames.get(i) + ";";
				outputParamDeclaration.add(line);
			}
		}
		
		//Parche: si el macrobloque es OELPSAT se añaden los parametros XD y XQ de la maquina
		if (outputHeading.toLowerCase().equals("model oelpsat")) {
			outputParamDeclaration.add("  parameter Real XD;");
			outputParamDeclaration.add("  parameter Real XQ;");
		}
		
	}
	
	
	public void BlocksDeclaration(){
		
		Boolean found_init;
		
		outputParamDeclaration.add("  parameter Real SNREF;");
		outputParamDeclaration.add("  parameter Real SN;");
		outputParamDeclaration.add("  parameter Real PN;");
		outputParamDeclaration.add("  parameter Real PNALT;");
		
		for (int i=0; i<Blocks.length; i++){
			if (Blocks[i].idEu!=27 && Blocks[i].idEu!=50 && Blocks[i].idEu!=28 && Blocks[i].idEu!=31 && Blocks[i].idEu!=49 && Blocks[i].idEu!=60) {
				String modelDeclaration;
				String modelParameters;
				Boolean first;
				Boolean previous;
				Element model = CT.get(Blocks[i].idEu);
				modelDeclaration = "  " + model.pathModelica + " " + model.nameModelica + "_" + Blocks[i].count.toString(); 
				if (model.param.size()>0) {
					modelParameters = " (";
				} else {modelParameters = ";";}
				first = true;
				previous = true;
				for (int j=0; j<model.param.size(); j++) {
					if (!model.param.get(j).isEmpty()) {
						if (!first && previous) {
							modelParameters = modelParameters+", ";
						} else {first = false;}
						if (j==7){
							if (!Blocks[i].param[7].equals("?")) {
								modelParameters = modelParameters + model.param.get(j) + "=" + "init_" + Blocks[i].GraphicalNumber.toString();
								previous = false;
								if (Blocks[i].idEu.equals(1) || Blocks[i].idEu.equals(35)) {
									modelParameters = modelParameters + ", StartValue=true";
								}
							} else if (Blocks[i].idEu.equals(1) || Blocks[i].idEu.equals(35)){
								modelParameters = modelParameters + "StartValue=false";
							}
							
						} else if (j<7) {
							if (!model.param.get(j).isEmpty()){
								if (!Blocks[i].param[j].equals("?") && !Blocks[i].param[j].substring(0,1).equals("%") && !Blocks[i].param[j].substring(0, 1).equals("_") && !Blocks[i].param[j].substring(0, 1).equals("^")) {
									
									if (Blocks[i].idEu==69) {
										if (Blocks[i].param[j].contains("SND")) { //cas ImTimer mode=SEND
											modelParameters = modelParameters + model.param.get(j) + "= \"SEND\"";
										} else if (Blocks[i].param[j].contains("INT")) { //cas ImTimer mode=INTEGRATOR
											modelParameters = modelParameters + model.param.get(j) + "= \"INTEGRATOR\"";
										}
									} else {
										modelParameters = modelParameters + model.param.get(j) + "=" + Blocks[i].param[j];
									}
								} else if (Blocks[i].param[j].substring(0,1).equals("%")) {
									modelParameters = modelParameters + model.param.get(j) + "=" + Blocks[i].param[j].substring(1);
								} else if (Blocks[i].param[j].substring(0, 1).equals("_") || Blocks[i].param[j].substring(0, 1).equals("^")) {
									found_init = false;
									for (int k=0; k<outputParamDeclaration.size(); k++) {
										if (outputParamDeclaration.get(k).contains("parameter Real init_" + Blocks[i].param[j].substring(1) + ";")){
										//if (outputParamDeclaration.get(k).contains("parameter Real " + Blocks[i].param[j] + ";")){
											found_init = true;
										}
									}
									if (!found_init) {
										outputParamDeclaration.add("  parameter Real init_" + Blocks[i].param[j].substring(1) + ";");
//										outputParamDeclaration.add("  parameter Real " + Blocks[i].param[j] + ";");
									}
									modelParameters = modelParameters + model.param.get(j) + "=init_" + Blocks[i].param[j].substring(1);
//									modelParameters = modelParameters + model.param.get(j) + "=" + Blocks[i].param[j];
								} else if (Blocks[i].idEu.equals(2)) {
									modelParameters = modelParameters + model.param.get(j) + "=1";
								}else {
									modelParameters = modelParameters + model.param.get(j) + "=0";
								}
								previous = true;
							} else {previous = false;}
						}
					}
				}
				if (model.param.size()>0) {
					modelParameters = modelParameters + ");";
				}
//				if (modelParameters.contains("PN") && !PNdeclared) {
//					outputParamDeclaration.add("  parameter Real PN = 1000;");
//					PNdeclared = true;
//				}
				outputBlocksDeclaration.add(modelDeclaration + modelParameters + " //Eurostag Block number: " + Blocks[i].GraphicalNumber.toString());
			}
		}
	}
	
	
	public void ZeroPins() {
		Integer ind;
		for (int i=0; i<Blocks.length; i++){
			if (Blocks[i].idEu.intValue()!=27 && Blocks[i].idEu.intValue()!=50 && Blocks[i].idEu.intValue()!=28 && Blocks[i].idEu.intValue()!=31 && Blocks[i].idEu.intValue()!=49){
				Element model = CT.get(Blocks[i].idEu);
				for (int j=0; j<model.nInputPins; j++){
					if (!Blocks[i].UsedInputPins.get(j)){
						ind = j+1;

						if (Blocks[i].idEu.intValue()!=2 && Blocks[i].idEu.intValue()!=13 && Blocks[i].idEu.intValue()!=22) {
							outputZeroPins.add("  " + model.nameModelica + "_" + Blocks[i].count.toString() + ".p" + ind.toString() + "=0;");
						} else if (Blocks[i].idEu.intValue()==22) {
							outputZeroPins.add("  " + model.nameModelica + "_" + Blocks[i].count.toString() + ".p" + ind.toString() + "=9999;");
						} else {
							outputZeroPins.add("  " + model.nameModelica + "_" + Blocks[i].count.toString() + ".p" + ind.toString() + "=1;");
						}
					}
				}
			}
		}
	}
	
	
	
	public void Connection() {
		Integer nLinks = Link.length;
		String Conn;
		String ConnLeft;
		String ConnRight;
		Element model;
		String base;
		for (int i=0; i<nLinks; i++){
			Conn = "  connect(";
			model = CT.get(Blocks[Link[i][0]-1].idEu);
			if (Blocks[Link[i][0]-1].idEu == 27) {
				ConnLeft = "pin_TerminalVoltage";
			} else if (Blocks[Link[i][0]-1].idEu == 50) {
				ConnLeft = "pin_FieldCurrent";
			} else if (Blocks[Link[i][0]-1].idEu == 28) {
				base = Blocks[Link[i][0]-1].param[2].replaceAll("([\\W|[_]])+", "");
				ConnLeft = "pin_ActivePower" + base;
			} else if (Blocks[Link[i][0]-1].idEu == 31) {
				base = Blocks[Link[i][0]-1].param[2].replaceAll("([\\W|[_]])+", "");
				ConnLeft = "pin_ReactivePower" + base;
			} else if (Blocks[Link[i][0]-1].idEu == 49) {
				base = Blocks[Link[i][0]-1].param[2].replaceAll("([\\W|[_]])+", "");
				ConnLeft = "pin_FRZ" + base;
			} else if (Blocks[Link[i][0]-1].idEu == 60) {
				base = Blocks[Link[i][0]-1].param[2].replaceAll("([\\W|[_]])+", "");
				ConnLeft = "pin_Current";
			} else {
				ConnLeft = model.nameModelica + "_" + Blocks[Link[i][0]-1].count.toString() + ".n1";
			}
			model = CT.get(Blocks[Link[i][1]-1].idEu);
			ConnRight = model.nameModelica + "_" + Blocks[Link[i][1]-1].count.toString() + ".p" + Link[i][2].toString();
			Blocks[Link[i][1]-1].UsedInputPins.set(Link[i][2]-1, true);
			Conn = Conn + ConnLeft + ", " + ConnRight + ");";
			outputConnection.add(Conn);
		}
	}
	
	
	public void InputConnection() {
		//List<String> ImPins = new ArrayList<String>();
		String Conn;
		String ConnLeft;
		String ConnRight;
		Element model;
		Integer indConnRight;
		for (int i=0; i<Blocks.length; i++) {
			for (int j=0; j<Blocks[i].entries.length; j++) {
				if (!Blocks[i].entries[j].equals("?")) {
					model = CT.get(Blocks[i].idEu);
					ConnLeft = "pin_" + Blocks[i].entries[j].replaceAll("([\\W|[_]])+", "");
					indConnRight = j+1;
					ConnRight = model.nameModelica + "_" + Blocks[i].count.toString() + ".p" + indConnRight.toString();
					Conn = "  connect(" + ConnLeft + ", " + ConnRight + ");";
					Blocks[i].UsedInputPins.set(indConnRight-1, true);
					outputInputConnection.add(Conn);
				}
			}
		}
	}
	
	public void OutputConnection() {
		String Conn;
		String ConnLeft;
		String ConnRight;
		String base;
		Element model;
		for (int i=0; i<Blocks.length; i++){
			if (Blocks[i].output.length()>1) { // && (Blocks[i].output.substring(0, 1).equals("&") || Blocks[i].output.substring(0, 1).equals("/"))){
				model = CT.get(Blocks[i].idEu);
				ConnLeft = "pin_"+Blocks[i].output.replaceAll("([\\W|[_]])+", "");
				if (model.idEu.equals(27)) {
					ConnRight = "pin_TerminalVoltage";
				} else if (model.idEu.equals(50)) {
					ConnRight = "pin_FieldCurrent";
				} else if (model.idEu.equals(28)) {
					base = Blocks[i].param[2].replaceAll("([\\W|[_]])+", "");
					ConnRight = "pin_ActivePower" + base;
				} else if (model.idEu.equals(31)) {
					base = Blocks[i].param[2].replaceAll("([\\W|[_]])+", "");
					ConnRight = "pin_ReactivePower" + base;
				} else if (model.idEu.equals(49)) {
					base = Blocks[i].param[2].replaceAll("([\\W|[_]])+", "");
					ConnRight = "pin_FRZ" + base;
				} else if (model.idEu.equals(60)) {
					base = Blocks[i].param[2].replaceAll("([\\W|[_]])+", "");
					ConnRight = "pin_Current";
				} else {
					ConnRight = model.nameModelica + "_" + Blocks[i].count.toString() + ".n1";
				}
				Conn = "  connect(" + ConnLeft + ", " + ConnRight + ");";
				outputOutputConnection.add(Conn);
			} 
		}
	}
	
}
