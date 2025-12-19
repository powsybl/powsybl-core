/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.iidm.geodata.geojson.dto.GeometryDto;
import com.powsybl.iidm.geodata.geojson.dto.LineStringDto;
import com.powsybl.iidm.geodata.geojson.dto.PointDto;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class GeoJsonDataParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonDataParser.class);
    private static final ThreadLocal<double[]> POINT_BUFFER = ThreadLocal.withInitial(() -> new double[2]);
    private static final JsonFactory JSON_FACTORY = new JsonFactoryBuilder()
        .configure(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES, false)
        .configure(JsonFactory.Feature.INTERN_FIELD_NAMES, false)
        .build();

    private GeoJsonDataParser() {
    }

    /**
     * Parses GeoJSON data from the provided reader and extracts substation data, representing them
     * as a map where the keys are substation identifiers and the values are their geographical coordinates.
     *
     * @param reader the reader containing the GeoJSON data to parse
     * @throws IOException if an I/O error occurs while reading the GeoJSON data
     */
    public static void parseSubstations(Reader reader, BiConsumer<String, Coordinate> substationConsumer) throws IOException {
        parseFeatures(reader, (id, geometry) -> {
            if (geometry instanceof PointDto pointDto) {
                substationConsumer.accept(id, pointDto.getCoordinate());
            } else {
                logUnexpectedFeature(geometry);
            }
        });
    }

    /**
     * Parses GeoJSON data from the provided reader and extracts line data, representing them
     * as a map where the keys are line identifiers and the values are lists of coordinates
     * describing the line geometry.
     *
     * @param reader the reader containing the GeoJSON data to parse
     * @throws IOException if an I/O error occurs during parsing
     */
    public static void parseLines(Reader reader, BiConsumer<String, List<Coordinate>> lineConsumer) throws IOException {
        parseFeatures(reader, (id, geometry) -> {
            if (geometry instanceof LineStringDto lineDto) {
                lineConsumer.accept(id, lineDto.getCoordinates());
            } else {
                logUnexpectedFeature(geometry);
            }
        });
    }

    private static void parseFeatures(Reader reader, FeatureProcessor featureHandler) throws IOException {
        int count = 0;
        long start = System.nanoTime();

        try (JsonParser parser = JSON_FACTORY.createParser(reader)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected start of GeoJSON object");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                if ("features".equals(fieldName)) {
                    parser.nextToken(); // move to START_ARRAY

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        if (parseFeature(parser, featureHandler)) {
                            count++;
                        }
                    }
                } else {
                    parser.nextToken();
                    parser.skipChildren();
                }
            }
        }
        POINT_BUFFER.remove();

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        LOGGER.info("{} features processed in {} ms", count, durationMs);
    }

    private static boolean parseFeature(JsonParser parser, FeatureProcessor featureHandler) throws IOException {
        // Initialize variables
        List<Coordinate> coordinates = null;
        String id = null;
        Coordinate point = null;

        // Parse one feature
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String featureField = parser.currentName();
            parser.nextToken(); // move to value

            switch (featureField) {
                case "properties" -> id = parseId(parser);
                case "geometry" -> {
                    GeometryParseResult parseResult = parseGeometry(parser);
                    point = parseResult.point();
                    coordinates = parseResult.coordinates();
                }
                default -> parser.skipChildren();
            }
        }

        if (id == null) {
            return false;
        }
        if (point != null) {
            featureHandler.process(id, new PointDto(point));
            return true;
        } else if (coordinates != null && !coordinates.isEmpty()) {
            featureHandler.process(id, new LineStringDto(coordinates));
            return true;
        }
        return false;
    }

    private static String parseId(JsonParser parser) throws IOException {
        String id = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String propName = parser.currentName();
            parser.nextToken();
            if ("IDR".equals(propName)) {
                id = parser.getText();
            } else {
                parser.skipChildren();
            }
        }
        return id;
    }

    private static GeometryParseResult parseGeometry(JsonParser parser) throws IOException {
        String geometryType = null;
        List<Coordinate> coordinates = null;
        Coordinate point = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String geomField = parser.currentName();
            parser.nextToken();

            if ("type".equals(geomField)) {
                geometryType = parser.getText();
            } else if ("coordinates".equals(geomField)) {
                switch (geometryType) {
                    case "Point" -> point = readPoint(parser);
                    case "LineString" -> coordinates = readLineString(parser);
                    case "MultiLineString" -> coordinates = readMultiLineString(parser);
                    case null, default -> parser.skipChildren();
                }
            } else {
                parser.skipChildren();
            }
        }
        return new GeometryParseResult(point, coordinates);
    }

    private static List<Coordinate> readMultiLineString(JsonParser parser) throws IOException {
        List<Coordinate> list = new ArrayList<>(32);
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Expected START_ARRAY for MultiLineString");
        }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            readLines(parser, list);
        }
        return list;
    }

    private static List<Coordinate> readLineString(JsonParser parser) throws IOException {
        List<Coordinate> list = new ArrayList<>();
        readLines(parser, list);
        return list;
    }

    private static void readLines(JsonParser parser, List<Coordinate> list) throws IOException {
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Expected START_ARRAY for LineString");
        }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readPoint(parser));
        }
    }

    private static Coordinate readPoint(JsonParser parser) throws IOException {
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Expected START_ARRAY for point");
        }
        double[] buf = POINT_BUFFER.get();
        // First value
        parser.nextToken();
        buf[0] = parser.getDoubleValue();
        // Second value
        parser.nextToken();
        buf[1] = parser.getDoubleValue();
        consumeExtraData(parser);
        return new Coordinate(buf[1], buf[0]);
    }

    private static void consumeExtraData(JsonParser parser) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // Just skip remaining coordinates or nested arrays
            parser.skipChildren();
        }
    }

    private static void logUnexpectedFeature(GeometryDto geometryDto) {
        LOGGER.warn("Unexpected feature type: {} - feature: {}", geometryDto.getClass().getSimpleName(), geometryDto);
    }

    @FunctionalInterface
    private interface FeatureProcessor {
        void process(String id, GeometryDto feature);
    }

    private record GeometryParseResult(Coordinate point, List<Coordinate> coordinates) {
    }
}
