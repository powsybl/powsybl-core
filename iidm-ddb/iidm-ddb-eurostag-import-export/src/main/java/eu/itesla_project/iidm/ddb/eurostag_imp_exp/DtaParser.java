/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DtaParser {

	static Logger log = LoggerFactory.getLogger(DtaParser.class.getName());



	public DtaParser(final String name) {
	}

	static HashMap<String, String[]> componentsVariablesNames = new HashMap<String, String[]>();
	static HashMap<String, String[]> componentsInterfaceVariablesNames = new HashMap<String, String[]>();

	static {
		componentsVariablesNames.put("HEADER", new String[] {
				"keyword,date,version"
		});

		componentsVariablesNames.put("R", new String[] {
				"keyword, machine.name"
				,"macroblock.name,psetnum,coupling.par1,coupling.par2,coupling.par3,coupling.par4"
				,"coupling.par5,coupling.par6,coupling.par7,coupling.par8,coupling.par9"
		});

		componentsVariablesNames.put("LOADP", new String[] {
				"keyword, pattern.identifier"
				,"vf_subload.name,VF_RPPROPA,VF_RQPROPA"
				,"reg_subload.name,REG_RPPROPA,REG_RQPROPA,REG_RALFAPA,REG_RBETAPA,REG_RGAMMAPA,REG_RDELTAPA"
				,"dis_subload.name,DIS_RPPROPA,DIS_RQPROPA,DIS_RKSIMIMP,DIS_RKSIMAXP,DIS_RTTFOPA,DIS_RTAUFTOP,DIS_RUREFPA,DIS_RRTFOPA,DIS_RXTFOPA,DIS_RDELTAVP,DIS_RXSRPA,DIS_RTAUPA,DIS_RMUPA,DIS_GENMOTORNAME"
		});

		componentsVariablesNames.put("CH", new String[] {
				"keyword"
				,"identifier,definition,node.name,area.name,dynamic.zone.name"
		});


		componentsVariablesNames.put("M1", new String[] {
				/*record 1*/	 "machine.type, type.fortescue, saturated, XMACOUP, fnum1, fnum2, falpha"
				/*record 2*/	,"machine.name,connection.node.name,SN,UN,PP,PQ,DAMP,H"
				/*record 3*/	,"RA,WL,WLMD,WMRC,RDD,WLDD,RF,WLF"
				/*record 4*/	,"WLMDV,WLMQ,RQ1,WLQ1,RQ2,WLQ2,IWLMDV"
				/*record 5*/	,"transformer.included,RT,XT,PNT,UNM,UNN,PN,PNALT"
				/*record 6*/	,"RMD,RND,RMQ,RNQ,RIFO,RIXFO,R0FO,R0XFO"
		});

		componentsInterfaceVariablesNames.put("M1", new String[] {
				"UR", "UI", "URI", "UII", "URH", "UIH", "ID", "IQ", "OMEGA", "TETA", "EFD", "CM", 
				"LAMBDAF", "LAMBDAD", "LAMBDAQ1", "LAMBDAQ2", "LAMBDAAD", "LAMBDAAQ", 
				"TerminalVoltage", "FieldCurrent",
				"ActivePowerPNALT", "ActivePowerPN", "ActivePowerSNREF", 
				"ReactivePowerPNALT", "ReactivePowerPN", "ReactivePowerSNREF", "Current",
				"pin_ActivePowerSN", "pin_ReactivePowerSN"
		});

		componentsVariablesNames.put("M2", new String[] {
				/*record 1*/	 "machine.type, type.fortescue, saturated, XMACOUP, fnum1, fnum2, falpha"
				/*record 2*/	,"machine.name,connection.node.name,SN,UN,PP,PQ,DAMP,H"
				/*record 3*/	,"RA,WL,XD,XPD,XSD,TPD0,TSD0,TX"
				/*record 4*/	,"WLMDV,XQ,XPQ,XSQ,TPQ0,TSQ0,IMOD,IENR,IWLMDV"
				/*record 5*/	,"transformer.included,RT,XT,PNT,UNM,UNN,PN,PNALT"
				/*record 6*/	,"RMD,RND,RMQ,RNQ,RIFO,RIXFO,R0FO,R0XFO"
		});

		componentsInterfaceVariablesNames.put("M2", new String[] {
				"UR", "UI", "URI", "UII", "URH", "UIH", "ID", "IQ", "OMEGA", "TETA", "EFD", "CM", 
				"LAMBDAF", "LAMBDAD", "LAMBDAQ1", "LAMBDAQ2", "LAMBDAAD", "LAMBDAAQ", 
				"TerminalVoltage", "FieldCurrent",
				"ActivePowerPNALT", "ActivePowerPN", "ActivePowerSNREF", 
				"ReactivePowerPNALT", "ReactivePowerPN", "ReactivePowerSNREF", "Current",
				"pin_ActivePowerSN", "pin_ReactivePowerSN"
		});

		componentsVariablesNames.put("ABEGIN", new String[] {
				"keyword"
		});

		componentsVariablesNames.put("AEND", new String[] {
				"keyword"
		});

		componentsVariablesNames.put("A11", new String[] {
				"machine.name,connection.node.name,USINF,USRINF,TINF,TRINF,DELINF"
				,"connection.node.name,USSUP,USRSUP,TSUP,TRSUP,DELSUP"
		});

		componentsVariablesNames.put("A12", new String[] {
				"machine.name,VIMIN,TMIN,VIMAX,TMAX,TDEL"
		});
		
		componentsVariablesNames.put("A14", new String[] {
				"sending.node,receiving.node,index,R,E1,E2,T1,TINT,setpoint,VC,time.margin,transfo.name,control.type,ZNREF,transfo.side"
				,"tap.direction,TMAN,V1,V2,TV1,TDEL"
		});
		
		componentsVariablesNames.put("A33_ACMC", new String[] {
				"keyword,ma.name,interface.name,block.number,c.type,S1,S2,S3,T1,TDEL"
			   ,""
			   ,"ev.keyword,ev.type,keyword,ma.name,interface.name,block.number"
			   ,"equipment.name,seq.params"
		});
		
		componentsVariablesNames.put("M21", new String[] {
				 "machine.type, type.fortescue, XMACOUP, fnum1, fnum2, falpha"
				,"machine.name,connection.node.name,type.power.assigned,PP,PQ,PN,RIFO,RIXFO,R0FO,R0XFO"
		});

		componentsInterfaceVariablesNames.put("M21", new String[] {
				"TerminalVoltage"
		});
		
		componentsVariablesNames.put("MA", new String[] {
				  "keyword"
				 ,"ma.name,equipment.type,equipment.name"
		});
		
		componentsInterfaceVariablesNames.put("MA", new String[] {
				"TerminalVoltage"
		});
		
		componentsVariablesNames.put("RMA", new String[] {
				"keyword, machine.name"
				,"macroblock.name,psetnum,coupling.par1,coupling.par2,coupling.par3,coupling.par4"
				,"coupling.par5,coupling.par6,coupling.par7,coupling.par8,coupling.par9"
		});
		
		componentsVariablesNames.put("TRF", new String[] {
				"keyword"
				,"type.zone, zone.name, sending.node.name, receiving.node.name, parallel.index, device.name, time.down.change, max.taps.down, time.up.change, max.taps.up, dyn.zone.name, dyn, three.name, three"
		});
		
		componentsVariablesNames.put("BAT", new String[] {
				"keyword"
				,"type.zone, zone.name, bank.name, model.name, min.time1, min.time2, min.time3, min.time4"
		});

	}

	static HashMap<String, String[]> componentsDescriptors = new HashMap<String, String[]>();
	static {
		//OK, verified
		componentsDescriptors.put("HEADER", new String[] {
				"(A6, 5X, A8, 1X, A8)"
		});

		//OK, verified
		componentsDescriptors.put("M1", new String[] {
				/*record 1*/ "(A2, A1, 6X, A1, 1X, A1, 1X, F8, 1X, F8, 1X, A8)"
				/*record 2*/,"(A8, 1X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				/*record 3*/,"(9X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				/*record 4*/,"(9X, F8, 10X, F8, 10X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, A1)"
				/*record 5*/,"(8X, A1, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				/*record 6*/,"(9X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"});

		//OK, verified
		componentsDescriptors.put("M2", new String[] {
				/*record 1*/ "(A2, A1, 6X, A1, 1X, A1, 1X, F8, 1X, F8, 1X, A8)"
				/*record 2*/,"(A8, 1X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				/*record 3*/,"(9X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				/*record 4*/,"(9X, F8, 10X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, A1, 1X, A1, 1X, 5X, A1)"
				/*record 5*/,"(8X, A1, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				/*record 6*/,"(9X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"});

		//OK, verificato
		componentsDescriptors.put("R", new String[] {
				"(A1, 1X, A8)"
				//,"(A8, 1X, I3)"  //first version
				//,"(A8, 1X ,I3,13X,A2,1X,A19,1X,A1,1X,A24,1X,A24,1X,A24)" // second version
				,"(A8, 1X ,I3,13X,A24,1X,A24,1X,A24,1X,A24)"
				,"(A24,1X,A24,1X,A24,1X,A24,1X,A24)"
		});

		componentsDescriptors.put("LOADP", new String[] {
				"(A5, 4X, A8)"
				,"(9X, A8, 1X, F8, 1X, F8)"
				,"(9X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				,"(9X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X )"
		});

		componentsDescriptors.put("CH", new String[] {
				"(A2)"
				,"(A8, 1X, A1, 1X, A8, 1X, A2, 1X, A8)"
		});

		componentsDescriptors.put("ABEGIN", new String[] {
				"(A3)"
		});

		componentsDescriptors.put("AEND", new String[] {
				"(A6)"
		});

		componentsDescriptors.put("A11", new String[] {
				"(A8, 1X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
				,"(9X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
		});

		componentsDescriptors.put("A12", new String[] {
				"(A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
		});
		
		componentsDescriptors.put("A14", new String[] {
				"(A8, 1X, A8, 1X, A1, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, A1, 1X, F8, 1X, F5, 1X, A8, 1X, I1, 1X, A8, 1X, A1)"
				,"(17X, A2, 10X, F8, 7X, F8, 1X, F8, 1X, F8, 1X, F8)"
		});
		
		componentsDescriptors.put("A33_ACMC", new String[] {
				"(A3,1X,A8,1X,A8,1X,A8,1X,I1,1X,F8,1X,F8,1X,F8,1X,F8,1X,F8)"
				,"()"
				,"(A2,1X,A8,1X,A3,1X,A8,1X,A8,1X,A8)"
				,"(18X,A8,1X,A52)"
		});
		
		componentsDescriptors.put("M21", new String[] {
				 "(A3, A1, 1X, A1, 7X, F8, 1X, F8, 1X, A8)"
				,"(A8, 1X, A8, 1X, A1, 17X, F8, 1X, F8, 19X, F8, F8, 1X, F8, 1X, F8, 1X, F8)"
		});
		
		componentsDescriptors.put("MA", new String[] {
				 "(A2)"
				,"(A8, 1X, A2, 1X, A8)"
		});
		
		componentsDescriptors.put("RMA", new String[] {
				"(A3, 1X, A8)"
				//,"(A8, 1X, I3)"  //first version
				//,"(A8, 1X ,I3,13X,A2,1X,A19,1X,A1,1X,A24,1X,A24,1X,A24)" // second version
				,"(A8, 1X ,I3,13X,A24,1X,A24,1X,A24,1X,A24)"
				,"(A24,1X,A24,1X,A24,1X,A24,1X,A24)"
		});
		
		componentsDescriptors.put("TRF", new String[] {
				"(A3)"
			   ,"(A3, 6X, A2, 1X, A8, 1X, A8, 1X, A1, 1X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8, 1X, A8, 1X, A1, 1X, A8, A1)"
		});
		
		componentsDescriptors.put("BAT", new String[] {
				"(A3)"
			   ,"(A3, 6X, A2, 10X, A8, 1X, A8, 1X, F8, 1X, F8, 1X, F8, 1X, F8)"
		});
	}


	public static ArrayList<String> getVarNames(String title, int recnum) {
		String headNames=componentsVariablesNames.get(title)[recnum];
		StringTokenizer st = new StringTokenizer(headNames,",");
		ArrayList<String> retlist=new ArrayList<String>();
		while (st.hasMoreTokens()) {
			retlist.add(st.nextToken().trim());
		}
		return retlist;
	}

	public static String getRealTypeVar(String varTypes, int num) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < varTypes.length(); i++) {
			switch(varTypes.charAt(i)) {
			case '(': case ')': break;
			default: sb.append(varTypes.charAt(i));
			}
		}
		String varTypesStripped = sb.toString();
		StringTokenizer st = new StringTokenizer(varTypesStripped,",");
		ArrayList<String> realVarTypes=new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String curToken=st.nextToken().trim();
			if ((curToken.startsWith("F")) || (curToken.startsWith("A"))  || (curToken.startsWith("I"))) {
				realVarTypes.add(curToken);
			}
		}
		return realVarTypes.get(num);
	}

	public static String getVarFType(String componentName, String varName) {
		String varNames[]=componentsVariablesNames.get(componentName);
		String varTypes[]=componentsDescriptors.get(componentName);
		if ((varNames==null) || (varTypes==null))
			return null;

		//first step find record num containing varName
		int recNum=-1;
		int posNum=-1;
		for (int i = 0; i < varNames.length; i++) {
			StringTokenizer st = new StringTokenizer(varNames[i],",");
			int j=0;
			while (st.hasMoreTokens()) {
				String curToken=st.nextToken().trim();
				if (varName.equals(curToken)) {
					recNum=i;
					posNum=j;
					break;
				}
				j=j+1;
			}
			if (recNum != -1) {
				break;
			}
		}

		// step 2
		if (recNum!=-1) {
			String curRecVarTypes=varTypes[recNum];
			return getRealTypeVar(curRecVarTypes,posNum);
		}
		return null;
	}

	public static Set<String> getInterfaceVariablesNamesByComponentTypeName(String componentTypeName) {
		if (componentsInterfaceVariablesNames.containsKey(componentTypeName)) {
			return new HashSet<String>(Arrays.asList(componentsInterfaceVariablesNames.get(componentTypeName)));
		} else {
			return new HashSet<String>();
		}
	}

	public static void dumpHeader(Date date, String version, PrintStream out) throws ParseException, IOException {
		FortranFormat ff= new FortranFormat("()");
		ArrayList<Object> values = new ArrayList<Object>();
		values.add("HEADER");
		values.add(new SimpleDateFormat("dd/MM/YY").format(date));
		values.add(version);
		out.println(ff.write(values, componentsDescriptors.get("HEADER")[0]));
		out.println();
	}
	
	public static void dumpAutomatonHeader(String automatonType, boolean end, PrintStream out) throws ParseException, IOException {
		FortranFormat ff= new FortranFormat("()");
		ArrayList<Object> values = new ArrayList<Object>();
		if(!end) {
			values.add(automatonType);
			out.println(ff.write(values, componentsDescriptors.get("ABEGIN")[0]));
		} else {
			values.add("FIN"+automatonType);
			out.println(ff.write(values, componentsDescriptors.get("AEND")[0]));
			out.println();
		}
	}

	public static void dumpZone(EurostagRecord zone, PrintStream out) throws ParseException, IOException {
		//System.out.println("Dumping zone: " + zone.typeName);
		FortranFormat ff= new FortranFormat("()");
		switch (zone.typeName) {
		case "M1": case "M2": case "M21": case "TRF": case "BAT": case "MA" : case "A33_ACMC" :
			int rcount=1;
			String[] recordsFormatting=componentsDescriptors.get(zone.typeName);
			for (String string : recordsFormatting) {
				ArrayList<String> varNames=getVarNames(zone.typeName,rcount-1);
				ArrayList<Object> values = new ArrayList<Object>();
				for (String varName : varNames) {
					Object varVal=zone.data.get(varName);
					values.add(varVal);
					//log.debug(" dumping eurostag zone: " + zone.data.get(varName));
				}
				out.print(ff.write(values, recordsFormatting[rcount-1]));
				out.println();
				rcount++;
			}
			out.println();
			break;

		case "R": case "RMA":
			rcount=1;
			recordsFormatting=componentsDescriptors.get(zone.typeName);

			//standard keywords are the basic eurostag parameters defining the macroblock
			ArrayList<String> standardKeywords=new ArrayList<>();
			for (String string : recordsFormatting) {
				ArrayList<String> varNames=getVarNames(zone.typeName,rcount-1);
				ArrayList<Object> values = new ArrayList<Object>();
				for (String varName : varNames) {
					values.add(zone.data.get(varName));
					standardKeywords.add(varName);
				}
				out.print(ff.write(values, recordsFormatting[rcount-1]));
				out.println();
				rcount++;
			}

			// monitored block : TBD
			out.println();

			//supplemental parameters (
			ArrayList<String> exoKeywords=new ArrayList<>();
			for (String key : zone.data.keySet()) {
				if (!standardKeywords.contains(key)) {
					exoKeywords.add(key);
				}
			}

			if (exoKeywords.size()>0) {
				for (String key : exoKeywords) {
					ArrayList<Object> values2 = new ArrayList<Object>();
					values2.add(key);
					values2.add(zone.data.get(key));
					out.println(ff.write(values2, "(A8,1X,F8)"));
				}
			}

			out.println();
			break;

		case "LOADP":
			int rcountl=1;
			String[] recordsFormattingl=componentsDescriptors.get(zone.typeName);
			for (String string : recordsFormattingl) {
				ArrayList<String> varNames=getVarNames(zone.typeName,rcountl-1);
				ArrayList<Object> values = new ArrayList<Object>();
				for (String varName : varNames) {
					values.add(zone.data.get(varName));
				}
				out.print(ff.write(values, recordsFormattingl[rcountl-1]));
				out.println();
				rcountl++;
			}
			out.println();
			//tobefixed: rte suggestion to make the simulation run, add two white lines after LOADP
			out.println();
			out.println();
			break;

		case "CH":
			int chcountl=1;
			String[] recordsFormattingch=componentsDescriptors.get(zone.typeName);
			for (String string : recordsFormattingch) {
				ArrayList<String> varNames=getVarNames(zone.typeName,chcountl-1);
				ArrayList<Object> values = new ArrayList<Object>();
				for (String varName : varNames) {
					values.add(zone.data.get(varName));
				}
				out.print(ff.write(values, recordsFormattingch[chcountl-1]));
				out.println();
				chcountl++;
			}
			out.println();
			break;

		case "A11": case "A12": case "A14":
			int acountl=1;
			String[] recordsFormattinga=componentsDescriptors.get(zone.typeName);
			for (String string : recordsFormattinga) {
				ArrayList<String> varNames=getVarNames(zone.typeName,acountl-1);
				ArrayList<Object> values = new ArrayList<Object>();
				for (String varName : varNames) {
					values.add(zone.data.get(varName));
				}
				out.print(ff.write(values, recordsFormattinga[acountl-1]));
				out.println();
				acountl++;
			}
			break;

		default:
			break;
		}

	}

	public static void dumpGensInertia(String cimId, EurostagRecord zone, PrintStream out) throws ParseException, IOException {
		switch (zone.typeName) {
		case "M1": case "M2":
			int rcount=1;
			Object genInertiaObj=zone.data.get("H");
			//retrieve machine's inertia data
			out.print(cimId+";"+zone.data.get("machine.name")+";"+zone.data.get("H"));
			out.println();
			break;
		default:
			break;
		}

	}


	public static ArrayList<EurostagRecord> parseZones(Path dtaFile) throws IOException, ParseException {

		ArrayList<EurostagRecord> retZones=new ArrayList<EurostagRecord>();

		try (BufferedReader reader = Files.newBufferedReader(dtaFile, StandardCharsets.UTF_8)) {
			String line = reader.readLine();
			ArrayList<Object> repat=FortranFormat.read(line, componentsDescriptors.get("HEADER")[0]);

			if (!"HEADER".equals(repat.get(0))) {
				throw new RuntimeException("not a .dta file");
			}

			ArrayList<String> headVarNames=getVarNames("HEADER",0);
			int ii=0;
			for (Object obj : repat) {
				//System.out.println(headVarNames.get(ii) + "=" + obj);
				ii++;
			}

			//		String eurostagVersion=(String) repat.get(2);
			//		System.out.println("eversion= " + eurostagVersion);

			int lineNum=0;
			while ( line != null ) {
				line = reader.readLine();
				log.debug("- readline outer: " + line);
				if (line == null)
					continue;


				for (String hkey : componentsDescriptors.keySet()) {
					if (line.startsWith(hkey)) {
						log.debug("readline inner(recognized section): " + line);
						HashMap<String,Object> compHash=new HashMap<String,Object>();
						ArrayList<ArrayList<Object>> compArray=new ArrayList<ArrayList<Object>>();
						int i=0;
						for (String string : componentsDescriptors.get(hkey)) {
							compArray.add(FortranFormat.read(line, string));
							i++;
							if (i<componentsDescriptors.get(hkey).length)
								line = reader.readLine();
							log.debug("readline inner: " + line);
						}
						int rcount=1;

						//then  join properties names
						for (ArrayList<Object> record : compArray) {
							//System.out.println(" record "+ rcount+" "+record);

							ArrayList<String> varNames=getVarNames(hkey,rcount-1);
							int varid=0;
							for (Object obj : record) {
								//System.out.println(varNames.get(varid)+"="+obj);
								compHash.put(varNames.get(varid), obj);
								varid++;
							}

							rcount++;
						}

						EurostagRecord newZone=new EurostagRecord(hkey,compHash);
						switch (newZone.typeName) {
						case "M2": case "M1":
							String retVal="";
							retVal+=newZone.typeName;
							if (newZone.getData().get("type.fortescue") != null)
								retVal+=newZone.getData().get("type.fortescue");
							else retVal+=" ";
							if (newZone.getData().get("saturated") != null)
								retVal+=newZone.getData().get("saturated");
							else retVal+=" ";
							newZone.setKeyName(retVal);
							break;
						default:
							newZone.setKeyName(newZone.getTypeName());
							break;
						}

						retZones.add(newZone);


						lineNum++;
					}


				}
			}
		}
		return retZones;


	}



	/*
	 * read mapping CIM_ID, EUROSTAG_ID from a csv file
	 * assume here that first column in csv is the CIM id, whilst the second column is the BDD id
	 * other columns are simply skipped
	 */
	public static Map<String, String> readWithCsvMapReader(Path dicoFile) throws Exception {

		Map<String,String> retMap=new HashMap<>();
		try (ICsvMapReader mapReader = new CsvMapReader(Files.newBufferedReader(dicoFile, StandardCharsets.UTF_8), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {
			final String[] header = mapReader.getHeader(true);
			log.debug(" cvsheader length: " +header.length);
			final CellProcessor[] rowProcessors=new CellProcessor[header.length];
			for (int i = 0; i < rowProcessors.length; i++) {
				if (i==0) {
					rowProcessors[i]=new NotNull();
				} else {
					rowProcessors[i]=null;
				}
			}

			Map<String, Object> componentMap;
			while( (componentMap = mapReader.read(header, rowProcessors)) != null ) {
				//System.out.println(String.format("lineNo=%s, rowNo=%s, mapping=%s", mapReader.getLineNumber(), mapReader.getRowNumber(), customerMap));
				String eurostagId=	(String)componentMap.get(header[1]);
				String cimId=	(String)componentMap.get(header[0]);
				if (eurostagId == null ) {
					log.warn("eurostagId=" + eurostagId +", cimId="+cimId);
				} else {
					if (retMap.containsKey(eurostagId)) {
						log.warn("eurostagId=" + eurostagId +" already in the map");
					}
					retMap.put(eurostagId, cimId );
				}
			}
		}

		if (log.isTraceEnabled()) {
			log.trace("ids map: " + retMap);
		}
		return retMap;

	}



}
