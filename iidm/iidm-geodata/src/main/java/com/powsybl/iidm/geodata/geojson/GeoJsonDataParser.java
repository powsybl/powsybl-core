package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.network.extensions.Coordinate;
import org.apache.commons.lang3.time.StopWatch;
import org.geotools.api.feature.Feature;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class GeoJsonDataParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonDataParser.class);

    private GeoJsonDataParser() {
    }

    /**
     * Parses GeoJSON data from the provided reader and extracts substation data, representing them
     * as a map where the keys are substation identifiers and the values are their geographical coordinates.
     *
     * @param reader the reader containing the GeoJSON data to parse
     * @return a map where the keys are substation identifiers (as strings) and the values are their
     *         corresponding geographical coordinates
     * @throws UncheckedIOException if an I/O error occurs while reading the GeoJSON data
     */
    public static Map<String, Coordinate> parseSubstations(Reader reader) {
        return parseFeatures(reader, simpleFeature -> {
            Point point = (Point) simpleFeature.getDefaultGeometry();
            return new Coordinate(point.getY(), point.getX());
        });
    }

    /**
     * Parses GeoJSON data from the provided reader and extracts line data, representing them
     * as a map where the keys are line identifiers and the values are lists of coordinates
     * describing the line geometry.
     *
     * @param reader the reader containing the GeoJSON data to parse
     * @return a map where the keys are line IDs and the values are lists of coordinates
     *         describing the geometry of the lines
     * @throws UncheckedIOException if an I/O error occurs during parsing
     */
    public static Map<String, List<Coordinate>> parseLines(Reader reader) {
        return parseFeatures(reader, GeoJsonDataParser::getCoordinates);
    }

    private static <T> Map<String, T> parseFeatures(Reader reader, FeatureProcessor<T> featureProcessor) {
        Map<String, T> result = new LinkedHashMap<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            FeatureJSON fjson = new FeatureJSON();
            FeatureCollection<?, ?> featureCollection = fjson.readFeatureCollection(reader);
            try (FeatureIterator<?> features = featureCollection.features()) {
                while (features.hasNext()) {
                    Feature feature = features.next();
                    if (feature instanceof SimpleFeature simpleFeature) {
                        String id = simpleFeature.getAttribute("IDR").toString();
                        result.computeIfAbsent(id, key -> featureProcessor.process(simpleFeature));
                    } else {
                        logUnexpectedFeature(feature);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LOGGER.info("{} features read in {} ms", result.size(), stopWatch.getDuration().toMillis());
        return result;
    }

    private static void logUnexpectedFeature(Feature feature) {
        LOGGER.warn("Unexpected feature type: {} - feature: {}", feature.getClass().getSimpleName(), feature);
    }

    private static List<Coordinate> getCoordinates(SimpleFeature simpleFeature) {
        Object geometry = simpleFeature.getDefaultGeometry();
        List<Coordinate> coordinatesList = null;
        if (geometry instanceof LineString lineString) {
            // LineString seems to be read as MultiLineString, but let's be careful and cover the case
            coordinatesList = getCoordinatesFromLineString(lineString);
        } else if (geometry instanceof MultiLineString multiLineString) {
            coordinatesList = getCoordinatesFromMultiLineString(multiLineString);
        } else {
            String id = simpleFeature.getAttribute("IDR").toString();
            LOGGER.warn("Unsupported geometry type: {} for feature {}", geometry.getClass().getSimpleName(), id);
        }
        return coordinatesList;
    }

    private static List<Coordinate> getCoordinatesFromMultiLineString(MultiLineString multiLineString) {
        List<Coordinate> coordinatesList = new ArrayList<>();
        int numGeometries = multiLineString.getNumGeometries();
        for (int i = 0; i < numGeometries; i++) {
            LineString lineString = (LineString) multiLineString.getGeometryN(i);
            coordinatesList.addAll(getCoordinatesFromLineString(lineString));
        }
        return coordinatesList;
    }

    private static List<Coordinate> getCoordinatesFromLineString(LineString lineString) {
        List<Coordinate> coordinatesList = new ArrayList<>();
        int numPoints = lineString.getNumPoints();
        for (int i = 0; i < numPoints; i++) {
            coordinatesList.add(new Coordinate(lineString.getCoordinateN(i).getY(), lineString.getCoordinateN(i).getX()));
        }
        return coordinatesList;
    }

    @FunctionalInterface
    private interface FeatureProcessor<T> {
        T process(SimpleFeature feature);
    }
}
