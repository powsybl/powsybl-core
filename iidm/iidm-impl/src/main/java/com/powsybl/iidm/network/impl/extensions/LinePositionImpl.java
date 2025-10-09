/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;

import java.util.List;
import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class LinePositionImpl<T extends Identifiable<T>> extends AbstractExtension<T> implements LinePosition<T> {

    private final List<Coordinate> coordinates;

    LinePositionImpl(T line, List<Coordinate> coordinates) {
        super(line);
        this.coordinates = Objects.requireNonNull(coordinates);
    }

    public LinePositionImpl(Line line, List<Coordinate> coordinates) {
        this((T) line, coordinates);
    }

    public LinePositionImpl(DanglingLine danglingLine, List<Coordinate> coordinates) {
        this((T) danglingLine, coordinates);
    }

    @Override
    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
}
