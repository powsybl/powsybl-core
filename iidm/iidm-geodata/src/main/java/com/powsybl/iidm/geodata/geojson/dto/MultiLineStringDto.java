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
public class MultiLineStringDto extends AbstractGeometryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<List<CoordinatesDto>> coordinates;

    public MultiLineStringDto() {
    }

    public MultiLineStringDto(List<List<CoordinatesDto>> coordinatesDtoList) {
        this.coordinates = coordinatesDtoList;
    }

    public MultiLineStringDto(double[][][] coordinates) {
        this.coordinates = new ArrayList<>();
        Arrays.asList(coordinates)
            .forEach(cList -> {
                List<CoordinatesDto> currentList = new ArrayList<>();
                Arrays.asList(cList).forEach(c -> currentList.add(new CoordinatesDto(c)));
                this.coordinates.add(currentList);
            });
    }

    public List<List<CoordinatesDto>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<CoordinatesDto>> coordinates) {
        this.coordinates = coordinates;
    }

    @JsonIgnore
    public List<Coordinate> getCoordinateList() {
        return coordinates.stream()
            .flatMap(List::stream)
            .map(c -> new Coordinate(c.getLatitude(), c.getLongitude())).toList();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MultiLineStringDto otherLineString) {
            return coordinates.equals(otherLineString.coordinates);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash("MultiLineStringDto", coordinates);
    }
}
