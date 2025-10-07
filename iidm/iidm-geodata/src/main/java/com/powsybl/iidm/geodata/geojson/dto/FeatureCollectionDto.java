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
import java.util.List;
import java.util.Objects;

/**
 * Minimal GeoJSON feature collection DTO.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollectionDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String type = "FeatureCollection";
    private List<FeatureDto> features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FeatureDto> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeatureDto> features) {
        this.features = features;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FeatureCollectionDto otherFeatureCollectionDto) {
            return features.equals(otherFeatureCollectionDto.features);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, features);
    }
}
