/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

import java.util.List;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public interface LinePosition<T extends Identifiable<T>> extends Extension<T> {

    String NAME = "linePosition";

    @Override
    default String getName() {
        return NAME;
    }

    List<Coordinate> getCoordinates();
}
