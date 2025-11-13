/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson;

import com.powsybl.iidm.geodata.geojson.dto.LineStringDto;
import com.powsybl.iidm.geodata.geojson.dto.PointDto;
import com.powsybl.iidm.network.extensions.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GeoJsonDataParserTest {

    @Test
    void pointTest() {
        // Point
        PointDto point = new PointDto(1, 2);

        // Other constructor
        PointDto otherPoint = new PointDto(new Coordinate(1, 2));
        assertEquals(point, otherPoint);
        PointDto anotherPoint = new PointDto(new Coordinate(1, 3));
        assertNotEquals(point, anotherPoint);
    }

    @Test
    void lineStringTest() {
        // LineString
        LineStringDto lineString = new LineStringDto(List.of(new Coordinate(1, 2),
            new Coordinate(3, 4),
            new Coordinate(5, 6)));

        // Another constructor
        double[][] coordinates = {{1, 2}, {3, 4}, {5, 6}};
        LineStringDto otherLineString = new LineStringDto(coordinates);
        assertEquals(lineString, otherLineString);
        LineStringDto anotherLineString = new LineStringDto();
        anotherLineString.setCoordinates(List.of(new Coordinate(0, 2),
            new Coordinate(3, 4),
            new Coordinate(5, 6)));
        assertNotEquals(lineString, anotherLineString);
        anotherLineString.setCoordinates(List.of(new Coordinate(0, 2),
            new Coordinate(3, 4)));
        assertNotEquals(lineString, anotherLineString);
    }
}
