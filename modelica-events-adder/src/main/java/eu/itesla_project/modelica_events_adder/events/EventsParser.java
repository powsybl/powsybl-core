/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.HashAttributeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.modelica_events_adder.events.records.Event;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class EventsParser {
	
	public EventsParser(File eventsFile) {
		this.eventsFile = eventsFile;
	}

	public void parse() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(this.eventsFile));
			String line = null;
		
			while((line = reader.readLine()) != null) {
				if(line.startsWith("#")) { //Commented events
					continue;
				}
				Event event;
				
				List<String> data = new ArrayList<String>(Arrays.asList(line.split(";")));
				int fileId = Integer.parseInt(data.get(0));
				data.remove(0);
				int id = Integer.parseInt(data.get(0));
				data.remove(0);
				String eventType = data.get(0);
				data.remove(0);
				String device = data.get(0);
				data.remove(0);
				
				event = new Event(fileId, id, eventType, device, data);
				
				//
				if(eventsMap.containsKey(fileId)) {
					eventsMap.get(fileId).add(event);
				}
				else {
					List<Event> evList = new ArrayList<Event>();
					evList.add(event);
					eventsMap.put(fileId, evList);
				}
				
				//
				eventsList.add(event);
				eventsDevices.add(device);
			}
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}
	}
	
	public List<Event> getEventsList() {
		return eventsList;
	}

	public void setEventsList(List<Event> eventsList) {
		this.eventsList = eventsList;
	}

	public List<String> getEventsDevices() {
		return eventsDevices;
	}

	public void setEventsDevices(List<String> eventsDevices) {
		this.eventsDevices = eventsDevices;
	}

	public Map<Integer, List<Event>> getEventsMap() {
		return eventsMap;
	}

	public void setEventsMap(Map<Integer, List<Event>> eventsMap) {
		this.eventsMap = eventsMap;
	}



	private Map<Integer, List<Event>> eventsMap = new HashMap<Integer, List<Event>>();
	private List<Event> eventsList = new ArrayList<Event>();
	private List<String> eventsDevices = new ArrayList<String>();
	private File eventsFile;
	
	private static final Logger _log = LoggerFactory.getLogger(EventsParser.class);
}
