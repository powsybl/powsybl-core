/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling.util;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import eu.itesla_project.modules.histo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DataMiningFacadeHistodb implements DataMiningFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataMiningFacadeHistodb.class);

	private final HistoDbClient histoClient;

	public DataMiningFacadeHistodb(HistoDbClient histoClient) {
		this.histoClient=histoClient;
	}

    @Override
	public Wp41HistoData getDataFromHistoDatabase(DataMiningFacadeParams dmParams) throws IOException, InterruptedException {
		LOGGER.info("Get histodb data");
		LOGGER.info("	interval {}", dmParams.getInterval());
		LOGGER.info("	gens {}", dmParams.getGensIds());
		LOGGER.info("	loads {}", dmParams.getLoadsIds());
		LOGGER.info("	dangling lines {}", dmParams.getDanglingLinesIds());

		return parseData(dmParams);
	}

	private static void parseCsv(InputStream is, Set<HistoDbAttributeId> ids, Table<Integer,String,Float> hdTable, int expectedRowCount) throws IOException {
		int rowcount=0;
		try (ICsvMapReader mapReader = new CsvMapReader(new InputStreamReader(is), new CsvPreference.Builder('"', ',', "\r\n").build())) {

			final String[] header = mapReader.getHeader(true);

			final CellProcessor[] rowProcessors = new CellProcessor[header.length];
			Map<String, AtomicInteger> observationCount = new TreeMap<>();
            for (HistoDbAttributeId id : ids) {
                if (id instanceof HistoDbNetworkAttributeId) { // skip meta data attributes
                    observationCount.put(((HistoDbNetworkAttributeId) id).getEquipmentId(), new AtomicInteger());
                }
            }
			Map<String, Object> componentMap;
			while( (componentMap = mapReader.read(header, rowProcessors)) != null ) {
				for (HistoDbAttributeId id : ids) {
                    if (id instanceof HistoDbNetworkAttributeId) { // skip meta data attributes
                        String val = (String) componentMap.get(id.toString());
                        if (val != null) {
                            observationCount.get(((HistoDbNetworkAttributeId) id).getEquipmentId()).incrementAndGet();
                        }
                        hdTable.put(rowcount, id.toString(), val != null ? Float.valueOf(val) : null);
                    }
				}
				rowcount++;
			}
			if (rowcount != expectedRowCount) {
				throw new AssertionError("Unexpected row count " + rowcount + " != " + expectedRowCount);
			}
            LOGGER.info("Loaded records from historical DB data ({})", rowcount);
            Set<String> equipmentsNotFound = observationCount.entrySet().stream().filter(e -> e.getValue().get() == 0).map(Map.Entry::getKey).collect(Collectors.toSet());
            if (equipmentsNotFound.size() > 0) {
                LOGGER.warn("The following equipments ({}) either have not been found or are always disconnected in the historical DB: {}",
                        equipmentsNotFound.size(), equipmentsNotFound);
            }
		}
	}

	private Wp41HistoData parseData(DataMiningFacadeParams dmParams) throws IOException, InterruptedException {
		int rowCount = histoClient.queryCount(dmParams.getInterval(), HistoDbHorizon.SN);

        Set<HistoDbAttributeId> attributeIds = new LinkedHashSet<>((dmParams.getGensIds().size() +
                                                                    dmParams.getLoadsIds().size() +
                                                                    dmParams.getDanglingLinesIds().size()) * 2); // gens P, Q loads P, Q danglingLines P0, Q0
        for (String genId : dmParams.getGensIds()) {
            attributeIds.add(new HistoDbNetworkAttributeId(genId, HistoDbAttr.P));
            attributeIds.add(new HistoDbNetworkAttributeId(genId, HistoDbAttr.Q));
        }
        for (String loadId : dmParams.getLoadsIds()) {
            attributeIds.add(new HistoDbNetworkAttributeId(loadId, HistoDbAttr.P));
            attributeIds.add(new HistoDbNetworkAttributeId(loadId, HistoDbAttr.Q));
        }
        for (String dlId : dmParams.getDanglingLinesIds()) {
            attributeIds.add(new HistoDbNetworkAttributeId(dlId, HistoDbAttr.P0));
            attributeIds.add(new HistoDbNetworkAttributeId(dlId, HistoDbAttr.Q0));
        }

        List<Integer> rowIndexes;
        try (IntStream intStream = IntStream.range(0, rowCount)) {
            rowIndexes = intStream.boxed().collect(Collectors.toList());
        }
        List<String> colIndexes = attributeIds.stream().map(Object::toString).collect(Collectors.toList());

        ArrayTable<Integer, String, Float> hdTable = ArrayTable.create(rowIndexes, colIndexes);

        // parse csv generators
        try (InputStream is = histoClient.queryCsv(HistoQueryType.data, attributeIds, dmParams.getInterval(), HistoDbHorizon.SN, false, false)) {
            parseCsv(is, attributeIds, hdTable, rowCount);
        }

        return new Wp41HistoData(dmParams.getGensIds(), dmParams.getLoadsIds(), dmParams.getDanglingLinesIds(), hdTable);
	}

}
