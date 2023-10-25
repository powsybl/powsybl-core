/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Identifiable;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface LinePositionAdder<T extends Identifiable<T>>
        extends ExtensionAdder<T, LinePosition<T>> {

    @Override
    default Class<LinePosition> getExtensionClass() {
        return LinePosition.class;
    }

    LinePositionAdder<T> withCoordinates(List<Coordinate> coordinates);
}
