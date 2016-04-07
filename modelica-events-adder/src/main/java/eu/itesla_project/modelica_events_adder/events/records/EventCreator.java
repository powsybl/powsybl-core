/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.records;

import java.util.List;
import java.util.Map;


/**
 * @author Silvia Machado <machados@aia.es>
 */
public final class EventCreator {

	public static BusFaultRecord getBusEventRecord(Record busRecord, Event event) {
		BusFaultRecord busFaultRecord = new BusFaultRecord(busRecord, event);
		busFaultRecord.createModelicaName();
		
		return busFaultRecord;
	}
	
	public static LineFaultRecord getLineEventRecord(Record lineRecord, Event event, Map<String, Record> recordsMap, List<ConnectRecord> conRecList) {
		LineFaultRecord lineFaultRecord = new LineFaultRecord(lineRecord, event, recordsMap, conRecList);
		lineFaultRecord.createModelicaName();
		
		return lineFaultRecord;
	}	
	
	public static LineOpeningReceivingRecord getLineOpenRecEventRecord(Record lineRecord, Event event, Map<String, Record> recordsMap) {
		LineOpeningReceivingRecord lineOpenRecRecord = new LineOpeningReceivingRecord(lineRecord, event, recordsMap);
		lineOpenRecRecord.createModelicaName();
		
		return lineOpenRecRecord;
	}
	
	public static BankModificationRecord getBankModificationRecord(Record bankRecord, Event event) {
		BankModificationRecord bankModifRecord = new BankModificationRecord(bankRecord, event);
		bankModifRecord.createModelicaName();
		
		return bankModifRecord;
	}
	
	public static LoadVariationRecord getLoadVariationRecord(Record loadRecord, Event event, Map<String, Record> recordsMap) {
		LoadVariationRecord loadVariationRecord = new LoadVariationRecord(loadRecord, event, recordsMap);
		loadVariationRecord.createModelicaName();
		
		return loadVariationRecord;
	}	
	
	public static Line2OpeningsRecord getLine2OpenEventRecord(Record lineRecord, Event event, Map<String, Record> recordsMap) {
		Line2OpeningsRecord line2OpenRecord = new Line2OpeningsRecord(lineRecord, event, recordsMap);
		line2OpenRecord.createModelicaName();
		
		return line2OpenRecord;
	}
	
	public static BreakerRecord getBreakerEventRecord(Record busRecord, Event event) {
		BreakerRecord breakerRecord = new BreakerRecord(busRecord, event);
		breakerRecord.createModelicaName();
		
		return breakerRecord;
	}
	
}
