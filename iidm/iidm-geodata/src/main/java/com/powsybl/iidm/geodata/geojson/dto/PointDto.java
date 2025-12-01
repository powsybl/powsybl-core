/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson.dto;

import com.powsybl.iidm.network.extensions.Coordinate;

import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PointDto implements GeometryDto {

    private final Coordinate coordinates;

    public PointDto(double longitude, double latitude) {
        this.coordinates = new Coordinate(longitude, latitude);
    }

    public PointDto(Coordinate coordinates) {
        this.coordinates = coordinates;
    }

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
