package com.powsybl.iidm.geodata.geojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.geodata.geojson.dto.AbstractGeometryDto;
import com.powsybl.iidm.geodata.geojson.dto.FeatureCollectionDto;
import com.powsybl.iidm.geodata.geojson.dto.LineStringDto;
import com.powsybl.iidm.geodata.geojson.dto.MultiLineStringDto;
import com.powsybl.iidm.geodata.geojson.dto.PointDto;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class GeoJsonDataParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonDataParser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    public static Map<String, Coordinate> parseSubstations(Reader reader) throws IOException {
        return parseFeatures(reader, (result, id, geometry) -> {
            if (!(geometry instanceof PointDto pointDto)) {
                logUnexpectedFeature(geometry);
            } else {
                result.put(id, pointDto.getCoordinate());
            }
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
    public static Map<String, List<Coordinate>> parseLines(Reader reader) throws IOException {
        return parseFeatures(reader, (result, id, geometry) -> {
            if (!(geometry instanceof LineStringDto || geometry instanceof MultiLineStringDto)) {
                logUnexpectedFeature(geometry);
            } else {
                if (geometry instanceof LineStringDto lineStringDto) {
                    result.put(id, lineStringDto.getCoordinateList());
                } else {
                    MultiLineStringDto multiLineStringDto = (MultiLineStringDto) geometry;
                    result.put(id, multiLineStringDto.getCoordinateList());
                }
            }
        });
    }

    private static <T> Map<String, T> parseFeatures(Reader reader, GeoJsonDataParser.FeatureProcessor<T> featureProcessor) throws IOException {
        FeatureCollectionDto featureCollectionDto = MAPPER.readValue(reader, FeatureCollectionDto.class);
        Map<String, T> result = new LinkedHashMap<>(featureCollectionDto.getFeatures().size());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        featureCollectionDto.getFeatures().forEach(featureDto -> {
            if (!featureDto.getProperties().containsKey("IDR")) {
                LOGGER.warn("Missing IDR property for feature {}", featureDto);
            } else {
                String id = featureDto.getProperties().get("IDR");
                AbstractGeometryDto geometry = featureDto.getGeometry();
                featureProcessor.process(result, id, geometry);
            }
        });
        LOGGER.info("{} features read in {} ms", result.size(), stopWatch.getDuration().toMillis());
        return result;
    }

    private static void logUnexpectedFeature(AbstractGeometryDto geometryDto) {
        LOGGER.warn("Unexpected feature type: {} - feature: {}", geometryDto.getClass().getSimpleName(), geometryDto);
    }

    @FunctionalInterface
    private interface FeatureProcessor<T> {
        void process(Map<String, T> result, String id, AbstractGeometryDto feature);
    }
}
