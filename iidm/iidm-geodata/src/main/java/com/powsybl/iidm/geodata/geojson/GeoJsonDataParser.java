package com.powsybl.iidm.geodata.geojson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.iidm.geodata.geojson.dto.AbstractGeometryDto;
import com.powsybl.iidm.geodata.geojson.dto.LineStringDto;
import com.powsybl.iidm.geodata.geojson.dto.PointDto;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class GeoJsonDataParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonDataParser.class);
    private static final ThreadLocal<double[]> POINT_BUFFER = ThreadLocal.withInitial(() -> new double[2]);

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
        AtomicLong count = new AtomicLong(0L);
        long start = System.nanoTime();
        JsonFactory factory = new JsonFactoryBuilder()
            .configure(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES, false)
            .configure(JsonFactory.Feature.INTERN_FIELD_NAMES, false)
            .build();
        AtomicReference<List<Coordinate>> coordinates = new AtomicReference<>();
        AtomicReference<Coordinate> point = new AtomicReference<>();
        AtomicReference<String> id = new AtomicReference<>();

        try (JsonParser parser = factory.createParser(reader)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected start of GeoJSON object");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                if ("features".equals(fieldName)) {
                    parser.nextToken(); // move to START_ARRAY

                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        parseFeature(parser, featureHandler, count, coordinates, point, id);
                    }
                } else {
                    parser.nextToken();
                    parser.skipChildren();
                }
            }
        }
        POINT_BUFFER.remove();

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        LOGGER.info("{} features processed in {} ms", count.get(), durationMs);
    }

    private static void parseFeature(JsonParser parser, FeatureProcessor featureHandler, AtomicLong count,
                                     AtomicReference<List<Coordinate>> coordinates,
                                     AtomicReference<Coordinate> point,
                                     AtomicReference<String> id) throws IOException {

        // Parse one feature
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String featureField = parser.currentName();
            parser.nextToken(); // move to value

            switch (featureField) {
                case "properties" -> parseId(parser, id);
                case "geometry" -> parseGeometry(parser, coordinates, point);
                default -> parser.skipChildren();
            }
        }

        if (id.get() != null) {
            if (point.get() != null) {
                featureHandler.process(id.get(), new PointDto(point.get()));
                point.set(null);
            } else if (coordinates.get() != null && !coordinates.get().isEmpty()) {
                featureHandler.process(id.get(), new LineStringDto(coordinates.get()));
                coordinates.get().clear();
            }
            count.getAndIncrement();
        }
        id.set(null);
    }

    private static void parseId(JsonParser parser, AtomicReference<String> id) throws IOException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String propName = parser.currentName();
            parser.nextToken();
            if ("IDR".equals(propName)) {
                id.set(parser.getText());
            } else {
                parser.skipChildren();
            }
        }
    }

    private static void parseGeometry(JsonParser parser,
                                      AtomicReference<List<Coordinate>> coordinates,
                                      AtomicReference<Coordinate> point) throws IOException {
        String geometryType = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String geomField = parser.currentName();
            parser.nextToken();

            if ("type".equals(geomField)) {
                geometryType = parser.getText();
            } else if ("coordinates".equals(geomField)) {
                switch (geometryType) {
                    case "Point" -> point.set(readPoint(parser));
                    case "LineString" -> coordinates.set(readLineString(parser));
                    case "MultiLineString" -> coordinates.set(readMultiLineString(parser));
                    case null, default -> parser.skipChildren();
                }
            } else {
                parser.skipChildren();
            }
        }
    }

    private static List<Coordinate> readMultiLineString(JsonParser parser) throws IOException {
        List<Coordinate> list = new ArrayList<>();
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Expected START_ARRAY for MultiLineString");
        }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.addAll(readLineString(parser));
        }
        return list;
    }

    private static List<Coordinate> readLineString(JsonParser parser) throws IOException {
        List<Coordinate> list = new ArrayList<>();
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("Expected START_ARRAY for LineString");
        }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            list.add(readPoint(parser));
        }
        return list;
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
        if (parser.nextToken() != JsonToken.END_ARRAY) {
            consumeExtraData(parser);
        }
    }

    private static void logUnexpectedFeature(AbstractGeometryDto geometryDto) {
        LOGGER.warn("Unexpected feature type: {} - feature: {}", geometryDto.getClass().getSimpleName(), geometryDto);
    }

    @FunctionalInterface
    private interface FeatureProcessor {
        void process(String id, AbstractGeometryDto feature);
    }
}
