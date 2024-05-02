/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.powsybl.iidm.geodata.utils.GeoShapeDeserializer;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.util.List;

/**
 * @author Hugo Marcellin {@literal <hugo.marcelin at rte-france.com>}
 */
@JsonDeserialize(using = GeoShapeDeserializer.class)
public record GeoShape(List<Coordinate> coordinates) { }
