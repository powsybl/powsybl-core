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

class GeoJsonDataParserTest {

    @Test
    void serdeTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Point
        PointDto point = new PointDto(1, 2);
        String json = mapper.writeValueAsString(point);
        assertEquals(point, mapper.readValue(json, PointDto.class));

        // LineString
        LineStringDto lineString = new LineStringDto(List.of(new CoordinatesDto(1, 2),
            new CoordinatesDto(3, 4),
            new CoordinatesDto(5, 6)));
        json = mapper.writeValueAsString(lineString);
        assertEquals(lineString, mapper.readValue(json, LineStringDto.class));

        // MultiLineString
        MultiLineStringDto multiLineString = new MultiLineStringDto(List.of(
            List.of(new CoordinatesDto(1, 2),
                new CoordinatesDto(3, 4),
                new CoordinatesDto(5, 6)),
            List.of(new CoordinatesDto(7, 8),
                new CoordinatesDto(9, 10),
                new CoordinatesDto(11, 12))));
        json = mapper.writeValueAsString(multiLineString);
        assertEquals(multiLineString, mapper.readValue(json, MultiLineStringDto.class));

        // Feature
        FeatureDto feature = new FeatureDto();
        feature.setGeometry(lineString);
        feature.setProperties(Map.of("IDR", "NHV1_NHV2_1"));
        json = mapper.writeValueAsString(feature);
        assertEquals(feature, mapper.readValue(json, FeatureDto.class));

        // FeatureCollection
        FeatureCollectionDto featureCollection = new FeatureCollectionDto();
        featureCollection.setFeatures(List.of(feature));
        json = mapper.writeValueAsString(featureCollection);
        assertEquals(featureCollection, mapper.readValue(json, FeatureCollectionDto.class));
    }
}
