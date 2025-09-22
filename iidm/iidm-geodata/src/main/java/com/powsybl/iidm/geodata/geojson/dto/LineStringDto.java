/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class LineStringDto extends AbstractGeometryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<CoordinatesDto> coordinates;

    public LineStringDto() {
    }

    public LineStringDto(List<CoordinatesDto> coordinatesDtoList) {
        this.coordinates = coordinatesDtoList;
    }

    public LineStringDto(double[][] coordinates) {
        this.coordinates = new ArrayList<>();
        Arrays.asList(coordinates).forEach(c -> this.coordinates.add(new CoordinatesDto(c)));
    }

    public List<CoordinatesDto> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<CoordinatesDto> coordinates) {
        this.coordinates = coordinates;
    }

    @JsonIgnore
    public List<Coordinate> getCoordinateList() {
        return coordinates.stream().map(c -> new Coordinate(c.getLatitude(), c.getLongitude())).toList();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LineStringDto otherLineString) {
            return coordinates.equals(otherLineString.coordinates);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash("LineStringDto", coordinates);
    }
}
