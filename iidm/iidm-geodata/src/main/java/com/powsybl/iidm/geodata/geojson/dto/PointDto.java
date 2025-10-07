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
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PointDto extends AbstractGeometryDto {

    @Serial
    private static final long serialVersionUID = 1L;

    private CoordinatesDto coordinates;

    public PointDto() {
        this.coordinates = new CoordinatesDto();
    }

    public PointDto(double longitude, double latitude) {
        this.coordinates = new CoordinatesDto(longitude, latitude);
    }

    public PointDto(double longitude, double latitude, double altitude) {
        this.coordinates = new CoordinatesDto(longitude, latitude, altitude);
    }

    public PointDto(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    @JsonIgnore
    public Coordinate getCoordinate() {
        return new Coordinate(coordinates.getLatitude(), coordinates.getLongitude());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PointDto otherPoint) {
            return coordinates.equals(otherPoint.coordinates);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash("PointDto", coordinates);
    }
}
