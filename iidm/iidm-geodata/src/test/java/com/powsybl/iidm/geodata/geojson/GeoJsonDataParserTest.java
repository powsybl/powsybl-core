/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.geodata.geojson.dto.CoordinatesDto;
import com.powsybl.iidm.geodata.geojson.dto.FeatureCollectionDto;
import com.powsybl.iidm.geodata.geojson.dto.FeatureDto;
import com.powsybl.iidm.geodata.geojson.dto.LineStringDto;
import com.powsybl.iidm.geodata.geojson.dto.MultiLineStringDto;
import com.powsybl.iidm.geodata.geojson.dto.PointDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GeoJsonDataParserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void coordinatesSerdeTest() throws JsonProcessingException {
        // Coordinates
        CoordinatesDto coordinates = new CoordinatesDto(1, 2, 3);
        String json = MAPPER.writeValueAsString(coordinates);
        assertEquals(coordinates, MAPPER.readValue(json, CoordinatesDto.class));

        // Another constructor
        CoordinatesDto otherCoordinates = new CoordinatesDto();
        otherCoordinates.setLongitude(1);
        otherCoordinates.setLatitude(2);
        assertNotEquals(coordinates, otherCoordinates);
        assertEquals(0, otherCoordinates.getAltitude());
        otherCoordinates.setAltitude(3);
        assertEquals(coordinates, otherCoordinates);

        // Attributes
        assertEquals(1, coordinates.getLongitude());
        assertEquals(2, coordinates.getLatitude());
        assertEquals(3, coordinates.getAltitude());
    }

    @Test
    void pointTest() throws JsonProcessingException {
        // Point
        PointDto point = new PointDto(1, 2, 3);
        String json = MAPPER.writeValueAsString(point);
        assertEquals(point, MAPPER.readValue(json, PointDto.class));

        // Other constructors
        PointDto otherPoint = new PointDto(1, 2);
        assertNotEquals(point, otherPoint);
        otherPoint.setCoordinates(new CoordinatesDto(1, 2, 3));
        assertEquals(point, otherPoint);
        PointDto anotherPoint = new PointDto(new CoordinatesDto(1, 2, 3));
        assertEquals(point, anotherPoint);
    }

    @Test
    void lineStringTest() throws JsonProcessingException {
        // LineString
        LineStringDto lineString = new LineStringDto(List.of(new CoordinatesDto(1, 2),
            new CoordinatesDto(3, 4),
            new CoordinatesDto(5, 6)));
        String json = MAPPER.writeValueAsString(lineString);
        assertEquals(lineString, MAPPER.readValue(json, LineStringDto.class));

        // Another constructor
        double[][] expectedCoordinates = {{1, 2}, {3, 4}, {5, 6}};
        LineStringDto anotherLineString = new LineStringDto(expectedCoordinates);
        assertEquals(lineString, anotherLineString);
    }

    @Test
    void multiLineStringTest() throws JsonProcessingException {
        // MultiLineString
        double[][][] expectedCoordinates = {{{1, 2}, {3, 4}, {5, 6}}, {{7, 8}, {9, 10}, {11, 12}}};
        MultiLineStringDto multiLineString = new MultiLineStringDto(expectedCoordinates);
        String json = MAPPER.writeValueAsString(multiLineString);
        assertEquals(multiLineString, MAPPER.readValue(json, MultiLineStringDto.class));

        // Another constructor
        MultiLineStringDto sameMultiLineString = new MultiLineStringDto(List.of(
            List.of(new CoordinatesDto(1, 2),
                new CoordinatesDto(3, 4),
                new CoordinatesDto(5, 6)),
            List.of(new CoordinatesDto(7, 8),
                new CoordinatesDto(9, 10),
                new CoordinatesDto(11, 12))));
        assertEquals(sameMultiLineString, multiLineString);
    }

    @Test
    void featureTest() throws JsonProcessingException {
        // LineString
        LineStringDto lineString = new LineStringDto(List.of(new CoordinatesDto(1, 2),
            new CoordinatesDto(3, 4),
            new CoordinatesDto(5, 6)));

        // Feature
        FeatureDto feature = new FeatureDto();
        feature.setGeometry(lineString);
        feature.setProperties(Map.of("IDR", "NHV1_NHV2_1"));
        String json = MAPPER.writeValueAsString(feature);
        assertEquals(feature, MAPPER.readValue(json, FeatureDto.class));
    }

    @Test
    void featureCollectionTest() throws JsonProcessingException {
        // LineString
        LineStringDto lineString = new LineStringDto(List.of(new CoordinatesDto(1, 2),
            new CoordinatesDto(3, 4),
            new CoordinatesDto(5, 6)));

        // Feature
        FeatureDto feature = new FeatureDto();
        feature.setGeometry(lineString);
        feature.setProperties(Map.of("IDR", "NHV1_NHV2_1"));

        // FeatureCollection
        FeatureCollectionDto featureCollection = new FeatureCollectionDto();
        featureCollection.setFeatures(List.of(feature));
        String json = MAPPER.writeValueAsString(featureCollection);
        assertEquals(featureCollection, MAPPER.readValue(json, FeatureCollectionDto.class));
    }
}
