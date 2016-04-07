/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument.BranchElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.modelica_events_adder.events.records.ConnectRecord;
import eu.itesla_project.modelica_events_adder.events.records.Event;
import eu.itesla_project.modelica_events_adder.events.records.EventCreator;
import eu.itesla_project.modelica_events_adder.events.records.EventRecord;
import eu.itesla_project.modelica_events_adder.events.records.Record;
import eu.itesla_project.modelica_events_adder.events.utils.EventsHelper;
import eu.itesla_project.modelica_events_adder.events.utils.StaticData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class ModEventsExport {
	
	public ModEventsExport(File modelFile, File eventsFile) {
		this.modelFile = modelFile;
		this.eventsFile = eventsFile;
		this.moFileName = modelFile.getName().substring(0, modelFile.getName().indexOf("."));
	}

	public void export() {
		export(Paths.get("."));
	}

	public void export(Path outputParentDir){
		
		
		//We read the events files in order to have a list of devices with events. 
		if(eventsFile != null && eventsFile.length() > 0) {
			eventsParser = new EventsParser(eventsFile);
			eventsParser.parse();
			eventsMap = eventsParser.getEventsMap();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(modelFile));
			
			StringWriter outputStringMo = new StringWriter();
			parseMoFile(reader, outputStringMo);
			
			//A .mo file is created for each event.
			FileWriter writer = null;
			StringWriter stWriter = null;
			EventRecord eventRecord = null;
			String modified, eventFileName, eventFilePrefix;
			
			eventFilePrefix = moFileName + "_events_";
			
			//For each network file with events:
			for(Integer fileId : eventsMap.keySet()) {
				if(eventsMap.get(fileId) == null || eventsMap.get(fileId).isEmpty()) {
					log.error("There is no event.");
					System.exit(0);
				}	
				
				eventFileName = eventFilePrefix + fileId;

				stWriter = new StringWriter();
				stWriter.write(outputStringMo.toString());

				writer = new FileWriter(outputParentDir.resolve(eventFileName + StaticData.MO_EXTENSION).toFile());
				//Modification of the model name
				modified = stWriter.toString().replace(moFileName, eventFileName);
				stWriter = new StringWriter();
				stWriter.write(modified);
				
				eventsList = eventsMap.get(fileId);

				for(Event event : eventsList) {									
					Record record = recordsMap.get(event.getCIMDevice().toLowerCase());
					
					if(record == null) {
						log.warn("This device doesn't exist in the model.");
						continue;
					}
					
					if(event.getType().equals(EventsStaticData.BUS_FAULT)) { //Adding a BUS_FAULT event
						eventRecord = EventCreator.getBusEventRecord(record, event);
						eventRecord.createRecord();
						
						//We add fault
						modified = stWriter.toString().replace(StaticData.EQUATION, eventRecord.toString().concat("\n" + StaticData.EQUATION));
						stWriter = new StringWriter();
						stWriter.write(modified);
						
						//We add the fault connection
						String faultConnect = createBusFaultConnection(eventRecord, record);
						modified = stWriter.toString().replace(StaticData.CON_OTHERS, StaticData.CON_OTHERS.concat("\n").concat(faultConnect));
					}
					else if(event.getType().equals(EventsStaticData.LINE_FAULT)) { //Adding a LINE_FAULT event
						eventRecord = EventCreator.getLineEventRecord(record, event, recordsMap, conRecordsList);
						eventRecord.createRecord();
						
						//We delete the line model and add the line fault.
						modified = stWriter.toString().replace(record.toString().trim(), eventRecord.toString().trim());
						stWriter = new StringWriter();
						stWriter.write(modified);
						
						//We modify the fault connections
						modified = stWriter.toString().replace(record.getModelicaName().concat("."), eventRecord.getModelicaName().concat("."));
					} else if(event.getType().equals(EventsStaticData.LINE_OPEN_REC)) {//Adding a LINE OPEN RECEIVING event
						eventRecord = EventCreator.getLineOpenRecEventRecord(record, event, recordsMap);
						eventRecord.createRecord();

						//We delete the line model and add the line opening receiving.
						modified = stWriter.toString().replace(record.toString().trim(), eventRecord.toString().trim());
					}
					else if(event.getType().equals(EventsStaticData.BANK_MODIF)) {//Adding a BANK MODIFICATION event
						eventRecord = EventCreator.getBankModificationRecord(record, event);
						eventRecord.createRecord();
						
						//We delete the capacitor bank model and add the bank modification event.
						modified = stWriter.toString().replace(record.toString().trim(), eventRecord.toString().trim());
					} else if(event.getType().equals(EventsStaticData.LOAD_VAR)) { //Adding a LOAD VARIATION event
						eventRecord = EventCreator.getLoadVariationRecord(record, event, recordsMap);
						eventRecord.createRecord();
						
						//We delete the load model and add the load variation event.
						modified = stWriter.toString().replace(record.toString().trim(), eventRecord.toString().trim());
					} else if(event.getType().equals(EventsStaticData.LINE_2_OPEN)) {//Adding a LINE 2 OPENINGS event
						eventRecord = EventCreator.getLine2OpenEventRecord(record, event, recordsMap);
						eventRecord.createRecord();

						//We delete the line model and add the line 2 openings.
						modified = stWriter.toString().replace(record.toString().trim(), eventRecord.toString().trim());
					} else if(event.getType().equals(EventsStaticData.BREAKER)) {//Adding a BREAKER
						//If the device is a branch TWO breakers should be created
						String breakerEvent;
						String breakerConnect;
						if(record.getModelicaName().startsWith(StaticData.PREF_LINE) || record.getModelicaName().startsWith(StaticData.PREF_TRAFO)) {
							List<Record> busesRecords = getBusesRecord(record);
							
							eventRecord = EventCreator.getBreakerEventRecord(busesRecords.get(0), event);
							eventRecord.createRecord();
							breakerEvent = eventRecord.toString();
							
							stWriter = deleteConnections(record, busesRecords.get(0), stWriter);
							breakerConnect = createBreakerConnection(eventRecord, busesRecords.get(0), record);
							
							eventRecord = EventCreator.getBreakerEventRecord(busesRecords.get(1), event);
							eventRecord.createRecord();
							breakerEvent = breakerEvent.concat("\n").concat(eventRecord.toString());
							
							stWriter = deleteConnections(record, busesRecords.get(1), stWriter);
							breakerConnect = breakerConnect.concat("\n").concat(createBreakerConnection(eventRecord, busesRecords.get(1), record));
						} else {
							Record busRecord = getBus(record);
							
							eventRecord = EventCreator.getBreakerEventRecord(busRecord, event);
							eventRecord.createRecord();
							breakerEvent = eventRecord.toString();
							
							breakerConnect = createBreakerConnection(eventRecord, busRecord, record);
							
							stWriter = deleteConnections(record, busRecord, stWriter);
						}
						
						//We add the Breaker/s
						modified = stWriter.toString().replace(StaticData.EQUATION, breakerEvent.concat("\n" + StaticData.EQUATION));
						stWriter = new StringWriter();
						stWriter.write(modified);
						modified = stWriter.toString().replace(StaticData.CON_OTHERS, StaticData.CON_OTHERS.concat("\n").concat(breakerConnect));
					}
					
					if(record != null) {
						stWriter = new StringWriter();
						stWriter.write(modified);
					}
				}
				
				writer.write(stWriter.toString());				
				writer.close();
			}
		}catch (FileNotFoundException e) {
				log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	private StringWriter deleteConnections(Record record1, Record record2, StringWriter stWriter) {
		String modified = null;
		for(ConnectRecord conRec : conRecordsList) {
			if(conRec.containsElement(record1.getModelicaName()) && conRec.containsElement(record2.getModelicaName())) 
			{
				modified = stWriter.toString().replace(conRec.getConnectLine(), "");
			}
		}
		stWriter = new StringWriter();
		stWriter.write(modified);
		
		return stWriter;
	}
	
	private Record getBus(Record record) {
		String busName = null;
		for(ConnectRecord conRec : conRecordsList) 
		{			
			if(conRec.containsElement(record.getModelicaName())) 
			{
				if(conRec.getConnectedElement(record.getModelicaName()).startsWith(StaticData.PREF_BUS)) 
				{
					busName = conRec.getConnectedElement(record.getModelicaName());
					break;
				}
			} 
		}
		
		return recordsMap.get(parseName(busName).toLowerCase());
	}
	
	private List<Record> getBusesRecord(Record record) {
		List<ConnectRecord> busesList = new ArrayList<ConnectRecord>();
		
		int i = 0;
		for(ConnectRecord conRec : conRecordsList) 
		{			
			if(conRec.containsElement(record.getModelicaName())) 
			{
				if(conRec.getConnectedElement(record.getModelicaName()).startsWith(StaticData.PREF_BUS)) 
				{
					busesList.add(conRec);
					i++;
				}
			}
			if(i == 2) { 
				break;
			} 
		}
		
		String busFrom = busesList.get(0).getNodeF().equalsIgnoreCase(record.getModelicaName()) ? busesList.get(1).getNodeF() : busesList.get(0).getNodeF();
		String busTo = busesList.get(0).getNodeT().equalsIgnoreCase(record.getModelicaName()) ? busesList.get(1).getNodeT() : busesList.get(0).getNodeT();
		
		List<Record> twoSideBuses = new ArrayList<Record>();
		twoSideBuses.add(recordsMap.get(parseName(busFrom).toLowerCase()));
		twoSideBuses.add(recordsMap.get(parseName(busTo).toLowerCase()));
		
		return twoSideBuses;
	}
	
	private void parseMoFile(BufferedReader reader, Writer writer) throws IOException {
		String line = null;
		boolean isInsideRecord = false;
		Record record = null;
		ConnectRecord connectRecord = null;
		
		String[] data;
		String modelicaType = null; 
		String modelicaName = null;
		String elemF = null, elemT = null;
		Map<String, String> paramsMap = new HashMap<>();
		
		while ((line = reader.readLine()) != null) {

			//Create the Modelica records
			if(line.trim().startsWith(StaticData.IPSL)) {
				paramsMap = new HashMap<String, String>();
				isInsideRecord = true;
				record = new Record();
				
				data = line.trim().split(StaticData.WHITE_SPACE);
				modelicaType = data[0];
				modelicaName = data[1].endsWith("(") ? (data[1]).split("\\(")[0] : data[1];

				record.setModelicaType(modelicaType);
				record.setModelicaName(modelicaName);
				
				record.addValue(line);
				record.addValue(StaticData.NEW_LINE);
			}
			else if(line.trim().startsWith(StaticData.CONNECT)) {
				data = line.trim().split(",");
				elemF = data[0].trim().substring(data[0].indexOf("(")+1, data[0].indexOf("."));
				elemT = data[1].trim().substring(0, data[1].indexOf(".")-1);
				connectRecord = new ConnectRecord(elemF, elemT, line.trim());
				
				conRecordsList.add(connectRecord);
			}
			else if(line.trim().endsWith(StaticData.SEMICOLON) && isInsideRecord) {		
				isInsideRecord = false;
				record.addValue(line);
				record.addValue(StaticData.NEW_LINE);
				record.setParamsMap(paramsMap);
				
				String CIMid = EventsHelper.parseModelicaToCIM(modelicaName, modelicaType);
				
				if(!recordsMap.containsKey(CIMid)) {
					recordsMap.put(CIMid, record);
				}
			}
			else if(isInsideRecord) {
				record.addValue(line);
				record.addValue(StaticData.NEW_LINE);
				
				String[] par = line.trim().split("=");
				
				paramsMap.put(par[0].trim(), par[1].split(",")[0].trim());
			}
			
			writer.append(line);
			writer.append(StaticData.NEW_LINE);
		}
	}
	
	private String createBusFaultConnection(EventRecord eventRecord, Record busRecord) {
		String faultConnect = StaticData.CONNECT;
		String nodeName1 = eventRecord.getModelicaName().concat(".").concat(StaticData.POSITIVE_PIN);
		String nodeName2 = busRecord.getModelicaName().concat(".").concat(StaticData.POSITIVE_PIN);
		faultConnect = faultConnect + nodeName1 + ", " + nodeName2 + StaticData.ANNOT_CONNECT;
		
		return faultConnect;
	}
	
	private String createBreakerConnection(EventRecord eventRecord, Record busRecord, Record otherSideRecord) {
		String breakerConnect = StaticData.CONNECT;
		String nodeName1 = eventRecord.getModelicaName().concat(".").concat(StaticData.BREAKER_R_PIN);
		String nodeName2 = busRecord.getModelicaName().concat(".").concat(StaticData.POSITIVE_PIN);
		breakerConnect = breakerConnect + nodeName1 + ", " + nodeName2 + StaticData.ANNOT_CONNECT;
		
		breakerConnect = breakerConnect + StaticData.NEW_LINE;
		
		//Creating the connect to the other side
		breakerConnect = breakerConnect + StaticData.CONNECT;;
		nodeName1 = eventRecord.getModelicaName().concat(".").concat(StaticData.BREAKER_S_PIN);
		nodeName2 = otherSideRecord.getModelicaName().concat(".").concat(StaticData.POSITIVE_PIN);
		breakerConnect = breakerConnect + nodeName1 + ", " + nodeName2 + StaticData.ANNOT_CONNECT;
		
		return breakerConnect;
	}
		public String parseName(String name) {
		String parsedName;

		//Remove the bus prefix = "bus_"
		name = name.substring(5);
		parsedName = "_" + name.replaceAll("_", "-");
		
		return parsedName;
	}
	
	private Map<String, Record> recordsMap = new HashMap<String, Record>();
	private List<ConnectRecord> conRecordsList = new ArrayList<ConnectRecord>();
	private List<Event> eventsList;
	private Map<Integer, List<Event>> eventsMap = new HashMap<Integer, List<Event>>();
	
	private File			modelFile;
	private String			moFileName;
	private File			eventsFile;
	private EventsParser	eventsParser;
	
	private static final Logger log = LoggerFactory.getLogger(ModEventsExport.class);

}
