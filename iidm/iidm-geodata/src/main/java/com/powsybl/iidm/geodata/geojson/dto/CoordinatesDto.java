/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class CoordinatesDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private double[] coordinates;

    public CoordinatesDto() {
        this.coordinates = new double[2];
    }

    @JsonCreator
    public CoordinatesDto(double[] coordinates) {
        this.coordinates = coordinates != null ? coordinates.clone() : new double[2];
    }

    public CoordinatesDto(double longitude, double latitude) {
        this.coordinates = new double[] {longitude, latitude};
    }

    public CoordinatesDto(double longitude, double latitude, double altitude) {
        this.coordinates = new double[] {longitude, latitude, altitude};
    }

    @JsonValue
    public double[] getCoordinatesArray() {
        return coordinates.clone();
    }

    public void setLongitude(double longitude) {
        this.coordinates[0] = longitude;
    }

    public double getLongitude() {
        return coordinates[0];
    }

    public void setLatitude(double latitude) {
        this.coordinates[1] = latitude;
    }

    public double getLatitude() {
        return coordinates[1];
    }

    public void setAltitude(double altitude) {
        if (coordinates.length < 3) {
            double[] newCoords = new double[3];
            System.arraycopy(coordinates, 0, newCoords, 0, coordinates.length);
            coordinates = newCoords;
        }
        this.coordinates[2] = altitude;
    }

    public double getAltitude() {
        return coordinates.length > 2 ? coordinates[2] : 0.0;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CoordinatesDto otherCoordinates) {
            return this.getLatitude() == otherCoordinates.getLatitude()
                && this.getLongitude() == otherCoordinates.getLongitude()
                && this.getAltitude() == otherCoordinates.getAltitude();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }
}
