/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.LinePositionAdder;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LinePositionAdderImpl<T extends Identifiable<T>> extends AbstractExtensionAdder<T, LinePosition<T>> implements LinePositionAdder<T> {

    private List<Coordinate> coordinates;

    public LinePositionAdderImpl(T extendable) {
        super(extendable);
    }

    @Override
    protected LinePosition<T> createExtension(T extendable) {
        return new LinePositionImpl<>(extendable, coordinates);
    }

    @Override
    public LinePositionAdder<T> withCoordinates(List<Coordinate> coordinates) {
        this.coordinates = Objects.requireNonNull(coordinates);
        return this;
    }
}
