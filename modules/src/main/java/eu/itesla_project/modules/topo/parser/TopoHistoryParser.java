/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.HistoDbAttributeIdParser;
import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopoHistoryParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopoHistoryParser.class);

    private static final JsonFactory FACTORY = new JsonFactory();

    private static final String FICT_PATTERN = "fict";

    private static Set<TopoBus> parseTopoJson(String json, String substationId) throws IOException {
        JsonParser parser = FACTORY.createParser(json);

        Set<TopoBus> topo = null;
        Set<String> bus = null;

        JsonToken t;
        while ((t = parser.nextToken()) != null) {
            switch (t) {
                case START_ARRAY:
                    if (topo == null) {
                        topo = new HashSet<>();
                    } else {
                        bus = new HashSet<>();
                    }
                    break;

                case END_ARRAY:
                    if (bus != null && bus.size() > 0) {
                        topo.add(new TopoBus(bus, substationId));
                        bus = null;
                    }
                    break;

                case VALUE_STRING:
                    String eqId = parser.getText();
                    if (!eqId.contains(FICT_PATTERN)) {
                        bus.add(parser.getText());
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        }

        return topo;
    }

    public void parse(int rowCount, InputStream is, TopoHistoryHandler handler) throws IOException {
        LOGGER.info("Parsing topo csv...");
        long start = System.currentTimeMillis();

        CsvPreference prefs = new CsvPreference.Builder('"', ',', "\r\n").build();
        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(is), prefs)) {
            Map<Integer, Integer> colMap = new HashMap<>();
            String[] header = reader.getHeader(true);
            List<String> substationIds = new ArrayList<>();
            for (int col = 0; col < header.length; col++) {
                String cell = header[col];
                HistoDbAttributeId attributeId = HistoDbAttributeIdParser.parse(cell);
                if (attributeId instanceof HistoDbNetworkAttributeId) {
                    colMap.put(col, substationIds.size());
                    substationIds.add(((HistoDbNetworkAttributeId) attributeId).getEquipmentId());
                }
            }
            handler.onHeader(substationIds, rowCount);
            int row = 0;
            List<String> line;
            while( (line = reader.read()) != null ) {
                for (int col = 0; col < line.size(); col++) {
                    Integer newCol = colMap.get(col);
                    if (newCol != null) {
                        String substationId = substationIds.get(newCol);
                        String cell = line.get(col);
                        Set<TopoBus> topo = null;
                        if (cell != null && cell.trim().length() > 0) {
                            topo = parseTopoJson(cell, substationId);
                        }
                        handler.onTopology(row, newCol, topo);
                    }
                }
                row++;
            }
        }
        LOGGER.info("Parsing done in {} ms", (System.currentTimeMillis() - start));
    }

}