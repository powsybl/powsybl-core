/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.geojson.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PointDto.class, name = "Point"),
    @JsonSubTypes.Type(value = LineStringDto.class, name = "LineString"),
    @JsonSubTypes.Type(value = MultiLineStringDto.class, name = "MultiLineString")
})
public abstract class AbstractGeometryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
