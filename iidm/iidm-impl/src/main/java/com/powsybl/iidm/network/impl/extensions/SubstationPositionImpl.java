/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class SubstationPositionImpl extends AbstractExtension<Substation> implements SubstationPosition {

    private final Coordinate coordinate;

    public SubstationPositionImpl(Substation substation, Coordinate coordinate) {
        super(substation);
        this.coordinate = Objects.requireNonNull(coordinate);
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }
}
