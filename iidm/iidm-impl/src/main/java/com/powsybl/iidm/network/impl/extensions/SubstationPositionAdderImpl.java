/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SubstationPositionAdderImpl extends AbstractExtensionAdder<Substation, SubstationPosition> implements SubstationPositionAdder {

    private Coordinate coordinate;

    protected SubstationPositionAdderImpl(Substation substation) {
        super(substation);
    }

    @Override
    protected SubstationPosition createExtension(Substation substation) {
        return new SubstationPositionImpl(substation, coordinate);
    }

    @Override
    public SubstationPositionAdder withCoordinate(Coordinate coordinate) {
        this.coordinate = Objects.requireNonNull(coordinate);
        return this;
    }
}
