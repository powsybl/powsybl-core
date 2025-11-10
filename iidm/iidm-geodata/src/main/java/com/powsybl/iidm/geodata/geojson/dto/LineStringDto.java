/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson.dto;

import com.powsybl.iidm.network.extensions.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class LineStringDto extends AbstractGeometryDto {

    private List<Coordinate> coordinates;

    public LineStringDto() {
        this.coordinates = new ArrayList<>();
    }

    public LineStringDto(List<Coordinate> coordinatesDtoList) {
        this.coordinates = coordinatesDtoList;
    }

    public LineStringDto(double[][] coordinates) {
        this.coordinates = Arrays.stream(coordinates)
            .map(coords -> new Coordinate(coords[0], coords[1]))
            .collect(Collectors.toList());
    }

    public List<Coordinate> getCoordinates() {
        return List.copyOf(coordinates);
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LineStringDto otherLineString) {
            return compareCoordinates(coordinates, otherLineString.coordinates);
        }
        return false;
    }

    protected static boolean compareCoordinates(List<Coordinate> coordinates, List<Coordinate> otherCoordinates) {
        if (coordinates.size() != otherCoordinates.size()) {
            return false;
        }
        for (int i = 0; i < coordinates.size(); i++) {
            if (!coordinates.get(i).equals(otherCoordinates.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash("LineStringDto", coordinates);
    }
}
