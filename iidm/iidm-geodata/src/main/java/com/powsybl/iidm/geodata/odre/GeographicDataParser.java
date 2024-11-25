/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.powsybl.iidm.geodata.elements.GeoShape;
import com.powsybl.iidm.geodata.utils.DistanceCalculator;
import com.powsybl.iidm.geodata.utils.GeoShapeDeserializer;
import com.powsybl.iidm.geodata.utils.LineGraph;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.StopWatch;
import org.jgrapht.Graph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.min;
import static java.util.Collections.reverse;

/**
 * @author Chamseddine Benhamed {@literal <chamseddine.benhamed at rte-france.com>}
 */
public final class GeographicDataParser {

    private GeographicDataParser() {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GeographicDataParser.class);
    private static final int THRESHOLD = 5;

    /**
     * Parse the substations CSV data contained in the given reader, using the given odreConfig for column names.
     * @return the map of substation coordinates indexed by the substation id
     */
    public static Map<String, Coordinate> parseSubstations(Reader reader, OdreConfig odreConfig) {
        Map<String, Coordinate> substations = new LinkedHashMap<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            CSVParser records = CSVParser.parse(reader, FileValidator.CSV_FORMAT);
            if (FileValidator.validateSubstationsHeaders(records, odreConfig)) {
                for (CSVRecord row : records) {
                    String id = row.get(odreConfig.substationIdColumn());
                    substations.computeIfAbsent(id, key -> {
                        double lon = Double.parseDouble(row.get(odreConfig.substationLongitudeColumn()));
                        double lat = Double.parseDouble(row.get(odreConfig.substationLatitudeColumn()));
                        return new Coordinate(lat, lon);
                    });
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("{} substations read  in {} ms", substations.size(), stopWatch.getTime());
        return substations;
    }

    /**
     * Parse the lines CSV data contained in the given readers, using the given odreConfig for column names and line types.
     * @param aerialLinesReader the reader containing the aerial lines CSV data
     * @param undergroundLinesReader the reader containing the underground lines CSV data
     * @param odreConfig the config used for column names and line types
     * @return the map of line coordinates indexed by the line id
     */
    public static Map<String, List<Coordinate>> parseLines(Reader aerialLinesReader, Reader undergroundLinesReader, OdreConfig odreConfig) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Map<String, List<List<Coordinate>>> coordinatesListsByLine = new HashMap<>();

            CSVParser aerialLinesRecords = CSVParser.parse(aerialLinesReader, FileValidator.CSV_FORMAT);
            CSVParser undergroundLinesRecords = CSVParser.parse(undergroundLinesReader, FileValidator.CSV_FORMAT);

            if (!FileValidator.validateAerialLinesHeaders(aerialLinesRecords, odreConfig)
                    || !FileValidator.validateUndergroundHeaders(undergroundLinesRecords, odreConfig)) {
                return Collections.emptyMap();
            }

            parseLine(coordinatesListsByLine, aerialLinesRecords, odreConfig);
            parseLine(coordinatesListsByLine, undergroundLinesRecords, odreConfig);

            Map<String, List<Coordinate>> lines = fixLines(coordinatesListsByLine);

            LOGGER.info("{} lines read in {} ms", lines.size(), stopWatch.getTime());

            if (coordinatesListsByLine.size() != lines.size()) {
                LOGGER.warn("Total discarded lines : {}/{} ",
                        coordinatesListsByLine.size() - lines.size(), coordinatesListsByLine.size());
            }

            return lines;

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * "Fixing" the lines coordinates data: this method tries to calculate a single list when there are several lists for
     * a given line, which often occurs in the ODRE data.
     * @param coordinatesListsByLine the map of all the lists of coordinates, indexed by the line id
     * @return the map of the "fixed" line coordinates, indexed by the line id
     */
    private static Map<String, List<Coordinate>> fixLines(Map<String, List<List<Coordinate>>> coordinatesListsByLine) {
        Map<String, List<Coordinate>> lines = new HashMap<>();

        int linesWithOneConnectedSet = 0;
        int linesWithTwoOrMoreConnectedSets = 0;

        int oneConnectedSetDiscarded = 0;
        int twoOrMoreConnectedSetsDiscarded = 0;

        for (Map.Entry<String, List<List<Coordinate>>> e : coordinatesListsByLine.entrySet()) {
            String lineId = e.getKey();

            List<List<Coordinate>> coordinatesLists = e.getValue();
            if (coordinatesLists.size() == 1) {
                // Easy case: only one list
                lines.put(lineId, coordinatesLists.get(0));
            } else {
                // We want to calculate a single list: we construct a graph based on the coordinates, adding a vertex
                // for each coordinate, and adding an edge between two consecutive coordinates
                LineGraph<Coordinate, Object> graph = new LineGraph<>(Object.class);
                coordinatesLists.forEach(graph::addVerticesAndEdges);
                List<ConnectedSet> connectedSets = getConnectedSets(graph);
                if (connectedSets.size() == 1) {
                    linesWithOneConnectedSet++;
                    ConnectedSet connectedSet = connectedSets.get(0);
                    if (connectedSet.ends().size() == 2) {
                        // Only one connected set and two ends: this is a single line
                        lines.put(lineId, connectedSet.list());
                    } else {
                        // Only one connected set and more than two ends: there are forks in the lines
                        oneConnectedSetDiscarded++;
                    }
                } else {

                    // there are several connected sets: we try to order them to have a single line
                    linesWithTwoOrMoreConnectedSets++;
                    List<List<Coordinate>> coordinatesComponents = createMultipleConnectedSetsCoordinatesList(connectedSets);

                    if (coordinatesComponents.size() != connectedSets.size() || coordinatesComponents.size() > 2) {
                        // This happens if there is a fork in one of the connected components or if there are more than 2 connected components
                        twoOrMoreConnectedSetsDiscarded++;
                        continue;
                    }

                    lines.put(lineId, aggregateCoordinates(coordinatesComponents));
                }
            }
        }
        LOGGER.info("{} lines have one Connected set, {} of them were discarded", linesWithOneConnectedSet, oneConnectedSetDiscarded);
        LOGGER.info("{} lines have two or more Connected sets, {} of them were discarded", linesWithTwoOrMoreConnectedSets, twoOrMoreConnectedSetsDiscarded);
        return lines;
    }

    /**
     * Compute the connected sets for the given graph. The corresponding list of coordinates and list of extremities
     * for each connected component is computed at the same time.
     */
    private static List<ConnectedSet> getConnectedSets(Graph<Coordinate, Object> graph) {
        List<ConnectedSet> connectedSets = new ArrayList<>();
        Set<Coordinate> vertexSet = graph.vertexSet();

        Optional<Coordinate> endCoord = vertexSet.stream().filter(v -> graph.degreeOf(v) == 1).findFirst();
        if (endCoord.isPresent()) {
            var bfi = new BreadthFirstIterator<Coordinate, Object>(graph, () -> Stream.concat(Stream.of(endCoord.get()), vertexSet.stream()).iterator());
            bfi.addTraversalListener(new GeoTraversalListener(graph, connectedSets));
            while (bfi.hasNext()) {
                bfi.next();
            }
        } else {
            connectedSets = List.of(new ConnectedSet(vertexSet.stream().toList(), List.of()));
        }
        return connectedSets;
    }

    /**
     * Parsing the CSV lines data which is given by the CSVParser, using the given odreConfig for column names and line types.
     * The resulting coordinates are put in the given map.
     */
    private static void parseLine(Map<String, List<List<Coordinate>>> coordinateListsByLine, CSVParser csvParser, OdreConfig odreConfig) throws JsonProcessingException {
        Map<String, String> idsColumnNames = odreConfig.idsColumnNames();
        for (CSVRecord row : csvParser) {
            List<String> ids = Stream.of(row.get(idsColumnNames.get(OdreConfig.LINE_ID_KEY_1)),
                    row.get(idsColumnNames.get(OdreConfig.LINE_ID_KEY_2)),
                    row.get(idsColumnNames.get(OdreConfig.LINE_ID_KEY_3)),
                    row.get(idsColumnNames.get(OdreConfig.LINE_ID_KEY_4)),
                    row.get(idsColumnNames.get(OdreConfig.LINE_ID_KEY_5))).filter(Objects::nonNull).filter(s -> !s.isEmpty()).distinct().toList();
            GeoShape geoShape = GeoShapeDeserializer.read(row.get(odreConfig.geoShapeColumn()));

            if (ids.isEmpty() || geoShape.coordinates().isEmpty()) {
                continue;
            }

            for (String lineId : ids) {
                coordinateListsByLine.computeIfAbsent(lineId, key -> new ArrayList<>())
                        .add(geoShape.coordinates());
            }
        }
    }

    /**
     * Calculate the distance between the first and the last coordinates of the given list
     */
    private static double getBranchLength(List<Coordinate> coordinatesComponent) {
        return DistanceCalculator.distance(coordinatesComponent.get(0), coordinatesComponent.get(coordinatesComponent.size() - 1));
    }

    /**
     * Aggregate coordinates of two connected components into one single list by trying to find which extremity connects to which other extremity
     */
    private static List<Coordinate> aggregateCoordinates(List<List<Coordinate>> coordinatesComponents) {
        List<Coordinate> aggregatedCoordinates;

        List<Coordinate> coordinatesComponent1 = coordinatesComponents.get(0);
        List<Coordinate> coordinatesComponent2 = coordinatesComponents.get(1);

        double l1 = getBranchLength(coordinatesComponent1);
        double l2 = getBranchLength(coordinatesComponent2);

        if (100 * Math.min(l1, l2) / Math.max(l1, l2) < THRESHOLD) {
            return l1 > l2 ? coordinatesComponent1 : coordinatesComponent2;
        }

        double d1 = DistanceCalculator.distance(coordinatesComponent1.get(0), coordinatesComponent2.get(coordinatesComponent2.size() - 1));
        double d2 = DistanceCalculator.distance(coordinatesComponent1.get(0), coordinatesComponent2.get(0));
        double d3 = DistanceCalculator.distance(coordinatesComponent1.get(coordinatesComponent1.size() - 1), coordinatesComponent2.get(coordinatesComponent2.size() - 1));
        double d4 = DistanceCalculator.distance(coordinatesComponent1.get(coordinatesComponent1.size() - 1), coordinatesComponent2.get(0));

        List<Double> distances = Arrays.asList(d1, d2, d3, d4);
        double min = min(distances);

        if (d1 == min) {
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent2);
            aggregatedCoordinates.addAll(coordinatesComponent1);

        } else if (d2 == min) {
            reverse(coordinatesComponent1);
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent1);
            aggregatedCoordinates.addAll(coordinatesComponent2);

        } else if (d3 == min) {
            reverse(coordinatesComponent2);
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent1);
            aggregatedCoordinates.addAll(coordinatesComponent2);
        } else {
            aggregatedCoordinates = new ArrayList<>(coordinatesComponent1);
            aggregatedCoordinates.addAll(coordinatesComponent2);
        }
        return aggregatedCoordinates;
    }

    /**
     * Constructing the list of coordinates of each connected component, filtering out the connected sets which have more than two ends (or zero)
     */
    private static List<List<Coordinate>> createMultipleConnectedSetsCoordinatesList(List<ConnectedSet> connectedSets) {
        List<List<Coordinate>> coordinatesComponents = new ArrayList<>();
        for (ConnectedSet connectedSet : connectedSets) {
            List<Coordinate> endsComponent = connectedSet.ends();
            if (endsComponent.size() == 2) {
                coordinatesComponents.add(connectedSet.list());
            } else {
                break;
            }
        }
        return coordinatesComponents;
    }

    private record ConnectedSet(List<Coordinate> list, List<Coordinate> ends) {
    }

    private static class GeoTraversalListener extends TraversalListenerAdapter<Coordinate, Object> {
        private final List<ConnectedSet> connectedSets;
        private final Graph<Coordinate, Object> graph;
        private List<Coordinate> currentConnectedSet;
        private List<Coordinate> currentConnectedSetEnds;

        public GeoTraversalListener(Graph<Coordinate, Object> graph, List<ConnectedSet> connectedSets) {
            this.graph = graph;
            this.connectedSets = connectedSets;
        }

        @Override
        public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
            connectedSets.add(new ConnectedSet(currentConnectedSet, currentConnectedSetEnds));
        }

        @Override
        public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
            currentConnectedSet = new ArrayList<>();
            currentConnectedSetEnds = new ArrayList<>();
        }

        @Override
        public void vertexTraversed(VertexTraversalEvent<Coordinate> e) {
            Coordinate v = e.getVertex();
            currentConnectedSet.add(v);
            if (graph.degreeOf(v) == 1) {
                currentConnectedSetEnds.add(v);
            }
        }
    }
}
