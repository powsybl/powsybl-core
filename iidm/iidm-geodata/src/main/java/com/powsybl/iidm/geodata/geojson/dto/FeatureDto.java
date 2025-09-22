/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Minimal GeoJSON feature DTO.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String type = "Feature";
    private AbstractGeometryDto geometry;
    private Map<String, String> properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AbstractGeometryDto getGeometry() {
        return geometry;
    }

    public void setGeometry(AbstractGeometryDto geometry) {
        this.geometry = geometry;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FeatureDto otherFeatureDto) {
            return geometry.equals(otherFeatureDto.geometry) && properties.equals(otherFeatureDto.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, geometry, properties);
    }
}
