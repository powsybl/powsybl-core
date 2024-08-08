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
import com.powsybl.iidm.geodata.elements.LineGeoData;
import com.powsybl.iidm.geodata.elements.SubstationGeoData;
import com.powsybl.iidm.geodata.utils.DistanceCalculator;
import com.powsybl.iidm.geodata.utils.GeoShapeDeserializer;
import com.powsybl.iidm.geodata.utils.LineGraph;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
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

    public static Map<String, SubstationGeoData> parseSubstations(Reader reader, OdreConfig odreConfig) {
        Map<String, SubstationGeoData> substations = new HashMap<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int substationCount = 0;
        try {
            CSVParser records = CSVParser.parse(reader, FileValidator.CSV_FORMAT);
            if (FileValidator.validateSubstationsHeaders(records, odreConfig)) {
                for (CSVRecord row : records) {
                    String id = row.get(odreConfig.substationIdColumn());
                    double lon = Double.parseDouble(row.get(odreConfig.substationLongitudeColumn()));
                    double lat = Double.parseDouble(row.get(odreConfig.substationLatitudeColumn()));
                    SubstationGeoData substation = substations.get(id);
                    if (substation == null) {
                        SubstationGeoData substationGeoData = new SubstationGeoData(id, FileValidator.COUNTRY_FR, new Coordinate(lat, lon));
                        substations.put(id, substationGeoData);
                    }
                    substationCount++;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("{} substations read  in {} ms", substationCount, stopWatch.getTime());
        return substations;
    }

    private static double distanceCoordinate(Coordinate coord1, Coordinate coord2) {
        return DistanceCalculator.distance(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude());
    }

    public static Pair<String, String> substationOrder(Map<String, SubstationGeoData> substationGeoData, String lineId, List<Coordinate> coordinates) {
        String substation1 = lineId.substring(0, 5).trim();
        String substation2 = lineId.substring(8).trim();
        SubstationGeoData geo1 = substationGeoData.get(substation1);
        SubstationGeoData geo2 = substationGeoData.get(substation2);

        if (geo1 == null && geo2 == null) {
            LOGGER.warn("can't find any substation for {}", lineId);
            return Pair.of("", "");
        } else if (geo1 != null && geo2 != null) {
            return findStartAndEndSubstationsOfLine(lineId, geo1, geo2, substation1, substation2,
                    coordinates.get(0), coordinates.get(coordinates.size() - 1));
        } else {
            boolean isStart = distanceCoordinate((geo1 != null ? geo1 : geo2).getCoordinate(), coordinates.get(0)) < distanceCoordinate((geo1 != null ? geo1 : geo2).getCoordinate(), coordinates.get(coordinates.size() - 1));
            String substation = geo1 != null ? substation1 : substation2;
            return isStart ? Pair.of(substation, "") : Pair.of("", substation);
        }
    }

    public static Map<String, LineGeoData> parseLines(Reader aerialLinesReader, Reader undergroundLinesReader,
                                                      Map<String, SubstationGeoData> stringSubstationGeoDataMap, OdreConfig odreConfig) {
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

            Map<String, LineGeoData> lines = fixLines(coordinatesListsByLine, stringSubstationGeoDataMap);

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

    private static Map<String, LineGeoData> fixLines(Map<String, List<List<Coordinate>>> coordinatesListsByLine, Map<String, SubstationGeoData> stringSubstationGeoDataMap) {
        Map<String, LineGeoData> lines = new HashMap<>();

        int linesWithOneConnectedSet = 0;
        int linesWithTwoOrMoreConnectedSets = 0;

        int oneConnectedSetDiscarded = 0;
        int twoOrMoreConnectedSetsDiscarded = 0;

        for (Map.Entry<String, List<List<Coordinate>>> e : coordinatesListsByLine.entrySet()) {
            String lineId = e.getKey();

            List<List<Coordinate>> coordinatesLists = e.getValue();
            if (coordinatesLists.size() == 1) {
                List<Coordinate> coordinates = coordinatesLists.get(0);
                Pair<String, String> substations = substationOrder(stringSubstationGeoDataMap, lineId, coordinates);
                LineGeoData line = new LineGeoData(lineId, FileValidator.COUNTRY_FR, FileValidator.COUNTRY_FR, substations.getLeft(), substations.getRight(), coordinates);
                lines.put(lineId, line);
            } else {
                LineGraph<Coordinate, Object> graph = new LineGraph<>(Object.class);
                coordinatesLists.forEach(graph::addVerticesAndEdges);
                List<ConnectedSet> connectedSets = getConnectedSets(graph);
                if (connectedSets.size() == 1) {
                    linesWithOneConnectedSet++;
                    ConnectedSet connectedSet = connectedSets.get(0);
                    if (connectedSet.ends().size() == 2) {
                        List<Coordinate> coordinates = connectedSet.list();
                        Pair<String, String> substations = substationOrder(stringSubstationGeoDataMap, lineId, coordinates);
                        LineGeoData line = new LineGeoData(lineId, FileValidator.COUNTRY_FR, FileValidator.COUNTRY_FR, substations.getLeft(), substations.getRight(), coordinates);
                        lines.put(lineId, line);
                    } else {
                        oneConnectedSetDiscarded++;
                    }
                } else {
                    linesWithTwoOrMoreConnectedSets++;
                    List<List<Coordinate>> coordinatesComponents = fillMultipleConnectedSetsCoordinatesList(connectedSets);

                    if (coordinatesComponents.size() != connectedSets.size()) {
                        twoOrMoreConnectedSetsDiscarded++;
                        continue;
                    }

                    List<Coordinate> aggregatedCoordinates = aggregateCoordinates(coordinatesComponents);
                    Pair<String, String> substations = substationOrder(stringSubstationGeoDataMap, lineId, aggregatedCoordinates);
                    LineGeoData line = new LineGeoData(lineId, FileValidator.COUNTRY_FR, FileValidator.COUNTRY_FR, substations.getLeft(), substations.getRight(), aggregatedCoordinates);
                    lines.put(lineId, line);
                }
            }
        }
        LOGGER.info("{} lines have one Connected set, {} of them were discarded", linesWithOneConnectedSet, oneConnectedSetDiscarded);
        LOGGER.info("{} lines have two or more Connected sets, {} of them were discarded", linesWithTwoOrMoreConnectedSets, twoOrMoreConnectedSetsDiscarded);
        return lines;
    }

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

    private static double getBranchLength(List<Coordinate> coordinatesComponent) {
        return distanceCoordinate(coordinatesComponent.get(0), coordinatesComponent.get(coordinatesComponent.size() - 1));
    }

    private static List<Coordinate> aggregateCoordinates(List<List<Coordinate>> coordinatesComponents) {
        List<Coordinate> aggregatedCoordinates;

        List<Coordinate> coordinatesComponent1 = coordinatesComponents.get(0);
        List<Coordinate> coordinatesComponent2 = coordinatesComponents.get(1);

        double l1 = getBranchLength(coordinatesComponent1);
        double l2 = getBranchLength(coordinatesComponent2);

        if (100 * Math.min(l1, l2) / Math.max(l1, l2) < THRESHOLD) {
            return l1 > l2 ? coordinatesComponent1 : coordinatesComponent2;
        }

        double d1 = distanceCoordinate(coordinatesComponent1.get(0), coordinatesComponent2.get(coordinatesComponent2.size() - 1));
        double d2 = distanceCoordinate(coordinatesComponent1.get(0), coordinatesComponent2.get(0));
        double d3 = distanceCoordinate(coordinatesComponent1.get(coordinatesComponent1.size() - 1), coordinatesComponent2.get(coordinatesComponent2.size() - 1));
        double d4 = distanceCoordinate(coordinatesComponent1.get(coordinatesComponent1.size() - 1), coordinatesComponent2.get(0));

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

    private static Pair<String, String> findStartAndEndSubstationsOfLine(String lineId, SubstationGeoData geo1, SubstationGeoData geo2,
                                                                         String substation1, String substation2,
                                                                         Coordinate firstCoordinate, Coordinate lastCoordinate) {
        final double sub1pil1 = distanceCoordinate(geo1.getCoordinate(), firstCoordinate);
        final double sub2pil1 = distanceCoordinate(geo2.getCoordinate(), firstCoordinate);
        final double sub1pil2 = distanceCoordinate(geo1.getCoordinate(), lastCoordinate);
        final double sub2pil2 = distanceCoordinate(geo2.getCoordinate(), lastCoordinate);
        if ((sub1pil1 < sub2pil1) == (sub1pil2 < sub2pil2)) {
            LOGGER.error("line {} for substations {} and {} has both first and last coordinate nearest to {}", lineId, substation1, substation2, sub1pil1 < sub2pil1 ? substation1 : substation2);
            return Pair.of("", "");
        }
        return Pair.of(sub1pil1 < sub2pil1 ? substation1 : substation2, sub1pil1 < sub2pil1 ? substation2 : substation1);
    }

    private static List<List<Coordinate>> fillMultipleConnectedSetsCoordinatesList(List<ConnectedSet> connectedSets) {
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
